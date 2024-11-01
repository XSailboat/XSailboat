package team.sailboat.base.metrics;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class TimeString extends TimeObject
{
	
	String value ;
	
	public TimeString(long aTime , String aValue)
	{
		super(aTime) ;
		value = aValue ;
	}
	
	@Override
	public TimeString clone()
	{
		return new TimeString(time, value) ;
	}
}