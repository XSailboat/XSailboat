package team.sailboat.commons.ms.json ;

import java.io.IOException;

import org.springframework.boot.jackson.JsonComponent;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;

import jakarta.annotation.PostConstruct;
import team.sailboat.commons.fan.collection.IMultiMap;
import team.sailboat.commons.fan.json.JSONObject;

@JsonComponent(type = IMultiMap.class)
public class MultiMapSerializer extends JsonSerializer<IMultiMap>
{

	public MultiMapSerializer()
	{	
	}
	
	@PostConstruct
	void _init()
	{
		Apply.inject(this) ;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Class<IMultiMap> handledType()
	{
		return IMultiMap.class ;
	}
	
	private static class Apply extends BeanSerializerFactory
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		protected Apply(SerializerFactoryConfig aConfig)
		{
			super(aConfig);
		}
		
		public static void inject(MultiMapSerializer aSer)
		{
			_concrete.put(aSer.handledType().getName()  , aSer) ;
		}
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public void serialize(IMultiMap aValue, JsonGenerator aGen, SerializerProvider aSerializers)
			throws IOException
	{
		aGen.writeRawValue(new JSONObject(aValue).toJSONString()) ; 
	}
	
}
