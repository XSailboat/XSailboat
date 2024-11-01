package team.sailboat.commons.ms.infc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;
import team.sailboat.commons.fan.text.XString;

@Data
public abstract class UserSupport implements IUserSupport , ToJSONObject
{
	
	@Schema(description = "创建者id")
	protected String createUserId ;
	
	@Schema(description = "创建者显示名")
	protected String createUserDisplayName ; 
	
	@Schema(description = "最近修改者的用户id")
	protected String lastEditUserId ;
	
	@Schema(description = "最近修改者的显示名")
	protected String lastEditUserDisplayName ;
	
	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		return aJSONObj.put("createUserId" , createUserId)
				.putIf(XString.isNotEmpty(createUserDisplayName) , "createUserDisplayName" , createUserDisplayName)
				.put("lastEditUserId" , lastEditUserId)
				.putIf(XString.isNotEmpty(lastEditUserDisplayName) ,  "lastEditUserDisplayName" , lastEditUserDisplayName)
				;
	}
}
