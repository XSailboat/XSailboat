package team.sailboat.commons.fan.dtool.taos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import team.sailboat.commons.fan.infc.EConsumer;

class GetColumnSchemas implements EConsumer<ResultSet , SQLException> , TDengineConst
{
	static final String sCol_ColumnName = "field" ;
	static final String sCol_DataType = "type" ;
	static final String sCol_Length = "length" ;
	static final String sCol_Note = "note" ;
	
	
	List<TDengineColumnSchema> mColSchemaList = new ArrayList<>() ;
	
	public GetColumnSchemas()
	{
		TDengineColumnSchema colSchema = new TDengineColumnSchema("TBNAME", false) ;
		colSchema.setDataType(sDataType_NCHAR) ;
		colSchema.setDataLength(256);
		colSchema.setComment("普通表名[内置]") ;
		mColSchemaList.add(colSchema) ;
	}

	@Override
	public void accept(ResultSet aT) throws SQLException
	{
		TDengineColumnSchema colSchema = new TDengineColumnSchema() ;
//		aT.getMetaData()
		colSchema.setColumnName(aT.getString(sCol_ColumnName));
		String dataType = aT.getString(sCol_DataType) ;
		colSchema.setDataType(dataType);
		switch(dataType)
		{
		case sDataType_NCHAR :
		case sDataType_BINARY:
			colSchema.setDataLength(aT.getInt(sCol_Length)) ;
			break ;
		default:
		}
		String note = aT.getString(sCol_Note) ;
		colSchema.setTag("TAG".equals(note)) ;
		colSchema.setComment(note) ;
		mColSchemaList.add(colSchema) ;
	}
	
}
