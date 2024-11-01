package team.sailboat.ms.crane.cmd;

import java.io.File;
import java.util.function.Function;

import lombok.Data;

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
}
