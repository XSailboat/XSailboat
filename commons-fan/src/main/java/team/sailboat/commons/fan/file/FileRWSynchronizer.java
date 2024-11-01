package team.sailboat.commons.fan.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.serial.FlexibleBufferedFileOStream;
import team.sailboat.commons.fan.serial.FlexibleDataOutputStream;

/**
 *
 * <strong>功能：</strong>
 * <p style="text-indent:2em">
 * 此类用在这样的情形：一个进程可能同时存在两个或两个以上的线程，某些线程读某个文件，而另外的某些线程写这个文件。
 * <p style="text-indent:2em">
 * 这个类主要是为了有序的读写文件。		<br>
 * 举例:一个进程有多个线程并发对某个文件 R1 R2 W1 W2 R3		<br>
 * 执行顺序:					<br>
 * 1.R1，R2					<br>
 * 2.W1						<br>
 * 3.W2						<br>
 * 4.R3						<br>
 * 这里的等待是公平的，先到先执行
 * @author yyl
 * @since 2017年2月15日
 */
public class FileRWSynchronizer
{
	static final int sRead = 1 ;
	static final int sIdle = 0 ;
	static final int sWrite = -1 ;
	
	/**
	 * 等于-1，表示打开了输出流
	 * 大于0，表示打开了n个输入流
	 * 
	 */
	static final Map<File , Bean> sBeanMap = new HashMap<>() ;
	static final Object sMutext = new Object() ;
	
	/**
	 * 当前线程是否可以结束等待，继续执行
	 */
	static ThreadLocal<Boolean> sCanGoOn = new ThreadLocal<>() ;
	
	/**
	 * 
	 * @param aFile
	 * @param aWait			当此文件有线程正在写，或者在此之前有线程正在等待写时，aWait=true，此线程将等待；
	 * 		aWait=false,立刻返回null
	 * @return
	 * @throws FileNotFoundException
	 */
	public static InputStream openInStream(File aFile , boolean aWait) throws FileNotFoundException
	{
		Assert.notNull(aFile) ;
		Bean bean = sBeanMap.get(aFile) ;
		if(bean == null)
		{
			synchronized(sMutext)
			{
				bean = sBeanMap.get(aFile) ;
				if(bean == null)
				{
					bean = new Bean(aFile) ;
					InputStream ins = new _FileInStream(aFile) ;
					bean.holdForRead();
					sBeanMap.put(aFile, bean) ;
					return ins ;
				}
			}
		}
		synchronized(bean)
		{
			if(bean.getState() == sWrite)
			{	//有其它线程正在写
				if(aWait)
				{							//堵塞当前线程
					bean.waitRead();
				}
				else
					return null ;			//aWait=false，返回null，不等待，也不打开输入流
			}
			InputStream ins = new _FileInStream(bean.mFile) ;
			bean.holdForRead();
			return ins ;
		}
	}
	
	public static OutputStream openOutStream(File aFile , boolean aWait) throws IOException
	{
		Assert.notNull(aFile) ;
		Bean bean = sBeanMap.get(aFile) ;
		if(bean == null)
		{
			synchronized(sMutext)
			{
				bean = sBeanMap.get(aFile) ;
				if(bean == null)
				{
					bean = new Bean(aFile) ;
					aFile.getParentFile().mkdirs() ;
					_FileOutStream outs = new _FileOutStream(aFile) ;
					bean.holdForWrite();
					sBeanMap.put(aFile, bean) ;
					return outs ;
				}
			}
		}
		synchronized(bean)
		{
			if(bean.getState() != sIdle)
			{	//有其它线程正在读或者写
				if(aWait)
				{							//堵塞当前线程
					bean.waitWrite();
				}
				else
					return null ;			//aWait=false，返回null，不等待，也不打开输入流
			}
			_FileOutStream outs = new _FileOutStream(bean.mFile) ;
			bean.holdForWrite();
			return outs ;
		}
	}
	
