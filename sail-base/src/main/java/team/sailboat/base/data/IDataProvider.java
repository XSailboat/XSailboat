package team.sailboat.base.data ;

import java.util.Map;
import java.util.function.Consumer;

import team.sailboat.base.dataset.IDataset;
import team.sailboat.base.def.WorkEnv;
import team.sailboat.base.ds.DataSource;
import team.sailboat.commons.fan.json.JSONObject;

public interface IDataProvider
{
	void consume(DataSource aDs , IDataset aDataset , Map<String, Object> aParamMap
			, Consumer<Object[]> aConsumer) throws Exception ;
	/**
	 * 
	 * @param aDs
	 * @param aDataset
	 * @param aParamMap			键是参数名，值是参数值
	 * @return
	 */
	JSONObject getData(DataSource aDs , IDataset aDataset , Map<String, Object> aParamMap) throws Exception ;
	
	JSONObject getPreviewData(DataSource aDataSource, WorkEnv aEnv, String aTableName) throws Exception ;
}
