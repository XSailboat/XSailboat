package team.sailboat.base.logic;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name="NOT_EQUALS" , description="不相等")
public class NOT_EQUALS extends OneValueCnd
{

	public NOT_EQUALS()
	{
		super(Operator.NOT_EQUALS) ;
	}

}
