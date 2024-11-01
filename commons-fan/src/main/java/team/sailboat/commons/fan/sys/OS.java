package team.sailboat.commons.fan.sys;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import team.sailboat.commons.fan.file.FileUtils;

public class OS
{
	static boolean sInited = false ;
	
	static final String OS_NAME = "os.name" ;
	static final String OS_ARCH = "os.arch" ;
	static final String OS_VERSION = "os.version" ;
	
	static String sOSName ;
	static String sOSArch ;
	static String sOSVersion ;
	static EPlatform sPlatform ;
	static boolean m32 ;
	
	static String sOSUser ; 
	
	static void init()
	{
		sOSName = System.getProperty(OS_NAME) ;
		sOSArch = System.getProperty(OS_ARCH) ;
		sOSVersion = System.getProperty(OS_VERSION) ;
		String lowName = sOSName.toLowerCase() ;
		if(lowName.startsWith("windows"))
			sPlatform = EPlatform.Windows ;
		else if(lowName.startsWith("linux"))
			sPlatform = EPlatform.Linux ;
		else if(lowName.indexOf("mac")>=0&&lowName.indexOf("os")>0)
		{
			if(lowName.indexOf("x")<0)
				sPlatform = EPlatform.Mac_OS ;
			else
				sPlatform = EPlatform.Mac_OS_X ;
		}
		else if(lowName.indexOf("os/2")>=0)
			sPlatform = EPlatform.OS2 ;
		else if(lowName.indexOf("solaris")>=0)
			sPlatform = EPlatform.Solaris ;
		else if(lowName.indexOf("sunos")>=0)
			sPlatform = EPlatform.SunOS ;
		else if(lowName.indexOf("mpe/ix")>=0)
			sPlatform = EPlatform.MPEiX ;
		else if(lowName.indexOf("hp-ux")>=0)
			sPlatform = EPlatform.HP_UX ;
		else if(lowName.indexOf("aix")>=0)
			sPlatform = EPlatform.AIX ;
		else if(lowName.indexOf("os/390")>=0)
			sPlatform = EPlatform.OS390 ;
		else if(lowName.indexOf("freebsd")>=0)
			sPlatform = EPlatform.FreeBSD ;
		else if(lowName.indexOf("irix")>=0)
			sPlatform = EPlatform.Irix ;
		else if(lowName.indexOf("digital")>=0 && lowName.indexOf("unix")>0)
			sPlatform = EPlatform.Digital_Unix ;
		else if(lowName.indexOf("netware")>=0)
			sPlatform = EPlatform.NetWare_411 ;
		else if(lowName.indexOf("osf1")>=0)
			sPlatform = EPlatform.OSF1 ;
		else if(lowName.indexOf("openvms")>=0)
			sPlatform = EPlatform.OpenVMS ;
		
		sOSUser = System.getProperty("user.name") ;
		sInited = true ;
	}
	
	public static boolean is32()
	{
		if(!sInited) init() ;
		return sOSArch.contains("x86") ;
	}
	
	public static String getName()
	{
		if(!sInited) init() ;
		return sOSName ;
	}
	
	public static String getArch()
	{
		if(!sInited) init() ;
		return sOSArch ;
	}
	
	public static String getVersion()
	{
		if(!sInited) init() ;
		return sOSVersion ;
	}
	
	public static boolean isWindows()
	{
		if(sPlatform == null) init() ;
		return sPlatform == EPlatform.Windows ;
	}

	public static boolean isLinux()
	{ 
		if(sPlatform == null) init() ;
		return sPlatform == EPlatform.Linux ;
    }  
      
    public static boolean isMacOS(){  
    	if(sPlatform == null) init() ;
		return sPlatform == EPlatform.Mac_OS ;
    }  
      
    public static boolean isMacOSX(){  
    	if(sPlatform == null) init() ;
		return sPlatform == EPlatform.Mac_OS_X ;
    }
      
    public static boolean isOS2()
    {  
    	if(sPlatform == null) init() ;
		return sPlatform == EPlatform.OS2 ;
    }  
      
    public static boolean isSolaris()
    {  
    	if(sPlatform == null) init() ;
		return sPlatform == EPlatform.Solaris ; 
    }  
      
    public static boolean isSunOS()
    {  
    	if(sPlatform == null) init() ;
		return sPlatform == EPlatform.SunOS ; 
    }  
      
    public static boolean isMPEiX()
    {  
    	if(sPlatform == null) init() ;
		return sPlatform == EPlatform.MPEiX ;  
    }  
      
    public static boolean isHPUX()
    {  
    	if(sPlatform == null) init() ;
		return sPlatform == EPlatform.HP_UX ;   
    }  
      
    public static boolean isAix()
    {  
    	if(sPlatform == null) init() ;
		return sPlatform == EPlatform.AIX ; 
    }  
      
    public static boolean isOS390()
    {  
    	if(sPlatform == null) init() ;
		return sPlatform == EPlatform.OS390 ;
    }  
      
    public static boolean isFreeBSD()
    {  
    	if(sPlatform == null) init() ;
		return sPlatform == EPlatform.FreeBSD ; 
    }  
      
    public static boolean isIrix()
    {  
    	if(sPlatform == null) init() ;
		return sPlatform == EPlatform.Irix ;
    }  
      
