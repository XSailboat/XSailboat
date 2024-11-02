package team.sailboat.bd.base.yarn;

import team.sailboat.base.def.WorkEnv;
import team.sailboat.bd.base.def.Application;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;

public enum YarnQueueUsage implements ToJSONObject
{
	FlinkDev("开发环境flink集群" , WorkEnv.dev , Application.SailFlink) ,
	FlinkProd("生产环境flink集群" , WorkEnv.prod , Application.SailFlink) ,
	XTaskDev("开发环境XTask集群" , WorkEnv.dev , Application.XTask) ,
	XTaskProd("生产环境XTask集群" , WorkEnv.prod , Application.XTask) ,
	General("通用" , null)
	;
	
	String mDescription ;
	WorkEnv mWorkEnv ;
	Application[] mApps ;
	
	private YarnQueueUsage(String aDescription , WorkEnv aWorkEnv , Application... aApps)
	{
		mDescription = aDescription ;
		mWorkEnv = aWorkEnv ;
		mApps = aApps ;
	}
	
	public String getDescription()
	{
		return mDescription;
	}
	
	public boolean isCanApplyTo(WorkEnv aWorkEnv , Application aApp)
	{
		return (mWorkEnv == null || mWorkEnv == aWorkEnv)
				&& (XC.isEmpty(mApps) || XC.contains(mApps, aApp)) ;
	}
	
	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		return aJSONObj.put("description", mDescription)
				.put("name" , name())
				.put("workEnv" , mWorkEnv)
				;
	}
}
