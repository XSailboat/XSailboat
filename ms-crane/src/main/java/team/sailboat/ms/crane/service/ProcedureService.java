package team.sailboat.ms.crane.service;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.security.PublicKey;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;

import jakarta.annotation.PostConstruct;
import team.sailboat.commons.fan.app.App;
import team.sailboat.commons.fan.app.AppPaths;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.event.IXListener;
import team.sailboat.commons.fan.excep.ExceptionAssist;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.exec.CommonExecutor;
import team.sailboat.commons.fan.exec.RunStatus;
import team.sailboat.commons.fan.file.FileExtNameFilter;
import team.sailboat.commons.fan.file.FileUtils;
import team.sailboat.commons.fan.gadget.RSAUtils;
import team.sailboat.commons.fan.http.HttpClient;
import team.sailboat.commons.fan.http.Request;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.serial.StreamAssist;
import team.sailboat.commons.fan.statestore.IRunData;
import team.sailboat.commons.fan.struct.Tuples;
import team.sailboat.commons.fan.struct.Tuples.T2;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.ms.jackson.JacksonUtils;
import team.sailboat.commons.ms.jackson.TLCustmFilter;
import team.sailboat.commons.ms.log.LogMsg;
import team.sailboat.commons.ms.log.LogPool;
import team.sailboat.ms.crane.AppConsts;
import team.sailboat.ms.crane.IApis_PyInstaller;
import team.sailboat.ms.crane.bean.HostProfile;
import team.sailboat.ms.crane.bean.Operation;
import team.sailboat.ms.crane.bean.Procedure;
import team.sailboat.ms.crane.bean.SysProperty;
import team.sailboat.ms.crane.bench.IOperator;
import team.sailboat.ms.crane.bench.Operator_Cmds;
import team.sailboat.ms.crane.bench.Operator_xc1;

/**
 * 程式过程的实现层
 *
 * @author yyl
 * @since 2024年10月10日
 */
@Service
public class ProcedureService
{
	final Logger mLogger = LoggerFactory.getLogger(ProcedureService.class) ;
	static final Pattern sPtnImport = Pattern.compile("^@[^@]*\\.(md|html)$") ;
	
	@Autowired
	SysPlanService mSysPlanService ;
	
	@Autowired
	IRunData mRunData ;
	
	final TreeMap<String , Procedure> mProcedureMap = XC.treeMap(String::compareTo) ;
	File mProcedureDir ;
	
	/**
	 * 命令等的上下文参数
	 */
	final Map<String , String> mContextMap = XC.concurrentHashMap() ;
	/**
	 * 程式过程的执行任务
	 */
	final Map<String , ProcedureExecTask> mProcedureExecTaskMap = XC.autoCleanHashMap_idle(30) ;
	
	@PostConstruct
	void _init() throws Exception
	{
		AppPaths appPaths = App.instance().getAppPaths() ;
		mProcedureDir = new File(appPaths.getConfigDir() , "procedures") ;
		for(File file : mProcedureDir.listFiles(new FileExtNameFilter("yaml")))
		{
			try
			{
				Procedure pd = loadProcedure(file) ;
				mProcedureMap.put(pd.getName() , pd) ;
			}
			catch (Exception e)
			{
				mLogger.error(ExceptionAssist.getClearMessage(getClass(), e)) ;
				throw e ;
			}
		}
		mSysPlanService.addSysPropertiesChangeLsn(event->{
			Tuples.T2<String, String> entry =  (T2<String, String>) event.getSource() ;
			mContextMap.put(entry.getKey() , entry.getValue()) ;
		}) ;
		for(SysProperty property : mSysPlanService.getSysProperties())
		{
			mContextMap.put(property.getName() , property.getValue()) ;
		}
	}
	
	Procedure loadProcedure(File aFile) throws Exception
	{
		Procedure p = JacksonUtils.asBeanFromYaml(aFile , Procedure.class) ;
		p.setName(FileUtils.getCleanName(aFile)) ;
		File dir = aFile.getParentFile() ;
		p.setDescription(checkAndImport(p.getDescription() , dir)) ;
		List<Operation> operList = p.getOperations() ;
		if(XC.isNotEmpty(operList))
		{
			for(Operation oper : operList)
			{
				oper.setDescription(checkAndImport(oper.getDescription() , dir)) ;
			}
		}
		return p ;
	}
	
	String checkAndImport(String aDesc , File aDir) throws IOException
	{
		Matcher matcher = sPtnImport.matcher(aDesc) ;
		if(matcher.matches())
		{
			String fileName = aDesc.substring(1) ;
			return "!!"+matcher.group(1)+"\n"
					+ StreamAssist.load(new File(aDir , fileName) , "UTF-8").toString() ;
		}
		return aDesc ;
	}
	
