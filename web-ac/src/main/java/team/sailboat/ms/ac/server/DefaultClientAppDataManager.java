package team.sailboat.ms.ac.server;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import team.sailboat.commons.fan.collection.SRHashMap;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dpa.MapIndex;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.struct.Tuples;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.ms.ac.AppConsts;
import team.sailboat.ms.ac.bean.AppAmounts;
import team.sailboat.ms.ac.bean.Authority_Role;
import team.sailboat.ms.ac.dbean.Api;
import team.sailboat.ms.ac.dbean.Authority;
import team.sailboat.ms.ac.dbean.Authority.BAuthority;
import team.sailboat.ms.ac.dbean.ClientApp;
import team.sailboat.ms.ac.dbean.ClientApp.BClientApp;
import team.sailboat.ms.ac.dbean.R_App_Api;
import team.sailboat.ms.ac.dbean.R_Role_Authority;
import team.sailboat.ms.ac.dbean.R_User_App;
import team.sailboat.ms.ac.dbean.R_User_ResSpace_Role;
import team.sailboat.ms.ac.dbean.ResSpace;
import team.sailboat.ms.ac.dbean.ResSpace.BResSpace;
import team.sailboat.ms.ac.dbean.Role;
import team.sailboat.ms.ac.dbean.Role.BRole;
import team.sailboat.ms.ac.dbean.User;
import team.sailboat.ms.ac.frame.IUserAuthoritiesChangeListener;
import team.sailboat.ms.ac.frame.UserAuthoritiesChangeEvent;

/**
 * 
 * 缺省的ClientApp的数据管理器
 * 
 *
 * @author yyl
 * @since 2024年10月30日
 */
public class DefaultClientAppDataManager extends ResourceManageComponent implements IClientAppDataManager
{
	
	final Logger mLogger = LoggerFactory.getLogger(getClass()) ;
	
	MapIndex<ClientApp> mAppNameMapIndex ;
	MapIndex<ClientApp> mAppKey_CA ;
	
	MapIndex<Role> mClientAppId_Role ;
	
	MapIndex<Authority> mClientAppId_Authority ;
	MapIndex<Authority> mCode_Authority ;
	
	MapIndex<R_Role_Authority> mAuthorityId_R_RA ;
	MapIndex<R_Role_Authority> mRoleId_R_RA ;
	
	MapIndex<R_User_ResSpace_Role> mUserId_R_URR ;
	MapIndex<R_User_ResSpace_Role> mRoleId_R_URR ;
	MapIndex<R_User_ResSpace_Role> mResSpaceId_R_URR ;
	
	MapIndex<R_User_App> mUserId_R_UA ;
	MapIndex<R_User_App> mClientAppId_R_UA ;
	
	MapIndex<R_App_Api> mClientAppId_R_AA ;
	
	MapIndex<ResSpace> mResId_RS ;
	MapIndex<ResSpace> mClientAppId_RS ;
	
	IUserAuthoritiesChangeListener[] mLsns ;
	
	/**
	 * 占位，不会实际使用。			<br />
	 * 用在RequestMappingHandlerMapping用，用是否有处理方法来检测某个Request是否是获得授权的
	 */
	Method mPlaceHoldMethod ;
	
	/**
	 * 用户在认证中心的权限缓存。	<br />
	 * 
	 * 不包括在其它ClientApp的，其它应用查询用户的权限不会那么频繁，所以不用缓存，通过getAuthoritysOfUserInClientApp接口临时已查询即可
	 * <br />
	 * 
	 * 键是用户id
	 */
	final Map<String , Set<GrantedAuthority>> mUserAuthCache = XC.concurrentHashMap() ;
	
	/**
	 * 键是ClientApp的id，值是Api名称
	 */
	final SRHashMap<String, Set<String>> mAppCanVistApiNames = XC.srHashMap() ;
	
	public DefaultClientAppDataManager(ResourceManageServer aServer)
	{
		super(aServer) ;
		
		try
		{
			mPlaceHoldMethod = XClassUtil.getMethod(Object.class , "toString");
		}
		catch (NoSuchMethodException e)
		{
			WrapException.wrapThrow(e) ;
		}
	}
	
	public void init()
	{
		mAppNameMapIndex = mResMngServer.mRepo.mapIndex(ClientApp.class, "name") ;
		mAppKey_CA = mResMngServer.mRepo.mapIndex(ClientApp.class ,"appKey") ;
		
		mClientAppId_Role = mResMngServer.mRepo.mapIndex(Role.class , "clientAppId") ;
		
		mClientAppId_Authority = mResMngServer.mRepo.mapIndex(Authority.class , "clientAppId") ;
		mCode_Authority = mResMngServer.mRepo.mapIndex(Authority.class , "code") ;
		
		mAuthorityId_R_RA = mResMngServer.mRepo.mapIndex(R_Role_Authority.class , "authorityId") ;
		mRoleId_R_RA = mResMngServer.mRepo.mapIndex(R_Role_Authority.class , "roleId") ;
		
		mUserId_R_URR = mResMngServer.mRepo.mapIndex(R_User_ResSpace_Role.class , "userId") ;
		mRoleId_R_URR = mResMngServer.mRepo.mapIndex(R_User_ResSpace_Role.class , "roleId") ;
		mResSpaceId_R_URR = mResMngServer.mRepo.mapIndex(R_User_ResSpace_Role.class , "resSpaceId") ;
		
		mUserId_R_UA = mResMngServer.mRepo.mapIndex(R_User_App.class , "userId") ;
		mClientAppId_R_UA = mResMngServer.mRepo.mapIndex(R_User_App.class , "clientAppId") ;
		
		mClientAppId_R_AA = mResMngServer.mRepo.mapIndex(R_App_Api.class , "clientAppId") ;
		
		mResId_RS = mResMngServer.mRepo.mapIndex(ResSpace.class , "resId") ;
		mClientAppId_RS = mResMngServer.mRepo.mapIndex(ResSpace.class , "clientAppId") ;
		
		// 权限改变的话清理一下缓存
		addListener(new IUserAuthoritiesChangeListener()
		{
			@Override
			public void accept(UserAuthoritiesChangeEvent aT)
			{
				if(mResMngServer.mClientAppId_SailAC.equals(aT.getAppId()))
				{
					for(String userId : aT.getUserIds())
					{
						mUserAuthCache.remove(userId) ;
					}
				}
			}
		}) ;
	}

