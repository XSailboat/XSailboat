package team.sailboat.commons.fan.es.agg;

import team.sailboat.commons.fan.es.query.IExprNode;

public interface IAggExprNode extends IExprNode
{
	default AggsDefine root()
	{
		return (AggsDefine)IExprNode.super.root() ;
	}
}
