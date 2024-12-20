package team.sailboat.ms.ac.server;

/**
 * 
 * 资源管理服务构件
 *
 * @author yyl
 * @since 2024年10月30日
 */
public interface IResourceManageComponent
{
	ResourceManageServer getResMngServer() ;
	
	/**
	 * 初始化
	 */
	void init() ;
}
