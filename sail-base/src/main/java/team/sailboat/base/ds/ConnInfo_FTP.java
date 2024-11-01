package team.sailboat.base.ds;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema(name="ConnInfo_FTP" , description="FTP的连接信息")
public class ConnInfo_FTP extends ConnInfo_FileSystem
{
	
	public ConnInfo_FTP()
	{
		super("ConnInfo_FTP") ;
	}
	
	@Override
	public String getProtocol()
	{
		return "ftp" ;
	}
	
	@Override
	public ConnInfo_FTP clone()
	{
		ConnInfo_FTP clone = new ConnInfo_FTP() ;
		clone.copyFrom(this) ;
		return clone ;
	}
	
	public static ConnInfo_FTP parse(String aConnInfo)
	{
		return (ConnInfo_FTP) parse(aConnInfo, new ConnInfo_FTP()) ;
	}
}
