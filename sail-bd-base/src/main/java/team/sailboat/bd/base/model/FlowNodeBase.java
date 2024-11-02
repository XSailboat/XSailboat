package team.sailboat.bd.base.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import team.sailboat.base.util.IWSDBeanIdHelper;
import team.sailboat.bd.base.def.FlowNodeParamType;
import team.sailboat.bd.base.def.RunStatus;
import team.sailboat.bd.base.model.dag.InstGenWay;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dpa.anno.BColumn;
import team.sailboat.commons.fan.dpa.anno.BDataType;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.time.XTime;
import team.sailboat.dplug.anno.DBean;

@DBean
public abstract class FlowNodeBase implements IFlowNode
{
	
	@BColumn(name = "id" , primary = true , dataType = @BDataType(name="string" , length = 32) , comment = "唯一性标识，自动生成" , seq = 0)
	protected String mId ;
	
	@BColumn(name = "name" , dataType = @BDataType(name="string" , length = 32) , comment = "节点名称" , seq = 1)
	protected String mName ;
	
	/**
	 * 所属流程id
	 */
	@BColumn(name = "flow_id" , dataType = @BDataType(name="string" , length = 32) , comment = "流程id" , seq = 2)
	protected String mFlowId ;
	
	@BColumn(name="description" , dataType = @BDataType(name="string" , length = 512) , comment = "描述" , seq = 3)
	protected String mDescription ;
	
	/**
	 * 节点内容
	 */
	@BColumn(name = "content" , dataType = @BDataType(name="string" , length = 1024) , comment = "内容" , seq = 4 , serDeClass = Content.class)
	protected Content mContent ;
	
	/**
	 * 节点类型
	 */
	@BColumn(name = "type" , dataType = @BDataType(name="string" , length = 32) , comment = "节点类型" , seq = 5)
	protected NodeType mType ;
	
	/**
	 * 节点的配置
	 */
	@BColumn(name = "dispatch_conf" , dataType = @BDataType(name="string" , length = 2048) , comment = "节点调度参数配置" , seq = 6 , serDeClass = DispatchConfiguration.SerDe.class)
	protected DispatchConfiguration mDispatchConfiguration ;
	
	/**
	 * 输入id
	 */
	@BColumn(name="inputs" , dataType = @BDataType(name="string" , length = 1024) , comment="输入" , seq = 8
			, deserClass = ParamBinding.Deser_ArrayList.class)
	protected List<ParamBinding> mInputs ;
	
	/**
	 * 输出的id
	 */
	@BColumn(name="outputs" , dataType=@BDataType(name="string" , length = 1024) , comment="输出" , seq = 9
			, deserClass = ParamBinding.Deser_ArrayList.class)
	protected List<ParamBinding> mOutputs ;
	
	/**
	 * 创建时间
	 */
	@BColumn(name="create_time" , dataType=@BDataType(name="datetime") , comment = "创建时间" , seq = 10)
	protected Date mCreateTime ;
	
	/**
	 * 创建人id
	 */
	@BColumn(name="create_userid" , dataType=@BDataType(name="string" , length = 32) , comment = "责任人id" , seq = 13)
	protected String mCreateUserId ;
	
	/**
	 * 上次编辑时间
	 */
	@BColumn(name="last_edit_time" , dataType=@BDataType(name="datetime") , comment = "最近一次修改时间" , seq = 11)
	protected Date mLastEditTime ;
	
	@BColumn(name="last_edit_userid" , dataType=@BDataType(name="string" , length = 32) , comment = "最近一次修改的用户id" , seq = 12)
	protected String mLastEditUserId ;
	
	@BColumn(name="param_io_context" , dataType=@BDataType(name="string" , length= 2048) , comment="节点上下文" , seq = 14
			, serDeClass = ParamIOContext.SerDe.class)
	protected ParamIOContext mParamIOContext  ;
	
