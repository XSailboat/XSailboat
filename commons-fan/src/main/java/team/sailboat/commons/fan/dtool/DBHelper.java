package team.sailboat.commons.fan.dtool;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import javax.sql.DataSource;
import javax.sql.PooledConnection;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dtool.h2.H2Tool;
import team.sailboat.commons.fan.dtool.hive.HiveTool;
import team.sailboat.commons.fan.dtool.mysql.MySQLTool;
import team.sailboat.commons.fan.dtool.oracle.OracleTool;
import team.sailboat.commons.fan.dtool.pg.PgTool;
import team.sailboat.commons.fan.dtool.taos.TDengineTool;
import team.sailboat.commons.fan.infc.EConsumer;
import team.sailboat.commons.fan.infc.EPredicate;
import team.sailboat.commons.fan.infc.EPredicate2;
import team.sailboat.commons.fan.infc.ESupplier;
import team.sailboat.commons.fan.infc.IterateOpCode;
import team.sailboat.commons.fan.infc.IteratorPredicate;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONException;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.lang.YClassLoader;
import team.sailboat.commons.fan.struct.Wrapper;
import team.sailboat.commons.fan.text.XString;

public class DBHelper
{
	public static final String sDriverName_TDengine = "com.taosdata.jdbc.rs.RestfulDriver" ;
	
	public static final String sDriverName_MySQL = "com.mysql.cj.jdbc.Driver" ;
	
	public static final Map<DBType , Class<?>> sJDBCMap = XC.hashMap() ;
	
	static Pattern sPtn_Comment = Pattern.compile("(?ms)('(?:''|[^'])*')|--.*?$|/\\*.*?\\*/|#.*?$|") ; 
	
	/**
	 * 是否是数值类型的
	 * @param aType
	 * @return
	 */
	public static boolean isNumeric(int aType)
	{
		switch(aType)
		{
		case Types.NUMERIC:
		case Types.INTEGER:
			return true ;
		default:
			return false ;
		}
	}
	
	public static boolean isDate(int aType)
	{
		switch(aType)
		{
		case Types.DATE:
		case Types.TIME:
		case Types.TIMESTAMP:
			return true ;
		default:
			return false ;
		}
	}
	
	public static String getSchema(Connection aConn) throws SQLException
	{
		if(aConn instanceof PooledConnection)
			return ((PooledConnection)aConn).getConnection().getSchema() ;
		else
			return aConn.getSchema() ;
	}
	
	public static String getCatalog(Connection aConn) throws SQLException
	{
		if(aConn instanceof PooledConnection)
			return ((PooledConnection)aConn).getConnection().getCatalog() ;
		else
			return aConn.getCatalog() ;
	}
	
	/**
	 * 取得当前用户的所有表名
	 * @return
	 * @throws SQLException 
	 */
	public static String[] getTableNames_User(Connection aConn) throws SQLException
	{
		try(ResultSet rs = aConn.getMetaData().getTables(getCatalog(aConn) , getSchema(aConn) , null , null))
		{
			List<String> tableNames = new ArrayList<>() ;
			while(rs.next())
			{
				tableNames.add(rs.getString("TABLE_NAME")) ;
			}
			return tableNames.toArray(JCommon.sEmptyStringArray) ;
		}
	}
	
	public static JSONArray getTableDescriptors(Connection aConn
			, String aCatalogName
			, String aSchemaName
			, String... aTypes) throws SQLException
	{
		try(ResultSet rs = aConn.getMetaData().getTables(aCatalogName , aSchemaName , null , aTypes))
		{
			JSONArray ja = new JSONArray() ;
			while(rs.next())
			{
				ja.put(new JSONObject().put("name" , rs.getString("TABLE_NAME"))
						.put("catalog" , rs.getString("TABLE_CAT"))
						.put("schema" , rs.getString("TABLE_SCHEM"))
						.put("comment" , rs.getString("REMARKS"))
						.put("type" , rs.getString("TYPE_NAME"))) ;
			}
			return ja ;
		}
	}
	
	public static Map<String , String> getTableNameAndComments_default(Connection aConn) throws SQLException
	{
		try
		{
			IDBTool dbTool = getDBTool(aConn) ;
			return dbTool.getTableNameAndComments_default(aConn) ;
		}
		catch(Exception e)
		{
			try(ResultSet rs = aConn.getMetaData().getTables(getCatalog(aConn) , getSchema(aConn) , null , null))
			{
				Map<String, String> map = XC.linkedHashMap() ;
				while(rs.next())
				{
					map.put(rs.getString("TABLE_NAME") , rs.getString("REMARKS")) ;
				}
				return map ;
			}
		}
	}
	
