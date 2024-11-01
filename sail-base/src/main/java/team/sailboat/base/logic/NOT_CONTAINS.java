package team.sailboat.base.logic;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name="NOT_CONTAINS" , description="不包含")
public class NOT_CONTAINS extends OneValueCnd
{

	public NOT_CONTAINS()
	{
		super(Operator.NOT_CONTAINS);
	}

}
