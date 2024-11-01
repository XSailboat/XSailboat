package team.sailboat.base.data;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;

import team.sailboat.base.dataset.ApiArg;
import team.sailboat.base.dataset.ApiClientType;
import team.sailboat.base.dataset.DatasetDesc_Sql;
import team.sailboat.base.dataset.DatasetDescriptor;
import team.sailboat.base.dataset.IDataset;
import team.sailboat.base.dataset.InParam;
import team.sailboat.base.dataset.Param;
import team.sailboat.base.dataset.ParamList;
import team.sailboat.base.dataset.Param_Aviator;
import team.sailboat.base.dataset.Param_InvokeApi;
import team.sailboat.base.def.WorkEnv;
import team.sailboat.base.ds.DataSource;
import team.sailboat.commons.fan.collection.AutoCleanHashMap;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.collection.AutoCleanHashMap.Bean;
import team.sailboat.commons.fan.event.IXListener;
import team.sailboat.commons.fan.http.IRestClient;
import team.sailboat.commons.fan.http.Request;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.md5.MD5;
import team.sailboat.commons.fan.serial.TLPrintStream;
import team.sailboat.commons.fan.text.XString;

/**
 * 开发环境和生产环境弄两个DataWorkshop，为后续开发和生产分成两个进程做准备
 *
 * @author yyl
 * @since 2023年6月2日
 */
public class Workshop
{
	static AutoCleanHashMap<String, Object> sDebugLogCache ;
	
	WorkEnv mWorkEnv ;
	
	String mWsId ;
	
	final AutoCleanHashMap<String, Object> mApiResultCache ;
	
	public Workshop(WorkEnv aWorkEnv)
	{
		mWorkEnv = aWorkEnv ;
		mApiResultCache = AutoCleanHashMap.withExpired_Created(10) ;
		mWsId = "" ;
	}
	
	public Workshop(String aWsId , WorkEnv aWorkEnv)
	{
		mWsId = aWsId ;
		mWorkEnv = aWorkEnv ;
		mApiResultCache = AutoCleanHashMap.withExpired_Created(10) ;
	}
	
	public WorkEnv getWorkEnv()
	{
		return mWorkEnv;
	}
	
	public Map<String, Object> initParamMap(DataSource aDs , IDataset aDataset, Map<String, Object> aInitParamMap
			, DebugContext aDebugCtx) throws Exception
	{
		DatasetDescriptor dsDesc = (DatasetDesc_Sql)aDataset.getDatasetDescriptor() ;
		Map<String , Object> ctxMap = XC.hashMap(aInitParamMap) ;
		// 根据InParam中定义的类型，对aParamMap进行处理，将单引号进行转义，对数值、Boolean进行类型转换
		List<InParam> inParamList = dsDesc.getInParams() ;
		int inParamCount = XC.count(inParamList) ;
		if(inParamCount > 0)
		{
			for(InParam inParam : inParamList)
			{
				Object value = ctxMap.get(inParam.getName()) ;
				if(value != null)
				{
					// 看看是什么类型，把它转过去
					ctxMap.put(inParam.getName() , XClassUtil.typeAdapt(value, inParam.getDataType())) ;
				}
				else
				{
					if(XString.isNotEmpty(inParam.getDefaultValue()))
					{
						ctxMap.put(inParam.getName() , XClassUtil.typeAdapt(inParam.getDefaultValue() , inParam.getDataType())) ;
					}
					else
					{
						Assert.isNotTrue(inParam.isRequired(), "必填参数[%s]没有指定，且没有示例值！" , inParam.getName()) ;
					}
				}
			}
		}
		// 处理前置表达式
		ParamList paramList = dsDesc.getParamList() ;
		if(paramList != null && paramList.isNotEmpty())
		{
			List<Param> params = paramList.getParams() ;
			if(paramList.hasAviator() && ctxMap == null)
			{
				ctxMap = XC.hashMap() ;
				if(aDebugCtx != null && mWorkEnv == WorkEnv.dev)
					aDebugCtx.initIfNot() ;
			}
			for(Param param : params)
			{
				if(param instanceof Param_Aviator)
				{
					Param_Aviator param_1 = (Param_Aviator)param ;
					Expression expr = AviatorEvaluator.compile(param_1.getExpr() , true) ;
					Object val = expr.execute(ctxMap) ;
					if(val != null)
						ctxMap.put(param_1.getName() , val) ;
				}
				else if(param instanceof Param_InvokeApi)
				{
					Param_InvokeApi param_1 = (Param_InvokeApi)param ;
					invokeApiAndInjectToCtxMap(param_1, ctxMap) ;
				}
			}
		}
		
		return ctxMap ;
	}
	
	protected Request constructDSRequest(Param_InvokeApi aParam)
	{
		Request request = Request.method(aParam.getHttpMethod())
				.path(aParam.getApiId()) ;					// 不用加workEnv，DSClient会给它加上
		return request ;
	}
	
	protected Request constructGWRequest(Param_InvokeApi aParam)
	{
//		JSONArray ja = mGatewayClient.askJa(Request.GET().path("/api-gateway/mng/ApiMapping/many/byId")
//				.queryParam("apiIds", aApiId)) ;
//		Assert.isNotTrue(ja.isEmpty() , "在API网关上查询不到API[%s] !" , aApiId);
//		ja.optJSONObject(0). ;
		
		Request request = Request.method(aParam.getHttpMethod())
				.path(aParam.getPath()) ;
		return request ;
	}
	
