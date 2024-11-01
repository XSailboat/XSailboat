package team.sailboat.base.zk;

import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import team.sailboat.base.IZKSysProxy;
import team.sailboat.commons.fan.excep.ExceptionAssist;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.XString;

public class ZKConf
{
	final Logger mLogger = LoggerFactory.getLogger(getClass()) ;
	
	IZKSysProxy mZKSysProxy ;
	
	public ZKConf(IZKSysProxy aZKSysProxy) throws Exception
	{
		mZKSysProxy = aZKSysProxy ;
	}
	
	public String watch(String aZKPath , Consumer<String> aConsumer , String aDefaultValue
			, boolean aCreateIfNotExists) throws Exception
	{
		if(aCreateIfNotExists)
			mZKSysProxy.ensureExists(aZKPath , aDefaultValue);
		aConsumer.accept(JCommon.defaultIfNull(mZKSysProxy.getNodeData_Str(aZKPath) , aDefaultValue)) ;
		return mZKSysProxy.watchNode(aZKPath , new StringWatcher(aConsumer, aDefaultValue)) ;
	}
	
	public String watchInt(String aZKPath , Consumer<Integer> aConsumer , int aDefaultValue
			, boolean aCreateIfNotExists) throws Exception
	{
		if(aCreateIfNotExists)
			mZKSysProxy.ensureExists(aZKPath , Integer.toString(aDefaultValue));
		aConsumer.accept(JCommon.defaultIfNull(mZKSysProxy.getNodeData_int(aZKPath) , aDefaultValue)) ;
		return mZKSysProxy.watchNode(aZKPath , new IntWatcher(aConsumer, aDefaultValue)) ;
	}
	
	class TypedWatcher<T> implements Watcher
	{
		Consumer<T> mConsumer ;
		Function<String, T> mTypeAdapter ;
		T mDefaultValue ;
		
		public TypedWatcher(Consumer<T> aConsumer , T aDefaultValue , Function<String, T> aTypeAdpater)
		{
			mConsumer = aConsumer ;
			mTypeAdapter = aTypeAdpater ;
			mDefaultValue = aDefaultValue ;
		}

		@Override
		public void process(WatchedEvent aEvent)
		{
			try
			{
				String msgPtn = null ;
				switch(aEvent.getType())
				{
				case NodeCreated:
					msgPtn = "ZK中的配置项{}被创建，值是:{}" ;
				case NodeDataChanged:
					if(msgPtn == null)
						msgPtn = "ZK中的配置项{}被修改，新值是:{}" ;
					String val = mZKSysProxy.getNodeData_Str(aEvent.getPath()) ;
					mLogger.info(XString.msgFmt(msgPtn, aEvent.getPath() , val)) ;
					mConsumer.accept(XString.isEmpty(val)?mDefaultValue:mTypeAdapter.apply(val)) ;
					break ;
				default:
					
				}
			}
			catch (Exception e1)
			{
				mLogger.error(ExceptionAssist.getStackTrace(e1)) ;
			}
		}
		
	}
	
	class IntWatcher extends TypedWatcher<Integer>
	{

		public IntWatcher(Consumer<Integer> aConsumer, Integer aDefaultValue)
		{
			super(aConsumer, aDefaultValue, Integer::parseInt);
		}
	}
	
	class StringWatcher extends TypedWatcher<String>
	{

		public StringWatcher(Consumer<String> aConsumer, String aDefaultValue)
		{
			super(aConsumer, aDefaultValue, String::toString);
		}
	}
}
