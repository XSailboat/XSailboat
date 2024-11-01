package team.sailboat.commons.fan.es.query;

public class BaseExprNode implements IExprNode
{
	IExprNode mUpper ;
	
	protected BaseExprNode(IExprNode aUpper)
	{
		mUpper = aUpper ;
	}

	@Override
	public IExprNode up()
	{
		return mUpper ;
	}

}
