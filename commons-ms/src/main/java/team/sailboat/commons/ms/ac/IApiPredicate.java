package team.sailboat.commons.ms.ac;

/**
 * 
 *
 * @author yyl
 * @since 2024年11月30日
 */
public interface IApiPredicate
{
	boolean canInvokeApiOfClientApp(String aAppId , String aApiName) ;
}
