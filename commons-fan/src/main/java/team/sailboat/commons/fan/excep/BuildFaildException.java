package team.sailboat.commons.fan.excep;

/**
 * 对象、资源等构建失败异常
 *
 * @author yyl
 * @since 2024年9月21日
 */
public class BuildFaildException extends Exception 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public BuildFaildException(String aMsg)
	{
		super(aMsg) ;
	}
	
	public BuildFaildException(Exception aE)
	{
		super(aE) ;
	}
}
