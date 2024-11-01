package team.sailboat.base.sql;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLAllColumnExpr;
import com.alibaba.druid.sql.ast.expr.SQLBetweenExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLCastExpr;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLInListExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntervalExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumericLiteralExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLSubqueryTableSource;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.dialect.postgresql.ast.expr.PGTypeCastExpr;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import team.sailboat.base.sql.model.BColumn;
import team.sailboat.base.sql.model.BName;
import team.sailboat.base.sql.model.BTable;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.struct.Bits;
import team.sailboat.commons.fan.struct.Tuples;
import team.sailboat.commons.fan.text.XString;

/**
 * 
 * SQL的血缘分析引擎
 *
 * @author yyl
 * @since 2024年10月9日
 */
public abstract class SqlBloodEngine implements ISqlBloodEngine
{
	DbType mDbType ;
	
	String mDefaultDBName ;
	
	Table<String, String, BTable> mTables = HashBasedTable.create() ;
	
	final ThreadLocal<Map<BName, BTable>> mTL_TableCache = new ThreadLocal<>() ;
	
	public SqlBloodEngine(DbType aDbType , String aDefaultDBName)
	{
		mDbType = aDbType ;
		mDefaultDBName = toLowerCase_DbName(aDefaultDBName) ;
	}

	@Override
	public List<BTable> getTable(String aTableName)
	{
		return XC.arrayList(mTables.column(aTableName).values()) ;
	}
	
	/**
	 * 是否是数据库的系统参数
	 * @param aName
	 * @return
	 */
	protected boolean isSysParam(String aName)
	{
		return false ;
	}

	@Override
	public BTable getTable(BName aTableName)
	{
		Map<BName, BTable> map = mTL_TableCache.get() ;
		BTable tbl = null ;
		if(map != null)
			tbl = map.get(aTableName) ;
		if(tbl == null)
			tbl = mTables.get(aTableName.getPrefix() , aTableName.getLocalName()) ;
		return tbl ;
	}

	@Override
	public List<BTable> parse(String aCurrentDdName, String aSql)
	{
		String currentDbName = toLowerCase_DbName(aCurrentDdName) ;
		Map<BName, BTable> tableCache = XC.linkedHashMap() ;
		mTL_TableCache.set(tableCache) ;
		try
		{
			SQLStatement stm = SQLUtils.parseSingleStatement(aSql, mDbType) ;
			if(stm instanceof SQLSelectStatement)
			{
				SQLParseContext ctx = new SQLParseContext(JCommon.defaultIfEmpty(currentDbName, mDefaultDBName)) ;
				return Arrays.asList(parseSelect(ctx
						, ((SQLSelectStatement) stm).getSelect())) ;
			}
			else
				throw new IllegalStateException("未支持的："+stm.getClass().getName()) ;
//			System.out.println(new JSONArray(tableCache.values()));
		}
		finally
		{
			mTL_TableCache.set(null) ;
		}
	}
	
	/**
	 * 
	 * @param aStm
	 * @param aRelatedTableNames
	 */
	protected BTable parseSelect(SQLParseContext aCtx , SQLSelect aSlct)
	{
		BTable table = BTable.ofSelectVirtualTable(aCtx.getCurrentDbName()) ;
		SQLSelectQueryBlock query = aSlct.getQueryBlock() ;
		Assert.notNull(query , "无法取得SQLSelectQueryBlock，SQL:%s" , aSlct) ;
		List<SQLSelectItem> itemList = query.getSelectList() ;
		SQLTableSource from = query.getFrom() ;
		parseFrom(aCtx , from,  0) ;
		int checkActions = 0 ;
		if(aCtx.getTableAmount() > 1)
			checkActions |= sCheckAction_TableAliasPrefixField ;
		parseSelectItems(aCtx , itemList , table , checkActions) ;
		
		parseWhere(aCtx , query.getWhere() , checkActions);
		addTable(table) ;
		aCtx.putTableAlias(table.getName().getLocalName() , table.getName()) ;
		return table ;
	}
	
