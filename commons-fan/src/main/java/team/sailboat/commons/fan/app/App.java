package team.sailboat.commons.fan.app;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.util.Date;
import java.util.function.Supplier;

import team.sailboat.commons.fan.collection.HashMultiMap;
import team.sailboat.commons.fan.collection.IMultiMap;
import team.sailboat.commons.fan.collection.SizeIter;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.exec.CommonExecutor;
import team.sailboat.commons.fan.http.IdentityTrace;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.log.ILogListener;
import team.sailboat.commons.fan.log.Log;
import team.sailboat.commons.fan.serial.StreamAssist;
import team.sailboat.commons.fan.sys.XNet;
import team.sailboat.commons.fan.text.XString;

public class App
{
	public static enum Status
	{
		/**
		 * 启动中
		 */
		Starting(0),
		/**
		 * 服务可用
		 */
		Active(1),
		/**
		 * 停止过程中
		 */
		Stopping(2),
		/**
		 * 候补中
		 */
		StandBy(3) ;
		
		int mCode ;
		
		Status(int aCode)
		{
			mCode = aCode ;
		}
		 
	}
	
	protected static App sInstance ; 
	
	public static App instance()
	{
		if(sInstance == null)
		{
			sInstance = new App() ;
		}
		return sInstance ; 
	}
	
	Status mStatus ;
	
	protected String[] mAppArgs ;
	String mName ;
	String mVersion ;
	String mDescription ;
	String mSysEnv = "prod";
	
	Supplier<Boolean> mActiveCondition ;
	Runnable mActivePerformer ;
	Runnable mStandbyPerformer ;
	Runnable mStopPerformer ;
	
	Date mStartTime ;
	/**
	 * 第几次启动
	 */
	long mStartNum ;
	
	String mExtendTipMessage ;
	
	protected final AppPaths mAppPaths = new AppPaths() ;
	
	final IMultiMap<Status , Runnable> mPerformerMap = new HashMultiMap<>() ;
	
	public App()
	{
		try
		{
			Class<?> clazz = getClass().getClassLoader().loadClass("team.sailboat.commons.fan.log.Slf4jLogAdapter") ;
			try
			{
				Log.addListener((ILogListener)clazz.getConstructor().newInstance());
			}
			catch (Exception e)
			{
				Log.error("无法实例化类："+clazz.getName()) ;
				e.printStackTrace();
			}
		}
		catch (ClassNotFoundException e)
		{
		}
	}
	
	public App withIdentifier(String aName , String aVer , String aDesc , String aAppCategory)
	{
		mName = aName ;
		mVersion = aVer ;
		mDescription = aDesc ;
		IdentityTrace.setLocalModuleName(aName) ;
		mAppPaths.setAppCategory(aAppCategory) ;
		return this ;
	}
	
	public AppPaths getAppPaths()
	{
		return mAppPaths;
	}
	
	/**
	 * 应用程序的命令行参数
	 * @param aArgs
	 * @return
	 */
	public App withApplicationArgs(String... aArgs)
	{
		mAppArgs = aArgs ;
		
		int k = XC.indexOf(aArgs, "-x_console") ;
		if(k != -1)
			Log.setPrintOnConsole(true);
		
    	k = XC.indexOf(aArgs, "-sys_env") ;
		if(k != -1)
		{
			Assert.isTrue(k<aArgs.length-1 , "没有指定参数sys_env的参数值") ;
			mSysEnv = aArgs[k+1] ;
			Assert.isIn(mSysEnv , "sys_env的参数值["+mSysEnv+"]不合法", "prod" , "dev" , "test");
		}
		System.setProperty("sys_env", mSysEnv) ;
		Log.info("当前的系统环境是：" + mSysEnv) ;
		
		k = XC.indexOf(aArgs , "-natIp") ;
		if(k != -1)
		{
			try
			{
				XNet.getValidLocalIPv4s().add(aArgs[k+1]) ;
			}
			catch (SocketException e)
			{
				WrapException.wrapThrow(e) ;
				return this ;			// dead code
			}
		}
		
		k = XC.indexOf(aArgs , "-preferedNetSeg") ;
		if(k != -1)
		{
			Assert.isTrue(k<aArgs.length-1 , "没有指定参数preferedNetSeg的参数值！") ;
			XNet.setPreferedNetSeg(aArgs[k+1]) ;
			try
			{
				Log.info("首选网段：{}，首选本地IP：{}", aArgs[k+1] , XNet.getPreferedIpv4());
			}
			catch (SocketException e)
			{
				e.printStackTrace();
			}
		}

		return this ;
	}
	
	/**
	 * 取得程序运行的系统环境			<br>
	 * @return	dev（开发环境）、test(测试环境)、prod（生产环境）
	 */
	public String getSysEnv()
	{
		return mSysEnv;
	}
	
	/**
	 *  应用程序可以成为active状态的判定条件
	 * @param aCnd
	 * @return
	 */
	public App withActiveCondition(Supplier<Boolean> aCnd)
	{
		mActiveCondition = aCnd ;
		return this ;
	}
	
	/**
	 * 应用程序成为Active状态所需的一系列动作
	 * @param aPerformer
	 * @return
	 */
	public App withActivePerformer(Runnable aPerformer)
	{
		mActivePerformer = aPerformer ;
		return this ;
	}
	
	/**
	 * 应用程序后成为standBy状态所需的一系列动作
	 * @param aPerformer
	 * @return
	 */
	public App withStandyPerformer(Runnable aPerformer)
	{
		mStandbyPerformer = aPerformer ;
		return this ;
	}
	
	/**
	 *  停止应用时的一系列清理动作
	 * @param aPerformer
	 * @return
	 */
	public App withStopPerformer(Runnable aPerformer)
	{
		mStopPerformer = aPerformer ;
		return this ;
	}
	
