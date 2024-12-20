package team.sailboat.commons.ms.bean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import team.sailboat.commons.fan.json.JSONObject;

/**
 * 
 * 用户的简要信息。		<br />
 * 
 * 包括用户的id，姓名和所属部门
 *
 * @author yyl
 * @since 2024年11月6日
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "UserBrief" , description = "大数据平台的用户")
public class UserBrief implements ISwaggerJBean
{
	@Schema(description = "用户id")
	String id ;
	
	@Schema(description = "真实姓名")
	String realName ;
	
	@Schema(description = "所属部门名称")
	String department ;
	
	public static UserBrief of(JSONObject aJo)
	{
		UserBrief user = new UserBrief() ;
		user.setId(aJo.optString("id")) ;
		user.setRealName(aJo.optString("realName")) ;
		user.setDepartment(aJo.optString("department")) ;
		return user ;
	}
}
