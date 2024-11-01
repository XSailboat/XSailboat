package team.sailboat.commons.fan.dtool.oracle;

import team.sailboat.commons.fan.dtool.UpdateOrInsertKit;

public class OracleUpdateOrInsertKit extends UpdateOrInsertKit
{

	String mFirstSqlOfTransaction ;
	
	public OracleUpdateOrInsertKit(String aSql, int[] aColTypes)
	{
		super(aSql, aColTypes);
	}

	public OracleUpdateOrInsertKit(String aSql, String... aColTypes)
	{
		super(aSql, aColTypes);
	}
	
	void setFirstSqlOfTransaction(String aSql)
	{
		mFirstSqlOfTransaction = aSql ;
	}
	
}
