package team.sailboat.commons.fan.jquery;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * 构造、拼接SQL语句的辅助类
 *
 * @author yyl
 * @since 2022年5月7日
 */
public interface JSqlBuilder
{
	/**
	 * 当aWhen为true时，往SQL构建器中追加字符串aSqlSeg，aArgs为SQL的查询参数值，个数与aSqlSeg中的“?”数量相同
	 * @param aWhen
	 * @param aSqlSeg
	 * @param aArgs
	 * @return
	 */
	JSqlBuilder append(boolean aWhen , String aSqlSeg , Object...aArgs) ;
	
	JSqlBuilder appendMsgFmt(boolean aWhen , String aSqlSeg , Object...aArgs) ;
	
	/**
	 * 
	 * @param aWhen
	 * @param aSqlSeg
	 * @param aVals
	 * @return
	 */
	JSqlBuilder appendIn(boolean aWhen , String aSeqlSeg , Object...aVals) ;
	
	JSqlBuilder appendIn(boolean aWhen , String aSeqlSeg , Collection<?> aVals) ;
	
	/**
	 * 
	 * @param aWhen
	 * @param aSqlSupplier
	 * @param aArgs
	 * @return
	 */
	default JSqlBuilder append(boolean aWhen , Supplier<String> aSqlSupplier , Object...aArgs)
	{
		if(aWhen)
		{
			return append(true, aSqlSupplier.get() , aArgs) ;
		}
		return this ;
	}
	
	/**
	 * 往SQL构建器中追加字符串aSqlSeg。
	 * @param aSqlSeg
	 * @return
	 */
	JSqlBuilder append(String aSqlSeg) ;
	
	/**
	 * aSqlSeg中可以有${F0}、${F1}这样的字段名占位符。此方法会检查字段名的合法性，防止代码注入
	 * @param aWhen
	 * @param aSqlSeg
	 * @param aArgs
	 * @return
	 */
	JSqlBuilder checkAppend(boolean aWhen , String aSqlSeg , Object...aArgs) ;
	
	/**
	 * 当aWhen为true时，往SQL构建器中追加"ORDER BY"语句，aArgs按照：“列名”[,"ASC"|"DESC"|true|false] ，“列名”,...这样的顺序组织，其中“[]"表示可选
	 * ，当不设置时，表示按升序排列。
	 * @param aWhen
	 * @param Args
	 * @return
	 */
	JSqlBuilder appendOrderBy(boolean aWhen , Object...Args) ;
	
	/**
	 * 替换构造出来的SQL中指定的占位符。当aCnd为true时，占位符将用aSqlSeg替换，aArgs是其动态参数。当aCnd为false时，占位符将用aElseSeg替换，aElseSeg中不能有动态参数
	 * @param aPlaceHolder
	 * @param aCnd
	 * @param aElseSeg
	 * @param aSqlSeg
	 * @param aArgs
	 * @return
	 */
	JSqlBuilder replace(String aPlaceHolder , boolean aCnd , String aElseSeg , String aSqlSeg , Object...aArgs) ;
	
	/**
	 * 取得构造出来的SQL语句
	 * @return
	 */
	String getSql() ;
	
	/**
	 * 取的参数列表	<br>
	 * 返回的List只可读，不可修改
	 * @return
	 */
	List<Object> getArgList() ;
	
	/**
	 * 取的的参数数组		<br>
	 * @return
	 */
	Object[] getArgs() ;
	
	/**
	 * 取得一个JSqlBuilder实例
	 * @return
	 */
	public static JSqlBuilder one()
	{
		return new JSqlBuilderImpl() ;
	}
	
	/**
	 * 取得一个JSqlBuilder实例			<br />
	 * aBaseSql中的参数用“?”占位，个数要和参数数量一致。		<br />
	 * 参数占位“?”的位置要符合JDBC PreparedStatement的要求		<br />
	 * @param aBaseSql			
	 * @param aArgs
	 * @return
	 */
	public static JSqlBuilder one(String aBaseSql , Object...aArgs)
	{
		return new JSqlBuilderImpl(aBaseSql , aArgs) ;
	}
}
