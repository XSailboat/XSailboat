package team.sailboat.ms.ac.bean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 
 * 角色和资源空间的联合信息（简要）
 *
 * @author yyl
 * @since 2024年12月17日
 */
@Data
@Builder
@Schema(description = "角色和资源空间的联合信息")
public class Role_ResSpace
{
	@Schema(description = "资源空间id")
	String resSpaceId;
	
	@Schema(description = "角色全名。资源空间名.角色名")
	String roleFullName ;
	
	@Schema(description = "资源空间类型")
	String resSpaceType ;
	
	@Schema(description = "角色id")
	String roleId ;
}
