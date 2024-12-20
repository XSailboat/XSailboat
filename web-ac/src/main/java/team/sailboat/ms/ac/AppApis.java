package team.sailboat.ms.ac;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.fest.reflect.core.Reflection;
import org.springdoc.webmvc.api.OpenApiResource;
import org.springframework.context.ConfigurableApplicationContext;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dpa.DRepository;
import team.sailboat.commons.fan.dpa.MapIndex;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.ms.ACKeys_Common;
import team.sailboat.commons.ms.MSApp;
import team.sailboat.commons.ms.ac.ApiAccessControlAspect;
import team.sailboat.commons.web.ac.IAuthCenterConst;
import team.sailboat.ms.ac.dbean.Api;

/**
 * 
 * 认证中心可供其它应用调用的接口声明
 *
 * @author yyl
 * @since 2024年10月10日
 */
public class AppApis
{
	static JSONArray sJa_ApiDefs ;
	
	public static JSONArray getApis()
	{
		if(sJa_ApiDefs == null)
		{
			sJa_ApiDefs = new JSONArray() ;
			injectOAuth2Apis(sJa_ApiDefs) ;
			injectOpenApis(sJa_ApiDefs) ;
		}
		return sJa_ApiDefs ;
	}
	
	static void injectOAuth2Apis(JSONArray aJa)
	{
		aJa.put(new JSONObject().put("name" , "Oauth2TokenPOST")
				.put("description" , "用临时授权码获取token")
				.put("method", "POST")
				.put("path" , MSApp.realPath(IAuthCenterConst.sGET_token))
				.put("params" , new JSONArray()
							.put(new JSONObject().put("name", "client_id")
									.put("loc", "query")
									.put("description" , "客户端的appKey")
									.put("required" , true)
									.put("type", "string"))
							.put(new JSONObject().put("name" , "client_secret")
									.put("loc", "query")
									.put("description", "客户端的appSecret。只有当服务端支持https协议时才设置，否则不设置")
									.put("required" , false)
									.put("type" , "string"))
							.put(new JSONObject().put("name" , "grant_type")
									.put("loc", "query")
									.put("description" , "授权模式，可取值：authorization_code")
									.put("required" , true)
									.put("type", "string"))
							.put(new JSONObject().put("name" , "code")
									.put("loc", "query")
									.put("description" , "临时授权码。当授权模式时authorization_code时，需要设置")
									.put("required" , false)
									.put("type", "string"))
							.put(new JSONObject().put("name" , "redirect_uri")
									.put("loc", "query")
									.put("description" , "验证通过之后，token返回的回调url。它必需在认证中心注册app的回调url声明列表中。")
									.put("required" , true)
									.put("type", "string")))
				.put("produces", new JSONArray().put("application/json")))
			.put(new JSONObject())
			;
	}
	
	static void injectOpenApis(JSONArray aJa)
	{
		// 获取openAPI
		ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) AppContext.get(ACKeys_Common.sSpringAppContext) ;
		OpenApiResource openApiResource = (OpenApiResource) ctx.getBean("openApiResource") ;
		Assert.notNull(openApiResource , "找不到openApiResource!") ;
		OpenAPI openApi = Reflection.method("getOpenApi")
				.withReturnType(OpenAPI.class)
				.withParameterTypes(Locale.class)
				.in(openApiResource)
				.invoke(Locale.getDefault()) ;
		
