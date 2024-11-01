package team.sailboat.commons.fan.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import team.sailboat.commons.fan.lang.JCommon;

public class DefaultHttpsClient extends DefaultHttpClient
{
	static final String sDefaultSSLProtocol = "TLSv1.2" ;
	
	SSLSocketFactory mSocketFac ;
	HostnameVerifier mHostnameVerifier ;
	
	DefaultHttpsClient(String aHost, int aPort)
	{
		this(aHost , aPort , null) ;
	}
	
	DefaultHttpsClient(String aHost, int aPort , String aSSLProtocol)
	{
		super(aHost, aPort);
		SSLContext ctx = null;
		try
		{
	        ctx = SSLContext.getInstance(JCommon.defaultIfEmpty(aSSLProtocol , sDefaultSSLProtocol));
	        ctx.init(new KeyManager[0] 
	        		, new TrustManager[] { new DefaultTrustManager() }
	        		, new SecureRandom());
		}
		catch(NoSuchAlgorithmException | KeyManagementException e)
		{
			throw new RuntimeException(e) ;
		}
        mSocketFac = ctx.getSocketFactory();
        mHostnameVerifier = new DefaultHostnameVer() ;
	}
	
	@Override
	public String getProtocol()
	{
		return "https" ;
	}

	@Override
	protected HttpURLConnection createConnection(Request aRequest) throws IOException
	{
		HttpsURLConnection conn = (HttpsURLConnection)super.createConnection(aRequest);
		conn.setSSLSocketFactory(mSocketFac) ;
		conn.setHostnameVerifier(mHostnameVerifier) ;
		return conn ;
	}
	
	private static final class DefaultTrustManager implements X509TrustManager {

		@Override
		public void checkClientTrusted(X509Certificate[] aChain , String aAuthType)
				throws CertificateException
		{
		}

		@Override
		public void checkServerTrusted(X509Certificate[] aChain, String aAuthType)
				throws CertificateException
		{
		}

		@Override
		public X509Certificate[] getAcceptedIssuers()
		{
			return null;
		}
    }
	
	static class DefaultHostnameVer implements HostnameVerifier
	{

		@Override
		public boolean verify(String aArg0, SSLSession aArg1)
		{
			return true ;
		}
		
	}
}
