package team.sailboat.base ;

import java.io.StringReader;
import java.net.URI;
import java.util.Map;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;

import com.google.protobuf.InvalidProtocolBufferException;

import team.sailboat.base.HAZKInfoProtos.ActiveNodeInfo;
import team.sailboat.commons.fan.collection.PropertiesEx;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.excep.ExceptionAssist;
import team.sailboat.commons.fan.file.FileUtils;
import team.sailboat.commons.fan.infc.EFunction;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.text.XString;

/**
 * 
 * 基于Zookeeper中注册信息的HttpClient提供器
 *
 * @author yyl
 * @since 2024年12月6日
 */
public class ZKHttpClientProvider extends HttpClientProvider
{	
	static final String sCK_SysDefaultHdfs = "sysDefaultHdfs" ;
	
	String mZKPath_addr ;
	IZKProxy mZKSysProxy ;
	String mAppDirName ;
	EFunction<byte[], URI , Exception> mDataParser ;
	String mContextPath ;
	
	Watcher mWatcher ;
	
	protected ZKHttpClientProvider(IZKProxy aZKProxy , String aPath , EFunction<byte[], URI , Exception> aDataParser) throws Exception
	{
		this(aZKProxy, aPath, aDataParser, null) ;
	}
	
	/**
	 * 改用HttpClientProvider(String aAppDirName)
	 * @param aZKSysProxy
	 * @param aAppDirName
	 * @throws Exception 
	 */
	protected ZKHttpClientProvider(IZKProxy aZKProxy , String aPath , EFunction<byte[], URI , Exception> aDataParser
			, String aContextPath) throws Exception
	{
		super(aPath) ;
 		mZKSysProxy = aZKProxy ;
		mZKPath_addr = aPath ;
		mDataParser = aDataParser ;
		mContextPath = aContextPath ;
		mWatcher = (event)->{
			if(mZKPath_addr.equals(event.getPath()))
			{
				switch(event.getType())
				{
				case NodeDataChanged:
				case NodeCreated:
					sLogger.info("ZK节点[{}]发生事件：{}" , event.getType()) ;
					try
					{
						setServiceAddrs(getServiceUrlsFromZK());
					}
					catch (Throwable e)
					{
						sLogger.error("监测ZK中Sail的服务地址变更过程中出现如下异常。异常消息："+ExceptionAssist.getStackTrace(e)) ;	
					}
					break ;
				case NodeDeleted:
					sLogger.error("ZK节点[{}]被删除" , mZKPath_addr) ;
					break ;
				default:
					break ;
				}
			}
		} ;
		mZKSysProxy.watchNode(mZKPath_addr , mWatcher , true) ;
		mZKSysProxy.addReconnectedListener((e)->{
			try
			{
				setServiceAddrs(getServiceUrlsFromZK());
			}
			catch (Exception e1)
			{
				sLogger.error(ExceptionAssist.getClearMessage(getClass(), e1)) ;
			}
		});
		sLogger.info("监听Zookeeper节点[{}]" , mZKPath_addr) ;
		setServiceAddrs(getServiceUrlsFromZK());
	}
	
	URI[] parseAddrsStr(String aProtocol , String aAddrsStr)
	{
		String[] addrs = aAddrsStr.split(",") ;
		URI[] uris = new URI[addrs.length] ;
		for(int i=0 ; i<addrs.length ; i++)
		{
			uris[i] = URI.create(aProtocol + "://"+ addrs[i]) ;
		}
		return uris ;
	}
	
	URI[] getServiceUrlsFromZK() throws Exception
	{
		URI[] uris = mDataParser==null?parseAddrsStr(XString.lastSeg_i(mZKPath_addr , '/' , 0) , mZKSysProxy.getNodeData_Str(mZKPath_addr))
				: new URI[] { mDataParser.apply(mZKSysProxy.getNodeData(mZKPath_addr))} ;
		if(XString.isNotEmpty(mContextPath))
		{
			for(int i=0 ; i<uris.length ; i++)
			{
				if(XString.isEmpty(uris[i].getPath()))
				{
					uris[i] = URI.create(uris[i].toString()+mContextPath) ;
				}
			}
		}
		return uris ;
	}
	
	static ZKHttpClientProvider ofSysApp_0(String aAppDirName , String aContextPath) throws Exception
	{
		String path = XString.msgFmt(SysConst.sZK_SysPathPtn_http, aAppDirName) ;
		return ofSysApp(path, aAppDirName, aContextPath) ;
	}
	
	static ZKHttpClientProvider ofSysApp_SSL(String aAppDirName , String aContextPath) throws Exception
	{
		String path = XString.msgFmt(SysConst.sZK_SysPathPtn_https , aAppDirName) ;
		return ofSysApp(path, aAppDirName, aContextPath) ;
	}
	
