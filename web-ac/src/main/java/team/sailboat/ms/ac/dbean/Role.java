package team.sailboat.ms.ac.dbean;

import jakarta.validation.constraints.NotBlank;
import team.sailboat.commons.fan.dpa.anno.BColumn;
import team.sailboat.commons.fan.dpa.anno.BDataType;
import team.sailboat.commons.fan.dpa.anno.BFeature;
import team.sailboat.commons.fan.dpa.anno.BTable;
import team.sailboat.commons.fan.dtool.DBType;
import team.sailboat.commons.fan.dtool.mysql.MySQLFeatures;
import team.sailboat.dplug.anno.DBean;

/**
 * 
 * 角色表
 *
 * @author yyl
 * @since 2024年10月30日
 */
@BTable(name="ac_role" , comment="角色"  , features = {
		@BFeature(type = DBType.MySQL , name = MySQLFeatures.TABLE__ENGINE , value = "InnoDB") , 
		@BFeature(type = DBType.MySQL , name = MySQLFeatures.TABLE__CHARACTER_SET , value = "utf8")} ,
	id_category = "Role" , id_prefix = "ro"
)
@DBean(genBean = true , recordCreate = true , recordEdit = true)
public class Role
{
	
	@BColumn(name="id" , dataType = @BDataType(name="string" , length = 32) , comment="唯一性标识" , seq = 0 , primary = true)
	String id ;
	
	@NotBlank
	@BColumn(name="name" , dataType = @BDataType(name="string" , length = 64) , comment="角色名" , seq = 1)
	String name ;

	@BColumn(name="description" , dataType = @BDataType(name="string" , length = 256) , comment="描述，app自动声明" , seq = 2)
	String description ;
	
	@BColumn(name="custom_description" , dataType = @BDataType(name="string" , length = 256) , comment="描述，人手动添加的描述" , seq = 3)
	String customDescription ;
	
	@NotBlank
	@BColumn(name="client_app_id" , dataType = @BDataType(name="string" , length = 32) , comment="ClientApp的id" , seq = 5)
	String clientAppId ;
	
	@NotBlank
	@BColumn(name="res_space_type" , dataType = @BDataType(name="string" , length = 32) , comment="资源空间类型" , seq = 11)
	String resSpaceType ;
	
	@BColumn(name="ext_attributes" , dataType = @BDataType(name="string" , length = 2048) , comment="附加信息" , seq = 100)
	String extAttributes ;
}
