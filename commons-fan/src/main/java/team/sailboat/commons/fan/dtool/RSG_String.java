package team.sailboat.commons.fan.dtool;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RSG_String extends AResultSetGetter
{
	public RSG_String(int aIndex)
	{
		super(aIndex);
	}

	@Override
	public Object getResult(ResultSet aRS) throws SQLException
	{
		return aRS.getString(mIndex) ;
	}
	
	@Override
	public RSG_String clone()
	{
		return new RSG_String(mIndex) ;
	}
}
