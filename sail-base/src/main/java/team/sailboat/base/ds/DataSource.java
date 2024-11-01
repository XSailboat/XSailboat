package team.sailboat.base.ds ;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;
import team.sailboat.base.def.DataSourceType;
import team.sailboat.base.def.WorkEnv;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.serial.StreamAssist;

@Schema(name="DataSource" , description="数据源信息")
@JsonInclude(value = Include.NON_NULL)
public class DataSource
{

	DataSourceType mType ;
	String mId ;
	String mName ;
	
	String mMark ;
	
	DataOwner mDataOwner ;
	
//	String mDevConnInfo ;
	ConnInfo mDevConnInfo ;
//	String mProdConnInfo ;
	ConnInfo mProdConnInfo ;
	
	String mCreateTime ;
	
	String mLastEditTime ;
	
	String mLastEditUserId ;
	
	String mLastEditUserDisplayName ;
	
	String mCreateUserId ;
	
	String mCreateUserDisplayName ;
	
	boolean mManaged ;
	
	@Schema(name = "数据源类型" , allowableValues = {"Hive","MySql5","MySql","SFTP","Kafka"})
	public DataSourceType getType()
	{
		return mType;
	}
	public void setType(DataSourceType aType)
	{
		mType = aType;
	}
	
	@Schema(description = "数据源名称")
	public String getName()
	{
		return mName ;
	}
	public void setName(String aName)
	{
		if(aName != null)
			aName = aName.trim() ; 
		mName = aName ;
	}
	
	@Schema(description = "数据源代号")
	public String getMark()
	{
		return mMark;
	}
	public void setMark(String aMark)
	{
		mMark = aMark;
	}
	
	@Schema(description = "id")
	public String getId()
	{
		return mId ;
	}
	public void setId(String aId)
	{
		mId = aId ;
	}
	
	@Schema(description = "开发环境的数据源连接信息" , anyOf = {ConnInfo_RDB.class, ConnInfo_Hive.class , ConnInfo_Kafka.class})
	public ConnInfo getDevConnInfo()
	{
		return mDevConnInfo;
	}
	public void setDevConnInfo(ConnInfo aDevConnInfo)
	{
		if(mDevConnInfo != null)
			StreamAssist.close(mDevConnInfo) ;
		mDevConnInfo = aDevConnInfo;
		mDevConnInfo.setManaged(mManaged);
	}
	
	@Schema(description = "生产环境的数据源连接信息")
	public ConnInfo getProdConnInfo()
	{
		return mProdConnInfo;
	}
	public void setProdConnInfo(ConnInfo aProdConnInfo)
	{
		if(mProdConnInfo != null)
			StreamAssist.close(mProdConnInfo) ;
		mProdConnInfo = aProdConnInfo;
		mProdConnInfo.setManaged(mManaged) ;
	}
	
	@Schema(hidden = true)
	public ConnInfo getConnInfo(WorkEnv aEnv)
	{
		return aEnv == WorkEnv.dev?getDevConnInfo():getProdConnInfo() ;
	}
	
	@Schema(description = "创建时间，格式yyyy-MM-dd HH:mm:ss")
	public String getCreateTime()
	{
		return mCreateTime ;
	}
	public void setCreateTime(String aTime)
	{
		mCreateTime = aTime ;
	}
	
	@Schema(description = "最近一次修改时间，格式：yyyy-MM-dd HH:mm:ss")
	public String getLastEditTime()
	{
		return mLastEditTime;
	}
	public void setLastEditTime(String aLastEditTime)
	{
		mLastEditTime = aLastEditTime;
	}
	
	@Schema(description = "创建者的用户id")
	public String getCreateUserId()
	{
		return mCreateUserId ;
	}
	public void setCreateUserId(String aCreateUserId)
	{
		mCreateUserId = aCreateUserId ;
	}
	
	@Schema(description = "创建者的显示名")
	public String getCreateUserDisplayName()
	{
		return mCreateUserDisplayName;
	}
	public void setCreateUserDisplayName(String aCreatedByDisplayName)
	{
		mCreateUserDisplayName = aCreatedByDisplayName;
	}
	
