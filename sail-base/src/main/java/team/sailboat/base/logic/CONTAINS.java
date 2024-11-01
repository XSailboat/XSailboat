package team.sailboat.base.logic;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CONTAINS" , description="包含")
public class CONTAINS extends OneValueCnd
{

	public CONTAINS()
	{
		super(Operator.CONTAINS);
	}

}
