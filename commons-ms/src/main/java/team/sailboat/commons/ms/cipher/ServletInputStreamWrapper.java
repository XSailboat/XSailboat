package team.sailboat.commons.ms.cipher;

import java.io.IOException;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import team.sailboat.commons.fan.http.IDecoder;

public class ServletInputStreamWrapper extends ServletInputStream
{
	
	ServletInputStream mIns ;
	IDecoder mDecoder ;
	
	public ServletInputStreamWrapper(ServletInputStream aIns , IDecoder aDecoder)
	{
		mIns = aIns ;
		mDecoder = aDecoder ;
	}

	@Override
	public boolean isFinished()
	{
		return mIns.isFinished() ;
	}

	@Override
	public boolean isReady()
	{
		return mIns.isReady() ;
	}

	@Override
	public void setReadListener(ReadListener aListener)
	{
		mIns.setReadListener(aListener) ;
	}

	@Override
	public int read() throws IOException
	{
		return mDecoder.decode(mIns.read()) ;
	}

}
