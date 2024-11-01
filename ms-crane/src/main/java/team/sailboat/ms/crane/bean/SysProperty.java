package team.sailboat.ms.crane.bean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "系统参数配置项")
public class SysProperty
{
	@Schema(description = "参数名")
	String name ;
	
	@Schema(description = "参数描述")
	String value ;
	
	@Schema(description = "参数描述")
	String description ;
}
