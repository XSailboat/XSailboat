package team.sailboat.base.ds;

import io.swagger.v3.oas.annotations.media.Schema;
import team.sailboat.base.def.DataSourceType;
import team.sailboat.commons.fan.collection.PropertiesEx;
import team.sailboat.commons.fan.dtool.DBHelper;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.XString;


@Schema(name="ConnInfo_RDB" , description = "关系数据库的连接信息")
public class ConnInfo_RDB extends ConnInfo_Pswd
{
	
	String mDBName ;
	
	String mHost ;
	
	int mPort ;
	
	String mUsername ;
	
	String mParams ;
	
	public ConnInfo_RDB()
	{
		super("ConnInfo_RDB") ;
	}
	
	protected ConnInfo_RDB(String aType)
	{
		super(aType) ;
	}
	
	@Schema(description = "数据库名称")
	public String getDbName()
	{
		return mDBName;
	}
	public void setDbName(String aDBName)
	{
		mDBName = aDBName;
	}
	
	@Schema(description = "主机")
	public void setHost(String aHost)
	{
		mHost = aHost;
	}
	public String getHost()
	{
		return mHost;
	}
	
	@Schema(description = "端口")
	public void setPort(int aPort)
	{
		mPort = aPort;
	}
	public int getPort()
	{
		return mPort;
	}
	
	@Schema(description = "用户名")
	public void setUsername(String aUsername)
	{
		mUsername = aUsername;
	}
	public String getUsername()
	{
		return mUsername;
	}
	
	@Schema(description = "连接参数，格式：key1=value1&key2=value2&...")
	public void setParams(String aParams)
	{
		mParams = aParams;
	}
	public String getParams()
	{
		return mParams;
	}
	
	@Override
	public void checkForCreate()
	{
		// 1. hostName不能为空不能为空
		Assert.notEmpty(mHost , "主机名不能为空") ;
		// 2. port
		Assert.isTrue(mPort > 0 , "必需指定端口");
	}
	
	protected void checkSupport(DataSourceType aType)
	{
		Assert.isTrue(aType.isRDB() , "指定的数据源类型不是RDB，而是%s" , aType.name()) ;
	}
	
	@Override
	public String getConnURI(DataSourceType aType)
	{
		checkSupport(aType) ;
		String connUri = DBHelper.getConnStr(DataSourceType.toDBType(aType) , mHost, mPort, mDBName) ;
		
		if(XString.isNotEmpty(mParams))
			connUri += "?"+mParams ;
		return connUri ;
	}
	
	@Override
	protected boolean update(ConnInfo aConnInfo , boolean aPartially)
	{
		boolean changed = super.update(aConnInfo , aPartially);
		if(aConnInfo instanceof ConnInfo_RDB)
		{
			ConnInfo_RDB connInfo = (ConnInfo_RDB)aConnInfo ;
			boolean resourceChanged = false ;
			if((!aPartially || XString.isNotEmpty(connInfo.mDBName)) && JCommon.unequals(connInfo.mDBName ,mDBName))
			{
				mDBName = connInfo.mDBName ;
				changed = true ;
				resourceChanged = true ;
			}
			if((!aPartially || XString.isNotEmpty(connInfo.mHost)) && JCommon.unequals(connInfo.mHost , mHost))
			{
				mHost = connInfo.mHost ;
				changed = true ;
				resourceChanged = true ;
			}
			if((!aPartially || connInfo.mPort>0) && connInfo.mPort != mPort)
			{
				mPort = connInfo.mPort ;
				changed = true ;
				resourceChanged = true ;
			}
			if((!aPartially || XString.isNotEmpty(connInfo.mUsername)) && JCommon.unequals(connInfo.mUsername,mUsername))
			{
			 	mUsername = connInfo.mUsername ;
			 	changed = true ;
			 	resourceChanged = true ;
			}
			if((!aPartially || XString.isNotEmpty(connInfo.mParams)) && JCommon.unequals(connInfo.mParams , mParams))
			{
				mParams = connInfo.mParams ;
				changed = true ;
				resourceChanged = true ;
			}
			if(resourceChanged)
				closeResource() ;
		}
		return changed ;
	}
	
	
	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		return super.setTo(aJSONObj)
				.put("dbName" , mDBName)
				.put("host" , mHost)
				.put("port" , mPort)
				.put("username", mUsername)
				.put("params", mParams)
				;
	}
	
	@Override
	public boolean equals(Object aObj)
	{
		if(!super.equals(aObj))
			return false ;
		ConnInfo_RDB connInfo = (ConnInfo_RDB)aObj ;
		return JCommon.equals(connInfo.mDBName , mDBName)
				&& JCommon.equals(connInfo.mDescription , mDescription)
				&& JCommon.equals(connInfo.mHost , mHost)
				&& JCommon.equals(connInfo.mParams , mParams)
				&& JCommon.equals(connInfo.mPort , mPort)
				&& JCommon.equals(connInfo.mUsername , mUsername) ;
	}
	
	@Override
	public ConnInfo_RDB clone()
	{
		ConnInfo_RDB clone = new ConnInfo_RDB() ;
		clone.copyFrom(this) ;
		return clone ;
	}
	
	public static ConnInfo_RDB parse(String aConnInfo)
	{
		JSONObject jo = new JSONObject(aConnInfo) ;
		ConnInfo_RDB connInfo = new ConnInfo_RDB() ;
		updateFromJSON(connInfo, jo) ;
		return connInfo ;
	}
	
	protected static void updateFromJSON(ConnInfo_RDB aConnInfo
			, JSONObject aJo)
	{
		aConnInfo.setDescription(aJo.optString("description")) ;
		aConnInfo.setDbName(aJo.optString("dbName")) ;
		aConnInfo.setHost(aJo.optString("host")) ;
		aConnInfo.setPort(aJo.optInt("port", 0));
		aConnInfo.setUsername(aJo.optString("username")) ;
		aConnInfo.setPassword(PropertiesEx.deSecret(aJo.optString("password"))) ;
		aConnInfo.setParams(aJo.optString("params")) ;
	}
}