	/**
	 * 返回的map，如果get一个不存在的列名，将返回-1
	 * @param aRmd
	 * @return
	 * @throws SQLException
	 */
	public static TObjectIntMap<String> buildColNameIndexMap(ResultSetMetaData aRmd) throws SQLException
	{
		TObjectIntMap<String> map = TObjectIntHashMap.create(-1) ;
		
		final int colCount = aRmd.getColumnCount() ;
		for(int i=1 ; i<=colCount ; i++)
		{
			map.put(aRmd.getColumnLabel(i) , i) ;
		}
		
		return map ;
	}
	
	public static Connection connect(String aConnStr , String aUsername , String aPassword) throws SQLException, ClassNotFoundException
	{
		return connect(loadJDBC(getDBType(aConnStr)) , aConnStr , aUsername, aPassword) ;
	}
	
	public static Connection connect(String aConnStr , Properties aProps) throws SQLException, ClassNotFoundException
	{
		return connect(loadJDBC(getDBType(aConnStr)) , aConnStr , aProps) ;
	}
	
	/**
	 * 数据库JDBC连接工厂
	 * @param aDriverClass
	 * @param aConnStr
	 * @param aUsername
	 * @param aPassword
	 * @return
	 */
	public static ESupplier<Connection , SQLException> createConnFactory(Class<?> aDriverClass , String aConnStr , String aUsername , String aPassword)
	{
		return new DBConnectionFactory(aDriverClass , aConnStr , aUsername , aPassword) ;
	}
	
	public static Connection connect(Class<?> aClass , String aUrl , String aUsername , String aPassword) throws SQLException
	{
		Properties info = new Properties() ;
		if (aUsername != null)
			 info.put("user", aUsername);
		if (aPassword != null)
			info.put("password", aPassword);
		return connect(aClass, aUrl, info) ;
	}
	
	public static Connection connect(Class<?> aClass , String aUrl , Properties aProps) throws SQLException
	{
		Driver driver;
		try
		{
			driver = (Driver)aClass.getConstructor().newInstance();
		}
		catch (Exception e)
		{
			throw new IllegalStateException(e) ;
		}
		return driver.connect(aUrl, aProps) ;
	}
	
	public static Connection connect(Class<?> aJdbcClass , String aUrl , String aUsername , String aPassword , Properties aProps) throws SQLException
	{
		Driver driver;
		try
		{
			driver = (Driver)aJdbcClass.getConstructor().newInstance();
		}
		catch (Exception e)
		{
			throw new IllegalStateException(e) ;
		}
		if(aProps == null)
			aProps = new Properties() ;
		if (aUsername != null)
			 aProps.put("user", aUsername);
		if (aPassword != null)
			aProps.put("password", aPassword);
		return driver.connect(aUrl, aProps) ;
	}
	
	public static String getConnStr(DBType aDBType , String aHost , int aPort , String aServiceName)
	{
		return getConnStr(aDBType, aHost, aPort, aServiceName, null) ;
	}
	
	/**
	 * 
	 * @param aDBType
	 * @param aHost
	 * @param aPort
	 * @param aServiceName 达梦数据库的ServiceName可以不填
	 * @param aSchemaName
	 * @return
	 */
	public static String getConnStr(DBType aDBType , String aHost , int aPort , String aServiceName
			, String aSchemaName)
	{
		switch(aDBType)
		{
		case Oracle:
			return String.format("jdbc:oracle:thin:@//%1$s:%2$d/%3$s", aHost , aPort , aServiceName) ;
		case MySQL:
		case MySQL5:
			return String.format("jdbc:mysql://%1$s:%2$d/%3$s", aHost , aPort , aServiceName) ;
		case DM:
			if(XString.isEmpty(aServiceName))
				return String.format("jdbc:dm://%1$s:%2$d", aHost , aPort) ;
			else
				return String.format("jdbc:dm://%1$s:%2$d/%3$s", aHost , aPort , aServiceName) ;
		case Derby:
			return String.format("jdbc:derby://%1$s:%2$d/%3$s", aHost , aPort , aServiceName) ;
		case SQLServer:
			return String.format("jdbc:sqlserver://%1$s:%2$d;DatabaseName=%3$s", aHost , aPort , aServiceName) ;
		case Hive:
			return XString.msgFmt("jdbc:hive2://{}:{}/{}" , aHost , aPort , aServiceName) ;
		case PostgreSQL:
			if(XString.isEmpty(aSchemaName))
				return XString.msgFmt("jdbc:postgresql://{}:{}/{}" , aHost , aPort , aServiceName) ;
			else
				return XString.msgFmt("jdbc:postgresql://{}:{}/{}?currentSchema={}" , aHost , aPort , aServiceName
						, aSchemaName) ;
		case TDengine:
			String url = XString.msgFmt("jdbc:TAOS-RS://{}:{}", aHost , aPort) ;
			if(XString.isNotEmpty(aServiceName))
				url += "/"+aServiceName ;
			return url ;
		default:
			throw new IllegalStateException("未实现拼接"+aDBType.getAliasName()+"类型数据库的JDBC连接串") ;
		}
	}
	
