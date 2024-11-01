package team.sailboat.base.aviator;

import java.util.Map;
import java.util.function.Function;

import com.googlecode.aviator.AviatorEvaluator;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dataframe.ScalarExp;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.text.XString;

public class AviatorExp extends ScalarExp
{
	String mText ;
	
	com.googlecode.aviator.Expression mExpr ;
	
	int[] mArgColIndexes ;
	
	String[] mParamNames ;
	
	final Map<String, Object> mArgMap = XC.hashMap() ;
	
	public AviatorExp(Expression aBExpr , String name , String aDataType , Function<String, Integer> aColIndexPvd)
	{
		super(name , aDataType) ;
		mText = aBExpr.getText() ;
		String[] paramNames = aBExpr.getColumnNames() ;
		Map<String, String> paramValMap = XC.hashMap() ;
		mParamNames = new String[paramNames.length] ;
		mArgColIndexes = new int[paramNames.length] ;
		int i=0 ;
		for(String paramName : paramNames)
		{
			String newParamName = "$_"+paramName ;
			paramValMap.put(paramName , newParamName) ;
			mParamNames[i] = newParamName ;
			mArgColIndexes[i] = aColIndexPvd.apply(paramName) ;
			i++ ;
		}
		mExpr = AviatorEvaluator.compile(XString.format(aBExpr.getText(), paramValMap)) ;
		Map<String, Object> map = aBExpr.getParamMap() ;
		if(map != null)
			mArgMap.putAll(map) ;
	}
	
	@Override
	public Object eval(JSONArray aRowJa)
	{
		for(int i=0 ; i<mArgColIndexes.length ; i++)
		{
			mArgMap.put(mParamNames[i], aRowJa.opt(mArgColIndexes[i])) ;
		}
		return XClassUtil.typeAdapt(mExpr.execute(mArgMap) , getDataType()) ;
	}

	@Override
	public String toString()
	{
		return mText ;
	}
}
