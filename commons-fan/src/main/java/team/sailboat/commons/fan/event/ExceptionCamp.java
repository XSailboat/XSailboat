package team.sailboat.commons.fan.event;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.exec.CommonExecutor;
import team.sailboat.commons.fan.log.Log;
import team.sailboat.commons.fan.struct.IXDataBag;
import team.sailboat.commons.fan.struct.XDataBag;

public class ExceptionCamp implements IXDataBag
{
	protected IExceptionHandler[] mHandlers ;
	XDataBag mDataBag = new XDataBag() ;
	
	public ExceptionCamp()
	{}

	public void addExceptionHandler(IExceptionHandler aHandler)
	{
		if(mHandlers == null)
			mHandlers = new IExceptionHandler[]{aHandler} ;
		else if(!XC.contains(mHandlers , aHandler))
			mHandlers = XC.merge(mHandlers , aHandler) ; 
	}
	
	public IExceptionHandler[] getExceptionHandlers()
	{
		return mHandlers ;
	}
	
	public IExceptionHandler getFirstExceptionHandlerByClass(Class<?> aClass)
	{
		if(mHandlers != null)
		{
			for(IExceptionHandler handler : mHandlers)
				if(aClass.isAssignableFrom(handler.getClass()))
					return handler ;
		}
		return null ;
	}
	
	public void removeExceptionHandler(IExceptionHandler aHandler)
	{
		if(mHandlers != null && aHandler != null)
			mHandlers = XC.remove(mHandlers, aHandler) ;
	}
	
	public void handle(Throwable aE)
	{
		if(mHandlers == null)
			return ;
		for(IExceptionHandler handler : mHandlers)
		{
			try
			{
				if(handler.isAsynchronous())
				{
					CommonExecutor.exec(new HandleRun(handler, aE)) ; 
				}
				else
				{
					handler.handle(aE) ;
				}
			}
			catch(Throwable e)
			{
				Log.error(ExceptionCamp.class , e) ;
			}
		}
	}
	
	/**
	 * 提示消息
	 * @param aMsg
	 * @param aType
	 */
	public void promptMessage(String aMsg , int aType)
	{
		if(mHandlers == null)
			return ;
		for(IExceptionHandler handler : mHandlers)
		{
			if(handler.isAsynchronous())
			{
				CommonExecutor.exec(new PromptRun(handler , aMsg, aType)) ; 
			}
			else
			{
				try
				{
					handler.prompt(aMsg, aType);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 
	 * @param aMsg
	 * @param aType		取值为IMessageType
	 */
	public void log(String aMsg , int aType)
	{
		if(mHandlers == null)
			return ;
		for(IExceptionHandler handler : mHandlers)
		{
			if(handler.isAsynchronous())
			{
				CommonExecutor.exec(new LogRun(handler , aMsg, aType)) ; 
			}
			else
			{
				try
				{
					handler.log(aMsg, aType);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	public void error(String aMsg)
	{
		log(aMsg, IMessageType.sError) ;
	}
	
	public void warn(String aMsg)
	{
		log(aMsg, IMessageType.sWarning) ;
	}
	
	public void info(String aMsg)
	{
		log(aMsg, IMessageType.sInfo) ;
	}
	
	@Override
	public Object getData()
	{
		return mDataBag.getData() ;
	}
	
	@Override
	public Object getData(Object aKey)
	{
		return mDataBag.getData(aKey) ;
	}
	
	@Override
	public void setData(Object aData)
	{
		mDataBag.setData(aData) ;
	}
	
	@Override
	public void setData(Object aKey, Object aData)
	{
		mDataBag.setData(aKey , aData) ;
	}
	
	@Override
	public int getDataEntryAmount()
	{
		return mDataBag.getDataEntryAmount() ;
	}
	
	static class HandleRun implements Runnable
	{
		IExceptionHandler mHandler ;
		Throwable mException ;
		
		HandleRun(IExceptionHandler aHandler , Throwable aE)
		{
			mHandler = aHandler ;
			mException = aE ;
		}

		@Override
		public void run()
		{
			mHandler.handle(mException) ;
		}
	}
	
	static class PromptRun implements Runnable
	{
		IExceptionHandler mHandler ;
		String mMsg ;
		int mType ;
		
		public PromptRun(IExceptionHandler aHandler , String aMsg , int aType)
		{
			mHandler = aHandler ;
			mMsg = aMsg ;
			mType = aType ;
		}
		
		@Override
		public void run()
		{
			mHandler.prompt(mMsg, mType);
		}
	}
	
	static class LogRun implements Runnable
	{
		IExceptionHandler mHandler ;
		String mMsg ;
		int mType ;
		
		public LogRun(IExceptionHandler aHandler , String aMsg , int aType)
		{
			mHandler = aHandler ;
			mMsg = aMsg ;
			mType = aType ;
		}
		
		@Override
		public void run()
		{
			mHandler.log(mMsg, mType);
		}
	}
}