	void storeProcedure(Procedure aProcedure , File aFile) throws Exception
	{
		try(Closeable c = TLCustmFilter.enable())
		{
			JacksonUtils.storeToYaml(aProcedure, aFile) ;
		}
	}
	
	/**
	 * 所得所有程式过程
	 * @return
	 */
	public Map<String , Procedure> getAllProcedures()
	{
		return mProcedureMap ;
	}
	
	/**
	 * 取得所有程式过程，按catalog分组的
	 * @return
	 */
	public TreeMap<String , TreeSet<Procedure>> getAllProceduresGrouped()
	{
		TreeMap<String , TreeSet<Procedure>> map = XC.treeMap() ;
		mProcedureMap.forEach((pname ,p)->{
			String catalog = JCommon.defaultIfEmpty(p.getCatalog() , AppConsts.sDefaultProcedureCatalog) ;
			TreeSet<Procedure> s = map.get(catalog) ;
			if(s == null)
			{
				s = XC.treeSet((c1 , c2)->{
					return c1.getName().compareTo(c2.getName()) ;
				}) ;
				map.put(catalog , s) ;
			}
			s.add(p) ;
		}) ;
		return map ;
	}
	
	/**
	 * 启用/不启用某一操作
	 * @param aProcedureName
	 * @param aOperationName
	 * @param aEnabled
	 * @throws Exception 
	 */
	public void setOperationEnabled(String aProcedureFileName
			, String aOperationName
			, boolean aEnabled) throws Exception
	{
		Procedure procedure = mProcedureMap.get(aProcedureFileName) ;
		Assert.notNull(procedure , "无效的程式过程文件名：%s" , aProcedureFileName) ;
		Operation oper_0 = XC.findFirst(procedure.getOperations() , oper->oper.getName().equals(aOperationName))
				.orElseThrow(()->new IllegalArgumentException("无效的操作名："+aOperationName))
				;
		if(oper_0.isEnabled() != aEnabled)
		{
			oper_0.setEnabled(aEnabled) ;
			storeProcedure(procedure , new File(mProcedureDir , aProcedureFileName+".yaml")) ;
		}
	}
	
	public void executeProcedure(String aProcedureFileName)
	{
		synchronized(("exec_"+aProcedureFileName).intern())
		{
			Procedure procedure = mProcedureMap.get(aProcedureFileName) ;
			Assert.notNull(procedure , "无效的程式过程文件名：%s" , aProcedureFileName) ;
			ProcedureExecTask pExecTask = mProcedureExecTaskMap.get(aProcedureFileName) ;
			Assert.isTrue(pExecTask == null || pExecTask.isFinished() , "程式过程[%s]正在执行，请待它完成再试！"
					, aProcedureFileName) ;
			
			final LogPool logPool = new LogPool(1000) ;
			pExecTask = new ProcedureExecTask(logPool) ;
			mProcedureExecTaskMap.put(aProcedureFileName, pExecTask) ;
			Set<String> modules = procedure.getModules() ;
			if(XC.isEmpty(modules))
			{
				IOperator.logInfo(logPool , null , "程式过程[{}]没有相关的模块，无法执行！" , aProcedureFileName) ;
				pExecTask.setFinished(true) ;
				return ;
			}
			Collection<HostProfile> hosts = null ;
			if(modules.contains("ALL"))
				hosts = mSysPlanService.getAllHostProfiles() ;
			else
			{
				hosts = mSysPlanService.getHostProfiles(modules) ;
			}
			if(hosts.isEmpty())
			{
				IOperator.logInfo(logPool , null , "程式过程[{}]没有需要在其上执行的主机，无法执行！" , aProcedureFileName) ;
				pExecTask.setFinished(true) ;
				return ;
			}
			List<Operation> operList = XC.extract(procedure.getOperations() , Operation::isEnabled) ;
			if(XC.isEmpty(operList))
			{
				IOperator.logInfo(logPool , null , "程式过程[{}]没有需要执行的操作，无法执行！" , aProcedureFileName) ;
				pExecTask.setFinished(true) ;
				return ;
			}
			pExecTask.run(hosts , operList) ;
		}
	}
	
	public JSONObject getProcedureExecLogs(String aProcedureFileName
			, int aSeq)
	{
		synchronized(("exec_"+aProcedureFileName).intern())
		{
			ProcedureExecTask task = mProcedureExecTaskMap.get(aProcedureFileName) ;
			Assert.notNull(task , "程式过程[%s]最近未执行！" , aProcedureFileName) ;
			JSONObject resultJo = new JSONObject().put("taskFinished" , task.isFinished())
					.put("startSeq" , aSeq) ;
			LogMsg[] logMsgs = task.mLogPool.get(aSeq) ;
			return resultJo.put("endSeq" , aSeq + XC.count(logMsgs))
					.put("data" , logMsgs) ;
		}
	}
	
