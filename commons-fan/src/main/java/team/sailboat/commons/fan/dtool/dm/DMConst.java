package team.sailboat.commons.fan.dtool.dm;

import java.util.Set;
import java.util.regex.Pattern;

import team.sailboat.commons.fan.collection.XC;

public interface DMConst
{
	/**
	 * Check on a table , 作用于Column
	 */
	public static final String sConstraintType_C = "C" ;
	
	/**
	 * Read Only on a view，作用于Object
	 */
	public static final String sConstraintType_O = "O" ;
	
	/**
	 * Primary Key，作用于Object
	 */
	public static final String sConstraintType_P = "P" ;
	
	/**
	 * Referential AKA Foreign Key,作用于Column
	 */
	public static final String sConstraintType_R = "R" ;
	
	/**
	 * Unique Key，作用于Column
	 */
	public static final String sConstraintType_U = "U" ;
	
	/**
	 * Check Option on a view，作用于Object
	 */
	public static final String sConstraintType_V = "V" ;
	
	/**
	 * 可变长度的字符串 最大长度4000 bytes 可做索引的最大长度749
	 */
	public static final String sDataType_VARCHAR2 = "VARCHAR2" ;
	
	/**
	 * 根据字符集而定的可变长度字符串 最大长度4000 bytes
	 */
	public static final String sDataType_NVARCHAR2 = "NVARCHAR2" ;
	
	/**
	 * NUMBER(P,S) 数字类型 P为整数位，S为小数位
	 */
	public static final String sDataType_NUMBER = "NUMBER" ;
	
	/**
	 * DECIMAL(P,S) 数字类型 P为整数位，S为小数位
	 */
	public static final String sDataType_DECIMAL = "DECIMAL" ;
	
	/**
	 * 日期和时间类型
	 */
	public static final String sDataType_DATE = "DATE" ;
	
	/**
	 * 固定长度字符串 最大长度2000 bytes
	 */
	public static final String sDataType_CHAR = "CHAR" ;
	
	/**
	 * 根据字符集而定的固定长度字符串 最大长度2000 bytes
	 */
	public static final String sDataType_NCHAR = "NCHAR" ;
	
	/**
	 * 超长字符串 最大长度2G（231-1） 足够存储大部头著作
	 */
	public static final String sDataType_LONG = "LONG" ; 
	
	/**
	 * 二进制数据 最大长度4G
	 */
	public static final String sDataType_BLOB = "BLOB" ;
	
	/**
	 * 整数类型 小的整数
	 */
	public static final String sDataType_INTEGER = "INTEGER" ;
	
	public static final String sDataType_SMALLINT = "SMALLINT" ;
	
	/**
	 * 浮点数类型 NUMBER(38)，双精度
	 */
	public static final String sDataType_FLOAT = "FLOAT" ;
	
	/**
	 *  实数类型 NUMBER(63)，精度更高
	 */
	public static final String sDataType_REAL = "REAL" ;
	
	/**
	 * 字符数据 最大长度4G
	 */
	public static final String sDataType_CLOB = "CLOB" ;
	
	/**
	 * 存放在数据库外的二进制数据 最大长度4G
	 */
	public static final String sDataType_BFILE = "BFILE" ;
	
	/**
	 * 一个参数的数据类型
	 */
	public static final Set<String> sOneParamDataTypeSet = XC.hashSet(sDataType_VARCHAR2
			, sDataType_CHAR , sDataType_NCHAR , sDataType_NVARCHAR2) ;
	
	/**
	 * 两个参数的数据类型
	 */
	public static final Set<String> sTwoParamsDataTypeSet = XC.hashSet(sDataType_NUMBER
			, sDataType_DECIMAL) ;
	
	public static final Pattern sPtn_CC_NotNull = Pattern.compile("\"(.+)\"IS NOT NULL") ;
	
	public static final String sSYSDATE = "SYSDATE" ;
}
