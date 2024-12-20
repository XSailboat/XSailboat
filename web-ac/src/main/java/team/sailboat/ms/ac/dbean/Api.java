package team.sailboat.ms.ac.dbean;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dpa.anno.BColumn;
import team.sailboat.commons.fan.dpa.anno.BDataType;
import team.sailboat.commons.fan.dpa.anno.BFeature;
import team.sailboat.commons.fan.dpa.anno.BTable;
import team.sailboat.commons.fan.dtool.DBType;
import team.sailboat.commons.fan.dtool.mysql.MySQLFeatures;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.ms.MSApp;

/**
 * 
 * 认证中心给ClientApp调用的接口声明
 *
 * @author yyl
 * @since 2024年10月30日
 */
@BTable(name="ac_api" , comment="API接口"  , features = {
		@BFeature(type = DBType.MySQL , name = MySQLFeatures.TABLE__ENGINE , value = "InnoDB") , 
		@BFeature(type = DBType.MySQL , name = MySQLFeatures.TABLE__CHARACTER_SET , value = "utf8")} ,
	id_category = "API" , id_prefix = "api"
)
@team.sailboat.dplug.anno.DBean(genBean = true , recordCreate = true , recordEdit = true)
public class Api
{
	@BColumn(name="id" , dataType = @BDataType(name="string" , length = 32) , comment="唯一性标识" , seq = 0 , primary = true)
	String id ;
	
	@BColumn(name="name" , dataType = @BDataType(name="string" , length = 128) , comment="Api名称" , seq = 1)
	String name ;
	
	@BColumn(name="description" , dataType = @BDataType(name="string" , length = 256) , comment="Api描述" , seq = 2)
	String description ;
	
	@BColumn(name="custom_description" , dataType = @BDataType(name="string" , length = 256) , comment="描述，人工设置的描述信息" , seq = 3)
	String customDescription ;
	
	@BColumn(name="method" , dataType = @BDataType(name="string" , length = 16) , comment="请求方法" , seq = 4)
	String method ;
	
	@BColumn(name="path" , dataType = @BDataType(name="string" , length = 256) , comment="路径" , seq = 5)
	String path ;
	
	@BColumn(name="params" , dataType = @BDataType(name="string" , length = 5120) , comment="参数定义表" , seq = 6)
	String params ;
	
	@BColumn(name="consumes" , dataType = @BDataType(name="string" , length = 128) , comment="请求的content-type" , seq = 7)
	String consumes ;
	
	@BColumn(name="produces" , dataType = @BDataType(name="string" , length = 128) , comment="响应的content-type" , seq = 8)
	String produces ;
	
	@BColumn(name="ext_attributes" , dataType = @BDataType(name="string" , length = 2048) , comment="附加信息" , seq = 9)
	String extAttributes ;
	
	JSONObject mJoParams ;
	
	RequestMappingInfo mRequestInfo ;
	
	public Api()
	{
	}
	
	public JSONObject getParamsInJSONObject()
	{
		if(mJoParams == null)
		{
			if(XString.isEmpty(params))
				mJoParams = new JSONObject() ;
			else
				mJoParams = JSONObject.of(params) ;
		}
		return mJoParams ;
	}
	
	public String[] getParamNames()
	{
		return getParamsInJSONObject().keyArray() ;
	}
	
	public boolean addOrUpdateParam(String aParamName , String aParamLoc , String aType , boolean aRequired
			, String aDefaultValue , String aDescription)
	{
		JSONObject jo = getParamsInJSONObject() ;
		JSONObject paramJo = jo.optJSONObject(aParamName) ;
		if(paramJo == null)
		{
			paramJo = new JSONObject().put("name", aParamName) ;
			jo.put(aParamName , paramJo) ;
		}
		paramJo.put("loc" , aParamLoc)
			.put("type" , aType)
			.put("required" , aRequired)
			.put("defaultValue" , aDefaultValue)
			.put("description", aDescription) ;
		String param = jo.toJSONString() ;
		if(JCommon.unequals(params, param))
		{
			Object oldValue = params ;
			params = param ;
			setChanged("params" , params , oldValue);
			return true ;
		}
		return false ;
	}
	public boolean removeParamByNames(String... aParamNames)
	{
		if(XC.isEmpty(aParamNames))
			return false ;
		JSONObject jo = getParamsInJSONObject() ;
		boolean changed = false ;
		for(String paramName : aParamNames)
		{
			changed |= jo.remove(paramName) != null ;
		}
		if(changed)
		{
			Object oldValue = params ;
			params = jo.toJSONString() ;
			setChanged("params", params, oldValue) ;
			return true ;
		}
		return false ;
	}
	public boolean clearParams()
	{
		if(mJoParams != null)
			mJoParams.clear() ;
		Object oldValue = params ;
		if(oldValue == null)
			oldValue = "" ;
		params = "" ;
		if(JCommon.unequals(params, oldValue))
		{
			setChanged("params", params, oldValue);
			return true ;
		}
		return false ;
	}
	
	public String[] getRequiredQueryParamNames()
	{
		JSONObject jo = getParamsInJSONObject() ;
		List<String> paramNames = XC.arrayList() ;
		for(String paramName : jo.keyArray())
		{
			JSONObject pjo = jo.optJSONObject(paramName) ;
			if(pjo.optBoolean("required" , false) && "query".equals(pjo.optString("loc")))
			{
				paramNames.add(paramName) ;
			}
		}
		return paramNames.toArray(JCommon.sEmptyStringArray) ;
	}
	
	public String[] getRequiredHeaderNames()
	{
		JSONObject jo = getParamsInJSONObject() ;
		List<String> paramNames = XC.arrayList() ;
		for(String paramName : jo.keyArray())
		{
			JSONObject pjo = jo.optJSONObject(paramName) ;
			if(pjo.optBoolean("required" , false) && "header".equals(pjo.optString("loc")))
			{
				paramNames.add(paramName) ;
			}
		}
		return paramNames.toArray(JCommon.sEmptyStringArray) ;
	}
	
	
	public RequestMappingInfo getRequestMappingInfo()
	{
		if(mRequestInfo ==null)
		{
			mRequestInfo = RequestMappingInfo.paths(MSApp.codePath(getPath()))
					.methods(RequestMethod.valueOf(getMethod()))
					.headers(getRequiredHeaderNames())
					.params(getRequiredQueryParamNames())
					.build() ;
		}
		return mRequestInfo ;
	}
}
