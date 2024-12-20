package team.sailboat.commons.web.ac ;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import lombok.Getter;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.JCommon;

/**
 * 
 * 应用程序声明自己权限和角色的声明
 *
 * @author yyl
 * @since 2024年10月21日
 */
public class AppAuthStatement
{
	/**
	 * 子空间类型
	 */
	final TreeSet<String> resSpaceTypes = XC.treeSet() ;
	
	@Getter
	final List<ARole> roles = XC.arrayList() ;
	
	@Getter
	final List<AAuthority> authorities = XC.arrayList() ;
	
	@Getter
	Map<String, Set<String>> roleAuthsMap = XC.hashMap() ;
	
	public AppAuthStatement()
	{
	}
	
	public void addRole(ARole aRole)
	{
		roles.add(aRole) ;
		resSpaceTypes.add(aRole.getResSpaceType()) ;
	}
	
	public void addAuthority(AAuthority aAuth)
	{
		authorities.add(aAuth) ;
	}
	
	public void addRelation(String aRoleName , String aAuthCode)
	{
		Set<String> auths = roleAuthsMap.get(aRoleName) ;
		if(auths == null)
		{
			auths = XC.hashSet() ;
			roleAuthsMap.put(aRoleName, auths) ;
		}
		auths.add(aAuthCode) ;
	}
	
	public String[] getResSpaceTypesInArray()
	{
		return resSpaceTypes == null?null:resSpaceTypes.toArray(JCommon.sEmptyStringArray) ;
	}
}
