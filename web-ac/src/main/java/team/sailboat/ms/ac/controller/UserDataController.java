package team.sailboat.ms.ac.controller;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validator;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.http.IdentityTrace;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.fan.time.XTime;
import team.sailboat.commons.ms.ac.InnerProtectedApi;
import team.sailboat.commons.ms.bean.Page;
import team.sailboat.commons.ms.crypto.RSAKeyPairMaker4JS;
import team.sailboat.commons.ms.util.WebUtils;
import team.sailboat.commons.ms.valid.ValidateUtils;
import team.sailboat.commons.web.ac.ResId;
import team.sailboat.ms.ac.AppConfig;
import team.sailboat.ms.ac.AppConsts;
import team.sailboat.ms.ac.IAppAuths;
import team.sailboat.ms.ac.bean.ClientAppBrief;
import team.sailboat.ms.ac.bean.OrgUnit4User;
import team.sailboat.ms.ac.bean.UserItem;
import team.sailboat.ms.ac.dbean.ClientApp;
import team.sailboat.ms.ac.dbean.OrgUnit;
import team.sailboat.ms.ac.dbean.R_OrgUnit_User;
import team.sailboat.ms.ac.dbean.ResSpace;
import team.sailboat.ms.ac.dbean.Role;
import team.sailboat.ms.ac.dbean.User;
import team.sailboat.ms.ac.server.IAuthCenterDataManager;
import team.sailboat.ms.ac.server.IClientAppDataManager;
import team.sailboat.ms.ac.server.IUserDataManager;
import team.sailboat.ms.ac.server.ResourceManageServer;
import team.sailboat.ms.ac.utils.SecurityUtils;

/**
 * 用户的数据接口。给认证中心的web界面使用			<br />
 *
 * 包括：用户信息
 *	
 * @author yyl
 * @since 2024年10月30日
 */
@RestController
public class UserDataController
{
	final Logger mLogger = LoggerFactory.getLogger(getClass()) ;
	
	@Autowired
	Validator mValidator ;
	
	@Autowired
	ResourceManageServer mResMngServer ;
	
	@Autowired
	RSAKeyPairMaker4JS mRSAMaker;
	
	@Autowired
	AppConfig mAppConf ;
	
	@Autowired
	PasswordEncoder mPasswordEncoder;
	
	@Qualifier("resetPasswdUsernames")
	@Autowired
	Map<String, String> mResetPasswdUserNames;
	
