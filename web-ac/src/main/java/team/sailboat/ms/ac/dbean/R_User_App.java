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
 * 用户和可访问的App
 *
 * @author yyl
 * @since 2024年10月30日
 */
@BTable(name="ac_r_user_app" , comment="用户-域表，表示此用户能访问哪些应用"  , features = {
		@BFeature(type = DBType.MySQL , name = MySQLFeatures.TABLE__ENGINE , value = "InnoDB") , 
		@BFeature(type = DBType.MySQL , name = MySQLFeatures.TABLE__CHARACTER_SET , value = "utf8")} ,
	id_category = "R_User_App" , id_prefix = "rud"
)
@DBean(recordCreate = true)
public class R_User_App
{
	public static final String sType_owner = "主属" ;
	
	@BColumn(name="id" , dataType = @BDataType(name="string" , length = 32) , comment="唯一性标识" , seq = 0
			, primary = true)
	String id ;

	@BColumn(name="user_id" , dataType = @BDataType(name="string" , length = 32) , comment="用户id" , seq = 1)
	String userId ;
	
	@BColumn(name="client_app_id" , dataType = @BDataType(name="string" , length = 32) , comment="ClientApp的id" , seq = 2)
	String clientAppId ;
	
	@BColumn(name="type" , dataType = @BDataType(name="string" , length = 32) , comment="关系类型。取值：主属" , seq = 3)
	String type ;
	
	@BColumn(name="authorized_scopes" , dataType = @BDataType(name="string" , length = 256) , comment="用户已经授权应用获取它的信息范围" , seq = 4)
	String[] authorizedScopes ;
	
	/**
	 * 是不是主属关系
	 * @return
	 */
	public boolean isOwner()
	{
		return sType_owner.equals(type) ;
	}
}
