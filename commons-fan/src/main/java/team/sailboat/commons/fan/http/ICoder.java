package team.sailboat.commons.fan.http;

import java.io.DataOutputStream;
import java.io.OutputStream;

public interface ICoder
{
	
	public static final String sHeaderName_CipherAlgo = "y-cipher-algo" ;
	
	String getName() ;
	
	String encodeParam(String aParam) ;
	
	String encodeParamValue(String aParam) ;
	
	String encodeHeader(String aParam) ;
	
	String encodeHeaderValue(String aParam) ;
	
	String splitEncodePath(String aPath) ;
	
	DataOutputStream wrap(OutputStream aOuts) ;
}
