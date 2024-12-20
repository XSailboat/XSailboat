package team.sailboat.ms.ac.controller;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
import jakarta.validation.Validator;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.text.ChineseComparator;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.ms.ac.InnerProtectedApi;
import team.sailboat.commons.ms.bean.TreeBean;
import team.sailboat.commons.ms.bean.UserBrief;
import team.sailboat.commons.ms.valid.ValidateUtils;
import team.sailboat.ms.ac.AppConsts;
import team.sailboat.ms.ac.IAppAuths;
import team.sailboat.ms.ac.bean.ClientAppBrief;
import team.sailboat.ms.ac.bean.OrgUnitExt;
import team.sailboat.ms.ac.bean.TreeNode_OrgUnit;
import team.sailboat.ms.ac.bean.User_OrgUnit;
import team.sailboat.ms.ac.dbean.Api;
import team.sailboat.ms.ac.dbean.Authority;
import team.sailboat.ms.ac.dbean.ClientApp;
import team.sailboat.ms.ac.dbean.OrgUnit;
import team.sailboat.ms.ac.dbean.ResSpace;
import team.sailboat.ms.ac.dbean.User;
import team.sailboat.ms.ac.server.IAuthCenterDataManager;
import team.sailboat.ms.ac.server.IClientAppDataManager;
import team.sailboat.ms.ac.server.ResourceManageServer;
import team.sailboat.ms.ac.utils.SecurityUtils;

/**
 * 认证中心的数据接口。给认证中心的web界面使用			<br />
 *
 * 包括：API、组织单元
 *	
 * @author yyl
 * @since 2024年10月30日
 */
@RestController
public class AuthCenterDataController
{
	
	final Logger mLogger = LoggerFactory.getLogger(getClass()) ;
	
	static final GrantedAuthority sGA_CDU_ClientAppData = Authority.toSimple_defaultGlobal(IAppAuths.sAC_CDU_ClientAppData) ;
	
	static final GrantedAuthority sGA_View_AllClientAppData = Authority.toSimple_defaultGlobal(IAppAuths.sAC_View_AllClientAppData) ;
	
	static final GrantedAuthority sGA_View_AllUser = Authority.toSimple_defaultGlobal(IAppAuths.sAC_View_AllUsers) ;
	
	static final GrantedAuthority sGA_CDU_UserData = Authority.toSimple_defaultGlobal(IAppAuths.sAC_CDU_UserData) ;
	
	static final GrantedAuthority sGA_View_OrgUnitAndUsers = Authority.toSimple_defaultGlobal(IAppAuths.sAC_View_OrgUnitAndUsers) ;
	
	static final GrantedAuthority sGA_Manage_Special_CanVisitUser = Authority.toSimple_defaultGlobal(IAppAuths.sACP_Manage_Special_CanVisitUser) ;
	
	@Autowired
	ResourceManageServer mResMngServer ;
	
	@Autowired
	Validator mValidator ;
	
