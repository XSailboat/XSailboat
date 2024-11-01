package team.sailboat.base.data;

import java.util.Map;
import java.util.function.Consumer;

import team.sailboat.base.dataset.IDataset;
import team.sailboat.base.def.DataSourceType;
import team.sailboat.base.def.WorkEnv;
import team.sailboat.base.ds.DataSource;
import team.sailboat.commons.fan.json.JSONObject;

public interface IDataEngine
{
	
	void consume(IDataset aDataset , Map<String, Object> aParamMap , Consumer<Object[]> aConsume) throws Exception ;
	
	JSONObject getData(IDataset aDataset , Map<String, Object> aParamMap) throws Exception ;
	
	JSONObject getData(IDataset aDataset , Map<String, Object> aParamMap , String aReqId) throws Exception ;
	
	JSONObject getPreviewData(DataSource aDataSource , WorkEnv aEnv , String aTableName) throws Exception ;
	
	IDataProvider getDataProvider(DataSourceType aDsType) ;
}
