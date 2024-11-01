package team.sailboat.commons.fan.sys;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import team.sailboat.commons.fan.exec.CommonExecutor;
import team.sailboat.commons.fan.struct.Bytes;
import team.sailboat.commons.fan.text.XString;

public class Linux
{
	static final String sKey_DistributorID = "Distributor ID" ;
	static final String sKey_Description = "Description" ;
	static final String sKey_Release = "Release" ;
	static final String sKey_Codename = "Codename" ;
	
	static final String sCentOS = "CentOS" ;
	static final String sRedHat = "RedHat" ;
	static final String sRedHatEnterpriseServer = "RedHatEnterpriseServer" ;
	static final String sLinuxMint = "LinuxMint" ;
	
	static boolean sLsbInited = false ;
	static String sDistributorID ;
	static String sDescription ;
	static String sRelease ;
	static String sCodename ;
	
	static SysLoginBean sLoginBean ;
	
	static synchronized void initLsbInfo()
	{
		if(sLsbInited)
			return ;
		Process process = null ;
		InputStream ins = null ;
		try
		{
			process = Runtime.getRuntime().exec("lsb_release -a") ;
			ins = process.getInputStream() ;
			InputStreamReader reader = new InputStreamReader(ins) ;
			Bytes buf = new Bytes(512) ;
			byte[] buf0 = new byte[512] ;
			int len = -1 ;
			while((len=ins.read(buf0)) != -1)
			{
				buf.add(buf0, 0, len) ;
			}

			if(buf.mSize>0)
			{
				String info = new String(buf.mData, 0, buf.mSize) ;
				String separator = System.getProperty("line.separator") ;
				String[] splits = info.split(separator) ;
				for(String split : splits)
				{
					int index = split.indexOf(':') ;
					if(index != -1 && index<split.length()-1)
					{
						String key = split.substring(0, index) ;
						String value = split.substring(index+1).trim() ;
						if(sKey_DistributorID.equals(key))
							sDistributorID = value ;
						else if(sKey_Description.equals(key))
							sDescription = value ;
						else if(sKey_Release.equals(key))
							sRelease = value ;
						else if(sKey_Codename.equals(key))
							sCodename = value ;
					}
				}
			}
			reader.close() ;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			sLsbInited = true ;
			if(ins != null)
			{
				try
				{
					ins.close() ;
				}
				catch(Exception e)
				{}
			}
			if(process != null)
				process.destroy() ;
		}
	}
	
	public static SysLoginBean getCurrentSysLoginBean()
	{
		if(sLoginBean == null)
		{
			String user = System.getenv("USERNAME") ;
			if(user == null)
				user = System.getenv("USER") ;
			String sessionId = System.getenv("DISPLAY") ;
			sLoginBean = new SysLoginBean(user, sessionId, null, true) ;
		}
		return sLoginBean ;
	}
	
	public static String getDistributorID()
	{
		if(!sLsbInited)
			initLsbInfo() ;
		return sDistributorID;
	}
	
	public static String getDescription()
	{
		if(!sLsbInited)
			initLsbInfo() ;
		return sDescription;
	}
	
	public static String getRelease()
	{
		if(!sLsbInited)
			initLsbInfo() ;
		return sRelease;
	}
	
	public static String getCodename()
	{
		if(!sLsbInited)
			initLsbInfo() ;
		return sCodename;
	}
	
	public static boolean isCentOS()
	{
		return sCentOS.equals(getDistributorID()) ;
	}
	
	public static boolean isRedHat()
	{
		return getDistributorID().contains(sRedHat) ;
	}
	
	public static boolean isLinuxMint()
	{
		return sLinuxMint.equals(getDistributorID()) ;
	}
	
	public static void main(String[] args)
	{
		Linux.getCurrentSysLoginBean() ;
	}
	
	public static void delete(File aFile)
	{
		if(aFile.isDirectory())
		{
			try
			{
				Runtime.getRuntime().exec("rm -r "+aFile.getAbsolutePath()) ;
			}
			catch (IOException e)
			{
			}
		}
		else
			aFile.delete() ;
	}
	
	public static boolean ping(String aHost) throws IOException
	{
		//在linux系统上，ping操作有时会挂住，所以得采用以下实现
		try
		{
			Future<Boolean> future = CommonExecutor.exec(new PingCallable(aHost)) ;
			return future.get(2, TimeUnit.SECONDS).booleanValue() ;
		}
		catch (InterruptedException | ExecutionException | TimeoutException e)
		{
			return false ;
		}
	}
	
	static class PingCallable implements Callable<Boolean> , Closeable
	{
		String mHost ;
		Process mProcess ;
		
		PingCallable(String aHost)
		{
			mHost = aHost ;
		}

		@Override
		public Boolean call() throws Exception
		{
			String s = null;
			ProcessBuilder proBld = new ProcessBuilder("ping" , mHost) ;
			mProcess = proBld.start() ;
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
		    BufferedReader stdError = new BufferedReader(new InputStreamReader(mProcess.getErrorStream()));
		 
		    boolean begin = false ;
		    int count = 5 ;
		    while ((s = stdInput.readLine()) != null)
		    {
		    	if(begin)
		    	{
		    		if(s.toLowerCase().contains("ttl="))
		    		{
		    			close();
		    			return true ;
		    		}
		    		else
		    		{
		    			close();
		    			return false ;
		    		}
		    			
		    	}
		    	else if(s.toLowerCase().contains("ping"))
		    		begin = true ;
		    	if(count++>5)
		    		break ;
		    }
		    StringWriter writer = new StringWriter() ;
		    while ((s = stdError.readLine()) != null)
		    {
		      writer.write(s);
		      writer.write(XString.sLineSeparator);
		    }
		    close();
			throw new IOException(writer.toString()) ;
		}
		
		@Override
		public void close()
		{
			if(mProcess != null && mProcess.isAlive())
			{
				mProcess.destroyForcibly() ;
				mProcess = null ;
			}
		}
	}
}
