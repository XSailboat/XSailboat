package team.sailboat.commons.fan.serial;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

public class FlexibleBInputStream extends ByteArrayInputStream implements Cloneable
{
	int mRangeStart ;
	
	boolean mClosed = false ;

	public FlexibleBInputStream(byte[] aBuf)
	{
		super(aBuf);
		mRangeStart = 0 ;
	}

	public FlexibleBInputStream(byte[] aBuf, int aOffset, int aLength)
	{
		super(aBuf, aOffset, aLength);
		mRangeStart = Math.max(aOffset, 0) ;
	}
	
	public byte[] getBufData()
	{
		if(mRangeStart == 0 && this.count == buf.length)
			return buf ;
		else
			return Arrays.copyOfRange(buf, mRangeStart, this.count) ;
	}
	
	public int getPosition()
	{
		return pos ;
	}
	
	public boolean position(int aPosition)
	{
		if(aPosition<mRangeStart || aPosition>this.count)
			return false ;
		pos = aPosition ;
		return true ;
	}

	public FlexibleBInputStream clone()
	{
		return new FlexibleBInputStream(buf, mRangeStart, this.count-mRangeStart) ;
	}
	
	@Override
	public void close() throws IOException
	{
		mClosed = true ;
		super.close();
	}
	
	public boolean isClosed()
	{
		return mClosed ;
	}
	
	public void rewind()
	{
		mClosed = false ;
		pos = 0 ;
		mark = 0 ;
	}
	
	public void reset(byte[] aData)
	{
		this.buf = aData;
		this.pos = 0;
		this.count = buf.length;
		this.mark = 0 ;
		mRangeStart = 0;
	}
}
