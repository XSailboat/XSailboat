package team.sailboat.ms.crane;

import team.sailboat.ms.starter.IMSLauncher;

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
