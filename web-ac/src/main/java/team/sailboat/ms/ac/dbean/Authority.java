package team.sailboat.ms.ac.dbean ;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import team.sailboat.commons.fan.dpa.anno.BColumn;
import team.sailboat.commons.fan.dpa.anno.BDataType;
import team.sailboat.commons.fan.dpa.anno.BFeature;
import team.sailboat.commons.fan.dpa.anno.BTable;
import team.sailboat.commons.fan.dtool.DBType;
import team.sailboat.commons.fan.dtool.mysql.MySQLFeatures;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.dplug.anno.DBean;

/**
 * 
 * 权限数据
 *
 * @author yyl
 * @since 2024年10月30日
 */
@BTable(name="ac_authority" , comment="权限"  , features = {
		@BFeature(type = DBType.MySQL , name = MySQLFeatures.TABLE__ENGINE , value = "InnoDB") , 
		@BFeature(type = DBType.MySQL , name = MySQLFeatures.TABLE__CHARACTER_SET , value = "utf8")} ,
	id_category = "Authority" , id_prefix = "au"
)
@DBean(genBean = true , recordCreate = true , recordEdit = true)
public class Authority
{	
	@BColumn(name="id" , dataType = @BDataType(name="string" , length = 32) , comment="唯一性标识" , seq = 0 , primary = true)
	String id ;
	
	@BColumn(name="code" , dataType = @BDataType(name="string" , length = 64) , comment="权限代码" , seq = 1)
	String code ;

	@BColumn(name="description" , dataType = @BDataType(name="string" , length = 256) , comment="描述" , seq = 2)
	String description ;
	
	@BColumn(name="res_space_type" , dataType = @BDataType(name="string" , length = 32) , comment="适用的资源空间类型" , seq = 3)
	String resSpaceType ;
	
	@BColumn(name="custom_description" , dataType = @BDataType(name="string" , length = 256) , comment="描述，人工设置的描述信息" , seq = 4)
	String customDescription ;
	
	@BColumn(name="group_name" , dataType = @BDataType(name="string" , length = 32) , comment="权限分组名称" , seq = 5)
	String groupName ;
	
	@BColumn(name="ext_attributes" , dataType = @BDataType(name="string" , length = 2048) , comment="附加信息" , seq = 6)
	String extAttributes ;
	
	@BColumn(name="client_app_id" , dataType = @BDataType(name="string" , length = 32) , comment="ClientApp的id" , seq = 7)
	String clientAppId ;
	
	
	public SimpleGrantedAuthority toSimple(String aResSpaceId)
	{
		return toSimple(code , aResSpaceId) ;
	}
	
	public static SimpleGrantedAuthority toSimple_defaultGlobal(String aCode)
	{
		return new SimpleGrantedAuthority(toFullCode(aCode, null)) ;
	}
	
	public static SimpleGrantedAuthority toSimple(String aCode , String aResSpaceId)
	{
		return new SimpleGrantedAuthority(toFullCode(aCode, aResSpaceId)) ;
	}
	
	public static String toFullCode(String aCode , String aResSpaceId)
	{
		if(XString.isEmpty(aResSpaceId) || ResSpace.isDefaultGlobal(aResSpaceId)
				|| !aCode.endsWith(":"))
			return aCode ;
		else
			return aCode + ResSpace.getResIdFrom(aResSpaceId) ;
	}
}
