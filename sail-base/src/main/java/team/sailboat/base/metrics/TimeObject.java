package team.sailboat.base.metrics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class TimeObject implements Cloneable
{
	protected long time ;
	
	public abstract Object getValue() ;
}
