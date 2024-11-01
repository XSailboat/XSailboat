package team.sailboat.ms.crane.bean;

import java.util.Collection;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.ms.jackson.TLCustmFilter;

/**
 * 
 * 主机信息
 *
 * @author yyl
 * @since 2024年10月11日
 */
@Data
@Schema(description = "主机信息")
public class HostProfile
{
	@NotBlank
	@Schema(description = "IP地址")
	String ip ;
	
	@JsonInclude(value = Include.CUSTOM , valueFilter = TLCustmFilter.class)
	@NotBlank
	@Schema(description = "主机名称")
	String name ;
	
	@Schema(description = "管理员用户名")
	String adminUser ;
	
	@Schema(description = "管理员密码")
	String adminPswd ;
	
	@NotBlank
	@Schema(description = "系统用户名")
	String sysUser ;
	
	@NotBlank
	@Schema(description = "系统用户密码")
	String sysPswd ;
	
	@Schema(description = "部署的应用。应用模块名，它不是安装包名")
	TreeSet<String> deployModuleNames ;
	
	@Schema(description = "代理安装工具的服务端口。缺省是12205")
	Integer sailPyInstallerPort ;
	
	@Schema(description = "序号。在需要在平台内用整数表示机器的时候使用")
	Integer seq ;
	
	public int getSailPyInstallerPort()
	{
		return sailPyInstallerPort == null?12205:sailPyInstallerPort ;
	}
	
	public static String all_ip_host(Collection<HostProfile> aHosts)
	{
		if(aHosts == null)
			return "" ;
		StringBuilder strBld = new StringBuilder() ;
		for(HostProfile host : aHosts)
		{
			if(strBld.length() > 0)
				strBld.append(' ') ;
			strBld.append(host.getIp()).append(' ').append(host.getName()) ;
		}
		return strBld.toString() ;
	}
	
	/**
	 * 移除指定名称的部署模块
	 * @param aModuleName
	 * @return
	 */
	public boolean removeDeployModuleName(String aModuleName)
	{
		if(XC.isNotEmpty(deployModuleNames))
		{
			return deployModuleNames.remove(aModuleName) ;
		}
		return false ;
	}
}
