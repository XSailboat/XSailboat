package team.sailboat.ms.crane.bean;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "应用软件包")
public class AppPkg
{
	@Schema(description = "应用程序名。是应用的唯一标识。他可以是带版本的，带扩展名")
	String name ;
	
	@Schema(description = "文件大小。单位字节")
	Long fileLen ;
	
	/**
	 * 程序包的更新时间，用文件的修改时间
	 */
	@Schema(description = "创建时间")
	Date updateTime ;
}