	@Override
	public Collection<GrantedAuthority> getAuthoritysOfUserInClientApp(String aUserId, String aClientAppId)
	{
		if(mResMngServer.mClientAppId_SailAC.equals(aClientAppId))
		{
			// 是认证中心
			Set<GrantedAuthority> auths = mUserAuthCache.get(aUserId) ;
			if(auths == null)
			{
				synchronized (("auths_"+aUserId).intern())
				{
					auths = mUserAuthCache.get(aUserId) ;
					if(auths == null)
					{
						auths = _getAuthoritysOfUserInClientApp(aUserId, aClientAppId) ;
						mUserAuthCache.put(aUserId, auths) ;
					}
				}
			}
			return auths ;
		}
		else
		{
			return _getAuthoritysOfUserInClientApp(aUserId, aClientAppId) ;
		}
	}
	
	Set<GrantedAuthority> _getAuthoritysOfUserInClientApp(String aUserId, String aClientAppId)
	{
		Set<GrantedAuthority> auths = XC.hashSet() ;
		// 1. 用户在这个应用下的所有R_User_ResSpace_Role
		R_User_ResSpace_Role[] rs = mUserId_R_URR.get(aUserId, r->ResSpace.getClientAppIdFrom(r.getResSpaceId()).equals(aClientAppId)) ;
		// 2. 取R_Role_Authority
		if(XC.isNotEmpty(rs))
		{
			for(R_User_ResSpace_Role r : rs)
			{
				R_Role_Authority[] rs1 = mRoleId_R_RA.get(r.getRoleId()) ;
				XC.extract(rs1 , r1->Authority.toSimple(r1.getAuthorityCode() , r.getResSpaceId()) ,auths) ;
			}
		}
		return auths ;
	}
	
	@Override
	public Collection<String> getAuthorityCodesOfUserInClientApp(String aUserId, String aClientAppId)
	{
		Set<String> auths = XC.hashSet() ;
		// 1. 用户在这个应用下的所有R_User_ResSpace_Role
		R_User_ResSpace_Role[] rs = mUserId_R_URR.get(aUserId, r->ResSpace.getClientAppIdFrom(r.getResSpaceId()).equals(aClientAppId)) ;
		// 2. 取R_Role_Authority
		if(XC.isNotEmpty(rs))
		{
			for(R_User_ResSpace_Role r : rs)
			{
				R_Role_Authority[] rs1 = mRoleId_R_RA.get(r.getRoleId()) ;
				XC.extract(rs1 , r1->Authority.toFullCode(r1.getAuthorityCode() , r.getResSpaceId()) ,auths) ;
			}
		}
		// 添加用户id，作为权限放进去
		auths.add("USER_ID_"+aUserId) ;
		return auths ;
	}

	@Override
	public ClientApp getClientAppByName(String aAppName)
	{
		return mAppNameMapIndex.getFirst(aAppName) ;
	}

	@Override
	public ClientApp getClientApp(String aAppId)
	{
		return mResMngServer.mRepo.getByBid(ClientApp.class, aAppId) ;
	}

	@Override
	public R_User_ResSpace_Role[] getR_User_ResSpace_RoleOfUserInApp(String aUserId, String aAppId)
	{
		return mUserId_R_URR.get(aUserId , r->{
			return ResSpace.getClientAppIdFrom(r.getResSpaceId()).equals(aAppId) ;
		});
	}
	
	@Override
	public Role getRoleByName(String aAppId, String aName)
	{
		return mClientAppId_Role.getFirst(aAppId, r->r.getName().equals(aName)) ;
	}
	
	@Override
	public Role getRole(String aRoleId)
	{
		return mResMngServer.mRepo.getByBid(Role.class , aRoleId) ;
	}
	
	@Override
	public Role[] getRolesOfApp(String aAppId)
	{
		return mClientAppId_Role.get(aAppId) ;
	}
	
	@Override
	public String grantRoleToUser(String aResSpaceId, String aRoleId, String aTargetUserId
			, String aOperUserId)
	{
		Assert.isNotTrue(aTargetUserId.equals(mResMngServer.mAdminUserId) , "admin用户不能修改角色！");
		
		R_User_ResSpace_Role[] rs = mUserId_R_URR.get(aTargetUserId , r->r.getResSpaceId().equals(aResSpaceId) && r.getRoleId().equals(aRoleId)) ;
		if(XC.isNotEmpty(rs))
			return rs[0].getId() ;
		
		// 获取资源空间
		ResSpace resSpace = mResMngServer.mRepo.getByBid(ResSpace.class , aResSpaceId) ;
		Assert.notNull(resSpace, "不存在id为%s的资源空间id！", aResSpaceId) ;
		
		// 获取角色
		Role role = mResMngServer.mRepo.getByBid(Role.class , aRoleId) ;
		Assert.notNull(role, "不存在id为%s的角色！" , aRoleId) ;
		
		// 检查类型是否匹配
		Assert.equals(resSpace.getType()  , role.getResSpaceType() , "角色 %s[%s] 适用于 %s 类型的资源空间，不适用于 %s 类型资源空间 %s[%s]！"
				, role.getName() , role.getId() , role.getResSpaceType() 
				, resSpace.getType() , resSpace.getResName() , resSpace.getId()) ;
		
		// 检查用户是否存在
		User targetUser = mResMngServer.mRepo.getByBid(User.class , aTargetUserId) ;
		Assert.notNull(targetUser , "授权的目标用户[%s]不存在！", aTargetUserId) ;
		
		// 进行关联授权
		R_User_ResSpace_Role r = mResMngServer.mRepo.newBean(R_User_ResSpace_Role.class) ;
		r.setUserId(aTargetUserId) ;
		r.setResSpaceId(aResSpaceId) ;
		r.setRoleId(aRoleId) ;
		Date now = new Date() ;
		r.setCreateTime(now) ;
		r.setCreateUserId(aOperUserId) ;
		
		// 检查R_User_App关联是否已经存在，不存在创建它
		String clientAppId = ResSpace.getClientAppIdFrom(aResSpaceId) ;
		if(mUserId_R_UA.getFirst(aTargetUserId , r0->clientAppId.equals(r0.getClientAppId())) == null)
		{
			R_User_App rua = mResMngServer.mRepo.newBean(R_User_App.class) ;
			rua.setClientAppId(clientAppId) ;
			rua.setUserId(aTargetUserId) ;
			rua.setCreateTime(now) ;
			rua.setCreateUserId(aOperUserId) ;
		}
		// 
		notifyUserAuthoritiesChanged(role.getClientAppId() , aTargetUserId) ;
		
		return r.getId() ;
	}
	
