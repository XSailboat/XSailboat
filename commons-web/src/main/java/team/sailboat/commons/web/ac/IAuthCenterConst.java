package team.sailboat.commons.web.ac;

import org.springframework.security.oauth2.core.AuthorizationGrantType;

/**
 * 
 * 认证中心(SailAC)的一些基础常量
 *
 * @author yyl
 * @since 2024年10月21日
 */
public interface IAuthCenterConst
{
	/**
	 * 缺省全局空间			<br />
	 * 和具体资源无关
	 */
	public static final String sResSpaceType_default = "缺省全局空间" ;
	
	/**
	 * 资源范围			<br />
	 * 用户基本信息（姓名、性别）
	 */
	public static final String sScope_user_basic = "user_basic" ;
	
	
	/**
	 * 资源范围			<br />
	 * 用户所属组织及职务
	 */
	public static final String sScope_user_org_job = "user_org_job" ;
	
	
	/**
	 * 资源范围			<br />
	 * 用户联系方式（手机、email）
	 */
	public static final String sScope_user_contact_info = "user_contact_info" ;
	
	/**
	 * 认证中心的认证接口路径
	 */
	public static final String sGET_authorize = "/oauth2/authorize" ;
	
	/**
	 * 认证中心用授权码换取AccessToken的接口路径
	 */
	public static final String sGET_token = "/oauth2/token" ;
	
	/**
	 * 登录成功后凭AccessToken换取用户信息的接口路径
	 */
	public static final String sGET_userInfo = "/oauth2/user/info"  ;
	
	/**
	 * 返回授权码的响应体中的键			<br />
	 * 值是corsToken
	 */
	public static final String sTokenReply_corsToken = "corsToken" ;
	
	/**
	 * 应用认证授权模式		<br />
	 * cors_token
	 */
	public static final AuthorizationGrantType sGrantType_cork_token = new AuthorizationGrantType("cors_token") ;
	
	/**
	 * Client信息注册id
	 */
	public static final String sClientResitrationId = "sailboat" ;
}
