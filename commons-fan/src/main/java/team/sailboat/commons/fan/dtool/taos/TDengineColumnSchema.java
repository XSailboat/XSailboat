package team.sailboat.commons.fan.dtool.taos;

import java.util.List;

import team.sailboat.commons.fan.dtool.ColumnSchema;
import team.sailboat.commons.fan.dtool.DataType;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;

public class TDengineColumnSchema extends ColumnSchema implements TDengineConst , TDengineFeatures
{	

	boolean tag ;
	
	
	public TDengineColumnSchema()
	{
		super() ;
	}
	
	public TDengineColumnSchema(String aColName , boolean aTag)
	{
		super(aColName) ;
	}
	
	public boolean isTag()
	{
		return tag;
	}
	public void setTag(boolean aTag)
	{
		tag = aTag;
	}
	
	@Override
	public DataType getDataType0()
	{
		return TDengineDataType.valueOf(getDataType().toUpperCase()) ;
	}
	
	public boolean isInner()
	{
		return "TBNAME".equals(mColumnName) ;
	}
	
	@Override
	public String getSqlText()
	{
		StringBuilder colBld = new StringBuilder(getColumnName()) ;
		
		String dataType = getDataType() ;
		
		if(sOneParamDataTypeSet_fix.contains(dataType))
		{
			Integer len = getDataLength() ;
			Assert.notNull(len) ;
			colBld.append(String.format(" %1$s(%2$d)", dataType , len.intValue())) ;
		}
		else
		{
			colBld.append(' ').append(dataType) ;
		}
		
		return colBld.toString() ;
	}
	
	@Override
	protected void initClone(ColumnSchema aClone)
	{
		super.initClone(aClone);
		((TDengineColumnSchema)aClone).tag = tag ;
	}
	
	@Override
	public TDengineColumnSchema clone()
	{
		TDengineColumnSchema clone = new TDengineColumnSchema() ;
		initClone(clone) ;
		return clone ;
	}
	
	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		JSONObject jobj = super.setTo(aJSONObj);
		jobj.put("tag", tag) ;
		return jobj ;
	}

	@Override
	public List<String> getAddFieldSql(String aTableName)
	{
		return null;
	}
}