	@BColumn(name="run_record" , dataType=@BDataType(name="string" , length = 512) , comment="节点上次运行记录，JSON格式" , seq=15
			, serDeClass = FlowNodeRunRecord.class)
	protected FlowNodeRunRecord mRunRecord ;
	
//	@BColumn(name="arguments" , dataType=@BDataType(name="string" , length = 512) , comment="参数" , seq=16)
//	protected String mArguments ;
	
	@BColumn(name="params" , dataType=@BDataType(name="string" , length = 1024) , comment="参数" , seq=16
			, serDeClass = FlowNodeParam.SerDe_List.class)
	protected List<FlowNodeParam> mParams ;
	
	@BColumn(name="version" , dataType=@BDataType(name="long") , comment="版本" , seq=17)
	protected Long mVersion ;
	
	String mDefaultValveId ;
	
	public FlowNodeBase()
	{
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
		if(JCommon.unequals(mContent , aContent))
		{
			Content oldValue = mContent ;
			if(oldValue != null)
				Assert.isTrue(oldValue.getVersion()<aContent.getVersion()) ;
			mContent = aContent;
			setChanged("mContent" , mContent , oldValue);
			return true ;
		}
		return false ;
	}
	
	@Override
	public long getVersion()
	{
		if(mVersion == null)
			mVersion = mLastEditTime==null?0:mLastEditTime.getTime() ;
		return mVersion ;
	}
	@Override
	public boolean setVersion(long aVersion)
	{
		if(mVersion == null || mVersion.longValue() != aVersion)
		{
			Object oldValue = mVersion ;
			mVersion = aVersion ;
			setChanged("mVersion" , mVersion , oldValue) ;
			return true ;
		}
		return false;
	}
	
	@Override
	public boolean setLastEditTime(Date aLastEditTime)
	{
		Assert.notNull(aLastEditTime, "参数不能为null") ;
		if(mLastEditTime == null || aLastEditTime.after(mLastEditTime))
		{
			Date oldValue = mLastEditTime ;
			mLastEditTime = aLastEditTime;
			setChanged("mLastEditTime" , mLastEditTime, oldValue);
			return true ;
		}
		return false ;
	}
	
	/**
	 * 此节点的输入阀的id
	 * @return
	 */
	@Override
	public Collection<ParamBinding> getInputs()
	{
		return mInputs;
	}
	
	/**
	 * 返回结果不为null
	 * @return
	 */
	@Override
	public ParamBinding[] getInputArray()
	{
		return mInputs == null ? new ParamBinding[0] : mInputs.toArray(new ParamBinding[0]) ;
	}
	
	/**
	 * 
	 * @param aValveId			阀的id
	 */
	@Override
	public void addInput(String aValveId , ParamBindingSource aBindingSource)
	{
		if(mInputs == null)
			mInputs = XC.arrayList() ;
		else
		{
			for(ParamBinding binding : mInputs)
			{
				if(JCommon.equals(binding.getRef() , aValveId))
				{
					if(binding.getSource() != aBindingSource)
					{
						binding.setSource(aBindingSource) ;
						setChanged("mInputs", mInputs);
					}
					return ;
				}
			}
		}
		mInputs.add(new ParamBinding(aValveId, aBindingSource)) ;
		setChanged("mInputs", mInputs);
	}
	/**
	 * 
	 * @param aValveId		阀的id
	 */
	@Override
	public boolean removeInput(String aValveId)
	{
		if(mInputs != null)
		{
			Iterator<ParamBinding> it = mInputs.iterator() ;
			while(it.hasNext())
			{
				ParamBinding binding = it.next() ;
				if(JCommon.equals(binding.getRef() , aValveId))
				{
					it.remove();
					setChanged("mInputs", mInputs);
					return true ;
				}
			}
		}
		return false ;
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
		if(XC.isNotEmpty(aOutputs))
		{
			if(mOutputs == null)
			{
				mOutputs = XC.arrayList() ;
			}
			else if(!mOutputs.isEmpty())
			{
				ParamBinding[] inputs = mOutputs.toArray(new ParamBinding[0]) ;
				ParamBinding[] newInputs = aOutputs.toArray(new ParamBinding[0]) ;
				if(JCommon.equals(inputs , newInputs))
					return false ;
				mOutputs.clear() ;
			}
			mOutputs.addAll(aOutputs) ; 
			setChanged("mOutputs" , mOutputs) ;
			return true ;
		}
		else if(XC.isNotEmpty(mOutputs))
		{
			Object oldValue = mOutputs ;
			mOutputs = null ;
			setChanged("mOutputs" , mOutputs , oldValue);
			return true ;
		}
		return false ;
	}
	
