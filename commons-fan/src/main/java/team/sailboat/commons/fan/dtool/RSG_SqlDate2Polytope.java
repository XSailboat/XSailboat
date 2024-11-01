package team.sailboat.commons.fan.dtool;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import team.sailboat.commons.fan.json.Polytope;

public class RSG_SqlDate2Polytope extends AResultSetGetter
{
	SimpleDateFormat mSDF ;
	long mOffsetMs = 0 ;
	
	RSG_SqlDate2Polytope(int aIndex , SimpleDateFormat aSDF , Long aOffsetMs)
	{
		super(aIndex);
		mSDF = aSDF ;
		mOffsetMs = aOffsetMs ;
	}

	public RSG_SqlDate2Polytope(int aIndex , String aDateFmt , Long aOffsetMs)
	{
		super(aIndex);
		mSDF = new SimpleDateFormat(aDateFmt) ;
		if(aOffsetMs != null)
			mOffsetMs = aOffsetMs ;
	}
	
	public RSG_SqlDate2Polytope(int aIndex , String aDateFmt)
	{
		this(aIndex, aDateFmt, null) ;
	}

	@Override
	public Object getResult(ResultSet aRS) throws SQLException
	{
		Date date = aRS.getTimestamp(mIndex) ;
		if(date != null && mOffsetMs != 0)
			date.setTime(date.getTime() + mOffsetMs);
		return date == null?null:Polytope.of(date , mSDF.format(date)) ;
	}
	
	@Override
	public RSG_SqlDate2Polytope clone()
	{
		return new RSG_SqlDate2Polytope(mIndex, mSDF , mOffsetMs) ;
	}
}
