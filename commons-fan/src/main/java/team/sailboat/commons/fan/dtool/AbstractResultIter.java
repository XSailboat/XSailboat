package team.sailboat.commons.fan.dtool;

import java.sql.ResultSet;
import java.sql.SQLException;

import team.sailboat.commons.fan.infc.YIter;
import team.sailboat.commons.fan.struct.Wrapper;

public abstract class AbstractResultIter<T> implements YIter<T>
{
	protected ResultSet mRS ;
	protected final Wrapper<Exception> mExcepWrapper = new Wrapper<>() ;
	
	public AbstractResultIter(ResultSet aRS)
	{
		mRS = aRS ;
	}

	@Override
	public boolean hasNext()
	{
		try
		{
			return mRS.next() ;
		}
		catch (SQLException e)
		{
			mExcepWrapper.set(e) ;
			return false ;
		}
	}

	@Override
	public abstract T next() ;

	@Override
	public void orThrow() throws Exception
	{
		mExcepWrapper.orThrow(mExcepWrapper) ;
	}
	
}
