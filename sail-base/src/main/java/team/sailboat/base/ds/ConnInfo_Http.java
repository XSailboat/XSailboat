package team.sailboat.base.ds;

import io.swagger.v3.oas.annotations.media.Schema;
import team.sailboat.base.def.DataSourceType;
import team.sailboat.commons.fan.collection.PropertiesEx;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.XString;


@Schema(name="ConnInfo_Http" , description="Http(s)的连接信息")
public class ConnInfo_Http extends ConnInfo_Pswd
{
	String mHost ;
	
	int mPort ;
	
	String mContextPath ;
	
	ApiAuthType mApiAuthType ;
	
	String mUsername ;
	
	public ConnInfo_Http()
	{
		super("ConnInfo_Http") ;
	}
	
	protected ConnInfo_Http(String aType)
	{
		super(aType) ;
	}
	
	@Schema(description = "Context Path")
	public String getContextPath()
	{
		return mContextPath;
	}
	public void setContextPath(String aContextPath)
	{
		mContextPath = aContextPath;
	}
	
	@Schema(description = "API认证方式")
	public ApiAuthType getApiAuthType()
	{
		return mApiAuthType;
	}
	public void setApiAuthType(ApiAuthType aApiAuthType)
	{
		mApiAuthType = aApiAuthType;
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
	
	@Override
	public void checkForCreate()
	{
		// 1. hostName不能为空不能为空
		Assert.notEmpty(mHost , "主机名不能为空") ;
		// 2. port
		Assert.isTrue(mPort > 0 , "必需指定端口");
	}
	
	@Override
	public String getConnURI(DataSourceType aType)
	{
		Assert.isTrue(aType.isHttpService() , "指定的数据源类型不是HttpService，而是%s" , aType.name()) ;
		StringBuilder strBld = new StringBuilder(aType.getProtocol())
				.append("://")
				.append(mHost).append(":").append(mPort)
				.append("/").append(mContextPath) ;
		return strBld.toString() ;
	}
	
	@Override
	protected boolean update(ConnInfo aConnInfo , boolean aPartially)
	{
		boolean changed = super.update(aConnInfo , aPartially);
		if(aConnInfo instanceof ConnInfo_Http)
		{
			ConnInfo_Http connInfo = (ConnInfo_Http)aConnInfo ;
			boolean resourceChanged = false ;
			if((!aPartially || XString.isNotEmpty(connInfo.mContextPath))
					&& JCommon.unequals(connInfo.mContextPath ,mContextPath))
			{
				mContextPath = connInfo.mContextPath ;
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
			if((!aPartially || mApiAuthType != null) && mApiAuthType != connInfo.mApiAuthType)
			{
				mApiAuthType = connInfo.mApiAuthType ;
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
				.put("contextPath" , mContextPath)
				.put("host" , mHost)
				.put("port" , mPort)
				.put("username", mUsername)
				.put("apiAuthType" , mApiAuthType)
				;
	}
	
	@Override
	public boolean equals(Object aObj)
	{
		if(!super.equals(aObj))
			return false ;
		ConnInfo_Http connInfo = (ConnInfo_Http)aObj ;
		return JCommon.equals(connInfo.mContextPath , mContextPath)
				&& JCommon.equals(connInfo.mDescription , mDescription)
				&& JCommon.equals(connInfo.mHost , mHost)
				&& JCommon.equals(connInfo.mApiAuthType , mApiAuthType)
				&& JCommon.equals(connInfo.mPort , mPort)
				&& JCommon.equals(connInfo.mUsername , mUsername) ;
	}
	
	@Override
	public ConnInfo_Http clone()
	{
		ConnInfo_Http clone = new ConnInfo_Http() ;
		clone.copyFrom(this) ;
		return clone ;
	}
	
	public static ConnInfo_Http parse(String aConnInfo)
	{
		JSONObject jo = new JSONObject(aConnInfo) ;
		ConnInfo_Http connInfo = new ConnInfo_Http() ;
		connInfo.setDescription(jo.optString("description")) ;
		connInfo.setContextPath(jo.optString("contextPath")) ;
		connInfo.setHost(jo.optString("host")) ;
		connInfo.setPort(jo.optInt("port", 80));
		connInfo.setUsername(jo.optString("username")) ;
		connInfo.setPassword(PropertiesEx.deSecret(jo.optString("password"))) ;
		connInfo.setApiAuthType(jo.optEnum("apiAuthType" , ApiAuthType.class)) ;
		return connInfo ;
	}
}
