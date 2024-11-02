package team.sailboat.bd.base.bean;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import team.sailboat.bd.base.ZBDException;
import team.sailboat.bd.base.beanch.IFlowDiscovery;
import team.sailboat.bd.base.model.ContextInputParam;
import team.sailboat.bd.base.model.ContextOutputParam;
import team.sailboat.bd.base.model.IFlowNode;
import team.sailboat.bd.base.model.ParamBinding;
import team.sailboat.bd.base.model.ParamIOContext;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dpa.anno.BForwardMethod;
import team.sailboat.commons.fan.dpa.anno.BReverseMethod;
import team.sailboat.commons.fan.lang.Assert;

@Schema(description = "输入/输出和上下文输入输出")
@Data
public class NodeIOParams
{
	static final Logger sLogger = LoggerFactory.getLogger(NodeIOParams.class) ;
	
	@Schema(description = "节点的输入")
	List<NodeInput> inputs ;
	
	@Schema(description = "节点的输出")
	List<NodeOutput> outputs ;
	
	@Schema(description = "上下文输入参数")
	List<NodeInputCtxParam> inputContextParams ;
	
	@Schema(description = "上下文输出参数")
	List<NodeOutputCtxParam> outputContextParams ;

	
	public NodeOutput getOutputByValveId(String aValveId)
	{
		if(XC.isEmpty(outputs))
			return null ;
		for(NodeOutput output : outputs)
		{
			String valveId = output.getValveId() ;
			Assert.notNull(valveId , "输出[%s]的阀id为null" , output.getOutputName()) ;
			if(valveId.equals(aValveId))
			{
				return output ;
			}
		}
		return null ;
	}
	
	public static NodeIOParams of(IFlowNode aFlowNode , IFlowDiscovery aDiscovery)
			throws ZBDException
	{
		NodeIOParams ioParams = new NodeIOParams() ;
		Collection<ParamBinding> inputs = aFlowNode.getInputs() ;
		if(XC.isNotEmpty(inputs))
		{
			ioParams.inputs = NodeInput.ofList(inputs, aDiscovery) ;
			if(inputs.size() != ioParams.inputs.size())
			{
				// 说明有无效的输入参数需要删除
				Set<String> inputValveIds = XC.hashSet() ;
				XC.extract(ioParams.inputs , NodeInput::getValveId , inputValveIds) ;
				for(ParamBinding binding : inputs.toArray(new ParamBinding[0]))
				{
					if(!inputValveIds.contains(binding.getRef()))
					{
						sLogger.warn("节点的输入[{}]无效，将其删除！" , binding.getRef()) ;
						aFlowNode.removeInput(binding.getRef()) ;
					}
				}
			}
		}
		Collection<ParamBinding> outputs = aFlowNode.getOutputs() ;
		if(XC.isNotEmpty(outputs))
		{
			ioParams.outputs = NodeOutput.ofList(outputs, aDiscovery) ;
		}
		ParamIOContext ioCtx = aFlowNode.getParamIOContext() ;
		if(ioCtx != null)
		{
			Collection<ContextInputParam> inParams = ioCtx.getInputParams() ;
			if(XC.isNotEmpty(inParams))
			{
				ioParams.inputContextParams = NodeInputCtxParam.ofList(inParams, aDiscovery) ;
			}
			Collection<ContextOutputParam> outParams = ioCtx.getOutputParams() ;
			if(XC.isNotEmpty(outParams))
			{
				ioParams.outputContextParams = NodeOutputCtxParam.ofList(outParams) ;
			}
		}
		
		return ioParams ;
	}
	
	public static class SerDe
	{
		final static ObjectMapper sObjMappper = new ObjectMapper() ;
		final static ObjectWriter sObjWritter = sObjMappper.writerFor(NodeIOParams.class) ;
		
		@BForwardMethod
		public static String forward(NodeIOParams aSource)
		{
			if(aSource == null)
				return null ;
			StringWriter strWriter = new StringWriter() ;
			try
			{
				sObjWritter.writeValue(strWriter, aSource) ;
				return strWriter.toString() ;
			}
			catch (IOException e)
			{
				e.printStackTrace();
				return null ;
			}
			
		}
		
		@BReverseMethod
		public static NodeIOParams reverse(Object aSource)
		{
			if(aSource == null || aSource.toString().isEmpty())
				return null ;
			try
			{
				return sObjMappper.readValue(aSource.toString() , NodeIOParams.class) ;
			}
			catch (IOException e)
			{
				e.printStackTrace();
				return null ;
			}
		}
	}
}
