package team.sailboat.ms.ac.server;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import team.sailboat.ms.ac.dbean.User;

/**
 * 
 * 用户数据管理器
 *
 * @author yyl
 * @since 2024年10月30日
 */
public interface IUserDataManager extends UserDetailsService , IResourceManageComponent
{
	User loadUserByUsername(String aUsername) throws UsernameNotFoundException ;
	
	/**
	 * 
	 * 取得用户数量
	 * 
	 * @return
	 */
	int getUserAmount() ;
	
	/**
	 * 
	 * 取得全部用户
	 * 
	 * @return
	 */
	List<User> getAllUsers() ;
	
	/**
	 * 遍历User数据
	 * @param aPredUser		返回false时，终止遍历
	 */
	void forEachUser(Predicate<User> aPredUser) ;
	
	/**
	 * 
	 * 按用户的真实姓名升序排列
	 * 
	 * @return
	 */
	Collection<User> getAllUsersOrderByRealNameAsc() ;
	
	/**
	 * 
	 * 创建一个新用户		<br />
	 * 
	 * @param aUser		新建的用户信息
	 * @param aUserId	操作者用户id
	 * @return
	 */
	User createUser(User.BUser aUser , String aUserId) ;
	
	/**
	 * 
	 * 更新一个用户的信息。<br>
	 * 指定的用户id无效将抛出异常
	 * 
	 * 不包括密码
	 * 
	 * @param aUser		新建的用户信息
	 * @param aUserId	操作者用户id
	 */
	void updateUser(User.BUser aUser , String aUserId) ;
	
	/**
	 * 
	 * 取得指定id的用户
	 * 
	 * @param aUserId
	 * @return
	 */
	User getUser(String aUserId) ;
	
	/**
	 * 
	 * 取得指定id用户的显示名
	 * 
	 * @param aUserId
	 * @return
	 */
	String getUserDisplayName(String aUserId) ;
	
	/**
	 * 
	 * 删除指定id的用户
	 * 
	 * @param aTargetUserId 		目标用户id
	 * @param aUserId				操作者用户id
	 * @return			如果存在这个用户且被删除，返回true，否则返回false
	 */
	boolean deleteUser(String aTargetUserId , String aUserId) ;
	
	/**
	 * 
	 * 记录用户许可指定ClientApp获取它的信息范围
	 * 
	 * @param aUserId			用户id
	 * @param aAppId			ClientApp的id
	 * @param aScopes			允许取得的信息范围
	 */
	void recordConsentScopes(String aUserId, String aAppId, Collection<String> aScopes) ;
	
	/**
	 * 
	 * 取得指定用户许可指定ClientApp获取它的信息范围
	 * 
	 * @param aUserId
	 * @param aAppId
	 * @return
	 */
	String[] getScopesOfUserConsent(String aUserId, String aAppId) ;
	
	/**
	 * 真实姓名可能出现同名情况
	 * @param aRealName
	 * @return
	 */
	User[] getUsersByRealName(String aRealName) ;
	
	/**
	 * 
	 * 通过钉用户的openId获取用户
	 * 
	 * @param aOpenId
	 * @return
	 */
	User getUserByDingOpenId(String aOpenId) ;
}
