package team.sailboat.ms.ac.dbean;

import team.sailboat.commons.fan.dpa.anno.BColumn;
import team.sailboat.commons.fan.dpa.anno.BDataType;
import team.sailboat.commons.fan.dpa.anno.BFeature;
import team.sailboat.commons.fan.dpa.anno.BTable;
import team.sailboat.commons.fan.dtool.DBType;
import team.sailboat.commons.fan.dtool.mysql.MySQLFeatures;
import team.sailboat.dplug.anno.DBean;

/**
 * 组织单元和用户的关联表
 *
 * @author yyl
 * @since 2024年10月30日
 */
@BTable(name="ac_r_org_user" , comment="组织-用户表"  , features = {
		@BFeature(type = DBType.MySQL , name = MySQLFeatures.TABLE__ENGINE , value = "InnoDB") , 
		@BFeature(type = DBType.MySQL , name = MySQLFeatures.TABLE__CHARACTER_SET , value = "utf8")} ,
	id_category = "R_Organization_User" , id_prefix = "rou"
)
@DBean(recordCreate = true)
public class R_OrgUnit_User
{
	@BColumn(name="id" , dataType = @BDataType(name="string" , length = 32) , comment="唯一性标识" , seq = 0 , primary = true)
	String id ;
	
	@BColumn(name="org_unit_id" , dataType = @BDataType(name="string" , length = 32) , comment="组织id" , seq = 1)
	String orgUnitId ;

	@BColumn(name="user_id" , dataType = @BDataType(name="string" , length = 32) , comment="用户id" , seq = 2)
	String userId ;
	
	@BColumn(name="job" , dataType = @BDataType(name="string" , length = 64) , comment="职位。用户在此组织单元中的职位" , seq = 3)
	String job ;
}
