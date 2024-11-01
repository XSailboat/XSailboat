package team.sailboat.base.logic;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name="NOT_EMPTY" , description="非空")
public class NOT_EMPTY extends Condition
{

	public NOT_EMPTY()
	{
		super(Operator.NOT_EMPTY) ;
	}
	
}
