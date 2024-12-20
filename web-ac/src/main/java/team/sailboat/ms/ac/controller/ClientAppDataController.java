package team.sailboat.ms.ac.controller;

import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Validator;
import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.collection.PropertiesEx;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.gadget.RSAUtils;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.struct.Tuples;
import team.sailboat.commons.fan.text.ChineseComparator;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.ms.valid.ValidateUtils;
import team.sailboat.commons.web.ac.ResId;
import team.sailboat.ms.ac.AppConsts;
import team.sailboat.ms.ac.IAppAuths;
import team.sailboat.ms.ac.bean.Authority_Role;
import team.sailboat.ms.ac.bean.ClientAppBrief;
import team.sailboat.ms.ac.bean.Role_ResSpace;
import team.sailboat.ms.ac.bean.UserBrief_Role;
import team.sailboat.ms.ac.dbean.Api;
import team.sailboat.ms.ac.dbean.Authority;
import team.sailboat.ms.ac.dbean.ClientApp;
import team.sailboat.ms.ac.dbean.R_User_ResSpace_Role;
import team.sailboat.ms.ac.dbean.ResSpace;
import team.sailboat.ms.ac.dbean.Role;
import team.sailboat.ms.ac.dbean.User;
import team.sailboat.ms.ac.server.IClientAppDataManager;
import team.sailboat.ms.ac.server.ResourceManageServer;

/**
 * ClientApp的数据接口。给认证中心的web界面使用			<br />
 *
 * 包括：ClientApp信息、权限、角色、资源空间
 *	
 * @author yyl
 * @since 2024年10月30日
 */
@RestController
public class ClientAppDataController
{
	
	final Logger mLogger = LoggerFactory.getLogger(getClass()) ;
	
	@Autowired
	ResourceManageServer mResMngServer ;
	
	@Autowired
	Validator mValidator ;
	
	@PostConstruct
	void _init()
	{
		Function<Object, String> clientIdGetter = obj->((ClientApp.BClientApp)obj).getId() ;
		AppContext.set(AppConsts.sResIdGetter_getClientAppIdFromBClientApp , clientIdGetter) ;
		
		Function<Object, String> clientIdGetter1 = id->{
			R_User_ResSpace_Role r = mResMngServer.getClientAppDataMng().getR_User_ResSpace_Role((String)id) ;
			return r != null?ResSpace.getClientAppIdFrom(r.getResSpaceId()):null ;
		} ;
		AppContext.set(AppConsts.sResIdGetter_getClientAppIdFromIdOfR_User_ResSpace_Role , clientIdGetter1) ;
		
		Function<Object, String> clientIdGetter2 = resSpaceId->ResSpace.getClientAppIdFrom((String)resSpaceId) ;
		AppContext.set(AppConsts.sResIdGetter_getClientAppIdFromResSpaceId , clientIdGetter2) ;
		
		Function<Object, String> clientIdGetter3 = role->((Role.BRole)role).getClientAppId() ;
		AppContext.set(AppConsts.sResIdGetter_getClientAppIdFromBRole_clientAppId , clientIdGetter3) ;
		
		Function<Object, String> clientIdGetter4 = role->{
			Role role1 = mResMngServer.getClientAppDataMng().getRole(((Role.BRole)role).getId()) ;
			return role1 == null?null:role1.getClientAppId() ;
		} ;
		AppContext.set(AppConsts.sResIdGetter_getClientAppIdFromBRole_id , clientIdGetter4) ;
		
		Function<Object, String> clientIdGetter5 = roleId->{
			Role role1 = mResMngServer.getClientAppDataMng().getRole((String)roleId) ;
			return role1 == null?null:role1.getClientAppId() ;
		} ;
		AppContext.set(AppConsts.sResIdGetter_getClientAppIdFromRoleId , clientIdGetter5) ;
	}
	