	protected void parseSelectItems(SQLParseContext aCtx ,  List<SQLSelectItem> aItemList , BTable aSlctVirTable
			, int aCheckActions)
	{
		if(!aItemList.isEmpty())
		{
			LinkedHashMap<String , BName> aliasTableNameMap = aCtx.getAliasTableNameMap() ;
			for(SQLSelectItem item : aItemList)
			{
				SQLExpr expr = item.getExpr() ;
				List<BName> nameList = extractBNames(aCtx , expr , Bits.hit(aCheckActions , sCheckAction_TableAliasPrefixField)
						, this::throwColumnNotPrefixTableAlias) ;
				
				LinkedHashSet<BColumn> parentColList = null ;
				if(!nameList.isEmpty())
				{
					parentColList = XC.linkedHashSet() ;
					for(BName colName : nameList)
					{
						String tableAlias = colName.getPrefix() ;
						BName tableName = null ;
						if(XString.isEmpty(tableAlias))
						{
							int tsize = aliasTableNameMap.size() ;
							if(tsize == 1)
								tableName = XC.getFirst(aliasTableNameMap.values()) ;
							else
							{
								// 有可能是系统参数或基于系统参数的函数。例如：date_trunc('year' , CURRENT_DATE - INTERVAL '1 year')
								if(isSysParam(colName.getLocalName()))
									continue ;
								throw new IllegalStateException(String.format("无法确定列项[%s]所属的表！" , item)) ;
							}
						}
						else
							tableName = aliasTableNameMap.get(tableAlias) ;
						BTable table = getTable(tableName) ;
						Assert.notNull(table , "表[%s]未构建！" , tableName) ;
						parentColList.add(table.addColumnIfAbsent(colName.getLocalName())) ;
					}
				}
				String alias = item.getAlias() ;
				if(alias != null)
					BColumn.relate(parentColList, aSlctVirTable.addColumn(item.getAlias())) ;
				else
				{
					SQLExpr sqlExpr = item.getExpr() ;
					if(sqlExpr instanceof SQLIdentifierExpr)
					{
						SQLIdentifierExpr siExpr = (SQLIdentifierExpr)sqlExpr ;
						BColumn.relate(parentColList, aSlctVirTable.addColumn(
								XString.trim(siExpr.getLowerName() , '`'))) ;
					}
					else if(sqlExpr instanceof SQLAllColumnExpr)
					{
						// 所有字段的映射，没指定表，断定来源表只有1个
						Assert.isTrue(aliasTableNameMap.size() == 1 , "未使用表别名作为*的前缀！") ;
						BName tableBName = XC.getFirst(aliasTableNameMap.values()) ;
						aSlctVirTable.addTablesOfIncludeAllCols(tableBName) ;
					}
					else if(sqlExpr instanceof SQLPropertyExpr)
					{
						SQLPropertyExpr expr0 = (SQLPropertyExpr)sqlExpr ;
						String name = expr0.getName() ;
						if("*".equals(name))
						{
							String tableAliasName = expr0.getOwnerName() ;
							BName tableBName = aliasTableNameMap.get(tableAliasName) ;
							aSlctVirTable.addTablesOfIncludeAllCols(tableBName) ;
						}
						else
							aSlctVirTable.addColumn(XString.trim(expr0.getName(), '`')) ;
					}
					else
						throw new IllegalStateException(String.format("表达式[%s]必需指定别名！" , sqlExpr)) ;
//					table.addColumn(item.getExpr());
				}
			}
		}
	}
	
	protected void addTable(BTable aTable)
	{
		Map<BName, BTable> map = mTL_TableCache.get() ;
		if(map != null)
			map.put(aTable.getName() , aTable) ;
		else
			mTables.put(aTable.getName().getPrefix(), aTable.getName().getLocalName(), aTable) ;
	}
	
	/**
	 * 如果数据库是区分大小写的，可以覆盖此方法，不进行大小写转换
	 * @param aName
	 * @return
	 */
	protected String toLowerCase_DbName(String aName)
	{
		return aName != null?aName.toLowerCase():null ;
	}
	
	protected String toLowerCase_TableName(String aName)
	{
		return aName != null?aName.toLowerCase():null ;
	}
	
	protected void parseFrom(SQLParseContext aCtx , SQLTableSource aFrom
			, int aCheckActions)
	{
		parseTableNames(aCtx , aFrom, aCheckActions) ;
	}
	
