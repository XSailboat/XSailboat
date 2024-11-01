package team.sailboat.commons.fan.dtool.h2;

import java.util.List;

import team.sailboat.commons.fan.dtool.ColumnSchema;
import team.sailboat.commons.fan.dtool.DataType;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.text.XString;

public class H2ColumnSchema extends ColumnSchema implements H2Const , H2Features
{	
	String mColumnKey ;
	
	public H2ColumnSchema()
	{
		super() ;
	}
	
	public H2ColumnSchema(String aColName)
	{
		super(aColName) ;
	}
	
	public void setCollation(String aCollation)
	{
		putOtherProperty(COLUMN__COLLATION , aCollation) ;
	}
	
	public String getOnUpdate()
	{
		return (String)getOtherProperty(COLUMN__ON_UPDATE) ;
	}
	
	public void setOnUpdate(String aTimeStamp)
	{
		putOtherProperty(COLUMN__ON_UPDATE , aTimeStamp) ;
	}
	
	public String getColumnKey()
	{
		return mColumnKey ;
	}
	
	@Override
	public DataType getDataType0()
	{
		return H2DataType.valueOf(getDataType().toUpperCase()) ;
	}
	
	@Override
	public void putOtherProperty(String aKey, Object aVal)
	{
		if("COLUMN_TYPE".equals(aKey))
			mDisplayDataType = JCommon.toString(aVal) ;
		else if("IS_NULLABLE".equals(aKey))
		{
			setNullable(!"NO".equalsIgnoreCase(JCommon.toString(aVal)));
		}
		else if("ORDINAL_POSITION".equals(aKey))
			mOriginalSeq = XClassUtil.toInteger(aVal, 0) ;
		else if("COLUMN_KEY".equals(aKey))
			mColumnKey = JCommon.toString(aVal) ;
		else if("COLUMN_DEFAULT".equals(aKey))
			setDataDefault(aVal) ;
		else
			super.putOtherProperty(aKey, aVal);
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
		else if(sOneParamDataTypeSet_flex.contains(dataType))
		{
			Integer len = getDataLength() ;
			if(len == null)
				colBld.append(" ").append(dataType) ;
			else
				colBld.append(String.format(" %1$s(%2$d)", dataType , len.intValue())) ;
		}
		else if(sTwoParamsDataTypeSet_flex.contains(getColumnName()))
		{
			Integer len = getDataLength() ;
			if(len == null)
				colBld.append(" ").append(dataType) ;
			else
			{
				Integer precision = getDataPrecision() ;
				if(precision == null)
					colBld.append(String.format(" %1$s(%2$d)", dataType , len.intValue())) ;
				else
					colBld.append(String.format(" %1$s(%2$d , %3$d)", dataType
							, getDataLength() , getDataPrecision())) ;
			}
			
		}
		else
		{
			colBld.append(' ').append(dataType) ;
		}
		
		mOtherProps.forEach((key , val)->{
			if(sPV_Append_Directly.equals(val))
				colBld.append(" ").append(key) ;
		});
		
		if(!isNullable(true))
		{
			colBld.append(" NOT NULL") ;
		}
		
		Object dataDefault = getDataDefault() ;
		if(dataDefault != null)
		{
			String defaultText = null ;
			boolean needComma = false ;
			if(dataDefault instanceof String)
			{
				defaultText = (String)dataDefault ;
				needComma = !(sDataType_TIMESTAMP.equalsIgnoreCase(dataType) 
						&& defaultText.toUpperCase().contains("_TIMESTAMP")) ;
			}
			else if(dataDefault instanceof Integer || dataDefault instanceof Double)
			{
				needComma =false ;
				defaultText = JCommon.toString(dataDefault) ;
			}
			else
				throw new IllegalStateException("") ;
			if(needComma)
				colBld.append(" DEFAULT '").append(dataDefault).append('\'') ;
			else
				colBld.append(" DEFAULT ").append(dataDefault) ;
 		}
		
		String onupdate = getOnUpdate() ;
		if(XString.isNotEmpty(onupdate))
		{
			boolean needComma = !onupdate.toUpperCase().contains("_TIMESTAMP") ;
			if(needComma)
				colBld.append(" ON UPDATE '").append(onupdate).append('\'') ;
			else
				colBld.append(" ON UPDATE ").append(onupdate) ;
		}
		
		String comment = getComment() ;
		if(XString.isNotEmpty(comment))
		{
			colBld.append(" COMMENT '").append(comment).append('\'') ;
		}
		return colBld.toString() ;
	}
	
	@Override
	public List<String> getAddFieldSql(String aTableName)
	{
		return null;
	}
	
	@Override
	protected void initClone(ColumnSchema aClone)
	{
		super.initClone(aClone);
		((H2ColumnSchema)aClone).mColumnKey = mColumnKey ;
	}
	
	@Override
	public H2ColumnSchema clone()
	{
		H2ColumnSchema clone = new H2ColumnSchema() ;
		initClone(clone) ;
		return clone ;
	}
	
	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		JSONObject jobj = super.setTo(aJSONObj);
		jobj.put("columnKey", mColumnKey) ;
		return jobj ;
	}
}
