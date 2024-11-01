package team.sailboat.base.def;

import team.sailboat.commons.fan.text.XString;

public enum ParamType
{
	Query ,
	Head ,
	PathVariable
	;
	
	public static ParamType fuzzyMatch(String aName)
	{
		if(XString.isEmpty(aName))
			return null ;
		String lcName = aName.toLowerCase() ;
		if("query".equals(lcName)
				|| "form".equals(lcName))
			return Query ;
		if("head".equals(lcName)
				|| "header".equals(lcName))
			return Head ;
		if("path".equals(lcName) || "pathvariable".equals(lcName))
			return PathVariable ;
		
		throw new IllegalArgumentException("不能转成ParamType："+aName) ;
	}
}
