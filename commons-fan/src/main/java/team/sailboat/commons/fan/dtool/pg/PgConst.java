package team.sailboat.commons.fan.dtool.pg;

import java.util.Set;

import team.sailboat.commons.fan.collection.XC;

public interface PgConst
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
	 * 3 字节, 	大整数值
	 */
	public static final String sDataType_MEDIUMINT = "MEDIUINT" ; 
	
	/**
	 * 4字节，	大整数值
	 */
	public static final String sDataType_INTEGER = "INTEGER" ;
	
	/**
	 * 8 字节 ，极大整数值 
	 */
	public static final String sDataType_BIGINT = "BIGINT" ;
	
	/**
	 * 自增长列
	 */
	public static final String sDataType_BIGSERIAL = "BIGSERIAL" ;
	
	/**
	 * 4 字节 ，单精度，浮点数值 
	 */
	public static final String sDataType_FLOAT = "FLOAT" ;
	
	/**
	 * 8 字节 ，双精度浮点数值 
	 */
	public static final String sDataType_DOUBLE_PRECISION = "DOUBLE PRECISION" ;
	
	/**
	 * 对DECIMAL(M,D) ，如果M>D，为M+2否则为D+2 
	 */
	public static final String sDataType_DECIMAL = "DECIMAL" ;
	
	/**
	 * YYYY-MM-DD ,日期值
	 */
	public static final String sDataType_DATE = "DATE" ;
	
	/**
	 * HH:MM:SS，时间值或持续时间 
	 */
	public static final String sDataType_TIME = "TIME" ;
	
	/**
	 * YYYY ，年份值
	 */
	public static final String sDataType_YEAR = "YEAR" ;
	
	/**
	 * YYYY-MM-DD HH:MM:SS,混合日期和时间值 
	 */
	public static final String sDataType_TIMESTAMP = "TIMESTAMP" ;
	
	/**
	 * 0-255字节 ，定长字符串 
	 */
	public static final String sDataType_CHAR = "CHAR" ;
	
	/**
	 * 0-65535 字节 ，变长字符串 
	 */
	public static final String sDataType_VARCHAR = "VARCHAR" ;
	
	/**
	 * 0-255字节，不超过 255 个字符的二进制字符串
	 */
	public static final String sDataType_TINYBLOB = "TINYBLOB" ;
	
	/**
	 * 0-255字节 ，短文本字符串 
	 */
	public static final String sDataType_TINYTEXT = "TINYTEXT" ;
	
	/**
	 * bytea数据类型允许存储最多1GB的二进制数据
	 */
	public static final String sDataType_BYTEA = "BYTEA" ;
	
	/**
	 * 
	 */
	public static final String sDataType_BOOLEAN = "BOOLEAN" ;
	
	/**
	 * 0-65 535字节 ，长文本数据 
	 */
	public static final String sDataType_TEXT = "TEXT" ;
	
	/**
	 * 0-16 777 215字节 ，二进制形式的中等长度文本数据 
	 */
	public static final String sDataType_MEDIUMBLOB = "MEDIUMBLOB" ;
	
	/**
	 * 0-16 777 215字节 ， 	中等长度文本数据 
	 */
	public static final String sDataType_MEDIUMTEXT = "MEDIUMTEXT" ;
	
	/**
	 * 0-4 294 967 295字节 ，二进制形式的极大文本数据 
	 */
	public static final String sDataType_LONGBLOB = "LONGBLOB" ;
	
	/**
	 * 0-4 294 967 295字节 ，极大文本数据 
	 */
	public static final String sDataType_LONGTEXT = "LONGTEXT" ;
	
	
	public static final Set<String> sOneParamDataTypeSet_flex = XC.hashSet(sDataType_TINYINT
			, sDataType_SMALLINT , sDataType_MEDIUMINT , sDataType_BIGINT
			, sDataType_TINYTEXT , sDataType_TEXT , sDataType_MEDIUMTEXT , sDataType_LONGTEXT , sDataType_TIMESTAMP) ;
	
	/**
	 * 一个参数的数据类型
	 */
	public static final Set<String> sOneParamDataTypeSet_fix = XC.hashSet(sDataType_CHAR
			, sDataType_VARCHAR) ;
	
	/**
	 * 两个参数的数据类型
	 */
	public static final Set<String> sTwoParamsDataTypeSet_flex = XC.hashSet(sDataType_DECIMAL
			, sDataType_FLOAT) ;
	
	
	public static final String sCOL_FEATURE__ON_UPDATE__VAL__CURRENT_TIMESTAMP = "CURRENT_TIMESTAMP" ;
	
	public static final String sCOL_FEATURE__COLLATION__VAL__utf8_general_ci = "utf8_general_ci" ;
	
	public static final String sCOL_FEATURE__COLLATION__VAL__utf8_bin = "utf8_bin" ;
	
	public static final String sCOL_FEATURE__CHARSET__VAL__utf8 = "utf8" ;
	
	public static final String sTBL_FEATURE__COLLATION__VAL__utf8_general_ci = "utf8_general_ci" ;
	
	public static final String sTBL_FEATURE__COLLATION__VAL__utf8_bin = "utf8_bin" ;
	
	public static final Set<String> sKeyWords = XC.hashSet("source") ;
	
	/**
	 * 数据库的系统参数
	 */
	public static final Set<String> sSysParams = XC.treeSet("CURRENT_DATE") ;
}
