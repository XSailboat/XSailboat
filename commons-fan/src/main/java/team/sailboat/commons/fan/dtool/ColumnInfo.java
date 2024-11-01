package team.sailboat.commons.fan.dtool;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import team.sailboat.commons.fan.json.JSONArray;

@Data
@AllArgsConstructor
public class ColumnInfo
{
	String schemaName ;
	String tableName ;
	String columnName ;
	String columnLabel ;
	int index ;
	int sqlDataType ;
	
	public static JSONArray toColumns(List<ColumnInfo> aColInfoList)
	{
		JSONArray ja = new JSONArray() ;
		for(ColumnInfo colInfo : aColInfoList)
			ja.put(colInfo.getColumnName()) ;
		return ja ;
	}
}