	/**
	 * 
	 * @param aFile
	 * @param aWait		当此文件有线程正在读或者写的时候，aWait=true，此线程将等待；
	 * 		aWait=false,立刻返回null
	 * @param aAppend	true表示追加，false表示清空写入
	 * @return
	 * @throws IOException 
	 */
	public static FlexibleDataOutputStream openOutStream_FlexibleData(File aFile , boolean aWait 
			, boolean aAppend) throws IOException
	{
		Assert.notNull(aFile) ;
		Bean bean = sBeanMap.get(aFile) ;
		if(bean == null)
		{
			synchronized(sMutext)
			{
				bean = sBeanMap.get(aFile) ;
				if(bean == null)
				{
					bean = new Bean(aFile) ;
					_FlexibleDataOutStream outs = new _FlexibleDataOutStream(aFile , aAppend) ;
					bean.holdForWrite();
					sBeanMap.put(aFile, bean) ;
					return outs ;
				}
			}
		}
		synchronized(bean)
		{
			if(bean.getState() != sIdle)
			{	//有其它线程正在读或者写
				if(aWait)
				{							//堵塞当前线程
					bean.waitWrite();
				}
				else
					return null ;			//aWait=false，返回null，不等待，也不打开输入流
			}
			_FlexibleDataOutStream outs = new _FlexibleDataOutStream(bean.mFile , aAppend) ;
			bean.holdForWrite();
			return outs ;
		}
	}
	
	/**
	 * 
	 * @param aFile
	 * @param aWait
	 * @return
	 */
	public static void monopolize(File aFile)
	{
		Assert.notNull(aFile) ;
		Bean bean = sBeanMap.get(aFile) ;
		if(bean == null)
		{
			synchronized(sMutext)
			{
				bean = sBeanMap.get(aFile) ;
				if(bean == null)
				{
					bean = new Bean(aFile) ;
					bean.holdForWrite();
					sBeanMap.put(aFile, bean) ;
					return ;
				}
			}
		}
		synchronized(bean)
		{
			if(bean.getState() != sIdle)
			{	
				if(bean.mOwnerThread == Thread.currentThread())
					return ;
				//有其它线程正在读或者写，堵塞当前线程
				bean.waitWrite();
			}
			bean.holdForWrite();
		}
	}
	
	public static boolean tryMonopolize(File aFile)
	{
		Assert.notNull(aFile) ;
		Bean bean = sBeanMap.get(aFile) ;
		if(bean == null)
		{
			synchronized(sMutext)
			{
				bean = sBeanMap.get(aFile) ;
				if(bean == null)
				{
					bean = new Bean(aFile) ;
					bean.holdForWrite();
					sBeanMap.put(aFile, bean) ;
					return true ;
				}
			}
		}
		synchronized(bean)
		{
			if(bean.getState() != sIdle && bean.mOwnerThread != Thread.currentThread())
				return false ;
			bean.holdForWrite();
		}
		return true ;
	}
	
	public static int release(File aFile)
	{
		Assert.notNull(aFile) ;
		Bean bean = sBeanMap.get(aFile) ;
		if(bean != null && bean.getState() != sIdle && bean.mOwnerThread == Thread.currentThread())
		{
			int rc = bean.release();
			if(rc == 0)
				bean.notifyIdle();
			return rc ;
		}
		return -1 ;
	}
	
	static class _FileInStream extends FileInputStream
	{
		File mFile ;
		AtomicBoolean mClosed ;
		
		public _FileInStream(File aFile) throws FileNotFoundException
		{
			super(aFile) ;
			mFile = aFile ;
			mClosed = new AtomicBoolean(false) ;
		}
		
		@Override
		public void close() throws IOException
		{
			if(mClosed.compareAndSet(false, true))
			{
				super.close();
				Bean bean = sBeanMap.get(mFile) ;
				if(bean.release() == sIdle)
					bean.notifyIdle();
			}
		}
		
	}
	