	@Operation(description = "用户重置过期的密码。参数不要在url中传递")
	@Parameters({
		@Parameter(name="authToken" , description = "一个临时标识码，表名用户已经验证通过") ,
		@Parameter(name="codeId" , description = "动态RSA秘钥的标识码。Https协议下，可以不用加密") ,
		@Parameter(name="password" , description = "密码。用动态RSA秘钥的公钥加密过后的密码。Https协议下，可以不用加密") ,
	})
	@InnerProtectedApi
	@PostMapping(value="/user/self/password/reset/forExpired" , consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public void resetExpiredPassword(@RequestParam("authToken") String aAuthToken,
			@RequestParam(name="codeId" , required = false)String aCodeId,
			@RequestParam("password") String aPassword,
			HttpServletRequest aReq,
			HttpServletResponse aResp) throws Exception
	{
		Assert.notEmpty(aPassword, "新密码不能为空！");
		String username = mResetPasswdUserNames.remove(aAuthToken);
		Assert.notEmpty(username, "无效的AuthToken：%s", aAuthToken);

		User user = mResMngServer.getUserDataMng().loadUserByUsername(username);
		Assert.notNull(user, "无效的用户名：%s", username);

		aPassword = mRSAMaker.decrypt4js(aCodeId, aPassword);
		user.setPassword(mPasswordEncoder.encode(aPassword));
		Date date = XTime.plusDays(XTime.today(), mAppConf.getCredentialRenewalDays());
		user.setCredentialsExpiredTime(date);
		mLogger.info("用户[{}]的密码已经重新设置，凭据过期时间延长至{}", user.getRealName(), XTime.format$yyyyMMddHHmmssSSS(date));
		String authToken = XString.randomString(32);
		mResetPasswdUserNames.put(authToken,
				new JSONObject().put("username", user.getUsername())
								.put("password", aPassword)
								.toJSONString());
		
		aResp.sendRedirect(aReq.getContextPath() + AppConsts.sApiPath_login +"?authToken=" + authToken);
	}
	

	/**
	 * 内部已经做了安全校验			<br />
	 * 用户重置自己的密码
	 * @param aUsername
	 * @param aPassword
	 * @throws Exception 
	 */
	@Operation(description = "用户重置自己的密码")
	@Parameters({
		@Parameter(name="codeId" , description = "动态RSA秘钥的标识码。Https协议下，可以不用加密") ,
		@Parameter(name="username" , description = "用户名") ,
		@Parameter(name="oldPassword" , description = "旧密码。用动态RSA秘钥的公钥加密过后的密码。Https协议下，可以不用加密") ,
		@Parameter(name="password" , description = "密码。用动态RSA秘钥的公钥加密过后的密码。Https协议下，可以不用加密") ,
	})
	@InnerProtectedApi
	@PostMapping("/user/self/password/_reset")
	public void resetSelfPassword(@RequestParam(name="codeId" , required = false) String aCodeId,
			@RequestParam("username") String aUsername,
			@RequestParam("oldPassword") String aOldPassword,
			@RequestParam("password") String aPassword,
			HttpServletRequest aReq) throws Exception
	{
		Assert.notEmpty(aPassword, "新密码不能为空！");
		Assert.isNotTrue(aPassword.equals(aOldPassword), "新旧密码不能相同！");
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null)
		{
			mLogger.warn("已拒绝！未登陆，试图重置用户[{}]的密码。{}", aUsername, IdentityTrace.get(aReq));
			return;
		}
		User user = (User) auth.getPrincipal();
		if (JCommon.unequals(user.getUsername(), aUsername))
		{
			mLogger.warn("已拒绝！登陆用户[{}]试图修改用户[{}]的密码！", user.getUsername(), aUsername);
			return;
		}
		// 对aPassword进行解密
		aOldPassword = mRSAMaker.decrypt4js(aCodeId, aOldPassword);
		aPassword = mRSAMaker.decrypt4js(aCodeId, aPassword);

		Assert.isTrue(mPasswordEncoder.matches(aOldPassword, user.getPassword()), "旧密码不正确！");
		user.setPassword(mPasswordEncoder.encode(aPassword));
		mLogger.info("用户[{}]重置了自己的密码。{}", IdentityTrace.get(aReq));
	}
	
