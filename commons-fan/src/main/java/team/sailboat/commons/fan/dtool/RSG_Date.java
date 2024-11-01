package team.sailboat.commons.fan.dtool;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RSG_Date extends AResultSetGetter
{

	public RSG_Date(int aIndex)
	{
		super(aIndex);
	}

	@Override
	public Object getResult(ResultSet aRS) throws SQLException
	{
		Date date = aRS.getDate(mIndex) ;
		return date == null?null : new Date(date.getTime()) ;
	}
	
	@Override
	public RSG_Date clone()
	{
		return new RSG_Date(mIndex) ;
	}
}
