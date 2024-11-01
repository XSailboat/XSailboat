package team.sailboat.commons.fan.http;

import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.infc.EFunction2;
import team.sailboat.commons.fan.lang.Assert;

public class HttpRequestWrapper implements IRequestWrapper
{
	static Map<Class<?> , Map<String , EFunction2<Object, Object[], Object , Exception>>> sCls_MethodMap = new HashMap<>() ;
	
	Object mRequest ;
	EFunction2<Object, Object[], Object , Exception> mGetHeaderMethod = null ;
	EFunction2<Object, Object[], Object , Exception> mAddHeaderMethod = null ;
	EFunction2<Object, Object[], Object , Exception> mGetRemoteAddrMethod = null ;
	
	public HttpRequestWrapper(Object aRequest)
	{
		Assert.notNull(aRequest) ;
		mRequest = aRequest ;
		cacheMethods(); 
	}
	
	private void cacheMethods()
	{
		Class<?> clazz = mRequest.getClass() ;
 		Map<String , EFunction2<Object, Object[], Object , Exception>> map = sCls_MethodMap.get(clazz) ;
		if(map == null)
		{
			map = new HashMap<>() ;
			sCls_MethodMap.put(clazz , map) ;
		}
		else
		{
			mGetHeaderMethod = map.get("getHeader") ;
			mAddHeaderMethod = map.get("addHeader") ;
			mGetRemoteAddrMethod = map.get("getRemoteAddr") ;
		}
	}
	
	@Override
	public String getHeader(String aHeaderName)
	{
		if(mGetHeaderMethod == null)
		{
			Class<?> clazz = mRequest.getClass() ;
			try
			{
				Method method = clazz.getMethod("getHeader" ,String.class) ;
				mGetHeaderMethod = (source , args)->method.invoke(source, args) ;
			}
			catch (Exception e)
			{
				WrapException.wrapThrow(e) ; 
			}
			sCls_MethodMap.get(clazz)
					.put("getHeader", mGetHeaderMethod) ;
		}
		try
		{
			String val = (String)mGetHeaderMethod.apply(mRequest, new Object[] {aHeaderName}) ;
			return val != null?URLDecoder.decode(val , "UTF-8"):null ;
		}
		catch (Exception e)
		{
			WrapException.wrapThrow(e);
			return null ;						//dead code
		}
	}
	
	@Override
	public HttpRequestWrapper addHeader(String aHeaderName , String aValue)
	{
		if(mAddHeaderMethod == null)
		{
			Class<?> clazz = mRequest.getClass() ;
			try
			{
				Method method = clazz.getMethod("addHeader", String.class
						, String.class) ;
				mAddHeaderMethod = (source , args)->method.invoke(source, args) ;
			}
			catch (NoSuchMethodException | SecurityException e)
			{
				try
				{
					Method method = clazz.getMethod("addRequestHeader", String.class
							, String.class) ;
					mAddHeaderMethod = (source , args)->method.invoke(source, args) ;
				}
				catch (NoSuchMethodException | SecurityException e1)
				{
					WrapException.wrapThrow(e);
				}
			}
			sCls_MethodMap.get(clazz)
					.put("addHeader", mAddHeaderMethod) ;
		}
		try
		{
			mAddHeaderMethod.apply(mRequest, new Object[] {aHeaderName 
					, aValue!=null?URLEncoder.encode(aValue, "UTF-8"):null}) ;
		}
		catch (Exception e)
		{
			WrapException.wrapThrow(e);
		}
		return this ;
	}
	
	@Override
	public String getRemoteAddr()
	{
		if(mGetRemoteAddrMethod == null)
		{
			Class<?> clazz = mRequest.getClass() ;
			try
			{
				Method method = clazz.getMethod("getRemoteAddr") ;
				mGetRemoteAddrMethod = (source , args)->method.invoke(source, args) ;
			}
			catch (NoSuchMethodException | SecurityException e)
			{
				WrapException.wrapThrow(e);
			}
			sCls_MethodMap.get(clazz)
					.put("getRemoteAddr", mGetRemoteAddrMethod) ;
		}
		try
		{
			return (String) mGetRemoteAddrMethod.apply(mRequest , null) ;
		}
		catch (Exception e)
		{
			WrapException.wrapThrow(e); 
			return null ;						//dead code
		}
	}
}
