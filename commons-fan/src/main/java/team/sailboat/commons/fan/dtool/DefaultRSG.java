package team.sailboat.commons.fan.dtool;

import java.sql.ResultSet;
import java.sql.SQLException;

import team.sailboat.commons.fan.adapter.ITypeAdapter;
import team.sailboat.commons.fan.lang.XClassUtil;

public class DefaultRSG extends AResultSetGetter
{
	ITypeAdapter<?> mCast ;
	
	DefaultRSG(int aIndex)
	{
		super(aIndex) ;
	}

	public DefaultRSG(int aIndex , Class<?> aClass)
	{
		super(aIndex);
		mCast = XClassUtil.getTypeAdapter(aClass) ;
	}

	@Override
	public Object getResult(ResultSet aRS) throws SQLException
	{
		Object obj = aRS.getObject(mIndex);
		if(obj == null)
			return null ;
		return mCast.apply(obj) ;
	}
	
	public DefaultRSG clone()
	{
		DefaultRSG clone = new DefaultRSG(mIndex) ;
		clone.mCast = mCast ;
		return clone ;
	}
}