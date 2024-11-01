package team.sailboat.commons.fan.dtool.oracle;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import team.sailboat.commons.fan.dtool.ColumnSchema;
import team.sailboat.commons.fan.infc.EConsumer;

class GetColumnSchemas implements EConsumer<ResultSet , SQLException>
{
	static final String sCol_ColumnName = "COLUMN_NAME" ;
	static final String sCol_DataType = "DATA_TYPE" ;
	static final String sCol_DataLength = "DATA_LENGTH" ;
//	static final String sCol_DefaultLength = "DEFAULT_LENGTH" ;
	static final String sCol_DataPrecision = "DATA_PRECISION" ;
	static final String sCol_DataDefault = "DATA_DEFAULT" ;
	
	static final Set<String> sSpecialColNameSet = new HashSet<>() ;
	static
	{
		sSpecialColNameSet.add(sCol_ColumnName) ;
		sSpecialColNameSet.add(sCol_DataType) ;
		sSpecialColNameSet.add(sCol_DataLength) ;
//		sSpecialColNameSet.add(sCol_DefaultLength) ;
		sSpecialColNameSet.add(sCol_DataPrecision) ;
		sSpecialColNameSet.add(sCol_DataDefault) ;
		}
	
	List<ColumnSchema> mColSchemaList = new ArrayList<>() ;
	
	String[] mOtherColNames ;

	@Override
	public void accept(ResultSet aT) throws SQLException
	{
		OracleColumnSchema colSchema = new OracleColumnSchema() ;
		colSchema.setColumnName(aT.getString(sCol_ColumnName));
		colSchema.setDataType(aT.getString(sCol_DataType));
		colSchema.setDataLength(aT.getInt(sCol_DataLength));
		colSchema.setDataPrecision(aT.getInt(sCol_DataPrecision)) ;
		colSchema.setDataDefault(aT.getObject(sCol_DataDefault)) ;
//		colSchema.setDefaultLength(aT.getInt(sCol_DefaultLength));
		
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
