package team.sailboat.base.metrics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class TimeInt extends TimeObject
{	
	int value ;
	
	public TimeInt(long aTime , int aValue)
	{
		super(aTime) ;
	}
	
	public Integer getValue()
	{
		return value ;
	}
	
	public int getRawValue()
	{
		return value ;
	}
	
	@Override
	public TimeInt clone()
	{
		return new TimeInt(time, value) ;
	}
}