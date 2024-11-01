package team.sailboat.base.logic;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name="NOT_IN" , description="不属于")
public class NOT_IN extends CollectionCnd
{

	public NOT_IN()
	{
		super(Operator.NOT_IN) ;
	}
}
