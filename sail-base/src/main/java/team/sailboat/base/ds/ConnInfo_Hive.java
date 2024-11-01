package team.sailboat.base.ds;

import io.swagger.v3.oas.annotations.media.Schema;
import team.sailboat.commons.fan.collection.PropertiesEx;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.XString;


@Schema(name="ConnInfo_Hive" , description="Hive数据仓库的连接信息")
public class ConnInfo_Hive extends ConnInfo_RDB
{
	
	String mHdfsUrl ;
	
	String mHdfsParams ;
	
	public ConnInfo_Hive()
	{
		super("ConnInfo_Hive") ;
	}
	
	@Schema(description = "hdfs的连接信息。目前只能是hdfs的web api的url，支持多地址，用“;”分隔")
	public String getHdfsUrl()
	{
		return mHdfsUrl;
	}
	public void setHdfsUrl(String aHdfsUrl)
	{
		mHdfsUrl = aHdfsUrl;
	}
	
	@Schema(description = "hdfs的连接参数。目前只能是properties格式（键值形式）")
	public String getHdfsParams()
	{
		return mHdfsParams;
	}
	public void setHdfsParams(String aHdfsParams)
	{
		mHdfsParams = aHdfsParams;
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
	protected boolean update(ConnInfo aConnInfo , boolean aPartially)
	{
		boolean changed = super.update(aConnInfo, aPartially) ;
		if(aConnInfo instanceof ConnInfo_Hive)
		{
			ConnInfo_Hive connInfo = (ConnInfo_Hive)aConnInfo ;
			if((!aPartially || XString.isNotEmpty(connInfo.mHdfsUrl)) && JCommon.unequals(connInfo.mHdfsUrl , mHdfsUrl))
			{
				mHdfsUrl = connInfo.mHdfsUrl ;
				changed = true ;
			}
			if((!aPartially || XString.isNotEmpty(connInfo.mHdfsParams)) && JCommon.unequals(connInfo.mHdfsParams , mHdfsParams))
			{
				mHdfsParams = connInfo.mHdfsParams ;
				changed = true ;
			}
		}
		return changed ;
	}
	
	
	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		return super.setTo(aJSONObj)
				.put("hdfsUrl" , mHdfsUrl)
				.put("hdfsParams" , mHdfsParams) ;
	}
	
	@Override
	public boolean equals(Object aObj)
	{
		if(!super.equals(aObj))
			return false ;
		ConnInfo_Hive connInfo = (ConnInfo_Hive)aObj ;
		return JCommon.equals(connInfo.mHdfsUrl , mHdfsUrl)
				&& JCommon.equals(connInfo.mHdfsParams , mHdfsParams) ;
	}
	
	@Override
	public ConnInfo_Hive clone()
	{
		ConnInfo_Hive clone = new ConnInfo_Hive() ;
		clone.copyFrom(this) ;
		return clone ;
	}
	
	public static ConnInfo_Hive parse(String aConnInfo)
	{
		JSONObject jo = new JSONObject(aConnInfo) ;
		ConnInfo_Hive connInfo = new ConnInfo_Hive() ;
		connInfo.setDescription(jo.optString("description")) ;
		connInfo.setDbName(jo.optString("dbName")) ;
		connInfo.setHost(jo.optString("host")) ;
		connInfo.setPort(jo.optInt("port", 0));
		connInfo.setUsername(jo.optString("username")) ;
		connInfo.setPassword(PropertiesEx.deSecret(jo.optString("password"))) ;
		connInfo.setParams(jo.optString("params")) ;
		connInfo.setHdfsUrl(jo.optString("hdfsUrl"));
		connInfo.setHdfsParams(jo.optString("hdfsParams"));
		return connInfo ;
	}
}
