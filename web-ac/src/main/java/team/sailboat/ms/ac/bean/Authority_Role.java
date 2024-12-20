package team.sailboat.ms.ac.bean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.time.XTime;
import team.sailboat.ms.ac.dbean.Authority;
import team.sailboat.ms.ac.dbean.Role;

/**
 * 
 * 权限信息，包含了角色名和角色id
 *
 * @author yyl
 * @since 2024年11月6日
 */
@Data
public class Authority_Role
{
	@Schema(description = "权限id")
	String id ;
	
	@Schema(description = "权限码")
	String code ;
	
	@Schema(description = "权限分组")
	String groupName ;
	
	@Schema(description = "权限描述")
	String description;
	
	@Schema(description = "创建时间")
	String createTime;
	
	@Schema(description = "角色id")
	String roleId ;
	
	@Schema(description = "角色名称")
	String roleName ;
	
	@Schema(description = "资源空间类型")
	String resSpaceType ;

	public static Authority_Role of(Authority aAuth , Role aRole)
	{
		Authority_Role a = new Authority_Role() ;
		a.id = aAuth.getId() ;
		a.code = aAuth.getCode();
		a.groupName = aAuth.getGroupName() ;
		a.description = JCommon.defaultIfEmpty(aAuth.getCustomDescription() , aAuth.getDescription()) ;
		a.createTime = XTime.format$yyyyMMddHHmmss(aAuth.getCreateTime()) ;
		a.roleId = aRole.getId() ;
		a.roleName = aRole.getName() ;
		a.resSpaceType = aRole.getResSpaceType() ;
		return a ;
	}
}