	protected void parseTableNames(SQLParseContext aCtx , SQLTableSource aFrom
			, int aCheckActions)
	{
		if(aFrom == null)
			return ;
		if(aFrom instanceof SQLExprTableSource)
		{
			Map.Entry<BName , String> tableEntry = getFromTableName(aCtx , (SQLExprTableSource) aFrom) ;
			if(Bits.hit(aCheckActions, ISqlBloodEngine.sCheckAction_TableHasAlias) 
					&& XString.isEmpty(tableEntry.getValue()))
			{
				throw new IllegalStateException(XString.msgFmt("表[{}]必需得有别名！", tableEntry.getKey())) ;
			}
			BName bname = aCtx.getTableName(tableEntry.getValue()) ;
			if(bname == null)
			{
				BTable tbl = getTable(tableEntry.getKey()) ;
				if(tbl == null)
					addTable(BTable.ofTable(tableEntry.getKey())) ;
				aCtx.putTableAlias(tableEntry.getValue() , tableEntry.getKey()) ;
			}
			else if(!bname.equals(tableEntry.getKey()))
			{
				throw new IllegalStateException(String.format("别名[%s]重复！" , tableEntry.getValue())) ;
			}
		}
		else if(aFrom instanceof SQLJoinTableSource)
		{
			SQLJoinTableSource from_join = (SQLJoinTableSource)aFrom ;
			parseTableNames(aCtx , from_join.getLeft() , ISqlBloodEngine.sCheckAction_TableHasAlias) ;
			parseTableNames(aCtx , from_join.getRight() , ISqlBloodEngine.sCheckAction_TableHasAlias) ;
			addTableColumnsAccordingToExpr(aCtx , from_join.getCondition() , aCheckActions) ;
		}
		else if(aFrom instanceof SQLSubqueryTableSource)
		{
			SQLSubqueryTableSource sub_select = (SQLSubqueryTableSource)aFrom ;
			BTable slctVirTbl = parseSelect(aCtx.subContext(aFrom.toString()) , sub_select.getSelect()) ;
			aCtx.putTableAlias(sub_select.getAlias() , slctVirTbl.getName()) ;
		}
		else
			throw new IllegalStateException("未支持表解析的类型："+aFrom.getClass().getName()) ;
	}
	
	protected Map.Entry<BName , String> getFromTableName(SQLParseContext aCtx , SQLExprTableSource aFrom)
	{
		SQLExpr expr = aFrom.getExpr() ;
		List<BName> nameList = extractBNames(aCtx , expr , false , null) ;
		Assert.notEmpty(nameList , "未预料到的FROM类型：%s" , aFrom.getClass().getName()) ;
		BName tableBName = nameList.get(0) ;
		if(tableBName.getPrefix() == null)
			tableBName = tableBName.prefix(aCtx.getCurrentDbName()) ;
		tableBName = tableBName.transform(this::toLowerCase_DbName , this::toLowerCase_TableName) ;
		String alias = JCommon.defaultIfNull(aFrom.getAlias() , "") ;
		return Tuples.of(tableBName , alias) ;
	}
	
	protected List<BName> extractBNames(SQLParseContext aCtx , SQLExpr aExpr , boolean aMustHavePrefix
			, Consumer<SQLExpr> aMsgThrower)
	{
		List<BName> bnameList = XC.arrayList() ;
		extractBNames(aCtx , aExpr, aMustHavePrefix , aMsgThrower, bnameList) ;
		return bnameList ;
	}
	
	protected void parseWhere(SQLParseContext aCtx , SQLExpr aExpr, int aCheckActions )
	{
		addTableColumnsAccordingToExpr(aCtx , aExpr , aCheckActions) ;
	}
	
