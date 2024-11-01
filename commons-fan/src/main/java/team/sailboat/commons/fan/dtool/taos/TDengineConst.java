package team.sailboat.commons.fan.dtool.taos;

import java.util.Set;

import team.sailboat.commons.fan.collection.XC;

public interface TDengineConst
{
	/**
	 * 1 字节 ，小整数值 
	 */
	public static final String sDataType_TINYINT = "TINYINT" ;
	
	/**
	 * 2 字节, 大整数值 
	 */
	public static final String sDataType_SMALLINT = "SMALLINT" ;
	
	/**
	 * 4字节，	大整数值
	 */
	public static final String sDataType_INT = "INT" ;
	
	/**
	 * 8 字节 ，极大整数值 
	 */
	public static final String sDataType_BIGINT = "BIGINT" ;
	
	/**
	 * 4 字节 ，单精度，浮点数值 
	 */
	public static final String sDataType_FLOAT = "FLOAT" ;
	
	/**
	 * 8 字节 ，双精度浮点数值 
	 */
	public static final String sDataType_DOUBLE = "DOUBLE" ;
	
	/**
	 * 时间值，时间戳
	 */
	public static final String sDataType_TIMESTAMP = "TIMESTAMP" ;
	
	/**
	 * 变长字符串 
	 */
	public static final String sDataType_NCHAR = "NCHAR" ;
	
	
	/**
	 * MySQL没有内置的布尔类型。 但是它使用TINYINT(1)。 为了更方便，MySQL提供BOOLEAN或BOOL作为TINYINT(1)的同义词。
	 */
	public static final String sDataType_BOOL = "BOOL" ; 
	
	/**
	 * 只用在tag上
	 */
	public static final String sDataType_JSON = "JSON" ;
	
	public static final String sDataType_BINARY = "BINARY" ;
	
	/**
	 * 一个参数的数据类型
	 */
	public static final Set<String> sOneParamDataTypeSet_fix = XC.hashSet(sDataType_NCHAR
			, sDataType_BINARY) ;
}
