package team.sailboat.commons.fan.dtool.taos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import team.sailboat.commons.fan.dtool.IndexSchema;
import team.sailboat.commons.fan.infc.EConsumer;

class GetIndexSchemas implements EConsumer<ResultSet, SQLException>
{
	static final String sCol_NonUnique = "NON_UNIQUE" ;
	static final String sCol_COLLATION = "COLLATION" ;
	
	static final String sCol_TableName = "TABLE_NAME" ;
	static final String sCol_ColumnName = "COLUMN_NAME" ;
	static final String sCol_Descend = "DESCEND" ;
	static final String sCol_IndexName = "INDEX_NAME" ;
	
	Map<String , IndexSchema> mIndexMap = new HashMap<>() ;

	public GetIndexSchemas()
	{
	}
	
	public IndexSchema[] getIndexSchemas()
	{
		return mIndexMap.values().toArray(new IndexSchema[0]) ;
	}
	
	@Override
	public void accept(ResultSet aT) throws SQLException
	{
		String indexName = aT.getString(sCol_IndexName) ;
		IndexSchema indexSchema = mIndexMap.get(indexName) ;
		if(indexSchema == null)
		{
			indexSchema = new IndexSchema(indexName) ;
			indexSchema.setTableName(aT.getString(sCol_TableName)) ;
			mIndexMap.put(indexName, indexSchema) ;
		}
		indexSchema.addColumn(aT.getString(sCol_ColumnName), "A".equals(aT.getString(sCol_COLLATION))) ;
	}
	
}
