package team.sailboat.commons.fan.dtool.mysql;

import java.sql.Types;
import java.util.List;
import java.util.Map;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dtool.DataType;

public enum MySqlDataType implements DataType
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
	
	private MySqlDataType(boolean aRecommend , String aCommonType , int aType)
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
	
	static final MySqlDataType[] sRecommends ;
	static final Map<String, MySqlDataType> sCommonTypeMap = XC.concurrentHashMap() ;
	
	static {
		List<MySqlDataType> dataTypes = XC.arrayList() ;
		for(MySqlDataType dataType : values())
		{
			if(dataType.isRecommend())
			{
				dataTypes.add(dataType) ;
				sCommonTypeMap.put(dataType.getCommonType() , dataType) ;
			}
		}
		sRecommends = dataTypes.toArray(new MySqlDataType[0]) ;
	}
	
	public static MySqlDataType[] getRecommends()
	{
		return sRecommends ;
	}
	
	public static MySqlDataType getDataTypeForCommonType(String aCommonType)
	{
		MySqlDataType dataType = sCommonTypeMap.get(aCommonType) ;
		if(dataType == null && sBool.equals(aCommonType))
			return TINYINT ;
		return dataType ;
	}
}
