package team.sailboat.base.logic;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name="NOT_ENDS_WITH" , description="结尾不是")
public class NOT_ENDS_WITH extends OneValueCnd
{

	public NOT_ENDS_WITH()
	{
		super(Operator.NOT_ENDS_WITH) ;
	}

}
