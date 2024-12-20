package team.sailboat.ms.ac.server;

import java.sql.SQLException;
import java.util.Date;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dpa.DRepository;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.ms.ac.AppAuths;
import team.sailboat.ms.ac.AppConsts;
import team.sailboat.ms.ac.dbean.Api;
import team.sailboat.ms.ac.dbean.Authority;
import team.sailboat.ms.ac.dbean.ClientApp;
import team.sailboat.ms.ac.dbean.OrgUnit;
import team.sailboat.ms.ac.dbean.R_App_Api;
import team.sailboat.ms.ac.dbean.R_OrgUnit_User;
import team.sailboat.ms.ac.dbean.R_Role_Authority;
import team.sailboat.ms.ac.dbean.R_User_App;
import team.sailboat.ms.ac.dbean.R_User_ResSpace_Role;
import team.sailboat.ms.ac.dbean.ResSpace;
import team.sailboat.ms.ac.dbean.Role;
import team.sailboat.ms.ac.dbean.User;

/**
 * 
 * 资源管理服务组件（核心）<br />
 * 
 * 了解更多，见<a href="https://www.yuque.com/okgogogooo/nkpqbh/cvedgvly5vn7b4re">《认证中心架构》</a>
 *
 * @author yyl
 * @since 2024年10月30日
 */
public class ResourceManageServer
{
	/**
	 * 用户的数据管理器		<br />
	 * 包括：用户(User)
	 */
	final IUserDataManager mUserDataMng ;
	
	/**
	 * 客户端的数据管理器		<br />
	 * 包括：角色(Role)，权限(Authority)，资源空间(ResSpace)，客户端应用(ClientApp)，及其关联关系
	 */
	final IClientAppDataManager mClientAppDataMng ;
	
	/**
	 * 认证中心的数据管理器		<br />
	 * 包括：组织单元(OrgUnit)、Api
	 */
	final IAuthCenterDataManager mAuthCenterDataMng ;
	
	final DRepository mRepo ;
	
	/**
	 * 认证中心在ClientApp中id。		<br>
	 * 认证中心会在ClientApp中注册自己，以实现用户在认证中心的权限控制和管理
	 */
	String mClientAppId_SailAC ;
	
	/**
	 * Admin的密码是否已经设置		<br />
	 * 在认证中心初次部署之后，访问认证中心的页面，会要求设置admin的密码。设置之后就不会出现了。
	 */
	boolean mAdminPasswordSetted ;
	
	/**
	 * admin用户的id
	 */
	String mAdminUserId ;
	
	/**
	 * 
	 * 用户在认证中心的权限(GrantedAuthority)提供者
	 * 
	 */
	IUserAuthsProviderInAuthCenter mUserAuthsProviderInAuthCenter ;
	
	public ResourceManageServer(DRepository aRepo)
	{
		mRepo = aRepo ;
		mUserDataMng = new DefaultUserDataManager(this) ;
		mClientAppDataMng = new DefaultClientAppDataManager(this) ;
		mAuthCenterDataMng = new DefaultAuthCenterDataManager(this) ;
	}
	
	public void init() throws SQLException
	{
		mRepo.load(User.class)
				.load(Role.class)
				.load(Authority.class)
				.load(ClientApp.class)
				.load(OrgUnit.class)
				.load(Api.class)
				.load(ResSpace.class)
				.load(R_OrgUnit_User.class)
				.load(R_User_ResSpace_Role.class)
				.load(R_User_App.class)
				.load(R_Role_Authority.class)
				.load(R_App_Api.class)
				;
		mUserAuthsProviderInAuthCenter = userId->mClientAppDataMng
				.getAuthoritysOfUserInClientApp(userId, mClientAppId_SailAC) ;
		mRepo.forEach(User.class , (user)->{
			user.setUserAuthsProviderInAuthCenter(mUserAuthsProviderInAuthCenter) ;
			return true ;
		}) ;
		
		mClientAppDataMng.init();
		mUserDataMng.init();
		mAuthCenterDataMng.init();
		
		if(mRepo.getSize(User.class) == 0)
		{
			// 系统没有初始化，给它注入admin用户
			injectData();
			mAdminPasswordSetted = false ;
		}
		else
		{
			ClientApp app = mClientAppDataMng.getClientAppByName(AppConsts.sAppName) ;
			Assert.notNull(app , "找不到应用[%s]" , AppConsts.sAppName) ;
			mClientAppId_SailAC = app.getId() ;
			mClientAppDataMng.updateAppAuths(mClientAppId_SailAC , AppAuths.getAppAuthStatement()) ;
			User user = mUserDataMng.loadUserByUsername(AppConsts.sUser_admin) ;
			Assert.notNull(user, "没有admin用户！") ;
			mAdminUserId = user.getId() ;
			mAdminPasswordSetted = XString.isNotEmpty(user.getPassword()) ;
			R_User_ResSpace_Role[] rs = mClientAppDataMng.getR_User_ResSpace_RoleOfUserInApp(mAdminUserId , mClientAppId_SailAC) ;
			if(XC.isEmpty(rs))
			{
				// admin 用户还没有授予任何角色，那么将管理员角色授权给它
				Role adminRole = mClientAppDataMng.getRoleByName(mClientAppId_SailAC , AppConsts.sRoleName_admin) ;
				mClientAppDataMng.grantRoleToUser(ResSpace.getDefaultGlobalResSpaceId(mClientAppId_SailAC) 
						, adminRole.getId() , mAdminUserId , AppConsts.sUserId_sys) ;
			}
		}
	}
	
