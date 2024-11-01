package team.sailboat.commons.fan.dtool;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import team.sailboat.commons.fan.infc.EFunction;
import team.sailboat.commons.fan.json.JSONArray;

public class RS2JSONArray implements EFunction<ResultSet, JSONArray , SQLException>
{
	IResultSetGetter[] mGetters ;
	
	public RS2JSONArray(ResultSetMetaData aRSMD) throws SQLException
	{
		this(aRSMD, null, null, null) ;
	}
	public RS2JSONArray(ResultSetMetaData aRSMD , boolean[] aIncludes) throws SQLException
	{
		this(aRSMD, aIncludes, null, null) ;
	}
	
	public RS2JSONArray(ResultSetMetaData aRSMD , boolean[] aIncludes
			, Collection<String> aJoCols 
			, Collection<String> aJaCols) throws SQLException
	{
		int colCount = aRSMD.getColumnCount() ;
		List<IResultSetGetter> getters = new ArrayList<>() ;
		for(int i=0 ; i<colCount ; i++)
		{
			if(aIncludes != null && (aIncludes.length<=i || !aIncludes[i]))
				continue ;
			String colName = aRSMD.getColumnLabel(i+1) ;
			switch(aRSMD.getColumnType(i+1))
			{
			case Types.VARCHAR:
			case Types.CHAR:
			case Types.NCHAR:
			case Types.LONGVARCHAR:
				if(aJoCols != null && aJoCols.contains(colName))
					getters.add(new RSG_JSONObject(i+1)) ;
				else if(aJaCols != null && aJaCols.contains(colName))
					getters.add(new RSG_JSONArray(i+1)) ;
				else
					getters.add(new RSG_String(i+1)) ;
				break ;
			case Types.BIGINT:
				getters.add(new DefaultRSG(i+1 , Long.class)) ;
				break ;
			case Types.INTEGER:
			case Types.SMALLINT:
			case Types.TINYINT:
				getters.add(new DefaultRSG(i+1 , Integer.class)) ;
				break ;
			case Types.DOUBLE:
			case Types.FLOAT:
			case Types.DECIMAL:
			case Types.REAL:
			case Types.NUMERIC:
				getters.add(new DefaultRSG(i+1 , Double.class)) ;
				break ;
			case Types.DATE:
			case Types.TIME:
			case Types.TIMESTAMP:
				getters.add(new RSG_SqlDate2Polytope(i+1 , "yyyy-MM-dd HH:mm:ss.SSS")) ;
				break ;
			case Types.BINARY:
				if(aRSMD.getClass().getName().contains("com.taosdata."))
					getters.add(new RSG_String(i+1)) ;
				else
					getters.add(new RSG_Cover(i+1, "[字节数组，不支持显示]")) ;
				break ;
			case Types.LONGVARBINARY :
			case Types.BLOB:
				getters.add(new RSG_Cover(i+1, "[字节数组，不支持显示]")) ;
				break ;
			case Types.BOOLEAN:
			case Types.BIT:
				getters.add(new DefaultRSG(i+1, Boolean.class)) ;
				break ;
			default:
				throw new IllegalStateException("不支持SQL类型："+aRSMD.getColumnTypeName(i+1)+"["+aRSMD.getColumnType(i+1)+"]"+" 写入JSON对象") ;
			}
		}
		mGetters = getters.toArray(new IResultSetGetter[0]) ;
	}

	@Override
	public JSONArray apply(ResultSet aValue) throws SQLException
	{
		JSONArray ja = new JSONArray() ;
		for(int i=0 ; i<mGetters.length ; i++)
		{
			ja.put(mGetters[i].getResult(aValue)) ;
		}
		return ja ;
	}
}
