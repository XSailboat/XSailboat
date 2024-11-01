package team.sailboat.ms.base.def;

public interface IGateway
{
	public static final String sDI_DataSourceAllVersion_GET = "/sail-di/ms-api/pivot/dataSource/all/_checkVersion" ;
	
	public static final String sDI_DataSourceTableNames_GET = "/sail-di/ms-api/pivot/dataSource/tables/name" ;
	
	public static final String sDI_DataSourceFilesystemDirList_GET = "/sail-di/ms-api/pivot/dataSource/filesystem/dir/_list" ;
	
	public static final String sDI_DataSourceTableColumns_GET = "/sail-di/ms-api/pivot/dataSource/tables/one/columns" ;
	
	public static final String sDI_DataSourcePassword = "/sail-di/ms-api/pivot/dataSource/password" ;
	
	public static final String sGM_CreateApiMapping = "/api-gateway/mng/ApiMapping" ;
	
	public static final String sGM_UpdateApiMapping = "/api-gateway/mng/ApiMapping" ;
	
	public static final String sGM_GetApiIdsByName = "/api-gateway/mng/ApiMapping/id/many/byName" ;
	
	public static final String sGM_GetApiGroupName = "/api-gateway/mng/ApiGroup" ;
	
	public static final String sGM_OfflineAndDeleteApi = "/api-gateway/mng/ApiMapping/many/_offlineAndDelete" ;
	
	public static final String sSTERN_DeployCPipe = "/sailstern/ms-api/flink/cpipe/_deploy" ;
}
