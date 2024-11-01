package team.sailboat.base.dataset;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.XString;

/**
 * 调用API
 *
 * @author yyl
 * @since 2023年4月6日
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "API调用表达式前置处理器")
public class Param_InvokeApi extends Param
{
	@Schema(description = "API调用客户端类型")
	ApiClientType clientType ;
	
	@Schema(description = "内部客户端用内部id，网关客户端用网关上的apiId")
	String apiId ;
	
	@Schema(description = "API的HttpMethod")
	String httpMethod ;
	
	@Schema(description = "API路径")
	String path ;
	
	@Schema(description = "调用的API的参数")
	List<ApiArg> args ;
	
	@Schema(description = "缓存时间，单位：秒")
	int cacheTime ;
	
	@Schema(description = "路径上是否有参数")
	@JsonIgnore
	@Setter(value = AccessLevel.NONE)
	@Getter(value = AccessLevel.NONE)
	Map<String , Expression> mPathExprMap = null ;
	
	
	public void setPath(String aPath)
	{
		if(JCommon.unequals(path, aPath))
		{
			path = aPath ;
			mPathExprMap = null ;
		}
	}
	
	@JsonIgnore
	public boolean hasParamsInPath()
	{
		return getPathExprs().size() > 0 ;
	}
	
	@JsonIgnore
	public Map<String , Expression> getPathExprs()
	{
		Map<String , Expression> exprMap = mPathExprMap ;
		if(exprMap == null)
		{
			Set<String> exprStrs = XString.extractParamNames(path) ;
			if(exprStrs.isEmpty())
				exprMap = Collections.emptyMap() ;
			else
			{
				exprMap = XC.hashMap() ;
				for(String exprStr : exprStrs)
				{
					exprMap.put(exprStr , AviatorEvaluator.compile(exprStr)) ;
				}
			}
			mPathExprMap = exprMap ;
		}
		return exprMap ;
	}

	@Override
	protected Param initClone(Param aClone)
	{
		Param_InvokeApi clone = (Param_InvokeApi)super.initClone(aClone) ;
		clone.clientType = clientType ;
		clone.apiId = apiId ;
		clone.args = XC.deepCloneArrayList(args) ;
		clone.cacheTime = cacheTime ; 
		return clone ;
	}
}