	public static Class<?> loadJDBC(DBType aDBType) throws ClassNotFoundException
	{
		if(aDBType == null)
			return null ;
		Class<?> clazz = sJDBCMap.get(aDBType) ;
		if(clazz == null)
		{
			String symbolicName = null ;
			String driver = null ;
			switch(aDBType)
			{
			case Oracle:
				symbolicName = "com.cimstech.xsql.oracle" ;
				driver = "oracle.jdbc.driver.OracleDriver" ;
				break ;
			case MySQL:
				symbolicName = "com.cimstech.xsql.mysql" ;
				driver = sDriverName_MySQL ;
				break ;
			case DM:
				symbolicName = "com.cimstech.xsql.dm" ;
				driver = "dm.jdbc.driver.DmDriver" ;
				break ;
			case SQLServer:
				symbolicName = "com.cimstech.xsql.sqlserver" ;
				driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver" ;
				break ;
			case Hive:
				driver = "org.apache.hive.jdbc.HiveDriver" ;
				break ;
			case H2:
				driver = "org.h2.Driver" ;
				break ;
			case PostgreSQL:
				driver = "org.postgresql.Driver" ;
				break ;
			case TDengine:
				driver = sDriverName_TDengine ;
				break ;
			default:
				throw new IllegalStateException("未定义"+aDBType.name()+"数据库JDBC") ;
			}
			
			YClassLoader classLoader = JCommon.getYClassLoader() ;
			try
			{
				clazz = classLoader.loadClass(driver) ;
			}
			catch (Exception e)
			{}
			if(clazz == null)
			{
				if(JCommon.isEclipseApp())
				{
					try
					{
						Class<?> platformCls = classLoader.loadClass("org.eclipse.core.runtime.Platform") ;
						if(platformCls != null)
						{
							Object bundle = XClassUtil.invokeStaticMethod(platformCls , "getBundle" , symbolicName) ;
							if(bundle == null)
								throw new ClassNotFoundException("未加载插件 "+symbolicName+" , 无法加载类"+driver) ;
							else
							{
								clazz = (Class<?>) XClassUtil.invokeMethod(bundle, "loadClass", driver) ;
								classLoader.addBundle(bundle) ;
							}
						}
					}
					catch(Exception e)
					{}
				}
				else
				{
					classLoader.addBundle(symbolicName) ;
					clazz = classLoader.loadClass(driver) ;
				}
			}
			if(clazz == null)
				throw new ClassNotFoundException(XString.splice("无法加载类：" , driver)) ;
			sJDBCMap.put(aDBType, clazz) ;
		}
		return clazz ;
	}
	
	public static DBType getDBType(String aConnStr)
	{
		if(XString.isBlank(aConnStr))
			return null ;
		if(aConnStr.startsWith("jdbc:oracle"))
			return DBType.Oracle ;
		else if(aConnStr.startsWith("jdbc:mysql"))
			return DBType.MySQL ;
		else if(aConnStr.startsWith("jdbc:sqlserver") || aConnStr.startsWith("jdbc:microsoft:sqlserver"))
			return DBType.SQLServer ;
		else if(aConnStr.startsWith("jdbc:dm"))
			return DBType.DM ;
		else if(aConnStr.startsWith("jdbc:derby"))
			return DBType.Derby ;
		else if(aConnStr.startsWith("jdbc:hive2"))
			return DBType.Hive ;
		else if(aConnStr.startsWith("jdbc:postgresql"))
			return DBType.PostgreSQL ;
		else if(aConnStr.startsWith("jdbc:h2"))
			return DBType.H2 ;
		else if(aConnStr.startsWith("jdbc:TAOS-RS"))
			return DBType.TDengine ;
		else
			throw new IllegalStateException("未能识别的数据库类型的数据库连接:"+aConnStr) ;
 	}
	
