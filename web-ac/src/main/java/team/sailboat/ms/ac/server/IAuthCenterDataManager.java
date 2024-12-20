package team.sailboat.ms.ac.server;

import java.util.List;
import java.util.function.Consumer;

import team.sailboat.ms.ac.bean.User_OrgUnit;
import team.sailboat.ms.ac.dbean.Api;
import team.sailboat.ms.ac.dbean.OrgUnit;
import team.sailboat.ms.ac.dbean.R_OrgUnit_User;

/**
 * 
 * 认证中心的数据管理器
 *
 * @author yyl
 * @since 2024年10月30日
 */
public interface IAuthCenterDataManager extends IResourceManageComponent
{
	/**
	 * 
	 * 获取一个ClientApp应该拥有的基本的可调用API
	 * 
	 * @return
	 */
	List<Api> getBasicAuthApis() ;
	
	/**
	 * 
	 * 将目标用户绑定到特定的组织单元下
	 * 
	 * @param aTargetUserId			目标用户id
	 * @param aOrgUnitId			组织单元id
	 * @param aJob					用户在这个组织单元下的职位
	 * @param aUserId				操作者用户id
	 * @return
	 */
	R_OrgUnit_User bindUserToOrgUnit(String aTargetUserId , String aOrgUnitId , String aJob , String aUserId) ;
	
	/**
	 * 
	 * 将目标用户和指定的组织单元解绑
	 * 
	 * @param aTargetUserId			目标用户id
	 * @param aOrgUnitId			组织单元id
	 * @param aUserId				操作者用户id
	 * @return				如果存在关联，且解绑成功返回true，否则返回false
	 */
	boolean unbindUserToOrgUnit(String aTargetUserId , String aOrgUnitId , String aUserId) ;
	
	/**
	 * 
	 * 取得指定用户的R_OrgUnit_User数据
	 * 
	 * @param aUserId
	 * @return
	 */
	R_OrgUnit_User[] getR_OrgUnit_UserOfUser(String aUserId) ;
	
	/**
	 * 
	 * 取得认证中心给ClientApp调用的接口声明
	 * 
	 * @return
	 */
	List<Api> getAllApis() ;
	
	/**
	 * 
	 * 取得指定组织单元的下一层组织单元。如果不指定上一层组织单元，则表示获取最顶层的组织单元
	 * 
	 * @param aParentId		上一层组织单元id
	 * @return
	 */
	OrgUnit[] getChildOrgUnit(String aParentId) ;
	
	/**
	 * 
	 * 遍历各个组织单元
	 * 
	 * @param aConsumer
	 */
	void forEachOrgUnit(Consumer<OrgUnit> aConsumer) ;
	
	/**
	 * 
	 * 获取指定id的组织单元
	 * 
	 * @param aId
	 * @return
	 */
	OrgUnit getOrgUnit(String aId) ;
	
	/**
	 * 
	 * 取得子组织单元的数量
	 * 
	 * @param aId
	 * @return
	 */
	int getChildOrgUnitAmount(String aId) ;
	
	/**
	 * 
	 * 创建一个组织单元
	 * 
	 * @param aOrgUnit
	 * @param aUserId
	 * @return
	 */
	OrgUnit createOrgUnit(OrgUnit.BOrgUnit aOrgUnit , String aUserId) ;
	
	/**
	 * 
	 * 更新指定的组织单元
	 * 
	 * @param aOrgUnit
	 * @param aUserId
	 * @return
	 */
	OrgUnit updateOrgUnit(OrgUnit.BOrgUnit aOrgUnit , String aUserId) ;
	
	/**
	 * 
	 * 删除指定的组织单元
	 * 
	 * @param aOrgUnitId
	 * @param aUserId
	 * @return				如果指定的组织单元存在且成功删除，返回true，否则返回false
	 */
	boolean deleteOrgUnit(String aOrgUnitId , String aUserId) ;
	
	/**
	 * 
	 * 取得指定组织单元下面的用户
	 * 
	 * @param aOrgUnitId
	 * @return
	 */
	List<User_OrgUnit> getChildUsers(String aOrgUnitId) ;
	
	/**
	 * 
	 * 用户挂到指定的组织单元上，并且设定或更新用户在这个组织单元中的职位
	 * 
	 * @param aOrgUnitId		组织单元id
	 * @param aTargetUserId		挂接的目标用户id
	 * @param aJob				用户在组织单元下的职位
	 * @param aUserId			操作者用户id
	 * @return
	 */
	R_OrgUnit_User hookUserToOrgUnit(String aOrgUnitId , String aTargetUserId , String aJob
			, String aUserId) ;
	
	/**
	 * 
	 * 用户挂到指定的组织单元上，如果原先已经挂接上，不会设定或更新用户在这个组织单元中的职位
	 * 
	 * @param aOrgUnitId		组织单元id
	 * @param aTargetUserId		挂接的目标用户id
	 * @param aJob				用户在组织单元下的职位
	 * @param aUserId			操作者用户id
	 * @return
	 */
	R_OrgUnit_User hookUserToOrgUnit(String aOrgUnitId , String aTargetUserId
			, String aUserId) ;
	
	/**
	 * 
	 * 解除用户挂到指定的组织单元上
	 * 
	 * @param aOrgUnitId		组织单元id
	 * @param aTargetUserId		挂接的目标用户id
	 * @param aUserId			操作者用户id
	 * @return
	 */
	boolean unhookUserToOrgUnit(String aOrgUnitId , String aTargetUserId
			, String aUserId) ;
}
