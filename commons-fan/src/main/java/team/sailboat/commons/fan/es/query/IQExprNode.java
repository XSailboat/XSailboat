package team.sailboat.commons.fan.es.query;

public interface IQExprNode extends IExprNode
{
	default QueryDefine root()
	{
		if(this instanceof QueryDefine)
			return (QueryDefine)this ;
		
		IExprNode upper = up(null) ;
		while(upper != null && !(upper instanceof QueryDefine))
		{
			upper = upper.up(null) ;
		}
		return (QueryDefine)upper ;
	}

}
