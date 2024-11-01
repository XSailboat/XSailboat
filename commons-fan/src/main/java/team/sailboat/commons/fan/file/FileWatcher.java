package team.sailboat.commons.fan.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import team.sailboat.commons.fan.event.IXListener;
import team.sailboat.commons.fan.event.XEvent;

public class FileWatcher
{
	static FileWatcher sInstance ;
	
	public static FileWatcher getInstance()
	{
		if(sInstance == null)
			sInstance = new FileWatcher() ;
		return sInstance ;
	}
	
	
	Map<WatchKey , IXListener> mMap = new HashMap<>() ;
	
	WatchService mWatchService ;
	Run mRun ;
	
	protected FileWatcher()
	{
		try
		{
			mWatchService = FileSystems.getDefault().newWatchService() ;
			startWatch() ; 
		}
		catch (IOException e)
		{
			throw new IllegalStateException(e) ;
		}
	}
	
	public WatchKey watch(File aDirectory ,  IXListener aLsn , Kind<?>... aKinds) throws IOException
	{
		Path path = Paths.get(aDirectory.toURI()) ;
		WatchKey watchKey = path.register(mWatchService, aKinds) ;
		mMap.put(watchKey, aLsn) ;
		return watchKey ;
	}
	
	protected void startWatch()
	{
		if(mRun == null)
		{
			mRun = new Run() ;
			new Thread(mRun , "文件监视").start(); ;
		}
	}
	
	class Run implements Runnable
	{

		boolean mInterrupted = false ;
		
		@Override
		public void run()
		{
			while(!mInterrupted)
			{
				try
				{
					WatchKey watchKey = mWatchService.poll(1, TimeUnit.SECONDS) ;
					if(watchKey != null)
					{
						IXListener lsn = mMap.get(watchKey) ;
						if(lsn != null)
						{
							try
							{
								lsn.handle(new XEvent(watchKey, 0));
							}
							catch(Exception e)
							{
								e.printStackTrace(); 
							}
						}
						watchKey.reset() ;
					}
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				Iterator<Entry<WatchKey , IXListener>> it = mMap.entrySet().iterator() ;
				while(it.hasNext())
				{
					Entry<WatchKey , IXListener> entry = it.next() ;
					if(!entry.getKey().isValid())
						it.remove(); 
				}
			}
		}
		
	}
}
