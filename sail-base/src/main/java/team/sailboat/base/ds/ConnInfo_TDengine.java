package team.sailboat.base.ds;

import io.swagger.v3.oas.annotations.media.Schema;
import team.sailboat.base.def.DataSourceType;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;


@Schema(name="ConnInfo_TDengine" , description = "TDengine时序数据库的连接信息")
public class ConnInfo_TDengine extends ConnInfo_RDB
{
	
	@Override
	protected void checkSupport(DataSourceType aType)
	{
		Assert.isTrue(aType == DataSourceType.TDengine , "指定的数据源类型不是TDengine，而是%s" , aType.name()) ;
	}
	
	@Override
	public ConnInfo_TDengine clone()
	{
		ConnInfo_TDengine clone = new ConnInfo_TDengine() ;
		clone.copyFrom(this) ;
		return clone ;
	}
	
	public static ConnInfo_TDengine parse(String aConnInfo)
	{
		JSONObject jo = new JSONObject(aConnInfo) ;
		ConnInfo_TDengine connInfo = new ConnInfo_TDengine() ;
		ConnInfo_RDB.updateFromJSON(connInfo, jo);
		return connInfo ;
	}
}