	/**
	 * 
	 * 获取用户在认证中心的权限(GrantedAuthority)提供者
	 * 
	 * @return
	 */
	public IUserAuthsProviderInAuthCenter getUserAuthsProviderInAuthCenter()
	{
		return mUserAuthsProviderInAuthCenter;
	}
	
	void injectData()
	{
		Date now = new Date() ;
		//应用
		ClientApp app = mClientAppDataMng.createClientApp(AppConsts.sAppName , AppConsts.sAppDesc
				, AppConsts.sCompany , false , AppConsts.sUserId_sys) ; 
		app.setSimpleName("SAC") ;
		
		mClientAppId_SailAC = app.getId() ;
		
		User.BUser buser = new User.BUser() ;
		buser.setUsername(AppConsts.sUser_admin) ;
		buser.setRealName("超级管理员") ;
		User user = mUserDataMng.createUser(buser, AppConsts.sUserId_sys) ;
 		
		String adminUserId = user.getId() ;
		
		// admin 访问 域
		R_User_App r = mRepo.newBean(R_User_App.class) ;
		r.setUserId(adminUserId) ;
		r.setClientAppId(mClientAppId_SailAC) ;
		r.setCreateUserId(AppConsts.sUserId_sys) ;
		r.setCreateTime(now) ;
		
		// 创建缺省资源空间
		mClientAppDataMng.createOrUpdateDefaultGlobalResSpace(mClientAppId_SailAC , AppConsts.sUserId_sys) ;
		// 创建SailAC的ClientApp类型资源空间
		ResSpace.BResSpace brs = new ResSpace.BResSpace() ;
		brs.setClientAppId(mClientAppId_SailAC) ;
		brs.setResId(mClientAppId_SailAC) ;
		brs.setResName(AppConsts.sAppName) ;
		brs.setType(AppConsts.sResSpaceType_ClientApp) ;
		mClientAppDataMng.createOrUpdateResSpace(brs, AppConsts.sUserId_sys) ;
		
	
		mClientAppDataMng.updateAppAuths(mClientAppId_SailAC , AppAuths.getAppAuthStatement()) ;
		
		mClientAppDataMng.grantRoleToUser(ResSpace.getDefaultGlobalResSpaceId(mClientAppId_SailAC)
				, mClientAppDataMng.getRoleByName(mClientAppId_SailAC, AppConsts.sRoleName_admin).getId()
				, adminUserId
				, AppConsts.sUserId_sys) ;
		
		mAdminUserId = adminUserId ;
	}
	
	/**
	 * 
	 * 取得认证中心的ClientApp id
	 * 
	 * @return
	 */
	public String getClientAppId_SailAC()
	{
		return mClientAppId_SailAC;
	}
	
	public IUserDataManager getUserDataMng()
	{
		return mUserDataMng;
	}
	
	public IClientAppDataManager getClientAppDataMng()
	{
		return mClientAppDataMng;
	}
	
	public IAuthCenterDataManager getAuthCenterDataMng()
	{
		return mAuthCenterDataMng;
	}
	
	public void setAdminPasswordSetted(boolean aAdminPasswordSetted)
	{
		mAdminPasswordSetted = aAdminPasswordSetted;
	}
	
	public boolean isAdminPasswordSetted()
	{
		return mAdminPasswordSetted;
	}
}
