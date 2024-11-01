package team.sailboat.base.logic;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name="STARTS_WITH" , description="开头是")
public class STARTS_WITH extends OneValueCnd
{

	public STARTS_WITH()
	{
		super(Operator.STARTS_WITH) ;
	}

}
