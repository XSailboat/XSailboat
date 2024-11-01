package team.sailboat.commons.fan.http;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Map.Entry;

import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.collection.IMultiMap;
import team.sailboat.commons.fan.collection.SizeIter;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.XString;

public class CipherCoder implements ICoder
{
	public static final String sNamePrefix = "CipherCoder" ;
	
	static CipherCoder sDefault ;
	static CipherCoder sInstance_1 ;
	
	static final int RADIX = 16;
	/**
	 * Unreserved characters, i.e. alphanumeric, plus: {@code _ - ! . ~ ' ( ) *}
	 * <p>
	 *  This list is the same as the {@code unreserved} list in
	 *  <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC 2396</a>
	 */
	private static final BitSet UNRESERVED = new BitSet(256);
	/**
	 * Punctuation characters: , ; : $ & + =
	 * <p>
	 * These are the additional characters allowed by userinfo.
	 */
	private static final BitSet PUNCT = new BitSet(256);
	/** Characters which are safe to use in userinfo,
	 * i.e. {@link #UNRESERVED} plus {@link #PUNCT}uation */
	private static final BitSet USERINFO = new BitSet(256);
	/** Characters which are safe to use in a path,
	 * i.e. {@link #UNRESERVED} plus {@link #PUNCT}uation plus / @ */
	private static final BitSet PATHSAFE = new BitSet(256);
	/** Characters which are safe to use in a query or a fragment,
	 * i.e. {@link #RESERVED} plus {@link #UNRESERVED} */
	private static final BitSet URIC = new BitSet(256);

	/**
	 * Reserved characters, i.e. {@code ;/?:@&=+$,[]}
	 * <p>
	 *  This list is the same as the {@code reserved} list in
	 *  <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC 2396</a>
	 *  as augmented by
	 *  <a href="http://www.ietf.org/rfc/rfc2732.txt">RFC 2732</a>
	 */
	private static final BitSet RESERVED = new BitSet(256);

	/**
	 * Safe characters for x-www-form-urlencoded data, as per java.net.URLEncoder and browser behaviour,
	 * i.e. alphanumeric plus {@code "-", "_", ".", "*"}
	 */
	private static final BitSet URLENCODER = new BitSet(256);

	private static final BitSet PATH_SPECIAL = new BitSet(256);
	
	private static final BitSet HEADER_VAL = new BitSet(256) ;

	static
	{
		// unreserved chars
		// alpha characters
		for (int i = 'a'; i <= 'z'; i++)
		{
			UNRESERVED.set(i);
		}
		for (int i = 'A'; i <= 'Z'; i++)
		{
			UNRESERVED.set(i);
		}
		// numeric characters
		for (int i = '0'; i <= '9'; i++)
		{
			UNRESERVED.set(i);
		}
		UNRESERVED.set('_'); // these are the charactes of the "mark" list
		UNRESERVED.set('-');
		UNRESERVED.set('.');
		UNRESERVED.set('*');
		URLENCODER.or(UNRESERVED); // skip remaining unreserved characters
		UNRESERVED.set('!');
		UNRESERVED.set('~');
		UNRESERVED.set('\'');
		UNRESERVED.set('(');
		UNRESERVED.set(')');
		
		HEADER_VAL.or(UNRESERVED) ;
		HEADER_VAL.set(',') ;
		HEADER_VAL.set('/') ;
		HEADER_VAL.set(';') ;
		HEADER_VAL.set(':') ;
		HEADER_VAL.set('$') ;
		HEADER_VAL.set('+') ;
		HEADER_VAL.set('@') ;
		HEADER_VAL.set('=') ;
		
		// punct chars
		PUNCT.set(',');
		PUNCT.set(';');
		PUNCT.set(':');
		PUNCT.set('$');
		PUNCT.set('&');
		PUNCT.set('+');
		PUNCT.set('=');
		// Safe for userinfo
		USERINFO.or(UNRESERVED);
		USERINFO.or(PUNCT);

		// URL path safe
		PATHSAFE.or(UNRESERVED);
		PATHSAFE.set(';'); // param separator
		PATHSAFE.set(':'); // RFC 2396
		PATHSAFE.set('@');
		PATHSAFE.set('&');
		PATHSAFE.set('=');
		PATHSAFE.set('+');
		PATHSAFE.set('$');
		PATHSAFE.set(',');

		PATH_SPECIAL.or(PATHSAFE);
		PATH_SPECIAL.set('/');

		RESERVED.set(';');
		RESERVED.set('/');
		RESERVED.set('?');
		RESERVED.set(':');
		RESERVED.set('@');
		RESERVED.set('&');
		RESERVED.set('=');
		RESERVED.set('+');
		RESERVED.set('$');
		RESERVED.set(',');
		RESERVED.set('['); // added by RFC 2732
		RESERVED.set(']'); // added by RFC 2732

		URIC.or(RESERVED);
		URIC.or(UNRESERVED);
	}

