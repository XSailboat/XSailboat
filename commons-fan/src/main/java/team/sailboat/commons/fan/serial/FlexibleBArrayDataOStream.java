package team.sailboat.commons.fan.serial;

import java.io.IOException;
import java.util.Arrays;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.struct.Bytes;

/**
 * 
 *
 * @author yyl
 * @version 1.0 
 * @since 2015-1-13
 */
public class FlexibleBArrayDataOStream extends FlexibleDataOutputStream
{
	protected Bytes mBuf ;
	protected int mPoint = 0 ;
	
	/**
	 * 初始容量为256B
	 */
	public FlexibleBArrayDataOStream()
	{
		this(256) ;
	}
	
	public FlexibleBArrayDataOStream(int aCapacity)
	{
		mBuf = new Bytes(aCapacity) ;
	}
	
	@Override
	public long point()
	{
		return mPoint ;
	}
	
	@Override
	public void skipTo(long aPos)
	{
		Assert.betweenL_R(0, size(), aPos) ;
		mPoint = (int)aPos ;
	}
	
	/**
	 * 返回缓存数据的原始byte数组，它的尾部可能有很多空余位置，需要结合size()方法使用
	 * @return
	 */
	public byte[] getSourceArray()
	{
		return mBuf.mData ;
	}

	@Override
	public void write(byte[] aB) throws IOException
	{
		if(XC.isNotEmpty(aB)) 
			write(aB, 0, aB.length) ;
	}
	
	@Override
	public void write(byte[] aB, int aOff, int aLen) throws IOException
	{
		if(XC.isEmpty(aB))
			return ;
		Assert.betweenL_r(0, aB.length, aOff);
		Assert.isTrue(aLen>0 && aOff+aLen<=aB.length);
		if(mPoint == size())
		{
			mBuf.add(aB, aOff, aLen) ;
		}
		else
		{
			mBuf.set(mPoint, aB , aOff , aLen);
		}
		mPoint += aLen ;
	}
	
	@Override
	public void write(int aB) throws IOException
	{
		if(mPoint == size())
		{
			mBuf.add((byte)aB) ;
			mPoint++ ;
		}
		else
			mBuf.set(mPoint++, (byte)aB) ;
	}
	
	@Override
	public long size()
	{
		return mBuf.mSize ;
	}
	
	/**
	 * 有效数据数组
	 * @return
	 */
	public byte[] toByteArray()
	{
		return Arrays.copyOf(mBuf.mData, mBuf.mSize) ;
	}
	
	public void reset()
	{
		mPoint = 0 ;
		mBuf.mSize = 0 ;
	}
}

