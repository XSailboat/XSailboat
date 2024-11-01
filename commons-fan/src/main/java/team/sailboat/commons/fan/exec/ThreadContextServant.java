package team.sailboat.commons.fan.exec;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import team.sailboat.commons.fan.app.AppContext;

public class ThreadContextServant
{
	static final String sThreadContextServant = "ThreadContextServant" ;
	
	Map<String , Object> mMap = new LinkedHashMap<>() ;
	
	protected ThreadContextServant()
	{
	}
	
	public Object put(String aKey , Object aVal)
	{
		return mMap.put(aKey, aVal) ;
	}
	
	public Object remove(String aKey)
	{
		return mMap.remove(aKey) ;
	}
	
	/**
	 * 初始化
	 */
	public final void init()
	{
		AppContext.setThreadLocal(sThreadContextServant , this) ;
		_init();
	}
	
	/**
	 * 清除
	 */
	public final void destroy()
	{
		_clear();
		AppContext.setThreadLocal(sThreadContextServant, null) ;
	}
	
	protected void _init()
	{
		for(Entry<String, Object> entry : mMap.entrySet())
			AppContext.setThreadLocal(entry.getKey() , entry.getValue()) ;
	}

	protected void _clear()
	{
		for(String key : mMap.keySet())
			AppContext.setThreadLocal(key , null) ;
	}
	
	public static Runnable wrap(Runnable aWorker)
	{
		ThreadContextServant servant = (ThreadContextServant) AppContext.getThreadLocal(sThreadContextServant) ;
		return servant == null?aWorker : new WrapperRun(servant , aWorker) ;
	}
	
	public static Runnable wrap(Runnable aWorker , ThreadContextServant aServant)
	{
		return aServant == null?aWorker : new WrapperRun(aServant , aWorker) ;
	}
	
	public static ThreadContextServant get(boolean aCreateIfNotExists)
	{
		ThreadContextServant servant = (ThreadContextServant) AppContext.getThreadLocal(sThreadContextServant) ;
		if(servant ==null && aCreateIfNotExists)
		{
			servant = new ThreadContextServant() ;
			AppContext.setThreadLocal(sThreadContextServant, servant);
		}
		return servant ;
	}
	
	public static void set(String aKey , Object aVal)
	{
		get(true).put(aKey, aVal) ;
		AppContext.setThreadLocal(aKey, aVal); 
	}
	
	public static void clear()
	{
		ThreadContextServant servant = get(false) ;
		if(servant != null)
			servant.destroy();
	}
}
