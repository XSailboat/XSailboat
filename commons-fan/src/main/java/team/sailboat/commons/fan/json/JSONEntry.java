package team.sailboat.commons.fan.json;

import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.XClassUtil;

public class JSONEntry
{
	boolean mObject ;
	
	Object mSource ;
	Object mIndex ;
	
	public JSONEntry(JSONObject aSource)
	{
		Assert.notNull(aSource , "源对象不能为空") ;
		mObject = true ;
		mSource = aSource ;
	}
	
	public JSONEntry(JSONArray aSource)
	{
		Assert.notNull(aSource , "源对象不能为空") ;
		mObject = false ;
		mSource = aSource ;
	}
	
	public JSONEntry(JSONObject aSource , String aKey)
	{
		Assert.notNull(aSource , "源对象不能为空") ;
		mObject = true ;
		mSource = aSource ;
		mIndex = aKey ;
	}
	
	public JSONEntry(JSONArray aSource , int aIndex)
	{
		Assert.notNull(aSource , "源对象不能为空") ;
		mObject = false ;
		mSource = aSource ;
		mIndex = aIndex ;
	}
	
	public void setIndex(String aKey)
	{
		Assert.isTrue(mObject , "String类型的index，只能用于JSONObject的情形") ;
		mIndex = aKey ;
	}
	
	public void setIndex(int aSeq)
	{
		Assert.isNotTrue(mObject , "int类型的index，只能用于JSONArray的情形") ;
		mIndex = aSeq ;
	}
	
	public boolean isObject()
	{
		return mSource instanceof JSONObject ;
	}
	
	public boolean isArray()
	{
		return mSource instanceof JSONArray ;
	}
	
	public JSONObject source_object()
	{
		return (JSONObject)mSource ;
	}
	
	public JSONArray source_array()
	{
		return (JSONArray)mSource ;
	}
	
	public String index_key()
	{
		return (String)mIndex ;
	}
	
	public int index_seq()
	{
		return ((Integer)mIndex).intValue() ;
	}
	
	public Object value()
	{
		return isObject()?source_object().opt(index_key()):source_array().opt(index_seq()) ;
	}
	
	public String value_String()
	{
		return XClassUtil.toString(value()) ;
	}
	
	public int value_int(int aDefaultVal)
	{
		return XClassUtil.toInteger(value() , aDefaultVal) ;
	}
	
	public void discard()
	{
		if(isObject())
			source_object().remove(index_key()) ;
		else
			source_array().remove(index_seq()) ;
	}
}
