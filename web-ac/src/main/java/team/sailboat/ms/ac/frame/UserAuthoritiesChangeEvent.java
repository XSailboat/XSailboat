package team.sailboat.ms.ac.frame;

import java.util.Set;

import lombok.Getter;
import team.sailboat.commons.fan.collection.XC;

/**
 * 
 * 用户的权限改变事件
 *
 * @author yyl
 * @since 2024年10月31日
 */
@Getter
public class UserAuthoritiesChangeEvent
{
	String appId ;
	
	Set<String> userIds ;
	
	public UserAuthoritiesChangeEvent(String aAppId , String...aUserIds)
	{
		appId = aAppId ;
		userIds = XC.hashSet(aUserIds) ;
	}
	
	public UserAuthoritiesChangeEvent(String aAppId , Set<String> aUserIds)
	{
		appId = aAppId ;
		userIds = aUserIds ;
	}
}