	public static DBType getDBType(Connection aConn) throws SQLException
	{
		String name = aConn.getMetaData().getDatabaseProductName() ;
		switch(name)
		{
		case "MySQL":
			return DBType.MySQL ;
		case "MySQL5":
			return DBType.MySQL5 ;
		case "Hive":
		case "Apache Hive" :
			return DBType.Hive ;
		case "PostgreSQL":
			return DBType.PostgreSQL ;
		case "DM DBMS":
			return DBType.DM ;
		case "TDengine":
			return DBType.TDengine ;
		case "Oracle":
			return DBType.Oracle ;
		case "H2":
			return DBType.H2 ;
		case "Microsoft SQL Server":
			return DBType.SQLServer ;
		default:
			throw new IllegalStateException("未区分DBType："+name) ;
		}
	}
	
	public static IDBTool getDBTool(DBType aDBType)
	{
		IDBTool dbTool = getDBTool_0(aDBType) ;
		Assert.notNull(dbTool , "尚未实现%s类型数据库的IDBTool工具" , aDBType.getAliasName()) ;
		return dbTool ;
	}
	
	public static IDBTool getDBTool_0(DBType aDBType)
	{
		return switch(aDBType)
		{
		case Oracle -> new OracleTool() ;
		case MySQL -> new MySQLTool(true) ;
		case MySQL5 -> new MySQLTool(false) ;
		case Hive -> new HiveTool() ;
		case PostgreSQL -> new PgTool() ;
		case H2 -> new H2Tool() ;
		case TDengine -> new TDengineTool() ;
		default -> null ;
		} ;
	}
	
	/**
	 * 如果不支持此数据库类型，将抛出异常
	 * @param aConn
	 * @return
	 * @throws SQLException
	 */
	public static IDBTool getDBTool(Connection aConn) throws SQLException
	{
		return getDBTool(getDBType(aConn)) ;
	}
	
	/**
	 * 如果不支持此数据库类型返回null
	 * @param aConn
	 * @return
	 * @throws SQLException
	 */
	public static IDBTool getDBTool_0(Connection aConn) throws SQLException
	{
		return getDBTool_0(getDBType(aConn)) ;
	}
	
	public static String getTableFullName(String aOwner  , String aTableName)
	{
		return XString.isEmpty(aOwner)?aTableName:XString.splice(aOwner , "." , aTableName) ;
	}
	
	public static Object[] getRowData(ResultSet aRS , final int aColAmount) throws SQLException
	{
		Object[] objs = new Object[aColAmount] ;
		for(int i=0 ; i<aColAmount ; i++)
			objs[i] = aRS.getObject(i+1) ;
		return objs ;
	}
	
	public static void getRowData(ResultSet aRS , Object[] aRow) throws SQLException
	{
		final int colAmount = aRow.length ;
		for(int i=0 ; i<colAmount ; i++)
			aRow[i] = aRS.getObject(i+1) ;
	}
	
//	public static String getCommonType(int aSqlType)
//	{
//		switch(aSqlType)
//		{
//		case Types.VARCHAR:
//		case Types.NVARCHAR:
//		case Types.NCHAR:
//			return XClassUtil.sCSN_String ;
//		case Types.INTEGER:
//			return XClassUtil.sCSN_Integer ;
//		case Types.DOUBLE:
//		case Types.DECIMAL:
//			return XClassUtil.sCSN_Double ;
//		case Types.FLOAT:
//			return XClassUtil.sCSN_Float ;
//		case Types.DATE:
//		case Types.TIME:
//		case Types.TIMESTAMP:
//			return XClassUtil.sCSN_DateTime ;
//		default:
//			throw new SQLException(String.format("未处理的类型:%d" , aSqlType)) ;
//		}
//	}
	
	public static void executeQuery(DataSource aDS , String aSql , EConsumer<ResultSet , SQLException> aConsumer) throws SQLException
	{
		try(Connection conn = aDS.getConnection())
		{
			executeQuery(conn, aSql, aConsumer, 2500);
		}
	}
	
	public static void executeQuery(Connection aConn , String aSql , EConsumer<ResultSet , SQLException> aConsumer) throws SQLException
	{
		executeQuery(aConn, aSql, aConsumer, 2500);
	}
	
	public static void executeQuery(Statement aStm , String aSql , EConsumer<ResultSet , SQLException> aConsumer) throws SQLException
	{
		try(ResultSet rs = aStm.executeQuery(aSql))
		{
			while(rs.next())
				aConsumer.accept(rs);
		}
	}
	
	public static void executeQuery(DataSource aDS , String aSql 
			, EConsumer<ResultSet , SQLException> aConsumer , int aFetchSize , Object...aArgs) throws SQLException
	{
		try(Connection conn = aDS.getConnection())
		{
			executeQuery(conn, aSql, aConsumer, aFetchSize, aArgs) ;
		}
	}
	
