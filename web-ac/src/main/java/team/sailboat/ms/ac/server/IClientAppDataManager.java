package team.sailboat.ms.ac.server;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.struct.Tuples;
import team.sailboat.commons.ms.ac.IApiPredicate;
import team.sailboat.commons.web.ac.AAuthority;
import team.sailboat.commons.web.ac.ARole;
import team.sailboat.commons.web.ac.AppAuthStatement;
import team.sailboat.ms.ac.AppConsts;
import team.sailboat.ms.ac.bean.AppAmounts;
import team.sailboat.ms.ac.bean.Authority_Role;
import team.sailboat.ms.ac.dbean.Api;
import team.sailboat.ms.ac.dbean.Authority;
import team.sailboat.ms.ac.dbean.ClientApp;
import team.sailboat.ms.ac.dbean.R_User_ResSpace_Role;
import team.sailboat.ms.ac.dbean.ResSpace;
import team.sailboat.ms.ac.dbean.Role;
import team.sailboat.ms.ac.dbean.User;
import team.sailboat.ms.ac.frame.IUserAuthoritiesChangeNotifier;

/**
 * 
 * ClientApp的数据管理器
 *
 * @author yyl
 * @since 2024年10月30日
 */
public interface IClientAppDataManager extends IUserAuthsProvider , IResourceManageComponent 
								, IUserAuthoritiesChangeNotifier , IApiPredicate
{
	
	/**
	 * 
	 * 通过应用名获取ClientApp
	 * 
	 * @param aAppName
	 * @return
	 */
	ClientApp getClientAppByName(String aAppName) ;
	
	/**
	 * 
	 * 通过id获取ClientApp
	 * 
	 * @param aAppId
	 * @return
	 */
	ClientApp getClientApp(String aAppId) ;
	
	/**
	 * 
	 * 通过AppKey获取指定的ClientApp
	 * 
	 * @param aAppKey
	 * @return
	 */
	ClientApp getClientAppByAppKey(String aAppKey) ;
	
	/**
	 * 
	 * 取得指定应用下指定名称的角色
	 * 
	 * @param aAppId
	 * @param aName
	 * @return
	 */
	Role getRoleByName(String aAppId, String aName) ;
	
	/**
	 * 
	 * 取得指定id的角色
	 * 
	 * @param aRoleId
	 * @return
	 */
	Role getRole(String aRoleId) ;
	
	/**
	 * 
	 * 取得指定应用定义的角色信息
	 * 
	 * @param aAppId
	 * @return
	 */
	Role[] getRolesOfApp(String aAppId) ;
	
	/**
	 * 
	 * 创建角色
	 * 
	 * @param aRole
	 * @param aUserId
	 * @return
	 */
	Role createRole(Role.BRole aRole , String aUserId) ;
	
	/**
	 * 
	 * 更新角色			<br />
	 * 如果要修改资源空间类型，那么这个角色不能被授予任何人,即没有相关的R_User_ResSpace_Role数据。否则会抛出异常
	 * 
	 * @param aRole
	 * @param aUserId
	 * @return
	 */
	Role updateRole(Role.BRole aRole , String aUserId) ;
	
	/**
	 * 
	 * 删除角色
	 * 
	 * @param aRoleId		角色id
	 * @param aUserId		操作者用户id
	 * @return
	 */
	boolean deleteRole(String aRoleId , String aUserId) ;
	
	/**
	 * 
	 * 取得用户在指定资源空间下的角色
	 * 
	 * @param aUserId
	 * @param aResSpaceId
	 * @return
	 */
	List<Role> getRoleOfUserInResSpace(String aUserId , String aResSpaceId) ;
	
	/**
	 * 
	 * 将指定资源空间下的角色授权给指定用户
	 * 
	 * @param aResSpaceId
	 * @param aRoleId
	 * @param aTargetUserId		授权目标用户
	 * @param aUserId			操作用户
	 * @return 					授权关系id
	 */
	String grantRoleToUser(String aResSpaceId , String aRoleId , String aTargetUserId
			, String aUserId) ;
	
	/**
	 * 
	 * 将指定资源空间下的角色授权给指定用户
	 * 
	 * @param aResSpaceId
	 * @param aRoleName			角色名称
	 * @param aTargetUserId		授权目标用户
	 * @param aUserId			操作用户
	 * @return 					授权关系id
	 */
	default String grantRoleToUserByName(String aResSpaceId , String aRoleName , String aTargetUserId
			, String aUserId)
	{
		Role role = getRoleByName(ResSpace.getClientAppIdFrom(aResSpaceId) , aRoleName) ;
		Assert.notNull(aRoleName , "无效的角色名称：%s" , aRoleName) ;
		return grantRoleToUser(aResSpaceId, role.getId(), aTargetUserId, aUserId) ;
	}
	
	/**
	 * 
	 * 将指定的适用于指定资源空间的角色授权给指定的用户
	 * 
	 * @param aResSpaceId
	 * @param aRoleIds
	 * @param aTargetUserId			目标用户id
	 * @param aUserId				操作者用户id
	 */
	void grantRolesToUser(String aResSpaceId , String[] aRoleIds
			, String aTargetUserId , String aUserId) ;
	
	/**
	 * 
	 * 取消将指定资源空间下的角色授权给指定用户
	 * 
	 * @param aResSpaceId
	 * @param aRoleId
	 * @param aTargetUserId		授权目标用户
	 * @param aUserId			操作者用户id
	 */
	void ungrantRoleToUser(String aResSpaceId , String aRoleId , String aTargetUserId
			, String aUserId) ;
	
	/**
	 * 
	 * 取消将指定资源空间下的角色授权给指定用户
	 * 
	 * @param aId		授权关系id
	 * @param aUserId	操作者用户id
	 */
	void ungrantRoleToUser(String aId , String aUserId) ;
	
	/**
	 * 
	 * 将指定应用的可访问权限授予指定用户
	 * 
	 * @param aAppId			ClientApp的id
	 * @param aTargetUserId		被授权的目标用户
	 * @param aUserId			操作用户id
	 */
	void grantClientAppToUser(String aAppId, String aTargetUserId
			, String aUserId) ;
	
	/**
	 * 
	 * 取消将指定应用的可访问权限授予指定用户
	 * 
	 * @param aAppId
	 * @param aTargetUserId
	 * @param aUserId
	 */
	void ungrantClientAppToUser(String aAppId, String aTargetUserId
			, String aUserId) ;
	
	/**
	 * 
	 * 将指定的API授权给指定的CLientApp调用
	 * 
	 * @param aAppId		ClientApp的id
	 * @param aApiId		API的id
	 * @param aUserId		操作者用户id
	 * @return
	 */
	String grantApiToClientApp(String aAppId , String aApiId
			, String aUserId) ;
	
	/**
	 * 
	 * 取消将指定的API授权给指定的CLientApp调用
	 * 
	 * @param aAppId		ClientApp的id
	 * @param aApiId		API的id
	 * @param aUserId		操作者用户id
	 */
	void ungrantApiToClientApp(String aAppId , String aApiId
			, String aUserId) ;
	
	/**
	 * 
	 * 取得ClientApp所声明的权限
	 * 
	 * @param aAppId
	 * @param aResSpaceType				使用于那种资源空间类型
	 * @param aReturnDefaultGlobal		是否一并返回缺省全局资源空间下的
	 * @return
	 */
	Authority[] getAuthoritiesOfClientApp(String aAppId , String aResSpaceType , boolean aReturnDefaultGlobal) ;
	
	/**
	 * 
	 * 取得指定Role的相关权限
	 * 
	 * @param aRoleId		角色id
	 * @return
	 */
	List<Authority> getAuthoritiesOfRole(String aRoleId) ;
	
	/**
	 * 
	 * 通过权限id获取权限
	 * 
	 * @param aId
	 * @return
	 */
	Authority getAuthority(String aId) ;
	
	/**
	 * 
	 * 更新权限			<br />
	 * 
	 * 
	 * @param aAuth
	 * @param aUserId
	 * @return
	 */
	Authority updateAuthority(Authority.BAuthority aAuth , String aUserId) ;
	
	/**
	 * 
	 * 创建指定ClientApp的一个权限
	 * 
	 * @param aAppId
	 * @param aCode
	 * @param aDescription
	 * @param aGroupName
	 * @param aResSpaceType			适用的资源空间类型
	 * @return
	 */
	Authority createAuthority(String aAppId, String aCode, String aDescription
			, String aGroupName
			, String aResSpaceType) ;
	
	/**
	 * 
	 * 通过权限码取得指定应用下的权限信息
	 * 
	 * @param aAppId
	 * @param aCode
	 * @return
	 */
	Authority getAuthorityByCode(String aAppId, String aCode) ;
	
	/**
	 * 
	 * 删除指定的权限
	 * 
	 * @param aAuthorityId
	 */
	void deleteAuthority(String aAuthorityId) ;
	
	/**
	 * 
	 * 将指定的权限和角色关联起来
	 * 
	 * @param aAuthId		权限id
	 * @param aRoleId		角色id
	 * @param aUserId		操作者用户id
	 */
	void bindAuthorityToRole(String aAuthId, String aRoleId, String aUserId) ;
	
	/**
	 * 
	 * 取消将指定的权限和角色关联起来
	 * 
	 * @param aAuthId		权限id
	 * @param aRoleId		角色id
	 * @param aUserId		操作者用户id
	 */
	void unbindAuthorityToRole(String aAuthId, String aRoleId, String aUserId) ;
	
	/**
	 * 
	 * 创建ClientApp
	 * 
	 * @param aAppName
	 * @param aDescription
	 * @param aCompany			公司名称
	 * @param aGenCredential	是否生成凭据(appKey和appSecret)
	 * @param aUserId			操作者用户id
	 * @return
	 */
	ClientApp createClientApp(String aAppName, String aDescription
			, String aCompany
			, boolean aGenCredential
			, String aUserId) ;
	
	/**
	 * 
	 * 创建ClientApp
	 * 
	 * @param aClientAppInfo		ClientApp的信息
	 * @param aGenCredential	是否生成凭据(appKey和appSecret)
	 * @param aUserId				操作者用户id
	 * @return
	 */
	ClientApp createClientApp(ClientApp.BClientApp aClientAppInfo
			, boolean aGenCredential
			, String aUserId) ;
	
	/**
	 * 
	 * 更新ClientApp。不包括appKey和appSecret
	 * 
	 * @param aClientAppInfo		ClientApp的信息
	 * @param aUserId				操作者用户id
	 * @return
	 */
	ClientApp updateClientApp(ClientApp.BClientApp aClientAppInfo
			, String aUserId) ;
	
	/**
	 * 
	 * 删除指定id的ClientApp及其资源(ResSpace、Role、Authority及其相互自建的关联、R_User_App)
	 * 
	 * @param aClientAppId
	 * @return				如果指定的ClientApp存在，且成功删除返回true。否则返回false
	 */
	boolean deleteClientApp(String aClientAppId) ;
	
	/**
	 * 
	 * 取得全部ClientApp
	 * 
	 * @return
	 */
	List<ClientApp> getClientApps() ;
	
	/**
	 * 
	 * 取得指定用户可访问的ClientApp
	 * 
	 * @param aUserId
	 * @return
	 */
	List<ClientApp> getClientAppsOfUserCanVisit(String aUserId) ;
	
	
	/**
	 * 
	 * 取得可以访问指定ClientApp的用户
	 * 
	 * @param aClientAppId
	 * @return
	 */
	Collection<User> getUsersOfCanVisitClientApp(String aClientAppId) ;
	
	/**
	 * 
	 * 取得可以访问指定ClientApp的用户
	 * 
	 * @param aResSpaceId
	 * @return		元组的第1个元素是用户，第2个元素是这个用户在指定资源空间下的角色
	 */
	List<Tuples.T2<User, List<Role>>> getUsersOfCanVisitResSpace(String aResSpaceId) ;
	
	/**
	 * 
	 * 取得指定用户在指定ClientApp下所具有的空间访问关系
	 * 
	 * @param aUserId		用户id
	 * @param aAppId		ClientApp的id
	 * @return
	 */
	R_User_ResSpace_Role[] getR_User_ResSpace_RoleOfUserInApp(String aUserId , String aAppId) ;
	
	/**
	 * 
	 * 通过id获取R_User_ResSpace_Role
	 * 
	 * @param aId 		R_User_ResSpace_Role的对象id
	 * @return
	 */
	R_User_ResSpace_Role getR_User_ResSpace_Role(String aId) ;
	
	/**
	 * 
	 * 取得应用数量。包括：Web应用数量、中台微服务应用数量
	 * 
	 * @return
	 */
	AppAmounts getAppAmounts() ;
	
	/**
	 * 
	 * 获取指定id的资源空间
	 * 
	 * @param aResSpaceId
	 * @return
	 */
	ResSpace getResSpace(String aResSpaceId) ;
	
	/**
	 *
	 *	取得指定ClientApp的所有资源空间
	 *
	 * @param aClientAppId
	 * @return
	 */
	ResSpace[] getResSpaceOfClientApp(String aClientAppId) ;
	
	/**
	 * 
	 * 取得指定用户在指定应用下获得授权的子空间
	 * 
	 * @param aUserId
	 * @param aClientAppId
	 * @return
	 */
	List<ResSpace> getResSpaceOfUserInClientApp(String aUserId , String aClientAppId) ;
	
	/**
	 * 
	 * 将自己应用的某个资源空间下的某些角色授予某个用户
	 * 
	 * @param aResSpaceId		资源空间id
	 * @param aRoleNames		角色名称数组
	 * @param aTargetUserId		目标用户id
	 * @param aUserId			操作者用户id
	 */
	void grantResSpaceRoleToUser(String aResSpaceId , String[] aRoleNames 
			, String aTargetUserId
			, String aUserId) ;
	
	/**
	 * 
	 * 取得指定资源空间下的权限信息。权限里面包含了这个权限所属角色
	 * 
	 * @param aClientAppId				ClientApp的id
	 * @param aResSpaceType				资源空间类型
	 * @return
	 */
	List<Authority_Role> getAuthoritesForResSpaceType(String aClientAppId , String aResSpaceType) ;
	
	/**
	 * 
	 * 取得指定ClientApp可以调用的Api
	 * 
	 * @param aClientAppId
	 * @return
	 */
	List<Api> getApisOfClientAppCanInvoke(String aClientAppId) ;
	
	/**
	 * 
	 * 取得指定appKey对应的ClientApp可调用的API的请求处理映射表		<br />
	 * 这里并不是真的要用它来查询某个请求的处理方法，然后用方法来处理请求。
	 * 只是为了通过判断是否注册有请求处理方法，知道是否者客户端有这个API的调用权限。
	 * 
	 * @param aAppKey
	 * @return
	 */
	RequestMappingHandlerMapping getInvokableApiMapping(String aAppKey) ;
	
	/**
	 * 指定用户是否有权访问指定的app
	 * @param aUserId
	 * @param aAppId
	 * @return
	 */
	boolean canVisitApp(String aUserId , String aAppId) ;
	
	/**
	 * 声明APP的权限
	 * @param aAppId
	 * @param aStm
	 */
	default
	void updateAppAuths(String aAppId , AppAuthStatement aStm)
	{
		ClientApp clientApp = getClientApp(aAppId) ;
		Assert.notNull(clientApp , "不存在id为%s的ClientApp！" , aAppId) ;
		
		clientApp.setResSpaceTypes(aStm.getResSpaceTypesInArray()) ;
		
		Role[] roles = getRolesOfApp(aAppId) ;
		List<ARole> aroleList = aStm.getRoles() ;
		
		// 
		Map<String, Role> roleMap = XC.hashMap(Arrays.asList(roles) , Role::getName, true) ;
		// 角色只需要新建，不需要删除。如果要删除，可以登陆控制台去操作
		if(XC.isNotEmpty(aroleList))
		{
			for(ARole arole : aroleList)
			{
				Role role = roleMap.get(arole.getName()) ;
				if(role != null)
				{
					role.setDescription(arole.getDescription()) ;
					role.setResSpaceType(arole.getResSpaceType()) ;
				}
				else
				{
					Role.BRole brole = new Role.BRole() ;
					brole.setClientAppId(aAppId) ;
					brole.setName(arole.getName()) ;
					brole.setDescription(arole.getDescription()) ;
					brole.setResSpaceType(arole.getResSpaceType()) ;
					role = createRole(brole , AppConsts.sUserId_sys) ;
				}
			}
		}
		// 权限是需要完全同步的
		Authority[] authorities = getAuthoritiesOfClientApp(aAppId , null , true) ;
		List<AAuthority> aauthList = aStm.getAuthorities() ;
		Map<String, Authority> authMap = XC.hashMap(authorities , Authority::getCode, true) ;
		Map<String, AAuthority> aauthMap = XC.hashMap(aauthList, AAuthority::getCode, true) ;
		// 创建新的
		if(!aauthList.isEmpty())
		{
			for(AAuthority aauth : aauthList)
			{
				Authority auth = authMap.get(aauth.getCode()) ;
				if(auth == null)
				{
					auth = createAuthority(aAppId, aauth.getCode(), aauth.getDescription() 
							, aauth.getGroupName()
							, aauth.getResSpaceType()) ;
				}
				else
				{
					auth.setDescription(aauth.getDescription()) ;
					auth.setGroupName(aauth.getGroupName()) ;
				}
			}
		}
		// 删除没有了的
		if(XC.isNotEmpty(authorities))
		{
			for(Authority auth : authorities)
			{
				if(!aauthMap.containsKey(auth.getCode()))
				{
					// 先移除关联
					deleteAuthority(auth.getId()) ;
				}
			}
		}
		
		// 角色和权限的关系,只添加缺少的，不删除多的
		Map<String, Set<String>> map = aStm.getRoleAuthsMap() ;
		if(XC.isNotEmpty(map))
		{
			for(Map.Entry<String, Set<String>> entry : map.entrySet())
			{
				Set<String> authCodeList = entry.getValue() ;
				if(XC.isEmpty(authCodeList))
					continue ;
				Role role = getRoleByName(aAppId, entry.getKey()) ;
				if(role != null)
				{
					for(String code : authCodeList)
					{
						Authority auth = getAuthorityByCode(aAppId, code) ;
						if(auth != null)
						{
							// 构建角色 和 权限的关系
							bindAuthorityToRole(auth.getId(), role.getId(), AppConsts.sUserId_sys) ;
						}
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * 创建或更新资源空间
	 * 
	 * @param aResSpace			资源空间信息
	 * @param aUserId			操作者用户id
	 * @return					返回的2元组，第2个元素是ResSpace是新建的或发生修改的将是true，否则是false
	 */
	Tuples.T2<ResSpace , Boolean> createOrUpdateResSpace(ResSpace.BResSpace aResSpace , String aUserId) ;
	
	/**
	 * 
	 * 创建或更新缺省全局资源空间
	 * 
	 * @param aClientAppId		ClientApp的id
	 * @param aUserId			操作者用户id
	 * @return					返回的2元组，第2个元素是ResSpace是新建的或发生修改的将是true，否则是false
	 */
	default Tuples.T2<ResSpace , Boolean> createOrUpdateDefaultGlobalResSpace(String aClientAppId , String aUserId)
	{
		return createOrUpdateResSpace(ResSpace.newDefaultGlobalBResSpace(aClientAppId), aUserId) ;
	}
	
	/**
	 * 
	 * 给自己应用设置资源空间数据。			<br />
	 * 
	 * @param aResSpaces				应该已经确保是同一个ClientApp的，且信息是完备的，合法的
	 * @param aDeleteIfNoExists			如果原先已经存在的资源空间，不在新指定的列表中，是否删除
	 * @param aUserId					操作者用户id
	 */
	void createOrUpdateSubspaces(List<ResSpace.BResSpace> aResSpaces
			, boolean aDeleteIfNoExists
			, String aUserId) ;
	
	/**
	 * 
	 * 通过资源id，删除相关的资源空间
	 * 
	 * @param aResId			资源id
	 * @param aUserId			操作者用户id
	 * @return
	 */
	boolean deleteResSpaceByResId(String aResId , String aUserId) ;
}
