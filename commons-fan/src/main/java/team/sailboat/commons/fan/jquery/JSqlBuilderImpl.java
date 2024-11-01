package team.sailboat.commons.fan.jquery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.text.RegexUtils;
import team.sailboat.commons.fan.text.XString;

class JSqlBuilderImpl implements JSqlBuilder
{
	static final Pattern sFieldPtn = Pattern.compile("\\$\\{F(\\d+)\\}") ;
	
	protected final StringBuilder mSqlBld = new StringBuilder() ;
	
	protected final List<Object> mArgList = new ArrayList<>() ;

	public JSqlBuilderImpl()
	{
	}
	
	public JSqlBuilderImpl(String aBaseSql , Object...aArgs)
	{
		if(XString.isNotEmpty(aBaseSql))
		{
			mSqlBld.append(aBaseSql) ;
			XC.addAll(mArgList , aArgs) ;
		}
	}

	@Override
	public JSqlBuilder append(boolean aWhen, String aSqlSeg, Object... aArgs)
	{
		if(aWhen)
		{
			mSqlBld.append(aSqlSeg) ;
			XC.addAll(mArgList, aArgs) ;
		}
		return this ;
	}
	
	@Override
	public JSqlBuilder appendMsgFmt(boolean aWhen, String aSqlSeg, Object... aArgs)
	{
		if(aWhen)
		{
			mSqlBld.append(XString.msgFmt(aSqlSeg, aArgs)) ;
		}
		return this ;
	}
	
	@Override
	public JSqlBuilder appendIn(boolean aWhen, String aSqlSeg , Object... aVals)
	{
		if(aWhen)
		{
			mSqlBld.append(XString.msgFmt(aSqlSeg , XString.repeat(" , ", '?' , aVals.length)));
			XC.addAll(mArgList, aVals) ;
		}
		return this ;
	}
	
	@Override
	public JSqlBuilder appendIn(boolean aWhen, String aSqlSeg , Collection<?> aVals)
	{
		if(aWhen)
		{
			mSqlBld.append(XString.msgFmt(aSqlSeg , XString.repeat(" , ", '?' , aVals.size())));
			XC.addAll(mArgList, aVals) ;
		}
		return this ;
	}
	
	@Override
	public JSqlBuilder checkAppend(boolean aWhen, String aSqlSeg, Object... aArgs)
	{
		if(aWhen)
		{
			Matcher matcher = sFieldPtn.matcher(aSqlSeg) ;
			int maxSeq = 0 ;
			ArrayList<String> list = XC.arrayList() ;
			while(matcher.find())
			{
				String fieldPlaceHolder = matcher.group(0) ;
				int seq = Integer.parseInt(matcher.group(1)) ;
				maxSeq = Math.max(seq , maxSeq) ;
				list.add(seq, fieldPlaceHolder) ;
			}
			String sqlSeg = aSqlSeg ;
			Assert.isTrue(XC.count(aArgs)>maxSeq , "字段数量大于参数数量") ;
			for(int i=0 ; i<=maxSeq ; i++)
			{
				String fieldName = aArgs[i].toString() ;
				Assert.isNotTrue(XString.containsAny(fieldName , ' ' , '%' , '(') , "字段名“%s”不合法", fieldName) ;
				String placeHolder = list.get(i) ;
				if(placeHolder != null)
					sqlSeg = sqlSeg.replace(list.get(i) , fieldName) ;
			}
			// 检查剩余的参数数量 和 问号数量是否相等
			Assert.isTrue(XString.count(sqlSeg, '?', 0) == aArgs.length-(maxSeq+1) , "问号占位符的数量和参数数量不一致") ;
			append(true, sqlSeg, Arrays.copyOfRange(aArgs , maxSeq+1 , aArgs.length)) ;
		}
		return this ;
	}
	
	@Override
	public JSqlBuilder append(String aSqlSeg)
	{
		mSqlBld.append(aSqlSeg) ;
		return this ;
	}
	
	@Override
	public JSqlBuilder replace(String aPlaceHolder, boolean aCnd , String aElseSeg , String aSqlSeg, Object... aArgs)
	{
		if(XString.isNotEmpty(aPlaceHolder))
		{
			if(aCnd)
			{
				final int qmCount = XString.count(aSqlSeg, '?', 0) ;
				if(qmCount>0)
				{
					Assert.isTrue(qmCount == XC.count(aArgs , true) , "参数值数量与参数占位符数量不相同");
					int len = mSqlBld.length() ;
					final int plen = aPlaceHolder.length() ;
					final List<Object> argList = Arrays.asList(aArgs) ;
					char ch ;
					int qmc = 0 ;
					for(int i=0 ; i<len ;)
					{
						ch = mSqlBld.charAt(i) ;
						if(ch == aPlaceHolder.charAt(0))
						{
							//判断接下来的是否相同
							int j=1 ;
							for(;j<plen ; j++)
							{
								if(mSqlBld.charAt(i+j) != aPlaceHolder.charAt(j))
									break ;
							}
							if(j>=plen)
							{
								//说明相同
								mSqlBld.replace(i, i+plen , aSqlSeg) ;
								i += aSqlSeg.length() ;
								mArgList.addAll(qmc , argList) ;
								qmc += qmCount ;
								continue ;
							}
						}
						if(mSqlBld.charAt(i) == '?')
						{
							qmc++ ;
						}
						i++ ;
					}
					return this ;
				}
				mSqlBld.replace(0, mSqlBld.length(),  mSqlBld.toString().replace(aPlaceHolder, aSqlSeg)) ;
			}
			else if(aElseSeg != null)
				mSqlBld.replace(0, mSqlBld.length(),  mSqlBld.toString().replace(aPlaceHolder, aElseSeg)) ;
		}
		return this ;
	}
	
	@Override
	public JSqlBuilder appendOrderBy(boolean aWhen, Object... aArgs)
	{
		if(aWhen && XC.isNotEmpty(aArgs))
		{
			mSqlBld.append(" ORDER BY") ;
			boolean first = true ;
			for(Object arg : aArgs)
			{
				if(arg == null)
					continue ;
				if(arg instanceof Boolean)
					mSqlBld.append(((Boolean)arg).booleanValue()?" ASC":" DESC") ;
				else
				{
					String argStr = arg.toString() ;
					if(argStr.isEmpty())
						continue ;
					if("ASC".equalsIgnoreCase(argStr) || "DESC".equalsIgnoreCase(argStr))
						mSqlBld.append(' ').append(argStr) ;
					else
					{
						if(first)
						{
							mSqlBld.append(" ") ;
							first = false ;
						}
						else
							mSqlBld.append(" , ") ;
						mSqlBld.append(RegexUtils.checkDBFieldName(argStr)) ;
					}
				}
			}
		}
		return this ;
	}

	@Override
	public String getSql()
	{
		return mSqlBld.toString() ;
	}

	@Override
	public List<Object> getArgList()
	{
		return mArgList ;
	}

	@Override
	public Object[] getArgs()
	{
		return mArgList.toArray() ;
	}

}
