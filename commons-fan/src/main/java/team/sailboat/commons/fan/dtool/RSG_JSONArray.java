package team.sailboat.commons.fan.dtool;

import java.sql.ResultSet;
import java.sql.SQLException;

import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.text.XString;

public class RSG_JSONArray extends AResultSetGetter
{
	public RSG_JSONArray(int aIndex)
	{
		super(aIndex);
	}

	@Override
	public Object getResult(ResultSet aRS) throws SQLException
	{
		String val = aRS.getString(mIndex) ;
		return XString.isNotEmpty(val)?new JSONArray(val):null ;
	}
	
	@Override
	public RSG_JSONArray clone()
	{
		return new RSG_JSONArray(mIndex) ;
	}
}
