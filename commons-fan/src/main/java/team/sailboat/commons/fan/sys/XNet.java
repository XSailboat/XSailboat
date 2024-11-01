package team.sailboat.commons.fan.sys;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;

import team.sailboat.commons.fan.collection.SizeIter;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.serial.StreamAssist;
import team.sailboat.commons.fan.struct.IntObject;
import team.sailboat.commons.fan.struct.TimeHandle;
import team.sailboat.commons.fan.text.RegexUtils;
import team.sailboat.commons.fan.text.XString;

public class XNet
{
	static String[] sMacs ; 
	static String sHostName ;
	static final TimeHandle<SizeIter<String>> sIPs = new TimeHandle<>(2 , 60) ;
	static List<String> sValidIPv4 ;
	
	static IntObject<String> sPreferedNetSeg ;
	
	/**
	 * 
	 * @param aNetSeg   格式：192.168.0.23/16
	 */
	public static void setPreferedNetSeg(String aNetSeg)
	{
		int i = aNetSeg.indexOf('/') ;
		Assert.isTrue(i != -1 , "指定的网段[%s]不合法！" , aNetSeg) ;
		int pin = Integer.parseInt(aNetSeg.substring(i+1)) ;
		Assert.isTrue(pin>0 && pin<=32 , "“/”之后的数字[%d]不合法，应该是在[1,31]范围内的整数！" , pin);
		sPreferedNetSeg = new IntObject<String>(pin, aNetSeg.substring(0, i)) ;
		List<String> ipv4List = sValidIPv4 ;
		if(ipv4List != null && ipv4List.size()>1)
		{
			_sortIps(ipv4List);
		}
 	}
	
	static void _sortIps(List<String> aIpv4List)
	{
		final int len = aIpv4List.size() ;
		for(int i=0 ; i<len ; i++)
		{
			String ip = aIpv4List.get(i) ;
			if(isSame(ip, sPreferedNetSeg.getObject(), sPreferedNetSeg.getP()))
			{
				// 把它放在第一个
				if(i>0)
				{
					String first = aIpv4List.get(0) ;
					aIpv4List.set(0, ip) ;
					aIpv4List.set(i, first) ;
				}
				break ;
			}
		}
	}
	
	public static String getPreferedIpv4() throws SocketException
	{
		return XC.getFirst(getValidLocalIPv4s()) ;
	}
	
	public static String[] getMacs()
	{
		if(sMacs == null)
		{
			List<String> macs = new ArrayList<String>() ;
			try
			{
				Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces() ;
				while(nis.hasMoreElements())
				{
					NetworkInterface ni = nis.nextElement() ;
					byte[] bytes = ni.getHardwareAddress() ;
					if(bytes == null || bytes.length==0) continue ;
					StringBuilder builder = new StringBuilder() ;
					int i = 0 ;
					for(byte b : bytes)
					{
						if(i++>0) builder.append("-") ;
						builder.append(XString.toHex(b)) ;
					}
					macs.add(builder.toString());
				}
			}
			catch(Exception e)
			{}
			sMacs = macs.toArray(new String[0]) ;
		}
		return sMacs ;
	}
	
	public static String getHostName()
	{
		if(sHostName == null)
		try
		{
			sHostName = Inet4Address.getLocalHost().getHostName() ;
		}
		catch (UnknownHostException e)
		{
			return "UNKNOW" ;
		}
		return sHostName ;
	}
	
	/**
	 * 获取主机的IPv4地址
	 * @param aHost
	 * @return
	 * @throws UnknownHostException
	 */
	public static String getIPv4(String aHost) throws UnknownHostException
	{
		InetAddress host= Inet4Address.getByName(aHost) ;
		return host.getHostAddress() ;
	}
	
	private static List<String> _getLocalIPs() throws SocketException
	{
		List<String> ipList = new ArrayList<>() ;
		Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces() ;
		List<String> ipv4List = new ArrayList<>() ;
		while(nis.hasMoreElements())
		{
			NetworkInterface ni = nis.nextElement() ;
			// VB的虚拟网卡忽略掉
			if(ni.getDisplayName().contains("VirtualBox "))
				continue ;
			Enumeration<InetAddress> addrEnu =  ni.getInetAddresses() ;
			while(addrEnu.hasMoreElements())
			{
				String hostAddr = addrEnu.nextElement().getHostAddress() ;
				ipList.add(hostAddr) ;
				if("127.0.0.1".equals(hostAddr))
					continue ;
				if(RegexUtils.checkIPv4(hostAddr))
					ipv4List.add(hostAddr) ;
			}
		}
		// 
		if(sPreferedNetSeg != null && ipv4List.size()>1)
		{
			_sortIps(ipv4List) ;
		}
		sValidIPv4 = ipv4List ;
		return ipList ;
	}
	
	public static SizeIter<String> getLocalIPs() throws SocketException
	{
		return sIPs.getOrRebuildE(()->SizeIter.create(_getLocalIPs())) ;
	}
	
	public static Collection<String> getValidLocalIPv4s() throws SocketException
	{
		getLocalIPs() ;
		return sValidIPv4 ;
	}
	
	public static Collection<String> getValidLocalIPv4s_0()
	{
		try
		{
			return getValidLocalIPv4s() ;
		}
		catch (SocketException e)
		{
			WrapException.wrapThrow(e) ;
			return null ;		// dead code
		}
	}
	
	public static boolean isLocalPortUsing(int port)
	{
		return isPortUsing("127.0.0.1" , port) ;
	}
	
	public static boolean isPortUsing(String host, int port)
	{
		boolean flag = false;
		Socket socket = null;
		try
		{
			InetAddress Address = InetAddress.getByName(host);
			socket = new Socket(Address, port); //建立一个Socket连接
			flag = true;
		}
		catch (IOException e)
		{
			//log.info(e.getMessage(),e);
		}
		finally
		{
			StreamAssist.close(socket);
		}
		return flag;
	}
	
	/**
     * 获取可用端口
     * @return
     */
	public static int getAvailablePort()
	{
		int max = 65535;
		int min = 2000;

		Random random = new Random();
		int port = random.nextInt(max) % (max - min + 1) + min;
		boolean using = isLocalPortUsing(port);
		return using?getAvailablePort() : port ;
		
	}
	
	public static boolean isLocalIp(String aIp) throws SocketException
	{
		if("127.0.0.1".equals(aIp) || "localhost".equals(aIp)
				|| "0:0:0:0:0:0:0:1".equals(aIp))
			return true ;
		return getLocalIPs().contains(aIp) ;
	}
	
	public static boolean isSameWithLocalIp(String aIp , int aPins) throws SocketException
	{
		if(isLocalIp(aIp))
			return true ;
		for(String ip : getValidLocalIPv4s())
		{
			if(isSame(aIp , ip , aPins))
				return true ;
		}
		return false ;
	}
	
	public static boolean isSame(String  aIp1 , String aIp2 , int aPins)
	{
        int ipAddr1 = _getIpNum(aIp1) ;
        int ipAddr2 = _getIpNum(aIp2) ;
        int mask = 0xFFFFFFFF << (32 - aPins);
        return (ipAddr1 & mask) == (ipAddr2 & mask);
    }
	
	static int _getIpNum(String aIpv4)
	{
		String[] ips = aIpv4.split("\\.");
		return (Integer.parseInt(ips[0]) << 24)
				| (Integer.parseInt(ips[1]) << 16)
				| (Integer.parseInt(ips[2]) << 8) 
				| Integer.parseInt(ips[3]);
	}
}
