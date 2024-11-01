package team.sailboat.commons.fan.dtool.hive;

import java.util.List;

import team.sailboat.commons.fan.dtool.ColumnSchema;
import team.sailboat.commons.fan.dtool.DataType;

public class HiveColumnSchema extends ColumnSchema
{
	
	public HiveColumnSchema()
	{
	}
	
	@Override
	public DataType getDataType0()
	{
		return HiveDataType.valueOf(getDataType().toUpperCase()) ;
	}
	
	public HiveColumnSchema(String aName , String aDataType , String aComment)
	{
		super(aName, aDataType) ;
		setComment(aComment) ;
	}

	@Override
	public String getSqlText()
	{
		return new StringBuilder().append('`').append(mColumnName).append('`')
				.append("    ").append(mDataType).append(" COMMENT '").append(mComment).append("'")
				.toString() ;
	}
	
	@Override
	public List<String> getAddFieldSql(String aTableName)
	{
		return null;
	}

	@Override
	public HiveColumnSchema clone()
	{
		return new HiveColumnSchema(mColumnName , mDataType , mComment) ;
	}

}
