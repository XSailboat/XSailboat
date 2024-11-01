package team.sailboat.commons.fan.jfilter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Stack;

import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.struct.Tuples;

public class AviatorExpBuilder implements IFilterBuilder<AviatorExpression>
{
	final SimpleDateFormat mSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS") ;

	Stack<Entry<String , StringBuilder>> mUnionStack ;
	final AviatorExpression mExpr = new AviatorExpression() ;
	
	public AviatorExpBuilder()
	{
		mUnionStack = new Stack<>() ;
		mUnionStack.push(Tuples.of(null, new StringBuilder())) ;
	}

	@Override
	public AviatorExpression build()
	{
		if(mExpr.getValue() == null && !mUnionStack.isEmpty())
		{
			Assert.isTrue(mUnionStack.size() == 1) ;
			mExpr.setValue(mUnionStack.pop().getValue().toString()) ;
		}
		return mExpr ;
	}

	@Override
	public void setArgs(Object[] aArgs)
	{
	}

	@Override
	public FilterField parseFilterField(String aKey)
	{
		return new FilterField(aKey);
	}
	
	@Override
	public boolean term(String aKey, Object aValue)
	{
		Assert.notNull(aValue , "JFilter的term元素值不能是null") ;
		if(aValue == null)
			return false ;
		if(aValue instanceof Number)
			return termNumber(aKey, (Number)aValue) ;
		else
			return termString(aKey, aValue.toString()) ;
	}

	@Override
	public boolean termString(String aKey, String aValue)
	{
		appendString(checkAndAppendJoint().append("${").append(aKey).append('}')
				.append(" == ") , aValue) ;
		return true;
	}
	
	public static StringBuilder appendString(StringBuilder aStrBld , String string)
	{
		if(string == null)
			return aStrBld.append("nil") ;
		else if(string.isEmpty())
			return aStrBld.append("''") ;
		else
		{
			char c = 0;
			final int len = string.length();
	
			aStrBld.append('\'');
			for (int i = 0; i < len; i += 1)
			{
				c = string.charAt(i);
				switch (c)
				{
				case '/':
				case '\'':
					aStrBld.append('/')
						.append(c);
					break;
				default:
					aStrBld.append(c);
				}
			}
			return aStrBld.append('\'');
		}
	}

	@Override
	public boolean termNumber(String aKey, Number aValue)
	{
		checkAndAppendJoint().append("${").append(aKey).append('}')
				.append(" == ").append(aValue==null?"nil":aValue) ;
		return true;
	}

	@Override
	public boolean termDate(String aKey, Date aValue)
	{
		StringBuilder strBld = checkAndAppendJoint().append("${").append(aKey).append('}')
				.append(" == ") ;
		if(aValue == null)
			strBld.append("nil") ;
		else
			strBld.append('\'').append(mSdf.format(aValue)).append('\'') ;
		return true ;
	}

	@Override
	public boolean inNumbers(String aKey, List<? extends Number> aValues)
	{
		String paramName = mExpr.addParam(aValues) ;
		checkAndAppendJoint().append("include(").append(paramName)
				.append(" , ").append("${").append(aKey).append('}')
				.append(")") ;
		return true;
	}
	
	@Override
	public boolean inStrings(String aKey, List<String> aValues)
	{
		String paramName = mExpr.addParam(aValues) ;
		checkAndAppendJoint().append("include(").append(paramName)
				.append(" , ").append("${").append(aKey).append('}')
				.append(")") ;
		return true;
	}
	
	@Override
	public boolean rangeNumber(String aKey, Number aUpperValue, Number aDownValue, boolean aUpperEquals, boolean aDownEquals)
	{
		return range(aKey, aUpperValue, aDownValue, aUpperEquals, aDownEquals) ;
	}
	
