package team.sailboat.bd.base.hdfs;

import java.io.IOException;
import java.util.function.Supplier;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;

import team.sailboat.commons.fan.excep.WrapException;
/**
 * 
 *
 * @author yyl
 * @since 2024年9月29日
 */
public class HDFSProvider implements Supplier<FileSystem>
{
	final Object mMutex = new Object() ;
	
	Configuration mConf ;
	
	FileSystem mFs ;
	
	public HDFSProvider(Configuration aConf)
	{
		mConf = aConf ;
	}

	@Override
	public FileSystem get()
	{
		if(mFs == null)
		{
			synchronized (mMutex)
			{
				if(mFs == null)
				{
					try
					{
						mFs = FileSystem.get(mConf) ;
					}
					catch (IOException e)
					{
						WrapException.wrapThrow(e) ; 
						// dead code
						return null ;
					}
				}
			}
		}
		return mFs ;
	}

}
