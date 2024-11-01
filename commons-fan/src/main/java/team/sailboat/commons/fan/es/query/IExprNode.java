package team.sailboat.commons.fan.es.query;

public interface IExprNode
{
	IExprNode up() ;
	
	@SuppressWarnings("unchecked")
	default <T extends IExprNode> T up(Class<T> aClass)
	{
		return aClass==null?(T)up():aClass.cast(up()) ;
	}
	
	default IExprNode root()
	{	
		IExprNode current = this ;
		IExprNode upper = up(null) ;
		while(upper != null)
		{
			current = upper ;
			upper = upper.up(null) ;
		}
		return current ;
	}
}
