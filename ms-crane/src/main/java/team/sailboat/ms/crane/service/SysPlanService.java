package team.sailboat.ms.crane.service;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Validator;
import team.sailboat.commons.fan.app.App;
import team.sailboat.commons.fan.app.AppPaths;
import team.sailboat.commons.fan.collection.PropertiesEx;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.event.IXListener;
import team.sailboat.commons.fan.event.XEvent;
import team.sailboat.commons.fan.event.XListenerAssist;
import team.sailboat.commons.fan.excep.ExceptionAssist;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.gadget.RSAUtils;
import team.sailboat.commons.fan.http.HttpClient;
import team.sailboat.commons.fan.http.Request;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.statestore.IRunData;
import team.sailboat.commons.fan.struct.Tuples;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.ms.jackson.JacksonUtils;
import team.sailboat.commons.ms.jackson.TLCustmFilter;
import team.sailboat.commons.ms.valid.ValidateUtils;
import team.sailboat.ms.crane.AppConfig;
import team.sailboat.ms.crane.AppConsts;
import team.sailboat.ms.crane.IApis_PyInstaller;
import team.sailboat.ms.crane.bean.HostProfile;
import team.sailboat.ms.crane.bean.SysProperty;
import team.sailboat.ms.crane.bean.ValidResult;
import team.sailboat.ms.crane.cmd.LocalCmds;

/**
 * 
 * 系统规划的后端逻辑实现
 *
 * @author yyl
 * @since 2024年10月30日
 */
@Service
public class SysPlanService
{
	final Logger mLogger = LoggerFactory.getLogger(getClass()) ;
	
	@Autowired
	AppConfig mAppConf ;
	
	@Autowired
    Validator mValidator;
	
	@Autowired
	IRunData mRunData ;
	
	PropertiesEx mSysProperties ;
	
	final Map<String , HostProfile> mHostProfiles = XC.treeMap(String::compareTo) ;
	
	final TreeMap<String , SysProperty> mSysPropMap = XC.treeMap() ;
	
	final XListenerAssist mSysPropertiesChangeLsnAssist = new XListenerAssist() ;
	
	File mCustomSysPropFile ;
	
	File mHostProfilesFile ;
	
	@PostConstruct
	void _init() throws IOException
	{
		AppPaths appPaths = App.instance().getAppPaths() ;
		mCustomSysPropFile = appPaths.getConfigFile("custom_sys_param.ini") ;
		Properties defaultProps = mAppConf.getSys_params() ;
		mSysProperties = new PropertiesEx(defaultProps) ;
		loadCustomSysProperties(mSysProperties) ;
		XC.forEach(mSysProperties.propertyNames() , key->{
			String pn = (String)key ;
			if(!pn.endsWith(".description")
					|| (pn.startsWith("modules") && XString.count(pn , '.', 0) == 2))		// 模块的说明
			{
				mSysPropMap.put(pn , new SysProperty(pn , mSysProperties.getProperty(pn)
						, mSysProperties.getProperty(pn+".description"))) ;
			}
		}) ;
		// 主机规划的信息
		mHostProfilesFile = appPaths.getConfigFile("host_profiles.yaml") ;
		
		if(mHostProfilesFile.exists())
		{
			Map<String, HostProfile> hostProfileMap = JacksonUtils.asLinkedHashMapFromYaml(mHostProfilesFile 
					, String.class , HostProfile.class) ;
			hostProfileMap.forEach((hostName , hostProfile)->{
				hostProfile.setName(hostName) ;
				mHostProfiles.put(hostName, hostProfile) ;
			});
		}
		
		LocalCmds.getEnv().setAllHostSupplier(this::getAllHostProfiles) ;
		LocalCmds.getEnv().setAllModuleSupplier(this::getAllModules);
	}
	
	/**
	 * 加载用户设置的个性化设置项
	 * @param aProps
	 * @throws IOException 
	 */
	void loadCustomSysProperties(PropertiesEx aProps) throws IOException
	{
		if(mCustomSysPropFile.exists())
		{
			aProps.load(mCustomSysPropFile);
		}
	}
	
	void storeCustomSysProperties()
	{
		try
		{
			mSysProperties.store(mCustomSysPropFile) ;
		}
		catch (IOException e)
		{
			WrapException.wrapThrow(e) ;
		}
	}
	
	void storeHostProfiles() throws Exception
	{
		try(Closeable c = TLCustmFilter.enable())
		{
			JacksonUtils.storeToYaml(mHostProfiles , mHostProfilesFile) ;
		}
	}
	
	/**
	 * 取得所有主机信息
	 * @return
	 */
	public Collection<HostProfile> getAllHostProfiles()
	{
		return mHostProfiles.values() ;
	}
	
