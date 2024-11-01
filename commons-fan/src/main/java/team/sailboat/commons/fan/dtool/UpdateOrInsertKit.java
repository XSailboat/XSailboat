package team.sailboat.commons.fan.dtool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.event.IXListener;
import team.sailboat.commons.fan.event.XEvent;
import team.sailboat.commons.fan.event.XListenerAssist;
import team.sailboat.commons.fan.exec.CommonExecutor;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.log.Log;
import team.sailboat.commons.fan.serial.StreamAssist;
import team.sailboat.commons.fan.struct.Wrapper;

public class UpdateOrInsertKit implements ICommitKit
{
	String mSql ;
	int mColAmount ;
	Connection mConn ;
	PreparedStatement mPStm ;
	PreparedStatement mPStmCommitting ;
	final ReentrantLock mLock = new ReentrantLock() ;
	final Condition mCommitCnd = mLock.newCondition() ;
	int mCommittingCount ;
	Wrapper<SQLException> mExcepWrapper ;
	IPStmSetter[] mSetters ;
	
	int mAutoCommitSize = 2_000 ;
	int mCacheSize = 0 ;
	int mCommitSize = 0 ;
	long mTotalCommitAmount = 0 ;
	AtomicLong mAddAmount = new AtomicLong(0) ;
	int mTipCount = 0 ;
	
	Object[] mLastRow ;
	final XListenerAssist mLsnAssist = new XListenerAssist() ;
	
	int mCommitTimeoutSeconds = -1 ;
	
	public UpdateOrInsertKit(String aSql , int...aColTypes)
	{
		mSql = aSql ;
		mColAmount = aColTypes.length ;
		mSetters = new IPStmSetter[mColAmount] ;
		for(int i=0 ; i<mColAmount ; i++)
		{
			if(aColTypes[i] == 0)
			{
				mSetters[i] = new PStmSetter_Object(i+1 , i) ;
			}
			else
			{
				switch(aColTypes[i])
				{
					case Types.VARCHAR:
					case Types.NVARCHAR:
					case Types.NCHAR:
					case Types.CHAR:
						mSetters[i] = new PStmSetter_String(i+1 , i) ;
		 				break ;
					case Types.BLOB:
					case Types.BINARY:
					case Types.VARBINARY:
						mSetters[i] = new PStmSetter_Bytes(i+1 , i) ;
						break ;
					case Types.DATE:
					case Types.TIME:
					case Types.TIMESTAMP:
						mSetters[i] = new PStmSetter_DateTime(i+1 , i , true) ;
						break ;
					case Types.BOOLEAN:
						mSetters[i] = new PStmSetter_Boolean(i+1 , i) ;
						break ;
					case Types.INTEGER:
						mSetters[i] = new PStmSetter_Integer(i+1 , i) ;
						break ;
					case Types.DECIMAL:
					case Types.NUMERIC:
					case Types.DOUBLE:
					case Types.FLOAT:
						mSetters[i] = new PStmSetter_Double(i+1 , i) ;
						break ;
					default:
						throw new IllegalStateException("还没有实现"+aColTypes[i]+"类型的PresparedStatement数据注入接口") ;
				}
			}
		}
	}
	
	public UpdateOrInsertKit(String aSql , String...aColTypes)
	{
		this(aSql , null , aColTypes) ;
	}
	
	public void setCommitTimeoutSeconds(int aCommitTimeoutSeconds)
	{
		mCommitTimeoutSeconds = aCommitTimeoutSeconds;
	}
	
	/**
	 * 
	 * @return		返回-1表示无超时限制
	 */
	public int getCommitTimeoutSeconds()
	{
		return mCommitTimeoutSeconds;
	}
	
