package team.sailboat.commons.fan.http;

public interface IRequestWrapperBuilder
{
	boolean match(Object aReq) ;
	
	IRequestWrapper build(Object aReq) ;
}