	static abstract class ExecTask
	{
		final String mOperation ;
		
		public ExecTask(String aOperation)
		{
			mOperation = aOperation ;
		}
		
		public abstract IOperator doTask(LogPool aLogPool , IXListener aFinishLsn) ;
	}
	
	
	static class Xc1Task extends ExecTask
	{
		
		final String mCmd ;
		
		public Xc1Task(String aOperation
				, String aCmd)
		{
			super(aOperation) ;
			mCmd = aCmd ;
		}
		
		@Override
		public IOperator doTask(LogPool aLogPool , IXListener aFinishLsn)
		{
			IOperator operator = new Operator_xc1(aLogPool
					, mOperation
					, mCmd) ;
			if(aFinishLsn != null)
				operator.addFinishListener(aFinishLsn) ;
			IOperator.logInfo(aLogPool , null , "在本地执行相关操作。") ;
			CommonExecutor.exec(operator) ;
			return operator ;
		}
	}
	
	/**
	 * 需要在特定主机上执行的任务
	 *
	 * @author yyl
	 * @since 2024年9月14日
	 */
	static class HostExecTask extends ExecTask
	{
		final HostProfile mHostProfile ;
		
		final String[] mCmds ;
		
		public HostExecTask(HostProfile aHostProfile , String aOperation
				, List<String> aCmds)
		{
			super(aOperation) ;
			mHostProfile = aHostProfile ;
			mCmds = aCmds.toArray(JCommon.sEmptyStringArray) ;
		}
		
		@Override
		public IOperator doTask(LogPool aLogPool , IXListener aFinishLsn)
		{
			IOperator operator = new Operator_Cmds(aLogPool
					, mOperation
					, mHostProfile
					, mCmds) ;
			if(aFinishLsn != null)
				operator.addFinishListener(aFinishLsn) ;
			IOperator.logInfo(aLogPool , null , "在主机[{}]上执行相关操作。" , mHostProfile.getName()) ;
			CommonExecutor.exec(operator) ;
			return operator ;
		}
	}
	
	class ProcedureExecTask
	{
		
		final LogPool mLogPool ;
		Collection<HostProfile> mHosts ;
		List<Operation> mOperList ;
		int mExecOperIndex = 0 ;
		final IXListener mOperFinishListener ;
		final List<ExecTask> mOnboardHostTasks = XC.arrayList() ;
		final List<String> mFinishHosts = XC.arrayList() ;
		private boolean mFinished = false ;
		
		public ProcedureExecTask(LogPool aLogPool)
		{
			mLogPool = aLogPool ;
			List<String> failedHosts = XC.arrayList() ;
			mOperFinishListener = event->{
				Tuples.T2<String, RunStatus> t = event.getSource() ;
				if(t.getEle_2() == RunStatus.failure)
				{
					failedHosts.add(t.getEle_1()) ;
				}
				boolean allFinish = false ;
				synchronized (mFinishHosts)
				{
					mFinishHosts.add(t.getEle_1()) ;
					allFinish = mFinishHosts.size() == mOnboardHostTasks.size() ;
				}
				if(allFinish)
				{
					// 说明当前操作在所有主机上都已经执行完成
					mExecOperIndex++ ;
					if(!failedHosts.isEmpty())
					{
						if(mExecOperIndex == mOperList.size())
						{
							IOperator.logInfo(mLogPool, null , "最后一个操作[{}]在这些主机上[{}]执行失败!"
									, mOperList.get(mExecOperIndex-1).getName()
									, XString.toString(",", failedHosts)) ;
						}
						else
						{
							IOperator.logInfo(mLogPool, null , "因操作[{}]在这些主机上[{}]执行失败，放弃后续操作的执行!"
									, mOperList.get(mExecOperIndex-1).getName()
									, XString.toString(",", failedHosts)) ;
						}
						setFinished(true) ;
					}
					else
					{
						if(mExecOperIndex < mOperList.size())
						{
							mFinishHosts.clear();
							doOperation(mOperList.get(mExecOperIndex)) ;
						}
						else
						{
							// 都已经完成了
							setFinished(true) ;
						}
					}
					
				}
			} ;
		}
		
		public void setFinished(boolean aFinished)
		{
			mFinished = aFinished;
		}
		
