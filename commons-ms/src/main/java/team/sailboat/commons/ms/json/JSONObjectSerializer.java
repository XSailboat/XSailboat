package team.sailboat.commons.ms.json ;

import java.io.IOException;

import org.springframework.boot.jackson.JsonComponent;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;

import jakarta.annotation.PostConstruct;
import team.sailboat.commons.fan.json.JSONObject;

@JsonComponent(type = JSONObject.class)
public class JSONObjectSerializer extends JsonSerializer<JSONObject>
{

	public JSONObjectSerializer()
	{	
	}
	
	@PostConstruct
	void _init()
	{
		Apply.inject(this) ;
	}
	
	@Override
	public Class<JSONObject> handledType()
	{
		return JSONObject.class ;
	}
	
	@Override
	public void serialize(JSONObject aValue, JsonGenerator aGen, SerializerProvider aSerializers) throws IOException
	{
		aGen.writeRawValue(aValue.toJSONString()) ; 
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
		
		public static void inject(JSONObjectSerializer aSer)
		{
			_concrete.put(aSer.handledType().getName()  , aSer) ;
		}
		
	}
	
}
