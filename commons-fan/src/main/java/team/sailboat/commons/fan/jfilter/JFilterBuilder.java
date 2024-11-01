package team.sailboat.commons.fan.jfilter;

import team.sailboat.commons.fan.json.JSONObject;

public interface JFilterBuilder extends JFilterNodeBuilder
{
	
	JSONObject build() ;
	
//	static interface Node
//	{
//		JFilterBuilder back_root() ;
//	}
	
//	static interface Term extends Node
//	{
//		Must back_must() ;
//		Should back_should() ;
//		MustNot back_must_not() ;
//	}
//	
//	static interface Regex extends Node
//	{
//		Must back_must() ;
//		Should back_should() ;
//		MustNot back_must_not() ;
//	}
//	
//	static interface Range extends Node
//	{
//		Must back_must() ;
//		Should back_should() ;
//		MustNot back_must_not() ;
//	}
	
	
}
