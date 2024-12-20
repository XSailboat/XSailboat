package team.sailboat.commons.fan.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.collection.HashMultiMap;
import team.sailboat.commons.fan.collection.IMultiMap;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.excep.HttpException;
import team.sailboat.commons.fan.excep.RestApiException;
import team.sailboat.commons.fan.json.JSONException;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.serial.StreamAssist;
import team.sailboat.commons.fan.sys.MemoryAssist;
import team.sailboat.commons.fan.text.XString;

public class HttpEnvelop
{
	public static final String sMIME_XML = "text/xml" ;
	public static final String sMIME_JSON = "application/json" ;
	public static final String sMIME_PLAIN = "text/plain" ;
	public static final String sMIME_OCTET_STREAM = "application/octet-stream" ;
	
	/**
	 * 能转化成文本的MIME类型
	 */
	static Set<String> sTextMIMESet = new HashSet<>() ;
	static Pattern sPtn_ContentEncoding = Pattern.compile("charset *= *([\\w\\-]+)") ;
	static
	{
		sTextMIMESet.add("text/html") ;
		sTextMIMESet.add("application/x-javascript") ;
		sTextMIMESet.add(sMIME_XML) ;
		sTextMIMESet.add(sMIME_JSON) ;
		sTextMIMESet.add(sMIME_PLAIN) ;
	}
	
	String mRequestURL ;
	String mRequestMethod ;
	int mReplyCode ;
	IMultiMap<String , String> mRequestHeadersMap = new HashMultiMap<>() ;
	IMultiMap<String , String> mResponseHeadersMap = new HashMultiMap<>() ;
	byte[] mSourceData ;
	String mContentType ;
	int mContentLength ;
	String mContentEncoding ;
	String mContent ;
	
	InputStream mHttpInputStream ;
	
	public HttpEnvelop()
	{}
	
	HttpEnvelop(HttpURLConnection aHttpConn , IMultiMap<String , String> aHeaderMap , int aReplyCode) throws IOException
	{
		mReplyCode = aReplyCode ;
//		mReplyCode = aHttpConn.getResponseCode() ;
		if(aHeaderMap != null && !aHeaderMap.isEmpty())
			mRequestHeadersMap.putAll(aHeaderMap);
		mRequestMethod = aHttpConn.getRequestMethod() ;
		mRequestURL = aHttpConn.getURL().toString() ;
		Map<String , List<String>> headerMap = aHttpConn.getHeaderFields() ;
		if(headerMap != null && !headerMap.isEmpty())
		{
			for(Entry<String, List<String>> entry : headerMap.entrySet())
			{
				List<String> valueList = entry.getValue() ;
				if(!valueList.isEmpty())
				{
					final int len = valueList.size() ;
					for(int i=0 ; i<len ; i++)
					{
						String value = valueList.get(i) ;
						if(XString.isNotEmpty(value))
						{
							mResponseHeadersMap.put(entry.getKey() , URLDecoder.decode(value , "UTF-8")) ;
						}
						else
							mResponseHeadersMap.put(entry.getKey() , value) ;
					}
				}
				else
					mResponseHeadersMap.putAll(entry.getKey(), entry.getValue()) ;
			}
		}
		mContentType = aHttpConn.getContentType() ;
		mContentLength = aHttpConn.getContentLength() ;
		mContentEncoding = aHttpConn.getContentEncoding() ;
		if(mContentEncoding == null && mContentType != null)
		{
			String[] segs = mContentType.split(";") ;
			mContentType = null ;
			for(String seg : segs)
			{
				Matcher matcher = sPtn_ContentEncoding.matcher(seg.trim()) ;
				if(matcher.matches())
					mContentEncoding = matcher.group(1) ;
				else
					mContentType = seg.trim() ;
			}
		}
		boolean throwError = false ;
		if(!sMIME_OCTET_STREAM.equals(mContentType)
				&& !MediaType.TEXT_EVENT_STREAM_VALUE.equals(mContentType))
		{
			InputStream ins = null ;
			if(HttpUtils.isError(mReplyCode))
			{
				ins = aHttpConn.getErrorStream() ;
				throwError = true ;
			}
			else
			{
				ins = aHttpConn.getInputStream() ;
			}
			if(ins != null)
			{
				try
				{
					ByteArrayOutputStream bouts = new ByteArrayOutputStream(MemoryAssist.multiKB(512)) ;
					byte[] buf = new byte[MemoryAssist.multiKB(100)] ;
					int len = 0 ;
					while((len = ins.read(buf)) != -1)
						bouts.write(buf, 0, len);
					mSourceData = bouts.toByteArray() ;
				}
				finally
				{
					StreamAssist.close(ins) ;
				}
				if(isText() || mContentType == null)
				{
					mContent = new String(mSourceData, mContentEncoding==null?"UTF-8":mContentEncoding) ;
				}
			}
			else
			{
				// 返回错误码时，aHttpConn.getInputStream()是不能用的
				mContent = aHttpConn.getResponseMessage() ;
			}
		}
		else
			mHttpInputStream = aHttpConn.getInputStream() ;
		if(throwError)
		{
			String msg = mContent ;
			String detail = aHttpConn.getHeaderField("x-error-detail") ;
			if(XString.isNotEmpty(detail))
			{
				msg += " - "+new String(XString.toBytesOfHex(detail) , AppContext.sUTF8) ;
			}
			if(XString.isNotEmpty(msg) && msg.charAt(0) == '{')
			{
				try
				{
					JSONObject msgJo = JSONObject.of(msg) ;
					if(msgJo.has("message") && msgJo.has("rootExceptionClass"))
					{
						RestApiException.createAndThrow(aHttpConn.getRequestMethod() , aHttpConn.getURL()
								, mReplyCode , msgJo.optString("message")
								, msgJo.optString("rootExceptionClass"), new Date(msgJo.optLong("timestamp"))) ;
					}
				}
				catch (JSONException e)
				{}
			}
			HttpException.createAndThrow(aHttpConn.getRequestMethod() , aHttpConn.getURL() , mReplyCode,  msg);
		}
	}
	