	// 不小心写多了，暂且留着
	@Override
	public synchronized void grantRolesToUser(String aResSpaceId, String[] aRoleIds, String aTargetUserId
			, String aOperUserId)
	{
		// 查询角色
		List<Role> roleList = mResMngServer.mRepo.getByBids(Role.class , aRoleIds) ;
		// 检查角色是否是指定资源空间所属的ClientApp的。同时检查角色的适用资源空间类型
		String clientAppId = ResSpace.getClientAppIdFrom(aResSpaceId) ;
		ResSpace resSpace = mResMngServer.mRepo.getByBid(ResSpace.class , aResSpaceId) ;
		for(Role role : roleList)
		{
			Assert.equals(role.getClientAppId() , clientAppId , "指定的角色 %s[%s] 不是应用 %s 的！"
					, role.getName()
					, role.getId()
					, clientAppId);
			Assert.equals(resSpace.getType() , role.getResSpaceType() , "指定的角色 %s[%s] 在资源空间类型上不适用于指定的资源空间 %s[%s]!"
					, role.getName() , role.getId()
					, resSpace.getResName() , resSpace.getId()) ;
		}
		// 没有授权的话逐个进行授权
		Set<String> roleIds = XC.hashSet(Role::getId , roleList) ;
		mUserId_R_URR.get(aTargetUserId, r->{
			if(r.getResSpaceId().equals(aResSpaceId))
				roleIds.remove(r.getRoleId()) ;
			return false ;
		}) ;
		if(!roleIds.isEmpty())
		{
			Date now = new Date() ;
			for(String roleId : roleIds)
			{
				R_User_ResSpace_Role r = mResMngServer.mRepo.newBean(R_User_ResSpace_Role.class) ;
				r.setResSpaceId(aResSpaceId) ;
				r.setRoleId(roleId) ;
				r.setUserId(aTargetUserId) ;
				r.setCreateTime(now) ;
				r.setCreateUserId(aOperUserId) ;
			}
			notifyUserAuthoritiesChanged(clientAppId , aTargetUserId) ;
		}
	}
	
	@Override
	public void ungrantRoleToUser(String aResSpaceId, String aRoleId, String aTargetUserId, String aOperUserId)
	{
		// 不能让admin用户失去AuthCenter的管理员权限
		Assert.notEquals(mResMngServer.mAdminUserId , aTargetUserId , "admin用户不能修改角色！");
		
		R_User_ResSpace_Role[] rs = mUserId_R_URR.get(aTargetUserId , r->r.getResSpaceId().equals(aResSpaceId)
				&& r.getRoleId().equals(aRoleId)) ;
		if(XC.isNotEmpty(rs))
		{
			mResMngServer.mRepo.deleteAll(rs) ;
			notifyUserAuthoritiesChanged(ResSpace.getClientAppIdFrom(aResSpaceId) , aTargetUserId) ;		
		}
	}
	
	@Override
	public void ungrantRoleToUser(String aId, String aOperUserId)
	{
		R_User_ResSpace_Role r = mResMngServer.mRepo.getByBid(R_User_ResSpace_Role.class , aId) ;
		if(r != null)
		{
			// 不能让admin用户失去AuthCenter的管理员权限
			String userId = r.getUserId() ;
			Assert.notEquals(mResMngServer.mAdminUserId , userId , "admin用户不能修改角色！");
			mResMngServer.mRepo.delete(r) ;
			notifyUserAuthoritiesChanged(ResSpace.getClientAppIdFrom(r.getResSpaceId()) , userId) ;		
		}
	}
	
	@Override
	public Role createRole(BRole aRole, String aUserId)
	{
		String clientAppId = aRole.getClientAppId() ;
		ClientApp app = mResMngServer.mRepo.getByBid(ClientApp.class , clientAppId) ;
		Assert.notNull(app, "不存在id为%s的ClientApp！", clientAppId) ;
		
		// <资源空间类型 , 角色名>的组合不能重复
		String resSpaceType = aRole.getResSpaceType() ;
		String roleName = aRole.getName() ;
		Role[] roles = mClientAppId_Role.get(clientAppId , r->r.getResSpaceType().equals(resSpaceType)
				&& r.getName().equals(roleName)
				&& r.getClientAppId().equals(clientAppId)) ;
		Assert.isEmpty(roles , "在应用 %s 下已经存在使用于资源空间类型 %s ，名为 %s 的角色！" 
				, app.getName()
				, resSpaceType , roleName) ; 
		
		Role role = mResMngServer.mRepo.newBean(Role.class) ;
		role.update(aRole, aUserId, true) ;
		return role ;
	}
	
	@Override
	public Role updateRole(BRole aRole , String aUserId)
	{
		Role role = mResMngServer.mRepo.getByBid(Role.class , aRole.getId()) ;
		// 所属ClientApp不允许修改
		aRole.setClientAppId(role.getClientAppId()) ;
		// 检查是否修改了resSpaceType
		if(JCommon.unequals(aRole.getResSpaceType() , role.getResSpaceType()))
		{
			// 新指定的资源空间类型，必须是ClientApp中定义的资源空间类型之一
			ClientApp clientApp = mResMngServer.mRepo.getByBid(ClientApp.class , role.getClientAppId()) ;
			Assert.isTrue(XC.contains(clientApp.getResSpaceTypes() , aRole.getResSpaceType())
					, "给角色指定的资源空间类型 %s 不是应用 %s[%s]的资源空间类型之一！"
					, aRole.getResSpaceType() 
					, clientApp.getName() , clientApp.getId()) ;
			// 检查是否有R_User_ResSpace_Role数据
			Assert.isTrue(mRoleId_R_URR.getAmount(role.getId()) == 0 , "角色 %s[%s] 有授权给用户，不能修改资源空间类型！"
					, role.getName() , role.getId()) ; 
		}
		role.update(aRole, aUserId, false) ;
		return role ;
	}
	
