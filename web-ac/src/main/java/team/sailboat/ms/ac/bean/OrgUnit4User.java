package team.sailboat.ms.ac.bean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户的组织信息")
public class OrgUnit4User
{
	@Schema(description = "组织单元id")
	String orgUnitId ;
	
	@Schema(description = "组织单元名")
	String orgUnitName ;
	
	@Schema(description = "职位")
	String job ;
}
