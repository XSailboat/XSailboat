package team.sailboat.base.data;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.googlecode.aviator.AviatorEvaluator;

import team.sailboat.base.data.Workshop.DebugContext;
import team.sailboat.base.dataset.DatasetDescriptor;
import team.sailboat.base.dataset.IDataset;
import team.sailboat.base.dataset.IScopedDataset;
import team.sailboat.base.def.DataSourceType;
import team.sailboat.base.def.WorkEnv;
import team.sailboat.base.ds.DataSource;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.excep.ExceptionAssist;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.struct.Wrapper;
import team.sailboat.commons.fan.text.XString;

public class DataEngine implements IDataEngine
{
	final Logger mLogger = LoggerFactory.getLogger(getClass()) ;
	
	final Logger mAviatorLogger = LoggerFactory.getLogger("Aviator") ;
	
	BiFunction<String, WorkEnv , DataSource> mDataSourcePvd ;
	Map<DataSourceType , IDataProvider> mDataPvdMap = XC.hashMap() ;
	
	final Table<String , WorkEnv , Workshop> mWorkshopTbl = HashBasedTable.create() ;

	public DataEngine(BiFunction<String , WorkEnv , DataSource> aDataSourcePvd)
	{
		mDataSourcePvd = aDataSourcePvd ;
		mDataPvdMap.put(DataSourceType.MySql, new RDBDataProvider(DataSourceType.MySql)) ;
		mDataPvdMap.put(DataSourceType.MySql5, new RDBDataProvider(DataSourceType.MySql5)) ;
		mDataPvdMap.put(DataSourceType.Hive, new RDBDataProvider(DataSourceType.Hive)) ;
		mDataPvdMap.put(DataSourceType.DM , new RDBDataProvider(DataSourceType.DM)) ;
		mDataPvdMap.put(DataSourceType.TDengine , new RDBDataProvider(DataSourceType.TDengine)) ;
		mDataPvdMap.put(DataSourceType.PostgreSQL , new RDBDataProvider(DataSourceType.PostgreSQL)) ;
	}
	
	@Override
	public JSONObject getData(IDataset aDataset, Map<String, Object> aParamMap) throws Exception
	{
		return getData(aDataset, aParamMap, null) ;
	}
	
	void query(IDataset aDataset , Map<String, Object> aParamMap , String aReqId
			, ResultHandler aHandler) throws Exception
	{
		DataSource ds = mDataSourcePvd.apply(aDataset.getDataSourceId() , aDataset.getWorkEnv()) ;
		Assert.notNull(ds , "不存在id为%s的数据源！" , aDataset.getDataSourceId()) ;
		IDataProvider dataPvd = mDataPvdMap.get(ds.getType()) ;
		Assert.notNull(dataPvd , "不存在支持%s类型数据源的数据提供器！" , ds.getType().name()) ;
		Map<String, Object> ctxMap = null ;
		Workshop workshop = null ;
		DebugContext debugCtx = null ;
		if(aDataset instanceof IScopedDataset)
		{
			IScopedDataset sds = (IScopedDataset)aDataset ;
			workshop = mWorkshopTbl.get(sds.getWsId() , sds.getWorkEnv()) ;
			if(workshop == null)
			{
				synchronized ((sds.getWsId()+sds.getWorkEnv().name()).intern())
				{
					workshop = mWorkshopTbl.get(sds.getWsId() , sds.getWorkEnv()) ;
					if(workshop == null)
					{
						workshop = new Workshop(sds.getWsId() , sds.getWorkEnv()) ;
						mWorkshopTbl.put(sds.getWsId() , sds.getWorkEnv() , workshop) ;
					}
				}
			}
			if(aReqId != null)
			{
				debugCtx = Workshop.newDebugContext(aReqId) ;
			}
			ctxMap = workshop.initParamMap(ds, aDataset, aParamMap , debugCtx) ;
		}
		else
		{
			ctxMap = new Workshop(aDataset.getWorkEnv()).initParamMap(ds, aDataset, aParamMap, debugCtx) ;
		}
		aHandler.handle(dataPvd, ds, ctxMap , debugCtx) ;
	}
	
	@Override
	public void consume(IDataset aDataset, Map<String, Object> aParamMap, Consumer<Object[]> aConsume) throws Exception
	{
		query(aDataset, aParamMap, null, (dataPvd , ds , ctxMap , debugCtx)->{
			dataPvd.consume(ds, aDataset, ctxMap , aConsume) ;
		});
		
	}
	
	@Override
	public JSONObject getData(IDataset aDataset , Map<String, Object> aParamMap , String aReqId) throws Exception
	{
		Wrapper<JSONObject> resultWrapper = new Wrapper<JSONObject>() ;
		query(aDataset, aParamMap, aReqId , (dataPvd , ds , ctxMap , debugCtx)->{
			DatasetDescriptor dsDesc = aDataset.getDatasetDescriptor() ;
			JSONObject resultJo = dataPvd.getData(ds , aDataset, ctxMap) ;
			
			if(XString.isNotEmpty(dsDesc.getPostHandleExpr()))
			{
				ctxMap.put("_result_obj", resultJo) ;
				if(debugCtx != null)
					debugCtx.initIfNot();
				try
				{
					AviatorEvaluator.compile(dsDesc.getPostHandleExpr() , true).execute(ctxMap) ;
				}
				catch(Exception e)
				{
					String msg = ExceptionAssist.getStackTrace(e) ;
					mLogger.error(msg) ;
					if(debugCtx != null)
					{
						debugCtx.logDebug(msg) ;
						debugCtx.close() ;
					}
					throw e ;
				}
				if(debugCtx != null)
				{
					debugCtx.close();
				}
			}
			resultWrapper.set(resultJo) ;
		});
		return resultWrapper.get() ;
	}

	@Override
	public JSONObject getPreviewData(DataSource aDataSource, WorkEnv aEnv, String aTableName) throws Exception
	{
		IDataProvider dataPvd = mDataPvdMap.get(aDataSource.getType()) ;
		Assert.notNull(dataPvd , "不存在支持%s类型数据源的数据提供器！" , aDataSource.getType().name()) ;
		return dataPvd.getPreviewData(aDataSource , aEnv , aTableName) ;
	}
	
	@Override
	public IDataProvider getDataProvider(DataSourceType aDsType)
	{
		return mDataPvdMap.get(aDsType) ;
	}
	
	@FunctionalInterface
	static interface ResultHandler
	{
		void handle(IDataProvider dataPvd , DataSource ds , Map<String, Object> ctxMap
				, DebugContext debugCtx)
			throws Exception ;
	}
}