	@Override
	public boolean deleteRole(String aRoleId, String aUserId)
	{
		Role role = mResMngServer.mRepo.getByBid(Role.class , aRoleId) ;
		if(role == null)
			return false ;
		String appId = role.getClientAppId() ;
		
		// R_User_ResSpace_Role断开
		R_User_ResSpace_Role[] rs = mRoleId_R_URR.get(aRoleId) ;
		Set<String> userIds = null ;
		if(XC.isNotEmpty(rs))
		{
			userIds = XC.extractAsHashSet(rs , R_User_ResSpace_Role::getUserId) ;
			mResMngServer.mRepo.deleteAll(rs) ;
		}
		
		// R_Role_Authority 断开
		mResMngServer.mRepo.deleteAll(mRoleId_R_RA.get(aRoleId)) ;
		
		// 清理掉没有关联任何角色的权限
		for(Authority auth : mClientAppId_Authority.get(appId))
		{
			if(XC.isEmpty(mAuthorityId_R_RA.get(auth.getId())))
			{
				mResMngServer.mRepo.delete(auth) ;
			}
		}
		// 将角色删了
		mResMngServer.mRepo.delete(role) ;
		//
		if(userIds != null)
			notifyUserAuthoritiesChanged(appId, userIds) ;
		return true ;
	}
	
	@Override
	public Authority[] getAuthoritiesOfClientApp(String aAppId , String aResSpaceType
			, boolean aDefaultGlobal)
	{
		if(XString.isEmpty(aResSpaceType))
			return mClientAppId_Authority.get(aAppId) ;
		else
		{
			return mClientAppId_Authority.get(aAppId , au->{
				return (aDefaultGlobal && AppConsts.sResSpaceType_default.equals(au.getResSpaceType()))
						|| aResSpaceType.equals(au.getResSpaceType()) ;
			}) ;
		}
	}
	
	@Override
	public List<Authority> getAuthoritiesOfRole(String aRoleId)
	{
		List<Authority> authList = XC.arrayList() ;
		for(R_Role_Authority r : mRoleId_R_RA.get(aRoleId))
		{
			authList.add(mResMngServer.mRepo.getByBid(Authority.class , r.getAuthorityId())) ;
		}
		return authList ;
	}
	
	@Override
	public synchronized Authority createAuthority(String aAppId, String aCode, String aDescription
			, String aGroupName
			, String aResSpaceType)
	{
		ClientApp app = mResMngServer.mRepo.getByBid(ClientApp.class , aAppId) ;
		Assert.notNull(app, "不存在id为%s的ClientApp！", aAppId) ;
		
		Authority auth = mResMngServer.mRepo.newBean(Authority.class) ;
		
		if(XString.isNotEmpty(aDescription))
			auth.setDescription(aDescription) ;
		auth.setGroupName(aGroupName) ;
		auth.setClientAppId(aAppId) ;
		auth.setResSpaceType(aResSpaceType) ;
		if(AppConsts.sResSpaceType_default.equals(aResSpaceType))
		{
			// 不能以":"结尾
			auth.setCode(aCode.endsWith(":")?aCode.substring(0 , aCode.length()-1):aCode) ;
		}
		else
		{
			// 必需以":"结尾
			auth.setCode(aCode.endsWith(":")?aCode:(aCode+":")) ;
		}
		Date now = new Date() ;
		auth.setCreateTime(now) ;
		auth.setLastEditTime(now) ;
		auth.setCreateUserId(AppConsts.sUserId_sys) ;
		auth.setLastEditUserId(AppConsts.sUserId_sys) ;
		
		return auth ;
	}
	
	@Override
	public ClientApp updateClientApp(BClientApp aClientApp , String aUserId)
	{
		Assert.notEmpty(aClientApp.getId() , "指定的ClientApp信息中id为空！") ;
		ClientApp clientApp = mResMngServer.mRepo.getByBid(ClientApp.class , aClientApp.getId()) ;
		Assert.notNull(clientApp , "不存在id为%s的ClientApp！" , aClientApp.getId()) ;
		// 防止appKey和appSecret被修改
		aClientApp.setAppKey(clientApp.getAppKey()) ;
		aClientApp.setAppSecret(clientApp.getAppSecret()) ;
		
		clientApp.update(aClientApp, aUserId, false) ;
		return clientApp ;
	}
	
	@Override
	public boolean deleteClientApp(String aClientAppId)
	{
		ClientApp clientApp = mResMngServer.mRepo.getByBid(ClientApp.class , aClientAppId) ;
		if(clientApp != null)
		{
			// 1.删权限
			Authority[] auths = mClientAppId_Authority.get(aClientAppId) ;
			mResMngServer.mRepo.deleteAll(auths) ;
			// 2. 删Role
			Role[] roles = mClientAppId_Role.get(aClientAppId) ;
			mResMngServer.mRepo.deleteAll(roles) ;
			// 3. 删ResSpace
			mResMngServer.mRepo.deleteAll(mClientAppId_RS.get(mResMngServer.getClientAppId_SailAC()
					, r->aClientAppId.equals(r.getResId()))) ;
			// 4. 删R_Role_Authority 和 R_User_ResSpace_Role
			for(Role role : roles)
			{
				mResMngServer.mRepo.deleteAll(mRoleId_R_RA.get(role.getId())) ;
				mResMngServer.mRepo.deleteAll(mRoleId_R_URR.get(role.getId())) ;
			}
			// 5. 删R_User_App
			mResMngServer.mRepo.deleteAll(mClientAppId_R_UA.get(aClientAppId)) ;
			// 6. 删ClientApp
			mResMngServer.mRepo.delete(clientApp) ;
			return true ;
		}
		return false ;
	}
	
	@Override
	public Authority getAuthorityByCode(String aAppId, String aCode)
	{
		return mCode_Authority.getFirst(aCode , a->a.getClientAppId().equals(aAppId)) ;
	}
	
