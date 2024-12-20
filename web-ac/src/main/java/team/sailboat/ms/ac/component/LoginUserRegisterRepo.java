package team.sailboat.ms.ac.component;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.exec.AutoCleaner;
import team.sailboat.commons.fan.exec.DefaultAutoCleaner;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.ms.ac.data.LoginAppRecord;

/**
 * 已登陆用户的信息缓存表
 *
 * @author yyl
 * @since 2021年11月9日
 */
@Component
public class LoginUserRegisterRepo
{
	/**
	 * 键是用户id , 第2个键是appId
	 */
	final Map<String, Map<String , LoginAppRecord>> mLoginRcdMap = XC.concurrentHashMap() ;
	
	AutoCleaner mCleaner ;
	
	public LoginUserRegisterRepo()
	{
	}
	
	@PostConstruct
	void _init()
	{
		mCleaner = new DefaultAutoCleaner(600 , ()->{
			String[] userIds = mLoginRcdMap.keySet().toArray(JCommon.sEmptyStringArray) ;
			if(XC.isNotEmpty(userIds))
			{
				for(String userId : userIds)
				{
					Map<String, LoginAppRecord> rcdMap = mLoginRcdMap.get(userId) ;
					if(!rcdMap.isEmpty())
					{
						LoginAppRecord[] rcds = rcdMap.values().toArray(new LoginAppRecord[0]) ;
						for(LoginAppRecord rcd : rcds)
						{
							if(rcd.isExpired())
								rcdMap.remove(rcd.getAppId()) ;
						}
					}
					if(rcdMap.isEmpty())
						mLoginRcdMap.remove(userId) ;
 				}
			}
		}) ;
	}
	
	public void recordLogin(String aUserId , String aAppId , Date aLoginTime , Date aExpiredTime
			, String aOAuth2AuthorizationId)
	{
		Map<String, LoginAppRecord> rcdMap = mLoginRcdMap.get(aUserId) ;
		if(rcdMap == null)
		{
			rcdMap = XC.concurrentHashMap() ;
			mLoginRcdMap.put(aUserId, rcdMap) ;
		}
		LoginAppRecord rcd = rcdMap.get(aAppId) ;
		if(rcd == null)
		{
			rcd = new LoginAppRecord(aAppId , aLoginTime , aExpiredTime , aOAuth2AuthorizationId) ;
			rcdMap.put(aAppId, rcd) ;
		}
		else
		{
			rcd.setLoginTime(aLoginTime) ;
			rcd.setExpiredTime(aExpiredTime);
			rcd.setOAuth2AuthorizationId(aOAuth2AuthorizationId) ;
		}
	}
	
	public Set<String> filterNotExpiredUsersInApp(String aAppId , Collection<String> aUserIds)
	{
		if(XC.isEmpty(aUserIds))
			return Collections.emptySet() ;
		Set<String> userIds = XC.hashSet() ;
		for(String userId : aUserIds)
		{
			Map<String , LoginAppRecord> rcdMap = mLoginRcdMap.get(userId) ;
			if(rcdMap != null)
			{
				LoginAppRecord rcd = rcdMap.get(aAppId) ;
				if(rcd != null && !rcd.isExpired())
					userIds.add(userId) ;
 			}
		}
		return userIds ;
	}
	
	public List<LoginAppRecord> getNotExpiredLoginAppsOfUser(String aUserId)
	{
		Map<String, LoginAppRecord> rcdMap = mLoginRcdMap.get(aUserId) ;
		if(XC.isNotEmpty(rcdMap))
		{
			List<LoginAppRecord> appList = XC.arrayList() ;
			LoginAppRecord[] rcds = rcdMap.values().toArray(new LoginAppRecord[0]) ;
			for(LoginAppRecord rcd : rcds)
			{
				if(!rcd.isExpired())
					appList.add(rcd) ;
			}
			return appList ;
		}
		return null ;
	}
	
	/**
	 * 取得指定用户中在各个应用没过期的信息
	 * @param aUserIds
	 * @return			键是应用id，值是用户id集合
	 */
	public Map<String, Set<String>> getAppsNotExpiredUsers(Collection<String> aUserIds)
	{
		Map<String, Set<String>> map = XC.hashMap() ;
		if(XC.isNotEmpty(aUserIds))
		{
			for(String userId : aUserIds)
			{
				Map<String, LoginAppRecord> rcdMap = mLoginRcdMap.get(userId) ;
				if(rcdMap != null)
				{
					String[] appIds = rcdMap.keySet().toArray(JCommon.sEmptyStringArray) ;
					for(String appId : appIds)
					{
						LoginAppRecord rcd = rcdMap.get(appId) ;
						if(rcd.isExpired())
							rcdMap.remove(appId) ;
						else
						{
							Set<String> userIdSet = map.get(appId) ;
							if(userIdSet == null)
							{
								userIdSet = XC.hashSet() ;
								map.put(appId, userIdSet) ;
							}
							userIdSet.add(userId) ;
						}
					}
					if(rcdMap.isEmpty())
						mLoginRcdMap.remove(userId) ;
				}
			}
		}
		return map ;
	}
}
