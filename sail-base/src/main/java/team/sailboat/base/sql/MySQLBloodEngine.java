package team.sailboat.base.sql;

import com.alibaba.druid.DbType;

public class MySQLBloodEngine extends SqlBloodEngine
{

	public MySQLBloodEngine(String aDefaultDBName)
	{
		super(DbType.mysql , aDefaultDBName);
	}

	public static void main(String[] aArgs)
	{
		String sql = "SELECT id, site_id, signal_tag, oname, segma2, u, create_time, rule_id, value, ts_str, segma, lower_value, upper_value\n"
				+ "FROM normal_distribution_m008\n"
				+ "WHERE ts_str = (SELECT MAX(ts_str) mt FROM normal_distribution_m008 WHERE signal_tag = ${signalTag} AND site_id = ${siteId}) \n"
				+ "limit 1" ;
		new MySQLBloodEngine("ia_biz").parse(sql) ;
	}

}
