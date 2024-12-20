package team.sailboat.ms.ac.controller;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.ms.MSApp;
import team.sailboat.ms.ac.AppConfig;
import team.sailboat.ms.ac.AppConsts;
import team.sailboat.ms.ac.bean.ClientAppBrief;
import team.sailboat.ms.ac.dbean.ResSpace;
import team.sailboat.ms.ac.dbean.User;
import team.sailboat.ms.ac.exception.ValidateCodeException;
import team.sailboat.ms.ac.server.ResourceManageServer;
import team.sailboat.ms.ac.utils.SecurityUtils;

@Controller
public class ViewController
{
	@Autowired
	AppConfig mAppConfig;

	@Autowired
	ResourceManageServer mResMngServer;
	
	@RequestMapping("/index")
	public String index(Model aModel)
	{
		if (mResMngServer.isAdminPasswordSetted())
		{
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User)
			{
				return "redirect:/manage";
				//				UserDetails user = (UserDetails)auth.getPrincipal() ;
				//				if(user.getAuthorities().contains(AppAuths.sViewBackgroundMngPages))
				//				{
				//					return "redirect:m" ;
				//				}
				//				else
				//				{
				//					// 查看自己的信息
				//					return "redirect:user_info" ;
				//				}
			}
			aModel.addAttribute("dingLoginEnable", mAppConfig.isDingLoginEnable());
			// 如果已经登陆，就跳转到首页
			return "redirect:login_view";
			// 如果没有登陆，就跳转到登陆页面
		}
		else
		{
			aModel.addAttribute("banner", "设置超级系统管理员密码");
			aModel.addAttribute("user_admin", AppConsts.sUser_admin);
			return "t_init";
		}
	}

	//	http://localhost:12000/oauth2/consent_view?scope=user_basic&client_id=ys4Mp7HVepj7D42p&state=koaqHrHIHE_YoEN_SX7aO5HzHAnher11Hi2x6ZEvjIU=
	@RequestMapping(value=AppConsts.sViewPath_consent)
	public String consentView(@RequestParam("scope") String scope
			, @RequestParam("client_id") String client_id
			, @RequestParam("state") String state
			, Model aModel, HttpServletRequest request)
	{
		List<Object> consents = new LinkedList<>();
		String[] scopes = scope.split(" ");
		for (String sc : scopes)
		{
			Map<String, String> consent = new HashMap<>();
			consent.put("code", sc);
			if (sc.equals("user_basic"))
			{
				consent.put("description", "用户基本信息（姓名、性别）");
			}
			else if (sc.equals("user_org_job"))
			{
				consent.put("description", "用户所属组织及职务");
			}
			else if (sc.equals("user_contact_info"))
			{
				consent.put("description", "用户联系方式（手机、email）");
			}
			consents.add(consent);
		}
		aModel.addAttribute("consents", consents);
		ClientAppBrief clientAppBrief = ClientAppBrief.of(mResMngServer.getClientAppDataMng()
				.getClientAppByAppKey(client_id));
		aModel.addAttribute("app", clientAppBrief);
		aModel.addAttribute("state", state);
		aModel.addAttribute("banner", "授权管理");
		return "t_consent";
	}

	@RequestMapping("/login_view")
	public String loginView(Model aModel, HttpServletRequest request, HttpSession session)
	{
		if (mResMngServer.isAdminPasswordSetted())
		{
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			if (auth.isAuthenticated() && auth.getPrincipal() instanceof User)
				return "redirect:/manage";
			Map<String, String[]> map = request.getParameterMap();
			if (map.containsKey("error"))
			{
				Exception e = (Exception) request.getSession().getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
				if (e == null)
				{
					aModel.addAttribute("errMsg", "");
				}
				else if (e instanceof BadCredentialsException || e instanceof InternalAuthenticationServiceException)
				{
					Integer remainRetryTimes = (Integer) request.getSession().getAttribute("remainRetryTimes");
					if (remainRetryTimes == 0)
					{
						aModel.addAttribute("errMsg", "登录失败5次，请30分钟之后再试！");
					}
					else
					{
						aModel.addAttribute("errMsg", "用户名或者密码错误！");
					}
				}
				else if (e instanceof ValidateCodeException)
				{
					aModel.addAttribute("errMsg", e.getMessage());
				}
				else if (e instanceof AccountExpiredException)
				{
					aModel.addAttribute("errMsg", "当前用户已过期，请联系管理员!");
				}
				else if (e instanceof CredentialsExpiredException)
				{
					aModel.addAttribute("errMsg", "登录密码已过期");
				}
				else
				{
					aModel.addAttribute("errMsg", e.getMessage());
				}
				request.getSession().removeAttribute("SPRING_SECURITY_LAST_EXCEPTION");
			}
			aModel.addAttribute("appId", mAppConfig.getDingAppKey());
			aModel.addAttribute("banner", "用户登录");
			aModel.addAttribute("callbackUrl", mAppConfig.getDingCodeCallbackUrl());
			return "t_login";
			//		return "t_bind_account";
			//		return "t_init";
		}
		else
		{
			aModel.addAttribute("banner", "设置超级系统管理员密码");
			aModel.addAttribute("user_admin", AppConsts.sUser_admin);
			return "t_init";
		}
	}

	@RequestMapping("/pwd_reset")
	public String pwdReset(Model aModel, HttpServletRequest request, HttpSession session)
	{
		aModel.addAttribute("authToken", request.getAttribute("authToken"));
		return "t_pwd_reset";
	}

	@RequestMapping("/manage")
	public String manage(Model aModel)
	{
		User user = SecurityUtils.checkUser();
		// 找寻用户是否有ClientApp类型资源空间
		List<ResSpace> resSpaces = mResMngServer.getClientAppDataMng().getResSpaceOfUserInClientApp(user.getId()
				, mResMngServer.getClientAppId_SailAC()) ;
		if(resSpaces.size() > 0)
		{
			ResSpace resSpace = XC.findFirst(resSpaces , rs->AppConsts.sResSpaceType_ClientApp.equals(rs.getType()))
					.orElse(null) ;
			if(resSpace != null)
			{
				aModel.addAttribute("resId", resSpace.getResId());
			}
		}
		aModel.addAttribute("sysEnv", MSApp.instance().getSysEnv());
		aModel.addAttribute("user", new JSONObject().put("displayName", user.getDisplayName()).put("id", user.getId()));
		aModel.addAttribute("authes", user.getAuthorities());
		aModel.addAttribute("credentialRenewalDays", mAppConfig.getCredentialRenewalDays());
		return "pages/manage/index";
	}
}
