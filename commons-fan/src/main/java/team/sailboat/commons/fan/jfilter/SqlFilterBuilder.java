package team.sailboat.commons.fan.jfilter;

import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Stack;

import team.sailboat.commons.fan.dtool.DBType;
import team.sailboat.commons.fan.dtool.SqlParams;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.First;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.struct.Tuples;

public class SqlFilterBuilder implements IFilterBuilder<SqlParams>
{
	
	Stack<Entry<String , StringBuilder>> mUnionStack ;
	protected final SqlParams mSqlParams = new SqlParams() ; 
	
	DBType mDBType = DBType.MySQL ;
	
	public SqlFilterBuilder()
	{
		mUnionStack = new Stack<>() ;
		mUnionStack.push(Tuples.of(null, new StringBuilder())) ;
	}

	@Override
	public SqlParams build()
	{
		if(mSqlParams.getSql() == null && !mUnionStack.isEmpty())
		{
			Assert.isTrue(mUnionStack.size() == 1) ;
			mSqlParams.setSql(mUnionStack.pop().getValue().toString()) ;
		}
		return mSqlParams ;
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
		checkAndAppendJoint().append(aKey).append(" = ?") ;
		mSqlParams.addParam(aValue , Types.VARCHAR) ;
		return true;
	}

	@Override
	public boolean termNumber(String aKey, Number aValue)
	{
		checkAndAppendJoint().append(aKey).append(" = ?") ;
		mSqlParams.addParam(aValue, Types.NUMERIC) ;
		return true;
	}

	@Override
	public boolean termDate(String aKey, Date aValue)
	{
		checkAndAppendJoint().append(aKey).append(" = ?") ;
		mSqlParams.addParam(aValue, Types.TIMESTAMP) ;
		return true ;
	}

	@Override
	public boolean
			rangeNumber(String aKey, Number aUpperValue, Number aDownValue, boolean aUpperEquals, boolean aDownEquals)
	{
		return range(aKey, aUpperValue, aDownValue, aUpperEquals, aDownEquals, Types.NUMERIC) ;
	}
	
	@Override
	public boolean inNumbers(String aKey, List<? extends Number> aValues)
	{
		if(aValues == null && aValues == null)
			return false ;
		StringBuilder sqlBld = checkAndAppendJoint() ;
		sqlBld.append(aKey).append(" in (") ;
		First first = new First() ;
		for(Number n : aValues)
		{
			if(!first.checkDo())
				sqlBld.append(" , ") ;
			sqlBld.append(n) ;
		}
		sqlBld.append(')') ;
		return true ;
	}
	
	@Override
	public boolean inStrings(String aKey, List<String> aValues)
	{
		if(aValues == null && aValues == null)
			return false ;
		StringBuilder sqlBld = checkAndAppendJoint() ;
		sqlBld.append(aKey).append(" in (") ;
		First first = new First() ;
		for(String s : aValues)
		{
			if(!first.checkDo())
				sqlBld.append(" , ") ;
			int i= s.indexOf('\'') ;
			if(i == -1)
				sqlBld.append('\'').append(s).append('\'') ;
			else
			{
				Assert.isTrue(s.indexOf('"') == -1 , "一个集合元素中不能同时有单引号和双引号不能有单引号！") ;
				sqlBld.append('"').append(s).append('"') ;
			}
		}
		sqlBld.append(')') ;
		return true ;
	}
	
	@Override
	public boolean range(String aKey, Object aUpperValue, Object aDownValue, boolean aUpperEquals, boolean aDownEquals)
	{
		if(aUpperValue == null && aDownValue == null)
			return false ;
		StringBuilder sqlBld = checkAndAppendJoint() ;
		sqlBld.append('(') ;
		if(aUpperValue != null)
		{
			sqlBld.append(aKey).append(aUpperEquals ? " <= ?":" < ?") ;
			if(aUpperValue instanceof Number)
				mSqlParams.addParam(aUpperValue, Types.NUMERIC) ;
			else if(aUpperValue instanceof Date)
				mSqlParams.addParam(aUpperValue, Types.TIMESTAMP) ;
			else
				mSqlParams.addParam(aUpperValue.toString() , Types.VARCHAR) ;
		}
		
		if(aDownValue != null)
		{
			if(aUpperValue != null)
				sqlBld.append(" AND ") ;
			sqlBld.append(aKey).append(aDownEquals?" >= ?":" > ?") ;
			if(aDownValue instanceof Number)
				mSqlParams.addParam(aDownValue, Types.NUMERIC) ;
			else if(aDownValue instanceof Date)
				mSqlParams.addParam(aDownValue, Types.TIMESTAMP) ;
			else
				mSqlParams.addParam(aDownValue.toString() , Types.VARCHAR) ;
		}
		sqlBld.append(')') ;
		return true ;
	}

	@Override
	public boolean
			rangeString(String aKey, String aUpperValue, String aDownValue, boolean aUpperEquals, boolean aDownEquals)
	{
		return range(aKey, aUpperValue, aDownValue, aUpperEquals, aDownEquals, Types.VARCHAR) ;
	}
	
	boolean range(String aKey , Object aUpperValue , Object aDownValue , boolean aUpperEquals , boolean aDownEquals , int aDataType)
	{
		if(aUpperValue == null && aDownValue == null)
			return false ;
		StringBuilder sqlBld = checkAndAppendJoint() ;
		sqlBld.append('(') ;
		if(aUpperValue != null)
		{
			sqlBld.append(aKey).append(aUpperEquals?" <= ?":" < ?") ;
			mSqlParams.addParam(aUpperValue , aDataType) ;
		}
		
		if(aDownValue != null)
		{
			sqlBld.append(" AND ").append(aKey).append(aDownEquals?" >= ?":" > ?") ;
			mSqlParams.addParam(aDownValue, aDataType) ;
		}
		sqlBld.append(')') ;
		return true ;
	}

	@Override
	public boolean rangeDate(String aKey, Date aUpperValue, Date aDownValue, boolean aUpperEquals, boolean aDownEquals)
	{
		return range(aKey, aUpperValue, aDownValue, aUpperEquals, aDownEquals , Types.TIMESTAMP) ;
	}
	
	@Override
	public boolean contains(String aKey, String aValue)
	{
		checkAndAppendJoint().append(aKey).append(" LIKE ?") ;
		mSqlParams.addParam("%"+JCommon.defaultIfNull(aValue , "")+"%" , Types.VARCHAR) ;
		return true;
	}
	
	@Override
	public boolean startsWith(String aKey, String aValue)
	{
		checkAndAppendJoint().append(aKey).append(" LIKE ?") ;
		mSqlParams.addParam(JCommon.defaultIfNull(aValue , "")+"%" , Types.VARCHAR) ;
		return true;
	}
	
	@Override
	public boolean endsWith(String aKey, String aValue)
	{
		checkAndAppendJoint().append(aKey).append(" LIKE ?") ;
		mSqlParams.addParam("%"+JCommon.defaultIfNull(aValue , "") , Types.VARCHAR) ;
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
		checkAndAppendJoint().append(aKey)
				.append(" IS NULL") ;
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
				sqlBld_p.append(" AND ") ;
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
			sqlBld_p.append("NOT ") ;
		
		sqlBld_p.append('(').append(entry.getValue().toString()).append(')') ;
	}
	
	protected String getJoint(String aUnionName)
	{
		switch(aUnionName)
		{
		case sUN_must:
		case sUN_must_not:
			return "AND" ;
		case sUN_should:
			return "OR" ;
		default:
			throw new IllegalStateException("不合法的联合名："+aUnionName) ;
		}
	}

}
