package team.sailboat.base.aviator;

import java.util.Map;
import java.util.function.Function;

import team.sailboat.commons.fan.dataframe.Exp;
import team.sailboat.commons.fan.dataframe.ScalarExp;
import team.sailboat.commons.fan.jfilter.AviatorExpression;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.XString;

/**
 * 
 *
 * @author yyl
 * @since 2024年12月19日
 */
public class Expression
{
	String mText ;
	
	String[] mColumnNames ;
	
	boolean mColumnValue ;
	
	Map<String, Object> mParamMap ;
	
	public Expression(String aText)
	{
		Assert.notEmpty(aText , "表达式不能为空！") ;
		mText = aText ;
		mColumnNames = XString.extractParamNames(mText).toArray(JCommon.sEmptyStringArray) ;
		mColumnValue = mColumnNames.length == 1 && XString.deflate(aText).equals("${"+mColumnNames[0]+"}") ;
	}
	
	public Expression(AviatorExpression aExpr)
	{
		this(aExpr.getValue()) ;
		mParamMap = aExpr.getParamMap() ;
	}
	
	public String[] getColumnNames()
	{
		return mColumnNames;
	}
	
	public boolean isColumnValue()
	{
		return mColumnValue;
	}
	
	public String getText()
	{
		return mText;
	}
	
	public Map<String, Object> getParamMap()
	{
		return mParamMap;
	}
	
	public static ScalarExp toExp(Expression aExpr , String aName , Function<String, Integer> aColIndexPvd)
	{
		if(aExpr.isColumnValue())
		{
			String colName = aExpr.getColumnNames()[0] ;
			return Exp.$col(aColIndexPvd.apply(colName) , aName , null) ;
		}
		else
		{
			return new AviatorExp(aExpr, aName, null ,aColIndexPvd) ;
		}
	}
}
