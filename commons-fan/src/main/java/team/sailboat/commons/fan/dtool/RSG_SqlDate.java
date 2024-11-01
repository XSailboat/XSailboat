package team.sailboat.commons.fan.dtool;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class RSG_SqlDate extends AResultSetGetter
{
	static SimpleDateFormat sSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS") ;

	public RSG_SqlDate(int aIndex)
	{
		super(aIndex);
	}

	@Override
	public Object getResult(ResultSet aRS) throws SQLException
	{
		Date date = aRS.getDate(mIndex) ;
		return date == null?null:sSDF.format(date) ;
	}
	
	public RSG_SqlDate clone()
	{
		return new RSG_SqlDate(mIndex) ;
	}
}
