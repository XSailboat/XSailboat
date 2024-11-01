package team.sailboat.commons.ms.infc;

import team.sailboat.commons.fan.lang.JCommon;

public interface IMSActiveSupport
{
	default String[] getServicePackages()
	{
		return JCommon.sEmptyStringArray ;
	}
}
