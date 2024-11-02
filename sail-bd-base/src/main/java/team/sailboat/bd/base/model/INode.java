package team.sailboat.bd.base.model;

public interface INode
{
	String getId() ;
	
	String getName() ;
	boolean setName(String aName) ;
	
	long getVersion() ;
	boolean setVersion(long aVersion) ;
}
