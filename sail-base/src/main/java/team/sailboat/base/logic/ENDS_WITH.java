package team.sailboat.base.logic;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ENDS_WITH" , description="结尾是")
public class ENDS_WITH extends OneValueCnd
{

	public ENDS_WITH()
	{
		super(Operator.ENDS_WITH) ;
	}

}