	public static void executeQuery(Connection aConn , String aSql 
			, EConsumer<ResultSet , SQLException> aConsumer , int aFetchSize , Object...aArgs) throws SQLException
	{
		IDBTool dbTool = getDBTool_0(aConn) ;
		if(dbTool != null)
			dbTool.iterate(aConn, aSql, aConsumer, aFetchSize, aArgs) ;
		else
			IDBTool._query(aConn, aSql, (rs)->{
				while(rs.next())
					aConsumer.accept(rs) ;
			}, aFetchSize, aArgs) ;
	}
	
	public static void executeQuery(DataSource aDS , String aSql , EPredicate<ResultSet , SQLException> aPredicate) throws SQLException
	{
		try(Connection conn = aDS.getConnection())
		{
			executeQuery(conn, aSql, aPredicate);
		}
	}
	
	/**
	 * 
	 * @param aConn
	 * @param aSql
	 * @param aPredicate			返回false的话将中断遍历
	 * @throws SQLException
	 */
	public static void executeQuery(Connection aConn , String aSql , EPredicate<ResultSet , SQLException> aPredicate) throws SQLException
	{
		executeQuery(aConn, aSql, aPredicate, 2500) ;
	}
	
	public static void executeQuery(DataSource aDS , String aSql 
			, EPredicate<ResultSet , SQLException> aPredicate
			, int aFetchSize , Object...aArgs) throws SQLException
	{
		try(Connection conn = aDS.getConnection())
		{
			executeQuery(conn, aSql, aPredicate, aFetchSize, aArgs);
		}
	}
	
	public static void executeQuery(ESupplier<Connection , SQLException> aConnSupplier , String aSql 
			, EPredicate<ResultSet , SQLException> aPredicate
			, int aFetchSize , Object...aArgs) throws SQLException
	{
		try(Connection conn = aConnSupplier.get())
		{
			executeQuery(conn, aSql, aPredicate, aFetchSize, aArgs);
		}
	}
	
	public static void executeQuery(Connection aConn , String aSql 
			, EPredicate<ResultSet , SQLException> aPredicate
			, int aFetchSize , Object...aArgs) throws SQLException
	{
		IDBTool dbTool = getDBTool_0(aConn) ;
		if(dbTool != null)
			dbTool.iterate(aConn, aSql, aPredicate, aFetchSize, aArgs) ;
		else
			IDBTool._query(aConn, aSql, (rs)->{
				while(rs.next())
				{
					if(!aPredicate.test(rs))
						return ;
				}
			}, aFetchSize, aArgs) ;
	}
	
	public static void executeQuery(Statement aStm , String aSql 
			, EPredicate<ResultSet , SQLException> aPredicate
			, int aFetchSize) throws SQLException
	{
		aStm.setFetchSize(aFetchSize) ;
		try(ResultSet rs = aStm.executeQuery(aSql))
		{
			while(rs.next())
			{
				if(!aPredicate.test(rs))
					break ;
			}
		}
	}
	
	public static void execute(DataSource aDS , String aSql , Object...aArgs) throws SQLException
	{
		try(Connection conn = aDS.getConnection())
		{
			execute(conn , true , aSql, aArgs) ;
		}
	}
	
	public static void batchExecute(DataSource aDS , Collection<String> aSqls) throws SQLException
	{
		try(Connection conn = aDS.getConnection())
		{
			boolean autoCommit = conn.getAutoCommit() ;
			conn.setAutoCommit(false) ;
			try(Statement stm = conn.createStatement())
			{
				for(String sql : aSqls)
				{
					stm.addBatch(sql);
				}
				stm.executeBatch() ;
				conn.commit(); 
			}
			finally
			{
				conn.setAutoCommit(autoCommit) ;
			}
		}
	}
	
	public static void execute(Connection aConn , boolean aCommit , String aSql , Object...aArgs) throws SQLException
	{
		try(PreparedStatement pstm = aConn.prepareStatement(aSql))
		{
			if(XC.isNotEmpty(aArgs))
			{
				for(int i=0 ; i<aArgs.length ; i++)
				{
					pstm.setObject(i+1, aArgs[i]) ;
				}
			}
			pstm.execute() ;
			if(aCommit)
				aConn.commit() ;
		}
	}
	
	public static void execute(Connection aConn , String aSql , Object...aArgs) throws SQLException
	{
		execute(aConn, false, aSql, aArgs) ;
	}
	
	public static void executeQuery(DataSource aDS , String aSql , EPredicate2<ResultSet , String[] , SQLException> aPred
			, int aFetchSize , Object...aArgs) throws SQLException
	{
		try(Connection conn = aDS.getConnection())
		{
			executeQuery(conn, aSql, aPred, aFetchSize, aArgs) ;
		}
	}
	
