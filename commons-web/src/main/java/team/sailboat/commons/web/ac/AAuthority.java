package team.sailboat.commons.web.ac;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import team.sailboat.commons.fan.lang.JCommon;

/**
 * 权限
 *
 * @author yyl
 * @since 2024年3月26日
 */
@Schema(description = "权限")
@Getter
@Builder
public class AAuthority
{
	@Schema(description = "权限码")
	String code ;
	
	@Schema(description = "权限描述")
	String description ;
	
	@Schema(description = "权限分组")
	String groupName ;
	
	@Schema(description = "资源空间类型")
	String resSpaceType ;
	
	/**
	 * 
	 * @return		返回结果不为空
	 */
	public String getResSpaceType()
	{
		return JCommon.defaultIfEmpty(resSpaceType , IAuthCenterConst.sResSpaceType_default) ;
	}
	
	public static AAuthority ofDefaultGlobalRespace(String aCode , String aDescription , String aGroupName)
	{
		return new AAuthority(aCode , aDescription , aGroupName , IAuthCenterConst.sResSpaceType_default) ;
	}
	
	public static AAuthority of(String aCode , String aDescription , String aGroupName
			, String aResSpaceType)
	{
		return new AAuthority(aCode , aDescription , aGroupName  , aResSpaceType) ;
	}
	
}
