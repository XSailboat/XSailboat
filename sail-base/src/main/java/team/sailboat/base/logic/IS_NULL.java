package team.sailboat.base.logic;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name="IS_NULL" , description="为NULL")
public class IS_NULL extends Condition
{

	public IS_NULL()
	{
		super(Operator.IS_NULL) ;
	}
	
}
