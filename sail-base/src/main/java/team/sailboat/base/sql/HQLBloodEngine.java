package team.sailboat.base.sql;

import com.alibaba.druid.DbType;

public class HQLBloodEngine extends SqlBloodEngine
{

	public HQLBloodEngine(String aDefaultDBName)
	{
		super(DbType.hive , aDefaultDBName);
	}

	

}
