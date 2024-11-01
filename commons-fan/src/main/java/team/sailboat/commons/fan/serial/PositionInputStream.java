package team.sailboat.commons.fan.serial;

import java.io.IOException;
import java.io.InputStream;

public class PositionInputStream extends InputStream
{
	InputStream mIns ;
	long mPosition = 0 ;
	
	public PositionInputStream(InputStream aIns)
	{
		mIns = aIns ;
	}

	@Override
	public int read() throws IOException
	{
		int v = mIns.read();
		mPosition++ ;
		return v ;
	}
	
	@Override
	public int read(byte[] aB, int aOff, int aLen) throws IOException
	{
		int n = mIns.read(aB, aOff, aLen);
		if(n > 0)
			mPosition += n ;
		return n ;
	}
	
	public long getPosition()
	{
		return mPosition ;
	}

	@Override
	public void close() throws IOException
	{
		mIns.close();
	}
	
	@Override
	public long skip(long aN) throws IOException
	{
		long num = super.skip(aN);
		mPosition += num ;
		return num ;
	}
}