	/**
	 * 
	 * @param aSql
	 * @param aColAmount
	 * @param aTypes		取XClassUtils.sCSN_*
	 */
	public UpdateOrInsertKit(String aSql , int[] aAddiArgIndex , String...aColTypes)
	{
		mSql = aSql ;
		mColAmount = aColTypes.length ;
		mSetters = new IPStmSetter[mColAmount + XC.count(aAddiArgIndex)] ;
		for(int i=0 ; i<mSetters.length ; i++)
		{
			int cellIndex = i ;
			if(i >= mColAmount)
			{
				cellIndex = aAddiArgIndex[i - mColAmount] ;
			}
			if(aColTypes[cellIndex] == null)
			{
				mSetters[i] = new PStmSetter_Object(i+1 , cellIndex) ;
			}
			else
			{
				switch(aColTypes[cellIndex])
				{
					case XClassUtil.sCSN_String:
						mSetters[i] = new PStmSetter_String(i+1 , cellIndex) ;
		 				break ;
					case XClassUtil.sCSN_Long:
						mSetters[i] = new PStmSetter_Long(i+1 , cellIndex) ;
						break ;
					case XClassUtil.sCSN_Integer:
						mSetters[i] = new PStmSetter_Integer(i+1 , cellIndex) ;
						break ;
					case XClassUtil.sCSN_Double:
						mSetters[i] = new PStmSetter_Double(i+1 , cellIndex) ;
						break ;
					case XClassUtil.sCSN_DateTime:
						mSetters[i] = new PStmSetter_DateTime(i+1 , cellIndex , true) ;
						break ;
					case XClassUtil.sCSN_Bool:
						mSetters[i] = new PStmSetter_Boolean(i+1 , cellIndex) ;
						break ;
					case XClassUtil.sCSN_Bytes:
						mSetters[i] = new PStmSetter_Bytes(i+1 , cellIndex) ;
						break ;
					default:
						throw new IllegalStateException("还没有实现"+aColTypes[cellIndex]+"类型的PresparedStatement数据注入接口") ;
				}
			}
		}
	}
	
	@Override
	public String getSql()
	{
		return mSql ;
	}
	
	/**
	 * Event里面的tag为0表示是提交动作之前的事件
	 * Event里面的tag为1表示是提交动作之后的事件
	 * @param aLsn
	 */
	public void addCommitListener(IXListener aLsn)
	{
		mLsnAssist.addListener(aLsn) ;
	}
	
