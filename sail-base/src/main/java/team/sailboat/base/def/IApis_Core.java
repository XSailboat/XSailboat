package team.sailboat.base.def;

public interface IApis_Core extends IApis_Common
{
	/**
	 * 创建数据源
	 */
	public static final String sPOST_createDataSource = "/dataSource/one" ;
	
	/**
	 * 更新数据源
	 */
	public static final String sPUT_updateDataSource = "/dataSource/one" ;
	
	/**
	 * 获取数据源的密码
	 */
	public static final String sGET_dataSourcePswd = "/dataSource/password" ;
	
	/**
	 * 取得数据源。如果指定version和服务器上的version相同，则不会把数据源信息都返回一次
	 */
	public static final String sGET_dataSourceAllVersion = "/dataSource/all/_checkVersion" ;
	
	/**
	 * 通过标签的路径名获取标签
	 */
	public static final String sGET_labelByPathName = "/label/one/byPathName" ;
	
	/**
	 * 指定标签在指定源上的绑定信息
	 */
	public static final String sGET_bindingsOfLabelAtSource = "/label/one/binding/all/ofLabel/atSource" ;
}