	@Override
	public ParamBinding[] getOutputArray()
	{
		return mOutputs == null ? new ParamBinding[0] : mOutputs.toArray(new ParamBinding[0]) ;
	}
	
	/**
	 * 
	 * @param aValveId		阀的id
	 */
	@Override
	public void addOutput(String aValveId , ParamBindingSource aBindingSource)
	{
		if(mOutputs == null)
			mOutputs = XC.arrayList() ;
		else
		{
			for(ParamBinding binding : mOutputs)
			{
				if(JCommon.equals(binding.getRef() , aValveId))
				{
					if(binding.getSource() != aBindingSource)
					{
						binding.setSource(aBindingSource) ;
						setChanged("mOutputs", mOutputs);
					}
					return ;
				}
			}
		}
		mOutputs.add(new ParamBinding(aValveId, aBindingSource)) ;
		setChanged("mOutputs", mOutputs);
	}
	/**
	 * 
	 * @param aValveId		阀的id
	 */
	@Override
	public void removeOutput(String aValveId)
	{
		if(mOutputs != null)
		{
			Iterator<ParamBinding> it = mOutputs.iterator() ;
			while(it.hasNext())
			{
				ParamBinding binding = it.next() ;
				if(JCommon.equals(binding.getRef() , aValveId))
				{
					it.remove();
					setChanged("mOutputs", mOutputs);
					break ;
				}
			}
		}
	}
	
	@Override
	public DispatchConfiguration getDispatchConfiguration()
	{
		return mDispatchConfiguration;
	}
	@Override
	public boolean setDispatchConfiguration(DispatchConfiguration aDispatchConfiguration)
	{
		if(JCommon.unequals(mDispatchConfiguration, aDispatchConfiguration))
		{
			DispatchConfiguration oldValue = aDispatchConfiguration ;
			mDispatchConfiguration = aDispatchConfiguration;
			setChanged("mDispatchConfiguration", mDispatchConfiguration, oldValue) ;
			return true ;
		}
		return false ;
	}
	
	@Override
	public String getDescription()
	{
		return mDescription;
	}
	@Override
	public boolean setDescription(String aDescription)
	{
		if(JCommon.unequals(aDescription, mDescription))
		{
			String oldValue = mDescription ;
			mDescription = aDescription ;
			setChanged("mDescription" , mDescription , oldValue);
			return true ;
		}
		return false ;
	}
	
//	@Override
//	public String getArguments()
//	{
//		return mArguments;
//	}
//	@Override
//	public boolean setArguments(String aArguments)
//	{
//		if(JCommon.unequals(mArguments, aArguments))
//		{
//			Object oldValue = aArguments ;
//			mArguments = aArguments;
//			setChanged("mArguments" , mArguments, oldValue);
//			return true ;
//		}
//		return false ;
//	}
	
	@Override
	public List<FlowNodeParam> getParams()
	{
		return mParams ;
	}
	@Override
	public boolean setParams(Collection<FlowNodeParam> aParams)
	{	
		if(XC.isEmpty(aParams))
		{
			if(XC.isEmpty(mParams))
				return false ;
			else
			{
				Object oldValue = mParams ;
				mParams = null ;
				setChanged("mParams" , mParams , oldValue) ;
				return true ;
			}
		}
		else
		{
			List<FlowNodeParam> newList = new ArrayList<>(aParams) ;
			if(newList.equals(mParams))
				return false ;
			else
			{
				Object oldValue = mParams ;
				mParams = newList ;
				setChanged("mParams" , mParams , oldValue) ;
				return true ;
			}
		}
	}
	
