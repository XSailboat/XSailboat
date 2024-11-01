package team.sailboat.base.logic;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name="NOT_STARTS_WITH" , description="开头不是")
public class NOT_STARTS_WITH extends OneValueCnd
{

	public NOT_STARTS_WITH()
	{
		super(Operator.NOT_STARTS_WITH) ;
	}

}
