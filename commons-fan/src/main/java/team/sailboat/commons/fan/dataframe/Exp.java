package team.sailboat.commons.fan.dataframe;

import team.sailboat.commons.fan.json.JSONArray;

public interface Exp
{
	
	Object eval(JSONArray aRow) ;
	
	String getDataType() ;

	static ScalarExp $col(int aColIndex , String aColName , String aDataType)
	{
		return new GenericColumn(aColIndex , aColName , aDataType) ;
	}
	
	static CountAggExp aggCount(String aColName)
	{
		return new CountAggExp($col(0, aColName , null)) ;
	}
	
	static SumAggExp aggSum(ScalarExp aExp)
	{
		return new SumAggExp(aExp , aExp.getName()) ;
	}
}
