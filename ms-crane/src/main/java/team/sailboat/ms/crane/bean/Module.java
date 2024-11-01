package team.sailboat.ms.crane.bean;

import java.util.TreeSet;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 软件模块
 *
 * @author yyl
 * @since 2024年10月16日
 */
@Data
@NoArgsConstructor
@Schema(description = "软件模块")
public class Module
{
	@Schema(description = "模块名")
	String name ;
	
	@Schema(description = "模块描述")
	String description ;
	
	@Schema(description = "软件模块的端口")
	TreeSet<Integer> ports ;
	
	public Module(String aName , String aDescription)
	{
		name = aName ;
		description = aDescription ;
	}
}
