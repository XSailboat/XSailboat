package team.sailboat.commons.fan.jquery;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.function.Consumer;

import team.sailboat.commons.fan.dtool.DBHelper;
import team.sailboat.commons.fan.infc.EConsumer;
import team.sailboat.commons.fan.infc.EFunction;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;

public class JQueryComponents
{
	static final ThreadLocal<JQueryComponents> sInstanceTL = new ThreadLocal<JQueryComponents>() ;
	
	protected JSONObject mMetaJo ;
	
	protected JSONObject mColumnsJo ;
	
	protected JSONObject mResultJo ;
	
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
					.put("columns" , mColumnsJo)
					.put("meta", mMetaJo) ;
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
	
	static JQueryComponents newInstance_TL()
	{
		JQueryComponents instance = new JQueryComponents() ;
		sInstanceTL.set(instance) ;
		return instance ;
	}
	
	static JQueryComponents getInstance_TL()
	{
		return sInstanceTL.get() ;
	}
	
	static JQueryComponents clearInstance_TL()
	{
		JQueryComponents instance =  sInstanceTL.get() ;
		sInstanceTL.remove();
		return instance ;
	}
}