    public static boolean isDigitalUnix()
    {  
    	if(sPlatform == null) init() ;
		return sPlatform == EPlatform.Digital_Unix ; 
    }  
      
    public static boolean isNetWare()
    {  
    	if(sPlatform == null) init() ;
		return sPlatform == EPlatform.NetWare_411 ;  
    }  
      
    public static boolean isOSF1(){  
    	if(sPlatform == null) init() ;
		return sPlatform == EPlatform.OSF1 ;  
    }  
      
    public static boolean isOpenVMS()
    {  
    	if(sPlatform == null) init() ;
		return sPlatform == EPlatform.OpenVMS ;   
    }
    
//    public static SysLoginBean getCurrentSysLoginBean()
//    {
//    	if(OS.isWindows())
//    		return Windows.getCurrentSysLoginBean() ;
//    	else if(OS.isLinux())
//    		return Linux.getCurrentSysLoginBean() ;
//    	else
//    		throw new IllegalStateException() ;
//    }
    
    /**
     * windows下，一个用户只能有一个界面会话，所以windows下用用户名来表示会话id。windows下没有DISPLAY性质
     * @return {user , sessionId}
     */
    public static String[] getLoginIdentifier()
    {
    	String user = System.getenv("USERNAME") ;
		if(user == null)
			user = System.getenv("USER") ;
		String sessionId = System.getenv("DISPLAY") ;
		if(sessionId == null)
			sessionId = user ;
		return new String[]{user , sessionId} ;
    }
    
    public static String getExactName()
    {
    	if(isWindows())
    	{
    		try
			{
				return Windows.getExactName() ;
			}
			catch (IOException | InterruptedException | Error e)
			{
				throw new IllegalStateException(e) ;
			}
    	}
    	else if(isLinux())
    		return Linux.getDistributorID()+" "+Linux.getRelease() ;
    	return getName() ;
    }
    
    public static String getUser()
    {
    	if(!sInited) init() ;
    	return sOSUser ;
    }
    	
    
    static enum EPlatform
    {
    	Any("any"),  
        Linux("Linux"),  
        Mac_OS("Mac OS"),  
        Mac_OS_X("Mac OS X"),  
        Windows("Windows"),  
        OS2("OS/2"),  
        Solaris("Solaris"),  
        SunOS("SunOS"),  
        MPEiX("MPE/iX"),  
        HP_UX("HP-UX"),  
        AIX("AIX"),  
        OS390("OS/390"),  
        FreeBSD("FreeBSD"),  
        Irix("Irix"),  
        Digital_Unix("Digital Unix"),  
        NetWare_411("NetWare"),  
        OSF1("OSF1"),  
        OpenVMS("OpenVMS"),  
        Others("Others"); 
    	
    	private String mName ;
    	private EPlatform(String aName)
    	{
    		mName = aName ;
    	}
    	
    	public String getName()
    	{
    		return mName ;
    	}
    	
    	@Override
    	public String toString()
    	{
    		return mName ;
    	}
    }
    
    public static void delete(File aFile)
    {
    	if(!sInited)
    		init();
    	if(isWindows())
    		Linux.delete(aFile) ;
    	else if(isLinux())
    		Linux.delete(aFile);
    	else
    		FileUtils.deleteFile(aFile);		
    }
    
    /**
     * 
     * @param aHost
     * @return true表示能ping通主机，false表示不能ping通主机
     * @throws IOException 
     */
    public static boolean ping(String aHost) throws IOException
    {
    	if(isWindows())
    		return Windows.ping(aHost) ;
    	else if(isLinux())
    		return Linux.ping(aHost) ;
    	else
    		return Linux.ping(aHost) ;
    }
    
    public static boolean browse(String aUrl)
    {
    	try
		{
			if(isWindows())
			{
				if(Desktop.isDesktopSupported())
		    	{
		    		try
					{
						Desktop.getDesktop().browse(new URI(aUrl));
						return true ;
					}
					catch (IOException|URISyntaxException e)
					{
						e.printStackTrace();
					} 
		    	}
				Runtime.getRuntime().exec(new String[]{"rundll32 url.dll,FileProtocolHandler " + aUrl});
			}
			else if(isMacOS() || isMacOSX())
				Runtime.getRuntime().exec(new String[]{"open " + aUrl});
			else
			{
				String[] browsers = { "epiphany", "firefox", "mozilla", "konqueror",
						"netscape", "opera", "links", "lynx" };

				// Build a command string which looks like "browser1 "url" || browser2 "url" ||..."
				StringBuffer cmd = new StringBuffer();
				for (int i = 0; i < browsers.length; i++)
					cmd.append((i == 0 ? "" : " || ") + browsers[i] + " \"" + aUrl + "\" ");

				Runtime.getRuntime().exec(new String[] { "sh", "-c", cmd.toString() });
			}
			return true ;
		}
		catch (IOException e)
		{
			//此种方法在CenterOS+OpenJDK并不可用，所以此种方法作为linux下的次用方案
			if(Desktop.isDesktopSupported())
	    	{
	    		try
				{
					Desktop.getDesktop().browse(new URI(aUrl));
					return true ;
				}
				catch (IOException|URISyntaxException e1)
				{
					e.printStackTrace();
				} 
	    	}
			return false ;
		}
    }
}
