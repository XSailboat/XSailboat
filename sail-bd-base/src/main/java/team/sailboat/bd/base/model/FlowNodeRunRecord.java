package team.sailboat.bd.base.model;

import team.sailboat.bd.base.def.RunStatus;
import team.sailboat.commons.fan.dpa.anno.BForwardMethod;
import team.sailboat.commons.fan.dpa.anno.BReverseMethod;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;
import team.sailboat.commons.fan.lang.JCommon;

/**
 * 节点的运行结果记录
 *
 * @author yyl
 * @since 2021年6月19日
 */
public class FlowNodeRunRecord implements ToJSONObject
{
	/**
	 * 成功/失败 
	 */
	RunStatus mStatus ;
	
	/**
	 * 运行时节点的lastEditTime
	 */
	Long mContentVersion ;
	
	/**
	 * 执行id，凭此获取此次执行的日志和结果
	 */
	String mExecId ;
	
	/**
	 * 当mStatus == Running时，上HBase上获取状态的时间		<br>
	 * 此字段不需要序列化出去
	 */
	long mLastRefreshTime ;
	
	
	public FlowNodeRunRecord()
	{
	}
	
	public FlowNodeRunRecord(long aContentVersion , String aExecId , RunStatus aRunStatus)
	{
		mContentVersion = aContentVersion ;
		mExecId = aExecId ;
		mStatus = aRunStatus ;
	}
	
	public void setStatus(RunStatus aStatus)
	{
		mStatus = aStatus;
	}
	public RunStatus getStatus()
	{
		return mStatus ;
	}

	public Long getContentVersion()
	{
		return mContentVersion;
	}

	public void setContentVersion(Long aVersion)
	{
		mContentVersion = aVersion;
	}

	public String getExecId()
	{
		return mExecId;
	}

	public void setExecId(String aExecId)
	{
		mExecId = aExecId;
	}
	
	
	public void setLastRefreshTime(long aLastRefreshTime)
	{
		mLastRefreshTime = aLastRefreshTime;
	}
	public long getLastRefreshTime()
	{
		return mLastRefreshTime;
	}
	
	@Override
	public boolean equals(Object aObj)
	{
		if(aObj == null)
			return false ;
		if(!(aObj instanceof FlowNodeRunRecord))
			return false ;
		FlowNodeRunRecord other = (FlowNodeRunRecord)aObj ;
		return mStatus == other.mStatus
				&& JCommon.equals(mContentVersion, other.mContentVersion)
				&& JCommon.equals(mExecId, other.mExecId) ;
	}

	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		return aJSONObj.put("status", mStatus.name())
				.put("contentVersion" , mContentVersion)
				.put("execId" , mExecId) ;
	}
	
	public static FlowNodeRunRecord parse(JSONObject aJObj)
	{
		FlowNodeRunRecord rcd = new FlowNodeRunRecord() ;
		rcd.setStatus(RunStatus.valueOf(aJObj.optString("status")));
		rcd.setExecId(aJObj.optString("execId")) ;
		rcd.setContentVersion(aJObj.optLong_0("contentVersion")) ;
		return rcd ;
	}
	
	@BForwardMethod
	public static Object forward(FlowNodeRunRecord aRecord)
	{
		return aRecord==null?null:aRecord.toJSONString() ;
	}
	
	@BReverseMethod
	public static FlowNodeRunRecord reverse(Object aSource)
	{
		return aSource == null?null:parse(new JSONObject(aSource.toString())) ;
	}
}
