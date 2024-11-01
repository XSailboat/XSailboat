package team.sailboat.commons.fan.dtool.dm;

import java.util.List;

import team.sailboat.commons.fan.dtool.ColumnSchema;
import team.sailboat.commons.fan.dtool.DataType;

public class DMColumnSchema extends ColumnSchema
{
	
	public DMColumnSchema()
	{
		super() ;
	}
	
	public DMColumnSchema(String aName)
	{
		super(aName) ;
	}

	@Override
	public String getSqlText()
	{
		return null;
	}
	
	@Override
	public List<String> getAddFieldSql(String aTableName)
	{
		return null;
	}

	@Override
	public DMColumnSchema clone()
	{
		return null;
	}
	
	@Override
	public DataType getDataType0()
	{
		throw new IllegalStateException("未实现！") ;
	}

}