	private static ZKHttpClientProvider ofSysApp(String aZKPath , String aAppDirName , String aContextPath) throws Exception
	{
		String key = XString.isEmpty(aContextPath)?aZKPath:(aZKPath+":"+aContextPath) ;
		ZKHttpClientProvider clientPvd = (ZKHttpClientProvider) sHttpClientPvdMap.get(key) ;
		if(clientPvd == null)
		{
			clientPvd = new ZKHttpClientProvider(ZKSysProxy.getSysDefault() , aZKPath , null , aContextPath) ;
			sHttpClientPvdMap.put(key, clientPvd) ;
		}
		return clientPvd ; 
	}
	
	static ZKHttpClientProvider ofSysDefaultHdfs_0() throws Exception
	{
		ZKHttpClientProvider clientPvd = (ZKHttpClientProvider) sHttpClientPvdMap.get("sysDefaultHdfs") ;
		if(clientPvd == null)
		{
			IZKSysProxy sysProxy = ZKSysProxy.getSysDefault() ;
			String props = sysProxy.getNodeData_Str(SysConst.sZK_CommonPath_hadoop) ;
			PropertiesEx propEx = new PropertiesEx() ;
			propEx.load(new StringReader(props));
			String cluster = propEx.getProperty("cluster") ;
			
			IZKProxy proxy = ZKProxy.get(ZKSysProxy.getDefaultQuorum()) ;
			if(XString.isEmpty(cluster))
			{
				String clusterPath = proxy.getAnyOneChildPath("/hadoop-ha") ;
				Assert.notEmpty(clusterPath , "ZK中的路径“/hadoop-ha”下没有注册集群") ;
				cluster = FileUtils.getFileName(clusterPath) ;
				sysProxy.setNodeData(SysConst.sZK_CommonPath_hadoop , cluster) ;
				sLogger.warn("在ZK的系统目录下没有设置系统使用的hadoop集群名称({})，将其设置为{}" , SysConst.sZK_CommonPath_hadoop , cluster) ;
			}
			String path = XString.msgFmt("/hadoop-ha/{}/ActiveStandbyElectorLock", cluster) ;
			clientPvd = new ZKHttpClientProvider(proxy, path, new HAZKInfoParser(sCK_SysDefaultHdfs
					, sysProxy , SysConst.sZK_CommonPath_hdfs_http)) ;
			sHttpClientPvdMap.put("sysDefaultHdfs" , clientPvd) ;
		}
		return clientPvd ;
	}
	
	static class HAZKInfoParser implements EFunction<byte[] , URI , Exception>
	{

		Map<String, URI> mHostName_httpAddrMap = null; ;
		
		String mPvdCacheKey ;
		
		HAZKInfoParser(String aPvdCacheKey , IZKProxy aZKProxy 
				, String aZKPathOfServiceAddrs) throws Exception
		{
			mPvdCacheKey = aPvdCacheKey ;
			refreshAddresses(aZKProxy.getNodeData_Str(aZKPathOfServiceAddrs));
			aZKProxy.watchNode(aZKPathOfServiceAddrs, new Watcher() {

				@Override
				public void process(WatchedEvent aEvent)
				{
					if(aEvent.getType() == EventType.NodeDataChanged)
					{
						try
						{
							refreshAddresses(aZKProxy.getNodeData_Str(aEvent.getPath())) ;
						}
						catch (Exception e)
						{
							sLogger.error(ExceptionAssist.getClearMessage(getClass(), e)) ;
						}
					}
				}
			}) ;
		}
		
		public void refreshAddresses(String aAddrStrs) throws Exception
		{
			String[] addrs = aAddrStrs.split(";") ;
			Map<String, URI> hostURLMap = XC.hashMap() ;
			for(int i=0 ; i<addrs.length ; i++)
			{
				URI uri = URI.create("http://"+addrs[i]) ;
				hostURLMap.put(uri.getHost(), uri) ;
			}
			sLogger.info("缓存键 {} 对应的HttpClientProvider的服务地址切换成：{}" , mPvdCacheKey , aAddrStrs) ;
			mHostName_httpAddrMap = hostURLMap ;
			ZKHttpClientProvider clientPvd = (ZKHttpClientProvider) sHttpClientPvdMap.get(mPvdCacheKey) ;
			if(clientPvd != null)
			{
				URI[] uris = clientPvd.getServiceUrlsFromZK() ;
				if(XC.isEmpty(uris))
					sLogger.error("无法从ZK中的{}节点取得服务地址信息，如果服务地址被注册，将自动连接上" , clientPvd.mZKPath_addr) ;
				else
				{
					clientPvd.setServiceAddrs(uris) ; 
				}
			}
		}
		
		@Override
		public URI apply(byte[] aT) throws InvalidProtocolBufferException
		{
			ActiveNodeInfo activeNodeInfo = HAZKInfoProtos.ActiveNodeInfo.parseFrom(aT) ;
			sLogger.info("ActiveNode是：{}" , activeNodeInfo.getHostname()) ;
			URI uri = mHostName_httpAddrMap.get(activeNodeInfo.getHostname()) ;
			Assert.notNull(uri, "没有取得主机[%s]对应的http服务地址！", activeNodeInfo.getHostname()) ;
			return uri ;
		}
	}

}