	/**
	 * 创建一个主机信息
	 * @param aHostProfile
	 * @throws Exception 
	 */
	public void createHostProfile(HostProfile aHostProfile) throws Exception
	{
		ValidateUtils.validateAndThrow(mValidator ,aHostProfile) ;
		Assert.isNull(mHostProfiles.get(aHostProfile.getName()) , "已经存在名为 %s 的主机配置！" , aHostProfile.getName()) ;
		mHostProfiles.put(aHostProfile.getName()  , aHostProfile) ;
		storeHostProfiles();
	}
	
	/**
	 * 更新指定主机名的一个主机信息			<br />
	 * @param aHostProfile
	 * @param aOldHostName			如果修改了主机名，则必需设置aOldHostName
	 * @throws Exception
	 */
	public void updateHostProfile(HostProfile aHostProfile , String aOldHostName) throws Exception
	{
		ValidateUtils.validateAndThrow(mValidator , aHostProfile) ;
		if(XString.isEmpty(aOldHostName) || JCommon.equals(aHostProfile.getName() , aOldHostName))
		{
			// 没有修改主机名
			Assert.notNull(mHostProfiles.get(aHostProfile.getName()) , "不存在名为 %s 的主机配置！" , aHostProfile.getName()) ;
		}
		else
		{
			// 修改了主机名
			Assert.notNull(mHostProfiles.remove(aOldHostName) , "不存在名为 %s 的主机配置！" , aOldHostName) ;
		}
		mHostProfiles.put(aHostProfile.getName()  , aHostProfile) ;
		mRunData.put(aHostProfile.getIp() , AppConsts.sHostProfile_SyncStatus_changed) ;
		storeHostProfiles();
	}
	
	/**
	 * 删除一个主机信息
	 * @param aHostname
	 * @throws Exception 
	 */
	public void deleteHostProfile(String aHostname) throws Exception
	{
		mHostProfiles.remove(aHostname) ;
		storeHostProfiles();
	}
	
	/**
	 * 从指定的名称的主机规划中取消部署指定的模块
	 * @param aHostName
	 * @param aModuleName
	 * @throws Exception
	 */
	public void removeModuleFromHostProfile(String aHostName , String aModuleName) throws Exception
	{
		HostProfile hostProfile = mHostProfiles.get(aHostName) ;
		Assert.notNull(hostProfile , "不存在名为 %s 的主机！" , aHostName) ;
		if(hostProfile.removeDeployModuleName(aModuleName))
		{
			mRunData.put(hostProfile.getIp() , AppConsts.sHostProfile_SyncStatus_changed) ;
			storeHostProfiles() ;
		}
	}
	
	/**
	 * 取得系统参数表
	 * @return
	 */
	public List<SysProperty> getSysProperties()
	{
		return XC.arrayList(mSysPropMap.values()) ;
	}
	
	/**
	 * 更新系统参数表			<br />
	 * 修改即保存
	 * @param aKey
	 * @param aValue
	 * @param aDescription
	 */
	public void updateSysProperty(String aKey , String aValue
			, String aDescription)
	{
		String propValue = mSysProperties.getProperty(aKey, null) ;
		boolean valueChanged = XString.isNotEmpty(aValue)
				&& JCommon.unequals(propValue, aValue) ;
		String descKey = aKey+".description" ;
		boolean descriptionChanged = XString.isNotEmpty(aDescription)
				&& JCommon.unequals(aDescription , mSysProperties.get(descKey)) ;
		if(valueChanged || descriptionChanged)
		{
			if(valueChanged)
				mSysProperties.setProperty(aKey , aValue) ;
			if(descriptionChanged)
				mSysProperties.setProperty(descKey , aDescription) ;
			SysProperty sysProp = mSysPropMap.get(aKey) ;
			if(sysProp != null)
			{
				if(valueChanged)
					sysProp.setValue(aValue) ;
				if(descriptionChanged)
					sysProp.setDescription(aDescription);
			}
			storeCustomSysProperties();
			if(valueChanged)
				mSysPropertiesChangeLsnAssist.notifyLsns(new XEvent(Tuples.of(aKey , aValue) , 0));
		}
	}
	
	/**
	 * 创建一个新的系统参数
	 * @param aKey
	 * @param aValue
	 * @param aDescription
	 */
	public void createSysProperty(String aKey , String aValue
			, String aDescription)
	{
		String propValue = mSysProperties.getProperty(aKey, null) ;
		Assert.isNull(propValue , "已经存在名为 %s 的键！" , aKey) ;	
		mSysProperties.setProperty(aKey , aValue) ;
		mSysPropMap.put(aKey, new SysProperty(aKey, aValue, aDescription)) ;
		storeCustomSysProperties();
		mSysPropertiesChangeLsnAssist.notifyLsns(new XEvent(Tuples.of(aKey , aValue) , 0));
	}
	