	@Override
	public boolean updateCustomParams(Collection<FlowNodeParam> aParams)
	{
		List<FlowNodeParam> oldCustomParams = XC.filter(mParams , param->param.getType()==FlowNodeParamType.CustomParam) ;
		List<FlowNodeParam> newCustomParams = XC.filter(aParams , param->param.getType()==FlowNodeParamType.CustomParam) ;
		if(!JCommon.equals(oldCustomParams , newCustomParams))
		{
			if(XC.isNotEmpty(mParams))
			{
				Iterator<FlowNodeParam> it = mParams.iterator() ;
				while(it.hasNext())
				{
					FlowNodeParam param = it.next() ;
					if(param.getType() == FlowNodeParamType.CustomParam)
						it.remove(); 
				}
			}
			if(XC.isNotEmpty(aParams))
			{
				for(FlowNodeParam param : aParams)
				{
					if(param.getType() == FlowNodeParamType.CustomParam)
					{
						if(mParams == null)
							mParams = XC.arrayList() ;
						mParams.add(param) ;
					}
				}
			}
			return true ;
		}
		return false ;
	}
	
	@Override
	public boolean setRunRecord(long aContentVersion , String aExecId , RunStatus aRunStatus)
	{
		FlowNodeRunRecord rcd = new FlowNodeRunRecord(aContentVersion , aExecId , aRunStatus) ;
		if(JCommon.unequals(rcd , mRunRecord))
		{
			Object oldValue = mRunRecord ;
			mRunRecord = rcd ;
			setChanged("mRunRecord" , mRunRecord , oldValue) ;
			return true ;
		}
		return false ;
	}
	
	@Override
	public FlowNodeRunRecord getRunRecord()
	{
		return mRunRecord;
	}
	@Override
	public void setRunRecord(FlowNodeRunRecord aRunRecord)
	{
		if(JCommon.unequals(mRunRecord , aRunRecord))
		{
			Object oldValue = mRunRecord ;
			mRunRecord = aRunRecord ;
			setChanged("mRunRecord", mRunRecord, oldValue);
		}
	}
	
	@Override
	public RunStatus getCurrentRunStatus()
	{
		FlowNodeRunRecord rcd = mRunRecord ;
		if(rcd == null || rcd.getContentVersion() != getContent().getVersion())
			return RunStatus.norun ;
		return rcd.getStatus() ;
	}
	
	@Override
	public ParamIOContext getParamIOContext()
	{
		if(mParamIOContext == null)
			mParamIOContext = new ParamIOContext() ;
		return mParamIOContext ;
	}
	@Override
	public boolean setParamIOContext(ParamIOContext aParamIOContext)
	{
		if(JCommon.unequals(mParamIOContext, aParamIOContext))
		{
			Object oldValue = mParamIOContext ;
			mParamIOContext = aParamIOContext;
			setChanged("mParamIOContext" , mParamIOContext, oldValue);
			return true ;
		}
		return false ;
	}
	
	@Override
	public boolean isWorkspaceRoot()
	{
		return false;
	}

	@Override
	public boolean setInputs(Collection<ParamBinding> aInputs)
	{
		if(XC.isNotEmpty(aInputs))
		{
			if(mInputs == null)
			{
				mInputs = XC.arrayList() ;
			}
			else if(!mInputs.isEmpty())
			{
				ParamBinding[] inputs = mInputs.toArray(new ParamBinding[0]) ;
				ParamBinding[] newInputs = aInputs.toArray(new ParamBinding[0]) ;
				if(JCommon.equalsA(inputs , newInputs))
					return false ;
				mInputs.clear() ;
			}
			mInputs.addAll(aInputs) ; 
			setChanged("mInputs" , mInputs) ;
			return true ;
		}
		else if(XC.isNotEmpty(mInputs))
		{
			Object oldValue = mInputs ;
			mInputs = null ;
			setChanged("mInputs" , mInputs , oldValue);
			return true ;
		}
		return false ;
	}