	/**
	 * 设置 admin用户的密码，只有系统部署的时候，可以设置一次
	 * @param aPassword
	 * @param aReq
	 * @throws Exception 
	 */
	@Operation(description = "设置 admin用户的密码，只有系统部署的时候，可以设置一次")
	@Parameters({
		@Parameter(name="codeId" , description = "动态RSA秘钥的标识码。Https协议下，可以不用加密") ,
		@Parameter(name="password" , description = "密码。用动态RSA秘钥的公钥加密过后的密码。Https协议下，可以不用加密") ,
	})
	@InnerProtectedApi
	@PostMapping(value="/user/password/admin" , consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String setAdminPassword(@RequestParam(name="codeId" , required = false) String aCodeId,
			@RequestParam("password") String aPassword,
			HttpServletRequest aReq,
			HttpServletResponse aRes,
			Model aModel) throws Exception
	{
		User user = mResMngServer.getUserDataMng().loadUserByUsername(AppConsts.sUser_admin);
		Assert.notNull("不存在指定的用户[%s]", AppConsts.sUser_admin);
		if (XString.isNotEmpty(user.getPassword()))
		{
			// 说明已经设置过admin的密码了，此接口就不能再调用了
			mLogger.warn("已拒绝！admin已经设置密码，拒绝通过此无需认证的接口重置密码。{}", IdentityTrace.get(aReq));
			aModel.addAttribute("code", HttpStatus.FORBIDDEN.value());
			aModel.addAttribute("message", "已拒绝！admin已经设置密码，拒绝通过此无需认证的接口重置密码");
			return WebUtils.isWebRequest(aReq) ? "redirect:error" : "";
		}
		// 对aPassword进行解密
		aPassword = mRSAMaker.decrypt4js(aCodeId, aPassword);
		user.setPassword(mPasswordEncoder.encode(aPassword));
		mResMngServer.setAdminPasswordSetted(true);
		mLogger.info("admin的密码被初始化。{}", IdentityTrace.get(aReq).toString());
		// 如果是ajax请求的话
		if (WebUtils.isWebRequest(aReq))
		{
			aRes.sendRedirect(aReq.getContextPath() + AppConsts.sViewPath_login);
		}
		return "";
	}
	
	@Operation(description = "取得所有用户")
	@Parameters({
		@Parameter(name="pageSize" , description = "每页数据量") ,
		@Parameter(name="pageIndex" , description = "页码，从0开始") ,
	})
	@GetMapping(value="/user/all" , produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<UserItem> getUsersOfPage(@RequestParam(name="pageSize" , required = false , defaultValue = "100") int aPageSize
			, @RequestParam(name="pageIndex" , required = false , defaultValue = "0") int aPageIndex)
	{
		IUserDataManager userDataMng = mResMngServer.getUserDataMng() ;
		Collection<User> users = userDataMng.getAllUsersOrderByRealNameAsc() ;
		IAuthCenterDataManager acDataMng = mResMngServer.getAuthCenterDataMng() ;
		return Page.of(aPageSize, aPageIndex , XC.arrayList(users , aPageIndex , aPageSize , user->{
			UserItem ui = new UserItem() ;
			user.initBean(ui) ;
			R_OrgUnit_User[] rs = acDataMng.getR_OrgUnit_UserOfUser(user.getId()) ;
			if(rs != null && rs.length >0)
			{
				ui.setOrgUnits(XC.extractAsArrayList(rs , r->{
					OrgUnit4User ouu = new OrgUnit4User();
					ouu.setOrgUnitId(r.getOrgUnitId()) ;
					ouu.setJob(r.getJob()) ;
					OrgUnit ou = acDataMng.getOrgUnit(r.getOrgUnitId()) ;
					if(ou != null)
						ouu.setOrgUnitName(ou.getName()) ;
					else
					{
						// 说明这是一条脏数据，应该注意修复造成脏数据的bug
						mLogger.warn("组织单元[{}]已不存在，但是在R_OrgUnit_User表中仍有与之相关的记录！"
								, r.getOrgUnitId()) ;
					}
					return ouu ;
				})) ;
			}
			ui.setCreateUserDisplayName(userDataMng.getUserDisplayName(user.getCreateUserId())) ;
			ui.setLastEditUserDisplayName(userDataMng.getUserDisplayName(user.getLastEditUserId())) ;
 			return ui ;
		} , true) , users.size()) ;
	}
	
	@Operation(description = "取得当前登录用户的信息")
	@GetMapping(value="/user/current" , produces = MediaType.APPLICATION_JSON_VALUE)
	public User.BUser getCurrentUser()
	{
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return user.asBean() ;
	}
	
	@Operation(description = "用户的数量统计，包括总数(totalAmount)、锁定用户数量(lockedAmount)，未被锁的用户数量(unlockedAmount)")
	@GetMapping(value="/user/amount" , produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Integer> getUserAmount()
	{
		int unlockedAmount = 0 ;
		int lockedAmount = 0 ;
		
		List<User> userList = mResMngServer.getUserDataMng().getAllUsers() ;
		for(User user : userList)
		{
			if (user.isAccountNonLocked())
			{
				++unlockedAmount ;
			}
			else
			{
				++lockedAmount ;
			}
		} ;
		Map<String, Integer> sts = new HashMap<>();
		sts.put("totalAmount" , userList.size());
		sts.put("unlockedAmount" , unlockedAmount);
		sts.put("lockedAmount" , lockedAmount);
		return sts;
	}
	
	@Operation(description = "检查指定的用户名是否可用")
	@Parameter(name="username" , description = "用户名")
	@PreAuthorize(IAppAuths.sHasAuthority_CDU_UserData)
	@PostMapping(value="/user/username/_check" , produces = MediaType.TEXT_PLAIN_VALUE)
	public String checkUsernameAvailable(@RequestParam("username") String aUsername)
	{
		try
		{
			mResMngServer.getUserDataMng().loadUserByUsername(aUsername) ;
			return "false" ;
		}
		catch(UsernameNotFoundException e)
		{
			return "true" ;
		}
	}
	
	@Operation(description = "创建用户")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "用户信息")
	@Parameter(name="codeId" , description = "如果想在此次创建中把密码设置上，就得获取RSA公钥，对密码进行加密。"
			+ "加密后的内容设置在password字段上。Https协议下，可以不用加密")
	@PreAuthorize(IAppAuths.sHasAuthority_CDU_UserData)
	@PostMapping(value = "/user/one" , produces = MediaType.TEXT_PLAIN_VALUE)
	public String createUser(@RequestBody UserItem aUser
			, @RequestParam(name="codeId" , required = false) String aCodeId) throws Exception
	{
		User operUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if(XString.isEmpty(aUser.getPassword()))
			aUser.setPassword(null) ;
		else
		{
			String password = mRSAMaker.decrypt4js(aCodeId , aUser.getPassword()) ;
			aUser.setPassword(mPasswordEncoder.encode(password)) ;
		}
		ValidateUtils.validateAndThrow(mValidator, aUser)	;
		User user = mResMngServer.getUserDataMng().createUser(aUser , operUser.getId()) ;
		if(XC.isNotEmpty(aUser.getOrgUnits()))
		{
			IAuthCenterDataManager acDataMng = mResMngServer.getAuthCenterDataMng() ;
			for(OrgUnit4User ouu : aUser.getOrgUnits())
			{
				acDataMng.bindUserToOrgUnit(user.getId()
						,  ouu.getOrgUnitId() , ouu.getJob(), operUser.getId()) ;
			}
		}
		return user.getId() ;
	}
	
	@Operation(description = "更新用户信息（非自己）。不包括密码")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "用户信息")
	@PreAuthorize(IAppAuths.sHasAuthority_CDU_UserData)
	@PutMapping(value="/user/one")
	public void updateUser(@RequestBody UserItem aUser) throws Exception
	{
		User operUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal() ;
		mResMngServer.getUserDataMng().updateUser(aUser , operUser.getId()) ;
		if(XC.isNotEmpty(aUser.getOrgUnits()))
		{
			IAuthCenterDataManager acDataMng = mResMngServer.getAuthCenterDataMng() ;
			for(OrgUnit4User ouu : aUser.getOrgUnits())
			{
				acDataMng.bindUserToOrgUnit(aUser.getId()
						,  ouu.getOrgUnitId() , ouu.getJob(), operUser.getId()) ;
			}
		}
	}
	
	@Operation(description = "重置其它用户的密码")
	@Parameters({
		@Parameter(name="codeId" , description = "动态RSA秘钥的标识码。Https协议下，可以不用加密") ,
		@Parameter(name="password" , description = "密码。用动态RSA秘钥的公钥加密过后的密码") ,
		@Parameter(name="userId" , description = "目标用户id，被修改密码的用户id")
	})
	@PreAuthorize(IAppAuths.sHasAuthority_Reset_PasswordOfOtherUser)
	@PostMapping(value="/user/password/_reset" , produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public void resetUserPassword(@RequestParam(name="codeId" , required = false) String aCodeId
			, @RequestParam("password") String aPassword
			, @RequestParam("userId") String aUserId) throws Exception
	{
		User operUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal() ;
		User user = mResMngServer.getUserDataMng().getUser(aUserId) ;
		Assert.notNull(user , "无效的用户id：%s" , aUserId) ;
		String password = mRSAMaker.decrypt4js(aCodeId , aPassword);
		user.setPassword(mPasswordEncoder.encode(password)) ;
		user.setLastEditTime(new Date());
		user.setLastEditUserId(operUser.getId());
		mLogger.info("用户" + operUser.getDisplayName() + "修改了【" + user.getDisplayName() + "】的登录密码！") ;
	}

	@Operation(description = "删除指定用户")
	@Parameter(name="userId" , description = "目标用户id，被删除的用户id")
	@PreAuthorize(IAppAuths.sHasAuthority_CDU_UserData)
	@GetMapping(value="/user/one/delete")
	public void deleteUser(@RequestParam("userId") String aUserId)
	{
		User operUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal() ;
		User user = mResMngServer.getUserDataMng().getUser(aUserId) ;
		if(mResMngServer.getUserDataMng().deleteUser(aUserId , operUser.getId()))
		{
			mLogger.info("用户[{}]删除了指定用户 {}[{}] !" , operUser.getDisplayName() , user.getDisplayName() ,user.getId()) ;
		}
	}
	
	@InnerProtectedApi
	@Operation(description = "取得当前会话用户可以访问的应用简要信息")
	@GetMapping(value="/user/self/clientAppBrief/ofCanVisit" , produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ClientAppBrief> getClientAppBriefsOfSelfCanVisit()
	{
		User operUser = SecurityUtils.checkUser() ;
		String userId = operUser.getId();
		List<ClientApp> clientApps = mResMngServer.getClientAppDataMng().getClientAppsOfUserCanVisit(userId) ;
		return XC.extractAsArrayList(clientApps , ClientAppBrief::of) ;
	}
	
	@Operation(description = "取得指定用户可以访问的应用简要信息。（需要权限）")
	@Parameter(name="userId" , description = "目标用户id")
	@PreAuthorize(IAppAuths.sHasAuthority_CDU_UserData
			+ " or " + IAppAuths.sHasAuthority_View_AllUsers)
	@GetMapping(value="/user/clientAppBrief/ofCanVisit" , produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ClientAppBrief> getClientAppBriefsOfUserCanVisit(@RequestParam("userId") String aUserId)
	{
		List<ClientApp> clientApps = mResMngServer.getClientAppDataMng().getClientAppsOfUserCanVisit(aUserId) ;
		return XC.extractAsArrayList(clientApps , ClientAppBrief::of) ;
	}
	
	@Operation(description = "添加/删除用户可访问某些应用的权限")
	@Parameters({
		@Parameter(name="userId" , description = "目标用户id") ,
		@Parameter(name="addClientAppIds" , description = "新添加可访问权限的ClientApp的id，多个之间用“,”相连") ,
		@Parameter(name="delClientAppIds" , description = "删除可访问权限的ClientApp的id，多个之间用“,”相连") ,
	})
	@PreAuthorize(IAppAuths.sHasAuthority_CDU_UserData)
	@PostMapping(value="/user/clientApp/ofCanVisit/_addOrDelete")
	public void addClientAppVisitRightToUser(@RequestParam("userId") String aUserId
			, @RequestParam(name="addClientAppIds" , required = false) String[] aAddClientAppIds
			, @RequestParam(name="delClientAppIds" , required = false) String[] aDelClientAppIds)
	{
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		IClientAppDataManager clientAppDataMng = mResMngServer.getClientAppDataMng() ;
		if(XC.isNotEmpty(aAddClientAppIds))
		{
			for (String appId : aAddClientAppIds)
			{
				if (XString.isNotEmpty(appId))
					clientAppDataMng.grantClientAppToUser(appId, aUserId, user.getId()) ;
			}
		}
		if(XC.isNotEmpty(aDelClientAppIds))
		{
			for (String appId : aAddClientAppIds)
			{
				if (XString.isNotEmpty(appId))
					clientAppDataMng.ungrantClientAppToUser(appId, aUserId, user.getId()) ;
			}
		}
	}
	
	@Operation(description = "取得用户在指定应用下所拥有的资源空间")
	@Parameters({
		@Parameter(name="clientAppId" , description = "客户端应用id") ,
		@Parameter(name="userId" , description = "用户id")
	})
	@PreAuthorize(IAppAuths.sHasAuthority_CDU_UserData
			+ " or " + IAppAuths.sHasAuthority_View_AllUsers)
	@GetMapping(value="/user/clientApp/resSpace" , produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ResSpace.BResSpace> getResSpacesOfUserInClientApp(@RequestParam("userId") String aUserId
			, @RequestParam("clientAppId") String aClientAppId)
	{
		return XC.extractAsArrayList(mResMngServer.getClientAppDataMng().getResSpaceOfUserInClientApp(aUserId, aClientAppId)
				, ResSpace::asBean) ;
	}
	
	@Operation(description = "取得用户在指定资源空间下面的角色")
	@Parameters({
		@Parameter(name="userId" , description = "用户id") ,
		@Parameter(name="resSpaceId" , description = "资源空间id") ,
	})
	@PreAuthorize(IAppAuths.sHasAuthority_CDU_UserData
			+ " or " + IAppAuths.sHasAuthority_View_AllUsers
			+ " or " + IAppAuths.sHasResAuthority_Manage_Special_CanVisitUser)
	@GetMapping(value="/user/clientApp/resSapce/role" , produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Role.BRole> getRolesOfUserInResSapce(@RequestParam("userId") String aUserId
			, @ResId(AppConsts.sResIdGetter_getClientAppIdFromResSpaceId) @RequestParam("resSpaceId") String aResSpaceId)
	{
		return XC.extractAsArrayList(mResMngServer.getClientAppDataMng().getRoleOfUserInResSpace(aUserId, aResSpaceId)
				, Role::asBean) ;
	}
	
	@Operation(description =  "添加/删除某个用户在某个应用的资源空间下的角色")
	@Parameters({
		@Parameter(name="userId" , description = "用户id") ,
		@Parameter(name="resSpaceId" , description = "资源空间id") ,
		@Parameter(name="addRoleIds" , description = "需要添加的角色id，多个之间用“,”分隔") ,
		@Parameter(name="delRoleIds" , description = "需要删除的角色id，多个之间用“,”分隔") ,
	})
	@PreAuthorize(IAppAuths.sHasAuthority_CDU_UserData
			+ " or " + IAppAuths.sHasResAuthority_Manage_Special_CanVisitUser)
	@PostMapping("/user/clientApp/resSpace/role/_addOrDelete")
	public void addOrDeleteRolesOfUserInRespace(@RequestParam("userId") String aUserId
			, @ResId(AppConsts.sResIdGetter_getClientAppIdFromResSpaceId) @RequestParam("resSpaceId") String aResSpaceId
			, @RequestParam(name="addRoleIds" , required = false) String[] aAddRoleIds
			, @RequestParam(name="delRoleIds" , required = false) String[] aDelRoleIds)
	{
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		IClientAppDataManager clientAppDataMng = mResMngServer.getClientAppDataMng() ;
		if(XC.isNotEmpty(aAddRoleIds))
		{
			for(String roleId : aAddRoleIds)
				clientAppDataMng.grantRoleToUser(aResSpaceId , roleId , aUserId, user.getId()) ;
		}
		if(XC.isNotEmpty(aDelRoleIds))
		{
			for(String roleId : aDelRoleIds)
				clientAppDataMng.ungrantRoleToUser(aResSpaceId, roleId, aUserId, user.getId()) ;
		}
	}
}