	@Operation(description = "取得所有ClientApp的简要信息")
	@PreAuthorize(IAppAuths.sHasAuthority_CDU_ClientAppData
			+ " or " + IAppAuths.sHasAuthority_View_AllClientAppData)
	@GetMapping(value = "/clientApp/brief/all" , produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ClientAppBrief> getClientAppBriefs()
	{
		List<ClientApp> clientApps = mResMngServer.getClientAppDataMng().getClientApps() ;
		return XC.extractAsArrayList(clientApps , ClientAppBrief::of) ;
	}
	
	@Operation(description = "取得指定id的ClientApp的详情。AppSecret不会返回，需要另外专门获取")
	@Parameter(name="clientAppId" , description = "ClientApp的id")
	@PreAuthorize(IAppAuths.sHasAuthority_CDU_ClientAppData
			+ " or " + IAppAuths.sHasAuthority_View_AllClientAppData
			+ " or " + IAppAuths.sHasResAuthority_View_Special_ClientAppData
			+ " or " + IAppAuths.sHasResAuthority_Update_Special_ClientAppData)
	@GetMapping(value="/clientApp/one" , produces = MediaType.APPLICATION_JSON_VALUE)
	public ClientApp.BClientApp getClientApp(@ResId @RequestParam("clientAppId") String aClientAppId)
	{
		ClientApp clientApp = mResMngServer.getClientAppDataMng().getClientApp(aClientAppId) ;
		if(clientApp != null)
		{
			ClientApp.BClientApp bClientApp = clientApp.asBean() ;
			bClientApp.setAppSecret(null) ;
			return bClientApp ;
		}
		return null ;
	}
	
	@Operation(description = "查询ClientApp的AppSecret")
	@Parameters({
		@Parameter(name="clientAppId" , description = "ClientApp的id") ,
		@Parameter(name="publicKey" , description = "浏览器端生成的RSA秘钥对的公钥。将用公钥加密后发送给客户端") ,
	})
	@PreAuthorize(IAppAuths.sHasAuthority_View_AllClientAppSecret
			+ " or " + IAppAuths.sHasResAuthority_View_Special_ClientAppSecret
			+ " or " + IAppAuths.sHasAuthority_Reset_AllClientAppSecret
			+ " or " + IAppAuths.sHasAuthority_Reset_Special_ClientAppSecret)
	@PostMapping(value = "/clientApp/one/appSecret" , produces = MediaType.TEXT_PLAIN_VALUE)
	public String getAppSecret(@ResId @RequestParam("clientAppId") String aClientAppId
			, @RequestParam("publicKey") String aPublicKey) throws Exception
	{
		ClientApp app = mResMngServer.getClientAppDataMng().getClientApp(aClientAppId) ;
		String serect = app.getAppSecret();
		String publicKey = URLDecoder.decode(aPublicKey , "UTF-8");
		return RSAUtils.encrypt(RSAUtils.getPublicKey(publicKey, Base64.getDecoder()), serect);
	}
	
	@Operation(description = "以Property加密秘文的形式返回AppSecret")
	@Parameters({
		@Parameter(name="clientAppId" , description = "ClientApp的id") ,
		@Parameter(name="publicKey" , description = "浏览器端生成的RSA秘钥对的公钥。将用公钥加密后发送给客户端") ,
	})
	@PreAuthorize(IAppAuths.sHasAuthority_View_AllClientAppSecret
			+ " or " + IAppAuths.sHasResAuthority_View_Special_ClientAppSecret)
	@PostMapping(value="/clientApp/one/appSecret/_asPropertySecret" , produces = MediaType.TEXT_PLAIN_VALUE)
	public String getAppSecretAsPropertySecret(@ResId @RequestParam("clientAppId") String aClientAppId
			, @RequestParam("publicKey") String aPublicKey) throws Exception
	{
		ClientApp app = mResMngServer.getClientAppDataMng().getClientApp(aClientAppId) ;
		String serect = PropertiesEx.asSecret(app.getAppSecret()) ;
		String publicKey = URLDecoder.decode(aPublicKey , "UTF-8");
		return RSAUtils.encrypt(RSAUtils.getPublicKey(publicKey, Base64.getDecoder()), serect);
	}
	
	@Operation(description = "创建一个ClientApp")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "ClientApp的信息")
	@PreAuthorize(IAppAuths.sHasAuthority_CDU_ClientAppData)
	@PostMapping(value="/clientApp/one" , produces = MediaType.APPLICATION_JSON_VALUE)
	public ClientApp.BClientApp createClientApp(@RequestBody ClientApp.BClientApp aClientApp)
	{
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		IClientAppDataManager clientAppDataMng = mResMngServer.getClientAppDataMng() ;
		ClientApp.BClientApp bclientApp = clientAppDataMng.createClientApp(aClientApp, true , user.getId())
				.asBean() ;
		bclientApp.setAppSecret(null) ;
		
		// 创建一个ClientApp类型的资源空间
		ResSpace.BResSpace brs = new ResSpace.BResSpace() ;
		brs.setClientAppId(mResMngServer.getClientAppId_SailAC()) ;
		brs.setResId(bclientApp.getId()) ;
		brs.setResName(bclientApp.getName()) ;
		brs.setType(AppConsts.sResSpaceType_ClientApp) ;
		clientAppDataMng.createOrUpdateResSpace(brs , user.getId()) ;
		
		return bclientApp ;
	}
	
