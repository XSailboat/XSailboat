package team.sailboat.commons.fan.dtool.taos;

import java.sql.Types;
import java.util.List;
import java.util.Map;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dtool.DataType;

public enum TDengineDataType implements DataType
{
	//
	TINYINT(false, sInteger , Types.TINYINT) ,
	SMALLINT(false , sInteger , Types.SMALLINT) ,
	INT(true , sInteger , Types.INTEGER) ,
	BIGINT(true, sLong , Types.BIGINT) ,
	//
	FLOAT(false , sDouble , Types.FLOAT) ,
	DOUBLE(true, sDouble , Types.DOUBLE) ,
	//
	TIMESTAMP(true, sDateTime , Types.TIMESTAMP) ,
	//
	NCHAR(true , sString , Types.VARCHAR) ,
	//
	BOOL(true , sString , Types.BOOLEAN) ,
	//
	BINARY(true , sBytes , Types.BINARY)
	;
	
	/**
	 * 是否推荐使用
	 */
	boolean mRecommend ;
	
	String mCommonType ;
	
	int mType ;
	
	private TDengineDataType(boolean aRecommend , String aCommonType , int aType)
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
	
	static final TDengineDataType[] sRecommends ;
	static final Map<String, TDengineDataType> sCommonTypeMap = XC.concurrentHashMap() ;
	
	static {
		List<TDengineDataType> dataTypes = XC.arrayList() ;
		for(TDengineDataType dataType : values())
		{
			if(dataType.isRecommend())
			{
				dataTypes.add(dataType) ;
				sCommonTypeMap.put(dataType.getCommonType() , dataType) ;
			}
		}
		sRecommends = dataTypes.toArray(new TDengineDataType[0]) ;
	}
	
	public static TDengineDataType[] getRecommends()
	{
		return sRecommends ;
	}
	
	public static TDengineDataType getDataTypeForCommonType(String aCommonType)
	{
		TDengineDataType dataType = sCommonTypeMap.get(aCommonType) ;
		if(dataType == null && sBool.equals(aCommonType))
			return TINYINT ;
		return dataType ;
	}
}
