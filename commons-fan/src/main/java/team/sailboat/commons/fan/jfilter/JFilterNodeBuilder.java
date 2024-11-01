package team.sailboat.commons.fan.jfilter;

import java.util.List;

public interface JFilterNodeBuilder
{
	Must must() ;
	
	Should should() ;
	
	MustNot must_not() ;
	
	JFilterNodeBuilder term(String aField , Object aValue) ;
	
	JFilterNodeBuilder contains(String aField , String aValue) ;
	
	JFilterNodeBuilder startsWith(String aField , String aValue) ;
	
	JFilterNodeBuilder endsWith(String aField, String aValue) ;
	
	JFilterNodeBuilder in(String aField , List<Object> aCollection) ;
	
	JFilterNodeBuilder expr(String aField , String aExpr) ;
	
	JFilterNodeBuilder range(String aField , Object aMinVal , boolean aMinEquals , Object aMaxVal , boolean aMaxEquals) ;
	
	JFilterNodeBuilder isNull(String aField) ;
	
	interface Logic extends JFilterNodeBuilder
	{
		JFilterBuilder back_root() ;
	}
	
	static interface Must extends Logic
	{
		Should back_should() ;
		MustNot back_must_not() ;
		
		Should should() ;
		MustNot must_not() ;
		
		Must term(String aField , Object aValue) ;
		Must contains(String aField , String aValue) ;
		Must startsWith(String aField , String aValue) ;
		Must endsWith(String aField, String aValue) ;
		Must in(String aField , List<Object> aCollection) ;
		Must expr(String aField , String aExpr) ;
		Must range(String aField , Object aMinVal , boolean aMinEquals , Object aMaxVal , boolean aMaxEquals) ;
		Must isNull(String aField) ;
	}
	
	static interface Should extends Logic
	{
		Must back_must() ;
		MustNot back_must_not() ;
		
		Must must() ;
		MustNot must_not() ;
		
		Should term(String aField , Object aValue) ;
		Should contains(String aField , String aValue) ;
		Should startsWith(String aField , String aValue) ;
		Should endsWith(String aField, String aValue) ;
		Should in(String aField , List<Object> aCollection) ;
		Should expr(String aField , String aExpr) ;
		Should range(String aField , Object aMinVal , boolean aMinEquals , Object aMaxVal , boolean aMaxEquals) ;
		Should isNull(String aField) ;
	}
	
	static interface MustNot extends Logic
	{
		Must back_must() ;
		Should back_should() ;
		
		Must must() ;
		Should should() ;
		
		MustNot term(String aField , Object aValue) ;
		MustNot contains(String aField , String aValue) ;
		MustNot startsWith(String aField , String aValue) ;
		MustNot endsWith(String aField, String aValue) ;
		MustNot in(String aField , List<Object> aCollection) ;
		MustNot expr(String aField , String aExpr) ;
		MustNot range(String aField , Object aMinVal , boolean aMinEquals , Object aMaxVal , boolean aMaxEquals) ;
		MustNot isNull(String aField) ;
	}
}
