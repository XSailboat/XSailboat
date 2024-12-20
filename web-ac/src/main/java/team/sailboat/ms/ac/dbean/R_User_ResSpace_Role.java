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
 * 用户、资源空间和角色的三者关联表
 *
 * @author yyl
 * @since 2024年10月30日
 */
@BTable(name="ac_r_user_res_space_role" , comment="用户-资源空间-角色关联"  , features = {
		@BFeature(type = DBType.MySQL , name = MySQLFeatures.TABLE__ENGINE , value = "InnoDB") , 
		@BFeature(type = DBType.MySQL , name = MySQLFeatures.TABLE__CHARACTER_SET , value = "utf8")} ,
	id_category = "R_User_ResSpace_Role" , id_prefix = "rurr"
)
@DBean(recordCreate = true)
public class R_User_ResSpace_Role
{
	
	@BColumn(name="id" , dataType = @BDataType(name="string" , length = 32) , comment="唯一性标识" , seq = 0
			, primary = true)
	String id ;

	@BColumn(name="res_space_id" , dataType = @BDataType(name="string" , length = 32) , comment="资源空间id" , seq = 1)
	String resSpaceId ;
	
	@BColumn(name="role_id" , dataType = @BDataType(name="string" , length = 32) , comment="角色id" , seq = 2)
	String roleId ;
	
	@BColumn(name="user_id" , dataType = @BDataType(name="string" , length = 32) , comment="用户id" , seq = 3)
	String userId ;
}