	@Operation(description = "取得认证中心给ClientApp调用的接口声明")
	@PreAuthorize(IAppAuths.sHasAuthority_View_Apis)
	@GetMapping(value="/api/all" , produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Api.BApi> getAllApis()
	{
		return XC.extractAsArrayList(mResMngServer.getAuthCenterDataMng().getAllApis() , Api::asBean) ;
	}
	
	@Operation(description = "取得指定组织单元的下一层组织单元。如果不指定上一层组织单元，则表示获取最顶层的组织单元")
	@Parameter(name="parentId" , description = "上一层组织单元id")
	@PreAuthorize(IAppAuths.sHasAuthority_CDU_OrgUnit
			+ " or " + IAppAuths.sHasAuthority_View_OrgUnitAndUsers)
	@GetMapping(value = "/orgUnit/child/all" , produces = MediaType.APPLICATION_JSON_VALUE)
	public List<OrgUnitExt> getChildOrgUnits(@RequestParam(name="parentId" , required = false) String aParentId)
	{
		IAuthCenterDataManager acDataMng = mResMngServer.getAuthCenterDataMng() ;
		return XC.sort(XC.extractAsArrayList(acDataMng.getChildOrgUnit(aParentId)
				, ou->{
					OrgUnitExt bou = new OrgUnitExt() ;
					ou.initBean(bou) ;
					bou.setHasChildren(acDataMng.getChildOrgUnitAmount(ou.getId()) > 0) ;
					return bou ;
				}) , (ou1 , ou2)->ChineseComparator.comparePingYin(ou1.getName(), ou2.getName())) ;
	}
	
	@InnerProtectedApi
	@Operation(description = "通过人姓名搜索用户。搜索范围受当前用户的权限限制。"
			+ "如果用户拥有查看所有用户的权限，那么搜索范围将不局限于指定的ClientApp")
	@Parameters({
		@Parameter(name="searchText" , description = "搜索文本。包含这个文本") ,
		@Parameter(name="clientAppId" , description = "应用id") ,
	})
	@GetMapping(value="/user/_search" , produces = MediaType.APPLICATION_JSON_VALUE)
	public List<UserBrief> searchUsersByRealName(@RequestParam("searchText") String aSearchText
			, @RequestParam(name="clientAppId" , required = false) String aClientAppId)
	{
		User operUser = SecurityUtils.checkUser() ;
		Collection<? extends GrantedAuthority> auths = operUser.getAuthorities() ;
		boolean needFilter = XString.isNotEmpty(aSearchText) ;
		List<UserBrief> userBriefList = XC.arrayList() ;
		IClientAppDataManager clientAppDataMng = mResMngServer.getClientAppDataMng() ;
		if(XC.containsAny(auths , sGA_View_AllUser
				, sGA_CDU_UserData
				, sGA_View_OrgUnitAndUsers))
		{
			// 返回所有用户
			mResMngServer.getUserDataMng().forEachUser(user->{
				if(needFilter && !user.getRealName().contains(aSearchText))
					return true ;
				userBriefList.add(user.toBrief()) ;
				return userBriefList.size() <= 50 ;
			}) ;
			
		}
		else if(XString.isNotEmpty(aClientAppId)
				&& auths.contains(new SimpleGrantedAuthority(IAppAuths.sACP_Manage_Special_CanVisitUser + aClientAppId)))
		{
			// 只返回这个应用的可访问用户
			Collection<User> list = clientAppDataMng.getUsersOfCanVisitClientApp(aClientAppId) ;
			for (User user : list)
			{
				if(needFilter && !user.getRealName().contains(aSearchText))
					continue ;
				userBriefList.add(user.toBrief()) ;
				if(userBriefList.size() >= 50)
					break ;
			}
		}
		return userBriefList ;
	}
	
	@Operation(description = "通过名字搜索orgUnit")
	@Parameter(name="searchText" , description = "搜索文本。包含这个文本")
	@PreAuthorize(IAppAuths.sHasAuthority_CDU_OrgUnit
			+ " or " + IAppAuths.sHasAuthority_View_OrgUnitAndUsers)
	@GetMapping(value="/orgUnit/tree/_search" , produces = MediaType.APPLICATION_JSON_VALUE)
	public TreeBean<TreeNode_OrgUnit> searchOrgUnitByName(@RequestParam("searchText") String aSearchText)
	{
		IAuthCenterDataManager acDataMng = mResMngServer.getAuthCenterDataMng() ;
		TreeBean<TreeNode_OrgUnit> tree = new TreeBean<TreeNode_OrgUnit>(id->{
			OrgUnit ou = acDataMng.getOrgUnit(id) ;
			if(ou == null)
				return null ;
			TreeNode_OrgUnit node = new TreeNode_OrgUnit() ;
			ou.initBean(node) ;
			return node ;
		}) ;
		acDataMng.forEachOrgUnit(orgUnit->{
			if(orgUnit.getName().contains(aSearchText))
			{
				TreeNode_OrgUnit node = new TreeNode_OrgUnit() ;
				orgUnit.initBean(node) ;
				tree.addNode(node) ;
			}
		}) ;
		return tree ;
	}
	
	@Operation(description = "创建一个组织单元")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "组织单元信息")
	@PreAuthorize(IAppAuths.sHasAuthority_CDU_OrgUnit)
	@PostMapping(value = "/orgUnit/one" , produces = MediaType.APPLICATION_JSON_VALUE)
	public OrgUnit.BOrgUnit createOrgUnit(@RequestBody OrgUnit.BOrgUnit aOrgUnit)
	{
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		ValidateUtils.validateAndThrow(mValidator , aOrgUnit) ;
		return mResMngServer.getAuthCenterDataMng().createOrgUnit(aOrgUnit, user.getId()).asBean() ;
	}
	
