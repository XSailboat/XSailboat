package team.sailboat.commons.fan.dtool;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.infc.EFunction;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.XString;

public class RS2JSONObject implements EFunction<ResultSet, JSONObject , SQLException>
{
	String[] mColNames ;
	Map<String , String> mColNameMap ;
	IResultSetGetter[] mGetters ;
	
	/**
	 * true表示下划线去掉，后面一个字符变大写
	 */
	boolean mHumpFomat = false ;
	
	public RS2JSONObject(ResultSetMetaData aRSMD , String...aExcludeCloumns) throws SQLException
	{
		this(aRSMD , null , null , null , aExcludeCloumns) ;
	}
	
	public RS2JSONObject(ResultSetMetaData aRSMD , Map<String , String> aColNameMap , String...aExcludeCloumns) throws SQLException
	{
		this(aRSMD, aColNameMap, null, null, aExcludeCloumns) ;
	}
	
	public RS2JSONObject(ResultSetMetaData aRSMD , boolean aHumpFomat , Map<String , String> aColNameMap , String...aExcludeCloumns) throws SQLException
	{
		this(aRSMD , aHumpFomat, aColNameMap, null, null, aExcludeCloumns) ;
	}
	
	public RS2JSONObject(ResultSetMetaData aRSMD
			, Map<String , String> aColNameMap
			, Collection<String> aJoCols 
			, Collection<String> aJaCols
			, String...aExcludeCloumns) throws SQLException
	{
		this(aRSMD, false , aColNameMap, aJoCols, aJaCols, aExcludeCloumns) ;
	}
	
	public RS2JSONObject(ResultSetMetaData aRSMD , boolean aHumpFomat
			, Map<String , String> aColNameMap
			, Collection<String> aJoCols 
			, Collection<String> aJaCols
			, String...aExcludeCloumns) throws SQLException
	{
		mHumpFomat = aHumpFomat ;
		int colCount = aRSMD.getColumnCount() ;
		List<String> colNames = new ArrayList<>() ;
		List<IResultSetGetter> getters = new ArrayList<>() ;
		for(int i=0 ; i<colCount ; i++)
		{
			String colName = aRSMD.getColumnLabel(i+1) ;
			if(aExcludeCloumns != null && aExcludeCloumns.length>0)
			{
				if(XC.containsIgnoreCase(aExcludeCloumns, colName))
					continue ;
			}
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
			case Types.NULL:
				getters.add(new RSG_Null()) ;
				break ;
			default:
				throw new IllegalStateException("不支持SQL类型："+aRSMD.getColumnTypeName(i+1)+"["+aRSMD.getColumnType(i+1)+"]"+" 写入JSON对象") ;
			}
			colNames.add(aRSMD.getColumnLabel(i+1)) ;
		}
		mColNames = colNames.toArray(JCommon.sEmptyStringArray) ;
		mGetters = getters.toArray(new IResultSetGetter[0]) ;
		mColNameMap = aColNameMap ;
		if(mHumpFomat)
		{
			for(int i=0 ; i<mColNames.length ; i++)
			{
				String colName = mColNames[i] ;
				if(mColNameMap != null && mColNameMap.containsKey(colName))
					continue ;
				colName = XString.removeUnderLine(colName) ;
				if(JCommon.unequals(colName, mColNames[i]))
				{
					if(mColNameMap == null)
						mColNameMap = XC.hashMap() ;
					mColNameMap.put(mColNames[i], colName) ;
				}
			}
		}
	}
	
	private String convertColName(String aName)
	{
		return mColNameMap == null?aName:JCommon.defaultIfNull(mColNameMap.get(aName) , aName) ;
	}

	@Override
	public JSONObject apply(ResultSet aValue) throws SQLException
	{
		JSONObject jobj = new JSONObject() ;
		for(int i=0 ; i<mGetters.length ; i++)
		{
			jobj.put(convertColName(mColNames[i]) , mGetters[i].getResult(aValue)) ;
		}
		return jobj ;
	}
}
