package team.sailboat.ms.ac;

/**
 * 
 * 权限及权限检查语句的定义
 *
 * @author yyl
 * @since 2024年11月4日
 */
public interface IAppAuths
{
	// ==================================================================
	// 用户的数据相关的权限。包括用户信息
	// ==================================================================
	
	/**
	 * 查看所有用户的信息		<br />
	 */
	static final String sAC_View_AllUsers = "View_AllUsers" ;
	static final String sHasAuthority_View_AllUsers = "hasAuthority('" + sAC_View_AllUsers + "')" ;
	
	/**
	 * 用户的数据的管理权限			<br>
	 * 增删改用户的权限，包括查询所有用户
	 */
	static final String sAC_CDU_UserData = "CDU_UserData" ;
	static final String sHasAuthority_CDU_UserData = "hasAuthority('" + sAC_CDU_UserData + "')" ;
	
	/**
	 * 
	 * 修改其它用户密码的权限
	 * 
	 */
	static final String sAC_Reset_PasswordOfOtherUser = "Reset_PasswordOfOtherUser" ;
	static final String sHasAuthority_Reset_PasswordOfOtherUser = "hasAuthority('" + sAC_Reset_PasswordOfOtherUser + "')" ;
	
	// ==================================================================
	// ClientApp的数据相关的权限。包括：角色、权限、资源空间
	// ==================================================================
	
	/**
	 * 增删改查ClientApp的数据。包括：ClientApp、角色、权限、资源空间		<br>
	 * 
	 */
	static final String sAC_CDU_ClientAppData = "CDU_ClientAppData" ;
	static final String sHasAuthority_CDU_ClientAppData = "hasAuthority('" + sAC_CDU_ClientAppData + "')" ;
	
	/**
	 * 查看所有ClientApp的数据(ClientApp、角色、权限、资源空间)		<br />
	 */
	static final String sAC_View_AllClientAppData = "View_AllClientAppData" ;
	static final String sHasAuthority_View_AllClientAppData = "hasAuthority('" + sAC_View_AllClientAppData + "')" ;
	
	/**
	 * 查看所有ClientApp的AppSecret		<br />
	 */
	static final String sAC_View_AllClientAppSecret = "View_AllClientAppSecret" ;
	static final String sHasAuthority_View_AllClientAppSecret = "hasAuthority('" + sAC_View_AllClientAppSecret + "')" ;
	
	/**
	 * 查看特定ClientApp的AppSecret		<br />
	 */
	static final String sACP_View_Special_ClientAppSecret = "View_Special_ClientAppSecret:" ;
	static final String sHasResAuthority_View_Special_ClientAppSecret = "hasAuthority('" 
				+ sACP_View_Special_ClientAppSecret + "' + #_resId_)" ;
	
	/**
	 * 重设任何一个ClientApp的AppSecret。包括了查看权限。得看看现在怎么样，重置后怎么样，不能看不到效果		<br />
	 */
	static final String sAC_Reset_AllClientAppSecret = "Reset_AllClientAppSecret" ;
	static final String sHasAuthority_Reset_AllClientAppSecret = "hasAuthority('" + sAC_Reset_AllClientAppSecret + "')" ;
	
	/**
	 * 重设某一个ClientApp的AppSecret。包括了查看权限。得看看现在怎么样，重置后怎么样，不能看不到效果		<br />
	 */
	static final String sACP_Reset_Special_ClientAppSecret = "Reset_Special_ClientAppSecret" ;
	static final String sHasAuthority_Reset_Special_ClientAppSecret = "hasAuthority('"
				+ sACP_Reset_Special_ClientAppSecret + "' + #_resId_)" ; ;
		
	/**
	 * 查看指定的ClientApp的数据(ClientApp、角色、权限、资源空间)的权限		<br />
	 * 属于“ClientApp”类型的资源空间
	 */
	static final String sACP_View_Special_ClientAppData = "View_Special_ClientAppData:" ;
	static final String sHasResAuthority_View_Special_ClientAppData = "hasAuthority('"
			+ sACP_View_Special_ClientAppData + "' + #_resId_)" ;
	
	/**
	 * 更新指定的ClientApp的数据(角色、权限、资源空间)的权限。		<br />
	 * 属于“ClientApp”类型的资源空间。不包括ClientApp的信息修改，这需要让认证中心管理员去做
	 */
	static final String sACP_Update_Special_ClientAppData = "Update_Special_ClientAppData:" ;
	static final String sHasResAuthority_Update_Special_ClientAppData = "hasAuthority('"
			+ sACP_Update_Special_ClientAppData + "' + #_resId_)" ;
	
	/**
	 * 管理指定ClientApp的可访问用户的权限				<br />
	 * 包括对用户授权、取消授权
	 */
	static final String sACP_Manage_Special_CanVisitUser = "Manage_Special_CanVisitUser:" ;
	static final String sHasResAuthority_Manage_Special_CanVisitUser = "hasAuthority('"
			+ sACP_Manage_Special_CanVisitUser + "' + #_resId_)" ;
	
	
	// ==================================================================
	// AuthCenter的数据相关的权限。包括：API和组织单元
	// ==================================================================
	
	/**
	 * 增删改查组织单元数据的权限
	 */
	static final String sAC_CDU_OrgUnit = "CDU_OrgUnit" ;
	static final String sHasAuthority_CDU_OrgUnit = "hasAuthority('" + sAC_CDU_OrgUnit + "')" ;
	
	/**
	 * 查看组织单元下面用户的权限
	 */
	static final String sAC_View_OrgUnitAndUsers = "View_OrgUnitAndUsers" ;
	static final String sHasAuthority_View_OrgUnitAndUsers = "hasAuthority('" + sAC_View_OrgUnitAndUsers + "')" ;
	
	/**
	 * 查看认证中心声明的供ClientApp调用的API权限		<br />
	 * 是所有API，不是具体某个ClientApp的
	 */
	static final String sAC_View_Apis = "View_Apis" ;
	static final String sHasAuthority_View_Apis = "hasAuthority('" + sAC_View_Apis + "')" ;
	
	// ==================================================================
	// 认证中心的页面权限
	// ==================================================================
	
	/**
	 * 认证中心的首页，主要是总体的统计信息
	 */
	static final String sAC_View_HomePage = "View_HomePage" ;
	static final String sHasAuthority_View_HomePage = "hasAuthority('" + sAC_View_HomePage + "')" ;
	
	
	// ==================================================================
	// 统计数据
	// ==================================================================
	/**
	 * 查看全局统计数据
	 */
	static final String sAC_View_GlobalStsData = "View_GlobalStsData" ;
	static final String sHasAuthority_View_GlobalStsData = "hasAuthority('" + sAC_View_GlobalStsData + "')" ;
	
}
