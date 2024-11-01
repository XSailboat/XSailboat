package team.sailboat.base.metrics;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class TimeDouble extends TimeObject
{
	
	double value ;
	
	public TimeDouble(long aTime , double aValue)
	{
		super(aTime) ;
		value = aValue ;
	}
	
	public Double getValue()
	{
		return value ;
	}
	
	public double getRawValue()
	{
		return value ;
	}
	
	@Override
	public TimeDouble clone()
	{
		return new TimeDouble(time, value) ;
	}
}