	static class _FlexibleDataOutStream extends FlexibleBufferedFileOStream
	{
		AtomicBoolean mClosed ;
		File mFile ;
		
		public _FlexibleDataOutStream(File aFile , boolean aAppend) throws IOException
		{
			super(aFile , aAppend) ;
			mFile = aFile ;
			mClosed = new AtomicBoolean(false) ;
		}
		
		@Override
		public void close() throws IOException
		{
			if(mClosed.compareAndSet(false, true))
			{
				super.close();
				Bean bean = sBeanMap.get(mFile) ;
				if(bean.release() == sIdle)
					bean.notifyIdle();
			}
		}
	}
	
	static class _FileOutStream extends FileOutputStream
	{
		AtomicBoolean mClosed ;
		File mFile ;
		
		public _FileOutStream(File aFile) throws IOException
		{
			super(aFile) ;
			mFile = aFile ;
			mClosed = new AtomicBoolean(false) ;
		}
		
		@Override
		public void close() throws IOException
		{
			if(mClosed.compareAndSet(false, true))
			{
				super.close();
				Bean bean = sBeanMap.get(mFile) ;
				if(bean.release() == sIdle)
					bean.notifyIdle();
			}
		}
	}
	
	private static class Bean
	{
		AtomicInteger mTag = new AtomicInteger(0) ;
		Queue<WaitBean> mWaitQueue = new LinkedList<>() ;
		Thread mOwnerThread ;
		File mFile ;
		
		public Bean(File aFile)
		{
			mFile = aFile ;
		}
		
		void holdForRead()
		{
			Assert.isTrue(mTag.get()>=0);
			mTag.incrementAndGet() ;
		}
		
		void holdForWrite()
		{
			Assert.isTrue(mTag.get()<=0);
			if(mOwnerThread == null || mOwnerThread == Thread.currentThread())
			{
				mTag.decrementAndGet() ;
				mOwnerThread = Thread.currentThread() ;
			}
			else
				throw new IllegalStateException("写保护锁已经被其它线程占有，试图获取写保护锁失败") ; 
		}
		
		int release()
		{
			if(mTag.get()>0)
			{
				return mTag.decrementAndGet() ;
			}
			else if(mTag.get()<0)
			{
				mOwnerThread = null ;
				return mTag.incrementAndGet() ;
			}
			return 0 ;
		}
		
		int getState()
		{
			int tag = mTag.get() ;
			return tag>0?sRead:(tag==0?sIdle:sWrite) ;
		}
		
		void waitRead()
		{
			_wait(true);
		}
		
		void waitWrite()
		{
			_wait(false);
		}
		
		synchronized void _wait(boolean aRead)
		{
			WaitBean waitBean = new WaitBean(aRead) ;
			mWaitQueue.offer(waitBean) ;
			do
			{
				try
				{
					wait(1000);
				}
				catch (InterruptedException e)
				{}
			}
			while(!waitBean.mGoOn) ;
		}
		
		synchronized void notifyIdle()
		{
			if(!mWaitQueue.isEmpty())
			{
				WaitBean waitBean = mWaitQueue.poll() ;
				waitBean.mGoOn = true ;
				if(waitBean.mWaitRead)
				{
					while(!mWaitQueue.isEmpty())
					{
						WaitBean waitBean_0 = mWaitQueue.peek() ;
						if(waitBean.mWaitRead == waitBean_0.mWaitRead)
						{
							mWaitQueue.poll() ;
							waitBean_0.mGoOn = true ;
						}
						else
							break ;
					}
				}
				notifyAll();
			}
			else
				sBeanMap.remove(mFile) ;
		}	
	}
	
	private static class WaitBean
	{
		/**
		 * true表示正在等待，准备读			<br>
		 * false表示正在等待，准备写			<br>
		 */
		boolean mWaitRead ;
		
		boolean mGoOn = false ;
		
		public WaitBean(boolean aWaitRead)
		{
			mWaitRead = aWaitRead ;
		}
	}
}