		/**
		 * 
		 * 异步执行操作
		 * 
		 * @param aOper
		 */
		void doOperation(Operation aOper)
		{
			mOnboardHostTasks.clear() ;
			List<ExecTask> taskList = XC.arrayList() ;
			List<HostProfile> allHosts = XC.arrayList(mSysPlanService.getAllHostProfiles()) ;
			String all_ip_host = HostProfile.all_ip_host(allHosts) ;
			if(aOper.isLocalOne())
			{
				// 本地一次执行命令
				Map<String , Object> contextMap = XC.hashMap(mContextMap) ;
				contextMap.put("all_ip_host" , all_ip_host) ;
				contextMap.put("hosts" , allHosts) ;
				taskList.add(new  Xc1Task(aOper.getName() , buildRealCmd(contextMap , aOper.getCommands().get(0)))) ;
			}
			else
			{
				try
				{	
					for(HostProfile host : mHosts)
					{
						if(!AppConsts.sHostProfile_SyncStatus_sync.equals(mRunData.get(host.getIp())))
						{
							HttpClient client = HttpClient.of(host.getIp() , host.getSailPyInstallerPort()) ;
							// 取得一个动态公钥
							JSONObject jo = client.askJo(Request.GET().path(IApis_PyInstaller.sGET_RSAPublicKey)) ;
							PublicKey pk = RSAUtils.getPublicKey(jo.optString("publicKeyModulus"), jo.optString("publicKeyExponent")) ;
							String encodedAdminPswd = RSAUtils.encrypt(RSAUtils.sAlgorithm_PKCS1
									, pk
									, host.getAdminPswd()) ;
							String encodedSysPswd = RSAUtils.encrypt(RSAUtils.sAlgorithm_PKCS1
									, pk
									, host.getSysPswd()) ;
							JSONObject hostJo = JacksonUtils.toJSONObject(host)
									.put("adminPswd", encodedAdminPswd)
									.put("sysPswd" , encodedSysPswd) ;
							// 同步主机规划配置
							client.ask(Request.POST()
											.path(IApis_PyInstaller.sPOST_CreateOrUpdateHostProfile)
											.queryParam("codeId" , jo.optString("codeId"))
											.setJsonEntity(hostJo)) ;
							mRunData.put(host.getIp() , AppConsts.sHostProfile_SyncStatus_sync) ;
						}
						
						Map<String , Object> contextMap = XC.hashMap(mContextMap) ;
						contextMap.put("host.seq" , host.getSeq()) ;
						contextMap.put("host.name" , host.getName()) ;
						contextMap.put("host.ip" , host.getIp()) ;
						contextMap.put("host.sysUser" , host.getSysUser()) ;
						contextMap.put("host.adminUser" , host.getAdminUser()) ;
						contextMap.put("all_ip_host" , all_ip_host) ;
						contextMap.put("hosts" , allHosts) ;
						
						List<String> commands = aOper.getCommands() ;
						if(XC.isEmpty(commands))
						{
							IOperator.logInfo(mLogPool ,null , "操作[{}]没有命令列表，忽略！" , aOper.getName()) ;
							continue ;
						}
						
						List<String> realCmds = XC.arrayList() ;
						for(String cmd : commands)
						{
							realCmds.add(buildRealCmd(contextMap , cmd)) ;
						}
						taskList.add(new HostExecTask(host , aOper.getName() , realCmds)) ;
					}
				}
				catch(Exception e)
				{
					setFinished(true) ;
				}
			}
			for(ExecTask task : taskList)
			{
				task.doTask(mLogPool, mOperFinishListener) ;
				mOnboardHostTasks.add(task) ;
			}
		}
		
		public void run(Collection<HostProfile> aHosts , List<Operation> aOperList)
		{
			mHosts = aHosts ;
			mOperList = aOperList ;
			doOperation(mOperList.get(mExecOperIndex)) ;
		}
		
		/**
		 * 是否已经执行完成
		 * @return
		 */
		public boolean isFinished()
		{
			return mFinished ;
		}
		
		String buildRealCmd(Map<String , Object> aContextMap , String aCmd)
		{
			List<String> paramNames = XC.filter(XString.extractParamNames(aCmd)
					, ele->ele.startsWith(":")) ;
			if(!paramNames.isEmpty())
			{
				for(String paramName : paramNames)
				{
					String exprStr = paramName.substring(1) ;
					Expression expr = null ;
					try
					{
						expr = AviatorEvaluator.compile(exprStr , true) ;
					}
					catch(Exception e)
					{
						IOperator.logError(mLogPool ,null , "表达式编译错误！。错误消息：{}。表达式：{}"
								, ExceptionAssist.getRootMessage(e)
								, exprStr) ;
						WrapException.wrapThrow(e, "表达式是：{}", exprStr) ;
					}
					aContextMap.put(paramName , XClassUtil.toString(expr.execute(aContextMap))) ;
				}
			}
			return XString.format(aCmd , aContextMap) ;
		}
	}
}
