package team.sailboat.commons.fan.sys;

import java.io.File;
import java.io.IOException;

import team.sailboat.commons.fan.app.AppArgs;
import team.sailboat.commons.fan.lang.Assert;

public class AppRestarter
{

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		if(args != null && args.length>0)
		{
			AppArgs appArgs = new AppArgs(args) ;
			String cmd = appArgs.get("cmd") ;
			String runDir = appArgs.get("rundir") ;
			String delayStr = appArgs.get("delay") ;
			long delay = delayStr == null?1000:Long.parseLong(delayStr) ;
			Assert.notNull(cmd , "未指定-cmd 执行命令") ;
			Assert.notNull(runDir , "未指定-rundir 运行目录") ;
			try
			{
				try
				{
					Thread.sleep(delay);
				}
				catch (InterruptedException e)
				{}
				Runtime.getRuntime().exec(new String[] {cmd} , null , new File(runDir)) ;
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 
	 * @param aCmd
	 * @param aRunDir
	 * @param aDelay		延时执行，单位毫秒
	 * @throws IOException
	 */
	public static void restart(String aCmd , File aRunDir , long aDelay) throws IOException
	{
		Runtime.getRuntime().exec(new String[] {String.format("java -classpath plugins/com.cimstech.xfront.common_1.0.0.jar com.cimstech.xfront.common.sys.AppRestarter"
				+ " -cmd \"%1$s\" -rundir %2$s -delay %3$d" , aCmd , aRunDir.getAbsolutePath() , aDelay)} 
				, null , aRunDir) ;
	}
}
