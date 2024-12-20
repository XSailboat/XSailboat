package team.sailboat.commons.fan.statestore;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import team.sailboat.commons.fan.dtool.DBHelper;
import team.sailboat.commons.fan.dtool.DBType;
import team.sailboat.commons.fan.dtool.IDBTool;
import team.sailboat.commons.fan.dtool.TableSchema;
import team.sailboat.commons.fan.dtool.UpdateOrInsertKit;
import team.sailboat.commons.fan.dtool.mysql.MySQLConst;
import team.sailboat.commons.fan.dtool.mysql.MySQLFeatures;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.infc.ESupplier;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.XString;

/**
 * 
 * 基于数据库的程序运行数据存取器
 *
 * @author yyl
 * @since 2024年11月20日
 */
public class RunData_RDB implements IRunData
{

	ESupplier<Connection , SQLException> mConnSupplier ;
	
	String mTableName ;
	
	Map<String , String > mDataMap = new ConcurrentHashMap<String, String>() ;
	
	IDBTool mDBTool ;
	
	public RunData_RDB(ESupplier<Connection , SQLException> aConnSupplier , String aTableName) throws SQLException
	{
		mConnSupplier = aConnSupplier ;
		mTableName = aTableName ;
		_init() ;
	}
	
	void _init() throws SQLException
	{
		try(Connection conn = mConnSupplier.get())
		{
			mDBTool = DBHelper.getDBTool(conn) ;
			
			if(!mDBTool.isTableExists(conn, mTableName, null))
			{
				TableSchema tblSchema = mDBTool.builder_tableSchema()
						.name(mTableName)
						.comment("运行期配置")
						.column("mkey")
							.dataType_vchar(128)
							.comment("主键")
						.and().column("mvalue")
							.dataType_vchar(2048)
							.comment("值")
						.and().withPrimaryKey("mkey")
						.featureFor(MySQLFeatures.TABLE__ENGINE, "InnoDB", DBType.MySQL)
						.featureFor(MySQLFeatures.TABLE__CHARACTER_SET, "utf8", DBType.MySQL)
						.featureFor(MySQLFeatures.TABLE__COLLATION 
								, MySQLConst.sTBL_FEATURE__COLLATION__VAL__utf8_general_ci 
								, DBType.MySQL)
						.build() ;
				mDBTool.createTables(conn , tblSchema) ;
			}
			else
			{
				//加载数据
				DBHelper.executeQuery(conn, "SELECT mkey , mvalue FROM " + mTableName , (rs)->{
					mDataMap.put(rs.getString(1) , rs.getString(2)) ;
				});
			}
		}
	}
	
	
	UpdateOrInsertKit createUpdateOrInsertKit(Connection aConn) throws SQLException
	{
		UpdateOrInsertKit kit = mDBTool.createUpdateOrInsertKit(mTableName, new String[] {"mkey" , "mvalue"}
			, new int[] {Types.VARCHAR, Types.VARCHAR} ,  0) ;
		kit.prepare(aConn) ;
		return kit ;
	}
	
	public String get(String aKey)
	{
		return mDataMap.get(aKey) ;
	}
	
	public long getLong(String aKey , long aDefault)
	{
		String v = mDataMap.get(aKey) ;
		return XString.isEmpty(v)?aDefault:Long.parseLong(v) ;
	}
	
	/**
	 * 
	 * @param aKey
	 * @param aValue
	 * @return			变化了将返回true
	 * @throws SQLException 
	 */
	boolean _put(String aKey , String aValue , UpdateOrInsertKit aKit) throws SQLException
	{
		if(aValue == null)
			aValue = "" ;
		String oldValue = mDataMap.put(aKey, aValue) ;
		if(JCommon.unequals(aValue, oldValue))
		{
			aKit.add(aKey , aValue) ;
			return true ;
		}
		return false ;
	}
	
	public void put(String aKey , long aValue)
	{
		put(aKey , Long.toString(aValue)) ;
	}
	
	public void put(String aKey , String aValue)
	{
		if(aValue == null)
			aValue = "" ;
		String oldValue = mDataMap.put(aKey, aValue) ;
		if(JCommon.unequals(aValue, oldValue))
		{
			try(Connection conn = mConnSupplier.get())
			{
				UpdateOrInsertKit kit = createUpdateOrInsertKit(conn) ;
				kit.add(aKey , aValue) ;
				kit.finish();
			}
			catch (SQLException e)
			{
				WrapException.wrapThrow(e) ;
			}
		}
	}
	
	public void putAll(Map<String, String> aDataMap)
	{
		try(Connection conn = mConnSupplier.get())
		{
			UpdateOrInsertKit kit = createUpdateOrInsertKit(conn) ;
			for(Entry<String, String> entry : aDataMap.entrySet())
			{
				_put(entry.getKey() , entry.getValue() , kit) ;
			}
			kit.finish();
		}
		catch (SQLException e)
		{
			WrapException.wrapThrow(e) ;
		}
	}
}