	@Override
	public boolean range(String aKey, Object aUpperValue, Object aDownValue, boolean aUpperEquals, boolean aDownEquals)
	{
		if(aUpperValue == null && aDownValue == null)
			return false ;
		StringBuilder strBld = checkAndAppendJoint() ;
		strBld.append('(') ;
		if(aUpperValue != null)
		{
			strBld.append("${").append(aKey).append('}').append(aUpperEquals ? " <= ":" < ") ;
			if(aUpperValue instanceof Number)
				strBld.append(aUpperValue) ;
			else if(aUpperValue instanceof Date)
				strBld.append('\'').append(mSdf.format(aUpperValue)).append('\'') ;
			else
				strBld.append('\'').append(aUpperValue).append('\'') ;
		}
		
		if(aDownValue != null)
		{
			if(aUpperValue != null)
				strBld.append(" && ") ;
			strBld.append("${").append(aKey).append('}').append(aDownEquals?" >= ":" > ") ;
			if(aDownValue instanceof Number)
				strBld.append(aUpperValue) ;
			else if(aDownValue instanceof Date)
				strBld.append('\'').append(mSdf.format(aUpperValue)).append('\'') ;
			else
				strBld.append('\'').append(aUpperValue).append('\'') ;
		}
		strBld.append(')') ;
		return true ;
	}

	@Override
	public boolean rangeString(String aKey, String aUpperValue, String aDownValue, boolean aUpperEquals, boolean aDownEquals)
	{
		return range(aKey, aUpperValue, aDownValue, aUpperEquals, aDownEquals) ;
	}

	@Override
	public boolean rangeDate(String aKey, Date aUpperValue, Date aDownValue, boolean aUpperEquals, boolean aDownEquals)
	{
		return range(aKey, aUpperValue, aDownValue, aUpperEquals, aDownEquals) ;
	}
	
	@Override
	public boolean contains(String aKey, String aValue)
	{
		checkAndAppendJoint().append("string.contains(").append("${").append(aKey).append('}').append(" , '")
				.append(aValue.replace("'", "\\'")).append('\'') ;
		return true;
	}
	
	@Override
	public boolean startsWith(String aKey, String aValue)
	{
		checkAndAppendJoint().append("string.startsWith(").append("${").append(aKey).append('}').append(" , '")
			.append(aValue.replace("'", "\\'")).append('\'') ;
		return true;
	}
	
	@Override
	public boolean endsWith(String aKey, String aValue)
	{
		checkAndAppendJoint().append("string.endsWith(").append("${").append(aKey).append('}').append(" , '")
			.append(aValue.replace("'", "\\'")).append('\'') ;
		return true;
	}
	
	@Override
	public boolean expr(String aKey, String aValue)
	{
		checkAndAppendJoint().append(aValue.replace("$$", aKey)) ;
		return true;
	}

	@Override
	public boolean isNull(String aKey)
	{
		checkAndAppendJoint().append("${").append(aKey).append('}')
				.append(" == nil") ;
		return true ;
	}

	@Override
	public void unionBegin(String aUnionName)
	{
		mUnionStack.push(Tuples.of(aUnionName , new StringBuilder())) ;
	}
	
	protected StringBuilder checkAndAppendJoint()
	{
		StringBuilder sqlBld_p = mUnionStack.peek().getValue() ;
		boolean first = sqlBld_p.length() == 0 ;
		if(!first)
		{
			String unionName_p = mUnionStack.peek().getKey() ;
			if(unionName_p == null)
				sqlBld_p.append(" && ") ;
			else
				sqlBld_p.append(' ').append(getJoint(unionName_p)).append(' ') ;
		}
		return sqlBld_p ;
	}

	@Override
	public void unionEnd(String aUnionName)
	{
		Entry<String , StringBuilder> entry = mUnionStack.pop() ;
		Assert.isTrue(aUnionName.equals(entry.getKey())) ;
		if(entry.getValue().length() == 0)
			return ;
		
		StringBuilder sqlBld_p = checkAndAppendJoint();
		if(sUN_must_not.equals(aUnionName))
			sqlBld_p.append("!") ;
		
		sqlBld_p.append('(').append(entry.getValue().toString()).append(')') ;
	}
	
	protected String getJoint(String aUnionName)
	{
		switch(aUnionName)
		{
		case sUN_must:
		case sUN_must_not:
			return "&&" ;
		case sUN_should:
			return "||" ;
		default:
			throw new IllegalStateException("不合法的联合名："+aUnionName) ;
		}
	}

}
