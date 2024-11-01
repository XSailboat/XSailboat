package team.sailboat.commons.fan.dtool.hive;

import java.sql.Types;
import java.util.List;
import java.util.Map;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dtool.DataType;

public enum HiveDataType implements DataType
{
	BIGINT(true, sLong , Types.BIGINT) ,
	DOUBLE(true, sDouble , Types.DOUBLE) ,
	STRING(true, sString , Types.VARCHAR) ,
	TIMESTAMP(true, sDateTime , Types.TIMESTAMP) ,
	BOOLEAN(true, sBool , Types.BOOLEAN) ,
	BINARY(true , sBytes , Types.BLOB) ,
	TINYINT(false , sInteger , Types.TINYINT) ,
	INT(true , sInteger , Types.INTEGER) ,
	DATE(true , sDate , Types.DATE)
	;
	
	/**
	 * 是否推荐使用
	 */
	boolean mRecommend ;
	
	String mCommonType ;
	
	int mType ;
	
	private HiveDataType(boolean aRecommend , String aCommonType , int aType)
	{
		
		mRecommend = aRecommend ;
		mType = aType ;
		mCommonType = aCommonType ;
	}
	
	@Override
	public boolean isRecommend()
	{
		return mRecommend;
	}
	
	@Override
	public String getCommonType()
	{
		return mCommonType;
	}
	
	@Override
	public int getJdbcType()
	{
		return mType ;
	}
	
	static final HiveDataType[] sRecommends ;
	static final Map<String, HiveDataType> sCommonTypeMap = XC.concurrentHashMap() ;
	
	static {
		List<HiveDataType> dataTypes = XC.arrayList() ;
		for(HiveDataType dataType : values())
		{
			if(dataType.isRecommend())
			{
				dataTypes.add(dataType) ;
				sCommonTypeMap.put(dataType.getCommonType() , dataType) ;
			}
		}
		sRecommends = dataTypes.toArray(new HiveDataType[0]) ;
	}
	
	public static HiveDataType[] getRecommends()
	{
		return sRecommends ;
	}
	
	public static HiveDataType getDataTypeForCommonType(String aCommonType)
	{
		return sCommonTypeMap.get(aCommonType) ;
	}
}
