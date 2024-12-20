package team.sailboat.ms.ac.server;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dpa.MapIndex;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.ChineseComparator;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.ms.ac.AppConsts;
import team.sailboat.ms.ac.dbean.R_User_App;
import team.sailboat.ms.ac.dbean.R_User_ResSpace_Role;
import team.sailboat.ms.ac.dbean.ResSpace;
import team.sailboat.ms.ac.dbean.User;
import team.sailboat.ms.ac.dbean.User.BUser;

/**
 * 
 * 缺省的用户数据管理器
 *
 * @author yyl
 * @since 2024年10月30日
 */
public class DefaultUserDataManager extends ResourceManageComponent implements IUserDataManager
{
	
	MapIndex<User> mUsernameMapIndex ;
	MapIndex<User> mRealName_U ;
	MapIndex<User> mDingOpenId_U ;
	
	MapIndex<R_User_App> mUserId_RUA ;
	
	MapIndex<R_User_ResSpace_Role> mUserId_RURSR ;
	
	public DefaultUserDataManager(ResourceManageServer aServer)
	{
		super(aServer) ;
	}
	
	public void init()
	{
		mUsernameMapIndex = mResMngServer.mRepo.mapIndex(User.class, "username") ;
		mRealName_U = mResMngServer.mRepo.mapIndex(User.class , "realName") ;
		mDingOpenId_U = mResMngServer.mRepo.mapIndex(User.class , "dingOpenId") ;
		
		mUserId_RUA = mResMngServer.mRepo.mapIndex(R_User_App.class , "userId") ;
		
		mUserId_RURSR = mResMngServer.mRepo.mapIndex(R_User_ResSpace_Role.class , "userId") ;
	}

	@Override
	public User loadUserByUsername(String aUsername) throws UsernameNotFoundException
	{
		User user = mUsernameMapIndex.getFirst(aUsername) ;
		if(user == null)
			throw new UsernameNotFoundException("找不到用户名为%s的用户！".formatted(aUsername)) ;
		return user ;
	}

	@Override
	public int getUserAmount()
	{
		return mResMngServer.mRepo.getSize(User.class) ;
	}

	@Override
	public List<User> getAllUsers()
	{
		List<User> userList = XC.arrayList() ;
		mResMngServer.mRepo.forEach(User.class, userList::add);
		return userList ;
	}
	
	public void forEachUser(Predicate<User> aPredUser)
	{
		mResMngServer.mRepo.forEach(User.class, aPredUser);
	}
	
	@Override
	public Collection<User> getAllUsersOrderByRealNameAsc()
	{
		Set<User> users = XC.treeSet((u1 , u2)->{
			int rc = ChineseComparator.comparePingYin(u1.getRealName() , u2.getRealName()) ;
			if(rc == 0)
				return u1.getId().compareTo(u2.getId()) ;
			return rc ;
		}) ;
		mResMngServer.mRepo.forEach(User.class, users::add);
		return users ;
	}

	@Override
	public synchronized User createUser(User.BUser aUser , String aOperUserId)
	{
		// 检查一下用户名是否重复
		Assert.isTrue(mUsernameMapIndex.getAmount(aUser.getUsername()) == 0
				, "用户名[%s]已经被占用！" , aUser.getUsername()) ;
		User user = mResMngServer.mRepo.newBean(User.class) ;
		user.update(aUser, aOperUserId, true) ;
		user.setUserAuthsProviderInAuthCenter(mResMngServer.getUserAuthsProviderInAuthCenter()) ; 
		
		// 用户和认证中心的关系
		R_User_App r = mResMngServer.mRepo.newBean(R_User_App.class) ;
		r.setUserId(user.getId()) ;
		r.setClientAppId(mResMngServer.getClientAppId_SailAC()) ;
		r.setCreateUserId(aOperUserId) ;
		r.setCreateTime(user.getCreateTime()) ;
		
		return user ;
	}
	
