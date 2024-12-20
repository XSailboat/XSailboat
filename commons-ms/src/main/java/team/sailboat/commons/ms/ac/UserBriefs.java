package team.sailboat.commons.ms.ac ;

import java.util.Map;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import team.sailboat.commons.fan.collection.AutoCleanHashMap;
import team.sailboat.commons.fan.excep.ExceptionAssist;
import team.sailboat.commons.fan.http.HttpClient;
import team.sailboat.commons.fan.http.Request;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.ms.bean.UserBrief;

public class UserBriefs
{
	
	static final Logger sLogger = LoggerFactory.getLogger(UserBriefs.class) ;
	
	static UserBriefs sDefault ;
	
	Supplier<HttpClient> mClientPvd ;
	
	/**
	 * 创建出来5分钟以后就强制刷新
	 */
	Map<String, UserBrief> mUserMap = AutoCleanHashMap.withExpired_Created(1) ;
	
	Map<String, String> mDisplayNameMap = AutoCleanHashMap.withExpired_Created(1) ;
	
	/**
	 * 键是用户的真名，值是用户id
	 */
	Map<String, String> mRealNameMap = AutoCleanHashMap.withExpired_Created(1) ;
	
	public UserBriefs(Supplier<HttpClient> aClientPvd)
	{
		mClientPvd = aClientPvd ;
		if(sDefault == null)
			sDefault = this ;
	}
	
	/**
	 * 这个接口除非确定必要，确定已经授权，否则不要调用
	 * @param aRealName
	 * @return
	 * @throws Exception
	 */
	public UserBrief getUserByRealName(String aRealName) throws Exception
	{
		return getByRealName(aRealName, false) ;
	}

	/**
	 * 
	 * 这个接口除非确定必要，确定已经授权，否则不要调用
	 * 
	 * @param aRealName
	 * @return
	 * @throws Exception
	 */
	public UserBrief getOrCreateByRealName(String aRealName) throws Exception
	{
		return getByRealName(aRealName, true) ;
	}
	
	UserBrief getByRealName(String aRealName , boolean aCreateIfNotExists) throws Exception
	{
		String userId = mRealNameMap.get(aRealName) ;
		if(userId == null)
		{
			synchronized ((aRealName+".UserBrief".intern()))
			{
				userId = mRealNameMap.get(aRealName) ;
				if(userId == null)
				{
					JSONArray ja = mClientPvd.get().askJa(Request.GET().path("/foreign/user/brief/byRealName")
							.queryParam("realName" , aRealName)) ;
					UserBrief brief = null ;
					if(ja.isEmpty())
					{
						if(aCreateIfNotExists)
						{
							// 创建这个人
							userId = mClientPvd.get().askForString(Request.POST().path("/foreign/user/one")) ;
							brief = new UserBrief() ;
							brief.setId(userId) ;
							brief.setRealName(aRealName) ;
						}
						else
							return null ;
					}
					if(brief == null)
					{
						brief = UserBrief.of(ja.optJSONObject(0)) ;
						userId = brief.getId() ;
					}
					mRealNameMap.put(aRealName , userId) ;
					mUserMap.put(userId, brief) ;
					return brief ;
				}
			}
		}
		return getUser(userId) ;
	}
	
	public String getUserIdByForeignId(String aId)
	{
		return aId ;
	}
	
	public UserBrief getUser(String aId) throws Exception
	{
		UserBrief user = mUserMap.get(aId) ;
		if(user == null)
		{
			synchronized ((aId+".UserBrief").intern())
			{
				user = mUserMap.get(aId) ;
				if(user == null)
				{
					JSONObject jobj = (JSONObject)mClientPvd.get().ask(Request.GET().path("/foreign/user/brief/multi").queryParam("userIds", aId)) ;
					if(jobj == null)
						return null ;
					user = UserBrief.of(jobj) ;
					mUserMap.put(aId , user) ;
				}
			} 
		}
		return user ;
 	}
	
	public String getDisplayName(String aUserId)
	{
		if(XString.isEmpty(aUserId))
			return null ;
		if("__SYS__".equals(aUserId))
			return "系统" ;
		String name = mDisplayNameMap.get(aUserId) ;
		if(name == null)
		{
			synchronized ((aUserId+".displayName").intern())
			{
				name = mDisplayNameMap.get(aUserId) ;
				if(name == null)
				{
					HttpClient httpClient = mClientPvd.get() ;
					try
					{
						JSONObject jo = (JSONObject)httpClient.ask(Request.GET().path("/foreign/user/displayName/multi").queryParam("userIds", aUserId));
						name = jo.optString(aUserId) ;
					}
					catch (Exception e)
					{
						sLogger.error(ExceptionAssist.getClearMessage(getClass(), e , httpClient.toString())) ;
						name = "未知" ;
					}
					
					if(name == null)
						name = "" ;
					mDisplayNameMap.put(aUserId, name) ;
				}
			}
		}
		if(name.isEmpty())
			return null ;
		else
			return name ;
	}
	
	public static void setDefault(UserBriefs aDefault)
	{
		sDefault = aDefault;
	}
	
	public static UserBriefs getDefault()
	{
		return sDefault;
	}
}
