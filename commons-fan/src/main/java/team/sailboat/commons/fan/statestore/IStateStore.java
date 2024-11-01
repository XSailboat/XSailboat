package team.sailboat.commons.fan.statestore;

import java.util.Collection;
import java.util.Map;

import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.struct.LongObject;

/**
 * 状态存储器
 *
 * @author yyl
 * @since 2022年9月6日
 */
public interface IStateStore
{
	/**
	 * 
	 * @param aKey
	 * @return	取不到将返回null
	 * @throws Exception
	 */
	LongObject<String> get(String aKey) throws Exception ;
	
	Map<String, LongObject<String>> getAll(String... aKeys) throws Exception ;
	
	/**
	 * 获取状态
	 * @param aPattern 		包含的字符串，中间的空格代表1个或1个以上的任意字符
	 * @param aMaxAmount
	 * @return
	 * @throws Exception
	 */
	JSONObject getRecords(String aPattern , int aMaxAmount) throws Exception ;
	
	/**
	 * 
	 * @param aKeys
	 * @return		键是domain
	 * @throws Exception
	 */
	Map<String, LongObject<String>> getAllCrossDomain(String aKey) throws Exception ;
	
	/**
	 * 如果版本小于等于0，将被视为Insert		<br />
	 * 参数里面设置的是用来校验的基础版本，实际存储的时候会在此版本基础上+1
	 * @param aMap
	 * @return		没有更新的状态量的键。返回结果不为null
	 * @throws Exception
	 */
	Collection<String> putAll_expect(Map<String, LongObject<String>> aMap) throws Exception ;
	
	boolean put(String aKey , String aValue , Long aExpectVersion) throws Exception ;
	
	/**
	 * 删除指定的状态
	 * @param aKeyVerMap		键、版本
	 */
	void removeStates(Map<String, Long> aKeyVerMap) throws Exception ;
	
	/**
	 * 清理当前任务域下面的所有状态
	 * @throws Exception
	 */
	void clear() throws Exception ;
	
	/**
	 * 缓存当前Job的所有状态数据。当任务有多实例时不能保证状态的一致性。只有一个任务时，它是安全的。
	 * @return 如果模式发生切换，将返回true，否则返回false
	 */
	boolean cacheAllAndAsSingletonInstanceMode() ;
	
	boolean isCacheAllAndAsSingletonInstanceMode() ;
}
