package team.sailboat.bd.base.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import team.sailboat.base.util.IWSDBeanIdHelper;
import team.sailboat.bd.base.BdConst;
import team.sailboat.bd.base.def.RunStatus;
import team.sailboat.bd.base.model.dag.InstGenWay;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.time.XTime;

public class RootFlowNodeBase implements IFlowNode
{
	final protected String mId ;
	
	final protected String mName ;
	
	final protected String mDescription = "工作空间根节点" ;
	
	protected Content mContent ;
	
	final protected NodeType mType = NodeType.ZBDVirtual ;
	
	/**
	 * 节点的配置
	 */
	final protected DispatchConfiguration mDispatchConfiguration ;
	
	final protected ParamBinding[] mInputs = new ParamBinding[0] ;
	
	protected List<ParamBinding> mOutputs ;
	
	final protected Date mCreateTime = new Date(0) ;
	
	/**
	 * 上次编辑时间
	 */
	final protected Date mLastEditTime = new Date(0) ;
	
	/**
	 * 责任人id
	 */
	final protected String mResPersonId = BdConst.sUserId_sys ;
	
	protected ParamIOContext mParamIOContext  ;
	
	final String mDefaultValveId ;
	
	public RootFlowNodeBase(String aWsId , String aWsName)
	{
		mId = aWsId+"#root" ;
		mName = aWsName.toLowerCase()+"_root" ;
		mContent = Content.ofHdfs("", 0) ;
		mDispatchConfiguration = new DispatchConfiguration() ;
		mDefaultValveId = IWSDBeanIdHelper.getDefaultValveId(getId()) ;
		mOutputs = Arrays.asList(new ParamBinding(mDefaultValveId, ParamBindingSource.auto)) ;
	}
	
	@Override
	public String getId()
	{
		return mId ;
	}
	
	@Override
	public String getFlowId()
	{
		return null ;
	}
	public boolean setFlowId(String aFlowId)
	{
		throw new IllegalStateException("工作空间根节点，此方法不支持!") ; 
	}
	
	@Override
	public String getName()
	{
		return mName ;
	}
	@Override
	public boolean setName(String aName)
	{
		throw new IllegalStateException("工作空间根节点，此方法不支持!") ; 
	}
	
	@Override
	public boolean isWorkspaceRoot()
	{
		return true ;
	}
	
	@Override
	public Content getContent()
	{
		if(mContent == null)
			mContent = new Content(ContentType.hdfs , "" , 0) ;
		return mContent;
	}
	@Override
	public boolean setContent(Content aContent)
	{
		throw new IllegalStateException("根节点不支持此方法！") ;
	}
	
	@Override
	public NodeType getType()
	{
		return mType;
	}
	@Override
	public boolean setType(NodeType aType)
	{
		throw new IllegalStateException("工作空间根节点，此方法不支持!") ;
	}
	
	@Override
	public String getLastEditUserId()
	{
		return null;
	}
	@Override
	public boolean setLastEditUserId(String aPersonId)
	{
		throw new IllegalStateException("工作空间根节点，此方法不支持!") ; 
	}
	
	@Override
	public long getVersion()
	{
		return 0;
	}
	@Override
	public boolean setVersion(long aVersion)
	{
		throw new IllegalStateException("工作空间根节点，此方法不支持!") ; 
	}
	
	@Override
	public boolean updateCustomParams(Collection<FlowNodeParam> aParams)
	{
		throw new IllegalStateException("工作空间根节点，此方法不支持!") ; 
	}
	
	@Override
	public String getCreateUserId()
	{
		return mResPersonId;
	}
	@Override
	public boolean setCreateUserId(String aResPersonId)
	{
		throw new IllegalStateException("工作空间根节点，此方法不支持!") ; 
	}
	
	public Date getCreateTime()
	{
		return mCreateTime;
	}
	public boolean setCreateTime(Date aCreateTime)
	{
		throw new IllegalStateException("工作空间根节点，此方法不支持!") ; 
	}
	
	public Date getLastEditTime()
	{
		return mLastEditTime;
	}
	public boolean setLastEditTime(Date aLastEditTime)
	{
		throw new IllegalStateException("工作空间根节点，此方法不支持!") ; 
	}
	
	/**
	 * 此节点的输入阀的id
	 * @return
	 */
	@Override
	public Collection<ParamBinding> getInputs()
	{
		return Collections.emptyList() ;
	}
	
	/**
	 * 返回结果不为null
	 * @return
	 */
	@Override
	public ParamBinding[] getInputArray()
	{
		return mInputs ;
	}
	
	/**
	 * 
	 * @param aValveId			阀的id
	 */
	@Override
	public void addInput(String aValveId , ParamBindingSource aBindingSource)
	{
		throw new IllegalStateException("工作空间根节点，此方法不支持!") ; 
	}
	
