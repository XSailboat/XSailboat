package team.sailboat.base.sql;

import com.alibaba.druid.DbType;

import team.sailboat.commons.fan.dtool.pg.PgConst;

/**
 * Pg库的SQL血缘分析引擎
 *
 * @author yyl
 * @since 2024年10月9日
 */
public class PgBloodEngine extends SqlBloodEngine
{

	public PgBloodEngine(String aDefaultDBName)
	{
		super(DbType.postgresql , aDefaultDBName);
	}

	public static void main(String[] aArgs)
	{
		String sql = "SELECT id, site_id, signal_tag, oname, segma2, u, create_time, rule_id, value, ts_str, segma, lower_value, upper_value\n"
				+ "FROM normal_distribution_m008\n"
				+ "WHERE ts_str = (SELECT MAX(ts_str) mt FROM normal_distribution_m008 WHERE signal_tag = ${signalTag} AND site_id = ${siteId}) \n"
				+ "limit 1" ;
		new PgBloodEngine("ia_biz").parse(sql) ;
	}
	
	@Override
	protected boolean isSysParam(String aName)
	{
		return aName==null?false:PgConst.sSysParams.contains(aName.toUpperCase()) ;
	}
}
