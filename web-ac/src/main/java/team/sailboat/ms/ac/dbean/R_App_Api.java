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
 * ClientApp和API的关联表		<br />
 * 
 * 表示这个ClientApp可以调用这个API
 * 
 *
 * @author yyl
 * @since 2024年10月30日
 */
@BTable(name="ac_r_app_api" , comment="ClientApp-API表"  , features = {
		@BFeature(type = DBType.MySQL , name = MySQLFeatures.TABLE__ENGINE , value = "InnoDB") , 
		@BFeature(type = DBType.MySQL , name = MySQLFeatures.TABLE__CHARACTER_SET , value = "utf8")} ,
	id_category = "R_App_Api" , id_prefix = "raapi"
)
@DBean(recordCreate = true)
public class R_App_Api
{
	@BColumn(name="id" , dataType = @BDataType(name="string" , length = 32) , comment="唯一性标识" , seq = 0 , primary = true)
	String id ;
	
	@BColumn(name="client_app_id" , dataType = @BDataType(name="string" , length = 32) , comment="ClientApp的id" , seq = 1)
	String clientAppId ;
	
	@BColumn(name="api_id" , dataType = @BDataType(name="string" , length = 32) , comment="API的id" , seq = 2)
	String apiId ;
}
