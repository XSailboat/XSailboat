package team.sailboat.commons.fan.jquery;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

import team.sailboat.commons.fan.dtool.ColumnInfo;
import team.sailboat.commons.fan.gadget.ScrollQuerySite;
import team.sailboat.commons.fan.infc.EConsumer;
import team.sailboat.commons.fan.infc.EFunction;
import team.sailboat.commons.fan.infc.EFunction2;
import team.sailboat.commons.fan.infc.EPredicate;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;

/**
 * 
 * 在JSqlBuilder的基础上增加了查询和结果处理的接口。
 * @author yyl
 * @since 2022年5月7日
 */
public interface JQuery extends JSqlBuilder
{
	
	List<ColumnInfo> getColumnInfos(ResultSetMetaData aRsmd
			, List<ColumnInfo> aColInfos) throws SQLException ;
	
	/**
	 * 当aWhen为true时，往SQL构建器中追加字符串aSqlSeg，aArgs为SQL的查询参数值，个数与aSqlSeg中的“?”数量相同
	 * @param aWhen
	 * @param aSqlSeg
	 * @param aArgs
	 * @return
	 */
	JQuery append(boolean aWhen , String aSqlSeg , Object...aArgs) ;
	
	JQuery appendMsgFmt(boolean aWhen , String aSqlSeg , Object...aArgs) ;
	
	/**
	 * 
	 * @param aWhen
	 * @param aSqlSeg
	 * @param aVals
	 * @return
	 */
	JQuery appendIn(boolean aWhen , String aSqlSeg , Object...aVals) ;
	
	/**
	 * 
	 * @param aWhen
	 * @param aSqlSupplier
	 * @param aArgs
	 * @return
	 */
	default JQuery append(boolean aWhen , Supplier<String> aSqlSupplier , Object...aArgs)
	{
		return (JQuery) JSqlBuilder.super.append(aWhen, aSqlSupplier , aArgs) ;
	}
	
	/**
	 * aSqlSeg中可以有${F0}、${F1}这样的字段名占位符。此方法会检查字段名的合法性，防止代码注入
	 * @param aWhen
	 * @param aSqlSeg
	 * @param aArgs
	 * @return
	 */
	JQuery checkAppend(boolean aWhen , String aSqlSeg , Object...aArgs) ;
	
	/**
	 * 往SQL构建器中追加字符串aSqlSeg。
	 * @param aSqlSeg
	 * @return
	 */
	JQuery append(String aSqlSeg) ;
	
	/**
	 * 当aWhen为true时，往SQL构建器中追加"ORDER BY"语句，aArgs按照：“列名”[,"ASC"|"DESC"|true|false] ，“列名”,...这样的顺序组织，其中“[]"表示可选
	 * ，当不设置时，表示按升序排列。
	 * @param aWhen
	 * @param Args
	 * @return
	 */
	JQuery appendOrderBy(boolean aWhen , Object...Args) ;
	
	/**
	 * aMetaConsumer不为null时，表示关心分页查询信息。当执行完查询之后，aMetaConsumer的accept方法将被调用
	 * ，其参数JSONObject包含pageIndex（第几页，从0开始）,pageSize（每页条目数）,totalAmount（总数）,pageAmount（页数）信息
	 * @param aMetaConsumer
	 * @return
	 */
	default JQuery carePageQueryMeta(Consumer<JSONObject> aMetaConsumer)
	{
		return carePageQueryMeta(aMetaConsumer, true) ;
	}
	
	/**
	 * 
	 * @param aMetaConsumer
	 * @param aCareTotalAmount			是否关心总条目数，以及分页数
	 * @return
	 */
	JQuery carePageQueryMeta(Consumer<JSONObject> aMetaConsumer , boolean aCareTotalAmount) ;
	
	/**
	 * aRsmdConsumer不为null时，表示关心查询列的元信息。当执行完查询之后，aRsmdConsumer的accept方法将被调用
	 * ，其参数是ResultSetMetaData。
	 * @param aRsmdConsumer
	 * @return
	 */
	JQuery careResultSetMetadata(EConsumer<ResultSetMetaData , SQLException> aRsmdConsumer) ;
	
