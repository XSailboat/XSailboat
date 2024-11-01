package team.sailboat.base.ds;

import io.swagger.v3.oas.annotations.media.Schema;
import team.sailboat.base.def.DataSourceType;
import team.sailboat.commons.fan.collection.PropertiesEx;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.XString;


public abstract class ConnInfo_FileSystem extends ConnInfo_Pswd
{
	String mHost ;
	
	int mPort = 22 ;
	
	String mPath ;
	
	String mUsername ;
	
	protected ConnInfo_FileSystem(String aType)
	{
		super(aType) ;
	}
	
	public abstract String getProtocol() ;
	
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
		return mPort == 0 ? 22 : mPort ;
	}
	
	@Schema(description = "工作目录")
	public String getPath()
	{
		return mPath ;
	}
	public void setPath(String aPath)
	{
		mPath = aPath ;
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
	}
	
	@Override
	public String getConnURI(DataSourceType aType)
	{
		Assert.isTrue(aType.isFileSystem() , "指定的数据源类型不是文件系统，而是%s" , aType.name()) ;
		return XString.splice(aType.getProtocol() , "://" , mHost , ":"
				, mPort , mPath) ;
	}
	
	@Override
	protected boolean update(ConnInfo aConnInfo , boolean aPartially)
	{
		boolean changed = super.update(aConnInfo, aPartially) ;
		if(aConnInfo instanceof ConnInfo_FileSystem)
		{
			boolean resourceChange = false ;
			ConnInfo_FileSystem connInfo = (ConnInfo_FileSystem)aConnInfo ;
			if((!aPartially || XString.isNotEmpty(connInfo.mPath)) && JCommon.unequals(mPath, connInfo.mPath))
			{
				mPath = connInfo.mPath ;
				changed = true ;
			}
			if((!aPartially || XString.isNotEmpty(connInfo.mHost)) && JCommon.unequals(mHost, connInfo.mHost))
			{
				mHost = connInfo.mHost ;
				changed = true ;
				resourceChange = true ;
			}
			if((!aPartially || XString.isNotEmpty(connInfo.mUsername)) && JCommon.unequals(mUsername, connInfo.mUsername))
			{
				mUsername = connInfo.mUsername ;
				changed = true ;
				resourceChange = true ;
			}
			if((!aPartially || connInfo.mPort > 0) && connInfo.mPort != mPort)
			{
				mPort = connInfo.mPort ;
				changed = true ;
				resourceChange = true ;
			}
			if(resourceChange)
				closeResource();
		}
		return changed ;
	}
	
	
	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		return super.setTo(aJSONObj)
				.put("path" , mPath)
				.put("host" , mHost)
				.put("port" , mPort)
				.put("username", mUsername)
				;
	}
	
	@Override
	public boolean equals(Object aObj)
	{
		if(!super.equals(aObj))
			return false ;
		ConnInfo_FileSystem connInfo = (ConnInfo_FileSystem)aObj ;
		return super.equals(aObj) 
				&& JCommon.equals(connInfo.mDescription , mDescription)
				&& JCommon.equals(connInfo.mHost , mHost)
				&& JCommon.equals(connInfo.mPath, mPath)
				&& JCommon.equals(connInfo.mPort , mPort)
				&& JCommon.equals(connInfo.mUsername , mUsername) ;
	}
	
	protected static ConnInfo_FileSystem parse(String aConnInfoStr , ConnInfo_FileSystem aConnInfo)
	{
		JSONObject jo = new JSONObject(aConnInfoStr) ;
		aConnInfo.setDescription(jo.optString("description")) ;
		aConnInfo.setPath(jo.optString("path")) ;
		aConnInfo.setHost(jo.optString("host")) ;
		aConnInfo.setPort(jo.optInt("port", 0));
		aConnInfo.setUsername(jo.optString("username")) ;
		aConnInfo.setPassword(PropertiesEx.deSecret(jo.optString("password"))) ;
		return aConnInfo ;
	}
}
