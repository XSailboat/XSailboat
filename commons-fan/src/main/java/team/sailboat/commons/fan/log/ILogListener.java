package team.sailboat.commons.fan.log;

public interface ILogListener extends ILogLevel
{
	void log(int aType , String aMsg) ;
}
