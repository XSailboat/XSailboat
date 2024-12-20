package team.sailboat.ms.ac;

import team.sailboat.commons.web.ac.AAuthority;
import team.sailboat.commons.web.ac.ARole;
import team.sailboat.commons.web.ac.AppAuthStatement;

/**
 *
 * 应用(SailAC)的权限和角色声明
 *
 * @author yyl
 * @since 2024年10月10日
 */
public class AppAuths implements IAppAuths
{
	
	public static final String sRoleName_Admin = "后台管理员" ;
	public static final String sRoleName_BGGuest = "后台游客" ;
	public static final String sRoleName_Common = "一般用户" ;
	public static final String sRoleName_ClientAppAdmin = "ClientApp负责人" ;
	public static final String sRoleName_ClientAppGuest = "ClientApp游客" ;
	
	static AppAuthStatement sStatement ;
	
	public static AppAuthStatement getAppAuthStatement()
	{
		if(sStatement == null)
		{
			sStatement = new AppAuthStatement() ;
			
			// 角色
			sStatement.addRole(ARole.ofDefaultGlobalRespace(sRoleName_Admin , "具有后台完全的查看、管理权限")) ;
			sStatement.addRole(ARole.ofDefaultGlobalRespace(sRoleName_BGGuest , "具有查看后台部分页面的权限")) ;
			sStatement.addRole(ARole.ofDefaultGlobalRespace(sRoleName_Common , "只具有查看和修改自己信息的权限")) ;
			
			sStatement.addRole(ARole.ofRespace(sRoleName_ClientAppAdmin , "具有管理特定ClientApp的数据的权限"
					, AppConsts.sResSpaceType_ClientApp)) ;
			sStatement.addRole(ARole.ofRespace(sRoleName_ClientAppGuest , "具有管理特定ClientApp的数据的权限"
					, AppConsts.sResSpaceType_ClientApp)) ;
			
			// 权限
			sStatement.addAuthority(AAuthority.ofDefaultGlobalRespace(sAC_View_HomePage , "查看首页" , "后台管理")) ;
			sStatement.addAuthority(AAuthority.ofDefaultGlobalRespace(sAC_View_GlobalStsData , "查看全局统计数据" , "后台管理")) ;
			
			sStatement.addAuthority(AAuthority.ofDefaultGlobalRespace(sAC_CDU_ClientAppData , "增删改应用" , "应用管理"));
			sStatement.addAuthority(AAuthority.ofDefaultGlobalRespace(sAC_Reset_AllClientAppSecret , "重置任一应用的AppSecret，隐含了查看权限" , "应用管理"));
			sStatement.addAuthority(AAuthority.ofDefaultGlobalRespace(sAC_View_AllClientAppData , "查看所有应用详细信息" , "应用管理")) ;
			sStatement.addAuthority(AAuthority.ofDefaultGlobalRespace(sAC_View_AllClientAppSecret , "查看所有应用的AppSecret" , "应用管理"));
			sStatement.addAuthority(AAuthority.of(sACP_Reset_Special_ClientAppSecret , "重置特定应用的AppSecret" , "应用管理" , AppConsts.sResSpaceType_ClientApp)) ;
			sStatement.addAuthority(AAuthority.of(sACP_Manage_Special_CanVisitUser, "管理特定应用的可访问用户" , "应用管理" , AppConsts.sResSpaceType_ClientApp)) ;
			sStatement.addAuthority(AAuthority.of(sACP_Update_Special_ClientAppData , "更新特定应用的数据" , "应用管理" , AppConsts.sResSpaceType_ClientApp)) ;
			sStatement.addAuthority(AAuthority.of(sACP_View_Special_ClientAppData , "查看特定应用的数据" , "应用管理" , AppConsts.sResSpaceType_ClientApp)) ;
			sStatement.addAuthority(AAuthority.of(sACP_View_Special_ClientAppSecret , "查看特定应用的AppSecret" , "应用管理" , AppConsts.sResSpaceType_ClientApp)) ;
			
			
			sStatement.addAuthority(AAuthority.ofDefaultGlobalRespace(sAC_View_AllUsers , "查看所有用户" , "用户管理")) ;
			sStatement.addAuthority(AAuthority.ofDefaultGlobalRespace(sAC_CDU_UserData , "增删改用户数据" ,"用户管理")) ;
			sStatement.addAuthority(AAuthority.ofDefaultGlobalRespace(sAC_Reset_PasswordOfOtherUser , "修改其它用户的密码" , "用户管理")) ;
			
			sStatement.addAuthority(AAuthority.ofDefaultGlobalRespace(sAC_CDU_OrgUnit , "增删改组织单元" , "组织管理")) ;
			sStatement.addAuthority(AAuthority.ofDefaultGlobalRespace(sAC_View_OrgUnitAndUsers , "查看组织单元及下面用户" , "组织管理")) ;
			
			sStatement.addAuthority(AAuthority.ofDefaultGlobalRespace(sAC_View_Apis , "查看API" , "API管理")) ;
			
			// 角色-权限关联
			// 后台管理员
			sStatement.addRelation(sRoleName_Admin, sAC_View_HomePage) ;
			sStatement.addRelation(sRoleName_Admin, sAC_View_GlobalStsData) ;
			sStatement.addRelation(sRoleName_Admin, sAC_View_HomePage) ;
			sStatement.addRelation(sRoleName_Admin, sAC_CDU_ClientAppData) ;
			sStatement.addRelation(sRoleName_Admin, sAC_Reset_AllClientAppSecret) ;
			sStatement.addRelation(sRoleName_Admin, sAC_CDU_UserData) ;
			sStatement.addRelation(sRoleName_Admin, sAC_Reset_PasswordOfOtherUser) ;
			sStatement.addRelation(sRoleName_Admin, sAC_CDU_OrgUnit) ;
			sStatement.addRelation(sRoleName_Admin, sAC_View_Apis) ;
			
			// 后台游客，和后台管理员相比，能看但改不了
			sStatement.addRelation(sRoleName_BGGuest , sAC_View_HomePage) ;
			sStatement.addRelation(sRoleName_BGGuest, sAC_View_GlobalStsData) ;
			sStatement.addRelation(sRoleName_BGGuest , sAC_View_AllClientAppData) ;
			sStatement.addRelation(sRoleName_BGGuest , sAC_View_AllUsers) ;
			sStatement.addRelation(sRoleName_BGGuest , sAC_View_OrgUnitAndUsers) ;
			sStatement.addRelation(sRoleName_BGGuest , sAC_View_Apis) ;
			
			// ClientApp负责人 ，
			sStatement.addRelation(sRoleName_ClientAppAdmin , sAC_View_Apis) ;
			sStatement.addRelation(sRoleName_ClientAppAdmin , sACP_Manage_Special_CanVisitUser) ;
			sStatement.addRelation(sRoleName_ClientAppAdmin , sACP_Update_Special_ClientAppData) ;
			sStatement.addRelation(sRoleName_ClientAppAdmin , sACP_Reset_Special_ClientAppSecret) ;
			
			// ClientApp游客
			sStatement.addRelation(sRoleName_ClientAppGuest , sAC_View_Apis) ;
			sStatement.addRelation(sRoleName_ClientAppGuest , sACP_View_Special_ClientAppData) ;
			sStatement.addRelation(sRoleName_ClientAppGuest , sACP_View_Special_ClientAppData) ;
			
		}
		return sStatement ;
	}
}