	@Operation(description = "更新一个组织单元")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "组织单元信息")
	@PreAuthorize(IAppAuths.sHasAuthority_CDU_OrgUnit)
	@PutMapping(value="/orgUnit/one" , produces = MediaType.APPLICATION_JSON_VALUE)
	public OrgUnit.BOrgUnit updateOrgUnit(@RequestBody OrgUnit.BOrgUnit aOrgUnit)
	{
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal() ;
		ValidateUtils.validateAndThrow(mValidator , aOrgUnit) ;
		return mResMngServer.getAuthCenterDataMng().updateOrgUnit(aOrgUnit, user.getId())
				.asBean() ;
	}
	
	@Operation(description = "删除指定的组织单元。如果指定的组织单元下面有子节点或有人员都不能删除")
	@Parameter(name="orgUnitId" , description = "组织单元id")
	@DeleteMapping(value="/orgUnit/one")
	public void deleteOrgUnit(@RequestParam("orgUnitId") String aOrgUnitId)
	{
		User user = SecurityUtils.checkUser() ;
		mResMngServer.getAuthCenterDataMng().deleteOrgUnit(aOrgUnitId, user.getId()) ;
	}
	
	@Operation(description = "将用户挂到指定的组织单元上，并且设定或更新用户在这个组织单元中的职位")
	@Parameters({
		@Parameter(name="orgUnitId" , description = "组织单元id") ,
		@Parameter(name="userId" , description = "用户id") ,
		@Parameter(name="job" , description = "职位") ,
	})
	@PostMapping(value="/orgUnit/user/one")
	public void hookUserToOrgUnit(@RequestParam("orgUnitId") String aOrgUnitId
			, @RequestParam("userId") String aUserId
			, @RequestParam(name="job" , required = false) String aJob)
	{
		User user = SecurityUtils.checkUser() ;
		mResMngServer.getAuthCenterDataMng().hookUserToOrgUnit(aOrgUnitId, aUserId, aJob, user.getId()) ;
	}
	
	@Operation(description = "将用户挂到指定的组织单元上，并且设定或更新用户在这个组织单元中的职位")
	@Parameters({
		@Parameter(name="orgUnitId" , description = "组织单元id") ,
		@Parameter(name="userId" , description = "用户id") ,
		@Parameter(name="job" , description = "职位") ,
	})
	@PostMapping(value="/orgUnit/user/many")
	public void hookUsersToOrgUnit(@RequestParam("orgUnitId") String aOrgUnitId
			, @RequestParam("userIds") String[] aUserIds)
	{
		User operUser = SecurityUtils.checkUser() ;
		IAuthCenterDataManager acDataMng = mResMngServer.getAuthCenterDataMng() ;
		for(String userId : aUserIds)
		{
			acDataMng.hookUserToOrgUnit(aOrgUnitId, userId , operUser.getId()) ;
		}
	}
	
	
	@Operation(description = "解除用户挂接到指定的组织单元")
	@Parameters({
		@Parameter(name="orgUnitId" , description = "组织单元id") ,
		@Parameter(name="userId" , description = "用户id") ,
	})
	@DeleteMapping(value="/orgUnit/user/one")
	public void unhookOrgUnitToUser(@RequestParam("orgUnitId") String aOrgUnitId
			, @RequestParam("userId") String aUserId)
	{
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		mResMngServer.getAuthCenterDataMng().unhookUserToOrgUnit(aOrgUnitId, aUserId, user.getId()) ;
	}
	
	
	@Operation(description = "取得指定组织单元下面的用户")
	@Parameter(name="orgUnitId" , description = "组织单元id")
	@GetMapping(value="/orgUnit/child/user/all" , produces = MediaType.APPLICATION_JSON_VALUE)
	List<User_OrgUnit> getChildUsers(@RequestParam("orgUnitId") String aOrgUnitId)
	{
		return mResMngServer.getAuthCenterDataMng().getChildUsers(aOrgUnitId) ;
	}
	
	@InnerProtectedApi
	@Operation(description = "取得当前会话用户可以在认证中心查看或管理的应用的简要信息")
	@GetMapping(value="/clientApp/brief/ofCanView" , produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ClientAppBrief> getClientAppBriefsOfSelfCanView()
	{
		User operUser = SecurityUtils.checkUser() ;
		Assert.notNull(operUser , "无法取得当前的登录用户！") ;
		String userId = operUser.getId();
		IClientAppDataManager clientAppDataMng = mResMngServer.getClientAppDataMng() ;
		List<ResSpace> resSpaces = null ;
		List<ClientAppBrief> clientAppBriefs = XC.arrayList() ;
		if(XC.containsAny(operUser.getAuthorities() , sGA_CDU_ClientAppData , sGA_View_AllClientAppData))
			resSpaces = Arrays.asList(clientAppDataMng.getResSpaceOfClientApp(mResMngServer.getClientAppId_SailAC())) ;
		else
			resSpaces = clientAppDataMng.getResSpaceOfUserInClientApp(userId
						, mResMngServer.getClientAppId_SailAC()) ;
		if(resSpaces.size() > 0)
		{
			for(ResSpace resSpace : resSpaces)
			{
				if(AppConsts.sResSpaceType_ClientApp.equals(resSpace.getType()))
				{
					ClientApp clientApp = clientAppDataMng.getClientApp(resSpace.getResId()) ;
					if(clientApp != null)
					{
						clientAppBriefs.add(ClientAppBrief.of(clientApp)) ;
					}
				}
			}
		}
		clientAppBriefs.sort(ClientAppBrief.sDefaultComp) ;
		return clientAppBriefs ;
	}
}
