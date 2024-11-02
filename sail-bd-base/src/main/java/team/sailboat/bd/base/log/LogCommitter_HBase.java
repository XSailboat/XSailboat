package team.sailboat.bd.base.log;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.BufferedMutator;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Put;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import team.sailboat.bd.base.BdConst;
import team.sailboat.bd.base.hbase.HBaseUtils;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.excep.ExceptionAssist;
import team.sailboat.commons.fan.exec.CommonExecutor;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.struct.Tuples;
import team.sailboat.commons.fan.text.XString;

public class LogCommitter_HBase implements BdConst , ILogCommitter
{
	final List<Entry<String, JSONArray>> mLogCommitTasks = XC.arrayList() ;
	final Map<String, AtomicInteger> mTaskAmountMap = XC.concurrentHashMap() ;
	final Map<String, Runnable> mCndRunMap = XC.concurrentHashMap() ;
	final ReentrantLock mLock = new ReentrantLock() ;
	final Condition mCnd = mLock.newCondition() ;
	
	final Logger mLogger = LoggerFactory.getLogger(getClass()) ;
	
	String mName ;
	Connection mHBaseConn ;
	TableName mLogTableName ;
	BufferedMutator mLogTable ;
	
	boolean mClosed = false ;
	
	final AtomicLong mLastTime = new AtomicLong(0) ;
	
	final AtomicInteger mCount = new AtomicInteger(0) ;
	
	public LogCommitter_HBase(String aName , Connection aConn , TableName aLogTblName) 
	{
		mName = aName ;
		mHBaseConn = aConn ;
		mLogTableName = aLogTblName ;
	}
	
	/**
	 * 如果没有指定执行号相关的任务，aRun将会立刻执行，否则的话将加入到等待队列，等任务执行完之后再执行aRun
	 * @param aExecId
	 * @param aRun
	 */
	@Override
	public void delegateRun(String aExecId, Runnable aRunnable)
	{
		boolean runImmediately = false ;
		mLock.lock();
		try
		{
			int v = XC.get(mTaskAmountMap , aExecId , 0) ;
			if(v <= 0)
				runImmediately = true ;
			else
			{
				mLogger.info("委托的任务交给日志提交线程处理");
				mCndRunMap.put(aExecId, aRunnable) ;
			}
		}
		finally
		{
			mLock.unlock();
		}
		if(runImmediately)
		{
			mLogger.info("立即执行代理委托任务");
			aRunnable.run();
			
		}
	}
	
	@Override
	public void start()
	{
		// pytask提交到此服务的日志提交到HBase
		CommonExecutor.execInSelfThread(()->{
			final List<Entry<String, JSONArray>> taskList = XC.arrayList() ;
			final List<Put> putList = XC.arrayList() ;
			Map<String, AtomicInteger> commitAmountMap = XC.hashMap() ;
			while(!mClosed)
			{
				if(mLogTable == null)
				{
					try
					{
						mLogTable = mHBaseConn.getBufferedMutator(mLogTableName) ;
						mLogTable.setWriteBufferPeriodicFlush(500 , 500) ;
					}
					catch (IOException e)
					{
						mLogger.error(ExceptionAssist.getClearMessage(getClass(), e)) ;
						JCommon.sleep(1000) ;
						continue ;
					}
				}
				mLock.lock() ;
				try
				{
					while(mLogCommitTasks.isEmpty())
						mCnd.await(2 , TimeUnit.SECONDS) ;
					taskList.addAll(mLogCommitTasks) ;
					mLogCommitTasks.clear() ;
				}
				catch (InterruptedException e)
				{
					JCommon.sleepInSeconds(1) ;
					continue ;
				}
				finally
				{
					mLock.unlock() ;
				}
				for(Entry<String, JSONArray> entry : taskList)
				{
					String execId = entry.getKey() ;
					XC.addAndGet(commitAmountMap, execId, 1) ;
					JSONArray logsJa = entry.getValue() ;
					final int len = logsJa.size() ;
					for(int i=0 ; i<len ; i++)
					{
						JSONObject logJo = logsJa.optJSONObject(i) ;
						String timestamp = logJo.optString("timestamp") ;
						putList.add(new Put(HBaseUtils.toBytes(execId + "#" + timestamp))
								.addColumn(sHBase_FNB_logInfo , sHBase_CNB_body , HBaseUtils.toBytes(logJo.optString("log")))) ;
					}
				}
				taskList.clear() ;
				try
				{
					mLogTable.mutate(putList) ;
//					mLogger.info("往HBase的表{}中写入{}条数据" , mLogTableName.getNameAsString() , putList.size()) ;
				}
				catch (IOException e)
				{
					mLogger.error(ExceptionAssist.getClearMessage(getClass(), e)) ;
					continue ;
				}
				finally
				{
					// 不管它成功或失败
					for(Entry<String, AtomicInteger> entry : commitAmountMap.entrySet())
					{
						mLogger.info(entry.getKey()+"当前提交任务数"+entry.getValue().get()) ;
						if(XC.addAndGet(mTaskAmountMap, entry.getKey() , -entry.getValue().get()) == 0)
						{
							mLogger.info(entry.getKey()+"的日志已经提交完成") ;
							mTaskAmountMap.remove(entry.getKey()) ;
							Runnable run = mCndRunMap.remove(entry.getKey()) ;
							if(run != null)
							{
								CommonExecutor.exec(run) ;
								mLogger.info("由LogCommitter安排执行finish方法，提交数据至HBase") ;
							}
						}
					}
					commitAmountMap.clear() ;
				}
				putList.clear() ;
			}
		}, "日志提交至"+mLogTableName.getNameAsString() , Thread.NORM_PRIORITY , true) ;
		
		mLogger.info("日志保存到HBase中的表[{}]的线程已经启动" , mLogTableName.getNameAsString()) ;
	}
	
	public void submitLogs(String aExecId , JSONArray aLogsJa)
	{
		mLock.lock();
		try
		{
			mLogCommitTasks.add(Tuples.of(aExecId , aLogsJa)) ;
			XC.addAndGet(mTaskAmountMap, aExecId, 1) ;
			mCnd.signalAll() ;
		}
		finally
		{
			mLock.unlock();
		}
	}
	
	@Override
	public void submitLogs(String aExecId , String aPrefix , String... aLogs)
	{
		if(XC.isEmpty(aLogs))
			return ;
		long now = System.currentTimeMillis() ;
		long lastTime = mLastTime.get() ;
		if(lastTime < now && mLastTime.compareAndSet(lastTime, now))
		{
			mCount.set(0) ;
		}
		final int len = aLogs.length ;
		int seq = mCount.getAndAdd(len) ;
		JSONArray ja = new JSONArray() ;
		String timestr = Long.toString(now) ;
		for(int i=0 ; i<len ; i++)
		{
			String timekey = timestr + String.format("%03d", i+seq) ;
			ja.put(new JSONObject().put("timestamp", timekey)
					.put("log", XString.isNotEmpty(aPrefix)?aPrefix+aLogs[i]:aLogs[i])) ;
		}
		submitLogs(aExecId, ja) ;
	}
	
	@Override
	public String getName()
	{
		return mName ;
	}
	
	@Override
	public void close() throws IOException
	{
		mClosed = true ;
	}
}
