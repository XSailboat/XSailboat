package team.sailboat.commons.fan.dataframe;

import team.sailboat.commons.fan.json.JSONArray;

public class GenericColumn extends ScalarExp
{
	protected int mColIndex ;
	
	public GenericColumn(int aColIndex , String name , String aDataType)
	{
		super(name , aDataType) ;
		mColIndex = aColIndex ;
	}
	
	@Override
	public Object eval(JSONArray aRowJa)
	{
		return aRowJa.opt(mColIndex) ;
	}

	@Override
	public String toString()
	{
		return mColIndex >= 0 ? "$col(" + mColIndex + ")" : mName;
	}
}
