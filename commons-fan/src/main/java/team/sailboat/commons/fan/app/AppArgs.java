package team.sailboat.commons.fan.app;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.struct.Tuples;
import team.sailboat.commons.fan.text.XString;

public class AppArgs
{
	List<Tuples.T2<String, String>> mArgEntryList = new ArrayList<>() ;
	
	public AppArgs()
	{}
	
	public AppArgs(String[] aArgs)
	{
		if(aArgs != null && aArgs.length>0)
		{
			for(int i=0 ; i<aArgs.length ;)
			{
				if(aArgs[i].startsWith("-"))
				{
					mArgEntryList.add(Tuples.of(aArgs[i++].substring(1)
							, i<aArgs.length&&!aArgs[i].startsWith("-")?aArgs[i++]:null)) ;
				}
				else
					mArgEntryList.add(Tuples.of(null, aArgs[i++])) ;
			}
		}
	}
	
	public String getIgnoreCase(String aKey)
	{
		if(mArgEntryList.isEmpty())
			return null ;
		for(Entry<String, String> entry : mArgEntryList)
		{
			if(XString.equalsStrIgnoreCase(aKey, entry.getKey()))
				return entry.getValue() ;
		}
		return null ;
	}
	
	public String get(String aKey)
	{
		return Tuples.getEle_2(mArgEntryList, aKey) ;
	}
	
	public String[] getStringArray(String aKey)
	{
		Collection<String> vals = Tuples.getEle_2s(mArgEntryList, aKey) ;
		List<String> result = new ArrayList<>() ;
		for(String val : vals)
		{
			if(XString.isEmpty(val))
				continue ;
			XC.addAll(result , val.split(",")) ;
		}
		return result.toArray(JCommon.sEmptyStringArray) ;
	}
	
	public boolean has(String aKey)
	{
		return Tuples.hasEle_1(mArgEntryList, aKey) ;
	}
}
