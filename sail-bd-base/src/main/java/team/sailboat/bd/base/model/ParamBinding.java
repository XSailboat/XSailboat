package team.sailboat.bd.base.model;

import java.util.ArrayList;
import java.util.Collection;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dpa.anno.BForwardMethod;
import team.sailboat.commons.fan.dpa.anno.BReverseMethod;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;
import team.sailboat.commons.fan.lang.JCommon;

public class ParamBinding implements ToJSONObject
{
	String mRef ;
	
	/**
	 * 
	 */
	ParamBindingSource mSource ;
	
	public ParamBinding()
	{
	}
	
	public ParamBinding(String aRef , ParamBindingSource aSource)
	{
		mRef = aRef ;
		mSource = aSource ;
	}

	public String getRef()
	{
		return mRef;
	}

	public void setRef(String aRef)
	{
		mRef = aRef;
	}

	public ParamBindingSource getSource()
	{
		return mSource;
	}

	public void setSource(ParamBindingSource aSource)
	{
		mSource = aSource;
	}
	
	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		return aJSONObj.put("ref", mRef)
				.put("source" , mSource.name()) ;
	}
	
	@Override
	public boolean equals(Object aObj)
	{
		if(aObj == this)
			return true ;
		if(aObj == null || !(aObj instanceof ParamBinding))
			return false ;
		ParamBinding other = (ParamBinding)aObj ;
		return JCommon.equals(mRef, other.mRef)
				&& mSource == other.mSource ;
	}
	
	public static ParamBinding parse(JSONObject aJo)
	{
		return new ParamBinding(aJo.optString("ref") , ParamBindingSource.valueOf(aJo.optString("source"))) ;
	}
	
	public static class Deser_ArrayList
	{
		@BForwardMethod
		public static String forward(Collection<?> aSource)
		{
			return aSource==null?null:new JSONArray(aSource).toString() ;
		}
		
		@BReverseMethod
		public static ArrayList<ParamBinding> reverse(Object aSource)
		{
			if(aSource == null)
				return null ;
			JSONArray ja = null ;
			if(aSource instanceof JSONArray)
				ja = (JSONArray)aSource ;
			else
			{
				String str = aSource.toString() ;
				if(str.isEmpty())
					return null ;
				ja = new JSONArray(str) ;
			}
			ArrayList<ParamBinding> mBindings = XC.arrayList() ;
			ja.forEachJSONObject(jobj->{
				mBindings.add(parse(jobj)) ;
			});
			return mBindings ;
		}
	}
	
}
