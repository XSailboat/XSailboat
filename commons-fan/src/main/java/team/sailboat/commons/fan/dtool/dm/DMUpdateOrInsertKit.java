package team.sailboat.commons.fan.dtool.dm;

import team.sailboat.commons.fan.dtool.UpdateOrInsertKit;

public class DMUpdateOrInsertKit extends UpdateOrInsertKit
{

	String mFirstSqlOfTransaction ;
	
	public DMUpdateOrInsertKit(String aSql, int[] aColTypes)
	{
		super(aSql, aColTypes);
	}

	public DMUpdateOrInsertKit(String aSql, String... aColTypes)
	{
		super(aSql, aColTypes);
	}
	
	void setFirstSqlOfTransaction(String aSql)
	{
		mFirstSqlOfTransaction = aSql ;
	}
	
}
