package team.sailboat.ms.base.dataset.tool ;

import java.util.Collection;
import java.util.LinkedHashSet;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.Assert;

public class SqlParams
{
	/**
	 * 键是shu r
	 */
	LinkedHashSet<String> mInParamNames ;
	
	LinkedHashSet<String> mOutParamNames ;
	
	/**
	 * Sql中的表达式段
	 */
	LinkedHashSet<String> mExprSqlSegs ;	// 包含:
	
	public SqlParams()
	{
	}
	
	public SqlParams(LinkedHashSet<String> aInParamNames)
	{
		if(XC.isNotEmpty(aInParamNames))
		{
			for(String name : aInParamNames)
			{
				int i = name.indexOf(':') ;
				if(i == -1)
				{
					if(mInParamNames == null)
						mInParamNames = XC.linkedHashSet() ;
					mInParamNames.add(name) ;
				}
				else
				{
					if(i > 0)
					{
						if(mInParamNames == null)
							mInParamNames = XC.linkedHashSet() ;
						String[] names = name.substring(0 , i).split(",") ;
						for(String name_1 : names)
						{
							name_1 = name_1.trim() ;
							if(!name_1.isEmpty())
								mInParamNames.add(name_1) ;
						}
					}
					Assert.isTrue(name.length() > i+1 , "参数表达式[%s]不合法！" , name);
					int nextChar = name.charAt(i+1) ;
					if(nextChar == ':')
					{
						// 连着两个:（::）表示是SQL段，不是参数占位符
						if(mExprSqlSegs == null)
							mExprSqlSegs = XC.linkedHashSet() ;
						mExprSqlSegs.add(name) ;
					}
				}
			}
		}
	}
	
	public LinkedHashSet<String> getExprSqlSegs()
	{
		return mExprSqlSegs;
	}
	
	public boolean hasInParams()
	{
		return mInParamNames != null && mInParamNames.size()>0 ;
	}
	
	public LinkedHashSet<String> getInParamNames()
	{
		return mInParamNames;
	}
	
	public LinkedHashSet<String> getOutParamNames()
	{
		return mOutParamNames;
	}
	public void setOutParamNames(Collection<String> aOutParamNames)
	{
		if(aOutParamNames == null || aOutParamNames instanceof LinkedHashSet)
			mOutParamNames = (LinkedHashSet<String>) aOutParamNames;
		else
			mOutParamNames = new LinkedHashSet<String>(aOutParamNames) ;
	}
}
