package team.sailboat.commons.fan.statestore;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.collection.PropertiesEx;
import team.sailboat.commons.fan.lang.JCommon;

/**
 * 
 * 基于Properties文件的程序运行数据存取器
 *
 * @author yyl
 * @since 2024年11月20日
 */
public class RunData_Properties implements IRunData
{

	File mPropertiesFile ;
	
	PropertiesEx mPropEx ;
	
	public RunData_Properties(File aPropertiesFile) throws IOException
	{
		mPropertiesFile = aPropertiesFile ;
		_init() ;
	}
	
	void _init() throws IOException
	{
		mPropEx = PropertiesEx.loadFromFile(mPropertiesFile , AppContext.sUTF8) ;
	}
	
	@Override
	public String get(String aKey)
	{
		return mPropEx.getProperty(aKey) ;
	}
	
	@Override
	public long getLong(String aKey , long aDefault)
	{
		return mPropEx.getLong(aKey, aDefault) ;
	}
	
	@Override
	public void put(String aKey , long aValue) throws IOException
	{
		put(aKey , Long.toString(aValue)) ;
	}
	
	@Override
	public void put(String aKey , String aValue) throws IOException
	{
		if(aValue == null)
			aValue = "" ;
		Object oldValue = mPropEx.put(aKey, aValue) ;
		if(JCommon.unequals(aValue, oldValue))
		{
			mPropEx.store(mPropertiesFile, AppContext.sUTF8) ;
		}
	}
	
	@Override
	public void putAll(Map<String, String> aDataMap) throws IOException
	{
		boolean changed = false ;
		for(Entry<String, String> entry : aDataMap.entrySet())
		{
			Object oldValue = mPropEx.put(entry.getKey() , entry.getValue()) ;
			changed |= JCommon.unequals(entry.getValue() , oldValue) ;
		}
		if(changed)
		{
			mPropEx.store(mPropertiesFile, AppContext.sUTF8) ;
		}
	}
}
