package team.sailboat.commons.fan.dtool;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RSG_Cover extends AResultSetGetter
{
	String mCover ;
	
	public RSG_Cover(int aIndex , String aCover)
	{
		super(aIndex);
		mCover = aCover ;
	}

	@Override
	public Object getResult(ResultSet aRS) throws SQLException
	{
		Object obj = aRS.getObject(mIndex) ;
		return obj==null?null:mCover ;
	}
	
	@Override
	public RSG_Cover clone()
	{
		return new RSG_Cover(mIndex, mCover) ;
	}
}
