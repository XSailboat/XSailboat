package team.sailboat.ms.ac.server;

import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dpa.MapIndex;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.ms.ac.AppConsts;
import team.sailboat.ms.ac.bean.User_OrgUnit;
import team.sailboat.ms.ac.dbean.Api;
import team.sailboat.ms.ac.dbean.OrgUnit;
import team.sailboat.ms.ac.dbean.OrgUnit.BOrgUnit;
import team.sailboat.ms.ac.dbean.R_OrgUnit_User;
import team.sailboat.ms.ac.dbean.User;

/**
 * 
 * 认证中心的数据管理器			<br />
 * 
 * 包括：组织单元、API
 *
 * @author yyl
 * @since 2024年10月30日
 */
public class DefaultAuthCenterDataManager extends ResourceManageComponent implements IAuthCenterDataManager
{
	MapIndex<Api> mApiNameMapIndex ;
	
	MapIndex<R_OrgUnit_User> mUserId_ROUU ;
	MapIndex<R_OrgUnit_User> mOrgUnitId_R_OU ;
	
	MapIndex<OrgUnit> mParentId_OU ;
	
	List<String> mBasicAuthApiNames = XC.arrayList(AppConsts.sApiName_GetAccessToken) ;

	public DefaultAuthCenterDataManager(ResourceManageServer aResMngServer)
	{
		super(aResMngServer);
	}
	
	public void init()
	{
		mApiNameMapIndex = mResMngServer.mRepo.mapIndex(Api.class , "name") ;
		
		mUserId_ROUU = mResMngServer.mRepo.mapIndex(R_OrgUnit_User.class , "userId") ;
		mOrgUnitId_R_OU = mResMngServer.mRepo.mapIndex(R_OrgUnit_User.class , "orgUnitId") ;
		
		mParentId_OU = mResMngServer.mRepo.mapIndex(OrgUnit.class , "parentId") ;
	}

	@Override
	public List<Api> getBasicAuthApis()
	{
		List<Api> apiList = XC.arrayList() ;
		for(String apiName : mBasicAuthApiNames)
		{
			Api api = mApiNameMapIndex.getFirst(apiName) ;
			if(api != null)
				apiList.add(api) ;
		}
		return apiList ;
	}

	@Override
	public R_OrgUnit_User bindUserToOrgUnit(String aTargetUserId, String aOrgUnitId
			, String aJob
			, String aOperUserId)
	{
		R_OrgUnit_User[] rs = mUserId_ROUU.get(aTargetUserId , r->r.getOrgUnitId().equals(aOrgUnitId)) ;
		if(XC.isNotEmpty(rs))
			return rs[0] ;
		R_OrgUnit_User r = mResMngServer.mRepo.newBean(R_OrgUnit_User.class) ;
		r.setOrgUnitId(aOrgUnitId) ;
		r.setUserId(aTargetUserId) ;
		r.setJob(aJob) ;
		r.setCreateTime(new Date()) ;
		r.setCreateUserId(aOperUserId) ;
		return r ;
	}

	@Override
	public boolean unbindUserToOrgUnit(String aTargetUserId, String aOrgUnitId, String aUserId)
	{
		R_OrgUnit_User[] rs = mUserId_ROUU.get(aTargetUserId , r->r.getOrgUnitId().equals(aOrgUnitId)) ;
		if(XC.isNotEmpty(rs))
		{
			mResMngServer.mRepo.delete(rs[0]) ;
			return true ;
		}
		return false ;
	}
	
	@Override
	public R_OrgUnit_User[] getR_OrgUnit_UserOfUser(String aUserId)
	{
		return mUserId_ROUU.get(aUserId) ;
	}
	
	@Override
	public List<Api> getAllApis()
	{
		List<Api> apiList = XC.arrayList() ;
		mResMngServer.mRepo.forEach(Api.class , apiList::add);
		return apiList ;
	}
	
	@Override
	public OrgUnit[] getChildOrgUnit(String aParentId)
	{
		return mParentId_OU.get(JCommon.defaultIfEmpty(aParentId, null)) ;
	}
	
	@Override
	public int getChildOrgUnitAmount(String aId)
	{
		return mParentId_OU.getAmount(JCommon.defaultIfEmpty(aId, null)) ;
	}
	
	@Override
	public OrgUnit getOrgUnit(String aId)
	{
		return mResMngServer.mRepo.getByBid(OrgUnit.class , aId) ;
	}

	@Override
	public OrgUnit createOrgUnit(BOrgUnit aOrgUnit , String aUserId)
	{
		String parentId = aOrgUnit.getParentId() ;
		String parentPathId = "" ;
		if(XString.isEmpty(parentId))
		{
			aOrgUnit.setParentId(null) ;
		}
		else
		{
			OrgUnit parent = mResMngServer.mRepo.getByBid(OrgUnit.class , parentId) ;
			Assert.notNull(parent , "无效的组织单元父节点id：%s" , parentId) ;
			parentPathId = parent.getPathId() ;
			// 同一个父节点下的组织单元名称不能重复
			for(OrgUnit child : mParentId_OU.get(parentId))
			{
				Assert.notEquals(child.getName() , aOrgUnit.getName()
						, "在指定的组织单元父节点 %s[%s]下已经存在名为 %s 的节点！" 
						, parent.getName() , parent.getId()
						, aOrgUnit.getName()) ;
			}
		}
		OrgUnit orgUnit = mResMngServer.mRepo.newBean(OrgUnit.class) ;
		aOrgUnit.setPathId(parentPathId+"/"+orgUnit.getId()) ;
		orgUnit.update(aOrgUnit, aUserId, true) ;
		return orgUnit ;
	}

