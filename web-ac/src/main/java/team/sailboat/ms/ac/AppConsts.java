package team.sailboat.ms.ac;

import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import team.sailboat.base.SysConst;
import team.sailboat.commons.web.ac.IAuthCenterConst;

/**
 * 认证中心的常量表
 *
 * @author yyl
 * @since 2024年10月10日
 */
public interface AppConsts extends SysConst , IAuthCenterConst
{

	public static final String sAppName = "SailAC";

	public static final String sAppCnName = "认证中心";

	public static final String sAppDesc = "大数据平台的认证授权中心";

	public static final String sFN_AppConfig = "config.ini";

	public static final String sLogFileName = "service.log";

	public static final String sAppDirName = "SailAC";

	public static final String sSysLogName = "System";

	public static final String sAccessLogName = "AccessLog";

	public static final String sHTTP_HN_clientUser = "clientUser";

	public static final String sDomain = sAppName;

	/**
	 * 管理员用户名
	 */
	public static final String sUser_admin = "admin";

	/**
	 * 管理员的真名
	 */
	public static final String sUserRelaName_admin = "超级管理员";
	
	/**
	 * 角色-后台管理员
	 */
	public static final String sRoleName_admin = "后台管理员";

	/**
	 * 认证中心的厂家
	 */
	public static final String sCompany = "威海欣智信息科技有限公司";
	
	/**
	 * 资源空间类型：ClientApp
	 */
	public static final String sResSpaceType_ClientApp = "ClientApp" ;

	/**
	 * 重新设置过期密码的页面路径
	 */
	public static final String sPagePath_ResetExpiredPasswd = "/pwd_reset";

	/**
	 * 登录接口
	 */
	public static final String sApiPath_login = "/login";

	/**
	 * 登录失败的缺省URL页面
	 */
	public static final String sViewPath_loginFailure = "/login_view?error";
	
	/**
	 * 登录页面
	 */
	public static final String sViewPath_login = "/login_view" ;
	
	/**
	 * 用户授权ClientApp获取自己的信息页
	 */
	public static final String sViewPath_consent = "/oauth2/consent_view" ;

	/**
	 * XApp签名方式
	 */
	public static final ClientAuthenticationMethod sXAppSign = new ClientAuthenticationMethod("XAppSign");
	
	/**
	 * API名称		<br />
	 * 用临时授权码换取AccessToken
	 */
	public static final String sApiName_GetAccessToken = "Oauth2TokenPOST" ;
	
	/**
	 * ResId的提取方法。		<br />
	 * 从BClientApp中提取id作为ResId
	 */
	public static final String sResIdGetter_getClientAppIdFromBClientApp = "getClientAppIdFromBClientApp" ;
	
	/**
	 * ResId的提取方法。		<br />
	 * 从R_User_ResSpace_Role关联id中提取ClientApp的id
	 */
	public static final String sResIdGetter_getClientAppIdFromIdOfR_User_ResSpace_Role = "getClientAppIdFromIdOfR_User_ResSpace_Role" ;
	
	/**
	 * ResId的提取方法。		<br />
	 * 从ResSpace的id中提取ClientApp的id
	 */
	public static final String sResIdGetter_getClientAppIdFromResSpaceId = "getClientAppIdFromResSpaceId" ;
	
	/**
	 * ResId的提取方法。		<br />
	 * 提取BRole的clientAppId属性
	 */
	public static final String sResIdGetter_getClientAppIdFromBRole_clientAppId = "getClientAppIdFromBRole_clientAppId" ;
	
	/**
	 * ResId的提取方法。		<br />
	 * 提取BRole的id，然后查询Role，获取其中的clientAppId属性
	 */
	public static final String sResIdGetter_getClientAppIdFromBRole_id = "getClientAppIdFromBRole_id" ;
	
	/**
	 * ResId的提取方法。		<br />
	 * 通过roleId查询Role，获取其中的clientAppId属性
	 */
	public static final String sResIdGetter_getClientAppIdFromRoleId = "getClientAppIdFromRoleId" ;
	
	/**
	 * API分类标签名：供ClientApp调用的接口
	 */
	public static final String sTagName_foreign = "供ClientApp调用的接口" ;
	
	/**
	 * 验证用的图片存放的目录名
	 */
	public static final String sFN_secu_images = "secu_images" ;
	
	/**
	 * ClientApp的授权方式			<br />
	 * 授权码模式
	 */
	public static final String sAppGrantType_authorization_code = "authorization_code" ;
	
	/**
	 * ClientApp的授权方式			<br />
	 * 刷新码模式
	 */
	public static final String sAppGrantType_refresh_token = "refresh_token" ;
	
	/**
	 * ClientApp的授权方式			<br />
	 * 中台微服务模式
	 */
	public static final String sAppGrantType_app_only = "app_only" ;
}
