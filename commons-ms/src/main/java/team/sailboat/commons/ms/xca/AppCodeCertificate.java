package team.sailboat.commons.ms.xca;

public class AppCodeCertificate implements AppCertificate
{
	String mAppId ;
	String mAppCode ;
	
	public AppCodeCertificate(String aAppId , String aAppCode)
	{
		mAppId = aAppId ;
		mAppCode = aAppCode ;
	}
	
	@Override
	public String getAppId()
	{
		return mAppId ;
	}
	
	public String getAppCode()
	{
		return mAppCode;
	}

	@Override
	public AppCertificateType getType()
	{
		return AppCertificateType.AppCode ;
	}

}
