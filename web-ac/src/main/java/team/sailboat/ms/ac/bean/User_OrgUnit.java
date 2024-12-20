package team.sailboat.ms.ac.bean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import team.sailboat.commons.ms.bean.UserBrief;
import team.sailboat.ms.ac.dbean.R_OrgUnit_User;
import team.sailboat.ms.ac.dbean.User;

/**
 * 
 * 用户在组织单元中的信息
 *
 * @author yyl
 * @since 2024年11月6日
 */
@Schema(description = "用户的简要信息、角色")
@Data
@EqualsAndHashCode(callSuper = true)
public class User_OrgUnit extends UserBrief
{
	@Schema(description = "性别。可取值：男、女")
	String sex ;
	
	@Schema(description = "用户在组织单元中的职位")
	String job ;
	
	public static User_OrgUnit of(User aUser , R_OrgUnit_User aR)
	{
		User_OrgUnit u = new User_OrgUnit() ;
		u.setId(aUser.getId()) ;
		u.setRealName(aUser.getRealName()) ;
		u.setDepartment(aUser.getDepartment()) ;
		u.setSex(aUser.getSex()) ;
		u.setJob(aR.getJob()) ;
		return u ;
	}
}
