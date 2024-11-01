package team.sailboat.base.logic;

import io.swagger.v3.oas.annotations.media.Schema;

public abstract class OneValueCnd extends Condition
{
	
	Object mValue ;

	public OneValueCnd(Operator aOperator)
	{
		super(aOperator);
	}

	@Schema(description = "值")
	public Object getValue()
	{
		return mValue;
	}
	public void setValue(Object aValue)
	{
		mValue = aValue;
	}
}