	protected void addTableColumnsAccordingToExpr(SQLParseContext aCtx , SQLExpr aExpr 
			, int aCheckActions)
	{
		if(aExpr == null)
			return ;
		List<BName> nameList = extractBNames(aCtx , aExpr, Bits.hit(aCheckActions , sCheckAction_TableAliasPrefixField) 
				, this::throwColumnNotPrefixTableAlias) ;
		if(!nameList.isEmpty())
		{
			for(BName name : nameList)
			{
				String tableAliasName = name.getPrefix() ;
				if(tableAliasName == null)
				{
					if(aCtx.getTableAmount() > 1)
						throw new IllegalStateException(XString.msgFmt(mDefaultDBName, "多个来源表，未指定列别名，无法从SQL上分析列来自哪个表！")) ;
					else
						tableAliasName = "" ;
				}
				BName tableBName = aCtx.getTableName(tableAliasName) ;
				Assert.notNull(tableBName, "表达式[%1$s]的表别名[%2$s]没有找到对应的表名！"
						, aExpr , tableAliasName) ;
				BTable table = getTable(tableBName) ;
				Assert.notNull(table, "找不到表[%s]！", tableBName) ;
				table.addColumnIfAbsent(name.getLocalName()) ;
			}
		}
	}
	
	protected void extractBNames(SQLParseContext aCtx , SQLExpr aExpr , boolean aMustHavePrefix , Consumer<SQLExpr> aMsgThrower
			, List<BName> aResult)
	{
		if(aExpr instanceof SQLPropertyExpr)
		{
			aResult.add(new BName(((SQLPropertyExpr) aExpr).getOwnerName() 
					, ((SQLPropertyExpr) aExpr).getName())) ;
		}
		else if(aExpr instanceof SQLIdentifierExpr)
		{
			if(aMustHavePrefix)
			{
				if(aMsgThrower != null)
					aMsgThrower.accept(aExpr) ;
				return ;
			}
			aResult.add(new BName(null , XString.trim(((SQLIdentifierExpr) aExpr).getName() , '`'))) ;
		}
		else if(aExpr instanceof SQLMethodInvokeExpr)
		{
			SQLMethodInvokeExpr expr = (SQLMethodInvokeExpr)aExpr ;
			List<SQLExpr> exprList = expr.getArguments() ;
			if(XC.isNotEmpty(exprList))
			{
				for(SQLExpr sqlExpr : exprList)
					extractBNames(aCtx , sqlExpr, aMustHavePrefix, aMsgThrower, aResult) ;
			}
		}
		else if(aExpr instanceof SQLBinaryOpExpr)
		{
			SQLBinaryOpExpr expr = (SQLBinaryOpExpr)aExpr ;
			extractBNames(aCtx ,  expr.getLeft() , aMustHavePrefix , aMsgThrower , aResult) ;
			extractBNames(aCtx , expr.getRight() , aMustHavePrefix , aMsgThrower , aResult) ;
		}
		else if(aExpr instanceof SQLIntervalExpr)
		{
			// 例子： INTERVAL 1 DAY
			// 忽略
		}
		else if(aExpr instanceof SQLAllColumnExpr
				|| aExpr instanceof SQLVariantRefExpr
				|| aExpr instanceof SQLCharExpr
				|| aExpr instanceof SQLNumericLiteralExpr)
		{
			// * 忽略
			// ${XXX} 忽略
			// 'A' 忽略
		}
		else if(aExpr instanceof SQLQueryExpr)
		{
			SQLQueryExpr expr = (SQLQueryExpr)aExpr ;
			parseSelect(aCtx.subContext(expr.toString()) , expr.getSubQuery()) ;
		}
		else if(aExpr instanceof SQLInListExpr)
		{
			// field IN ('a' , 'b')
			SQLInListExpr expr = (SQLInListExpr)aExpr ;
			extractBNames(aCtx ,  expr.getExpr() , aMustHavePrefix , aMsgThrower , aResult) ;
		}
		else if(aExpr instanceof SQLBetweenExpr)
		{
			SQLBetweenExpr expr = (SQLBetweenExpr)aExpr ;
			extractBNames(aCtx , expr.getBeginExpr() , aMustHavePrefix, aMsgThrower) ;
			extractBNames(aCtx , expr.getEndExpr() , aMustHavePrefix, aMsgThrower) ;
		}
		else if(aExpr instanceof SQLCastExpr expr)
		{
			extractBNames(aCtx , expr.getExpr() , aMustHavePrefix, aMsgThrower)  ;
		}
		else
			throw new IllegalStateException("未支持的表达式类型："+aExpr.getClass().getName()) ;
	}
	
	protected void throwColumnNotPrefixTableAlias(SQLExpr aExpr)
	{
		throw new IllegalStateException(String.format("表达式[%s]中的列必需有表别名作限定！" , aExpr)) ;
	}
}
