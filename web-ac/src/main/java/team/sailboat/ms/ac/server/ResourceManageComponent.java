package team.sailboat.ms.ac.server;

public abstract class ResourceManageComponent implements IResourceManageComponent
{
	protected final ResourceManageServer mResMngServer ;
	
	public ResourceManageComponent(ResourceManageServer aResMngServer)
	{
		mResMngServer = aResMngServer ;
	}
	
	@Override
	public ResourceManageServer getResMngServer()
	{
		return mResMngServer ;
	}
}
