package team.sailboat.bd.base.bean;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import team.sailboat.bd.base.model.Content;
import team.sailboat.bd.base.model.IFlowNode;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.text.XString;

@Schema(description="节点的部分信息")
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NodeCorner
{
	@Schema(description = "节点id")
	String id ;
	
	@Schema(description = "节点名称")
	String name ;
	
	@Schema(description = "输出表名，多个表名之间用“,”分隔")
	String outputTableNames ;
	
	public static NodeCorner of(IFlowNode aNode)
	{
		NodeCorner corner = new NodeCorner() ;
		corner.setId(aNode.getId()) ;
		corner.setName(aNode.getName()) ;
		Content content = aNode.getContent() ;
		if(content != null)
		{
			Set<String> createTableNames = content.getCreateTableNames() ;
			if(XC.isNotEmpty(createTableNames))
				corner.setOutputTableNames(XString.toString(",", createTableNames)) ;
		}
		return corner ;
	}
}
