package team.sailboat.ms.crane.bean;

import java.util.LinkedHashSet;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 步骤、程式、过程
 *
 * @author yyl
 * @since 2024年9月13日
 */
@Data
@Schema(description = "过程")
public class Procedure
{	
	@Schema(description = "描述。有的命令描述信息过长，可以在外部用.md或.html文件存储，通过@a.md来引用")
	String description ;
	
	@Schema(description = "操作")
	List<Operation> operations ;
	
	@Schema(description = "相关模块。将通过相关模块集规划，确定该在哪些机器上操作。“ALL”表示所有模块")
	LinkedHashSet<String> modules ;
	
	@Schema(description = "分类目录")
	String catalog ;
	
	@Schema(description = "程式过程名")
	String name ;
}
