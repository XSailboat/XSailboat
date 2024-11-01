package team.sailboat.base.logic;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name="IN" , description="属于" )
public class IN extends CollectionCnd
{

	public IN()
	{
		super(Operator.IN) ;
	}
	
}
