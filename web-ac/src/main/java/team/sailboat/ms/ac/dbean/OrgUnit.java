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
 * 组织单元表
 *
 * @author yyl
 * @since 2024年10月30日
 */
@BTable(name="ac_org_unit" , comment="组织单元"  , features = {
		@BFeature(type = DBType.MySQL , name = MySQLFeatures.TABLE__ENGINE , value = "InnoDB") , 
		@BFeature(type = DBType.MySQL , name = MySQLFeatures.TABLE__CHARACTER_SET , value = "utf8")} ,
	id_category = "OrgUnit" , id_prefix = "og"
)
@DBean(genBean = true , recordCreate = true , recordEdit = true)
public class OrgUnit
{
	@BColumn(name="id" , dataType = @BDataType(name="string" , length = 32) , comment="唯一性标识" , seq = 0 , primary = true)
	String id ;
	
	@NotBlank
	@BColumn(name="name" , dataType = @BDataType(name="string" , length = 64) , comment="名称" , seq = 1)
	String name ;
	
	@BColumn(name="simple_name" , dataType = @BDataType(name="string" , length = 16) , comment="简化名" , seq = 2)
	String simpleName ;

	@BColumn(name="parent_id" , dataType = @BDataType(name="string" , length = 32) , comment="父组织id" , seq = 3)
	String parentId ;
	
	@BColumn(name="path_id" , dataType = @BDataType(name="string" , length = 512) , comment="组织id路径，用“.”分隔" , seq = 4)
	String pathId ;

	@BColumn(name="ext_attributes" , dataType = @BDataType(name="string" , length = 2048) , comment="附加信息" , seq = 8)
	String extAttributes ;
	
}
