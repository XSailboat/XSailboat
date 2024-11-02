package team.sailboat.bd.base.kafka;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import team.sailboat.commons.fan.collection.XC;

@Data
public class TopicExtraConfig
{
	@Schema(description = "日志保留时间(单位毫秒)。配置键：retention.ms")
	Long retentionMs ;
	
	@Schema(description = "过期清理方式" , allowableValues = {"compact","delete"})
	String cleanupPolicy ;
	
	@Schema(hidden = true)
	@JsonIgnore
	public Map<String, String> getConfigMap()
	{
		Map<String, String> map = XC.hashMap() ;
		if(retentionMs != null && retentionMs > 0)
			map.put("retention.ms", retentionMs.toString()) ;
		if(cleanupPolicy != null)
		{
			String policy = cleanupPolicy.toLowerCase() ; 
			switch(policy)
			{
			case "compact":
			case "delete":
				map.put("cleanup.policy" , policy) ;
				break ;
			default:
				break ;
			}
		}
		return map ;
	}
}
