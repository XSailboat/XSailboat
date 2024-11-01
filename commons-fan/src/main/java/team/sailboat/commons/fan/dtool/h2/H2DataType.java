package team.sailboat.commons.fan.dtool.h2;

import java.sql.Types;
import java.util.List;
import java.util.Map;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dtool.DataType;

public enum H2DataType implements DataType
{
	//
	TINYINT(false, sInteger , Types.TINYINT) ,
	SMALLINT(false , sInteger , Types.SMALLINT) ,
	INT(true , sInteger , Types.INTEGER) ,
	BIGINT(true, sLong , Types.BIGINT) ,
	FLOAT(false , sDouble , Types.FLOAT) ,
	DOUBLE(true, sDouble , Types.DOUBLE) ,
	DECIMAL(false, sDouble , Types.DECIMAL) ,
	//
	DATETIME(true , sDateTime , Types.TIMESTAMP) ,
	TIMESTAMP(false, sDateTime , Types.TIMESTAMP) ,
	DATE(true , sDate , Types.DATE) ,
	//
	CHAR(false , sString , Types.CHAR) ,
	VARCHAR(true , sString , Types.VARCHAR) ,
	BLOB(false , sBytes , Types.BLOB) ,
	TEXT(false , sString , Types.LONGVARCHAR)
	;
	
	/**
	 * 是否推荐使用
	 */
	boolean mRecommend ;
	
	String mCommonType ;
	
	int mType ;
	
	private H2DataType(boolean aRecommend , String aCommonType , int aType)
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
	
	static final H2DataType[] sRecommends ;
	static final Map<String, H2DataType> sCommonTypeMap = XC.concurrentHashMap() ;
	
	static {
		List<H2DataType> dataTypes = XC.arrayList() ;
		for(H2DataType dataType : values())
		{
			if(dataType.isRecommend())
			{
				dataTypes.add(dataType) ;
				sCommonTypeMap.put(dataType.getCommonType() , dataType) ;
			}
		}
		sRecommends = dataTypes.toArray(new H2DataType[0]) ;
	}
	
	public static H2DataType[] getRecommends()
	{
		return sRecommends ;
	}
	
	public static H2DataType getDataTypeForCommonType(String aCommonType)
	{
		H2DataType dataType = sCommonTypeMap.get(aCommonType) ;
		if(dataType == null && sBool.equals(aCommonType))
			return TINYINT ;
		return dataType ;
	}
}
