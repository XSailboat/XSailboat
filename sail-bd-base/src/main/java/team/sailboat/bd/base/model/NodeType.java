package team.sailboat.bd.base.model;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import team.sailboat.commons.fan.collection.XC;

public enum NodeType
{
	ZBDSql,
	ZBDPython,
	ZBDVirtual,
	ZBDDi
	;
	
	private NodeType()
	{
	}
	
	
	private static final Set<String> sNames = XC.hashSet() ;
	private static final AtomicBoolean sInited = new AtomicBoolean(false) ; 
	
	public static boolean isValid(String aName)
	{
		if(sInited.compareAndSet(false, true))
		{
			for(NodeType type : values())
			{
				sNames.add(type.name()) ;
			}
		}
		return sNames.contains(aName) ;
	}
}
