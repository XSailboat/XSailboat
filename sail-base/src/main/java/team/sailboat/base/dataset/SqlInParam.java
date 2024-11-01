package team.sailboat.base.dataset;

import lombok.Data;

@Data
public class SqlInParam
{
	String wholeExpr ;
	
	String valueExpr ;
	
	/**
	 * true表示是sql段，false表示是参数值
	 */
	boolean sqlSeg = false ;
	
	boolean paramHolder = true;
	
	public SqlInParam(String aWholeExpr)
	{
		wholeExpr = aWholeExpr ;
		int i= wholeExpr.indexOf(':') ;
		if(i == -1)
		{
			valueExpr = wholeExpr ;
		}
		else
		{
			paramHolder = false ;
			char nextChar = wholeExpr.charAt(i+1) ;
			if(nextChar == ':')
			{
				valueExpr = wholeExpr.substring(i+2) ;
				sqlSeg = true ;
			}
			else
				valueExpr = wholeExpr.substring(i+1) ;
		}
	}
	
}
