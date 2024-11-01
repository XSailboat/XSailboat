package team.sailboat.base.msg;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.excep.ExceptionAssist;
import team.sailboat.commons.fan.json.JSONException;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.text.XString;

@Schema(description = "消息过滤器")
@Data
@JsonInclude(value = Include.NON_NULL)
public class MsgFilter
{
	static final Logger sLogger = LoggerFactory.getLogger(MsgFilter.class) ;
	
	@Schema(description = "分区")
	String[] partitions ;
	
	@Schema(description = "消息类型，多个消息类型之间是或的关系")
	MsgType[] types ;
	
	@Schema(description = "消息等级（1～10），多个之间是或的关系")
	int[] levels ;
	
	@Schema(description = "data字段Aviator表达式")
	String dataScript ;
	
	/**
	 * 支持搜索
	 */
	@Schema(description = "消息模式，正则表达式")
	TextPattern contentPattern ;
	
	/**
	 * 支持搜索
	 */
	@Schema(description = "消息位置模式，正则表达式")
	TextPattern locationPattern ;
	
	/**
	 *	支持搜索 
	 */
	@Schema(description = "目标模式，正则表达式")
	TextPattern destinationPattern ;
	
	@Schema(description = "消息来源")
	String[] sources ;
	
	@Schema(description = "消息事件时间过滤表达式，Aviator表达式")
	String eventTimeExpr ;
	
	@Schema(description = "客户端id")
	String[] clientIds ;
	
	@Schema(hidden = true)
	@JsonIgnore
	Predicate<Msg> filter ;
	
	/**
	 * 是否是简单查询模式。不涉及内容，目标和位置
	 * @return
	 */
	@JsonIgnore
	public boolean isSimpleQuery()
	{
		return contentPattern == null
				&& locationPattern == null
				&& destinationPattern == null ;
	}
	
	public static boolean accept(List<MsgFilter> aFilters , Msg aMsg)
	{
		if(XC.isEmpty(aFilters))
			return true ;
		for(MsgFilter filter : aFilters)
		{
			if(filter.getFilter().test(aMsg))
				return true ;
		}
		return false ;
	}
	
	public Predicate<Msg> getFilter()
	{
		if(filter == null)
		{
			filter = new MsgPredicate() ;
		}
		return filter ;
	}
	
	class MsgPredicate implements Predicate<Msg>
	{
		Predicate<String> mLocationPred ;
		Predicate<String> mContentPred ;
		Expression mDataFilterExpr ;
		
		public MsgPredicate()
		{
			if(locationPattern != null)
			{
				mLocationPred = TextPattern.toPredicate(locationPattern) ;
			}
			if(contentPattern != null)
			{
				mContentPred = TextPattern.toPredicate(contentPattern) ;
			}
			if(XString.isNotEmpty(dataScript))
				mDataFilterExpr = AviatorEvaluator.compile(dataScript) ;
		}
		
		@Override
		public boolean test(Msg msg)
		{
			if(XC.isNotEmpty(partitions))
			{
				boolean accept = false ;
				for(String partition : partitions)
				{
					if(partition.equals(msg.getPartition()))
					{
						accept = true ;
						break ;
					}
				}
				if(!accept)
					return false ;
			}
			
			if(XC.isNotEmpty(types))
			{
				boolean accept = false ;
				for(MsgType type : types)
				{
					if(type == msg.getType())
					{
						accept = true ;
						break ;
					}
				}
				if(!accept)
					return false ;
			}
			
			if(XC.isNotEmpty(levels))
			{
				boolean accept = false ;
				for(int level : levels)
				{
					if(level == msg.getLevel())
					{
						accept = true ;
						break ;
					}
				}
				if(!accept)
					return false ;
			}
			
			if(XC.isNotEmpty(sources))
			{
				boolean accept = false ;
				for(String source : sources)
				{
					if(source.equals(msg.getSource()))
					{
						accept = true ;
						break ;
					}
				}
				if(!accept)
					return false ;
			}
			
			if(XC.isNotEmpty(clientIds))
			{
				boolean accept = false ;
				for(String clientId : clientIds)
				{
					if(clientId.equals(msg.getClientId()))
					{
						accept = true ;
						break ;
					}
				}
				if(!accept)
					return false ;
			}
			
			if(mLocationPred != null)
			{
				if(!mLocationPred.test(msg.getLocation()))
					return false ;
			}
			if(mContentPred != null)
			{
				if(!mContentPred.test(msg.getContent()))
					return false ;
			}		
			if(mDataFilterExpr != null)
			{
				Map<String, Object> ctxMap = XC.hashMap() ;
				if(XString.isEmpty(msg.data))
					ctxMap.put("data" ,  null) ;
				else
				{
					try
					{
						ctxMap.put("data" , new JSONObject(msg.data)) ;
					}
					catch(JSONException e)
					{
						sLogger.error(ExceptionAssist.getStackTrace(e)) ;
					}
				}
				try
				{
					return XClassUtil.toBoolean(mDataFilterExpr.execute(ctxMap) , false) ;
				}
				catch(Exception e)
				{
					// 出异常，认为它不符合条件，把它排除
					Object loc = AppContext.getThreadLocal("MsgFilterLoc") ;
					sLogger.error(ExceptionAssist.getClearMessage(MsgFilter.class , e , (loc ==null?"":loc+"的")+ "data字段过滤的表达式执行异常，表达式："+dataScript)) ;
					return false ;
				}
			}
			return true ;
		}
	}
}
