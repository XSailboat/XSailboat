package team.sailboat.commons.fan.http;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.text.XString;

public class CipherDecoder implements IDecoder
{
	
	Charset mCharset = AppContext.sUTF8;
	
	int mDiff ;

	public CipherDecoder(int aDiff)
	{
		mDiff = aDiff ;
	}
	
	@Override
	public String decode(String aText)
	{
		if(XString.isEmpty(aText))
			return aText ;
		ByteBuffer bb = mCharset.encode(aText) ;
		final ByteBuffer buf = ByteBuffer.allocate(bb.limit()) ;
		while (bb.hasRemaining())
		{
			buf.put((byte)decode(bb.get())) ;
		}
		buf.rewind() ;
		return mCharset.decode(buf).toString() ;
	}
	
	@Override
	public final int decode(int aB)
	{
		return (aB - mDiff) & 0xFF ;
	}

	@Override
	public DataOutputStream wrap(OutputStream aOuts)
	{
		return null;
	}
	
}