	/**
	 * 对返回结果进行过滤，在返回结果中仅包含接受的记录行。
	 * @param aPred
	 * @return
	 */
	JQuery filter(EPredicate<ResultSet , Throwable> aPred) ;
	
	/**
	 * 执行查询，将查询结果用JSONArray表示，JSONARRAY下面的一个JSONObject表示一行
	 * @return
	 * @throws SQLException
	 */
	JSONArray query() throws SQLException ;
	
	JSONArray query(int aAmountLimit) throws SQLException ;
	
	/**
	 * 执行分页查询，将查询结果用JSONArray表示，JSONARRAY下面的一个JSONObject表示一行。查询结果中不包含分页相关的信息。
	 * @param aPageSize
	 * @param aPage
	 * @return
	 * @throws SQLException
	 */
	JSONArray query(int aPageSize , int aPage) throws SQLException ;
	
	/**
	 * 执行查询，返回经resultFactory注册的处理器处理之后得到的对象，返回对象的实际类型由这个处理器决定。
	 * @return
	 * @throws SQLException
	 */
	Object queryCustom() throws SQLException ;
	
	Object queryCustom(int aAmountLimit) throws SQLException ;
	
	/**
	 * 执行分页查询，返回经resultFactory注册的处理器处理之后得到的对象，返回对象的实际类型由这个处理器决定。
	 * @param aPageSize
	 * @param aPage
	 * @return
	 * @throws SQLException
	 */
	Object queryPageCustom(int aPageSize , int aPage) throws SQLException ;
	
	/**
	 * 替换构造出来的SQL中指定的占位符。当aCnd为true时，占位符将用aSqlSeg替换，aArgs是其动态参数。当aCnd为false时，占位符将用aElseSeg替换，aElseSeg中不能有动态参数
	 * @param aPlaceHolder
	 * @param aCnd
	 * @param aElseSeg
	 * @param aSqlSeg
	 * @param aArgs
	 * @return
	 */
	JQuery replace(String aPlaceHolder , boolean aCnd , String aElseSeg , String aSqlSeg , Object...aArgs) ;
	
	/**
	 * 记录行转成JSONObject之后相互之间的排序器
	 * @param aComparator
	 * @return
	 */
	default JQuery resultArrayComparator(Comparator<Object> aComparator)
	{
		return resultArrayComparator(true, aComparator) ;
	}
	
	/**
	 * 记录行转成JSONObject之后相互之间的排序器
	 * @param aComparator
	 * @return
	 */
	JQuery resultArrayComparator(boolean aCnd , Comparator<Object> aComparator) ;
	
	/**
	 * 设置查询得到的数据（JSONArray形式）的处理器，处理得到的结果将作为queryCustom方法的返回结果
	 * @param aFac
	 * @return
	 */
	JQuery resultFactory(EFunction<JSONArray , Object , SQLException> aFac) ;
	
	JQuery resultFactory(EFunction2<JSONArray , QueryContext , Object , SQLException> aFac) ;
	
	/**
	 * 取得构造出来的SQL语句
	 * @return
	 */
	String getSql() ;
	
	
	JSONObject scrollQuery(int aSize , int aLifeCycleInSeconds) throws SQLException ;
	
	/**
	 * 
	 * @param aRsmd
	 * @param aColIndex		从1开始
	 * @return
	 */
	String getSchemaName(ResultSetMetaData aRsmd , int aColIndex) ;
	
	public default JSONObject scrollQuery(int aSize)  throws SQLException
	{
		return scrollQuery(aSize, 120) ;
	}
	
