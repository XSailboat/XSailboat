package team.sailboat.base.logic;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "EQUALS" , description="相等")
public class EQUALS extends OneValueCnd
{

	public EQUALS()
	{
		super(Operator.EQUALS) ;
	}

}
