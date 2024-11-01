package team.sailboat.commons.fan.dtool.mysql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import team.sailboat.commons.fan.infc.EConsumer;
import team.sailboat.commons.fan.lang.JCommon;

class GetColumnSchemas implements EConsumer<ResultSet , SQLException>
{
	static final String sCol_ColumnName = "COLUMN_NAME" ;
	static final String sCol_DataType = "DATA_TYPE" ;
	static final String sCol_CHARACTER_MAXIMUM_LENGTH = "CHARACTER_MAXIMUM_LENGTH" ;
	static final String sCol_NUMERIC_PRECISION = "NUMERIC_PRECISION" ;
	static final String sCol_CHARACTER_SET_NAME = "CHARACTER_SET_NAME" ;
	static final String sCol_COLLATION_NAME = "COLLATION_NAME" ;
	static final String sCol_COLUMN_TYPE = "COLUMN_TYPE" ;
	static final String sCol_COLUMN_KEY = "COLUMN_KEY" ;
	static final String sCol_COLUMN_COMMENT = "COLUMN_COMMENT" ;
	
	
	static final Set<String> sSpecialColNameSet = new HashSet<>() ;
	static final Map<String , String> sOtherColNameMap = new HashMap<>() ;
	static
	{
		sSpecialColNameSet.add(sCol_ColumnName) ;
		sSpecialColNameSet.add(sCol_DataType) ;
		sSpecialColNameSet.add(sCol_CHARACTER_MAXIMUM_LENGTH) ;
		sSpecialColNameSet.add(sCol_NUMERIC_PRECISION) ;
		sOtherColNameMap.put("CHARACTER_SET" , MySQLFeatures.COLUMN__CHARSET) ;
		sOtherColNameMap.put("COLLATION" , MySQLFeatures.COLUMN__COLLATION) ;
		sOtherColNameMap.put("ON_UPDATE" , MySQLFeatures.COLUMN__ON_UPDATE) ;
	}
	
	List<MySQLColumnSchema> mColSchemaList = new ArrayList<>() ;
	
	String[] mOtherColNames ;

	@Override
	public void accept(ResultSet aT) throws SQLException
	{
		MySQLColumnSchema colSchema = new MySQLColumnSchema() ;
		
		colSchema.setColumnName(aT.getString(sCol_ColumnName));
		String dataType = aT.getString(sCol_DataType) ;
		colSchema.setDataType(dataType);
		switch(dataType)
		{
		case "varchar":
		case "char":
		case "blob":
			colSchema.setDataLength(aT.getInt(sCol_CHARACTER_MAXIMUM_LENGTH)) ;
			break ;
		case "int":
			colSchema.setDataPrecision(aT.getInt(sCol_NUMERIC_PRECISION)+1) ;
			break ;
		case "tinyint":
		{
			String columnType = aT.getString(sCol_COLUMN_TYPE) ;
			int i = columnType.indexOf('(') ;
			if(i != -1)
				colSchema.setDataLength(Integer.parseInt(columnType.substring(i+1 , columnType.indexOf(')')))) ;
			break ;
		}
		}
		colSchema.setComment(aT.getString(sCol_COLUMN_COMMENT)) ;
		
		if(mOtherColNames == null)
		{
			ResultSetMetaData md = aT.getMetaData() ;
			int colCount = md.getColumnCount() ;
			mOtherColNames = new String[colCount] ;
			for(int i=0 ; i<colCount ; i++)
			{
				mOtherColNames[i] = md.getColumnName(i+1) ;
				if(sSpecialColNameSet.contains(mOtherColNames[i]))
					mOtherColNames[i] = null ;
				else
					mOtherColNames[i] = JCommon.defaultIfNull(sOtherColNameMap.get(mOtherColNames[i]) , mOtherColNames[i]) ;
			}
		}
		
		for(int i=0 ; i<mOtherColNames.length ; i++)
		{
			if(mOtherColNames[i] != null)
			{
				Object val = aT.getObject(i+1) ;
				if(val != null)
					colSchema.putOtherProperty(mOtherColNames[i],val);
			}
		}
		mColSchemaList.add(colSchema) ;
	}
	
}