	public static JSONObject scrollNext(String aHandle , int aSize)
	{
		return ScrollQuerySite.getInstance().scrollNext(aHandle, aSize) ;
	}
	
//	public static JSONObject scrollNext(String aHandle , int aSize) throws Throwable 
//	{
//		DBResource dbRes = sDBResMap.remove(aHandle) ;
//		if(dbRes != null)
//		{
//			final JSONArray result = new JSONArray()
//					.put(dbRes.mNextObj) ;
//			dbRes.mNextObj = null ;
//			if(aSize<=0)
//				aSize = dbRes.mLastGetSize ;
//			else
//				dbRes.mLastGetSize = aSize ;
//			final int getSize = aSize+1 ;
//			while(result.length() < getSize && dbRes.mRS.next())
//			{
//				if(dbRes.mEPred != null)
//				{
//					try
//					{
//						if(dbRes.mEPred.test(dbRes.mRS))
//						{
//							JSONObject jobj = dbRes.mCvt.apply(dbRes.mRS) ;
//							if(dbRes.mRecordHandler != null)
//								dbRes.mRecordHandler.accept(jobj) ;
//							result.put(jobj) ;
//						}
//					}
//					catch(InterruptedException e)
//					{
//						break ;
//					}
//				}
//				else
//				{
//					JSONObject jobj = dbRes.mCvt.apply(dbRes.mRS) ;
//					if(dbRes.mRecordHandler != null)
//						dbRes.mRecordHandler.accept(jobj) ;
//					result.put(jobj) ;
//				}
//			}
//			if(result.length() == getSize)
//			{
//				dbRes.mNextObj = result.removeLast() ;
//			}
//			if(result.isNotEmpty() && dbRes.mComparator != null)
//				result.sort(dbRes.mComparator) ;
//			Object data = result ;
//			if(dbRes.mFac != null)
//				data = dbRes.mFac.apply(result) ;
//			JSONObject resultJo = new JSONObject() ;
//			if(!(data instanceof JSONObject))
//			{
//				resultJo = new JSONObject().put("data", data) ;
//			}
//			else
//			{
//				resultJo = (JSONObject)data ;
//			}
//			dbRes.mHandle = UUID.randomUUID().toString() ;
//			sDBResMap.put(dbRes.mHandle , dbRes) ;
//			return resultJo.put("hasMore" , dbRes.mNextObj != null)
//						.put("handle", dbRes.mHandle) ;
//		}
//		return null;
//	}
	
	public static JSONObject scrollNext(String aHandle) throws Throwable 
	{
		return scrollNext(aHandle, -1) ;
	}
	
	/**
	 * 把
	 * <pre>
	 * [ 
	 *   {field_1:f1 , field_2:f2 , field_3:i1 , field_4:d1} ,
	 *   {field_1:f1 , field_2:f2 , field_3:i2 , field_4:d2}
	 * ]
	 * </pre>
	 * 转成
	 * <pre>
	 * [
	 *   {
	 *     features:{
	 *       field_1: f1 ,
	 *       field_2: f2
	 *     } ,
	 *     data:[i1 , i2]
	 *   }
	 * ]
	 * </pre>
	 * 的形式
	 * @param aJa
	 * @param aIndicatorField
	 * @param aIndexFunc
	 * @param aFeatureFields
	 * @return
	 */
	public static JSONArray transposition(JSONArray aJa , String aIndicatorField , ToIntFunction<JSONObject> aIndexFunc ,  String...aFeatureFields)
	{
		Assert.notEmpty(aFeatureFields , "必需指定特征字段") ;
		final JSONArray newJa = new JSONArray() ;
		if(aJa == null)
			return newJa ;
		
		final int len = aJa.size() ;
		Map<String, JSONObject> map = new HashMap<String, JSONObject>() ;
		for(int i=0 ; i<len ; i++)
		{
			JSONObject jo = aJa.optJSONObject(i) ;
			StringBuilder keyBld = new StringBuilder() ;
			for(String featureField : aFeatureFields)
			{
				keyBld.append(jo.optString(featureField)) ;
			}
			String key = keyBld.toString() ;
			JSONObject jo1 = map.get(key) ;
			if(jo1 == null)
			{
				jo1 = new JSONObject()
						.put("features" , new JSONObject().copyFrom(jo, aFeatureFields))
						.put("data" , new JSONArray());
				map.put(key, jo1) ;
				newJa.put(jo1) ;
			}
			JSONArray ja1 = jo1.optJSONArray("data") ;
			ja1.put(aIndexFunc.applyAsInt(jo), jo.opt(aIndicatorField)) ;
		}
		return newJa ;
	}
}
