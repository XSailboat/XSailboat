package team.sailboat.commons.fan.dtool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;

public abstract class ColumnSchema implements Cloneable , ToJSONObject
{
	
	public static final String sPV_Append_Directly = "__APPEND_DIRECTLY__" ;
	/**
	 * 序号
	 */
	protected int mOriginalSeq ;
	protected String mColumnName ;
	protected String mDataType ;
	Integer mDataLength ;
	Boolean mNullable ;
	Integer mDataPrecision ;
	Object mDataDefault ;
	protected String mComment ;
	
	protected String mDisplayDataType ;
	
	protected final Map<String , Object> mOtherProps = new HashMap<>() ;
	
	public ColumnSchema()
	{
	}
	
	public ColumnSchema(String aColumnName)
	{
		mColumnName = aColumnName ;
	}
	
	public ColumnSchema(String aColumnName , String aDataType)
	{
		mColumnName = aColumnName ;
		mDataType = aDataType ;
	}

	public String getColumnName()
	{
		return mColumnName;
	}
	
	public String getColumnNameInUpperCase()
	{
		return mColumnName.toUpperCase() ;
	}

	public void setColumnName(String aColumnName)
	{
		mColumnName = aColumnName;
	}

	public String getDataType()
	{
		return mDataType;
	}

	public void setDataType(String aDataType)
	{
		mDataType = aDataType;
	}
	
	public abstract DataType getDataType0() ;

	public Integer getDataLength()
	{
		return mDataLength;
	}

	public void setDataLength(int aDataLength)
	{
		mDataLength = aDataLength;
	}

	public boolean isNullable(boolean aDefaultVal)
	{
		return mNullable == null?aDefaultVal : mNullable;
	}

	public void setNullable(boolean aNullable)
	{
		mNullable = aNullable;
	}

	public Integer getDataPrecision()
	{
		return mDataPrecision;
	}
	
	public void setDataPrecision(int aDataPrecision)
	{
		mDataPrecision = aDataPrecision;
	}

	public Object getDataDefault()
	{
		return mDataDefault;
	}

	public void setDataDefault(Object aDataDefault)
	{
		mDataDefault = aDataDefault;
	}
	
	public void putOtherProperty(String aKey , Object aVal)
	{
		mOtherProps.put(aKey , aVal) ;
	}
	
	public Object getOtherProperty(String aKey)
	{
		return mOtherProps.get(aKey) ;
	}
	
	public String getComment()
	{
		return mComment;
	}
	
	public void setComment(String aComment)
	{
		mComment = aComment;
	}
	
	public abstract String getSqlText() ;
	
	public abstract List<String> getAddFieldSql(String aTableName) ;
	
	protected void initClone(ColumnSchema aClone)
	{
		aClone.mDataType = mDataType ;
		aClone.mColumnName = mColumnName ;
		aClone.mDataLength = mDataLength ;
		aClone.mDataPrecision = mDataPrecision ;
		aClone.mNullable = mNullable ;
		aClone.mDataDefault = mDataDefault ;
		aClone.mOtherProps.putAll(mOtherProps);
	}
	
	public int getOriginalSeq()
	{
		return mOriginalSeq;
	}
	
	public abstract ColumnSchema clone() ;
	
	public String getDisplayDataType()
	{
		if(mDisplayDataType == null)
		{
			StringBuilder strBld = new StringBuilder(mDataType) ;
			if(mDataLength != null)
			{
				strBld.append("(").append(mDataLength) ;
				if(mDataPrecision != null)
					strBld.append(",").append(mDataPrecision) ;
				strBld.append(")") ;
			}
			return strBld.toString() ;
		}
		return mDisplayDataType;
	}
	
	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		aJSONObj.put("originalSeq", mOriginalSeq)
			.put("name", mColumnName)
			.put("comment", mComment)
			.put("dataType", mDataType)
			.put("dataLength", mDataLength)
			.put("dataPrecision", mDataPrecision)
			.put("nullable" , mNullable)
			.put("dataDefault", mDataDefault) ;
		
		
		aJSONObj.put("displayDataType", getDisplayDataType()) ;
		
//		if(!mOtherProps.isEmpty())
//		{
//			for(Entry<String , Object> entry : mOtherProps.entrySet())
//			{
//				aJSONObj.put(entry.getKey(), entry.getValue()) ;
//			}
//		}
		
		return aJSONObj ;
	}
}
