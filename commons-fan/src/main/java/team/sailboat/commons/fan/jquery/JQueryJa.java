package team.sailboat.commons.fan.jquery;

import java.util.function.Consumer;
import java.util.function.Supplier;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.text.XString;

/**
 * 返回的每一条记录行是一个JSONArray格式
 *
 * @author yyl
 * @since 2022年5月7日
 */
public interface JQueryJa extends JQuery
{
	
	/**
	 * 数据查询结果的记录行转成JSONArray之后调用此函数式接口，以对这个JSONArray进行某些处理。
	 * @param aConsumer
	 * @return
	 */
	JQueryJa recordHandler(Consumer<JSONArray> aConsumer) ;
	
	/**
	 * 往SQL构建器中追加字符串aSqlSeg。
	 * @param aSqlSeg
	 * @return
	 */
	JQueryJa append(String aSqlSeg) ;
	
	JQueryJa append(boolean aWhen , String aSqlSeg , Object...aArgs) ;
	
	default JQueryJa appendInts(boolean aWhen , String aSqlSeg , int...aArgs)
	{
		return append(aWhen, aSqlSeg, (Object[])XC.extract(aArgs , Integer::valueOf, Integer.class)) ;
	}
	
	JQueryJa append(boolean aWhen , Supplier<String> aSqlSeg , Object...aArgs) ;
	
	JQueryJa appendMsgFmt(boolean aWhen , String aSqlSeg , Object...aArgs) ;
	
	JQueryJa appendOrderBy(boolean aWhen , Object...Args) ;
	
	/**
	 * 
	 * @param aWhen
	 * @param aSqlSeg
	 * @param aVals
	 * @return
	 */
	JQueryJa appendIn(boolean aWhen , String aSqlSeg , Object...aVals) ;
	
	public static void makeColumnNameHumpFormat(JSONObject aMetaJo)
	{
		aMetaJo.rekeys(XString::removeUnderLine) ;
	}
}