	protected static Object[] getApiArgValues(List<ApiArg> aApiArgs , Map<String, Object> aCtxMap)
	{
		if(aApiArgs == null)
			return null ;
		final int len = aApiArgs.size() ;
		if(len == 0)
			return JCommon.sEmptyObjectArray ;
		Object[] values = new Object[len] ;
		for(int i=0 ; i<len ; i++)
		{
			ApiArg arg = aApiArgs.get(i) ;
			if(!arg.hasParams())
			{
				values[i] = arg.getValue() ;
			}
			else
			{
				Map<String, Expression> exprMap = arg.getExprs() ;
				Map<String , Object> resultMap = XC.hashMap() ;
				for(Entry<String , Expression> entry : exprMap.entrySet())
				{
					resultMap.put(entry.getKey() , entry.getValue().execute(aCtxMap)) ;
				}
				values[i] = XString.format(arg.getValue() , resultMap) ;
			}
		}
		return values ;
	}
	
	protected Request setRequest(Request aRequest , List<ApiArg> aApiArgs , Object[] aArgs)
	{
		if(XC.isNotEmpty(aApiArgs))
		{
			int i=0 ;
			for(ApiArg arg : aApiArgs)
			{
				switch(arg.getType())
				{
				case Query:
					aRequest.queryParam(arg.getName() , XClassUtil.toString(aArgs[i])) ;
					break ;
				case Head:
					aRequest.header(arg.getName() , XClassUtil.toString(aArgs[i])) ;
					break ;
				default:
					break ;
				}
			}
		}
		return aRequest ;
	}
	
	String buildCacheKey(Param_InvokeApi aParam , Object[] aArgs)
	{
		StringBuilder keyBld = new StringBuilder(aParam.getApiId()) ;
		if(aParam.getClientType() == ApiClientType.Gateway)
		{
			keyBld.append("GW").append(mWsId) ;
		}
		keyBld.append(MD5.calMd5(XString.toString(",", aArgs))) ;
		return keyBld.toString() ;
	}
	
	public void cacheDebugLogs(String aKey , String aLogsStr)
	{
		mApiResultCache.put(aKey, aLogsStr.length() > 1000 ? aLogsStr.substring(0 , 1000) : aLogsStr) ;
	}
	
	public static DebugContext newDebugContext(String aReqId)
	{
		return new DebugContext(aReqId) ;
	}
	
	public static String getDebugLog(String aReqId)
	{
		return sDebugLogCache==null?null:(String)sDebugLogCache.get(aReqId) ;
	}
	
	public static class DebugContext
	{
		String reqId ;
		
		StringBuilder logBld ;
		
		IXListener lsn ;
		
		DebugContext(String aReqId)
		{
			reqId = aReqId ;
		}
		
		public void initIfNot()
		{
			logBld = new StringBuilder() ;
			lsn = TLPrintStream.wrapSysOut().addMessageListener(e
					-> logBld.append(e.getDescription())) ;
		}
		
		public void logDebug(String aMsg)
		{
			if(logBld != null)
				logBld.append(aMsg) ;
		}
		
		public void close()
		{
			if(lsn != null)
				TLPrintStream.removeSysOutListener(lsn) ;
			
			if(logBld != null && logBld.length() > 0)
			{
				if(sDebugLogCache == null)
				{
					synchronized ((getClass().getSimpleName()+".DebugLogCache").intern())
					{
						if(sDebugLogCache == null)
							sDebugLogCache = AutoCleanHashMap.withExpired_Created(10) ;
					}
				}
				sDebugLogCache.put(reqId , logBld.toString()) ;
			}
		}
	}
	
	public void invokeApiAndInjectToCtxMap(Param_InvokeApi aApiParam , Map<String , Object> aCtxMap) throws Exception
	{
		final int cacheTime = aApiParam.getCacheTime() ;
		String cacheKey = null ;		// 有的接口，可能参数相同，但不同应用调用，结果可能不同
		IRestClient client = null ;
		Request request = null ;
		Object[] argValues = getApiArgValues(aApiParam.getArgs() , aCtxMap) ;
		if(cacheTime >0)
		{
			cacheKey = buildCacheKey(aApiParam, argValues) ;
			Bean<Object> rbean = mApiResultCache.getBean(cacheKey) ;
			if(rbean != null)
			{
				if(System.currentTimeMillis() -  rbean.getCreatedTime() <= cacheTime)
				{
					aCtxMap.put(aApiParam.getName() , rbean.getEle()) ;
					return ;
				}
				else
					mApiResultCache.remove(cacheKey) ;
			}
		}
		switch(aApiParam.getClientType())
		{
		case Workspace:
		{
			client = (IRestClient)aCtxMap.get("_ds_client") ;
			// 在服务内部查询接口
			request = constructDSRequest(aApiParam) ;
			if(cacheTime > 0)
			{
				cacheKey  =  aApiParam.getApiId() ;
			}
		}
			break ;
		case Gateway:
			client = (IRestClient)aCtxMap.get("_gw_client") ;
			request = constructGWRequest(aApiParam) ;
			if(cacheTime > 0)
				cacheKey = aApiParam.getApiId()+"_GW_"+mWsId ;
			break ;
		default:
			throw new IllegalArgumentException("未支持的ClientType="+aApiParam.getClientType().name()) ;
		}
		Assert.notNull(client , "没有取得HttpClient!" ) ;
		Object result = client.ask(setRequest(request, aApiParam.getArgs() , argValues)) ;
		if(cacheTime > 0)
		{
			// 单位秒，缓存一下
			mApiResultCache.put(cacheKey , result) ;
		}
		aCtxMap.put(aApiParam.getName() , result) ;
	}
}
