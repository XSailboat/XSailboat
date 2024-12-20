package team.sailboat.ms.ac.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import team.sailboat.commons.ms.infc.IUserSupport;
import team.sailboat.ms.ac.dbean.User;

@JsonInclude(value = Include.NON_NULL)
@Schema(description = "用户对象详细信息，用在用户管理页面中")
@Data
@EqualsAndHashCode(callSuper = true)
public class UserItem extends User.BUser implements IUserSupport
{
	
	@Schema(description = "用户的组织单元信息")
	List<OrgUnit4User> orgUnits ;
	
	@Schema(description = "创建者显示名")
	String createUserDisplayName ;
	
	@Schema(description = "最近编辑者显示名")
	String lastEditUserDisplayName ;	
}