	public static void executeQuery(Connection aConn , String aSql , EPredicate2<ResultSet , String[] , SQLException> aPred
			, int aFetchSize , Object...aArgs) throws SQLException
	{
		IDBTool dbTool = getDBTool_0(aConn) ;
		EConsumer<ResultSet , SQLException> consumer = (rs)->{
			ResultSetMetaData rsmd = rs.getMetaData() ;
			String[] cols = new String[rsmd.getColumnCount()] ;
			for(int i=0 ; i<cols.length ; i++)
				cols[i] = rsmd.getColumnLabel(i+1) ;
			while(rs.next())
			{
				if(!aPred.test(rs , cols))
					return ;
			}
		} ;
		if(dbTool != null)
			dbTool.query(aConn, aSql, consumer , aFetchSize, aArgs);
		else
			IDBTool._query(aConn, aSql, consumer, aFetchSize, aArgs) ;
	}
	
	public static String eliminateComments(String aSql)
	{
		return sPtn_Comment.matcher(aSql).replaceAll("$1");
	}
	
	public static String safeLen(String aName)
	{
		Assert.notEmpty(aName) ;
		return aName.length()>32?aName.substring(0, 32):aName ;
	}
	
	public static String escapeForJSONString(String string)
	{
		if (string == null || string.isEmpty())
			return string;
		char[] chs = string.toCharArray();
		final int len = chs.length;
		StringBuilder strBld = null;
		for (int i = 0; i < len; i++)
		{
			switch (chs[i])
			{
			case '\'':
				if (strBld == null)
				{
					strBld = new StringBuilder();
					strBld.append(chs, 0, i);
				}
				strBld.append('\'').append('\'');
				break;
			case '\\':
				if (strBld == null)
				{
					strBld = new StringBuilder();
					strBld.append(chs, 0, i);
				}
				strBld.append('\\').append('\\');
				break;
			default:
				if (strBld != null)
					strBld.append(chs[i]);
			}
		}
		return strBld == null ? string : strBld.toString();
	}
	
	public static Class<?> getJavaType(int aDBDataType)
	{
		switch(aDBDataType)
		{
			case Types.VARCHAR:
			case Types.CHAR:
			case Types.NCHAR:
			case Types.LONGVARCHAR:
				return String.class ;
			case Types.BIGINT:
				return Long.class ;
			case Types.INTEGER:
			case Types.SMALLINT:
			case Types.TINYINT:
				return Integer.class ;
			case Types.DOUBLE:
			case Types.FLOAT:
			case Types.DECIMAL:
			case Types.REAL:
				return Double.class ;
			case Types.DATE:
			case Types.TIME:
			case Types.TIMESTAMP:
				return java.util.Date.class ;
			default:
				throw new IllegalStateException("不支持SQL类型："+aDBDataType) ;
		}
	}
	
	public static JSONArray queryAll(DataSource aDS , String aSql , Object...aParamVals) throws SQLException
	{
		return queryAll_3(aDS, aSql, null , null , null , null , aParamVals) ;
	}
	
	public static JSONArray queryAll_2(DataSource aDS , String aSql , Map<String , String> aColumnNameMap 
			, Object...aParamVals) throws SQLException
	{
		return queryAll_3(aDS, aSql, aColumnNameMap , null, null , null , aParamVals) ;
	}
	
	public static JSONArray queryAll_3(DataSource aDS , String aSql , Map<String , String> aColumnNameMap 
			, IteratorPredicate<ResultSet> aPred , Consumer<JSONObject> aObjHandler , Map<String , String> aColNameCvtMap , Object...aParamVals) throws SQLException
	{
		try(Connection conn = aDS.getConnection())
		{
			IDBTool dbTool = DBHelper.getDBTool(conn) ;
			JSONArray result = new JSONArray() ;
			if(XC.isNotEmpty(aParamVals))
			{
				Collection<Object> list = null ;
				for(int i=0 ; i<aParamVals.length ; i++)
				{
					if(aParamVals[i] == null)
					{
						list = XC.extract(aParamVals, (obj)->obj!=null) ;
						break ;
					}
				}
				if(list != null)
 					aParamVals = list.toArray() ;
			}
			dbTool.query(conn, aSql, (rs)->{
				if(aColumnNameMap != null)
				{
					ResultSetMetaData rsmd = rs.getMetaData() ;
					int len = rsmd.getColumnCount() ;
					for(int i=1 ; i<=len ; i++)
					{
						aColumnNameMap.put(rsmd.getTableName(i)+"."+rsmd.getColumnName(i) , rsmd.getColumnLabel(i)) ;
					}
				}
				RS2JSONObject cvt = new RS2JSONObject(rs.getMetaData() , aColNameCvtMap) ;
				while(rs.next())
				{
					if(aPred != null)
					{
						switch(aPred.visit(rs))
						{
						case IterateOpCode.sContinue:
						{
							JSONObject jobj = cvt.apply(rs) ;
							if(aObjHandler != null)
								aObjHandler.accept(jobj) ;
							result.put(jobj) ;
							break ;
						}
						case IterateOpCode.sBreak:
							break ;
						case IterateOpCode.sInterrupted:
							return ;
						default:
							throw new IllegalStateException("不合法的迭代行为代码") ;
						}
					}
					else
					{
						JSONObject jobj = cvt.apply(rs) ;
						if(aObjHandler != null)
							aObjHandler.accept(jobj) ;
						result.put(jobj) ;
					}
				}
			}, 1000 , aParamVals);
			return result ;
		}
	}
	
