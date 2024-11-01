package team.sailboat.commons.fan.dpa;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import team.sailboat.commons.fan.collection.IMultiMap;
import team.sailboat.commons.fan.collection.PropertiesEx;
import team.sailboat.commons.fan.dpa.anno.BColumn;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.XClassUtil;

public class ColumnMeta
{
	BColumn mAnnotation ;
	
	Field mField ;
	
	IFieldSerDe mSerDe ;
	
	public ColumnMeta()
	{
	}
	
	private ColumnMeta(BColumn aAnno , Field aField)
	{
		mField = aField ;
		mAnnotation = aAnno ;
		Class<?> serClass = aAnno.serClass() ;
		Class<?> deClass = aAnno.deserClass() ;
		Class<?> serDeClass = aAnno.serDeClass() ;
		if(aField.getType().isEnum())
		{
			//没有指定序列化和反序列化方法的话，指定缺省的
			if((Object.class.equals(serClass) || serClass == null)
					&& (Object.class.equals(serDeClass) || serDeClass == null))
			{
				serClass = SerDeFactory.Enum.class ;
			}
			
			if((Object.class.equals(deClass) || deClass == null)
					&& (Object.class.equals(serDeClass) || serDeClass == null))
			{
				deClass = SerDeFactory.Enum.class ;
			}
		}
		else if(List.class.isAssignableFrom(aField.getType()))
		{
			Assert.isTrue(XClassUtil.sCSN_String.equalsIgnoreCase(aAnno.dataType().name()) , "List类型的字段，在数据库上只能存储为string类型，不能是%s" 
					, aAnno.dataType().name()) ;
			//没有指定序列化和反序列化方法的话，指定缺省的
			if((Object.class.equals(serClass) || serClass == null)
					&& (Object.class.equals(serDeClass) || serDeClass == null))
			{
				serClass = SerDeFactory.List_String.class ;
			}
			
			if((Object.class.equals(deClass) || deClass == null)
					&& (Object.class.equals(serDeClass) || serDeClass == null))
			{
				deClass = SerDeFactory.List_String.class ;
			}
		}
		else if(Set.class.isAssignableFrom(aField.getType()))
		{
			Assert.isTrue(XClassUtil.sCSN_String.equalsIgnoreCase(aAnno.dataType().name()) , "LinkedHashSet类型的字段，在数据库上只能存储为string类型，不能是%s" 
					, aAnno.dataType().name()) ;
			//没有指定序列化和反序列化方法的话，指定缺省的
			if((Object.class.equals(serClass) || serClass == null)
					&& (Object.class.equals(serDeClass) || serDeClass == null))
			{
				serClass = SerDeFactory.Set_String.class ;
			}
			
			if((Object.class.equals(deClass) || deClass == null)
					&& (Object.class.equals(deClass) || deClass == null))
			{
				if(LinkedHashSet.class.isAssignableFrom(aField.getType()))
					deClass = SerDeFactory.LinkedHashSet_String.class ;
				else
					deClass = SerDeFactory.Set_String.class ;
			}
		}
		else if(JSONObject.class.equals(aField.getType()))
		{
			//没有指定序列化和反序列化方法的话，指定缺省的
			if((Object.class.equals(serClass) || serClass == null)
					&& (Object.class.equals(serDeClass) || serDeClass == null))
			{
				serClass = SerDeFactory.JSONObjectSerDe.class ;
			}
			
			if((Object.class.equals(deClass) || deClass == null)
					&& (Object.class.equals(serDeClass) || serDeClass == null))
			{
				deClass = SerDeFactory.JSONObjectSerDe.class ;
			}
		}
		else if(JSONArray.class.equals(aField.getType()))
		{
			//没有指定序列化和反序列化方法的话，指定缺省的
			if((Object.class.equals(serClass) || serClass == null)
					&& (Object.class.equals(serDeClass) || serDeClass == null))
			{
				serClass = SerDeFactory.JSONArraySerDe.class ;
			}
			
			if((Object.class.equals(deClass) || deClass == null)
					&& (Object.class.equals(serDeClass) || serDeClass == null))
			{
				deClass = SerDeFactory.JSONArraySerDe.class ;
			}
		}
		else if(PropertiesEx.class.equals(aField.getType()))
		{
			if((Object.class.equals(serClass) || serClass == null)
					&& (Object.class.equals(serDeClass) || serDeClass == null))
			{
				serClass = SerDeFactory.PropertiesExSerDe.class ;
			}
			
			if((Object.class.equals(deClass) || deClass == null)
					&& (Object.class.equals(serDeClass) || serDeClass == null))
			{
				deClass = SerDeFactory.PropertiesExSerDe.class ;
			}
		}
		else if(Map.class.isAssignableFrom(aField.getType()))
		{
			Assert.isTrue(XClassUtil.sCSN_String.equalsIgnoreCase(aAnno.dataType().name()) , "Map类型的字段，在数据库上只能存储为string类型，不能是%s" 
					, aAnno.dataType().name()) ;
			//没有指定序列化和反序列化方法的话，指定缺省的
			if((Object.class.equals(serClass) || serClass == null)
					&& (Object.class.equals(serDeClass) || serDeClass == null))
			{
				serClass = SerDeFactory.Map_String.class ;
			}
			
			if((Object.class.equals(deClass) || deClass == null)
					&& (Object.class.equals(serDeClass) || serDeClass == null))
			{
				deClass = SerDeFactory.Map_String.class ;
			}
		}
		else if(IMultiMap.class.isAssignableFrom(aField.getType()))
		{
			Assert.isTrue(XClassUtil.sCSN_String.equalsIgnoreCase(aAnno.dataType().name()) , "IMultiMap类型的字段，在数据库上只能存储为string类型，不能是%s" 
					, aAnno.dataType().name()) ;
			//没有指定序列化和反序列化方法的话，指定缺省的
			if((Object.class.equals(serClass) || serClass == null)
					&& (Object.class.equals(serDeClass) || serDeClass == null))
			{
				serClass = SerDeFactory.MultiMap_String.class ;
			}
			
			if((Object.class.equals(deClass) || deClass == null)
					&& (Object.class.equals(deClass) || deClass == null))
			{
				deClass = SerDeFactory.MultiMap_String.class ;
			}
		}
		
		mSerDe = new FieldSerDe(aField.getType() , serClass , deClass , serDeClass) ;
	}
	
	public Field getField()
	{
		return mField;
	}
	
	public BColumn getAnnotation()
	{
		return mAnnotation;
	}
	
	public IFieldSerDe getSerDe()
	{
		return mSerDe;
	}
	
	@Override
	public String toString()
	{
		return new StringBuilder().append(mField == null?"NULL":mField.getName())
			.append("-")
			.append(mSerDe==null?"NULL":mSerDe.getClass().getSimpleName())
			.toString() ;
	}
	
	public static ColumnMeta as(Field aField)
	{
		BColumn bcol = aField.getAnnotation(BColumn.class) ;
		if(bcol != null)
		{
			aField.setAccessible(true) ;
			return new ColumnMeta(bcol, aField) ;
		}
		return null ;
	}
}
