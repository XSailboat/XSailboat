package team.sailboat.commons.fan.dtool;

import java.sql.ResultSet;
import java.sql.SQLException;

import team.sailboat.commons.fan.infc.EFunction;

public interface IResultSetGetter extends EFunction<ResultSet, Object, SQLException> , Cloneable
{
	Object getResult(ResultSet aRS) throws SQLException ;
	
	@Override
	default Object apply(ResultSet aValue) throws SQLException
	{
		return getResult(aValue) ;
	}
	
	IResultSetGetter clone() ;
	
	void setIndex(int aIndex) ;
}