	public static JSONArray queryAll(Connection aConn , String aSql , int aSize) throws SQLException
	{
		IDBTool dbTool = DBHelper.getDBTool(aConn) ;
		JSONArray result = new JSONArray() ;
		final int size = aSize<=0?Integer.MAX_VALUE:aSize ;
		dbTool.query(aConn, aSql, (rs)->{
			RS2JSONObject cvt = new RS2JSONObject(rs.getMetaData()) ;
			while(rs.next())
			{
				result.put(cvt.apply(rs)) ;
				if(result.size() >= size)
					break ;
			}
		}, size);
		return result ;
	}
	
	public static JSONArray queryAll_1(DataSource aDS , String aSql , int aSize , Map<String , String> aColumnNameMap , Object...aParamVals) throws SQLException
	{
		try(Connection conn = aDS.getConnection())
		{
			IDBTool dbTool = DBHelper.getDBTool(conn) ;
			JSONArray result = new JSONArray() ;
			final int size = aSize<=0?Integer.MAX_VALUE:aSize ;
			dbTool.query(conn, aSql, (rs)->{
				if(aColumnNameMap != null)
				{
					ResultSetMetaData rsmd = rs.getMetaData() ;
					int len = rsmd.getColumnCount() ;
					for(int i=1 ; i<=len ; i++)
						aColumnNameMap.put(rsmd.getTableName(i)+"."+rsmd.getColumnName(i) , rsmd.getColumnLabel(i)) ;
				}
				RS2JSONObject cvt = new RS2JSONObject(rs.getMetaData()) ;
				while(rs.next())
				{
					result.put(cvt.apply(rs)) ;
					if(result.size() >= size)
						break ;
				}
			}, size , aParamVals);
			return result ;
		}
	}
	
	public static JSONObject queryPage(DataSource aDS , String aSql , int aPageSize , int aPage , Object...aParamVals) throws SQLException
	{
		return queryPageAndHandle(aDS, aSql, aPageSize, aPage, null, aParamVals) ;
	}
	
	public static JSONObject queryPageAndHandle(DataSource aDS , String aSql , int aPageSize , int aPage , Consumer<JSONObject> aHandler , Object...aParamVals) throws SQLException
	{
		return queryPageAndHandle(aDS, aSql, aPageSize, aPage, aHandler, null, null, aParamVals) ;
	}
	
	public static JSONObject queryPageAndHandle(DataSource aDS , String aSql , int aPageSize , int aPage 
			, Consumer<JSONObject> aHandler
			, Collection<String> aJoCols
			, Collection<String> aJaCols
			, Object...aParamVals) throws SQLException
	{
		try(Connection conn = aDS.getConnection())
		{
			IDBTool dbTool = DBHelper.getDBTool(conn) ;
			Wrapper<JSONObject> resultWrapper = new Wrapper<JSONObject>() ;
			Wrapper<RS2JSONObject> cvtWrapper = new Wrapper<>() ;
			Wrapper<JSONObject> metadataJobjWrapper = new Wrapper<>() ;
			JSONArray data = new JSONArray() ;
			dbTool.queryPage(conn, aSql, aPageSize, aPage, (rs)->{
				if(cvtWrapper.isNull())
				{
					cvtWrapper.set(new RS2JSONObject(rs.getMetaData() , null, aJoCols , aJaCols)) ;
					metadataJobjWrapper.set(toJSONObject(rs.getMetaData())) ;
				}
				JSONObject jobj = cvtWrapper.get().apply(rs) ;
				if(aHandler != null)
					aHandler.accept(jobj) ;
				data.put(jobj) ;
			}, resultWrapper , aParamVals);
			
			return resultWrapper.get().put("data", data).put("metadata" , metadataJobjWrapper.get()) ;
		}
	}
	
