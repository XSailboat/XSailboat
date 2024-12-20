package team.sailboat.commons.ms.service ;

import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.ms.ac.UserBriefs;
import team.sailboat.commons.ms.infc.ICreateUserSupport;
import team.sailboat.commons.ms.infc.IUserSupport;

/**
 * 
 * 获取用户显示名相关的基础支持Service
 *
 * @author yyl
 * @since 2024年11月19日
 */
public class BaseSupportService
{
	
	@Autowired
	protected UserBriefs mUserBriefs ;
	

	protected JSONObject injectUserDisplayName(JSONObject aJo)
	{
		aJo.put("createUserDisplayName",  mUserBriefs.getDisplayName(aJo.optString("createUserId"))) ;
		aJo.put("lastEditUserDisplayName" , mUserBriefs.getDisplayName(aJo.optString("lastEditUserId"))) ;
		return aJo ;
	}
	
	protected JSONObject injectUserDisplayName(JSONObject aJo , Map<String, String> aFieldMap)
	{
		aJo.put("createUserDisplayName",  mUserBriefs.getDisplayName(aJo.optString("createUserId"))) ;
		aJo.put("lastEditUserDisplayName" , mUserBriefs.getDisplayName(aJo.optString("lastEditUserId"))) ;
		if(XC.isNotEmpty(aFieldMap))
		{
			for(Entry<String, String> entry : aFieldMap.entrySet())
			{
				aJo.put(entry.getValue() , mUserBriefs.getDisplayName(aJo.optString(entry.getKey()))) ;
			}
		}
		return aJo ;
	}
	
	protected JSONArray injectUserDisplayName(JSONArray aJa)
	{
		if(aJa == null)
			return null ;
		return aJa.forEachJSONObject(this::injectUserDisplayName) ;
	}
	
	protected JSONArray injectUserDisplayName(JSONArray aJa , Map<String, String> aFieldMap)
	{
		return aJa.forEachJSONObject((jo)->{
			injectUserDisplayName(jo , aFieldMap) ;
		}) ;
	}
	
	protected <T extends IUserSupport> T injectUserDisplayName(T aT)
	{
		if(aT != null)
		{
			aT.setCreateUserDisplayName(mUserBriefs.getDisplayName(aT.getCreateUserId())) ;
			aT.setLastEditUserDisplayName(mUserBriefs.getDisplayName(aT.getLastEditUserId())) ;
		}
		return aT ;
	}
	
	protected <T extends Iterable<? extends IUserSupport>> T injectUserDisplayName(T aT)
	{
		aT.forEach((us)->{
			us.setCreateUserDisplayName(mUserBriefs.getDisplayName(us.getCreateUserId())) ;
			us.setLastEditUserDisplayName(mUserBriefs.getDisplayName(us.getLastEditUserId())) ;
		});
		return aT ;
	}
	
	protected <T extends ICreateUserSupport> T injectUserDisplayName(T aT)
	{
		if(aT != null)
		{
			aT.setCreateUserDisplayName(mUserBriefs.getDisplayName(aT.getCreateUserId())) ;
		}
		return aT ;
	}
	
	protected <T extends Iterable<? extends ICreateUserSupport>> T injectCreateUserDisplayName(T aT)
	{
		aT.forEach((us)->{
			us.setCreateUserDisplayName(mUserBriefs.getDisplayName(us.getCreateUserId())) ;
		});
		return aT ;
	}
	
	protected JSONObject injectUserDisplayName(JSONObject aJo , boolean aContainsDefault , String...aUserIdFields)
	{
		Map<String, String> fieldMap = XC.hashMap() ;
		fieldMap.put("createUserId" , "createUserDisplayName") ;
		fieldMap.put("lastEditUserId" , "lastEditUserDisplayName" ) ;
		if(aUserIdFields != null)
		{
			for(String userIdField : aUserIdFields)
			{
				Assert.isTrue(userIdField.endsWith("UserId") , "指定的字段名应该以UserId结尾！") ;
				fieldMap.put(userIdField, userIdField.substring(0, userIdField.length()-2)+"DisplayName") ;
			}
		}
		return injectUserDisplayName(aJo, fieldMap) ;
	}
}
