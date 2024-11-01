package team.sailboat.commons.fan.dtool;

import java.util.ArrayList;
import java.util.List;

public class SqlParams
{
	
	String mSql ;

	final List<SqlParam> mParams = new ArrayList<SqlParam>() ;

	public SqlParams()
	{
		
	}
	
	public String getSql()
	{
		return mSql;
	}
	
	public void setSql(String aSql)
	{
		mSql = aSql;
	}
	
	public void addParam(Object aValue , int aDataType)
	{
		mParams.add(new SqlParam(aValue , aDataType)) ;
	}
	
	public List<SqlParam> getParams()
	{
		return mParams;
	}
	
	public Object[] getParamValues()
	{
		
		Object[] array = new Object[mParams.size()] ;
		int i=0 ;
		for(SqlParam param : mParams)
		{
			array[i++] = param.getValue() ;
		}
		return array ;
	}

}
