package team.sailboat.commons.fan.sys;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import team.sailboat.commons.fan.serial.StreamAssist;
import team.sailboat.commons.fan.text.XString;

/**
 * Windows的Cmd
 * 
 * @author yl
 * @version 1.0 
 * @since 2015-10-10
 */
public class Cmd implements Closeable
{

	Process mProcess ;
	
	public Cmd()
	{
		try
		{
			mProcess = Runtime.getRuntime().exec(new String[]{"cmd"}) ;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	protected void clearInputStream()
	{
		
	}
	
	/**
	 * 等待1秒之后的输出结果
	 * @param aCmd
	 * @return
	 * @throws Error
	 * @throws IOException
	 */
	public String exec(String aCmd) throws Error , IOException
	{
		return exec(aCmd, 1000) ;
	}
	
	/**
	 * 
	 * @param aCmd
	 * @param aWaitInMillSecs
	 * @return
	 * @throws Error
	 * @throws IOException
	 */
	public String exec(String aCmd , int aWaitInMillSecs) throws Error , IOException
	{
		OutputStreamWriter writer = new OutputStreamWriter(mProcess.getOutputStream()) ;
		InputStreamReader errReader = new InputStreamReader(mProcess.getErrorStream()) ;
		InputStreamReader reader = new InputStreamReader(mProcess.getInputStream()) ;
		writer.write(aCmd+XString.sLineSeparator) ;
		writer.flush() ;
		if(aWaitInMillSecs<=0)
			aWaitInMillSecs = 1000 ;
		String result = StreamAssist.readAll(errReader , aWaitInMillSecs) ;
		if(!result.isEmpty())
		{
			throw new Error(result) ;
		}
		else
		{
			return StreamAssist.readAll(reader , aWaitInMillSecs) ;
		}
	}
	
	public void dispose()
	{
		close();
	}
	
	@Override
	public void close()
	{
		if(mProcess != null)
		{
			mProcess.destroy() ;
			mProcess = null ;
		}
	}
	
	public static String execAlone(String aCmd) throws IOException, Error, InterruptedException
	{
		Process process = null ;
		try
		{
			process = Runtime.getRuntime().exec(new String[] {aCmd}) ;
			OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream()) ;
			InputStreamReader errReader = new InputStreamReader(process.getErrorStream()) ;
			InputStreamReader reader = new InputStreamReader(process.getInputStream()) ;
			writer.write(aCmd+XString.sLineSeparator) ;
			writer.flush() ;
	
			if(0 == process.waitFor())
			{
				return StreamAssist.readAll(reader) ;
			}
			else
			{
				throw new Error(StreamAssist.readAll(errReader)) ;
			}
		}
		finally
		{
			if(process != null)
				process.destroy();
		}
	}
}
