package team.sailboat.base.data ;

import java.util.Map;
import java.util.function.Consumer;

import team.sailboat.base.dataset.IDataset;
import team.sailboat.base.def.WorkEnv;
import team.sailboat.base.ds.DataSource;
import team.sailboat.commons.fan.json.JSONObject;

/**
 * 
 * 数据提供器		<br />
 * 
 * 从数据源中查询数据
 *
 * @author yyl
 * @since 2024年12月19日
 */
public interface IDataProvider
{
	/**
	 * 从数据源中，按照数据集定义查询数据。将查询出的数据供aConsumer消费
	 * 
	 * @param aDs
	 * @param aDataset
	 * @param aParamMap
	 * @param aConsumer
	 * @throws Exception
	 */
	void consume(DataSource aDs , IDataset aDataset , Map<String, Object> aParamMap
			, Consumer<Object[]> aConsumer) throws Exception ;
	/**
	 * 从数据源中，按照数据集定义查询数据。
	 * 
	 * @param aDs
	 * @param aDataset
	 * @param aParamMap			键是参数名，值是参数值
	 * @return
	 */
	JSONObject getData(DataSource aDs , IDataset aDataset , Map<String, Object> aParamMap) throws Exception ;
	
	/**
	 * 
	 * 从指定数据源中获取指定表的一定数据量的数据，作为预览数据
	 * 
	 * @param aDataSource
	 * @param aEnv
	 * @param aTableName
	 * @return
	 * @throws Exception
	 */
	JSONObject getPreviewData(DataSource aDataSource, WorkEnv aEnv, String aTableName) throws Exception ;
}
