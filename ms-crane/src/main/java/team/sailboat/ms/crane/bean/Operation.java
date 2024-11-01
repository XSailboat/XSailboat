package team.sailboat.ms.crane.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.ms.jackson.TLCustmFilter;
import team.sailboat.ms.crane.cmd.LocalCmds;

/**
 * 操作
 *
 * @author yyl
 * @since 2024年9月13日
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "操作")
public class Operation
{
	@Schema(description = "操作名")
	String name ;
	
	@Schema(description = "描述信息")
	String description ;
	
	@Schema(description = "操作的命令列表")
	List<String> commands ;
	
	@Schema(description = "是否可用")
	boolean enabled ;
	
	@JsonInclude(value = Include.CUSTOM , valueFilter = TLCustmFilter.class)
	@Schema(description = "是否是本地执行一次的命令")
	public boolean isLocalOne()
	{
		if(commands == null || commands.isEmpty())
			return false ;
		if(commands.size() == 1)
		{
			return LocalCmds.isLocalOne(commands.get(0)) ;
		}
		else if(commands.size() > 1)
		{
			// 检查下面有没有LocalOne命令，有的话得抛出异常
			for(String cmd : commands)
			{
				Assert.isNotTrue(LocalCmds.isLocalOne(cmd) , "操作[%s]中有多个命令，其中包含了本地执行一次(LocalOne)命令[%s]，不合法！"
						, name , cmd) ;
			}
		}
		return false ;
	}
}
