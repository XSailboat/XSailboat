package team.sailboat.commons.fan.dtool.taos;

import team.sailboat.commons.fan.dtool.TableSchema;

public class TDengineTableSchema extends TableSchema
{

	public TDengineTableSchema()
	{
		super();
	}

	public TDengineTableSchema(String aSchema, String aName)
	{
		super(aSchema, aName);
	}
	
	@Override
	protected void initClone(TableSchema aClone)
	{
		super.initClone(aClone);
	}
}
