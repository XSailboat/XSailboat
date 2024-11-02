package team.sailboat.bd.base.model;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import team.sailboat.bd.base.def.RunStatus;
import team.sailboat.bd.base.model.dag.InstGenWay;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.text.XString;

public interface IFlowNode extends INode
{
	String getDescription() ;
	
	boolean setDescription(String aDescription) ;
	/**
	 * 取得流程id
	 * @return
	 */
	String getFlowId() ;
	boolean setFlowId(String aFlowId) ;
	
	NodeType getType() ;
	boolean setType(NodeType aType) ;
	
//	String getArguments() ;
//	boolean setArguments(String aArguments) ;
	
	List<FlowNodeParam> getParams() ;
	boolean setParams(Collection<FlowNodeParam> aParams) ;
	
	/**
	 * 更新自定义参数
	 * @param aParams
	 * @return
	 */
	boolean updateCustomParams(Collection<FlowNodeParam> aParams) ;
	
	/**
	 * 取得节点输入阀
	 * @return
	 */
	Collection<ParamBinding> getInputs() ;
	
	ParamBinding[] getInputArray() ;
	
	default List<String> getInputValveIds()
	{
		List<String> idList = XC.arrayList() ;
		for(ParamBinding binding : getInputArray())
			idList.add(binding.getRef()) ;
		return idList ;
	}
	
	boolean removeInput(String aValveId) ;
	
	void addInput(String aValveId , ParamBindingSource aBindingSource) ;
	
	boolean setInputs(Collection<ParamBinding> aInputs) ;
	
	/**
	 * 取得节点的输出阀
	 * @return
	 */
	Collection<ParamBinding> getOutputs() ;
	
	boolean setOutputs(Collection<ParamBinding> aOutputs) ;
	
	ParamBinding[] getOutputArray() ;
	
	default List<String> getOutputValveIds()
	{
		List<String> idList = XC.arrayList() ;
		for(ParamBinding binding : getOutputArray())
			idList.add(binding.getRef()) ;
		return idList ;
	}
	
	void addOutput(String aValveId , ParamBindingSource aBindingSource) ;
	
	void removeOutput(String aValveId) ;
	
	DispatchConfiguration getDispatchConfiguration() ;
	
	boolean setDispatchConfiguration(DispatchConfiguration aDispatchConfiguration) ;
	
	boolean setInstGenWay(InstGenWay aInstGenWay) ;
	
	boolean setRunWithNoLoad(boolean aNoLoad) ;
	
	boolean setValidTimeSpaceLower(Date aLower) ;
	
	boolean setValidTimeSpaceUpper(Date aUpper) ;
	
	boolean setSchedule(String aSchedule) ;
	
	/**
	 * 取得节点的输入/输出上下文
	 * @return
	 */
	ParamIOContext getParamIOContext() ;
	
	boolean setParamIOContext(ParamIOContext aParamIOContext) ;
	/**
	 * 
	 * @return     返回结果不为null
	 */
	Content getContent() ;
	boolean setContent(Content aContent) ;
	
	String getCreateUserId() ;
	boolean setCreateUserId(String aUserId) ;
	
	String getDefaultValveId() ;
	
	Date getCreateTime() ;
	
	boolean setCreateTime(Date aCreateTime) ;
	
	Date getLastEditTime() ;
	boolean setLastEditTime(Date aLastEditTime) ;
	
	String getLastEditUserId() ;
	boolean setLastEditUserId(String aUserId) ;
	
	FlowNodeRunRecord getRunRecord() ;
	
	/**
	 * 取得节点当前的运行状态。		<br />
	 * FlowNodeRunRecord 记录的是上次的运行状态，如果其中记录的内容版本和当前content的版本相同，
	 * 则FlowNodeRunRecord中的状态是当前的运行状态
	 * @return				返回结果不为null
	 */
	RunStatus getCurrentRunStatus() ;
	
	void setRunRecord(FlowNodeRunRecord aRunRecord) ;
	
	boolean setRunRecord(long aContentVersion , String aExecId , RunStatus aRunStatus) ;
	
	/**
	 * 是否是工作空间根节点
	 * @return
	 */
	boolean isWorkspaceRoot() ;
	
	JSONObject toDefinition() ;
	
	public static boolean isRootId(String aId)
	{
		return XString.isNotEmpty(aId) && aId.endsWith("#root") ;
	}
}
