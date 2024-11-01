package team.sailboat.commons.fan.dtool.taos;

import team.sailboat.commons.fan.dtool.UpdateOrInsertKit;

public class TDengineInsertKit extends UpdateOrInsertKit
{

	String mFirstSqlOfTransaction ;
	
	int mTableNameIndex = -1 ;
	int[] mTagIndexes = null ;
	
	public TDengineInsertKit(String aSql, int[] aColTypes)
	{
		super(aSql, aColTypes);
	}

	public TDengineInsertKit(String aSql, String... aColTypes)
	{
		super(aSql, aColTypes);
	}
	
	void setFirstSqlOfTransaction(String aSql)
	{
		mFirstSqlOfTransaction = aSql ;
	}
	
	public void setTableNameAndTagsColIndexes(int aTableNameIndex , int[] aTagIndexes)
	{
		mTableNameIndex = aTableNameIndex ;
		mTagIndexes = aTagIndexes ;
	}
	
}
