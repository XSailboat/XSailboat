package team.sailboat.base.sql;

import com.alibaba.druid.DbType;

public class TDengineSQLBloodEngine extends SqlBloodEngine
{

	public TDengineSQLBloodEngine(String aDefaultDBName)
	{
		super(DbType.other , aDefaultDBName);
	}

	public static void main(String[] aArgs)
	{
		String sql = "select\n"
				+ "	count(val) val\n"
				+ "from\n"
				+ "	(\n"
				+ "	select\n"
				+ "		pid,\n"
				+ "		last(`state`) val\n"
				+ "	from\n"
				+ "		walker_mav_log\n"
				+ "	where\n"
				+ "		siteid = ${siteId}\n"
				+ "	group by\n"
				+ "		pid \n"
				+ "	)t1" ;
		new TDengineSQLBloodEngine("ia_biz").parse(sql) ;
	}

}
