package team.sailboat.commons.fan.res;

public class ResourceUnavailableException extends Exception
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ResourceUnavailableException()
	{
		super("资源不可用") ;
	}

	public ResourceUnavailableException(String aMsg)
	{
		super(aMsg) ;
	}
}
