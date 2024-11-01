package team.sailboat.commons.fan.dtool.pg;

import java.sql.Types;
import java.util.List;
import java.util.Map;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dtool.DataType;

public enum PgDataType implements DataType
{
	//
	TINYINT(false, sInteger , Types.TINYINT) ,
	SMALLINT(false , sInteger , Types.SMALLINT) ,
	INT(true , sInteger , Types.INTEGER) ,
	BIGINT(true, sLong , Types.BIGINT) ,
	FLOAT(false , sDouble , Types.FLOAT) ,
	FLOAT8(true, sDouble , Types.DOUBLE) ,
	DECIMAL(false, sDouble , Types.DECIMAL) ,
	//
	TIMESTAMP(true , sDateTime , Types.TIMESTAMP) ,
	DATE(true , sDate , Types.DATE) ,
	//
	CHAR(false , sString , Types.CHAR) ,
	VARCHAR(true , sString , Types.VARCHAR) ,
	BLOB(false , sBytes , Types.BLOB) ,
	TEXT(false , sString , Types.LONGVARCHAR) ,
	BOOLEAN(true , sBool , Types.BOOLEAN)
	;
	
	/**
	 * 是否推荐使用
	 */
	boolean mRecommend ;
	
	String mCommonType ;
	
	int mType ;
	
	String mSqlType ;
	
	private PgDataType(boolean aRecommend , String aCommonType , int aType)
	{
		mRecommend = aRecommend ;
		mCommonType = aCommonType ;
		mType = aType ;
	}
	
	@Override
	public String getCommonType()
	{
		return mCommonType;
	}
	
	@Override
	public boolean isRecommend()
	{
		return mRecommend;
	}
	
	@Override
	public int getJdbcType()
	{
		return mType ;
	}
	
	static final PgDataType[] sRecommends ;
	static final Map<String, PgDataType> sCommonTypeMap = XC.concurrentHashMap() ;
	
	static {
		List<PgDataType> dataTypes = XC.arrayList() ;
		for(PgDataType dataType : values())
		{
			if(dataType.isRecommend())
			{
				dataTypes.add(dataType) ;
				sCommonTypeMap.put(dataType.getCommonType() , dataType) ;
			}
		}
		sRecommends = dataTypes.toArray(new PgDataType[0]) ;
	}
	
	public static PgDataType[] getRecommends()
	{
		return sRecommends ;
	}
	
	public static PgDataType getDataTypeForCommonType(String aCommonType)
	{
		PgDataType dataType = sCommonTypeMap.get(aCommonType) ;
		if(dataType == null && sBool.equals(aCommonType))
			return TINYINT ;
		return dataType ;
	}
}
