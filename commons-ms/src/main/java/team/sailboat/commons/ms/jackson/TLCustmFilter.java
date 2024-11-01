package team.sailboat.commons.ms.jackson;

import java.io.Closeable;

import team.sailboat.commons.fan.app.AppContext;

/**
 * 
 * 用以在某些情形下排除某些字段。		<br />
 * 
 * 和@JsonInclude配合使用。例如，在字段或方法上加上
 * <pre>@JsonInclude(value = Include.CUSTOM , valueFilter = TLCustmFilter.class)</pre>
 * 
 * 然后再JSON序列化时，<pre> 
 * try(Closeable c = TLCustmFilter.enable())
 * {
 *   // 序列化JSON
 * }
 * </pre>
 *
 * @author yyl
 * @since 2024年10月24日
 */
public class TLCustmFilter
{
	static final String sACKey = "jackson.exclude_custom" ; 
	
	
	public TLCustmFilter()
	{
	}
	
	
	@Override
	public boolean equals(Object aObj)
	{
		Object obj = AppContext.getThreadLocal(sACKey) ;
		if(obj != null)
		{
			return Boolean.TRUE.equals(obj) ;
		}
		return false ;
	}
	
	
	public static Closeable enable()
	{
		AppContext.setThreadLocal(sACKey, Boolean.TRUE) ;
		return ()->AppContext.removeThreadLocal(sACKey) ;
	}
	
}