	Charset mCharset = AppContext.sUTF8;
	boolean mBlankAsPlus = true;
	
	byte mDiff = 1 ;

	protected CipherCoder(boolean aBlankAsPlus)
	{
		mBlankAsPlus = aBlankAsPlus ;
	}
	
	@Override
	public String getName()
	{
		return sNamePrefix + (mDiff >= 0?"+"+mDiff:mDiff);
	}

	public String formatEncodeParams(IMultiMap<String, String> aParamMap)
	{
		if (XC.isEmpty(aParamMap))
			return "";
		StringBuilder strBld = new StringBuilder();
		for (Entry<String, String> entry : aParamMap.entrySet())
		{
			if (strBld.length() > 0)
				strBld.append('&');
			strBld.append(encodeParam(entry.getKey()));
			if (entry.getValue() != null)
				strBld.append('=').append(encodeParam(entry.getValue()));
		}
		return strBld.toString();
	}
	
	public String formatEncodeParams(IMultiMap<String, String> aParamMap , boolean aSort)
	{
		if (XC.isEmpty(aParamMap))
			return "";
		StringBuilder strBld = new StringBuilder();
		String[] keys = aParamMap.keySet().toArray(JCommon.sEmptyStringArray) ;
		Arrays.sort(keys);
		for (String key : keys)
		{
			SizeIter<String> sit = aParamMap.get(key) ;
			if (sit != null && sit.size()>0)
			{
				for(String val : aParamMap.get(key))
				{
					if (strBld.length() > 0)
						strBld.append('&');
					strBld.append(encodeParam(key));
					if(val != null)
						strBld.append('=').append(encodeParam(val));
				}
			}
		}
		return strBld.toString();
	}
	
	@Override
	public String encodeParam(String aParam)
	{
		return encode(aParam , URLENCODER , mBlankAsPlus , 0) ;
	}
	
	@Override
	public String encodeParamValue(String aParam)
	{
		return encode(aParam , URLENCODER , mBlankAsPlus , mDiff) ;
	}
	
	@Override
	public String encodeHeader(String aParam)
	{
		return encode(aParam , URLENCODER , true , 0) ;
	}
	
	@Override
	public String encodeHeaderValue(String aParam)
	{
		return encode(aParam , HEADER_VAL  , true , 0) ;
	}
	
	@Override
	public String splitEncodePath(String aPath)
    {
    	if(XString.isEmpty(aPath))
    		return aPath ;
    	Collection<String> segs = XString.split(aPath , '/') ;
    	StringBuilder strBld = new StringBuilder() ;
    	boolean first = true ;
    	for(String seg : segs)
    	{
    		if(first)
    			first = false ;
    		else
    			strBld.append('/') ;
    		strBld.append(encode(seg , PATHSAFE , false , 0)) ; 
    	}
    	return strBld.toString() ;
    }

	String encode(final String content, final BitSet safeChars, final boolean blankAsPlus
			, int aDiff)
	{
		if (content == null)
			return null;

		final StringBuilder buf = new StringBuilder();
		final ByteBuffer bb = mCharset.encode(content);
		while (bb.hasRemaining())
		{
			final int b = (bb.get() + aDiff) & 0xff;
			
			if (safeChars.get(b))
			{
				buf.append((char) b);
			}
			else if (blankAsPlus && b == ' ')
			{
				buf.append('+');
			}
			else
			{
				buf.append("%");
				final char hex1 = Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, RADIX));
				final char hex2 = Character.toUpperCase(Character.forDigit(b & 0xF, RADIX));
				buf.append(hex1);
				buf.append(hex2);
			}
		}
		return buf.toString();
	}
	
	@Override
	public DataOutputStream wrap(OutputStream aOuts)
	{
		return new _DataOutputStream(aOuts , mDiff) ;
	}
	
	public static CipherCoder getDefault()
	{
		if(sDefault == null)
			sDefault = new CipherCoder(true) ;
		
		return sDefault ;
	}
	
	/**
	 * 参数中的空格不转成“+”，而是进行编码
	 * @return
	 */
	public static CipherCoder getInstance_1()
	{
		if(sInstance_1 == null)
			sInstance_1 = new CipherCoder(false) ;
		
		return sInstance_1 ;
	}
	
	static class _DataOutputStream extends DataOutputStream
	{
		int mDiff ;

		public _DataOutputStream(OutputStream aOut , int aDiff)
		{
			super(aOut);
			mDiff = aDiff ;
		}
		
		@Override
		public synchronized void write(int aB) throws IOException
		{
			super.write((aB+mDiff) & 0xFF);
		}
	}
}