	@Schema(description = "最近更新者的id")
	public String getLastEditUserId()
	{
		return mLastEditUserId;
	}
	public void setLastEditUserId(String aLastEditUserId)
	{
		mLastEditUserId = aLastEditUserId;
	}
	
	@Schema(description = "最近更新者的显示名")
	public String getLastEditUserDisplayName()
	{
		return mLastEditUserDisplayName;
	}
	public void setLastEditUserDisplayName(String aLastEditUserDisplayName)
	{
		mLastEditUserDisplayName = aLastEditUserDisplayName;
	}
	
	@Schema(description = "数据归属")
	public DataOwner getDataOwner()
	{
		return mDataOwner;
	}
	public void setDataOwner(DataOwner aDataOwner)
	{
		mDataOwner = aDataOwner ;
	}
	
	/**
	 * 检查指定数据源可用来创建数据源是否信息齐备、取值合法
	 * @param aDs
	 */
	public void checkForCreate()
	{
		// 1. 名称不能为空
		Assert.notBlank(getName() , "数据源名称不能为空") ;
		// 2. 类型得合法
		DataSourceType type = getType() ;
		Assert.notNull(type , "数据源类型不能为空") ;
		// 3. devConnInfo不能为null
		Assert.notNull(getDevConnInfo() , "开发环境的连接信息不能为空") ;
		//4 . prodConnInfo不能为null
		Assert.notNull(getProdConnInfo() , "生产环境的连接信息不能为空") ;
		// 5. 开发环境的连接信息要合法
		getDevConnInfo().checkForCreate();
		// 6. 生产环境的连接信息要合法
		getProdConnInfo().checkForCreate();
//		// 7. 创建者的用户id不能为null
//		Assert.notEmpty(getCreatedBy() , "创建者的id不能为null") ;
		// 8. 数据归属
		Assert.notNull(mDataOwner , "数据的归属不能为空！") ;
	}
	
	public void checkForUpdate()
	{
		Assert.notEmpty(mId, "数据源的id不能为空") ;
		checkForCreate(); 
	}
	
	@Schema(hidden = true)
	@JsonIgnore
	public boolean isManaged()
	{
		return mManaged;
	}
	public void setManaged(boolean aManaged)
	{
		if(mManaged != aManaged)
		{
			mManaged = aManaged;
			if(mDevConnInfo != null)
				mDevConnInfo.setManaged(mManaged) ;
			if(mProdConnInfo != null)
				mProdConnInfo.setManaged(mManaged) ;
		}
	}
	
	public void updateFrom(DataSource aOtherDs)
	{
		mType = aOtherDs.mType ;
		mName = aOtherDs.mName ;
		mMark = aOtherDs.mMark ;
		
		mCreateTime = aOtherDs.mCreateTime ;
		mLastEditTime = aOtherDs.mLastEditTime ;
		mLastEditUserId = aOtherDs.mLastEditUserId ;
		mLastEditUserDisplayName = aOtherDs.mLastEditUserDisplayName ;
		mCreateUserId = aOtherDs.mCreateUserId ;
		mCreateUserDisplayName = aOtherDs.mCreateUserDisplayName ;
		
		mDataOwner = aOtherDs.mDataOwner ;
		
		if(JCommon.unequals(mDevConnInfo , aOtherDs.mDevConnInfo))
		{
			if(mDevConnInfo != null)
				StreamAssist.close(mDevConnInfo) ; 
			mDevConnInfo = aOtherDs.mDevConnInfo ;
			if(mDevConnInfo != null)
				mDevConnInfo.setManaged(mManaged) ;
		}
		if(JCommon.unequals(mProdConnInfo, aOtherDs.mProdConnInfo))
		{
			if(mProdConnInfo != null)
				StreamAssist.close(mProdConnInfo) ; 
			mProdConnInfo = aOtherDs.mProdConnInfo ;
			if(mProdConnInfo != null)
				mProdConnInfo.setManaged(true) ;
		}
	}
	
}
