package team.sailboat.base.logic;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name="EMPTY" , description="为空")
public class EMPTY extends Condition
{

	public EMPTY()
	{
		super(Operator.EMPTY) ;
	}
	
	

}
