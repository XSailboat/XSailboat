package team.sailboat.bd.base.def;

public enum Application
{
	SailFlink("Flink集群") , 
	XTask("XTask集群")
	;
	
	String mName ;
	
	private Application(String aName)
	{
		mName = aName ;
	}
}
