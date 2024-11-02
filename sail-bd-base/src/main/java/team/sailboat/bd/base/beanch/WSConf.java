package team.sailboat.bd.base.beanch;

import team.sailboat.base.def.WorkEnv;
import team.sailboat.commons.fan.collection.PropertiesEx;

public class WSConf
{
	/**
	 * Flink的JobManager的内存大小，单位MB
	 */
	public static final String sFLINK_JobManager_MemSize = "flink.job-manager.memory" ;
	
	/**
	 * Flink的JobManager的虚拟CPU数量
	 */
	public static final String sFLINK_JobManager_VCpus = "flink.job-manager.vcpus" ;
	
	/**
	 * Flink的TaskManager的插槽数量
	 */
	public static final String sFLINK_TaskManager_Slots = "flink.task-manager.slots" ;
	
	final PropertiesEx mPropEx ;
	
	WorkEnv mEnv ;
	
	public WSConf(WorkEnv aEnv)
	{	
		this(aEnv , new PropertiesEx()) ;
	}
	
	public WSConf(WorkEnv aEnv , PropertiesEx aPropEx)
	{	
		mEnv = aEnv ;
		mPropEx = aPropEx ;
	}
	
	public int get(String aKey , int aDefaultValue)
	{
		return mPropEx.getInt(aKey, aDefaultValue) ;
	}
	
	public WSConf set(String aKey , int aValue)
	{
		return set(mEnv, aKey, aValue) ;
	}
	
	public WSConf set(WorkEnv aEnv , String aKey , int aValue)
	{
		mPropEx.setProperty(aEnv.name() + "." + aKey, Integer.toString(aValue)) ;
		return this ;
	}
	
	public PropertiesEx getProperties()
	{
		return mPropEx ;
	}
	
	public static WSConf createDefault(WorkEnv aEnv)
	{
		WSConf conf = new WSConf(aEnv) ;
		conf.set(WorkEnv.dev , sFLINK_JobManager_MemSize , 512) ;
		conf.set(WorkEnv.dev , sFLINK_JobManager_VCpus , 2) ;
		conf.set(WorkEnv.dev , sFLINK_TaskManager_Slots , 3) ;
		conf.set(WorkEnv.prod , sFLINK_JobManager_MemSize , 1024) ;
		conf.set(WorkEnv.prod , sFLINK_JobManager_VCpus , 4) ;
		conf.set(WorkEnv.prod , sFLINK_TaskManager_Slots , 6) ;
		return conf ;
	}
}
