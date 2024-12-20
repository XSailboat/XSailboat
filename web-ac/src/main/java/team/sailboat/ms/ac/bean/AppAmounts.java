package team.sailboat.ms.ac.bean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * 应用数量
 *
 * @author yyl
 * @since 2024年11月4日
 */
@Schema(description = "应用数量")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppAmounts
{
	@Schema(description = "Web应用数量")
	int webAppAmount ;
	
	@Schema(description = "中台微服务应用数量")
	int msAppAmount ;
	
	public int increaseAndGetMsAppAmount()
	{
		return ++msAppAmount ;
	}
	
	public int increaseAndGetWebAppAmount()
	{
		return ++webAppAmount ;
	}
}