	public int getContentLength()
	{
		return mContentLength;
	}
	
	public InputStream getInputStream()
	{
		if(mSourceData == null && (sMIME_OCTET_STREAM.equals(mContentType)
				|| MediaType.TEXT_EVENT_STREAM_VALUE.equals(mContentType)))
			return mHttpInputStream ;
		else if(mSourceData != null)
			return new ByteArrayInputStream(mSourceData) ;
		else
			return null ;
	}
	
	public int getReplyCode()
	{
		return mReplyCode;
	}
	
	public void setReplyCode(int aReplyCode)
	{
		mReplyCode = aReplyCode;
	}
	
	public IMultiMap<String, String> getResponseHeadersMap()
	{
		return mResponseHeadersMap;
	}
	
	public IMultiMap<String , String> getRequestHeadersMap()
	{
		return mRequestHeadersMap;
	}
	
	public String getContentType()
	{
		return mContentType;
	}
	
	public boolean isText()
	{
		return sTextMIMESet.contains(mContentType) ;
	}
	
	public boolean isXML()
	{
		return sMIME_XML.equalsIgnoreCase(mContentType) ;
	}
	
	public boolean isJSON()
	{
		return sMIME_JSON.equalsIgnoreCase(mContentType) ;
 	}
	
	/**
	 * 如果
	 * @return
	 * @throws IOException 
	 */
	public String getContent() throws IOException
	{
		if(mContentLength == 0)
			return "" ;
		if(mContent == null && mContentType != null)
			throw new IOException("不支持转成文本的MIME类型"+mContentType) ;
		return mContent ;
	}
	
	public byte[] getSourceData()
	{
		return mSourceData;
	}
	
	@Override
	public String toString()
	{
		String indent = "  " ;
		StringBuilder strBld = new StringBuilder() ;
		strBld.append("[General]").append(XString.sLineSeparator) ;
		strBld.append(indent).append("Request URL: ").append(mRequestURL).append(XString.sLineSeparator)
				.append(indent).append("Request Method: ").append(mRequestMethod).append(XString.sLineSeparator)
				.append(indent).append("Status Code: ").append(mReplyCode).append(XString.sLineSeparator) ;
		
		strBld.append("[Response Headers]").append(XString.sLineSeparator) ;
		if(!mResponseHeadersMap.isEmpty())
		{
			for(String headerName : mResponseHeadersMap.keySet())
			{
				strBld.append(indent).append(headerName).append(": ")
						.append(XString.toString(" | ", mResponseHeadersMap.get(headerName)))
						.append(XString.sLineSeparator) ;
			}
		}
		
		strBld.append("[Request Headers]").append(XString.sLineSeparator);
		if(!mRequestHeadersMap.isEmpty())
		{
			for(String headerName : mRequestHeadersMap.keySet())
			{
				strBld.append(indent).append(headerName).append(": ")
						.append(XString.toString(" | ", mRequestHeadersMap.get(headerName))) 
						.append(XString.sLineSeparator) ;
			}
		}
		strBld.append("[Content]").append(XString.sLineSeparator) ;
		if(isText())
			strBld.append(indent).append(mContent) ;
		else
			strBld.append("二进制数据，长度为").append(XC.count(mSourceData)).append("字节") ;
		
		return strBld.toString() ;
 	}
}
