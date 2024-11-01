package team.sailboat.commons.fan.dtool;

import java.sql.ResultSet;
import java.sql.SQLException;

import team.sailboat.commons.fan.time.XTime;

public class RSG_SqlDate2String extends AResultSetGetter
{

	public RSG_SqlDate2String(int aIndex)
	{
		super(aIndex);
	}

	@Override
	public Object getResult(ResultSet aRS) throws SQLException
	{
		return XTime.format$yyyyMMddHHmmssSSS(aRS.getTimestamp(mIndex) , null) ;
	}
	
	@Override
	public RSG_SqlDate2String clone()
	{
		return new RSG_SqlDate2String(mIndex) ;
	}
}