	public synchronized void disposeResource()
	{
		mLock.lock() ;
		try
		{
			while(mCommittingCount > 0)
			{
				mCommitCnd.await(1 , TimeUnit.SECONDS) ;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace() ;
		}
		finally
		{
			mLock.unlock();
		}
		mConn = null ;	//此连接是外部传进来的，由外部代码释放
		if(mPStm != null)
		{
			StreamAssist.close(mPStm);
			mPStm = null ;
		}
		if(mPStmCommitting != null)
		{
			StreamAssist.close(mPStmCommitting);
			mPStmCommitting = null ;
		}
		
	}
	
	public void prepare(Connection aConn) throws SQLException
	{
		disposeResource();
		mConn = aConn ;
		mConn.setAutoCommit(false);
		mPStm = mConn.prepareStatement(mSql) ;
		mPStmCommitting = mConn.prepareStatement(mSql) ;
		mCommittingCount = 0 ;
		mExcepWrapper = new Wrapper<>() ;
	}
	
	public Connection getConnection()
	{
		return mConn ;
	}
	
	public boolean isPrepared()
	{
		return mPStm != null ;
	}
	
	public int getAutoCommitSize()
	{
		return mAutoCommitSize;
	}
	
	@Override
	public void setAutoCommitSize(int aAutoCommitSize)
	{
		mAutoCommitSize = aAutoCommitSize;
	}
	
	@Override
	public long add_0(Object...aColVals) throws SQLException
	{
		setStatement(mPStm, aColVals) ;
		mPStm.addBatch();
		if(++mCacheSize>=mAutoCommitSize)
		{
			Object[] colVals = XC.cloneArray(aColVals) ;
			mLsnAssist.notifyLsns(new XEvent(new CommitBrief(mTotalCommitAmount , mTotalCommitAmount+mCommitSize , colVals), 0));
			_commit(true , colVals);
		}
		return mAddAmount.incrementAndGet() ;
	}
	
	@Override
	public void setStatement(PreparedStatement aPstm , Object...aColVals) throws SQLException
	{
		Assert.isTrue(mColAmount == aColVals.length) ;
		for(IPStmSetter setter : mSetters)
			setter.set(aPstm, aColVals) ;
		mLastRow = aColVals ;
	}
	
	/**
	 * 确保同一时刻只有一个线程在异步提交
	 * @param aAsync
	 * @param aLastRow
	 * @throws SQLException
	 */
	private synchronized void _commit(boolean aAsync , final Object[] aLastRow) throws SQLException
	{
		if(mExcepWrapper.get() != null)
			throw mExcepWrapper.get() ;
		mLock.lock();
		try
		{
			while(mCommittingCount > 0)
				mCommitCnd.await(1 , TimeUnit.SECONDS) ;

			Assert.notNull(mPStm , "已释放资源，不能再用它提交数据！") ;
			
			PreparedStatement pstm_temp = mPStm ;
			mPStm = mPStmCommitting ;
			mPStmCommitting = pstm_temp ;
			mCommitSize = mCacheSize ;
			mCacheSize = 0 ;
			mCommittingCount++ ;
			Runnable run = ()->{
				mLock.lock();
				try
				{
//					Debug.clockOn();
					if(mCommitTimeoutSeconds > 0)
						mPStmCommitting.setQueryTimeout(mCommitTimeoutSeconds) ;
					mPStmCommitting.executeBatch() ;
//					Debug.clockOff("提交 %d 条记录", mCommitSize);
					long beginSeq = mTotalCommitAmount ;
					mTotalCommitAmount += mCommitSize ;
					mTipCount += mCommitSize ;
					if(mTipCount>=10000)
					{
						mTipCount = 0 ;
						Log.info("总共已经提交了 {} 条记录", mTotalCommitAmount);
					}
					mConn.commit(); 
					mPStmCommitting.clearBatch();
					mLsnAssist.notifyLsns(new XEvent(new CommitBrief(beginSeq , mTotalCommitAmount , aLastRow) ,1)) ;
				}
				catch (SQLException e)
				{
					mExcepWrapper.set(e) ;
				}
				finally
				{
					mCommittingCount-- ;
					mCommitCnd.signal();
					mLock.unlock();
				}
			} ;
			
			if(aAsync)
			{
				CommonExecutor.exec(run , true) ;
			}
			else
			{
				run.run(); ;
				if(mExcepWrapper.get() != null)
				{
					throw mExcepWrapper.get() ;
				}
			}
		}
		catch (InterruptedException e1)
		{
			throw new IllegalStateException(e1) ;
		}
		finally
		{
			mLock.unlock();
		}
	}
	
	public void commitImmediately() throws SQLException
	{
		commitImmediately(true) ;
	}
	
	public void commitImmediately(boolean aAsync) throws SQLException
	{
		if(mCacheSize>0)
		{
			mLsnAssist.notifyLsns(new XEvent(new CommitBrief(mTotalCommitAmount , mTotalCommitAmount+mCommitSize , mLastRow), 0));
			_commit(aAsync , Arrays.copyOf(mLastRow , mLastRow.length));
		}
	}
	/**
	 * 提交缓存着还没提交的数据，并且释放资源
	 * @return
	 * @throws SQLException
	 */
	public int finish() throws SQLException
	{
		if(mCacheSize>0)
		{
			mLsnAssist.notifyLsns(new XEvent(new CommitBrief(mTotalCommitAmount , mTotalCommitAmount+mCommitSize , mLastRow), 0));
			_commit(false , Arrays.copyOf(mLastRow , mLastRow.length));
		}
		disposeResource();
		return mCacheSize ;
	}
}
