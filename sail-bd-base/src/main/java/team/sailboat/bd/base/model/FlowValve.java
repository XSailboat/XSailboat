package team.sailboat.bd.base.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dpa.anno.BColumn;
import team.sailboat.commons.fan.dpa.anno.BDataType;
import team.sailboat.commons.fan.dpa.anno.BFeature;
import team.sailboat.commons.fan.dpa.anno.BTable;
import team.sailboat.commons.fan.dtool.DBType;
import team.sailboat.commons.fan.dtool.mysql.MySQLFeatures;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.dplug.anno.DBean;

/**
 * 流阀，用来作为输入输出		<br />
 * 因为它是全内存加载的，所以采用DBean方式实现会更合适		<br />
 * 一个阀有三种状态：成功、失败、未运行
 *
 * @author yyl
 * @since 2021年6月16日
 */
@BTable(name="flow_valve" , comment="流阀"  , features = {
		@BFeature(type = DBType.MySQL , name = MySQLFeatures.TABLE__ENGINE , value = "InnoDB") , 
		@BFeature(type = DBType.MySQL , name = MySQLFeatures.TABLE__CHARACTER_SET , value = "utf8")}
)
@DBean
public class FlowValve implements IFlowValve
{
	
	@BColumn(name = "id" , primary = true , dataType = @BDataType(name="string" , length = 32) , comment = "唯一性标识，自动生成" , seq = 0)
	String mId ;
	
	/**
	 * 以此阀作为输出的节点id
	 */
	@BColumn(name="sourceNodeId" , dataType=@BDataType(name="string" , length=1024) , comment="源节点id" , seq = 1)
	String mSourceNodeId ;
	
	/**
	 * 以此输出作为输入的节点id
	 */
	@BColumn(name="targetNodeIds" , dataType=@BDataType(name="string" , length=1024) , comment="目标节点id" , seq = 2)
	LinkedHashSet<String> mTargetNodeIds ;
	
	@BColumn(name="source" , dataType=@BDataType(name="string" , length=32) , comment="阀的来源" , seq = 3)
	ParamSource mSource ;
	
	public FlowValve()
	{
	}
	
	public void addTargetNodeIds(Collection<String> aNodeIds)
	{
		if(aNodeIds == null)
			return ;
		if(mTargetNodeIds == null)
			mTargetNodeIds = XC.linkedHashSet() ;
		if(mTargetNodeIds.addAll(aNodeIds))
			setChanged("mTargetNodeIds", mTargetNodeIds) ;
	}
	
	@Override
	public void setTargetNodeIds(String... aNodeIds)
	{
		boolean empty_new = XC.isEmpty(aNodeIds) ;
		boolean empty_old =  XC.isEmpty(mTargetNodeIds) ;
		if(empty_new && empty_old)
		{
			return ;
		}
		if(empty_new)
		{
			Object oldValve = mTargetNodeIds ;
			mTargetNodeIds = null;
			setChanged("mTargetNodeIds", mTargetNodeIds , oldValve) ;
		}
		else
		{
			Object oldValve = mTargetNodeIds ;
			mTargetNodeIds = XC.linkedHashSet(aNodeIds) ;
			setChanged("mTargetNodeIds", mTargetNodeIds, oldValve) ;
		}
	}
	
	public void addTargetNodeId(String aNodeId)
	{
		if(mTargetNodeIds == null)
			mTargetNodeIds = XC.linkedHashSet() ;
		if(mTargetNodeIds.add(aNodeId))
			setChanged("mTargetNodeId", mTargetNodeIds) ;
	}
	
	public boolean removeTargetNodeId(String aNodeId)
	{
		if(mTargetNodeIds != null && mTargetNodeIds.remove(aNodeId))
		{
			setChanged("mTargetNodeId", mTargetNodeIds) ;
			return true ;
		}
		return false ;
	}
	@Override
	public Set<String> getTargetNodeIds()
	{
		return mTargetNodeIds == null ? Collections.emptySet() : mTargetNodeIds ;
	}
	
	public ParamSource getSource()
	{
		return mSource;
	}
	public void setSource(ParamSource aSource)
	{
		if(JCommon.unequals(aSource, mSource))
		{
			ParamSource oldValue = mSource ;
			mSource = aSource;
			setChanged("mSource", mSource , oldValue);
		}
	}
	
}
