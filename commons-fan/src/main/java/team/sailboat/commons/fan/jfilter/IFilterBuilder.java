package team.sailboat.commons.fan.jfilter;

import java.util.Date;
import java.util.List;

public interface IFilterBuilder<T> extends FilterConstant
{	
	T build() ;
	
	void setArgs(Object[] aArgs) ;
	
	FilterField parseFilterField(String aKey) ;
	
	boolean term(String aKey , Object aValue) ;
	
	boolean termString(String aKey , String aValue) ;
	
	boolean termNumber(String aKey , Number aValue) ;
	
	boolean termDate(String aKey , Date aValue) ;
	
	boolean range(String aKey , Object aUpperValue , Object aDownValue , boolean aUpperEquals , boolean aDownEquals) ;
	
	boolean rangeNumber(String aKey , Number aUpperValue , Number aDownValue , boolean aUpperEquals , boolean aDownEquals) ;
	
	boolean rangeString(String aKey , String aUpperValue , String aDownValue , boolean aUpperEquals , boolean aDownEquals) ;
	
	boolean rangeDate(String aKey , Date aUpperValue , Date aDownValue , boolean aUpperEquals , boolean aDownEquals) ;
	
	boolean inNumbers(String aKey , List<? extends Number> aValues) ;
	
	boolean inStrings(String aKey , List<String> aValues) ;
	
	boolean contains(String aKey , String aValue) ;
	
	boolean startsWith(String aKey , String aValue) ;
	
	boolean endsWith(String aKey , String aValue) ;
	
	boolean expr(String aKey , String aValue);
	
	boolean isNull(String aKey) ;
	
	void unionBegin(String aUnionName) ;
	void unionEnd(String aUnionName) ;
}