	@Override
	public void updateUser(BUser aUser, String aUserId)
	{
		Assert.notEmpty(aUser.getId() , "没有指定待更新的用户id!") ;
		User user = mResMngServer.mRepo.getByBid(User.class , aUser.getId()) ;
		Assert.notNull(user , "无效的用户id：%s！" , aUser.getId()) ;
		aUser.setPassword(user.getPassword()) ;
		user.update(aUser, aUserId,  false) ;
	}

	@Override
	public User getUser(String aUserId)
	{
		return mResMngServer.mRepo.getByBid(User.class , aUserId) ;
	}
	
	@Override
	public String getUserDisplayName(String aUserId)
	{
		if(XString.isEmpty(aUserId))
			return null ;
		if(AppConsts.sUserId_sys.equals(aUserId))
			return "系统" ;
		User user = mResMngServer.mRepo.getByBid(User.class , aUserId) ;
		return user==null?null:user.getDisplayName() ;
	}
	
	@Override
	public boolean deleteUser(String aTargetUserId , String aUserId)
	{
		boolean changed = false ;
		// 删除人和组织的关系
		mResMngServer.mRepo.deleteAll(mResMngServer.getAuthCenterDataMng().getR_OrgUnit_UserOfUser(aTargetUserId)) ;
		
		// 删除人和app的关联
		R_User_App[] rs_app = mUserId_RUA.get(aTargetUserId) ;
		if(XC.isNotEmpty(rs_app))
		{
			mResMngServer.mRepo.deleteAll(R_User_App.class , XC.extract(rs_app, R_User_App::getId , String.class)) ;
			changed = true ;
		}
		// 删除人和资源空间、角色的关联
		Set<String> appIds = XC.hashSet() ;
		R_User_ResSpace_Role[] rs_role = mUserId_RURSR.get(aTargetUserId) ;
		if(XC.isNotEmpty(rs_role))
		{
			mResMngServer.mRepo.deleteAll(R_User_ResSpace_Role.class , XC.extract(rs_role, R_User_ResSpace_Role::getId , String.class)) ;
			XC.forEach(rs_role , (r)->{
				appIds.add(ResSpace.getClientAppIdFrom(r.getResSpaceId())) ;
			}) ;
			changed = true ;
		}
		// 删除人
		User user = mResMngServer.mRepo.getByBid(User.class , aTargetUserId) ;
		if(user != null)
		{
			mResMngServer.mRepo.delete(user) ;
			changed = true ;
		}
		//
		if(!appIds.isEmpty())
		{
			IClientAppDataManager clientAppDataMng = mResMngServer.getClientAppDataMng() ;
			for(String appId : appIds)
				clientAppDataMng.notifyUserAuthoritiesChanged(appId , aTargetUserId) ;
		}
		return changed ;
	}

	@Override
	public void recordConsentScopes(String aUserId, String aAppId, Collection<String> aScopes)
	{
		R_User_App[] rs = mUserId_RUA.get(aUserId , r->r.getClientAppId().equals(aAppId)) ;
		if(XC.isEmpty(rs))
		{
			// 创建
			R_User_App r = mResMngServer.mRepo.newBean(R_User_App.class) ;
			r.setClientAppId(aAppId) ;
			r.setUserId(aUserId) ;
			r.setCreateTime(new Date()) ;
			r.setCreateUserId(AppConsts.sUserId_sys) ;
		}
		for(R_User_App r : rs)
		{
			if(XC.isEmpty(aScopes))
				r.setAuthorizedScopes(null) ;
			else
				r.setAuthorizedScopes(aScopes.toArray(JCommon.sEmptyStringArray)) ;
		}
	}

	@Override
	public String[] getScopesOfUserConsent(String aUserId, String aAppId)
	{
		R_User_App[] rs = mUserId_RUA.get(aUserId , r->r.getClientAppId().equals(aAppId)) ;
		return XC.isNotEmpty(rs)?rs[0].getAuthorizedScopes():null ;
	}
	
	@Override
	public User[] getUsersByRealName(String aRealName)
	{
		return mRealName_U.get(aRealName) ;
	}
	
	@Override
	public User getUserByDingOpenId(String aOpenId)
	{
		return mDingOpenId_U.getFirst(aOpenId) ;
	}
}
