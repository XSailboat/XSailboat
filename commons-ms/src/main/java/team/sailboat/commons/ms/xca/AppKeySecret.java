package team.sailboat.commons.ms.xca;

public class AppKeySecret implements IClientApp , AppCertificate
{
	String mAppId ;
	String mAppKey ;
	String mAppSecret ;
	
	public AppKeySecret()
	{
	}

	public AppKeySecret(String aAppId , String aAppKey, String aAppSecret)
	{
		mAppId = aAppId ;
		mAppKey = aAppKey;
		mAppSecret = aAppSecret;
	}
	
	@Override
	public String getId()
	{
		return mAppId ;
	}

	@Override
	public String getAppId()
	{
		return mAppId ;
	}
	
	@Override
	public String getAppKey()
	{
		return mAppKey;
	}

	@Override
	public String getAppSecret()
	{
		return mAppSecret;
	}

	public void setAppSecret(String aAppSecret)
	{
		mAppSecret = aAppSecret;
	}
	
	@Override
	public AppCertificateType getType()
	{
		return AppCertificateType.AppKeySecret ;
	}
}
