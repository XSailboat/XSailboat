package team.sailboat.ms.ac.foreign.controller;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Validator;
import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.struct.Tuples;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.ms.ac.ProtectedApi;
import team.sailboat.commons.ms.bean.UserBrief;
import team.sailboat.commons.ms.valid.ValidateUtils;
import team.sailboat.commons.web.ac.AppAuthStatement;
import team.sailboat.ms.ac.AppConsts;
import team.sailboat.ms.ac.dbean.ClientApp;
import team.sailboat.ms.ac.dbean.ResSpace;
import team.sailboat.ms.ac.dbean.User;
import team.sailboat.ms.ac.server.IClientAppDataManager;
import team.sailboat.ms.ac.server.IUserDataManager;
import team.sailboat.ms.ac.server.ResourceManageServer;

@Tag(name=AppConsts.sTagName_foreign)
@RestController
@RequestMapping("/foreign")
public class Apis4OtherAppController
{
	final Logger mLogger = LoggerFactory.getLogger(Apis4OtherAppController.class) ;
	
	@Autowired
	ResourceManageServer mResMngServer ;

	@Autowired
	Validator mValidator ;
	
	@ProtectedApi
	@Operation(description = "声明ClientApp的权限、角色，以及两者关系")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "权限和角色声明" , required = true)
	@PostMapping(value="/clientApp/authority")
	public void decalreAppAuths(@RequestBody AppAuthStatement aAppAuthStm)
	{
		String clientId = (String)AppContext.getThreadLocal("ApiGuard:appId") ;
		Assert.notEmpty(clientId , "未能获取到当前的clientId");
		mResMngServer.getClientAppDataMng() .updateAppAuths(clientId, aAppAuthStm) ;
	}
	
	@ProtectedApi
	@Operation(description = "给自己应用增加或更新一个资源空间。限定为只能给自己添加。返回资源空间id")
	@Parameters({
		@Parameter(name="resSpaceType" , description = "资源空间类型" , required = true) ,
		@Parameter(name="resId" , description = "资源id" , required = true) ,
		@Parameter(name="resName" , description = "资源名称" , required = true)
	})
	@PostMapping(value="/clientApp/resSpace/_createOrUpdate" , produces = MediaType.TEXT_PLAIN_VALUE)
	public String createOrUpdateResSpace(@RequestParam("resSpaceType") String aResSpaceType 
			, @RequestParam("resId") String aResId 
			, @RequestParam("resName") String aResName)
	{
		// 取得当前应用
		String clientId = (String)AppContext.getThreadLocal("ApiGuard:appId") ;
		Assert.notEmpty(clientId , "未能获取到当前的clientId");
		
		ResSpace.BResSpace resSpace = new ResSpace.BResSpace() ;
		resSpace.setClientAppId(clientId) ;
		resSpace.setType(aResSpaceType) ;
		resSpace.setResName(aResName); ;
		resSpace.setResId(aResId) ;
		
		ValidateUtils.validateAndThrow(mValidator, resSpace) ;
		Tuples.T2<ResSpace , Boolean> result = mResMngServer.getClientAppDataMng().createOrUpdateResSpace(resSpace, AppConsts.sUserId_sys) ;
		if(result.getEle_2())
		{
			mLogger.info("添加或更新了资源 {}[{}] 的资源空间。" , aResName , aResId);
		}
		return result.getEle_1().getId() ;
	}
	
	@ProtectedApi
	@Operation(description = "给自己应用设置资源空间数据")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "资源空间数组" , required = true)
	@Parameter(name="deleteIfNotExists" , description = "如果原先已经存在的资源空间，不在新指定的列表中，是否删除")
	@PostMapping(value="/clientApp/resSpace/all")
	public void createOrUpdateSubspaces(@RequestBody List<ResSpace.BResSpace> aResSpaces
			, @RequestParam("deleteIfNotExists") boolean aDeleteIfNoExists)
	{
		// 取得当前应用
		String clientId = (String)AppContext.getThreadLocal("ApiGuard:appId") ;
		Assert.notEmpty(clientId , "未能获取到当前的clientId") ;
		
		aResSpaces.forEach(r->r.setClientAppId(clientId)) ;
		ValidateUtils.validateAndThrow(mValidator, aResSpaces) ;
		mResMngServer.getClientAppDataMng().createOrUpdateSubspaces(aResSpaces, aDeleteIfNoExists
				, AppConsts.sUserId_sys) ;
	}
	
	@ProtectedApi
	@Operation(description = "将自己应用的某个资源空间下的某些角色授予某个用户")
	@Parameters({
		@Parameter(name="resSpaceId" , description = "资源空间id") ,
		@Parameter(name="roleNames" , description = "角色名称，多个角色名称之间用“,”分隔" , required = true) ,
		@Parameter(name="userId" , description = "用户id" , required = true)
	})
	@PostMapping(value="/clientApp/resSpace/role/_grant")
	public void grantResSpaceRoleToUser(@RequestParam("resSpaceId") String aResSpaceId
			, @RequestParam("roleNames") String[] aRoleNames 
			, @RequestParam("userId") String aUserId)
	{
		// 取得当前应用
		String clientId = (String)AppContext.getThreadLocal("ApiGuard:appId") ;
		Assert.notEmpty(clientId , "未能获取到当前的clientId");
		
		String clientAppId = ResSpace.getClientAppIdFrom(aResSpaceId) ;
		Assert.equals(clientId, clientAppId, "指定的资源空间id不是当前调用接口的ClientApp的！") ;
		
		mResMngServer.getClientAppDataMng().grantResSpaceRoleToUser(aResSpaceId, aRoleNames, aUserId, AppConsts.sUserId_sys) ;
	}
	
	@ProtectedApi
	@Operation(description = "通过资源id删除自己应用的某个资源空间")
	@Parameter(name="resId" , description = "资源id" , required = true)
	@DeleteMapping(value="/clientApp/resSpace/one/byResId")
	public void deleteResSpaceByResId(@RequestParam("resId") String aResId)
	{
		String clientId = (String)AppContext.getThreadLocal("ApiGuard:appId") ;
		Assert.notEmpty(clientId , "未能获取到当前的clientId") ;
		ClientApp app = mResMngServer.getClientAppDataMng().getClientApp(clientId) ;
		if(mResMngServer.getClientAppDataMng().deleteResSpaceByResId(aResId, AppConsts.sUserId_sys))
		{
			mLogger.info("应用 {} 通过资源id[{}]删除了其下的资源空间。" , app.getName() 
					, aResId) ;
		}
	}
	
	@ProtectedApi
	@Operation(description = "获取指定id用户的显示名。键是用户id，值是显示名")
	@Parameter(name="userIds" , description = "用户id，多个用“,”分隔" , required = true)
	@GetMapping(value="/user/displayName/multi" , produces = MediaType.APPLICATION_JSON_VALUE)
	public String getUserDisplayNames(@RequestParam("userIds") String[] aUserIds , HttpServletRequest aReq)
	{
		if(XC.isEmpty(aUserIds))
			return "{}" ;
		String clientId = (String)AppContext.getThreadLocal("ApiGuard:appId") ;
		Assert.notEmpty(clientId , "未能获取到当前的clientId") ;
		ClientApp app = mResMngServer.getClientAppDataMng().getClientApp(clientId) ;
		mLogger.info("应用 {} 从地址 {} 上发起访问，获取用户的显示名：{}" , app.getName() , aReq.getRemoteAddr() 
				, XString.toString(",", aUserIds)) ;
		JSONObject jo = new JSONObject() ;
		IUserDataManager userDataMng = mResMngServer.getUserDataMng() ;
		for(String userId : aUserIds)
		{
			User user = userDataMng.getUser(userId) ;
			if(user != null)
				jo.put(userId, user.getDisplayName()) ;
		}
		return jo.toString() ;
	}
	
	@ProtectedApi
	@Operation(description = "取得指定用户的手机号")
	@Parameter(name="userId" , description = "用户Id" , required = true)
	@GetMapping(value="/user/mobile" , produces = MediaType.TEXT_PLAIN_VALUE)
	public String getUserMobile(@RequestParam("userId") String aUserId , HttpServletRequest aReq)
	{
		String clientId = (String)AppContext.getThreadLocal("ApiGuard:appId") ;
		Assert.notEmpty(clientId , "未能获取到当前的clientId") ;
		ClientApp app = mResMngServer.getClientAppDataMng().getClientApp(clientId) ;
		mLogger.info("应用 {} 从地址 {} 上发起访问，获取用户的手机号：{}" , app.getName() , aReq.getRemoteAddr() 
				, aUserId) ;	
		User user = mResMngServer.getUserDataMng().getUser(aUserId) ;
		return user != null?user.getMobile():null ;
	}
	
	@ProtectedApi
	@Operation(description = "取得所有可访问当前APP或当前ClientApp的某个资源空间的用户简略信息")
	@Parameter(name="resSpaceId" , description = "资源空间id")
	@GetMapping(value="/clientApp/user/brief/all" , produces = MediaType.APPLICATION_JSON_VALUE)
	public List<UserBrief> getUserBriefsOfClientApp(@RequestParam(name="resSpaceId" , required = false) String aResSpaceId 
			, HttpServletRequest aReq)
	{
		String clientId = (String)AppContext.getThreadLocal("ApiGuard:appId") ;
		Assert.notEmpty(clientId , "未能获取到当前的clientId") ;
		ClientApp app = mResMngServer.getClientAppDataMng().getClientApp(clientId) ;
		Assert.notNull(app , "无效的ClietApp的id：%s" , clientId) ;
		mLogger.info("应用 {} 从地址 {} 上发起访问，获取可访问此应用用户的简略信息" , app.getName() , aReq.getRemoteAddr()) ;
		Collection<User> userList = null ;
		if(XString.isNotEmpty(aResSpaceId))
		{
			String clientAppId = ResSpace.getClientAppIdFrom(aResSpaceId) ;
			Assert.equals(clientId, clientAppId , "指定的资源空间[%s]不是当前调用的ClientApp的！" , aResSpaceId) ;
			userList = XC.extractAsArrayList(mResMngServer.getClientAppDataMng().getUsersOfCanVisitResSpace(aResSpaceId)
					, Tuples.T2::getEle_1) ;
		}
		else
			userList = mResMngServer.getClientAppDataMng().getUsersOfCanVisitClientApp(clientId) ;
		return XC.extractAsArrayList(userList , User::toBrief) ;
	}
	
	@ProtectedApi
	@Operation(description = "获取指定用户的简要信息，包括真实姓名和显示名")
	@Parameter(name="userIds" , description = "用户id，多个用“,”分隔" , required = true)
	@GetMapping(value="/user/brief/multi" , produces = MediaType.APPLICATION_JSON_VALUE)
	public List<UserBrief> getUserBriefs(@RequestParam("userIds") String[] aUserIds , HttpServletRequest aReq)
	{
		String clientId = (String)AppContext.getThreadLocal("ApiGuard:appId") ;
		Assert.notEmpty(clientId , "未能获取到当前的clientId") ;
		IClientAppDataManager clientAppDataMng = mResMngServer.getClientAppDataMng() ;
		ClientApp app = clientAppDataMng.getClientApp(clientId) ;
		mLogger.info("应用 {} 从地址 {} 上发起访问，获取这些用户的简略信息：{}" , app.getName() , aReq.getRemoteAddr()
				, XString.toString(",", aUserIds)) ;
		if(XC.isEmpty(aUserIds))
			return Collections.emptyList() ;
		
		IUserDataManager userDataMng = mResMngServer.getUserDataMng() ;
		return XC.extractAsArrayList(aUserIds, userId->clientAppDataMng.canVisitApp(userId, clientId)
				, userId->{
					User user = userDataMng.getUser(userId) ;
					return user == null?null:user.toBrief() ;
				} , true) ;
	}
	
	@Operation(description = "通过用户的钉钉openId获取用户id")
	@Parameter(name="dingOpenId" , description = "用户的钉钉openId" , required = true)
	@GetMapping(value="/user/userId/byDingOpenId" , produces = MediaType.TEXT_PLAIN_VALUE)
	public String getUserIdByDingOpenId(@RequestParam("dingOpenId") String aDingOpenId)
	{
		User user = mResMngServer.getUserDataMng().getUserByDingOpenId(aDingOpenId) ;
		return user == null?null:user.getId() ;
	}
	
	@Operation(description = "创建一个不能用于登录的用户，返回这个对象的id。如果要想登录，必须由管理员设置上其它信息")
	@Parameter(name="realName" , description = "用户人名" , required = true)
	@PostMapping(value="/user/one" , produces = MediaType.TEXT_PLAIN_VALUE)
	public String createUnloginUser(@RequestParam("realName") String aRealName
			, HttpServletRequest aReq)
	{
		String clientId = (String)AppContext.getThreadLocal("ApiGuard:appId") ;
		Assert.notEmpty(clientId , "未能获取到当前的clientId") ;
		ClientApp app = mResMngServer.getClientAppDataMng().getClientApp(clientId) ;
		mLogger.info("应用 {} 从地址 {} 上发起访问，创建了一个用户：{}" , app.getName() , aReq.getRemoteAddr()
				, aRealName) ;
		User.BUser user = new User.BUser() ;
		user.setRealName(aRealName) ;
		return mResMngServer.getUserDataMng().createUser(user , AppConsts.sUserId_sys)
				.getId() ;
	}
	
	@ProtectedApi
	@Operation(description = "通过用户的真实姓名获取用户的简要信息。可能存在多个同名者。")
	@Parameter(name="realName" , description = "用户的真实姓名")
	@GetMapping(value="/user/brief/byRealName" , produces = MediaType.APPLICATION_JSON_VALUE)
	public List<UserBrief> getUserBriefsByUserRealName(@RequestParam("realName") String aRealName
			, HttpServletRequest aReq)
	{
		String clientId = (String)AppContext.getThreadLocal("ApiGuard:appId") ;
		Assert.notEmpty(clientId , "未能获取到当前的clientId") ;
		ClientApp app = mResMngServer.getClientAppDataMng().getClientApp(clientId) ;
		mLogger.info("应用 {} 从地址 {} 上发起访问，通过真实姓名获取用户的简要信息：{}" , app.getName() , aReq.getRemoteAddr()
				, aRealName) ;
		return XC.extractAsArrayList(mResMngServer.getUserDataMng().getUsersByRealName(aRealName)
				, User::toBrief) ;
	}
	
}