		Paths paths = openApi.getPaths() ;
		paths.forEach((path , pathItem)->{
			String apiNamePrefix = ApiAccessControlAspect.deflatePath(MSApp.codePath(path)) ;
			aJa.putIfNotNull(buildApiDescriptor(apiNamePrefix , path, pathItem.getGet() , HttpMethod.GET)) ;
			aJa.putIfNotNull(buildApiDescriptor(apiNamePrefix , path, pathItem.getPost() , HttpMethod.POST)) ;
			aJa.putIfNotNull(buildApiDescriptor(apiNamePrefix , path, pathItem.getPut() , HttpMethod.PUT)) ;
			aJa.putIfNotNull(buildApiDescriptor(apiNamePrefix , path, pathItem.getDelete() , HttpMethod.DELETE)) ;
			aJa.putIfNotNull(buildApiDescriptor(apiNamePrefix , path, pathItem.getPatch() , HttpMethod.PATCH)) ;
		});
	}
	
	static JSONObject buildApiDescriptor(String aApiNamePrefix , String aPath , Operation aOper , HttpMethod aMethod)
	{
		if(aOper == null
				|| !aOper.getTags().contains(AppConsts.sTagName_foreign))
			return null ;
		
		String apiName = aApiNamePrefix + aMethod.name() ;
		List<Parameter> params = aOper.getParameters() ;
		JSONArray jaParams = new JSONArray() ;
		if(XC.isNotEmpty(params))
		{
			for(Parameter param : params)
			{
				Schema<?> schema = param.getSchema() ;
				Assert.notNull(schema , "接口[%s-%s]的参数 %s 的类型为null！" , aPath , aMethod.name() , param.getName()) ;
				String dataType = JCommon.defaultIfEmpty(schema.getType() , XString.lastSeg_i(schema.get$ref() , '/' , 0)) ;
				jaParams.put(new JSONObject().put("name", param.getName())
						.put("loc", param.getIn())
						.put("description" , param.getDescription())
						.put("required" , param.getRequired())
						.put("type", dataType)) ;
			}
		}
		
		ApiResponse okResp = aOper.getResponses().get("200") ;
		Content okContent = okResp!=null?okResp.getContent():null ;
		
		return new JSONObject().put("name" , apiName)
				.put("description" , aOper.getDescription())
				.put("method", aMethod.name())
				.put("path" , aPath)
				.put("params" , jaParams)
				.putIf(okContent != null ,  "produces", ()->okContent.keySet())
			;
	}
	
	public static void syncApis(DRepository aRepo , JSONArray aJa_Apis)
	{
		MapIndex<Api> mapIndex = aRepo.mapIndex(Api.class, "name") ;
		final int len = aJa_Apis.size() ;
		Date now = new Date() ;
		Set<String> apiIds = XC.hashSet() ;
		for(int i=0 ; i<len ; i++)
		{
			JSONObject jo = aJa_Apis.optJSONObject(i) ;
			String name = jo.optString("name") ;
			if(XString.isEmpty(name))
				continue ;
			Api api = mapIndex.getFirst(name) ;
			if(api == null)
			{
				// 新建
				api = aRepo.newBean(Api.class) ;
				api.setName(name) ;
				api.setCreateTime(now) ;
			}
			boolean changed = api.setDescription(jo.optString("description")) ;
			changed |= api.setMethod(jo.optString("method")) ;
			changed |= api.setPath(jo.optString("path")) ;
			changed |= api.setConsumes(jo.optString("consumes")) ;
			changed |= api.setProduces(jo.optString("produces")) ;
			JSONArray jaParams = jo.optJSONArray("params") ;
			Set<String> newParamNames = XC.hashSet() ;
			if(jaParams != null && jaParams.size() >0)
			{
				final int len_p = jaParams.size() ;
				for(int j=0 ; j<len_p ; j++)
				{
					JSONObject joParam = jaParams.optJSONObject(j) ;
					String paramName = joParam.optString("name") ;
					newParamNames.add(paramName) ;
					changed |= api.addOrUpdateParam(paramName, joParam.optString("loc") , joParam.optString("type") 
							, joParam.optBoolean("required") , joParam.optString("defaultValue") ,joParam.optString("description")) ;
				}
			}
			String[] oldParamNames = api.getParamNames() ;
			if(XC.isNotEmpty(oldParamNames))
			{
				Set<String> oldParamNameSet = XC.hashSet(oldParamNames) ;
				oldParamNameSet.removeAll(newParamNames) ;
				if(!oldParamNameSet.isEmpty())
					changed |= api.removeParamByNames(oldParamNameSet.toArray(JCommon.sEmptyStringArray)) ;
			}
			if(changed)
				api.setLastEditTime(now) ;
			apiIds.add(api.getId()) ;
		}
		// 删除多余的api
		List<String> removeIds = XC.arrayList() ;
		aRepo.forEach(Api.class, (api)->{
			if(!apiIds.contains(api.getId()))
				removeIds.add(api.getId()) ;
			return true ;
		});
		if(!removeIds.isEmpty())
			aRepo.deleteAll(Api.class, removeIds.toArray(JCommon.sEmptyStringArray)) ;
	}
}
