package team.sailboat.commons.fan.http;

public interface IRequestWrapper
{
	String getHeader(String aHeaderName) ;
	
	IRequestWrapper addHeader(String aHeaderName , String aValue) ;
	
	String getRemoteAddr() ;
}
