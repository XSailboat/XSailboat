package team.sailboat.base.def;

import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;

public class Msg implements ToJSONObject
{
	Template mTemplate ;
	JSONObject mDataJo ;
	
	private Msg(Template aTemplate)
	{
		mTemplate = aTemplate ;
	}
	
	public Template getTemplate()
	{
		return mTemplate;
	}
	
	public Msg dataItem(String aKey , String aValue)
	{
		if(mDataJo == null)
			mDataJo = new JSONObject() ;
		mDataJo.put(aKey, aValue) ;
		return this ;
	}
	
	public Msg dataItem(String aKey , long aValue)
	{
		if(mDataJo == null)
			mDataJo = new JSONObject() ;
		mDataJo.put(aKey, aValue) ;
		return this ;
	}
	
	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		if(mTemplate != null)
		{
			aJSONObj.put("code", mTemplate.mCode)
				.put("name", mTemplate.name())
				.put("description", mTemplate.getDescription());
		}
		if(mDataJo != null)
		{
			aJSONObj.put("data", mDataJo) ;
		}
		return aJSONObj ;
	}
	
	@Override
	public String toString()
	{
		return toJSONString() ;
	}
	
	/**
	 * 10代表的是任务运行相关的消息
	 * 5XX代表服务端错误
	 *
	 * @author yyl
	 * @since 2021年5月14日
	 */
	public static enum Template
	{

		OK_RUN_TASK__ACCEPT(10201 , "接受执行任务的请求，容器申请已经提交，进入启动流程。") ,
		
		DENY_RUN_TASK__FULL(10510 , "拒绝执行任务！因为任务数量达到上限。") ,
		DENY_RUN_TASK__ALREADY(10511 , "拒绝执行任务！因为任务已经正在运行。") ,
		FAILURE_RUN_TASK_APPLY_CONTAINER_TIMEOUT(10520, "执行结果未知！申请容器超时。") ,
		FAILURE_RUN_TASK_EXCEPTION_OCCUR(10521, "执行失败！在申请容器的过程中出现了异常。") ,
	
		;
		
		
		int mCode ;
		String mDesc ;
		
		private Template(int aCode , String aDesc)
		{
			mCode = aCode ;
			mDesc = aDesc ;
		}
		
		public int getCode()
		{
			return mCode;
		}
		
		public String getDescription()
		{
			return mDesc ;
		}

		public Msg asMsg()
		{
			return new Msg(this) ;
		}
	}
}
