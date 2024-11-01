package team.sailboat.commons.fan.sys;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.csv.CsvReader;
import team.sailboat.commons.fan.struct.Bytes;
import team.sailboat.commons.fan.text.PlainTable;
import team.sailboat.commons.fan.text.XString;

public class Windows
{
	static boolean sVerInited = false ;
	static float sKernelVer = 0 ;
	static String sExactName ;
	
	public static boolean isWinNT_3_51()
	{
		if(!sVerInited)
			initVerInfo() ;
		return sKernelVer>=3.51 && sKernelVer<4 ;
	}
	
	public static boolean isWinNT_4_0()
	{
		if(!sVerInited)
			initVerInfo() ;
		return sKernelVer>=4 && sKernelVer<5 ;
	}
	
	public static boolean isWin2000()
	{
		if(!sVerInited)
			initVerInfo() ;
		return sKernelVer>=5 && sKernelVer<5.1 ;
	}
	
	public static boolean isWinXP()
	{
		if(!sVerInited)
			initVerInfo() ;
		return sKernelVer>=5.1 && sKernelVer<5.2 ;
	}
	
	public static boolean isWinServer2003()
	{
		if(!sVerInited)
			initVerInfo() ;
		return sKernelVer>=5.2 && sKernelVer<6 ;
	}
	
	public static boolean isWinVista_Or_Server2008()
	{
		if(!sVerInited)
			initVerInfo() ;
		return sKernelVer>=6 && sKernelVer<6.1 ;
	}
	
	public static boolean isWin7_Or_Server2008R2()
	{
		if(!sVerInited)
			initVerInfo() ;
		return sKernelVer>=6.1 && sKernelVer<6.3 ;
	}
	
	public static boolean isWin8()
	{
		if(!sVerInited)
			initVerInfo() ;
		return sKernelVer>=6.3 && sKernelVer<10 ;
	}
	
	public static boolean isWin10()
	{
		if(!sVerInited)
			initVerInfo() ;
		return sKernelVer>=10 ;
	}
	
	static synchronized void initVerInfo()
	{
		if(sVerInited)
			return ;
		Process process = null ;
		InputStream ins = null ;
		try
		{
			process = Runtime.getRuntime().exec(new String[]{"cmd /c ver"}) ;
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
				int begin = -1 ;
				int end = -1 ;
				for(int i=0 ; i<info.length() ; i++)
				{
					char ch = info.charAt(i) ;
					if(begin == -1)
					{
						if(ch>='0' && ch<='9')
							begin = i ;
					}
					else if(!(ch>='0' && ch<='9' || ch=='.'))
					{
						end = i ;
						break ;
					}
				}
				if(begin != -1 && end == -1)
					end = info.length() ;
				if(begin != -1 && end != -1)
				{
					String ver = info.substring(begin , end) ;
					char[] chs = new char[ver.length()] ;
					int count = 0 ;
					boolean doted = false ;
					for(int i=0 ; i<chs.length ; i++)
					{
						char ch = ver.charAt(i) ;
						if(ch == '.')
						{
							if(!doted)
							{
								doted = true ;
								chs[count++] = ch ;
							}
						}
						else
							chs[count++] = ch ;
					}
					sKernelVer = Float.parseFloat(new String(chs, 0, count)) ;
				}
			}
			reader.close() ;
			if(sKernelVer == 0)
				throw new IllegalStateException("无法识别windows系统版本") ;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			sVerInited = true ;
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
	
	public static String getExactName() throws IOException, Error, InterruptedException
	{
		if(sExactName == null)
		{
			try(Cmd cmd = new Cmd())
			{
				String result = Cmd.execAlone("systeminfo /FO CSV") ;
				try(CsvReader reader = new CsvReader(new StringReader(result)))
				{
					if(reader.readHeaders())
					{
						int index = reader.getIndex("OS 名称") ;
						if(index != -1)
						{
							if(reader.readRecord())
								sExactName = reader.get(index) ;
							else
								throw new Error("返回的结果无法解析："+result) ;
						}
						else
							throw new Error("没有找到“OS 名称”："+result) ;
					}
					else
						throw new Error("返回的结果无法解析："+result) ;
				}
			}
		}
		return sExactName ;
	}
	
	/**
	 * 取得当前登录的会话信息
	 * @return
	 */
	public static SysLoginBean getCurrentSysLoginBean()
	{
		Process process = null ;
		InputStream ins = null ;
		try
		{
			process = Runtime.getRuntime().exec(new String[]{"cmd /c query user"}) ;
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
				info = XString.replaceAll("(?:\\d{1,2})( )(?:\\d{1,2}\\:)"
						, info , "%_") ;
				PlainTable table = PlainTable.build(info);
				String[] column = table.getColumn(0) ;
				int index = XC.indexOf(column, obj->((String)obj).charAt(0) == '>') ;
				if(index != -1)
				{
					String[] row = table.getRow(index) ;
					if(row != null)
					{
						return new SysLoginBean(row[0].substring(1)
								, row[2] , row[5] , "运行中".equals(row[3])) ;
					}
				}
			}
			reader.close() ;
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace() ;
		}
		finally
		{
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
		return null ;
	}
	
	public static void main(String[] args)
	{
		System.out.println(getCurrentSysLoginBean()) ;
	}
	
	public static void delete(File aFile)
	{
		if(aFile.isDirectory())
		{
			try
			{
				Runtime.getRuntime().exec(new String[]{"rd /s /q "+aFile.getAbsolutePath()}) ;
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
		String s = null;
		ProcessBuilder proBld = new ProcessBuilder("ping" , aHost) ;
		Process process = proBld.start() ;
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
	    BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
	 
	    boolean begin = false ;
	    while ((s = stdInput.readLine()) != null)
	    {
	    	if(begin)
	    	{
	    		if(s.contains("ttl=") || s.contains("TTL="))
	    		{
	    			process.destroy();
	    			return true ;
	    		}
	    		else
	    		{
	    			process.destroy(); 
	    			return false ;
	    		}
	    			
	    	}
	    	else if(s.contains("Ping") || s.contains("ping"))
	    		begin = true ;
	    }
	    StringWriter writer = new StringWriter() ;
	    while ((s = stdError.readLine()) != null)
	    {
	      writer.write(s);
	      writer.write(XString.sLineSeparator);
	    }
		throw new IOException(writer.toString()) ;
	}
}
