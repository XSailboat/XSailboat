package team.sailboat.commons.ms.custom ;

import java.io.IOException;

import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import team.sailboat.commons.fan.collection.PropertiesEx;
import team.sailboat.commons.fan.lang.JCommon;

public class PropertiesExSourceFactory implements PropertySourceFactory
{
	PropertiesExSource mSource ;
	
	public PropertiesExSourceFactory()
	{
		
	}

	@Override
	public PropertySource<?> createPropertySource(String aName, EncodedResource aResource) throws IOException
	{
		if(mSource == null)
			mSource = new PropertiesExSource(aName, PropertiesEx.loadFromReader(aResource.getReader())) ;
		else
			mSource.getSource().load(aResource.getReader()) ;
		return mSource ;
	}
	
	static class  PropertiesExSource  extends EnumerablePropertySource<PropertiesEx>
	{

		public PropertiesExSource(String name, PropertiesEx source) 
		{
			super(name , source) ;
		}

		@Override
		public Object getProperty(String aName)
		{
			return getSource().getProperty(aName) ;
		}
		
		@Override
		public String[] getPropertyNames()
		{
			return getSource().stringPropertyNames().toArray(JCommon.sEmptyStringArray) ;
		}
		
	}

}
