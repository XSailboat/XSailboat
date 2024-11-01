package team.sailboat.commons.fan.dtool;

import java.sql.ResultSet;
import java.sql.SQLException;

import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.text.XString;

public class RSG_JSONObject extends AResultSetGetter
{
	public RSG_JSONObject(int aIndex)
	{
		super(aIndex);
	}

	@Override
	public Object getResult(ResultSet aRS) throws SQLException
	{
		String val = aRS.getString(mIndex) ;
		return XString.isNotEmpty(val)?new JSONObject(val):null ;
	}
	
	public RSG_JSONObject clone()
	{
		return new RSG_JSONObject(mIndex) ;
	}
}
