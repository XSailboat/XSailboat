package team.sailboat.base.sql;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import team.sailboat.base.sql.model.BName;
import team.sailboat.commons.fan.collection.XC;

@Data
public class SQLParseContext
{
	final String currentDbName ;
	
	final LinkedHashMap<String, BName> aliasTableNameMap = XC.linkedHashMap() ;
	
	@Getter(value = AccessLevel.NONE)
	@Setter(value = AccessLevel.NONE)
	Map<String , SQLParseContext> subContextMap ;
	
	public SQLParseContext(String aCurrentDbName)
	{
		currentDbName = aCurrentDbName ;
	}
	
	public BName getTableName(String aAlias)
	{
		return aliasTableNameMap.get(aAlias) ;
	}
	
	public void putTableAlias(String aAlias , BName aTableName)
	{
		aliasTableNameMap.put(aAlias, aTableName) ;
	}
	
	public SQLParseContext subContext(String aSubQuerySql)
	{
		if(subContextMap == null)
			subContextMap = XC.linkedHashMap() ;
		SQLParseContext ctx = subContextMap.get(aSubQuerySql) ;
		if(ctx == null)
		{
			ctx = new SQLParseContext(currentDbName) ;
			subContextMap.put(aSubQuerySql, ctx) ;
		}
		return ctx ;
	}
	
	public int getTableAmount()
	{
		return aliasTableNameMap.size() ;
	}
	
	
}