	@Override
	public synchronized void deleteAuthority(String aAuthorityId)
	{
		String appId = null ;
		R_Role_Authority[] rs_2 = mAuthorityId_R_RA.get(aAuthorityId) ;
		Set<String> userIds = null ;
		if(XC.isNotEmpty(rs_2))
		{
			Set<String> roleIds = XC.hashSet() ;
			mResMngServer.mRepo.deleteAll(R_Role_Authority.class , XC.extract(rs_2, (r)->{
				roleIds.add(r.getRoleId()) ;
				return r.getId() ;
			}, String.class)) ;
			// 找这些角色相关的人
			userIds = XC.hashSet() ;
			for(String roleId : roleIds)
			{
				R_User_ResSpace_Role[] rs_3 = mRoleId_R_URR.get(roleId) ;
				if(XC.isNotEmpty(rs_3))
				{
					for(R_User_ResSpace_Role r : rs_3)
					{
						userIds.add(r.getUserId()) ;
					}
				}
			}
		}
		// 删除权限
		mResMngServer.mRepo.delete(Authority.class, aAuthorityId) ;
		if(XC.isNotEmpty(userIds))
			notifyUserAuthoritiesChanged(appId, userIds) ;
	}
	
	@Override
	public void bindAuthorityToRole(String aAuthId, String aRoleId, String aUserId)
	{
		R_Role_Authority[] rs = mAuthorityId_R_RA.get(aAuthId , r->r.getRoleId().equals(aRoleId)) ;
		if(XC.isNotEmpty(rs))
			return ;
		
		Authority auth = mResMngServer.mRepo.getByBid(Authority.class, aAuthId) ;
		Assert.notNull(auth , "不存在id为%s的权限！" , aAuthId) ;
		
		Role role = mResMngServer.mRepo.getByBid(Role.class, aRoleId) ;
		Assert.notNull(role , "不存在id为%s的角色！" , aRoleId) ;
		
		// 必须属于同一个ClientApp
		Assert.equals(auth.getClientAppId() , role.getClientAppId() , "指定的角色和权限不是同一个ClientApp的！") ;		
		
		R_Role_Authority r = mResMngServer.mRepo.newBean(R_Role_Authority.class) ;
		r.setAuthorityId(aAuthId) ;
		r.setAuthorityCode(auth.getCode()) ;
		r.setRoleId(aRoleId) ;
		r.setCreateTime(new Date()) ;
		r.setCreateUserId(JCommon.defaultIfEmpty(aUserId , AppConsts.sUserId_sys)) ;
		
		//
		notifyRoleAuthoritiesChanged(aRoleId) ;
	}
	
	@Override
	public void unbindAuthorityToRole(String aAuthId, String aRoleId, String aUserId)
	{
		R_Role_Authority[] rs = mAuthorityId_R_RA.get(aAuthId , r->r.getRoleId().equals(aRoleId)) ;
		if(XC.isNotEmpty(rs))
		{
			for(R_Role_Authority r : rs)
			{
				mResMngServer.mRepo.delete(r) ;
				// 取得角色相关的人
				notifyRoleAuthoritiesChanged(aRoleId) ;
				return ;
			}
		}
	}
	
	@Override
	public ClientApp createClientApp(String aAppName,
			String aDescription,
			String aCompany,
			boolean aGenCredential,
			String aUserId)
	{
		// 1. 先检查appName是否重复
		ClientApp.BClientApp appInfo = new ClientApp.BClientApp() ;
		appInfo.setName(aAppName) ;
		appInfo.setDescription(aDescription) ;
		appInfo.setCompany(aCompany) ;
		
		return createClientApp(appInfo, aGenCredential, aUserId) ;
	}
	
	@Override
	public ClientApp createClientApp(BClientApp aClientAppInfo 
			, boolean aGenCredential
			, String aUserId)
	{
		// 1. 先检查appName是否重复
		ClientApp app = getClientAppByName(aClientAppInfo.getName()) ;
		Assert.isNull(app, "已经存在名为%s的ClientApp!" ,aClientAppInfo.getName()) ;
		
		if(aGenCredential)
		{
			aClientAppInfo.setAppKey(XString.randomString(16)) ;
			aClientAppInfo.setAppSecret(XString.randomString(32)) ;
		}
		
		app = mResMngServer.mRepo.newBean(ClientApp.class) ;
		app.update(aClientAppInfo, aUserId, true) ;
		
		Date now = app.getCreateTime() ;
		// 将认证中心基本的认证接口授权给它
		List<Api> apiList = mResMngServer.mAuthCenterDataMng.getBasicAuthApis() ;
		if(XC.isNotEmpty(apiList))
		{
			for(Api api : apiList)
			{
				R_App_Api r = mResMngServer.mRepo.newBean(R_App_Api.class) ;
				r.setClientAppId(app.getId()) ;
				r.setApiId(api.getId()) ;
				r.setCreateTime(now) ;
				r.setCreateUserId(aUserId) ;
			}
		}
		// 暂时直接这么设定，未在界面上提供设置项
		app.setAuthMethods(new String[] {"basic","post"}) ;
		return app ;
	}
	
	@Override
	public List<ClientApp> getClientApps()
	{
		List<ClientApp> apps = XC.arrayList() ;
		mResMngServer.mRepo.forEach(ClientApp.class , (app)->{
			apps.add(app) ;
			return true ;
		}) ;
		return apps ;
	}
	
	@Override
	public void notifyUserAuthoritiesChanged(UserAuthoritiesChangeEvent aEvent)
	{
		IUserAuthoritiesChangeListener[] lsns = mLsns ;
		if(lsns != null && lsns.length>0)
		{
			for(IUserAuthoritiesChangeListener lsn : lsns)
			{
				lsn.accept(aEvent) ;
			}
		}
	}
	
	@Override
	public void notifyUserAuthoritiesChanged(String aAppId , String... aUserIds)
	{
		notifyUserAuthoritiesChanged(new UserAuthoritiesChangeEvent(aAppId , aUserIds)) ;
	}
	
	protected void notifyUserAuthoritiesChanged(String aAppId , Set<String> aUserIds)
	{
		notifyUserAuthoritiesChanged(new UserAuthoritiesChangeEvent(aAppId , aUserIds)) ;
	}
	
