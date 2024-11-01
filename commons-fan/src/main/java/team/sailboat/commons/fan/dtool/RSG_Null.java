package team.sailboat.commons.fan.dtool;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RSG_Null extends AResultSetGetter
{
	public RSG_Null()
	{
		super(-1);
	}

	@Override
	public Object getResult(ResultSet aRS) throws SQLException
	{
		return null ;
	}
	
	public RSG_Null clone()
	{
		return new RSG_Null() ;
	}
}
