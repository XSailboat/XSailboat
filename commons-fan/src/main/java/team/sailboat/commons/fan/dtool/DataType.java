package team.sailboat.commons.fan.dtool;

import team.sailboat.commons.fan.lang.XClassUtil;

public interface DataType
{
	public static final String sString = XClassUtil.sCSN_String ;
	
	public static final String sLong = XClassUtil.sCSN_Long ;
	
	public static final String sBool = XClassUtil.sCSN_Bool ;
	
	public static final String sDouble = XClassUtil.sCSN_Double ;
	
	public static final String sDateTime = XClassUtil.sCSN_DateTime ;
	
	public static final String sBytes = XClassUtil.sCSN_Bytes ;
	
	public static final String sInteger = XClassUtil.sCSN_Integer ;
	
	public static final String sDate = "date" ;
	
	String getCommonType() ;
	
	boolean isRecommend() ;
	
	int getJdbcType() ;
}
