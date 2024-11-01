package team.sailboat.base.schema;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


@JsonTypeInfo(
	    use = JsonTypeInfo.Id.NAME, // Were binding by providing a name
	    include = JsonTypeInfo.As.PROPERTY, // The name is provided in a property
	    property = "type", // Property name is type
	    visible = true // Retain the value of type after deserialisation
	    , defaultImpl = BaseType.class
	)
@JsonSubTypes({//Below, we define the names and the binding classes.
    @JsonSubTypes.Type(value = ArrayType.class, name = "array") ,
    @JsonSubTypes.Type(value = ObjectType.class, name = "object")
})
public interface Type
{

}
