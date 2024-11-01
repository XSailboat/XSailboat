package team.sailboat.base.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import lombok.AllArgsConstructor;
import lombok.Data;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.excep.ExceptionAssist;
import team.sailboat.commons.fan.exec.CommonExecutor;
import team.sailboat.commons.fan.infc.ESupplier;
import team.sailboat.commons.fan.text.XString;

public class ActionTimeoutGuard
{
	static ActionTimeoutGuard sInstance ;
	
	public static ActionTimeoutGuard getInstance()
	{
		if(sInstance == null)
		{
			synchronized (ActionTimeoutGuard.class)
			{
				if(sInstance == null)
					sInstance = new ActionTimeoutGuard() ;
			}
		}
		return sInstance ;
	}
	
	final BlockingQueue<SendTask> tasks = XC.blockingQueue_linked() ;
	final AtomicLong mCounter = new AtomicLong() ;
	
	private ActionTimeoutGuard()
	{
		CommonExecutor.execInSelfThread(()->{
			while(true)
			{
				try
				{
					SendTask task = tasks.take() ;
					String msg = task.toString() ;
					MsgSender.sendOM(3 , msg, null, task.getMsg() , task.getMcId()) ;
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}, "动作超时消息发送") ;
	}
	
	public long actionBegin()
	{
		return System.currentTimeMillis() ;
	}
	
	public void actionEnd(long aBeginTime , String aMcId , Class<?> aClass , int aTimeoutMs
			, String aMsg , Object...aArgs)
	{
		long duration = System.currentTimeMillis() - aBeginTime ;
		if(duration > aTimeoutMs)
		{
			tasks.add(new SendTask(aMcId , new Exception() , aClass, aTimeoutMs, aMsg, aArgs
					, duration)) ;
		}
	}
	
	public <T, E extends Throwable>  T  doAction(ESupplier<T , E> aAction , String aMcId , int aTimeoutMs
			, Class<?> aClass
			, String aMsg , Object...aArgs) throws E
	{
		long beginTime = actionBegin() ;
		T result = aAction.get() ;
		actionEnd(beginTime , aMcId , aClass , aTimeoutMs , aMsg , aArgs) ;
		return result ;
	}
	
	@Data
	@AllArgsConstructor
	static class SendTask
	{
		String mcId ;
		Exception e ;
		Class<?> clazz ;
		int timeoutMs ;
		String msg ;
		Object[] args ;
		long duration ;
		
		
		public String toString()
		{
			return XString.msgFmt(msg, args)
					+ " \t耗时：" + duration + "ms（超时时长："+timeoutMs+"）"
					+ " \t位置："+ExceptionAssist.getLocation(clazz, e)
					+ " \t模式类id：" + mcId ;
		}
	}
}
