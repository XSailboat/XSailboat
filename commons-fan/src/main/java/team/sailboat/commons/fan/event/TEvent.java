package team.sailboat.commons.fan.event;

public class TEvent
{
	public int type ;
	public int time;
	public Object source ;
	public Object[] params ;
	
	public String mDescription ;
	
	public TEvent()
	{}
	
	public TEvent(int aType , Object aSource)
	{
		type = aType ;
		source = aSource ;
	}
}