	@Operation(description = "更新ClientApp的信息。不包括AppKey和AppSecret")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "ClientApp的信息")
	@PreAuthorize(IAppAuths.sHasAuthority_CDU_ClientAppData
			+ " or " + IAppAuths.sHasResAuthority_Update_Special_ClientAppData)
	@PutMapping(value="/clientApp/one" , produces = MediaType.APPLICATION_JSON_VALUE)
	public ClientApp.BClientApp updateClientApp(@ResId(AppConsts.sResIdGetter_getClientAppIdFromBClientApp)
								@RequestBody ClientApp.BClientApp aClientApp)
	{
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		IClientAppDataManager clientAppDataMng = mResMngServer.getClientAppDataMng() ;
		ClientApp.BClientApp bclientApp = clientAppDataMng.updateClientApp(aClientApp,user.getId())
				.asBean() ;
		bclientApp.setAppSecret(null) ;
		
		// 如果ClientApp的名字变了，认证中心的ClientApp类型的资源空间的名字跟着变
		String resSpaceId = ResSpace.spliceResSpaceId(mResMngServer.getClientAppId_SailAC() , bclientApp.getId()) ;
		ResSpace resSpace= clientAppDataMng.getResSpace(resSpaceId) ;
		if(resSpace != null)
		{
			if(JCommon.unequals(bclientApp.getName() , resSpace.getResName()))
			{
				resSpace.setResName(bclientApp.getName()) ;
 			}
		}	
		return bclientApp ;
	}
	
	@Operation(description = "删除指定的ClientApp")
	@Parameter(name="clientAppId" , description = "ClientApp的id")
	@PreAuthorize(IAppAuths.sHasAuthority_CDU_ClientAppData)
	@DeleteMapping(value="/clientApp/one")
	public void deleteClientApp(@RequestParam("clientAppId") String aClientAppId)
	{
		mResMngServer.getClientAppDataMng().deleteClientApp(aClientAppId) ;
	}
	
	@Operation(description = "取得可以访问指定资源空间的用户简要信息和角色")
	@Parameter(name = "resSpaceId" , description = "资源空间id")
	@PreAuthorize(IAppAuths.sHasAuthority_CDU_ClientAppData
			+ " or " + IAppAuths.sHasAuthority_View_AllClientAppData
			+ " or " + IAppAuths.sHasResAuthority_View_Special_ClientAppData
			+ " or " + IAppAuths.sHasResAuthority_Update_Special_ClientAppData)
	@GetMapping(value="/clientApp/resSpace/user/all/ofCanVisit" , produces = MediaType.APPLICATION_JSON_VALUE)
	public List<UserBrief_Role> getUsersOfCanVisitResSpace(@ResId(AppConsts.sResIdGetter_getClientAppIdFromResSpaceId)
					@RequestParam("resSpaceId") String aResSpaceId)
	{
		IClientAppDataManager clientAppDataMng = mResMngServer.getClientAppDataMng() ;
		List<Tuples.T2<User, List<Role>>> list = clientAppDataMng.getUsersOfCanVisitResSpace(aResSpaceId);
		List<UserBrief_Role> userBriefList = XC.arrayList() ;
		ResSpace resSpace = clientAppDataMng.getResSpace(aResSpaceId) ;
		String resName = resSpace.getResName()  ;
		String resNamePrefix = XString.isEmpty(resName)?"":(resName + ".") ;
		for (Tuples.T2<User, List<Role>> t : list)
		{
			User user = t.getEle_1() ;
			UserBrief_Role u = new UserBrief_Role() ;
			u.setId(user.getId()) ;
			u.setRealName(user.getRealName()) ;
			u.setDepartment(user.getDepartment()) ;
			Role_ResSpace[] roleReses = XC.extract(t.getEle_2(), r->
				Role_ResSpace.builder().resSpaceId(resSpace.getId())
						.resSpaceType(resSpace.getType())
						.roleFullName(resNamePrefix + r.getName())
						.roleId(r.getId())
						.build()
			 , Role_ResSpace.class) ;
			Arrays.sort(roleReses , (r1 , r2)->ChineseComparator.getInstance()
					.compare(r1.getRoleFullName(), r2.getRoleFullName())) ;
			u.setRoleResSpaces(roleReses) ;
			userBriefList.add(u) ;
		}
		return userBriefList ;
	}
	
	@Operation(description = "删除指定用户访问指定ClientApp的权利。删除在这个应用下的所有授权")
	@Parameters({
		@Parameter(name="clientAppId" , description = "ClientApp的id") ,
		@Parameter(name="userIds" , description = "用户的id，多个之间用“,”分隔")
	})
	@PreAuthorize(IAppAuths.sHasAuthority_CDU_ClientAppData
			+ " or " + IAppAuths.sHasResAuthority_Manage_Special_CanVisitUser)
	@DeleteMapping(value="/clientApp/user/many/ofCanVisit")
	public void deleteRightOfVisitClientApp(@ResId @RequestParam("clientAppId") String aClientAppId
			, @RequestParam("userIds") String[] aUserIds)
	{
		User operUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal() ;
		IClientAppDataManager clientAppDataMng = mResMngServer.getClientAppDataMng() ;
		for(String userId : aUserIds)
		{
			clientAppDataMng.ungrantClientAppToUser(aClientAppId, userId , operUser.getId()) ;
		}
	}
	
	@Operation(description = "取得可以访问指定应用的用户简要信息和角色")
	@Parameter(name = "clientAppId" , description = "ClientApp的id")
	@PreAuthorize(IAppAuths.sHasAuthority_CDU_ClientAppData
			+ " or " + IAppAuths.sHasAuthority_View_AllClientAppData
			+ " or " + IAppAuths.sHasResAuthority_View_Special_ClientAppData
			+ " or " + IAppAuths.sHasResAuthority_Update_Special_ClientAppData)
	@GetMapping(value="/clientApp/user/all/ofCanVisit" , produces = MediaType.APPLICATION_JSON_VALUE)
	public List<UserBrief_Role> getUsersOfCanVisitClientApp(@ResId
					@RequestParam("clientAppId") String aClientAppId)
	{
		IClientAppDataManager clientAppDataMng = mResMngServer.getClientAppDataMng() ;
		Collection<User> list = clientAppDataMng.getUsersOfCanVisitClientApp(aClientAppId) ;
		List<UserBrief_Role> userBriefList = XC.arrayList() ;
		for (User user : list)
		{
			UserBrief_Role u = new UserBrief_Role() ;
			u.setId(user.getId()) ;
			u.setRealName(user.getRealName()) ;
			u.setDepartment(user.getDepartment()) ;
			// 查询这个用户在这个应用下的各个资源空间的角色
			R_User_ResSpace_Role[] rs = clientAppDataMng.getR_User_ResSpace_RoleOfUserInApp(u.getId(), aClientAppId) ;
			List<Role_ResSpace> roleResList = XC.arrayList() ;
			for(R_User_ResSpace_Role r : rs)
			{
				ResSpace resSpace = clientAppDataMng.getResSpace(r.getResSpaceId()) ;
				String resName = resSpace.getResName() ;
				String roleName = clientAppDataMng.getRole(r.getRoleId()).getName()  ;
				roleResList.add(Role_ResSpace.builder().resSpaceId(r.getResSpaceId())
						.resSpaceType(resSpace.getType())
						.roleFullName((XString.isEmpty(resName)?"": (resName + ".")) + roleName)
						.roleId(r.getRoleId())
						.build()) ;
								
			}
			roleResList.sort((r1 , r2)->ChineseComparator.getInstance()
					.compare(r1.getRoleFullName(), r2.getRoleFullName())) ;
			u.setRoleResSpaces(roleResList.toArray(new Role_ResSpace[0]))  ;
			userBriefList.add(u) ;
		}
		return userBriefList ;
	}
	
	@Operation(description = "给指定用户授予角色。返回授权关系id")
	@Parameters({
		@Parameter(name = "resSpaceId" , description = "资源空间id") ,
		@Parameter(name = "roleId" , description = "角色id。这角色必须适用于指定资源空间") ,
		@Parameter(name="userId" , description = "用户id")
	})
	@PreAuthorize(IAppAuths.sHasAuthority_CDU_ClientAppData
			+ " or " + IAppAuths.sHasResAuthority_Manage_Special_CanVisitUser)
	@PostMapping(value="/clientApp/resSpace/role/user" , produces = MediaType.APPLICATION_JSON_VALUE)
	public String grantRoleToUser(@RequestParam("resSpaceId") String aResSpaceId
			, @RequestParam("roleId") String aRoleId
			, @RequestParam("userId") String aUserId)
	{
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal() ;
		return mResMngServer.getClientAppDataMng().grantRoleToUser(aResSpaceId, aRoleId , aUserId, user.getId()) ;
	}
	
	@Operation(description = "取消给指定用户授予予角色。")
	@Parameters({
		@Parameter(name = "resSpaceId" , description = "资源空间id") ,
		@Parameter(name = "roleId" , description = "角色id。这角色必须适用于指定资源空间") ,
		@Parameter(name="userId" , description = "用户id")
	})
	@PreAuthorize(IAppAuths.sHasAuthority_CDU_ClientAppData
			+ " or " + IAppAuths.sHasResAuthority_Manage_Special_CanVisitUser)
	@DeleteMapping(value="/clientApp/resSpace/role/user")
	public void ungrantRoleToUser(@ResId @RequestParam("resSpaceId") String aResSpaceId
			, @RequestParam("roleId") String aRoleId
			, @RequestParam("userId") String aUserId)
	{
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal() ;
		mResMngServer.getClientAppDataMng().ungrantRoleToUser(aResSpaceId, aRoleId , aUserId, user.getId()) ;
	}
	
	@Operation(description = "取消给指定用户授予予角色。")
	@Parameter(name="id" , description = "授权关系id")
	@PreAuthorize(IAppAuths.sHasAuthority_CDU_ClientAppData
			+ " or " + IAppAuths.sHasResAuthority_Manage_Special_CanVisitUser)
	@DeleteMapping(value="/clientApp/resSpace/role/user/byId")
	public void ungrantRoleToUserById(@ResId(AppConsts.sResIdGetter_getClientAppIdFromIdOfR_User_ResSpace_Role)
					@RequestParam("id") String aId)
	{
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal() ;
		mResMngServer.getClientAppDataMng().ungrantRoleToUser(aId , user.getId()) ;
	}
	
	@Operation(description = "查看指定应用下适用于指定资源空间类型的权限和角色信息")
	@Parameters({
		@Parameter(name="clientAppId" , description = "ClientApp的id") ,
		@Parameter(name="resSpaceType" , description = "资源空间类型") ,
	})
	@PreAuthorize(IAppAuths.sHasAuthority_CDU_ClientAppData
			+ " or " + IAppAuths.sHasAuthority_View_AllClientAppData
			+ " or " + IAppAuths.sHasResAuthority_View_Special_ClientAppData
			+ " or " + IAppAuths.sHasResAuthority_Update_Special_ClientAppData
			+ " or " + IAppAuths.sHasResAuthority_Manage_Special_CanVisitUser)
	@GetMapping(value="/clientApp/resSpace/authority_Role/all" , produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Authority_Role> getAuthoritesInResSpace(@ResId @RequestParam("clientAppId") String aClientAppId
			, @RequestParam("resSpaceType") String aResSpaceType)
	{
		return mResMngServer.getClientAppDataMng().getAuthoritesForResSpaceType(aClientAppId , aResSpaceType) ;
	}
	
	@Operation(description = "取得指定ClientApp能够调用的API")
	@Parameter(name="clientAppId" , description = "ClientApp的id")
	@PreAuthorize(IAppAuths.sHasAuthority_CDU_ClientAppData
			+ " or " + IAppAuths.sHasAuthority_View_AllClientAppData
			+ " or " + IAppAuths.sHasResAuthority_View_Special_ClientAppData
			+ " or " + IAppAuths.sHasResAuthority_Update_Special_ClientAppData)
	@GetMapping(value="/clientApp/api/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Api.BApi> getApisOfClientAppCanInvoke(@ResId @RequestParam("clientAppId") String aClientAppId)
	{
		return mResMngServer.getClientAppDataMng().getApisOfClientAppCanInvoke(aClientAppId)
				.stream().map(Api::asBean).collect(Collectors.toList()) ;
	}
	
	@Operation(description = "将指定的API授权给指定的ClientApp调用")
	@Parameters({
		@Parameter(name="clientAppId" , description = "ClientApp的id") ,
		@Parameter(name="apiId" , description = "API的id")
	})
	@PreAuthorize(IAppAuths.sHasAuthority_CDU_ClientAppData)
	@PostMapping(value="/clientApp/api/one" , produces = MediaType.TEXT_PLAIN_VALUE)
	public String grantApiToClientApp(@RequestParam("clientAppId") String aClientAppId
			, @RequestParam("apiId") String aApiId)
	{
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal() ;
		return mResMngServer.getClientAppDataMng().grantApiToClientApp(aClientAppId, aApiId, user.getId()) ;
	}
	
	@Operation(description = "取消将指定的API授权给指定的ClientApp调用")
	@Parameters({
		@Parameter(name="clientAppId" , description = "ClientApp的id") ,
		@Parameter(name="apiId" , description = "API的id")
	})
	@PreAuthorize(IAppAuths.sHasAuthority_CDU_ClientAppData)
	@DeleteMapping(value="/clientApp/api/one")
	public void ungrantApiToClientApp(@RequestParam("clientAppId") String aClientAppId
			, @RequestParam("apiId") String aApiId)
	{
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal() ;
		mResMngServer.getClientAppDataMng().ungrantApiToClientApp(aClientAppId, aApiId, user.getId()) ;
	}
	
	@Operation(description = "创建角色")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "角色信息")
	@PreAuthorize(IAppAuths.sHasAuthority_CDU_ClientAppData
			+ " or " + IAppAuths.sHasResAuthority_Update_Special_ClientAppData)
	@PostMapping(value="/clientApp/role/one" , produces = MediaType.APPLICATION_JSON_VALUE)
	public Role.BRole createRole(@ResId(AppConsts.sResIdGetter_getClientAppIdFromBRole_clientAppId)
						@RequestBody Role.BRole aRole)
	{
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		ValidateUtils.validateAndThrow(mValidator, aRole) ;
		return mResMngServer.getClientAppDataMng().createRole(aRole, user.getId())
				.asBean() ;
	}
	
	@Operation(description = "删除指定的角色")
	@Parameter(name = "roleId" , description = "角色id")
	@PreAuthorize(IAppAuths.sHasAuthority_CDU_ClientAppData
			+ " or " + IAppAuths.sHasResAuthority_Update_Special_ClientAppData)
	@DeleteMapping(value="/clientApp/role/one")
	public void deleteRole(@ResId(AppConsts.sResIdGetter_getClientAppIdFromRoleId)
						@RequestParam("roleId") String aRoleId)
	{
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal() ;
		mResMngServer.getClientAppDataMng().deleteRole(aRoleId, user.getId()) ;
	}
	
	@Operation(description = "更新角色信息。包括name、customDescription、资源空间类型。"
			+ "如果要修改资源空间类型，那么这个角色不能被授予任何人,即没有相关的R_User_ResSpace_Role数据")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "角色信息")
	@PreAuthorize(IAppAuths.sHasAuthority_CDU_ClientAppData
			+ " or " + IAppAuths.sHasResAuthority_Update_Special_ClientAppData)
	@PutMapping(value="/clientApp/role/one" , produces = MediaType.APPLICATION_JSON_VALUE)
	public Role.BRole updateRole(@ResId(AppConsts.sResIdGetter_getClientAppIdFromBRole_id)
						@RequestBody Role.BRole aRole)
	{
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal() ;
		Role role = mResMngServer.getClientAppDataMng().getRole(aRole.getId()) ;
		Assert.notNull(role , "无效的角色id：%s" , aRole.getId()) ;
		aRole.setDescription(role.getDescription()) ;
		return mResMngServer.getClientAppDataMng().updateRole(aRole, user.getId())
				.asBean() ;
	}
	
	
	@Operation(description = "取得与指定角色相关的权限")
	@Parameter(name="roleId" , description = "角色id")
	@PreAuthorize(IAppAuths.sHasAuthority_CDU_ClientAppData
			+ " or " + IAppAuths.sHasAuthority_View_AllClientAppData
			+ " or " + IAppAuths.sHasResAuthority_Manage_Special_CanVisitUser
			+ " or " + IAppAuths.sHasResAuthority_View_Special_ClientAppData
			+ " or " + IAppAuths.sHasResAuthority_Update_Special_ClientAppData)
	@GetMapping(value="/clientApp/role/authority/all" , produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Authority.BAuthority> getAuthoritiesOfRole(@ResId(AppConsts.sResIdGetter_getClientAppIdFromRoleId)
			@RequestParam("roleId") String aRoleId)
	{
		return XC.extractAsArrayList(mResMngServer.getClientAppDataMng().getAuthoritiesOfRole(aRoleId)
				, Authority::asBean) ;
	}
	
	@Operation(description = "将指定权限和指定的角色绑定")
	@Parameters({
		@Parameter(name="authorityId" , description = "权限id") ,
		@Parameter(name="roleId" , description = "角色id")
	})
	@PreAuthorize(IAppAuths.sHasAuthority_CDU_ClientAppData
			+ " or " + IAppAuths.sHasResAuthority_Update_Special_ClientAppData)
	@PostMapping(value="/clientApp/role/auhtority/one")
	public void bindAuthorityToRole(@RequestParam("authorityId") String aAuthorityId
			, @ResId(AppConsts.sResIdGetter_getClientAppIdFromRoleId) @RequestParam("roleId") String aRoleId)
	{
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		mResMngServer.getClientAppDataMng().bindAuthorityToRole(aAuthorityId, aRoleId, user.getId()) ;
	}
	
	@Operation(description = "取消将指定权限和指定的角色绑定")
	@Parameters({
		@Parameter(name="authorityId" , description = "权限id") ,
		@Parameter(name="roleId" , description = "角色id")
	})
	@PreAuthorize(IAppAuths.sHasAuthority_CDU_ClientAppData
			+ " or " + IAppAuths.sHasResAuthority_Update_Special_ClientAppData)
	@DeleteMapping(value="/clientApp/role/auhtority/one")
	public void unbindAuthorityToRole(@RequestParam("authorityId") String aAuthorityId
			, @ResId(AppConsts.sResIdGetter_getClientAppIdFromRoleId) @RequestParam("roleId") String aRoleId)
	{
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		mResMngServer.getClientAppDataMng().unbindAuthorityToRole(aAuthorityId, aRoleId, user.getId()) ;
	}
	
	@Operation(description = "更新权限信息。这是给界面使用的，不能设置description，这是个程序自动化设置用的")
	@PostMapping(value="/api/auth/update" , produces = MediaType.APPLICATION_JSON_VALUE)
	public Authority.BAuthority updateAuthority(@RequestBody Authority.BAuthority aAuth) throws Exception
	{
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal() ;
		Authority auth = mResMngServer.getClientAppDataMng().getAuthority(aAuth.getId()) ;
		Assert.notNull(auth , "无效的权限id：%s" , aAuth.getId()) ;
		aAuth.setDescription(auth.getDescription()) ;
		return mResMngServer.getClientAppDataMng().updateAuthority(aAuth , user.getId())
				.asBean() ;
	}
	
	
	@Operation(description = "取得指定ClientApp下的资源空间")
	@Parameter(name = "clientAppId" , description = "ClientApp的id")
	@PreAuthorize(IAppAuths.sHasAuthority_CDU_ClientAppData
			+ " or " + IAppAuths.sHasAuthority_View_AllClientAppData
			+ " or " + IAppAuths.sHasResAuthority_View_Special_ClientAppData
			+ " or " + IAppAuths.sHasResAuthority_Update_Special_ClientAppData)
	@GetMapping(value="/clientApp/resSpace/all/ofClientApp" , produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ResSpace.BResSpace> getResSpacesOfClientApp(@ResId @RequestParam("clientAppId") String aClientAppId)
	{
		return XC.extractAsArrayList(mResMngServer.getClientAppDataMng().getResSpaceOfClientApp(aClientAppId)
				, ResSpace::asBean) ;
	}
	
	@Operation(description = "取得指定ClientApp的角色信息")
	@Parameter(name = "clientAppId" , description = "ClientApp的id")
	@PreAuthorize(IAppAuths.sHasAuthority_CDU_ClientAppData
			+ " or " + IAppAuths.sHasAuthority_View_AllClientAppData
			+ " or " + IAppAuths.sHasResAuthority_View_Special_ClientAppData
			+ " or " + IAppAuths.sHasResAuthority_Update_Special_ClientAppData)
	@GetMapping(value="/clientApp/role/all" , produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Role.BRole> getRolesOfClientApp(@ResId @RequestParam("clientAppId") String aClientAppId)
	{
		return XC.extractAsArrayList(mResMngServer.getClientAppDataMng().getRolesOfApp(aClientAppId)
				, Role::asBean);
	}
	
	@Operation(description = "取得指定ClientApp声明的权限信息。如果没有指定资源空间类型，则返回所有。"
			+ "如果指定了，除了指定的资源空间类型之外，总是会返回缺省全局资源空间的")
	@Parameters({
		@Parameter(name = "clientAppId" , description = "ClientApp的id") ,
		@Parameter(name = "resSpaceType" , description = "返回适用那种资源空间类型的全景") ,
		@Parameter(name = "returnDefaultGlobal" , description = "是否返回适用于缺省全局空间的权限")
	})
	@PreAuthorize(IAppAuths.sHasAuthority_CDU_ClientAppData
			+ " or " + IAppAuths.sHasAuthority_View_AllClientAppData
			+ " or " + IAppAuths.sHasResAuthority_View_Special_ClientAppData
			+ " or " + IAppAuths.sHasResAuthority_Update_Special_ClientAppData)
	@GetMapping(value="/clientApp/authority/all" , produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Authority.BAuthority> getAuthoritiesOfClientApp(@ResId @RequestParam("clientAppId") String aClientAppId
			, @RequestParam(name="resSpaceType" , required = false) String aResSpaceType
			, @RequestParam(name="returnDefaultGlobal" , required = false, defaultValue = "true") boolean aReturnDefaultGlobal)
	{
		return XC.extractAsArrayList(mResMngServer.getClientAppDataMng()
					.getAuthoritiesOfClientApp(aClientAppId , aResSpaceType , aReturnDefaultGlobal)
				, Authority::asBean);
	}
}
