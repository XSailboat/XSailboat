package team.sailboat.commons.fan.jquery;

import java.sql.SQLException;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import team.sailboat.commons.fan.json.JSONObject;

/**
 * 返回的每一个记录行是JSONObject格式
 *
 * @author yyl
 * @since 2022年5月7日
 */
public interface JQueryJo extends JQuery
{	
	
	JQueryJo appendIn(boolean aWhen, String aSqlSeg, Object... aVals) ;
	
	JQueryJo append(boolean aWhen , String aSqlSeg , Object...aArgs) ;
	
	JQueryJo append(String aSqlSeg) ;
	
	/**
	 * 在返回结果中，用新列名aNewName替换旧列名aOldName
	 * @param aOldName
	 * @param aNewName
	 * @return
	 */
	JQueryJo columnNameMap(String aOldName , String aNewName) ;
	
	/**
	 * 在返回结果中，用新列名替换旧列名，aColumnNameMap的键是旧列名，值是新列名
	 * @param aColumnNameMap
	 * @return
	 */
	JQueryJo columnNameMaps(Map<String , String> aColumnNameMap) ;
	
	/**
	 * 
	 * @param aHumpFormat		true 表示去除下划线，后面紧跟着的一个字符变大写
	 * @return
	 */
	JQueryJo columnNameHumpFormat(boolean aHumpFormat) ;
	
	/**
	 * 数据查询结果的记录行转成JSONObject之后调用此函数式接口，以对这个JSONObject进行某些处理。
	 * @param aConsumer
	 * @return
	 */
	JQueryJo recordHandler(Consumer<JSONObject> aConsumer) ;
	
	JQueryJo appendOrderBy(boolean aWhen , Object...Args) ;
	
	void query(Predicate<JSONObject> aConsumerJo) throws SQLException ;
	
}
