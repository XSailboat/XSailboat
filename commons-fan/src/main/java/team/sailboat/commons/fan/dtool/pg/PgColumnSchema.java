package team.sailboat.commons.fan.dtool.pg;

import java.util.List;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dtool.ColumnSchema;
import team.sailboat.commons.fan.dtool.DataType;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.text.XString;

/**
 * Pg库的列定义
 *
 * @author yyl
 * @since 2024年10月30日
 */
public class PgColumnSchema extends ColumnSchema implements PgConst
{	
	String mColumnKey ;
	
	/**
	 * 是否自动更新时间。前提是这个字段是timestamp类型的
	 */
	boolean mAutoUpdateTimestamp = false ;
	
	public PgColumnSchema()
	{
		super() ;
	}
	
	public PgColumnSchema(String aColName)
	{
		super(aColName) ;
	}
	
	@Override
	public DataType getDataType0()
	{
		return null;
	}
	
	public String getCharacterSet()
	{
//		return (String)getOtherProperty(COLUMN__CHARSET) ;
		throw new IllegalStateException("未实现!") ;
	}
	
	public void setCharacterSet(String aCharacterSet)
	{
//		putOtherProperty(COLUMN__CHARSET  , aCharacterSet) ;
		throw new IllegalStateException("未实现!") ;
	}
	
	public String getCollation()
	{
//		return (String)getOtherProperty(COLUMN__COLLATION) ;
		throw new IllegalStateException("未实现!") ;
	}
	
	public void setCollation(String aCollation)
	{
//		putOtherProperty(COLUMN__COLLATION , aCollation) ;
		throw new IllegalStateException("未实现!") ;
	}
	
	public String getOnUpdate()
	{
//		return (String)getOtherProperty(COLUMN__ON_UPDATE) ;
		throw new IllegalStateException("未实现!") ;
	}
	
	public void setOnUpdate(String aTimeStamp)
	{
		if(sCOL_FEATURE__ON_UPDATE__VAL__CURRENT_TIMESTAMP.equals(aTimeStamp))
		{
			mAutoUpdateTimestamp = true ;
		}
		else
		{
			throw new IllegalStateException("未实现!") ;
		}
	}
	
	public boolean isAutoUpdateTimestamp()
	{
		return mAutoUpdateTimestamp;
	}
	
	public String getColumnKey()
	{
		return mColumnKey ;
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
		
		return colBld.toString() ;
	}
	
	@Override
	public List<String> getAddFieldSql(String aTableName)
	{
		List<String> sqls = XC.arrayList() ;
		sqls.add(XString.msgFmt("ALTER TABLE {} ADD {}"  , aTableName , getSqlText())) ;
		String comment = getComment() ;
		if(XString.isNotEmpty(comment))
		{
			sqls.add(XString.msgFmt("COMMENT ON COLUMN {}.{} IS '{}'" , aTableName , getColumnName()
					, comment.replace("'", "''"))) ;
		}
		return sqls ;
	}
	
	@Override
	protected void initClone(ColumnSchema aClone)
	{
		super.initClone(aClone);
		((PgColumnSchema)aClone).mColumnKey = mColumnKey ;
		((PgColumnSchema)aClone).mAutoUpdateTimestamp = mAutoUpdateTimestamp ;
	}
	
	@Override
	public PgColumnSchema clone()
	{
		PgColumnSchema clone = new PgColumnSchema() ;
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
