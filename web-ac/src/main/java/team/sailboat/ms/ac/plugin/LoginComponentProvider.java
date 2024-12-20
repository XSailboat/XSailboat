package team.sailboat.ms.ac.plugin;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.ms.ACKeys_Common;

public class LoginComponentProvider
{
	
	public void injectLoginFilters(HttpSecurity aSecurity , String aDefaultSuccessUrl)
	{
		ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) AppContext.get(ACKeys_Common.sSpringAppContext) ;
        // 获取bean工厂并转换为DefaultListableBeanFactory
        DefaultListableBeanFactory beanFac = (DefaultListableBeanFactory) ctx.getBeanFactory();
        String[] names = beanFac.getBeanNamesForType(ILoginComponent.class) ;
        if(XC.isNotEmpty(names))
        {
        	for(String name : names)
        	{
        		((ILoginComponent)beanFac.getBean(name)).injectFilter(aSecurity , aDefaultSuccessUrl) ;
        	}
        }
	}
}
