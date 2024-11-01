package team.sailboat.base.logic;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;

public abstract class Condition extends Node
{
	
	String mFieldName ;
	
	/**
	 * 操作符
	 */
	Operator mOperator ;
	
	public Condition(Operator aOperator)
	{
		super() ;
		mOperator = aOperator ;
	}
	
	@Schema(description = "字段名")
	public String getFieldName()
	{
		return mFieldName;
	}
	public void setFieldName(String aFieldName)
	{
		mFieldName = aFieldName;
	}
	
	@JsonIgnore
	@Schema(hidden = true)
	public Operator getOperator()
	{
		return mOperator;
	}
	public void setOperator(Operator aOperator)
	{
		mOperator = aOperator;
	}
	
}
