package team.sailboat.base.metrics;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface IMetricsReader<X extends TimeObject>
{
	
	/**
	 * 
	 * @param aItem
	 * @param aAmountLimit
	 * @return		返回结果是降序排列的
	 * @throws Exception
	 */
	List<X> getLatest(String aItem , int aAmountLimit) throws Exception ;
	
	Map<String , X> getLatest(Collection<String> aItems) throws Exception ;
	
	/**
	 * 
	 * @param aItem
	 * @param aStartTime			包含
	 * @param aEndTime				不包含
	 * @return
	 * @throws Exception
	 */
	List<X> getValues(String aItem , Date aStartTime , Date aEndTime) throws Exception ;
	
	/**
	 * 
	 * @param aItem
	 * @param aStartTime
	 * @param aEndTime
	 * @param aInterval			时间间隔，1m , 7d等
	 * @param aOperator
	 * @return
	 * @throws Exception
	 */
	List<? extends TimeObject> getValues(String aItem , Date aStartTime , Date aEndTime
			, String aInterval
			, AggOperator aOperator) throws Exception ;
	
	/**
	 * 
	 * @param aItems
	 * @param aStartTime
	 * @param aEndTime
	 * @return
	 * @throws Exception
	 */
	Map<String , List<X>> getValues(String[] aItems , Date aStartTime , Date aEndTime) throws Exception ;
	
	/**
	 * 
	 * @param aItems
	 * @param aStartTime
	 * @param aEndTime
	 * @param aInterval			必需指定
	 * @param aOperator			必需指定
	 * @return
	 * @throws Exception
	 */
	Map<String , List<TimeObject>> getValues(String[] aItems , Date aStartTime , Date aEndTime
			, String aInterval
			, AggOperator aOperator) throws Exception ;
}
