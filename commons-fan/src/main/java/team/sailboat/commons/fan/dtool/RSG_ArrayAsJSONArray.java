package team.sailboat.commons.fan.dtool;

import java.sql.ResultSet;
import java.sql.SQLException;

import team.sailboat.commons.fan.json.JSONArray;

public class RSG_ArrayAsJSONArray extends AResultSetGetter
{
	public RSG_ArrayAsJSONArray(int aIndex)
	{
		super(aIndex);
	}

	@Override
	public Object getResult(ResultSet aRS) throws SQLException
	{
		return new JSONArray(aRS.getArray(mIndex)) ;
	}
	
	@Override
	public RSG_ArrayAsJSONArray clone()
	{
		return new RSG_ArrayAsJSONArray(mIndex) ;
	}
}
