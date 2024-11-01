package team.sailboat.commons.fan.event;

import team.sailboat.commons.fan.lang.Assert;

public interface IStatus
{
	public static final int sReady = 0 ;
	public static final int sRunnging = 1 ;
	public static final int sPaused = 2 ;
	public static final int sInterrupted = 3 ;
	public static final int sFinished = 4 ;
	public static final int sFinalizing = 5 ;
	public static final int sFinalized = 6 ;
	public static final int sStarting = 7 ;
	public static final int sUnstart = 8 ;
	
	static final String[] sEnNames = new String[] {"ready" , "running" , "paused" , "interrupted" , "finished"
			, "finalizing" , "finalized" , "starting" , "unstart"} ;
	
	static final String[] sCnNames = new String[] {"就绪" , "执行中" , "已暂停" , "已中断" , "已完成" 
			, "收尾中" , "已收尾" , "启动中" , "未启动"} ;
	
	public static String getEnName(int aSVal)
	{
		Assert.betweenL_R(0, sEnNames.length, aSVal) ; 
		return sEnNames[aSVal] ;
	}
	
	public static String getCnName(int aSVal)
	{
		Assert.betweenL_R(0, sCnNames.length, aSVal) ; 
		return sCnNames[aSVal] ;
	}
}
