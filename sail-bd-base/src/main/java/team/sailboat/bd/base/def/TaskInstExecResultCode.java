package team.sailboat.bd.base.def;

import gnu.trove.map.TIntObjectMap;
import team.sailboat.commons.fan.collection.XC;

public enum TaskInstExecResultCode
{
	OK(200 , RunStatus.success , "成功") ,
	
	/**
	 * 申请资源失败
	 */
	ApplyResourceFaild(503 , RunStatus.failure , "申请执行资源失败") ,
	
	/**
	 * 部署应用失败
	 */
	DeployAppFaild(506 , RunStatus.failure , "在容器中部署应用失败") , 
	
	UnReportResult(507 , RunStatus.failure , "没有收到运行结果状态报告") ,
	
	TaskException(520 , RunStatus.failure , "任务执行过程中出现异常") ,
	
	UnsupportRunNodeType(400 , RunStatus.failure , "不支持运行的任务节点类型")
	;
	
	int mCode ;
	
	RunStatus mStatus ;
	
	String mDescription ;
	
	private TaskInstExecResultCode(int aCode , RunStatus aStatus , String aDescription)
	{
		mCode = aCode ;
		mStatus = aStatus ;
		mDescription = aDescription ;
	}
	
	public int getCode()
	{
		return mCode ;
	}
	
	public RunStatus getStatus()
	{
		return mStatus ;
	}
	
	public String getDescription()
	{
		return mDescription;
	}
	
	public boolean isFailure()
	{
		return mCode >= 300 ;
	}
	
	private static final TIntObjectMap<TaskInstExecResultCode> sCodeMap = XC.intKeyMap() ; 
	
	static 
	{
		for(TaskInstExecResultCode code : values())
			sCodeMap.put(code.getCode() , code) ;
	}
	
	public static TaskInstExecResultCode of(int aCode)
	{
		return sCodeMap.get(aCode) ;
	}
}
