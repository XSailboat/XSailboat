package team.sailboat.base.schema;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Data;
import team.sailboat.commons.fan.lang.XClassUtil;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class , property = "name"
		, resolver = BaseTypeIdResolver.class , scope = BaseType.class)
@JsonIdentityReference(alwaysAsId = true)
@Data
public class BaseType implements Type
{
	public static BaseType STRING = new BaseType(XClassUtil.sCSN_String) ;
	public static BaseType LONG = new BaseType(XClassUtil.sCSN_Long) ;
	public static BaseType DOUBLE = new BaseType(XClassUtil.sCSN_Double) ;
	public static BaseType INTEGER = new BaseType(XClassUtil.sCSN_Integer) ;
	public static BaseType BOOLEAN = new BaseType(XClassUtil.sCSN_Bool) ;
	public static BaseType DATETIME = new BaseType(XClassUtil.sCSN_DateTime) ;
	
	public static BaseType of(String aTypeName)
	{
		switch(aTypeName)
		{
		case XClassUtil.sCSN_String:
			return STRING ;
		case XClassUtil.sCSN_Long:
			return LONG ;
		case XClassUtil.sCSN_Double:
			return DOUBLE ;
		case XClassUtil.sCSN_Integer:
			return INTEGER ;
		case XClassUtil.sCSN_Bool:
			return BOOLEAN ;
		case XClassUtil.sCSN_DateTime:
			return DATETIME ;
		default:
			throw new IllegalArgumentException("未知的基本类型："+aTypeName) ;
		}
	}
	
	final String name ;
	
	private BaseType(String aName)
	{
		name = aName ;
	}
	
	@Override
	public String toString()
	{
		return name ;
	}
}
