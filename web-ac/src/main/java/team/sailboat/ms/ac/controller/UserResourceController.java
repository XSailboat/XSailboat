package team.sailboat.ms.ac.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.ms.ac.InnerProtectedApi;
import team.sailboat.ms.ac.AppConsts;
import team.sailboat.ms.ac.dbean.User;
import team.sailboat.ms.ac.server.ResourceManageServer;

/**
 * 
 * 用户资源获取接口
 *
 * @author yyl
 * @since 2024年12月4日
 */
@RestController
@RequestMapping("/oauth2/user")
public class UserResourceController
{
	@Autowired
	OAuth2AuthorizationService mAuthorizationService;
	
	@Autowired
	ResourceManageServer mResMngServer ;
	
	@InnerProtectedApi
	@Parameter(name="scopes" , description = "资源范围，多个自建用“,”分隔")
	@Operation(description = "取得指定AccessToken所授权的范围内的scope数据")
	@GetMapping(value="/resource/ofScope" , produces = MediaType.APPLICATION_JSON_VALUE)
	public String getResources(@RequestParam("scopes") String[] aScopes)
	{
		Authentication auth = SecurityContextHolder.getContext().getAuthentication() ;
		Object principal = auth.getPrincipal() ;
		if(principal instanceof Jwt j)
		{
			List<String> wantScopes = XC.arrayList(aScopes) ;
			List<String> scopes = j.getClaimAsStringList("scope") ;
			wantScopes.retainAll(scopes) ;
			if(wantScopes.isEmpty())
			{
				return "{}" ;
			}
			String userId = j.getClaimAsString("userId") ;
			User user = mResMngServer.getUserDataMng().getUser(userId) ;
			Assert.notNull(user , "不存在id为 %s 的用户！" , userId) ;
			JSONObject resultJo = JSONObject.one()
					.put("userId" , user.getId()) ;
			for(String scope : wantScopes)
			{
				switch(scope)
				{
				case AppConsts.sScope_user_basic :
					resultJo.put(AppConsts.sScope_user_basic , new JSONObject()
							.put("realName" , user.getRealName())
							.put("sex" , user.getSex())) ;
					break ;
				case AppConsts.sScope_user_org_job:
					resultJo.put(AppConsts.sScope_user_basic , new JSONObject()
							.put("department" , user.getDepartment())) ;
					break ;
				case AppConsts.sScope_user_contact_info:
					resultJo.put(AppConsts.sScope_user_basic , new JSONObject()
							.put("mobile" , user.getMobile())
							.put("email" , user.getEmail())) ;
					break ;
				}
			}
			return resultJo.toJSONString() ;
		}
		return null ;
	}
	
	@InnerProtectedApi
	@Operation(description = "取得指定AccessToken所授权的范围内的scope数据")
	@GetMapping(value="/info" , produces = MediaType.APPLICATION_JSON_VALUE)
	public String getUserInfo()
	{
		Authentication auth = SecurityContextHolder.getContext().getAuthentication() ;
		Object principal = auth.getPrincipal() ;
		JSONObject userInfoJo = JSONObject.one() ;
		if(principal instanceof Jwt j)
		{
			String userId = j.getClaimAsString("userId") ;
			User user = mResMngServer.getUserDataMng().getUser(userId) ;
			Assert.notNull(user , "不存在id为 %s 的用户！" , userId) ;
			List<String> scopes = j.getClaimAsStringList("scope") ;
			userInfoJo.put("userId" , user.getId())
					.put("username" , user.getUsername()) ;			// TO-DO 不能返回用户名,用显示名
			StringBuilder displayNameBld = new StringBuilder() ;
			for(String scope : scopes)
			{
				switch(scope)
				{
				case AppConsts.sScope_user_basic :
					userInfoJo.put("realName" , user.getRealName())
							.put("sex" , user.getSex()) ;
					break ;
				case AppConsts.sScope_user_org_job:
					userInfoJo.put("department" , user.getDepartment()) ;
					displayNameBld.append(user.getDepartment())
						.append('-') ;
					break ;
				case AppConsts.sScope_user_contact_info:
					userInfoJo.put("mobile" , user.getMobile())
							.put("email" , user.getEmail()) ;
					break ;
				}
			}
			displayNameBld.append(userInfoJo.optString("realName" , user.getId())) ;
			userInfoJo.put("displayName" , displayNameBld.toString()) ;
		}
		return userInfoJo.toJSONString() ;
	}
}
