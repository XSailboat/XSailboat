package team.sailboat.login.extend.ding;

import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dingtalk.api.response.OapiSnsGetuserinfoBycodeResponse.UserInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.ms.ac.InnerProtectedApi;
import team.sailboat.commons.ms.crypto.RSAKeyPairMaker4JS;
import team.sailboat.ms.ac.AppAuths;
import team.sailboat.ms.ac.AppConsts;
import team.sailboat.ms.ac.dbean.User;
import team.sailboat.ms.ac.exception.ValidateCodeException;
import team.sailboat.ms.ac.server.ResourceManageServer;

/**
 * 钉钉登录
 *
 * @author yyl
 * @since 2024年12月19日
 */
@Controller
public class DingLoginController
{	
	@Autowired
	ResourceManageServer mResMngServer ;
	
	@Autowired
	DingLoginComponent mComponent ;
	
	@Autowired
	RSAKeyPairMaker4JS mRSAMaker ;
	
	@Autowired
	PasswordEncoder mPasswordEncoder ;
	
	@Autowired
	AuthenticationEventPublisher mAuthEventPublisher ;
	
	public DingLoginController()
	{
	}
	
	@RequestMapping("/bind_acccount")
	public String bindAcccount()
	{
		return "t_bind_account" ;
	}
	
	@Operation(description = "绑定钉钉账号")
	@Parameters({
		@Parameter(name="username" , description = "用户名") ,
		@Parameter(name="codeId" , description = "动态RSA秘钥的标识码。Https协议下，可以不用加密") ,
		@Parameter(name="password" , description = "密码。用动态RSA秘钥的公钥加密过后的密码。Https协议下，可以不用加密") ,
	})
	@InnerProtectedApi
	@PostMapping("/bindAccount/ding")
	public String bindDingAccount(@RequestParam("username") String aUsername
			, @RequestParam(name="codeId" , required = false) String aCodeId
			, @RequestParam("password") String aPassword
			, HttpServletRequest aReq) throws Exception
	{
		HttpSession session = aReq.getSession() ;
		UserInfo userInfo = (UserInfo) session.getAttribute("dingCodeUserInfo");
		String dingCode = (String)session.getAttribute("dingCode") ;
		if(userInfo == null)
		{
			session.setAttribute("SPRING_SECURITY_LAST_EXCEPTION" , new ValidateCodeException("请先钉钉扫码！"));
			return "redirect:/login_view?error" ;
		}
		else
		{
			String password = mRSAMaker.decrypt4js(aCodeId , aPassword) ;
			User user = mResMngServer.getUserDataMng().loadUserByUsername(aUsername) ;
			Assert.notNull(user , "用户名或密码不正确！") ;
			Assert.isTrue(!mPasswordEncoder.matches(password, user.getPassword()) , "用户名或密码不正确！") ;
			session.removeAttribute("dingCodeUserInfo") ;
			session.removeAttribute("dingCode") ;
			user.setExtAttributes(user.getExtAttributes_JSONObject()
					.put(IDingConsts.sDingOpenId_UserExtAttr , userInfo.getOpenid())
					.toString()) ;
			user.setLastEditTime(new Date()) ;
			user.setLastEditUserId(user.getId()) ;
			
			session.setAttribute("DingLoginUser", user) ;
			mAuthEventPublisher.publishAuthenticationSuccess(new DingCodeAuthenticationToken(user , dingCode)) ;
		}
	    return "redirect:manager" ;
	}
	
	@InnerProtectedApi
	@PostMapping("/creteNewAccount/ofDing")
	public String createNewAccountofDing(HttpServletRequest aReq) throws Exception
	{
		HttpSession session = aReq.getSession() ;
		UserInfo userInfo = (UserInfo)session.getAttribute("dingCodeUserInfo" ) ;
		Assert.notNull(userInfo , "不存在钉用户信息！");
		session.removeAttribute("dingCodeUserInfo") ;
		// 开始创建新用户，这个用户创建出来之后，得设置密码之后才能用来登录
		User.BUser buser = new User.BUser() ;
		buser.setUsername(UUID.randomUUID().toString()) ;
		buser.setRealName(userInfo.getNick()) ;
		User user = mResMngServer.getUserDataMng().createUser(buser, AppConsts.sUserId_sys) ;
		
		JSONObject jobj = user.getExtAttributes_JSONObject() ;
		jobj.put(IDingConsts.sDingOpenId_UserExtAttr , userInfo.getOpenid()) ;
		user.setExtAttributes(jobj.toString()) ;
		user.setLastEditTime(new Date()) ;
		user.setDepartment(mComponent.getDingClient().getMainDepartmentName(userInfo.getUnionid())) ;
		
		// 赋予用户游客角色
		mResMngServer.getClientAppDataMng().grantRoleToUserByName(mResMngServer.getClientAppId_SailAC() 
				, AppAuths.sRoleName_Common , user.getId() , null) ;
		
		aReq.getSession().setAttribute("DingLoginUser", user) ;
	    return "redirect:/dingLogin" ;
	}
}
