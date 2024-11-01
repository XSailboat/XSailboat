package team.sailboat.commons.fan.dpa;

public class Event
{
	public long time = System.currentTimeMillis() ;
	public EventType type ;
	public Object source ;
	public Object[] params ;
	public String description ;
	
	public Event()
	{}
	
	public Event(EventType aType , Object aSource)
	{
		type = aType ;
		source = aSource ;
	}
	
	public Event(EventType aType , Object aSource , Object[] aParams)
	{
		type = aType ;
		source = aSource ;
		params = aParams ;
	}
}
