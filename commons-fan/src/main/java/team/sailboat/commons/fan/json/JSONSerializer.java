package team.sailboat.commons.fan.json;

@FunctionalInterface
public interface JSONSerializer
{
	void serialize(JSONWriter aWriter) ;
}
