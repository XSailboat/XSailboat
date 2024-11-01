package team.sailboat.commons.fan.es.index;

import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;

public class MappingsDefine
{
	IndexDefine mUp ;
	JSONObject mMappingsDefine ;
	JSONObject mPropertiesDefine ;
	
	MappingsDefine(IndexDefine aUp , JSONObject aMappingsDefine)
	{
		mUp = aUp ;
		mMappingsDefine = aMappingsDefine ;
		mPropertiesDefine = mMappingsDefine.optJSONObject("properties") ;
		if(mPropertiesDefine == null)
		{
			mPropertiesDefine = new JSONObject() ;
			mMappingsDefine.put("properties", mPropertiesDefine) ;
		}
	}
	
	MappingsDefine()
	{
		this(null , new JSONObject()) ;
	}
	
	public JSONObject getMappingsDefineJo()
	{
		return mMappingsDefine ;
	}
	
	public MappingsDefine property(String aPropName , String aType)
	{
		mPropertiesDefine.put(aPropName, new JSONObject().put("type", aType)) ;
		return this ;
	}
	
	public MappingsDefine property_keyword(String aPropName)
	{
		return property(aPropName , "keyword") ;
	}
	
	public MappingsDefine property_int(String aPropName)
	{
		return property(aPropName , "integer") ;
	}
	
	public MappingsDefine property_text(String aPropName)
	{
		return property(aPropName , "text") ;
	}
	
	public MappingsDefine property_match_only_text(String aPropName)
	{
		return property(aPropName , "match_only_text") ;
	}
	
	public MappingsDefine property_double(String aPropName)
	{
		return property(aPropName , "double") ;
	}
	
	public MappingsDefine property_float(String aPropName)
	{
		return property(aPropName , "float") ;
	}
	
	public MappingsDefine property_long(String aPropName)
	{
		return property(aPropName , "long") ;
	}
	
	public PropertyDefine_date propertySpecified_date(String aPropName)
	{
		return (PropertyDefine_date) propertySpecified(aPropName, "date") ;
	}
	
	public PropertyDefine_DenseVector propertySpecified_vector(String aPropName)
	{
		return (PropertyDefine_DenseVector) propertySpecified(aPropName, "dense_vector") ;
	}
	
	public MappingsDefine property_Object(String aPropName , MappingsDefine aMappingDef)
	{
		mPropertiesDefine.put(aPropName, aMappingDef.mMappingsDefine) ;
		return this ;
	}
	
	public PropertyDefine propertySpecified(String aPropName , String aType)
	{
		JSONObject propertyDefine = mPropertiesDefine.optJSONObject(aPropName) ;
		if(propertyDefine == null)
		{
			propertyDefine = new JSONObject() ;
			mPropertiesDefine.put(aPropName, propertyDefine) ;
		}
		propertyDefine.put("type", aType) ;
		switch(aType)
		{
		case "date" :
			return new PropertyDefine_date(this , propertyDefine) ;
		case "dense_vector":
			return new PropertyDefine_DenseVector(this, propertyDefine) ;
		default:
			return new PropertyDefine(this , propertyDefine) ;
		}
	}
	
	public MappingsDefine _source_excludes(String...aFields)
	{
		mMappingsDefine.put("_source", new JSONObject().put("excludes", new JSONArray(aFields))) ;
		return this ;
	}
	
	public IndexDefine up()
	{
		return mUp ;
	}
	
	public static MappingsDefine one()
	{
		return new MappingsDefine() ;
	}
}
