package team.sailboat.commons.web.ac;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import team.sailboat.commons.fan.lang.JCommon;

/**
 * 
 * 用户角色
 *
 * @author yyl
 * @since 2024年10月21日
 */
@Schema(description = "用户角色")
@Data
@Builder
public class ARole
{
	@Schema(description = "角色名")
	String name ;
	
	@Schema(description = "角色描述")
	String description ;
	
	@Schema(description = "资源空间类型。不设置表示是“缺省全局资源空间”")
	String resSpaceType ;
	
	/**
	 * 
	 * 取得资源空间类型
	 * 
	 * @return		返回结果不为空
	 */
	public String getResSpaceType()
	{
		return JCommon.defaultIfEmpty(resSpaceType , IAuthCenterConst.sResSpaceType_default) ;
	}
	
	/**
	 * 
	 * 构建一个缺省全局空间的角色
	 * 
	 * @param aName
	 * @param aDescription
	 * @return
	 */
	public static ARole ofDefaultGlobalRespace(String aName , String aDescription)
	{
		return new ARole(aName, aDescription, IAuthCenterConst.sResSpaceType_default) ;
	}
	
	/**
	 * 
	 * 构建一个适用于指定资源空间类型的角色
	 * 
	 * @param aName
	 * @param aDescription
	 * @param aResSpaceType
	 * @return
	 */
	public static ARole ofRespace(String aName , String aDescription , String aResSpaceType)
	{
		return new ARole(aName, aDescription, aResSpaceType) ;
	}
}
