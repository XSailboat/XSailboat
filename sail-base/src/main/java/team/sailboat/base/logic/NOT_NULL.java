package team.sailboat.base.logic;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name="NOT_NULL" , description="非NULL")
public class NOT_NULL extends Condition
{

	public NOT_NULL()
	{
		super(Operator.NOT_NULL) ;
	}
}
