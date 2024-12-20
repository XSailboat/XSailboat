package team.sailboat.ms.ac;

import team.sailboat.ms.starter.IMSLauncher;

/**
 * 
 * 启动入口
 *
 * @author yyl
 * @since 2024年10月10日
 */
public class MSLauncher implements IMSLauncher
{
	
	public MSLauncher()
	{
	}

	@Override
	public void start(String[] aArgs)
	{
		System.out.println("类加载器是："+getClass().getClassLoader().getClass().getName());
		MainApplication.main(aArgs) ;
	}

}