	/**
	 * 添加系统参数改变的监听器		<br />
	 * Event的source是Tuples.T2
	 * @param aLsn
	 */
	public void addSysPropertiesChangeLsn(IXListener aLsn)
	{
		mSysPropertiesChangeLsnAssist.addListener(aLsn) ;
	}
	
	/**
	 * 取得将部署指定模块的主机
	 * @param aModules
	 * @return
	 */
	public List<HostProfile> getHostProfiles(Collection<String> aModules)
	{
		List<HostProfile> hostList = XC.arrayList() ;
		for(HostProfile host : mHostProfiles.values())
		{
			TreeSet<String> moduleNames = host.getDeployModuleNames() ;
			if(XC.isEmpty(moduleNames))
				continue ;
			if(XC.containsAny(moduleNames , aModules))
			{
				hostList.add(host) ;
			}
		}
		return hostList ;
	}
	
	/**
	 * 取得所有软件模块		<br />
	 * 从系统参数里面获取
	 * @return
	 */
	public Collection<team.sailboat.ms.crane.bean.Module> getAllModules()
	{
		Map<String , team.sailboat.ms.crane.bean.Module> moduleMap = XC.treeMap() ;
		mSysPropMap.subMap("modules.", true , "modulet." , false)
			.forEach((key, value)->{
				String moduleName = XString.seg_i(key, '.', 1) ;
				team.sailboat.ms.crane.bean.Module module = moduleMap.get(moduleName)  ;
				if(module == null)
				{
					String descKey = value.getName()+".description" ;
					String desc = mSysProperties.getString(descKey) ;
					module = new team.sailboat.ms.crane.bean.Module(moduleName , desc) ;
					String portsKey = value.getName()+".ports" ;
					String[] ports = mSysProperties.getStringArray(portsKey) ;
					if(XC.isNotEmpty(ports))
					{
						TreeSet<Integer> ports_0 = XC.extract(ports, Integer::valueOf, TreeSet::new) ;
						module.setPorts(ports_0) ;
					}
					
					moduleMap.put(moduleName, module) ;
				}
			}) ;
		return moduleMap.values() ;
	}
	
	/**
	 * 
	 * 验证主机的连通性，以及管理员账号和密码
	 * 
	 * @param aHostName
	 */
	public ValidResult validateHostInfo(String aHostName)
	{
		HostProfile host = mHostProfiles.get(aHostName) ;
		if(host == null)
			return new ValidResult(false, "不存在名为%s的主机！".formatted(aHostName)) ;
		return validateHostUserPswd(host.getName() , host.getIp() , host.getSailPyInstallerPort()
				, host.getAdminUser() , host.getAdminPswd()) ;
	}
	
	/**
	 * 
	 * 验证指定的用户名密码，在指定的主机上是否正确
	 * 
	 * @param aHostName			主机名，只起提示作用，可以不设置
	 * @param aIp
	 * @param aPort				SailPyInstaller的端口
	 * @param aUsername
	 * @param aPassword
	 * @return
	 */
	public ValidResult validateHostUserPswd(String aHostName , String aIp , int aPort
			, String aUsername
			, String aPassword)
	{
		HttpClient client = HttpClient.of(aIp , aPort) ;
		try
		{
			JSONObject jo = client.askJo(Request.GET().path(IApis_PyInstaller.sGET_RSAPublicKey)) ;
			String encodedPswd = RSAUtils.encrypt(RSAUtils.sAlgorithm_PKCS1
					, RSAUtils.getPublicKey(jo.optString("publicKeyModulus"), jo.optString("publicKeyExponent"))
					, aPassword) ;
			
			String result = client.askForString(Request.POST().path(IApis_PyInstaller.sPOST_ValidateUserAndPswd)
					.setJsonEntity(new JSONObject().put("username" , aUsername)
							.put("password" , encodedPswd)
							.put("codeId" , jo.optString("codeId")))) ;
			if(XString.isEmpty(result))
				return new ValidResult(true, "管理员用户和密码正确。") ;
			else
				return new ValidResult(false, "管理员用户或密码不正确！") ;
		}
		catch(SocketException e)
		{
			mLogger.info(ExceptionAssist.getClearMessage(getClass(), e)) ;
			return new ValidResult(false, "主机 %s[%s:%s] 的服务无法连通！".formatted(
					JCommon.defaultIfEmpty(aHostName , "") , aIp , aPort)) ;
		}
		catch (Exception e)
		{
			mLogger.error(ExceptionAssist.getStackTrace(e)) ;
			return new ValidResult(false , "验证失败！") ;
		}
	}
}