	public App withExtendTipMessage(String aMessage)
	{
		mExtendTipMessage = aMessage ;
		return this ;
	}
	
	public synchronized App s0_init(Runnable aPerformer)
	{
		if(aPerformer != null)
			aPerformer.run() ;
		
		_countBaseInfo();
		return this ;
	}
	
	void _countBaseInfo()
	{
		try
		{
			System.out.println("本机IP:"+XString.toString(",", XNet.getLocalIPs())) ;
		}
		catch (SocketException e)
		{
			e.printStackTrace();
		}
	}
	
	public synchronized App s1_start(Runnable aPerformer)
	{
		mStatus = Status.Starting ;
		mStartTime = new Date() ;
		if(aPerformer != null)
			aPerformer.run();
		return this ;
	}
	
	public Date getStartTime()
	{
		return mStartTime ;
	}
	
	public synchronized App active()
	{
		if(mStatus != Status.Active)
		{
			if(mActiveCondition == null || Boolean.TRUE.equals(mActiveCondition.get()))
			{
				if(mActivePerformer != null)
					mActivePerformer.run();
			}
			mStatus = Status.Active ;
			notifyStatusChanged();
		}
		return this ;
	}
	
	/**
	 * 使当前线程挂起，直到应用程序退出
	 * @return
	 */
	public App s3_waiting()
	{
		runSTDMessageLoop();
		return this ;
	}
	
	/**
	 * 使应用处于Standby状态
	 * @return
	 */
	public synchronized App standby()
	{
		if(mStatus != Status.StandBy)
		{
			if(mStandbyPerformer != null)
				mStandbyPerformer.run() ;
			mStatus = Status.StandBy ;
			notifyStatusChanged() ;
		}
		return this ;
	}
	
	private void notifyStatusChanged()
	{
		Log.info("应用处于 {} 状态。" , mStatus);
		SizeIter<Runnable> it = mPerformerMap.get(mStatus) ;
		if(it != null && !it.isEmpty())
		{
			it.forEach(CommonExecutor::safeRun);
		}
	}
	
	/**
	 * 停止应用
	 */
	public synchronized void stop()
	{
		if(mStatus == Status.Active)
			standby() ;
		mStatus = Status.Stopping ;
		notifyStatusChanged() ;
		if(mStopPerformer != null)
		{
			try
			{
				mStopPerformer.run();
			}
			catch(Throwable e)
			{
				e.printStackTrace() ;
			}
		}
		System.exit(0) ;
	}
	
	public Status getStatus()
	{
		return mStatus ;
	}
	
	public String[] getStartArgs()
	{
		return mAppArgs ;
	}
	
	public String getName()
	{
		return mName;
	}
	
	public String getVersion()
	{
		return mVersion;
	}
	
	public String getDescription()
	{
		return mDescription;
	}
	
	public void setStartNum(long aStartNum)
	{
		mStartNum = aStartNum;
	}
	
	/**
	 * 第几次启动，不总是有效，得应用有记录和累加机制时，它才是有效的。<br />
	 * 0是无效数值，从1开始才认为被设置过，是有效的
	 * @return
	 */
	public long getStartNum()
	{
		return mStartNum;
	}
	
	protected void coutln(String aMsg , Object...aArgs)
	{
		Log.info(aMsg, aArgs) ;
	}
	
	/**
	 * 单程序转变成指定状态时调用一次。
	 * 
	 * @param aStatus		Activeh活着Standy
	 * @param aRun
	 * @param aCheckCurrentStatus			当为true时，检查当前状态,如果当前状态符合期望，就立即触发一次 
	 */
	public synchronized void registerPerformer(Status aStatus , Runnable aRun
			, boolean aCheckCurrentStatus)
	{
		if(aRun == null)
			return ;
		Assert.isTrue(aStatus == Status.Active || aStatus == Status.StandBy || aStatus == Status.Stopping);
		if(aCheckCurrentStatus && aStatus == mStatus)
		{
			CommonExecutor.safeRun(aRun) ;
		}
		mPerformerMap.put(aStatus, aRun) ;
	}
	
	void runSTDMessageLoop()
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String input = null;
		bp_0:while (true)
		{
			FileOutputStream out = null;
			try
			{
				System.out.flush();
				coutln("===================================================");
				coutln("===================================================");
				coutln("应 用 名 字：{} {}" , mName , mVersion);
				coutln("运 行 模 式：控制台模式");
				coutln("应 用 描 述："+mDescription);
				coutln("") ;
				if(XString.isNotEmpty(mExtendTipMessage))
					coutln(mExtendTipMessage) ;
				System.out.println();
				System.out.println("请输入字母选择相应命令〔please select〕,键入回车执行:");
				System.out.println("    x: eXit               - 退出");
				System.out.println("    i: infomation         - 模块信息");
				System.out.println("");
				System.out.println(">");
				input = br.readLine().trim();
				if (input.length() == 0)
					continue;
				else
				{
					long startTime = System.currentTimeMillis();
					switch (input.charAt(0))
					{
					case 'x':
					case 'X':
						Log.info("接收控制台命令，服务器主动退出");
						stop();
						break bp_0;
					case 'I':
					case 'i':
						
						break;
					default:
						System.out.println("无效命令：" + input);
					}
					System.out.println("运行指定命令耗时：" + (System.currentTimeMillis() - startTime) + "毫秒");
				}
			}
			catch (Exception e)
			{
				System.out.println("执行命令失败：" + e.getMessage());
				while (true)
				{
					JCommon.sleepInSeconds(100);;
				}
			}
			finally
			{
				StreamAssist.close(out);
			}
		}
	}
}
