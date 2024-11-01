package team.sailboat.commons.fan.dtool.oracle;

import java.util.List;

import team.sailboat.commons.fan.dtool.ColumnSchema;
import team.sailboat.commons.fan.dtool.DataType;

public class OracleColumnSchema extends ColumnSchema
{
	
	public OracleColumnSchema()
	{
		super() ;
	}
	
	public OracleColumnSchema(String aName)
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
	public OracleColumnSchema clone()
	{
		return null;
	}
	
	@Override
	public DataType getDataType0()
	{
		throw new IllegalStateException("未实现！") ;
	}

}
