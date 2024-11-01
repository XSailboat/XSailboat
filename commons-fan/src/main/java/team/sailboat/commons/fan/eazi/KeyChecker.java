package team.sailboat.commons.fan.eazi;

import java.util.ArrayList;
import java.util.List;

class KeyChecker
{
	List<List<String>> mKeys ;
	
	public KeyChecker()
	{
		mKeys = new ArrayList<List<String>>() ;
		mKeys.add(new ArrayList<String>()) ;
	}
	
	public void check(String aKey)
	{
		if(mKeys.get(mKeys.size()-1).contains(aKey))
			throw new IllegalStateException("键"+aKey+"重复") ;
		mKeys.get(mKeys.size()-1).add(aKey) ;
	}
	
	public void in()
	{
		mKeys.add(new ArrayList<String>()) ;
	}
	
	public void out()
	{
		mKeys.remove(mKeys.size()-1) ;
	}
}
