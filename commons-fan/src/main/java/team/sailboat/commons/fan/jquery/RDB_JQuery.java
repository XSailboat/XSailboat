package team.sailboat.commons.fan.jquery;

import javax.sql.DataSource;

public class RDB_JQuery
{
	DataSource mDataSource ;
	
	public RDB_JQuery(DataSource aDataSource)
	{
		mDataSource = aDataSource ;
	}
	
	public DataSource getDataSource()
	{
		return mDataSource ;
	}
	
	@Deprecated
	public JQueryJo one(String aBaseSql , Object...aArgs)
	{
		return new JQuery_JSONObjectRow(mDataSource, aBaseSql , aArgs) ;
	}
	
	/**
	 * 返回的数据库查询结果集的每一行是一个JSONArray
	 * @param aBaseSql
	 * @param aArgs
	 * @return
	 */
	public JQueryJa oneJa(String aBaseSql , Object...aArgs)
	{
		return new JQuery_JSONArrayRow(mDataSource, aBaseSql , aArgs) ;
	}
	
	/**
	 * 返回的数据库查询结果集的每一行是一个JSONObject
	 * @param aBaseSql
	 * @param aArgs
	 * @return
	 */
	public JQueryJo oneJo(String aBaseSql , Object...aArgs)
	{
		return new JQuery_JSONObjectRow(mDataSource, aBaseSql , aArgs) ;
	}
}