	public static Map<String , String> getColumnAliasNames(DataSource aDS , String aDBTableName) throws SQLException
	{
		try(Connection conn = aDS.getConnection())
		{
			IDBTool dbTool = DBHelper.getDBTool(conn) ;
			TableSchema tblSchema = dbTool.getTableSchema(conn, null, aDBTableName) ;
			Assert.notNull(tblSchema , "找不到数据库表的定义："+aDBTableName) ;
			List<ColumnSchema> cols = tblSchema.getColumnSchemas() ;
			Map<String , String> aliasNamesMap = new HashMap<String, String>() ; 
			if(XC.isNotEmpty(cols))
			{
				for(ColumnSchema col : cols)
				{
					String aliasName = col.getComment() ;
					if(XString.isEmpty(aliasName))
						continue ;
					int i = XString.indexOf(aliasName , ',' , ' ') ;
					aliasNamesMap.put(col.getColumnName(), i != -1?aliasName.substring(0, i):aliasName) ;
				}
			}
			return aliasNamesMap ;
		}
	}
	
	public static JSONObject toJSONObject(ResultSetMetaData aRsmd) throws JSONException, SQLException
	{
		JSONObject jobj = new JSONObject() ;
		final int colCount = aRsmd.getColumnCount() ;
		for(int i=1 ; i<=colCount  ; i++)
		{
			String colLabel = aRsmd.getColumnLabel(i) ;
			String colName = aRsmd.getColumnName(i) ;
			jobj.put(XString.isEmpty(colLabel)?colName:colLabel , new JSONObject()
					.put("columnName" , colName)
					.put("columnLabel" , colLabel)
					.put("tableName" , aRsmd.getTableName(i))
					.put("schemaName", aRsmd.getSchemaName(i))
					.put("dataType" , aRsmd.getColumnTypeName(i))
					.put("catalogName" , aRsmd.getCatalogName(i))) ;
		}
		return jobj ;
	}
	
	public static String getTableName(ResultSetMetaData aRsmd , int aColIndex) throws SQLException
	{
		try
		{
			return aRsmd.getTableName(aColIndex) ;
		}
		catch(SQLFeatureNotSupportedException e)
		{
			String colName = aRsmd.getColumnName(aColIndex) ;
			int i =colName.indexOf('.') ;
			return i == -1?null:colName.substring(0, i) ;
		}
	}
	
	public static String getCatalogName(ResultSetMetaData aRsmd , int aColIndex) throws SQLException
	{
		try
		{
			return aRsmd.getCatalogName(aColIndex) ;
		}
		catch(SQLFeatureNotSupportedException e)
		{
			return null ;
		}
	}
	
	public static String getSchemaName(ResultSetMetaData aRsmd , int aColIndex) throws SQLException
	{
		try
		{
			return aRsmd.getSchemaName(aColIndex) ;
		}
		catch(SQLFeatureNotSupportedException e)
		{
			return null ;
		}
	}
	
	public static String getColumnName(ResultSetMetaData aRsmd , int aColIndex) throws SQLException
	{
		try
		{
			String colName = aRsmd.getColumnName(aColIndex) ;
			int i = colName.indexOf('.') ;
			return i==-1?colName:colName.substring(i+1) ;
		}
		catch(SQLFeatureNotSupportedException e)
		{
			return null ;
		}
	}
	
	public static IResultSetGetter getResultSetGetter(int aColType , int aColIndex) throws SQLException
	{
		switch(aColType)
		{
		case Types.VARCHAR:
		case Types.CHAR:
		case Types.NCHAR:
		case Types.LONGVARCHAR:
			return new RSG_String(aColIndex) ;
		case Types.BIGINT:
			return new DefaultRSG(aColIndex , Long.class) ;
		case Types.INTEGER:
		case Types.SMALLINT:
		case Types.TINYINT:
			return new DefaultRSG(aColIndex , Integer.class) ;
		case Types.DOUBLE:
		case Types.FLOAT:
		case Types.DECIMAL:
		case Types.REAL:
			return new DefaultRSG(aColIndex , Double.class) ;
		case Types.DATE:
		case Types.TIME:
		case Types.TIMESTAMP:
			return new RSG_SqlDate2Polytope(aColIndex , "yyyy-MM-dd HH:mm:ss.SSS") ;
		case Types.LONGVARBINARY :
		case Types.BLOB:
		case Types.BINARY:
			return new RSG_Cover(aColIndex, "[字节数组，不支持显示]") ;
		case Types.BOOLEAN:
		case Types.BIT:
			return new DefaultRSG(aColIndex , Boolean.class) ;
		default:
			throw new IllegalStateException("不支持SQL类型："+aColType+"]") ;
		}
	}
}
