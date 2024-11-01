package team.sailboat.base.logic;

import io.swagger.v3.oas.annotations.media.Schema;
import team.sailboat.commons.fan.lang.JCommon;

@Schema(name = "IN_RANGE" , description="范围")
public class IN_RANGE extends Condition
{
	
	Boolean mMaxEquals ;
	Double mMax ;
	Boolean mMinEquals ;
	Double mMin ;

	public IN_RANGE()
	{
		super(Operator.IN_RANGE) ;
	}
	
	@Schema(description = "最大值是否包括相等")
	public boolean isMaxEquals()
	{
		return JCommon.defaultIfNull(mMaxEquals , Boolean.FALSE) ;
	}
	public void setMaxEquals(Boolean aMaxEquals)
	{
		mMaxEquals = aMaxEquals;
	}
	
	@Schema(description = "最大值")
	public Double getMax()
	{
		return mMax;
	}
	public void setMax(Double aMax)
	{
		mMax = aMax;
	}
	
	@Schema(description = "最小值是否包括相等")
	public boolean isMinEquals()
	{
		return JCommon.defaultIfNull(mMinEquals , Boolean.FALSE) ;
	}
	public void setMinEquals(Boolean aMinEquals)
	{
		mMinEquals = aMinEquals;
	}
	
	@Schema(description = "最小值")
	public Double getMin()
	{
		return mMin;
	}
	public void setMin(Double aMin)
	{
		mMin = aMin;
	}
}
