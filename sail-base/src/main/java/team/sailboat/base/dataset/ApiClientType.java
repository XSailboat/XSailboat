package team.sailboat.base.dataset;

public enum ApiClientType
{
	
	Gateway("网关") ,
	
	Workspace("内部") ;
	
	String displayName ;
	
	private ApiClientType(String aDisplayName)
	{
		displayName = aDisplayName ;
	}
	
	public String getDisplayName()
	{
		return displayName;
	}
}
