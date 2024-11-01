package team.sailboat.commons.fan.serial;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.math.XMath;
import team.sailboat.commons.fan.sys.MemoryAssist;

public class FlexibleBufferedFileOStream extends FlexibleDataOutputStream
{
	ByteBuffer mBuf ;
	/**
	 * 包含
	 */
	long mBufStartPos ;
	/**
	 * 不包含
	 */
	int mBufSize ;
	
	FileChannel mChnl ;
	long mSize ;
	
	public FlexibleBufferedFileOStream(File aFile) throws IOException
	{
		this(aFile, MemoryAssist.multiKB(128)) ;
	}
	
	public FlexibleBufferedFileOStream(File aFile , boolean aAppend) throws IOException
	{
		this(aFile, MemoryAssist.multiKB(128) , aAppend) ;
	}
	
	public FlexibleBufferedFileOStream(File aFile , int aBufSize) throws IOException
	{
		this(aFile, aBufSize, false) ;
	}
	
	public FlexibleBufferedFileOStream(File aFile , int aBufSize , boolean aAppend) throws IOException
	{
		if(aAppend)
			mChnl = FileChannel.open(aFile.toPath() , StandardOpenOption.WRITE  , StandardOpenOption.READ
					, StandardOpenOption.CREATE) ;
		else
			mChnl = FileChannel.open(aFile.toPath() , StandardOpenOption.READ , StandardOpenOption.WRITE 
					, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING) ;
		mBuf = ByteBuffer.wrap(new byte[aBufSize<=0?1024:aBufSize]) ;
	}

	@Override
	public long point()
	{
		return mBufStartPos+mBuf.position() ;
	}

	@Override
	public void skipTo(long aPos) throws IOException
	{
		if(aPos>=0 && aPos<=mSize)
		{
			if(/*aPos != mSize && */ !XMath.inSpace_L_r(mBufStartPos, mBufStartPos+mBufSize, aPos))
				rebuf(aPos , -1);
			else
				mBuf.position((int)(aPos-mBufStartPos)) ;
		}
		else
			throw new IOException(String.format("不合法的位置 %1$d ，超出了数据长度 %2$d"
					, aPos , mSize)) ;
	}
	
	/**
	 * 
	 * @param aPos			认为aPos是一个合法的位置，方法内部不再做检查
	 * @param aRemain		在aPos之后保留的空闲位置，-1表示不做要求
	 * @throws IOException
	 */
	protected final void rebuf(long aPos , int aRemain) throws IOException
	{
		if(mBufSize>0)
		{
			flush();
			mBuf.rewind() ;
		}
		mBufStartPos = point()>aPos?aPos-mBuf.capacity()/2 :aPos-mBuf.capacity()/3 ;
		if(aRemain != -1 && mBuf.capacity()-(aPos-mBufStartPos)<aRemain)
		{
			mBufStartPos = aPos + Math.min(aRemain, mBuf.capacity())-mBuf.capacity() ; 
		}
		if(mBufStartPos<0)
			mBufStartPos = 0 ;
		int n = mChnl.read(mBuf, mBufStartPos) ;
		mBufSize = n <= 0?0:n ;
		mBuf.position((int)(aPos-mBufStartPos)) ;
	}

	@Override
	public long size() throws IOException
	{
		return mSize ;
	}

	@Override
	public void write(int aB) throws IOException
	{
		if(mBuf.position()<mBuf.capacity())
		{
			mBuf.put((byte)aB) ;
			if(mBuf.position()>mBufSize)
			{
				mBufSize++ ;
				mSize++ ;
			}
		}
		else
		{
			rebuf(point() , -1);
			write(aB);
		}
	}
	
	@Override
	public void flush() throws IOException
	{
		if(mBufSize<=0)
			return ;
		int prevLimit = mBuf.limit() ;
		mBuf.limit(mBufSize) ;
		int prevPos = mBuf.position() ;
		mBuf.position(0) ;
		mChnl.write(mBuf, mBufStartPos) ;
		mBuf.position(prevPos) ;
		mBuf.limit(prevLimit) ;
	}
	
	@Override
	public void write(byte[] aB, int aOff, int aLen) throws IOException
	{
		Assert.notNull(aB) ;
		Assert.isTrue(aB.length>=aOff+aLen && aOff>=0 && aLen>=0);
		if(aLen<=mBuf.remaining())
		{
			//容量够，直接写
			mBuf.put(aB , aOff , aLen) ;
			long delta = mBufStartPos+mBuf.position() - mSize ; 
			if(delta>0)
			{
				mSize += delta ;
				mBufSize = mBuf.position() ;
			}
		}
		else
		{
			int remain = mBuf.remaining() ;
			if(remain>0)
			{
				int pos = mBuf.position() ;
				mBuf.put(aB, aOff, remain) ;
				mBufSize = Math.max(pos + remain , mBufSize) ;
				aOff += remain ;
				aLen -= remain ;
			}
			flush();
			mBuf.rewind() ;
			mBufStartPos += mBuf.capacity() ;
			mBufSize = 0 ;
			while(aLen>=mBuf.capacity())
			{
				mBufSize = mBuf.capacity() ;
				mBuf.put(aB, aOff, mBufSize) ;
				aOff += mBufSize ;
				aLen -= mBufSize ;
				flush();
				mBuf.rewind() ;
				mBufStartPos += mBufSize ;
				mBufSize = 0 ;
			}
			if(aLen>0)
			{
				rebuf(mBufStartPos , aLen) ;
				mBuf.put(aB , aOff , aLen) ;
			}
			long delta = mBufStartPos+mBuf.position() - mSize ; 
			if(delta>0)
			{
				mSize += delta ;
				mBufSize = mBuf.position() ;
			}
		}
	}
	
	@Override
	public void close() throws IOException
	{
		flush(); 
		mChnl.close();
	}
}
