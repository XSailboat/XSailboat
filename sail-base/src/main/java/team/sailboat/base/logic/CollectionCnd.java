package team.sailboat.base.logic;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public abstract class CollectionCnd extends Condition
{
	
	List<Object> mCollection ;

	public CollectionCnd(Operator aOperator)
	{
		super(aOperator);
	}
	
	@Schema(description = "集合")
	public List<Object> getCollection()
	{
		return mCollection;
	}
	public void setCollection(List<Object> aCollection)
	{
		mCollection = aCollection;
	}

}
