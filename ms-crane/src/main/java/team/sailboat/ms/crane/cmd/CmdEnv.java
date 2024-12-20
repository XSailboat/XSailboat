package team.sailboat.ms.crane.cmd;

import java.io.File;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.Data;
import team.sailboat.ms.crane.bean.HostProfile;

/**
 * 
 * 命令的执行环境
 *
 * @author yyl
 * @since 2024年10月19日
 */
@Data
public class CmdEnv
{
	/**
	 * 程序包文件获取器
	 */
	Function<String, File> appPkgFileGetter ;
	
	/**
	 * 所有主机信息的获取器
	 */
	Supplier<Collection<HostProfile>> allHostSupplier ;
	
	/**
	 * 所有模块信息的获取器
	 */
	Supplier<Collection<team.sailboat.ms.crane.bean.Module>> allModuleSupplier ;
}
