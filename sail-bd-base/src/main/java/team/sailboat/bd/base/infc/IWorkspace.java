package team.sailboat.bd.base.infc;

import team.sailboat.commons.fan.collection.PropertiesEx;

public interface IWorkspace
{
	String getId() ;

	String getName() ;
	
	String getDisplayName() ;

	String getDescription() ;
	
	PropertiesEx getConf() ;
	/**
	 * 
	 * @param aConf
	 * @return			总是返回true
	 */
	boolean setConf(PropertiesEx aConf) ;
	
	boolean isClosed() ;
}
