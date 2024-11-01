package team.sailboat.base.ds;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema(name="ConnInfo_SFTP" , description="SFTP的连接信息")
public class ConnInfo_SFTP extends ConnInfo_FileSystem
{
	
	public ConnInfo_SFTP()
	{
		super("ConnInfo_SFTP") ;
	}
	
	@Override
	public String getProtocol()
	{
		return "sftp" ;
	}
	
	@Override
	public ConnInfo_SFTP clone()
	{
		ConnInfo_SFTP clone = new ConnInfo_SFTP() ;
		clone.copyFrom(this) ;
		return clone ;
	}
	
	public static ConnInfo_SFTP parse(String aConnInfo)
	{
		return (ConnInfo_SFTP) parse(aConnInfo, new ConnInfo_SFTP()) ;
	}
}