	@Override
	public boolean setInstGenWay(InstGenWay aInstGenWay)
	{
		boolean changed = false ;
		if(mDispatchConfiguration != null)
			changed = mDispatchConfiguration.setInstGenWay(aInstGenWay) ;
		else
		{
			mDispatchConfiguration = new DispatchConfiguration() ;
			mDispatchConfiguration.setInstGenWay(aInstGenWay) ;
			changed = true ;
		}
		if(changed)
			setChanged("mDispatchConfiguration" , mDispatchConfiguration) ;
		return changed ;
	}

	@Override
	public boolean setRunWithNoLoad(boolean aNoLoad)
	{
		boolean changed = false ;
		if(mDispatchConfiguration != null)
			changed = mDispatchConfiguration.setRunWithNoLoad(aNoLoad);
		else
		{
			mDispatchConfiguration = new DispatchConfiguration() ;
			mDispatchConfiguration.setRunWithNoLoad(aNoLoad) ;
			changed = true ;
		}
		if(changed)
			setChanged("mDispatchConfiguration" , mDispatchConfiguration) ;
		return changed ;
	}
	
	@Override
	public boolean setSchedule(String aSchedule)
	{
		boolean changed = false ;
		if(mDispatchConfiguration != null)
			changed = mDispatchConfiguration.setSchedule(aSchedule) ;
		else
		{
			mDispatchConfiguration = new DispatchConfiguration() ;
			mDispatchConfiguration.setSchedule(aSchedule) ;
			changed = true ;
		}
		if(changed)
			setChanged("mDispatchConfiguration" , mDispatchConfiguration) ;
		return changed ;
	}

	@Override
	public boolean setValidTimeSpaceLower(Date aLower)
	{
		boolean changed = false ;
		if(mDispatchConfiguration != null)
			changed = mDispatchConfiguration.setValidTimeSpaceLower(aLower);
		else
		{
			mDispatchConfiguration = new DispatchConfiguration() ;
			mDispatchConfiguration.setValidTimeSpaceLower(aLower) ;
			changed = true ;
		}
		if(changed)
			setChanged("mDispatchConfiguration" , mDispatchConfiguration) ;
		return changed ;
	}

	@Override
	public boolean setValidTimeSpaceUpper(Date aUpper)
	{
		boolean changed = false ;
		if(mDispatchConfiguration != null)
			changed = mDispatchConfiguration.setValidTimeSpaceUpper(aUpper) ;
		else
		{
			mDispatchConfiguration = new DispatchConfiguration() ;
			mDispatchConfiguration.setValidTimeSpaceUpper(aUpper) ;
			changed = true ;
		}
		if(changed)
			setChanged("mDispatchConfiguration" , mDispatchConfiguration) ;
		return changed ;
	}

	/**
	 * 节点缺省的输出id
	 * @return
	 */
	@Override
	public String getDefaultValveId()
	{
		if(mDefaultValveId == null)
			mDefaultValveId = IWSDBeanIdHelper.getDefaultValveId(getId()) ;
		return mDefaultValveId ;
	}
	
	
	public JSONObject toDefinition()
	{
		return new JSONObject().put("id" , mId)
				.put("name" , mName)
				.put("flowId", mFlowId)
				.put("description", mDescription)
				.put("content" , new JSONObject(mContent))
				.put("type" , mType)
				.put("dispatchConfiguration" , new JSONObject(mDispatchConfiguration))
				.put("createTime" , XTime.format$yyyyMMddHHmmssSSS(mCreateTime , null))
				.put("createUserId" , mCreateUserId)
				.put("lastEditTime" , XTime.format$yyyyMMddHHmmssSSS(mLastEditTime , null))
				.put("lastEditUserId" , mLastEditUserId)
//				.put("arguments" , mArguments)
				.putIf(XC.isNotEmpty(mParams) , "params" , JSONArray.of(mParams))
				.put("inputs", JSONArray.of(mInputs))
				.put("outputs", JSONArray.of(mOutputs))
				.put("paramIOContext" , new JSONObject(mParamIOContext)) ;
	}

}