	@Override
	public boolean deleteOrgUnit(String aOrgUnitId, String aUserId)
	{
		OrgUnit orgUnit = mResMngServer.mRepo.getByBid(OrgUnit.class , aOrgUnitId) ;
		if(orgUnit == null)
			return false ;
		// 检查这个组织单元有没有下层节点，有的话就不能删除
		Assert.isTrue(mParentId_OU.getAmount(aOrgUnitId) == 0 , "指定的组织单元 %s[%s] 下面有子节点，不能删除！"
				, orgUnit.getName() , orgUnit.getId()) ;
		// 检查组织单元下面有没有挂人，挂人不能删
		Assert.isTrue(mUserId_ROUU.getAmount(aOrgUnitId) == 0 , "指定的组织单元 %s[%s] 下面有人员，不能删除！"
				, orgUnit.getName() , orgUnit.getId()) ;
		
		mResMngServer.mRepo.delete(orgUnit) ;
		return true ;
	}

	@Override
	public OrgUnit updateOrgUnit(BOrgUnit aOrgUnit , String aUserId)
	{
		OrgUnit orgUnit = mResMngServer.mRepo.getByBid(OrgUnit.class , aOrgUnit.getId()) ;
		Assert.notNull(orgUnit , "无效的组织单元id：%s", aOrgUnit.getId()) ;
		
		// 检查父节点id是否发生改变
		String parentId = aOrgUnit.getParentId() ;
		boolean noParent = XString.isEmpty(parentId) ;
		if(noParent)
		{
			aOrgUnit.setParentId(null) ;
		}
		boolean parentChanged = JCommon.unequals(orgUnit.getParentId() , aOrgUnit.getParentId()) ;
		if(parentChanged)
		{
			String parentPathId = "" ;
			if(!noParent)
			{
				// 检查parentId是否有效
				OrgUnit parent = mResMngServer.mRepo.getByBid(OrgUnit.class , parentId) ;
				Assert.notNull(parent , "无效的组织单元父节点id：%s" , parentId) ;
				parentPathId = parent.getPathId() ;
				// 同一个父节点下的组织单元名称不能重复
				for(OrgUnit child : mParentId_OU.get(parentId))
				{
					Assert.notEquals(child.getName() , aOrgUnit.getName()
							, "在指定的组织单元父节点 %s[%s]下已经存在名为 %s 的节点！" 
							, parent.getName() , parent.getId()
							, aOrgUnit.getName()) ;
				}
			}
			aOrgUnit.setPathId(parentPathId+"/"+orgUnit.getId()) ;
		}
		orgUnit.update(aOrgUnit , aUserId, false) ;
		if(parentChanged)
		{
			// 更新所有子节点的pathId
			_updateChildOrgUnitPathId(orgUnit.getId() , orgUnit.getPathId()) ;
		}
		return orgUnit ;
	}
	
	void _updateChildOrgUnitPathId(String aParentId , String aParentPathId)
	{
		for(OrgUnit child : mParentId_OU.get(aParentId))
		{
			child.setPathId(aParentPathId+"/"+child.getId()) ;
			_updateChildOrgUnitPathId(child.getId() , child.getPathId()) ;
		}
	}

	@Override
	public List<User_OrgUnit> getChildUsers(String aOrgUnitId)
	{
		List<User_OrgUnit> uoList = XC.arrayList() ;
		for(R_OrgUnit_User r : mOrgUnitId_R_OU.get(aOrgUnitId))
		{
			User user = mResMngServer.mRepo.getByBid(User.class , r.getUserId()) ;
			if(user != null)
			{
				uoList.add(User_OrgUnit.of(user, r)) ;
			}
 		}
		return uoList ;
	}
	
	protected R_OrgUnit_User hookUserToOrgUnit(String aOrgUnitId, String aTargetUserId, String aJob, String aUserId
			, boolean aUpdateJob)
	{
		// 一个用户是可以挂到多个不同的组织单元下的
		R_OrgUnit_User[] rs = mUserId_ROUU.get(aTargetUserId, r->r.getOrgUnitId().equals(aOrgUnitId)) ;
		if(XC.isNotEmpty(rs))
		{
			if(aUpdateJob)
				rs[0].setJob(aJob) ;
			return rs[0] ;
		}
		
		R_OrgUnit_User r = mResMngServer.mRepo.newBean(R_OrgUnit_User.class) ;
		r.setOrgUnitId(aOrgUnitId) ;
		r.setUserId(aTargetUserId) ;
		r.setJob(aJob) ;
		r.setCreateTime(new Date()) ;
		r.setCreateUserId(aUserId) ;
		
		return r ;
	}

	@Override
	public R_OrgUnit_User hookUserToOrgUnit(String aOrgUnitId, String aTargetUserId, String aJob, String aUserId)
	{
		return hookUserToOrgUnit(aOrgUnitId, aTargetUserId, aJob, aUserId, true) ;
	}
	
	@Override
	public R_OrgUnit_User hookUserToOrgUnit(String aOrgUnitId, String aTargetUserId, String aUserId)
	{
		return hookUserToOrgUnit(aOrgUnitId, aTargetUserId, null, aUserId, false) ;
	}

	@Override
	public boolean unhookUserToOrgUnit(String aOrgUnitId, String aTargetUserId, String aUserId)
	{
		R_OrgUnit_User[] rs = mUserId_ROUU.get(aTargetUserId, r->r.getOrgUnitId().equals(aOrgUnitId)) ;
		if(XC.isNotEmpty(rs))
		{
			mResMngServer.mRepo.deleteAll(rs);
			return true ;
		}
		return false ;
	}

	@Override
	public void forEachOrgUnit(Consumer<OrgUnit> aConsumer)
	{
		mResMngServer.mRepo.forEach(OrgUnit.class , ou->{
			aConsumer.accept(ou) ;
			return true ;
		});
	}
}
