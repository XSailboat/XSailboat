package team.sailboat.base.data;

import java.util.List;

import team.sailboat.base.dataset.InParam;
import team.sailboat.base.def.WorkEnv;
import team.sailboat.base.ds.DataSource;
import team.sailboat.commons.fan.json.JSONObject;

public interface IRDBDataProvider extends IDataProvider
{
	/**
	 * 
	 * @param aDs
	 * @param aEnv
	 * @param aSql
	 * @param aParamMap
	 * @param aContainsMeta
	 * @param aLimitAmount
	 * @return
	 * @throws Exception
	 */
	JSONObject getData(DataSource aDs , WorkEnv aEnv , String aSql , List<InParam> aInParams
			, boolean aContainsMeta
			, int aLimitAmount) throws Exception ;
}
