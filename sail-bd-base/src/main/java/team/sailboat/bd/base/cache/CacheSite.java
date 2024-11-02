package team.sailboat.bd.base.cache;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import team.sailboat.base.ZKSysProxy;
import team.sailboat.commons.fan.excep.ExceptionAssist;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.infc.ESupplier;

public abstract class CacheSite<T>
{
	final Logger mLogger = LoggerFactory.getLogger(getClass()) ;
	
	protected Map<String, T> mCache ;
	
	protected String mZKNodePath ;
	
	protected ESupplier<T[] , Exception> mSupplier ;
	
	protected int mVersion = 0 ;
 
	public CacheSite(String aZKNodePath , ESupplier<T[] , Exception> aSupplier) throws Exception
	{
		mZKNodePath = aZKNodePath ;
		mSupplier = aSupplier ;
		
		ZKSysProxy.getSysDefault().watchNode(mZKNodePath, (event)->{
			
			switch(event.getType())
			{
			case NodeCreated:
			case NodeDataChanged:
				try
				{
					int version = ZKSysProxy.getSysDefault().getNodeData_int(mZKNodePath) ;
					if(mVersion < version)
					{
						//重新加载
						update(mSupplier.get()) ;
						mVersion = version ;
					}
				}
				catch (Exception e)
				{
					mLogger.error("试图读取关于ZK节点[{}]的数据出现异常，异常消息："+ExceptionAssist.getClearMessage(getClass(), e) , mZKNodePath) ;
				}
				break ;
			default:
				break ;
			}
		});
	}
	
	protected abstract void update(T[] aData) ;
	
	public T get(String aId)
	{
		if(mCache == null)
		{
			try
			{
				update(mSupplier.get()) ;
			}
			catch (Exception e)
			{
				WrapException.wrapThrow(e) ; 
				return null ;			// dead code
			}
		}
		return mCache.get(aId) ;
	}
}
