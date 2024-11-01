package team.sailboat.base.schema;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class TypeSerializer extends JsonSerializer<Type>
{
	

	@Override
	public void serialize(Type aValue, JsonGenerator aGen, SerializerProvider aSerializers) throws IOException
	{
		if(aValue == null)
			aGen.writeNull() ;
		else if(aValue instanceof BaseType)
			aGen.writeString(aValue.toString()) ;
		else if(aValue instanceof ObjectType)
		{
			aGen.writeStartObject() ;
			aGen.writeStringField("type" , "object") ;
			aSerializers.defaultSerializeField("fields" , ((ObjectType)aValue).getFields() , aGen);
			aGen.writeEndObject() ;
		}
		else if(aValue instanceof ArrayType)
		{
			aGen.writeStartObject() ;
			aGen.writeStringField("type" , "array") ;
			aSerializers.defaultSerializeField("itemsType" , ((ArrayType)aValue).getItemType() , aGen);
			aGen.writeEndObject() ;
		}
	}

}
