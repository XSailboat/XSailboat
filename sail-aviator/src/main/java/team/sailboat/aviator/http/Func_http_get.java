package team.sailboat.aviator.http;

import java.util.Map;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorNil;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.http.IRestClient;
import team.sailboat.commons.fan.http.Request;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.XClassUtil;

public class Func_http_get extends AbstractFunction
{

	private static final long serialVersionUID = 1L;

	@Override
	public AviatorObject call(Map<String, Object> aEnv, AviatorObject aHttpClient 
			, AviatorObject aPath)
	{
		return call(aEnv, aHttpClient , aPath , AviatorNil.NIL) ;
	}
	
	@Override
	public AviatorObject call(Map<String, Object> aEnv, AviatorObject aHttpClient 
			, AviatorObject aPath
			, AviatorObject aQueryParams)
	{
		IRestClient client = (IRestClient)aHttpClient.getValue(aEnv) ;
		Assert.notNull(client , "无法取得Http客户端") ;
		Request req = Request.GET().path(XClassUtil.toString(aPath.getValue(aEnv))) ;
		@SuppressWarnings("rawtypes")
		Map<?, ?> queryMap = (Map)aQueryParams.getValue(aEnv) ;
		if(queryMap != null)
		{
			queryMap.forEach((k,v)->req.queryParam(XClassUtil.toString(k), XClassUtil.toString(v)
					, true)) ;
		}
		try
		{
			return AviatorRuntimeJavaType.valueOf(client.ask(req)) ;
		}
		catch (Exception e)
		{
			WrapException.wrapThrow(e) ;
			return AviatorNil.NIL ;				// dead code 
		}
	}

	@Override
	public String getName()
	{
		return "http.get" ;
	}
}
