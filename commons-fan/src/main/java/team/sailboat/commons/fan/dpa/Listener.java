package team.sailboat.commons.fan.dpa;

public interface Listener
{
	void handle(Event aEvent) ;
	
	boolean isDestroyed() ;
	
	void destroy() ;
}
