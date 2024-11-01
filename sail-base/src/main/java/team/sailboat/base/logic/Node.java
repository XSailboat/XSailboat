package team.sailboat.base.logic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonTypeInfo(
	    use = JsonTypeInfo.Id.NAME, // Were binding by providing a name
	    include = JsonTypeInfo.As.PROPERTY, // The name is provided in a property
	    property = "type", // Property name is type
	    visible = true // Retain the value of type after deserialisation
	)
	@JsonSubTypes({//Below, we define the names and the binding classes.
	    @JsonSubTypes.Type(value = LogicJoints.class, name = "LogicJoints") ,
	    @JsonSubTypes.Type(value = IS_NULL.class, name = "IS_NULL") ,
	    @JsonSubTypes.Type(value = NOT_NULL.class, name = "NOT_NULL") ,
	    @JsonSubTypes.Type(value = EMPTY.class, name = "EMPTY") ,
	    @JsonSubTypes.Type(value = NOT_EMPTY.class, name = "NOT_EMPTY") ,
	    @JsonSubTypes.Type(value = EQUALS.class, name = "EQUALS") ,
	    @JsonSubTypes.Type(value = NOT_EQUALS.class, name = "NOT_EQUALS") ,
	    @JsonSubTypes.Type(value = IN.class, name = "IN") ,
	    @JsonSubTypes.Type(value = NOT_IN.class, name = "NOT_IN") ,
	    @JsonSubTypes.Type(value = CONTAINS.class, name = "CONTAINS") ,
	    @JsonSubTypes.Type(value = NOT_CONTAINS.class, name = "NOT_CONTAINS") ,
	    @JsonSubTypes.Type(value = STARTS_WITH.class, name = "STARTS_WITH") ,
	    @JsonSubTypes.Type(value = NOT_STARTS_WITH.class, name = "NOT_STARTS_WITH") ,
	    @JsonSubTypes.Type(value = ENDS_WITH.class, name = "ENDS_WITH") ,
	    @JsonSubTypes.Type(value = NOT_ENDS_WITH.class, name = "NOT_ENDS_WITH") ,
	    @JsonSubTypes.Type(value = IN_RANGE.class, name = "IN_RANGE")
	})
@Schema(name = "Node" , description="数据源连接信息，这是一个抽象基类，注意根据数据库类型选择" 
	, subTypes = {LogicJoints.class , IS_NULL.class , NOT_NULL.class , EMPTY.class , NOT_EMPTY.class
			, EQUALS.class , NOT_EQUALS.class , IN.class, NOT_IN.class , CONTAINS.class , NOT_CONTAINS.class
			, STARTS_WITH.class , NOT_STARTS_WITH.class , ENDS_WITH.class , NOT_ENDS_WITH.class
			, IN_RANGE.class})
public abstract class Node implements INode<Node>
{
	@JsonIgnore
	String type ;
	
	LogicJoints mParent ;
	
	public Node()
	{
		type = getClass().getSimpleName() ;
	}
	
	@Schema(description = "节点类型" , allowableValues = {"LogicJoints","IS_NULL","NOT_NULL","EMPTY","NOT_EMPTY"
			,"EQUALS","NOT_EQUALS","IN","NO_IN","CONTAINS","NOT_CONTAINS","STARTS_WITH","NOT_STARTS_WITH"
			,"ENDS_WITH","NOT_ENDS_WITH","IN_RANGE"})
	public String getType()
	{
		return type;
	}
	public void setType(String aType)
	{
		type = aType;
	}
	
	@Schema(hidden = true)
	@JsonIgnore
	@Override
	public LogicJoints getParent()
	{
		return mParent ;
	}
	@Override
	public void setParent(ILogicJoints<Node> aParent)
	{
		mParent = (LogicJoints)aParent;
	}
	
}
