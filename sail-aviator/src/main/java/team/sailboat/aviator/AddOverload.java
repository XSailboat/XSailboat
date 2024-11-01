package team.sailboat.aviator;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

import com.googlecode.aviator.lexer.token.OperatorType;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;
import com.googlecode.aviator.runtime.type.AviatorString;
import com.googlecode.aviator.runtime.type.AviatorType;

import team.sailboat.aviator.bean.DateDuration;
import team.sailboat.commons.fan.adapter.TA_Date;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.XClassUtil;

public class AddOverload extends WedgeFunction
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public String getName()
	{
		return OperatorType.ADD.getToken();
	}

	@Override
	public AviatorObject call(Map<String, Object> aEnv, AviatorObject aArg1, AviatorObject aArg2)
	{
		if (aArg2.getAviatorType() == AviatorType.JavaType)
		{
			Object val2 = aArg2.getValue(aEnv) ;
			if(val2 instanceof DateDuration)
			{
				DateDuration d = (DateDuration)val2 ;
				Object val1 = aArg1.getValue(aEnv) ;
				if(val1 == null)
					return aArg2 ;
				return call(val1 , d) ;
			}
		}
		if(aArg1.getAviatorType() == AviatorType.JavaType)
		{
			Object val2 = aArg1.getValue(aEnv) ;
			if(val2 instanceof DateDuration)
			{
				DateDuration d = (DateDuration)val2 ;
				Object val1 = aArg2.getValue(aEnv) ;
				if(val1 == null)
					return aArg1 ;
				return call(val1 , d) ;
			}
		}
		return aArg1.add(aArg2, aEnv) ;
	}
		
	static AviatorObject call(Object val1 , DateDuration d)
	{	
		Date date = (Date)XClassUtil.typeAdapt(val1 , XClassUtil.sCSN_DateTime) ; 
		Assert.notNull(date , "%s 无法转化为Date类型！" , val1) ;
		
		ZonedDateTime dt = date.toInstant().atZone(ZoneId.systemDefault()) ;
		switch(d.getUnitMark())
		{
		case 'y':
			dt = dt.plusYears(d.getValue()) ;
			break ;
		case 'M':
			dt = dt.plusMonths(d.getValue()) ;
			break ;
		case 'd':
			dt = dt.plusDays(d.getValue()) ;
			break ;
		case 'w':
			dt = dt.plusWeeks(d.getValue()) ;
			break ;
		case 'H':
			dt = dt.plusHours(d.getValue()) ;
			break ;
		case 'm':
			dt = dt.plusMinutes(d.getValue()) ;
			break ;
		case 's':
			dt = dt.plusSeconds(d.getValue()) ;
			break ;
		default:
			throw new IllegalArgumentException("未支持的时间单位："+d.getUnitMark()) ;
		}
		
		// 考察时间是什么类型，转过去
		if(val1 instanceof Date)
			return AviatorRuntimeJavaType.valueOf(Date.from(dt.toInstant())) ;
		else if(val1 instanceof Long)
			return AviatorLong.valueOf(dt.toInstant().toEpochMilli()) ;
		else if(val1 instanceof String)
		{
			return new AviatorString(TA_Date.format(Date.from(dt.toInstant()) , (String)val1)) ;
		}
		else
			throw new IllegalStateException("不支持的类型："+val1.getClass().getName()) ;
	}
}