	/**
	 * 
	 * 通知角色的权限发生改变
	 * 
	 * @param aRoleId		角色id
	 */
	void notifyRoleAuthoritiesChanged(String aRoleId)
	{
		// 取得角色相关的人
		R_User_ResSpace_Role[] rs = mRoleId_R_URR.get(aRoleId) ;
		if(XC.isNotEmpty(rs))
		{
			notifyUserAuthoritiesChanged(ResSpace.getClientAppIdFrom(rs[0].getResSpaceId())
					, XC.extractAsHashSet(rs, R_User_ResSpace_Role::getUserId)) ;
		}
		return ;
	}
	
	@Override
	public AppAmounts getAppAmounts()
	{
		AppAmounts appAmounts = new AppAmounts() ;
		String appOnly = ClientApp.sAGT_AppOnly.getValue() ;
		mResMngServer.mRepo.forEach(ClientApp.class , (app)->{
			// 排除认证中心本身
			if(!AppConsts.sAppName.equals(app.getName()))
			{
				String[] grantTypes = app.getGrantTypes() ;
				if(XC.isEmpty(grantTypes)
						|| (grantTypes.length == 1 && appOnly.equals(grantTypes[0])))
				{
					// 是服务
					appAmounts.increaseAndGetMsAppAmount() ;
				}
				else
					appAmounts.increaseAndGetWebAppAmount() ;
			}
			return true ;
		}) ;
		return appAmounts ;
	}

	@Override
	public void addListener(IUserAuthoritiesChangeListener aLsn)
	{
		if(aLsn != null && !XC.contains(mLsns, aLsn))
		{
			mLsns = XC.merge(mLsns, aLsn) ;
 		}
	}

	@Override
	public void removeListener(IUserAuthoritiesChangeListener aLsn)
	{
		if(aLsn != null)
		{
			mLsns = XC.remove(mLsns ,aLsn) ;
		}
	}

	@Override
	public synchronized void grantClientAppToUser(String aAppId, String aTargetUserId, String aOperUserId)
	{
		Assert.notEquals(mResMngServer.mAdminUserId , aTargetUserId , "admin用户不能修改可访问的应用！");
		R_User_App[] rs = mUserId_R_UA.get(aTargetUserId , r->r.getClientAppId().equals(aAppId)) ;
		if(XC.isNotEmpty(rs))
			return ;
		
		User user = mResMngServer.mRepo.getByBid(User.class, aTargetUserId) ;
		Assert.notNull(user , "不存在id为%s的用户！" , aTargetUserId) ;
		ClientApp app = mResMngServer.mRepo.getByBid(ClientApp.class, aAppId) ;
		Assert.notNull(app , "不存在id为%s的应用！" , aAppId) ;
		R_User_App r = mResMngServer.mRepo.newBean(R_User_App.class) ;
		r.setUserId(aTargetUserId) ;
		r.setClientAppId(aAppId) ;
		r.setCreateTime(new Date()) ;
		r.setCreateUserId(aOperUserId) ;
		// 这个时候用户肯定没登陆这个app，所以不用通知用户在此应用的权限改变
	}
	
	@Override
	public void ungrantClientAppToUser(String aAppId, String aTargetUserId, String aOperUserId)
	{
		Assert.notEquals(mResMngServer.mAdminUserId , aTargetUserId , "admin用户不能修改可访问的应用！");
		R_User_App[] rs = mUserId_R_UA.get(aTargetUserId , r->r.getClientAppId().equals(aAppId)) ;
		if(XC.isEmpty(rs))
			return ;
		
		for(R_User_App r : rs)
		{
			// 用户与此应用下角色的关联也需要断开
			R_User_ResSpace_Role[] rs_1 = mUserId_R_URR.get(aTargetUserId , r0->
					ResSpace.getClientAppIdFrom(r0.getResSpaceId()).equals(aAppId)) ;
			
			mResMngServer.mRepo.deleteAll(rs_1) ;
			
			mResMngServer.mRepo.delete(r) ;
			
			notifyUserAuthoritiesChanged(aAppId, aTargetUserId) ;
		}
	}

	@Override
	public List<ClientApp> getClientAppsOfUserCanVisit(String aUserId)
	{
		User user = mResMngServer.mRepo.getByBid(User.class , aUserId) ;
		Assert.notNull(user, "不存在id为%s的用户！", aUserId) ;
		R_User_App[] rs = mUserId_R_UA.get(aUserId) ;
		if(XC.isEmpty(rs))
			return Collections.emptyList() ;
		Map<String , ClientApp> appMap = XC.linkedHashMap() ;
		for(R_User_App r : rs)
		{
			ClientApp app = mResMngServer.mRepo.getByBid(ClientApp.class, r.getClientAppId()) ;
			if(app != null)
				appMap.putIfAbsent(app.getId() , app) ;
		}
		List<ClientApp> clientApps = XC.arrayList(appMap.values()) ;
		clientApps.sort(ClientApp.sDefaultComp) ;
		return clientApps ;
	}
	
	@Override
	public Collection<User> getUsersOfCanVisitClientApp(String aClientAppId)
	{
		R_User_App[] rs = mClientAppId_R_UA.get(aClientAppId) ;
		Map<String , User> userMap = XC.linkedHashMap() ;
		if(XC.isNotEmpty(rs))
		{	
			for(R_User_App r : rs)
			{
				User user = mResMngServer.mRepo.getByBid(User.class, r.getUserId()) ;
				if(user != null)
					userMap.putIfAbsent(user.getId() , user) ;
			}
		}
		return userMap.values() ;
	}
	
	@Override
	public List<Tuples.T2<User, List<Role>>> getUsersOfCanVisitResSpace(String aResSpaceId)
	{
		R_User_ResSpace_Role[] rs = mResSpaceId_R_URR.get(aResSpaceId) ;
		Map<String , Tuples.T2<User, List<Role>>> userMap = XC.hashMap() ;
		if(XC.isNotEmpty(rs))
		{
			for(R_User_ResSpace_Role r : rs)
			{
				Tuples.T2<User , List<Role>> t = userMap.get(r.getUserId()) ;
				if(t == null)
				{
					User user = mResMngServer.mRepo.getByBid(User.class , r.getUserId()) ;
					if(user != null)
					{
						t = Tuples.of(user, XC.arrayList()) ;
						userMap.put(r.getUserId() , t) ;
					}
				}
				if(t != null)
				{
					t.getValue().add(mResMngServer.mRepo.getByBid(Role.class , r.getRoleId())) ;
				}
			}
		}
		return XC.arrayList(userMap.values()) ;
	}
	
