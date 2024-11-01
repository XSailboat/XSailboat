package team.sailboat.commons.ms.security;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

public class RemoveDefaultWSEHProcessor implements BeanDefinitionRegistryPostProcessor
{

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory aBeanFactory) throws BeansException
	{
	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry aRegistry) throws BeansException
	{
		aRegistry.removeBeanDefinition("webSecurityExpressionHandler");
	}

}
