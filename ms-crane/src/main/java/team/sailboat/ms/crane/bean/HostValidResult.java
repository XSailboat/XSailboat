package team.sailboat.ms.crane.bean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * 主机信息验证结果
 *
 * @author yyl
 * @since 2024年10月30日
 */
@Schema(description = "主机信息验证结果")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HostValidResult
{
	@Schema(description = "测试结果是否没问题")
	boolean ok ;
	
	@Schema(description = "测试结果描述")
	String msg ;
}
