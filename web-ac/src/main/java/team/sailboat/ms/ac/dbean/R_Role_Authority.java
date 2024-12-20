package team.sailboat.ms.ac.dbean;

import team.sailboat.commons.fan.dpa.anno.BColumn;
import team.sailboat.commons.fan.dpa.anno.BDataType;
import team.sailboat.commons.fan.dpa.anno.BFeature;
import team.sailboat.commons.fan.dpa.anno.BTable;
import team.sailboat.commons.fan.dtool.DBType;
import team.sailboat.commons.fan.dtool.mysql.MySQLFeatures;
import team.sailboat.dplug.anno.DBean;

/**
 * 
 * 角色和权限的关联表
 *
 * @author yyl
 * @since 2024年10月30日
 */
@BTable(name="ac_r_role_authority" , comment="角色-权限表"  , features = {
		@BFeature(type = DBType.MySQL , name = MySQLFeatures.TABLE__ENGINE , value = "InnoDB") , 
		@BFeature(type = DBType.MySQL , name = MySQLFeatures.TABLE__CHARACTER_SET , value = "utf8")} ,
	id_category = "R_Role_Authority" , id_prefix = "rrau"
)
@DBean(recordCreate = true)
public class R_Role_Authority
{
	@BColumn(name="id" , dataType = @BDataType(name="string" , length = 32) , comment="唯一性标识" , seq = 0 , primary = true)
	String id ;
	
	@BColumn(name="role_id" , dataType = @BDataType(name="string" , length = 32) , comment="角色id" , seq = 1)
	String roleId ;
	
	@BColumn(name="authority_id" , dataType = @BDataType(name="string" , length = 32) , comment="权限id" , seq = 2)
	String authorityId ;
	
	@BColumn(name="authority_code" , dataType = @BDataType(name="string" , length = 64) , comment="权限码" , seq = 3)
	String authorityCode ;
}
