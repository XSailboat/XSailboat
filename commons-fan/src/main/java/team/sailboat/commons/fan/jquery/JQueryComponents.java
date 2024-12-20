package team.sailboat.commons.fan.jquery;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.function.Consumer;

import team.sailboat.commons.fan.dtool.DBHelper;
import team.sailboat.commons.fan.infc.EConsumer;
import team.sailboat.commons.fan.infc.EFunction;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
/**
 * 
 * JQuery查询的功能构建
 *
 * @author yyl
 * @since 2024年12月11日
 */
public class JQueryComponents
{
	/**
	 * 创建一个新的实例
	 * 
	 * @return
	 */
	public static JQueryComponents one()
	{
		return new JQueryComponents() ;
	}
	
	protected JSONObject mMetaJo ;
	
	/**
	 * 各列信息
	 */
	protected JSONObject mColumnsJo ;
	
	/**
	 * 适用于矩阵型的返回数据			<br />
	 * 列名数组			<br />
	 * 例如：["ts" , "value"]
	 */
	protected JSONArray mColumnNamesJa ;
	
	protected JSONObject mResultJo ;
	
	public EConsumer<ResultSetMetaData , SQLException> columnsBuilder()
	{
		return (rsmd)->{
			int len = rsmd.getColumnCount() ;
			if(mColumnsJo == null)
				mColumnsJo = JSONObject.one() ;
			if(mColumnNamesJa == null)
				mColumnNamesJa = JSONArray.one() ;
			for(int i=1 ; i<=len ; i++)
			{
				String name = rsmd.getColumnLabel(i) ;
				int j = name.indexOf('.') ; 
				name = j == -1?name:name.substring(j+1) ; 
				mColumnsJo.put(name , new JSONObject().put("dataType" , rsmd.getColumnTypeName(i) )
						.put("index", i-1)
						) ;
				mColumnNamesJa.put(name) ;
			}
		} ;
	}
	
	public EConsumer<ResultSetMetaData , SQLException> metaBuilder()
	{
		return (rsmd)->{
			int len = rsmd.getColumnCount() ;
			if(mColumnsJo == null)
				mColumnsJo = new JSONObject() ;
			for(int i=1 ; i<=len ; i++)
			{
				String name = rsmd.getColumnLabel(i) ;
				int j = name.indexOf('.') ; 
				name = j == -1?name:name.substring(j+1) ; 
				mColumnsJo.put(name , new JSONObject().put("dataType" , rsmd.getColumnTypeName(i) )
						.put("index", i-1)) ;
				if(mMetaJo == null)
					mMetaJo = new JSONObject() ;
				mMetaJo.put(name, new JSONObject()
						.put("tableName",  DBHelper.getTableName(rsmd ,i))			// hive不支持getTableName
						.put("columnLabel", rsmd.getColumnLabel(i))
						.put("columnName", DBHelper.getColumnName(rsmd , i))
						.put("columnTypeName", rsmd.getColumnTypeName(i))
						.put("catalogName" , DBHelper.getCatalogName(rsmd , i))
						.put("schemaName" , DBHelper.getSchemaName(rsmd , i))) ;
			}
		} ;
	}
	
	public EFunction<JSONArray , Object , SQLException> resultFactory()
	{
		return (ja)->{
			if(mResultJo == null)
				mResultJo = new JSONObject() ;
			return mResultJo.put("data", ja)
					.putIf(mColumnsJo != null , "columns" , mColumnsJo)
					.putIf(mColumnNamesJa != null , "columnNames", mColumnNamesJa)
					.putIf(mMetaJo != null , "meta", mMetaJo) ;
		} ;
	}
	
	public Consumer<JSONObject> carePageQueryMeta()
	{
		return (jo)->{
			if(mResultJo == null)
				mResultJo = jo.clone() ;
			else
				mResultJo.copyAllFrom(jo) ;
		} ;
	}
}
