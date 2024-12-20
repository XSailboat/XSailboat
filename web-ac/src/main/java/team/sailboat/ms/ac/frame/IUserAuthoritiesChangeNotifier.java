package team.sailboat.ms.ac.frame;

/**
 * 用户权限改变的通知器
 *
 * @author yyl
 * @since 2021年11月10日
 */
public interface IUserAuthoritiesChangeNotifier
{
	void addListener(IUserAuthoritiesChangeListener aLsn) ;
	
	void removeListener(IUserAuthoritiesChangeListener aLsn) ;
	
	/**
	 * 
	 * 通知用户的权限改变事件
	 * 
	 * @param aEvent
	 */
	void notifyUserAuthoritiesChanged(UserAuthoritiesChangeEvent aEvent) ;
	
	/**
	 * 
	 * 通知指定应用，指定的这些用户角色发生了改变，需要刷新
	 * 
	 * @param aAppId
	 * @param aUserIds
	 */
	void notifyUserAuthoritiesChanged(String aAppId , String... aUserIds) ;
}
