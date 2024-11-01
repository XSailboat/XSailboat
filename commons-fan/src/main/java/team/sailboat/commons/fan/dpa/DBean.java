package team.sailboat.commons.fan.dpa;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.struct.Tuples;
import team.sailboat.commons.fan.text.XString;

/**
 * 主键字段一旦设置，就不允许修改
 *
 * @author yyl
 * @since Dec 24, 2020
 */
public abstract class DBean implements ToJSONObject
{
	private static final Map<String , DTableDesc> sTblDescMap = new HashMap<>() ;
	
	private DRepository mRepository ;
	
	private String mBid ;
	
	private DBeanStatus mStatus = DBeanStatus.NEW  ;
	
	protected DBean()
	{
	}
	
	DBean _setRepository(DRepository aRepository)
	{
		if(mRepository != aRepository)
		{
			if(mRepository != null)
				mRepository.beanDeleted(this) ;
			mRepository = aRepository ;
			if(mRepository != null)
				mRepository.beanCreated(this) ;
		}
		return this ;
	}
	
	synchronized void delete()
	{
		if(mStatus == DBeanStatus.NEW)
		{
			mRepository.removeDirectly(this) ;
			mStatus = DBeanStatus.DELETED ;
		}
		else if(mStatus == DBeanStatus.DELETED || mStatus == DBeanStatus.DELETED)
			return ;
		else
		{
			DRepository repo = mRepository ;
			mRepository.removeDirectly(this) ;
			mStatus = DBeanStatus.DELETING ;
			repo.addCommit(this) ;
		}
	}
	
	synchronized Tuples.T2<DOper, Object[]> flush()
	{
		Tuples.T2<DOper, Object[]> photo = null ;
		DTableDesc tblDesc = getTableDesc(getClass()) ;
		List<Object> argList = XC.arrayList() ;
		try
		{
			if(mStatus == DBeanStatus.NEW)
			{
				for(ColumnMeta col : tblDesc.getColumns())
				{
					argList.add(XClassUtil.typeAdapt(col.mSerDe.forward(col.getField().get(this)) , col.getAnnotation().dataType().name())) ;
				}
				photo = Tuples.of(DOper.INSERT , argList.toArray()) ;
				mStatus = DBeanStatus.SYNC ;
			}
			else if(mStatus == DBeanStatus.DELETING)
			{
				Collection<ColumnMeta> cols = tblDesc.getPKColumns() ;
				for(ColumnMeta col : cols)
				{
					argList.add(XClassUtil.typeAdapt(col.mSerDe.forward(col.getField().get(this)) , col.getAnnotation().dataType().name())) ;
				}
				photo = Tuples.of(DOper.DELETE , argList.toArray()) ;
				mStatus = DBeanStatus.DELETED ;
			}
			else if(mStatus == DBeanStatus.CHANGED)
			{
				Collection<ColumnMeta> cols = tblDesc.getPKColumns() ;
				for(ColumnMeta col : tblDesc.getColumns())
				{
					if(col.getAnnotation().primary())
						continue ;
					argList.add(XClassUtil.typeAdapt(col.mSerDe.forward(col.getField().get(this)) , col.getAnnotation().dataType().name())) ;
				}
				for(ColumnMeta col : cols)
				{
					argList.add(XClassUtil.typeAdapt(col.mSerDe.forward(col.getField().get(this)) , col.getAnnotation().dataType().name())) ;
				}
				photo = Tuples.of(DOper.UPDATE , argList.toArray()) ;
				mStatus = DBeanStatus.SYNC ;
			}
		}
		catch (IllegalArgumentException | IllegalAccessException e)
		{
			WrapException.wrapThrow(e) ;
		}
		if(mRepository != null)
			mRepository.removeCommit(this) ;
		return photo ;
	}
	
	public DBeanStatus getStatus()
	{
		return mStatus;
	}
	
	protected void setChanged(String aPropertyName , Object aNewVal)
	{
		if(mStatus == DBeanStatus.NEW || mStatus == DBeanStatus.CHANGED)
		{
			if(mRepository != null)
				mRepository._propertyChanged(this , aPropertyName ,  aNewVal) ;
			return ;
		}
		if(mStatus == DBeanStatus.SYNC)
		{
			mStatus = DBeanStatus.CHANGED ;
			mRepository.addCommit(this) ;
			if(mRepository != null)
				mRepository._propertyChanged(this , aPropertyName , aNewVal) ;
		}
		else
			throw new IllegalStateException(XString.msgFmt("当前DBean处于状态{}，不能setChanged()" , mStatus.name())) ;
	}
	
	protected void setChanged(String aPropertyName , Object aNewVal , Object aOldValue)
	{
		if(mStatus == DBeanStatus.NEW || mStatus == DBeanStatus.CHANGED)
		{
			if(mRepository != null)
				mRepository._propertyChanged(this , aPropertyName , aNewVal , aOldValue) ;
			return ;
		}
		if(mStatus == DBeanStatus.SYNC)
		{
			mStatus = DBeanStatus.CHANGED ;
			mRepository.addCommit(this) ;
			if(mRepository != null)
				mRepository._propertyChanged(this , aPropertyName , aNewVal , aOldValue) ;
		}
		else
			throw new IllegalStateException(XString.msgFmt("当前DBean处于状态{}，不能setChanged()" , mStatus.name())) ;
	}
	
	/**
	 * 只可以在实现IDBeanFactory的中加载bean的过程中使用		<br />
	 * 
	 */
	public void _setLoaded()
	{
		Assert.isTrue(mStatus == DBeanStatus.NEW) ;
		mStatus = DBeanStatus.SYNC ;
	}
	
	protected boolean canSet()
	{
		return mStatus != DBeanStatus.DELETING || mStatus != DBeanStatus.DELETED ;
	}

	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		return aJSONObj ;
	}
	
	@Override
	public String toString()
	{
		return toJSONString() ;
	}
	
	public static DTableDesc getTableDesc(Class<? extends DBean> aClazz)
	{
		if(aClazz == null)
			return null ;
		DTableDesc tblDesc = sTblDescMap.get(aClazz.getName()) ;
		if(tblDesc == null)
		{
			synchronized (aClazz.getClass().getName().intern())
			{
				tblDesc = sTblDescMap.get(aClazz.getName()) ;
				if(tblDesc == null)
				{
					tblDesc = DTableDesc.build(aClazz) ;
					sTblDescMap.put(aClazz.getName() , tblDesc) ;
				}
			}
		}
		return tblDesc ;
	}
	
	public static String getBID(DBean aBean)
	{
		if(aBean == null)
			return null ;
		if(aBean.mBid == null)
		{
			Function<DBean, String> bidGen = getTableDesc(aBean.getClass()).getBidGenerator() ;
			aBean.mBid = bidGen == null?"":bidGen.apply(aBean) ;
		}
		return aBean.mBid ;
	}
	
	
}
