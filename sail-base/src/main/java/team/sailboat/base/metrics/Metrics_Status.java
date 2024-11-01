package team.sailboat.base.metrics;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Metrics_Status extends Metrics
{
	int value ;
	
	/**
	 * 取值字典
	 */
	String valueDict ;
}
