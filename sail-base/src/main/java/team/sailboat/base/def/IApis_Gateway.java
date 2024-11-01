package team.sailboat.base.def;

public interface IApis_Gateway
{
	public static final String sGET_DataSourcePassword = "/sailmscore/dataSource/password" ;
	
	public static final String sGET_CPipeDetail_dev = "/sailmsworks/cpipe/detail" ;
	
	public static final String sGET_CPipeDetailByPackage = "/sailmspivot/cpipe/one/detail/byPkg" ;
	
	/**
	 * 取得一个或多个指定id的API映射
	 */
	public static final String sGET_ApiMappings = "/api-gateway/mng/ApiMapping/many/byId" ;
	
	/**
	 * 取得自己的应用id
	 */
	public static final String sGET_ClientAppIdOfSelf ="/api-gateway/in-public/ClientApp/id/ofSelf" ;
	
	
}