	@Override
	public List<ResSpace> getResSpaceOfUserInClientApp(String aUserId, String aClientAppId)
	{
		Set<String> resSpaceIds = XC.hashSet() ;
		mUserId_R_URR.get(aUserId , r->{
			if(ResSpace.getClientAppIdFrom(r.getResSpaceId()).equals(aClientAppId))
			{
				resSpaceIds.add(r.getResSpaceId()) ;
				return true ;
			}
			return false;
		}) ;
				
		return mResMngServer.mRepo.getByBids(ResSpace.class , resSpaceIds) ;
	}

	@Override
	public List<Role> getRoleOfUserInResSpace(String aUserId, String aResSpaceId)
	{
		List<Role> roleList = XC.arrayList() ;
		for(R_User_ResSpace_Role r : mUserId_R_URR.get(aUserId, r->r.getResSpaceId().equals(aResSpaceId)))
		{
			roleList.add(mResMngServer.mRepo.getByBid(Role.class , r.getRoleId())) ;
		}
		return roleList ;
	}

	@Override
	public List<Authority_Role> getAuthoritesForResSpaceType(String aClientAppId , String aResSpaceType)
	{
		// 取得资源空间使用的角色。通过适用资源空间类型来筛选
		Role[] roles = mClientAppId_Role.get(aClientAppId, r->r.getResSpaceType().equals(aResSpaceType)) ;
		if(XC.isNotEmpty(roles))
		{
			List<Authority_Role> arList = XC.arrayList() ;
			for(Role role : roles)
			{
				for(R_Role_Authority r : mRoleId_R_RA.get(role.getId()))
				{
					arList.add(Authority_Role.of(mResMngServer.mRepo.getByBid(Authority.class , r.getAuthorityId())
							, role)) ;
				}
			}
			return arList ;
		}
		return Collections.emptyList() ;
	}
	
	@Override
	public List<Api> getApisOfClientAppCanInvoke(String aClientAppId)
	{
		R_App_Api[] rs = mClientAppId_R_AA.get(aClientAppId) ;
		return XC.extractNotNull(rs, r->mResMngServer.mRepo.getByBid(Api.class ,r.getApiId())) ;
	}

	@Override
	public R_User_ResSpace_Role getR_User_ResSpace_Role(String aId)
	{
		return mResMngServer.mRepo.getByBid(R_User_ResSpace_Role.class , aId) ;
	}
	
	@Override
	public String grantApiToClientApp(String aAppId, String aApiId, String aUserId)
	{
		R_App_Api[] rs = mClientAppId_R_AA.get(aAppId , r->r.getApiId().equals(aApiId)) ;
		if(XC.isNotEmpty(rs))
			return rs[0].getId() ;
		
		R_App_Api r = mResMngServer.mRepo.newBean(R_App_Api.class) ;
		r.setApiId(aApiId) ;
		r.setClientAppId(aAppId) ;
		r.setCreateTime(new Date()) ;
		r.setCreateUserId(aUserId) ;
		return r.getId() ;
	}
	
	@Override
	public void ungrantApiToClientApp(String aAppId, String aApiId, String aOperUserId)
	{
		R_App_Api[] rs = mClientAppId_R_AA.get(aAppId , r->r.getApiId().equals(aApiId)) ;
		if(XC.isNotEmpty(rs))
		{
			mResMngServer.mRepo.deleteAll(rs) ;
		}
	}

	@Override
	public Authority getAuthority(String aId)
	{
		return mResMngServer.mRepo.getByBid(Authority.class , aId) ;
	}

	@Override
	public Authority updateAuthority(BAuthority aAuth , String aUserId)
	{
		Authority auth = mResMngServer.mRepo.getByBid(Authority.class , aAuth.getId()) ;
		Assert.notNull(auth , "不存在id为%s的权限！", aAuth.getId()) ;
		Assert.equals(auth.getClientAppId() , aAuth.getClientAppId() ,  "更新权限不能修改所属ClientApp!") ;
		boolean codeChanged = aAuth.getCode().equals(auth.getCode()) ;
		auth.update(aAuth, aUserId, false) ;
		if(codeChanged)
		{
			// 通知权限改变，得先知道哪些用户跟它相关
			Set<String> userIds = getUserIdsRelatedToAuthority(auth.getId()) ;
			if(!userIds.isEmpty())
				notifyUserAuthoritiesChanged(auth.getClientAppId() , userIds) ;
		}
		return auth ;
	}
	
	Set<String> getUserIdsRelatedToAuthority(String aAuthorityId)
	{
		Set<String> userIds = XC.hashSet() ;
		for(R_Role_Authority r : mAuthorityId_R_RA.get(aAuthorityId))
		{
			for(R_User_ResSpace_Role r1 : mRoleId_R_URR.get(r.getRoleId()))
			{
				userIds.add(r1.getUserId()) ;
			}
		}
		return userIds ;
	}

	@Override
	public ClientApp getClientAppByAppKey(String aAppKey)
	{
		return mAppKey_CA.getFirst(aAppKey) ;
	}

	@Override
	public RequestMappingHandlerMapping getInvokableApiMapping(String aAppKey)
	{
		ClientApp app = getClientAppByAppKey(aAppKey) ;
		if(app != null)
		{
			RequestMappingHandlerMapping apiMapping = app.getInvokableApiMapping() ;
			if(apiMapping == null)
			{
				apiMapping = new RequestMappingHandlerMapping() ;
				// 查api
				R_App_Api[] rs = mClientAppId_R_AA.get(app.getId()) ;
				if(XC.isNotEmpty(rs))
				{
					
					for(R_App_Api r : rs)
					{
						Api api = mResMngServer.mRepo.getByBid(Api.class, r.getApiId()) ;
						if(api != null)
						{
							apiMapping.registerMapping(api.getRequestMappingInfo() 
									, JCommon.sNullObject , mPlaceHoldMethod) ;
						}
					}
				}
				app.setInvokableApiMapping(apiMapping) ;
			}
			return apiMapping ;
		}
		return null ;
	}
	
