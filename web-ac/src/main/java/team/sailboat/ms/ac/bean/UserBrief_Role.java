package team.sailboat.ms.ac.bean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import team.sailboat.commons.ms.bean.UserBrief;

/**
 * 
 * 用户的简要信息、角色
 *
 * @author yyl
 * @since 2024年11月6日
 */
@Schema(description = "用户的简要信息、角色")
@Data
@EqualsAndHashCode(callSuper = true)
public class UserBrief_Role extends UserBrief
{
	@Schema(description = "资源空间名称.角色名称")
	Role_ResSpace[] roleResSpaces ;
	
}