	/**
	 * 
	 * @param aValveId		阀的id
	 */
	@Override
	public boolean removeInput(String aValveId)
	{
		throw new IllegalStateException("工作空间根节点，此方法不支持!") ; 
	}
	
	/**
	 * 此节点的输出阀的id
	 * @return
	 */
	@Override
	public Collection<ParamBinding> getOutputs()
	{
		return mOutputs;
	}
	
	@Override
	public boolean setOutputs(Collection<ParamBinding> aOutputs)
	{
		throw new IllegalStateException("工作空间根节点，此方法不支持!") ; 
	}
	
	@Override
	public ParamBinding[] getOutputArray()
	{
		return mOutputs.toArray(new ParamBinding[0]) ;
	}
	/**
	 * 
	 * @param aValveId		阀的id
	 */
	@Override
	public void addOutput(String aValveId , ParamBindingSource aBindingSource)
	{
		throw new IllegalStateException("工作空间根节点，此方法不支持!") ; 
	}
	/**
	 * 
	 * @param aValveId		阀的id
	 */
	@Override
	public void removeOutput(String aValveId)
	{
		throw new IllegalStateException("工作空间根节点，此方法不支持!") ; 
	}
	
	public DispatchConfiguration getDispatchConfiguration()
	{
		return mDispatchConfiguration ;
	}
	public boolean setDispatchConfiguration(DispatchConfiguration aDispatchConfiguration)
	{
		throw new IllegalStateException("工作空间根节点，此方法不支持!") ; 
	}
	
	@Override
	public String getDescription()
	{
		return mDescription;
	}
	@Override
	public boolean setDescription(String aDescription)
	{
		throw new IllegalStateException("工作空间根节点，此方法不支持!") ; 
	}
	
	
	@Override
	public List<FlowNodeParam> getParams()
	{
		return null;
	}
	@Override
	public boolean setParams(Collection<FlowNodeParam> aParams)
	{
		throw new IllegalStateException("工作空间根节点，此方法不支持!") ; 
	}
	
	/**
	 * 节点缺省的输出id
	 * @return
	 */
	@Override
	public String getDefaultValveId()
	{
		return mDefaultValveId ;
	}
	@Override
	public FlowNodeRunRecord getRunRecord()
	{
		return null ;
	}
	@Override
	public void setRunRecord(FlowNodeRunRecord aRunRecord)
	{
		throw new IllegalStateException("工作空间根节点，此方法不支持!") ; 
	}
	@Override
	public boolean setRunRecord(long aContentVersion, String aExecId, RunStatus aRunStatus)
	{
		throw new IllegalStateException("工作空间根节点，此方法不支持!") ; 
	}
	@Override
	public RunStatus getCurrentRunStatus()
	{
		return RunStatus.norun ;
	}
	
	@Override
	public ParamIOContext getParamIOContext()
	{
		return mParamIOContext;
	}

	@Override
	public boolean setInputs(Collection<ParamBinding> aInputs)
	{
		throw new IllegalStateException("工作空间根节点，此方法不支持!") ; 
	}

	@Override
	public boolean setInstGenWay(InstGenWay aInstGenWay)
	{
		throw new IllegalStateException("工作空间根节点，此方法不支持!") ; 
	}

	@Override
	public boolean setRunWithNoLoad(boolean aNoLoad)
	{
		throw new IllegalStateException("工作空间根节点，此方法不支持!") ; 
	}

	@Override
	public boolean setValidTimeSpaceLower(Date aLower)
	{
		throw new IllegalStateException("工作空间根节点，此方法不支持!") ; 
	}

	@Override
	public boolean setValidTimeSpaceUpper(Date aUpper)
	{
		throw new IllegalStateException("工作空间根节点，此方法不支持!") ; 
	}

	@Override
	public boolean setSchedule(String aSchedule)
	{
		throw new IllegalStateException("工作空间根节点，此方法不支持!") ; 
	}

	@Override
	public boolean setParamIOContext(ParamIOContext aParamIOContext)
	{
		throw new IllegalStateException("工作空间根节点，此方法不支持!") ; 
	}
	
	@Override
	public JSONObject toDefinition()
	{
		return new JSONObject().put("id" , mId)
				.put("name" , mName)
//				.put("flowId", mFlowId)
				.put("description", mDescription)
				.put("content" , new JSONObject(mContent))
				.put("type" , mType)
				.put("dispatchConfiguration" , new JSONObject(mDispatchConfiguration))
				.put("createTime" , XTime.format$yyyyMMddHHmmssSSS(mCreateTime , null))
				.put("lastEditTime" , XTime.format$yyyyMMddHHmmssSSS(mLastEditTime , null))
//				.put("lastEditPersonId" , mLastEditPersonId)
				.put("resPersonId" , mResPersonId)
//				.put("arguments" , mArguments)
				.put("inputs", new JSONArray(mInputs))
				.put("outputs", new JSONArray(mOutputs))
				.put("paramIOContext" , new JSONObject(mParamIOContext)) ;
	}
}