	@Override
	public boolean canVisitApp(String aUserId, String aAppId)
	{
		return XC.isNotEmpty(mUserId_R_UA.get(aUserId , r->r.getClientAppId().equals(aAppId))) ;
	}

	@Override
	public Tuples.T2<ResSpace , Boolean> createOrUpdateResSpace(BResSpace aResSpace , String aUserId)
	{
		String clientAppId = aResSpace.getClientAppId() ;
		ResSpace[] rss = mResId_RS.get(aResSpace.getResId() , r->r.getClientAppId().equals(clientAppId)) ;
		if(XC.isNotEmpty(rss))
		{
			// 更新
			boolean changed = rss[0].update(aResSpace, aUserId, false) ;
			return Tuples.of(rss[0] , changed) ;
 		}
		// 新建
		ResSpace rs = mResMngServer.mRepo.newBean(ResSpace.class , ResSpace
				.spliceResSpaceId(aResSpace.getClientAppId(), aResSpace.getResId())) ;
		rs.update(aResSpace, aUserId, true) ;
		return Tuples.of(rs , Boolean.TRUE);
	}
	
	@Override
	public void createOrUpdateSubspaces(List<BResSpace> aResSpaces, boolean aDeleteIfNoExists , String aUserId)
	{
		String clientAppId = aResSpaces.get(0).getClientAppId() ;
		Set<String> resIds = null ;
		if(aDeleteIfNoExists)
			resIds = XC.hashSet() ;
		for(ResSpace.BResSpace brs : aResSpaces)
		{
			if(aDeleteIfNoExists)
				resIds.add(brs.getResId()) ;
			ResSpace[] rss = mResId_RS.get(brs.getResId() , r->r.getClientAppId().equals(clientAppId)) ;
			if(rss.length > 1)
			{
				// 删除一个资源id，多个资源空间，多出来的那部分
				mResMngServer.mRepo.deleteAll(XC.subArray(rss, 1, rss.length-1)) ;
			}
			else if(rss.length == 0)
			{
				// 创建信息的
				ResSpace rs = mResMngServer.mRepo.newBean(ResSpace.class) ;
				rs.update(brs , aUserId, true) ;
				continue ;
			}
			rss[0].update(brs, aUserId, false) ;
		}
		if(aDeleteIfNoExists)
		{
			Set<String> resIds0 = resIds ;
			mResMngServer.mRepo.deleteAll(mClientAppId_RS.get(clientAppId, rs->!resIds0.contains(rs.getResId()))) ;
		}
	}
	
	@Override
	public ResSpace getResSpace(String aResSpaceId)
	{
		return mResMngServer.mRepo.getByBid(ResSpace.class , aResSpaceId) ;
	}
	
	@Override
	public ResSpace[] getResSpaceOfClientApp(String aClientAppId)
	{
		return mClientAppId_RS.get(aClientAppId) ;
	}
	
	@Override
	public void grantResSpaceRoleToUser(String aResSpaceId, String[] aRoleNames, String aTargetUserId
			, String aOperUserId)
	{
		if(XC.isEmpty(aRoleNames))
			return ;
		
		String clientAppId = ResSpace.getClientAppIdFrom(aResSpaceId) ;
		
		IClientAppDataManager clientAppDataMng = mResMngServer.getClientAppDataMng() ;
		// 检查一下资源空间是否存在
		ResSpace resSpace = clientAppDataMng.getResSpace(aResSpaceId)  ;
		Assert.notNull(resSpace , "无效的资源空间id：%s" , aResSpaceId) ;
		
		List<String> roleIds = XC.arrayList() ;
		for(String roleName : aRoleNames)
		{
			Role role = clientAppDataMng.getRoleByName(clientAppId ,  roleName) ;
			if(role != null)
			{
				Assert.equals(role.getResSpaceType() , resSpace.getType() , "角色 %s[%s] 适用的资源空间类型[%s]和指定的资源空间类型[%s]不匹配！"
						, role.getName() , role.getId() , role.getResSpaceType() , resSpace.getType()) ;
				roleIds.add(role.getId()) ;
			}
			else
				mLogger.warn("ClientApp[{}]的无效的资源空间角色：{}" , clientAppId , roleName) ;
		}
		if(!roleIds.isEmpty())
		{
			for(String roleId : roleIds)
			{
				mLogger.info("给用户[{}]在应用[{}]的资源空间[{}]上授予角色[{}]" , aTargetUserId , clientAppId , aResSpaceId , roleId) ;
				clientAppDataMng.grantRoleToUser(aResSpaceId , roleId , aTargetUserId , AppConsts.sUserId_sys) ;
			}
			notifyUserAuthoritiesChanged(clientAppId , aTargetUserId) ;
		}
	}
	
	@Override
	public boolean deleteResSpaceByResId(String aResId, String aUserId)
	{
		// 查询资源空间
		ResSpace rs = mResId_RS.getFirst(aResId) ;
		if(rs == null)
			return false ;
		// 删R_User_ResSpace_Role
		mResMngServer.mRepo.deleteAll(mResSpaceId_R_URR.get(rs.getId())) ;
		// 删ResSpace
		mResMngServer.mRepo.delete(rs) ;
		return true ;
	}

	@Override
	public boolean canInvokeApiOfClientApp(String aAppId, String aApiName)
	{
		Set<String> apiNameSet = mAppCanVistApiNames.get(aAppId) ;
		if(apiNameSet == null)
		{
			synchronized ((aAppId+".appCanVisit").intern())
			{
				apiNameSet = mAppCanVistApiNames.get(aAppId) ;
				if(apiNameSet == null)
				{
					apiNameSet = XC.hashSet() ; 
					R_App_Api[] rs = mClientAppId_R_AA.get(aAppId) ;
					if(XC.isNotEmpty(rs))
					{
						for(R_App_Api r : rs)
						{
							Api api = mResMngServer.mRepo.getByBid(Api.class, r.getApiId()) ;
							if(api != null)
							{
								apiNameSet.add(api.getName()) ;
							}
						}
					}
					mAppCanVistApiNames.put(aAppId, apiNameSet) ;
				}
			}
		}
		return apiNameSet.contains(aApiName) ;
	}
}
