package team.sailboat.bd.base.model;

import java.util.Set;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.text.XString;

/**
 * 
 *
 * @author yyl
 * @since 2024年10月9日
 */
public interface IProjTreeNodeTypes
{
	/**
	 * 流程节点
	 */
	public static String sDirFlow = "dir-flow" ;
	
	/**
	 * 流式计算管道
	 */
	public static String sDirStreamCPipe = "dir-stream-cpipe" ;
	
	/**
	 * 批量计算管道
	 */
	public static String sDirDatasetCPipe = "dir-dateset-cpipe" ;
	
	/**
	 * 开发目录
	 */
	public static String sLegalDirDev = "$dir-dev" ;
	/**
	 * 通用目录
	 */
	public static String sLegalDirCommon = "$dir-common" ;
	/**
	 * 数据集成目录
	 */
	public static String sLegalDirDi = "$dir-di" ;
	/**
	 * SailPower目录（引擎目录）
	 */
	public static String sLegalDirSailPower = "$dir-sailpower" ;
	/**
	 * 函数目录
	 */
	public static String sLegalDirFunction = "$dir-func" ;
	
	/**
	 * 计算节点
	 */
	public static String sLegalDirCPipe_NodeSet = "$dir-cpipe-nodeset" ;
	
	/**
	 * 计算管道的资源目录
	 */
	public static String sLegalDirCPipe_Resource = "$dir-cpipe-res" ;
	
	/**
	 * 业务流程的资源目录
	 */
	public static String sLegalDirFlow_Resource = "$dir-res" ;
	/**
	 * 创建的表目录
	 */
	public static String sLegalDirCreateTable = "$dir-create-table" ;
	
	/**
	 * 引用的表目录
	 */
	public static String sLegalDirReferenceTable = "$dir-ref-table" ;
	
	/**
	 * 业务流程资源目录下的文件夹
	 */
	public static String sDirFlow_Res = "dir-res" ;
	
	/**
	 * 计算管道下面的文件夹
	 */
	public static String sDirCPipe_Res = "dir-cpipe_res" ;
	
	/**
	 * 函数目录下的文件夹
	 */
	public static String sDirFunc = "dir-func" ;
	
	/**
	 * 函数
	 */
	public static String sFunc = "func" ;
	
	/**
	 * 资源目录下的jar文件
	 */
	public static String sJar = "jar" ;
	
	/**
	 * 资源目录下的普通文件
	 */
	public static String sFile = "file" ;
	/**
	 * 资源目录下下的py文件
	 */
	public static String sPy = "py" ;
	
	static final Set<String> sOLAFlowDirTypes = XC.hashSet(sDirFlow
			, sLegalDirDev , sLegalDirDi , sLegalDirCommon , sLegalDirSailPower
			, sLegalDirCreateTable , sLegalDirCreateTable 
			, sLegalDirFunction , sLegalDirFlow_Resource
			, sDirFlow_Res , sDirFunc
			, sLegalDirReferenceTable) ;
	
	static final Set<String> sCPipeDirTypes = XC.hashSet(sDirStreamCPipe
			, sDirDatasetCPipe
			, sLegalDirCPipe_NodeSet
			, sLegalDirCPipe_Resource
			, sDirCPipe_Res) ;
	
	
	public final static Set<String> sFlowResDirAndFileTypes = XC.hashSet(sDirFlow_Res
			, sJar , sFile , sPy) ;
	
	public final static Set<String> sCPipeResDirAndFileTypes = XC.hashSet(sDirCPipe_Res
			, sJar , sFile) ;
	
	public final static Set<String> sCanCreateChildDirTypes = XC.hashSet(sLegalDirFlow_Resource
			, sLegalDirFunction , sDirFlow_Res , sDirFunc
			, sLegalDirCPipe_Resource , sDirCPipe_Res) ;
	
	/**
	 * 是否是资源目录下（不包括缺省的资源根目录）的文件或文件夹
	 * @param aType
	 * @return
	 */
	public static boolean isFlowResourceDirOrFile(String aType)
	{
		return sFlowResDirAndFileTypes.contains(aType) ;
	}
	
	public static boolean isFunctionDirOrFunc(String aType)
	{
		return sFunc.equals(aType) || sDirFunc.equals(aType) ;
	}
	
	/**
	 * 是否可以在指定类型的父节点下创建目录
	 * @param aParentType
	 * @return
	 */
	public static boolean canCreateChildDir(String aParentType)
	{
		return sCanCreateChildDirTypes.contains(aParentType) ;
	}
	
	/**
	 * 是否可以在指定类型的父节点下创建指定类型的子节点
	 * @param aParentType
	 * @param aChildType
	 * @return
	 */
	public static boolean canCreateChildFile(String aParentType
			, String aChildType)
	{
		if(sLegalDirFlow_Resource.equals(aParentType)
				|| sDirFlow_Res.equals(aParentType))
		{
			return sJar.equals(aChildType)
					|| sPy.equals(aChildType)
					|| sFile.equals(aChildType) ;
		}
		else if(sLegalDirFunction.equals(aParentType)
				|| sDirFunc.equals(aParentType))
		{
			return sFunc.equals(aChildType) ;
		}
		else if(sLegalDirCPipe_Resource.equals(aParentType)
				|| sDirCPipe_Res.equals(aParentType))
		{
			return sJar.equals(aChildType)
					|| sFile.equals(aChildType) ;
		}
		return false ;
	}
	
	public static String getChildDirType(String aParentType)
	{
		if(sLegalDirFlow_Resource.equals(aParentType)
				|| sDirFlow_Res.equals(aParentType))
		{
			return sDirFlow_Res ;
		}
		else if(sLegalDirFunction.equals(aParentType)
				|| sDirFunc.equals(aParentType))
		{
			return sDirFunc ;
		}
		if(sLegalDirCPipe_Resource.equals(aParentType)
				|| sDirCPipe_Res.equals(aParentType))
		{
			return sDirCPipe_Res ;
		}
		throw new IllegalArgumentException(XString.msgFmt("不支持在指定类型[{}]的父节点下创建子目录" , aParentType)) ;
	}
	
	/**
	 * 是否是离线分析流程目录
	 * @param aType
	 * @return
	 */
	public static boolean isOLAFlowDir(String aType)
	{
		return sOLAFlowDirTypes.contains(aType) ;
	}
	
	/**
	 * 是否是实时计算管道目录
	 * @param aType
	 * @return
	 */
	public static boolean isCPipeDir(String aType)
	{
		return sCPipeDirTypes.contains(aType) ;
	}
}
