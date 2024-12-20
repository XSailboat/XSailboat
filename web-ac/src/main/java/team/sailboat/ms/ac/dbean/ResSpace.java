package team.sailboat.ms.ac.dbean;

import jakarta.validation.constraints.NotBlank;
import team.sailboat.commons.fan.dpa.anno.BColumn;
import team.sailboat.commons.fan.dpa.anno.BDataType;
import team.sailboat.commons.fan.dpa.anno.BFeature;
import team.sailboat.commons.fan.dpa.anno.BTable;
import team.sailboat.commons.fan.dtool.DBType;
import team.sailboat.commons.fan.dtool.mysql.MySQLFeatures;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.web.ac.IAuthCenterConst;
import team.sailboat.dplug.anno.DBean;

/**
 * 资源空间			<br>
 * 
 * 资源空间的id格式：rs#appId#resId		<br />
 * 缺省全局资源空间的id是:rs#appId#
 *
 * @author yyl
 * @since 2021年10月29日
 */
@BTable(name="ac_res_space" , comment="资源空间"  , features = {
		@BFeature(type = DBType.MySQL , name = MySQLFeatures.TABLE__ENGINE , value = "InnoDB") , 
		@BFeature(type = DBType.MySQL , name = MySQLFeatures.TABLE__CHARACTER_SET , value = "utf8")} ,
	id_category = "ResSpace" , id_prefix = "rs"
)
@DBean(genBean = true , recordCreate = true)
public class ResSpace
{
	/**
	 * 资源空间类型：缺省全局空间
	 */
	public static String sType_DefaultGlobal = IAuthCenterConst.sResSpaceType_default ;
	
	@BColumn(name="id" , dataType = @BDataType(name="string" , length = 64) , comment="唯一性标识" , seq = 0 , primary = true)
	String id ;
	
	@NotBlank
	@BColumn(name="client_app_id" , dataType = @BDataType(name="string" , length = 32) , comment="应用id" , seq = 1)
	String clientAppId ;
	
	@NotBlank
	@BColumn(name="res_id" , dataType = @BDataType(name="string" , length = 32) , comment="资源id。资源id在ClientApp范围内是唯一的" , seq = 2)
	String resId ;
	
	@NotBlank
	@BColumn(name="res_name" , dataType = @BDataType(name="string" , length = 32) , comment="资源名称" , seq = 3)
	String resName ;
	
	@NotBlank
	@BColumn(name="type" , dataType = @BDataType(name="string" , length = 32) , comment="空间类型" , seq = 5)
	String type  ;
	
	/**
	 * 
	 * 是否是缺省全局空间的资源空间id
	 * 
	 * @param aResSpaceId
	 * @return
	 */
	public static boolean isDefaultGlobal(String aResSpaceId)
	{
		return aResSpaceId != null && aResSpaceId.endsWith("#") ;
	}
	
	/**
	 * 
	 * 从资源空间id中提取出ClientApp的id
	 * 
	 * @param aResSpaceId
	 * @return
	 */
	public static String getClientAppIdFrom(String aResSpaceId)
	{
		return XString.seg_i(aResSpaceId, '#', 1) ;
	}
	
	/**
	 * 
	 * 从资源空间id中提取出资源id
	 * 
	 * @param aResSpaceId
	 * @return
	 */
	public static String getResIdFrom(String aResSpaceId)
	{
		return XString.seg_i(aResSpaceId, '#', 2) ;
	}
	
	/**
	 * 
	 * 根据ClientApp的id计算出这个ClientApp的缺省全局资源空间id
	 * 
	 * @param aClientAppId
	 * @return
	 */
	public static String getDefaultGlobalResSpaceId(String aClientAppId)
	{
		return "rs#"+aClientAppId+"#" ;
	}
	
	/**
	 * 
	 * 新建一个指定应用的缺省全局资源空间的BResSpace
	 * 
	 * @param aClientAppId
	 * @return
	 */
	public static BResSpace newDefaultGlobalBResSpace(String aClientAppId)
	{
		ResSpace.BResSpace dgResSpace = new ResSpace.BResSpace() ;
		dgResSpace.setClientAppId(aClientAppId) ;
		dgResSpace.setType(sType_DefaultGlobal) ;
		return dgResSpace ;
	}
	
	/**
	 * 
	 * 拼接资源空间id
	 * 
	 * @param aClientAppId
	 * @param aResId
	 * @return
	 */
	public static String spliceResSpaceId(String aClientAppId , String aResId)
	{
		return "rs#"+aClientAppId+"#"+JCommon.defaultIfEmpty(aResId , "") ;
	}
}
