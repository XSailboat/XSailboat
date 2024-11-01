package team.sailboat.commons.fan.dpa;

public enum DEventType implements EventType
{
	BEAN_CREATE ,
	BEAN_DELETE ,
	PROPERTY_CHANGE
	;
	private DEventType()
	{
	}

}
