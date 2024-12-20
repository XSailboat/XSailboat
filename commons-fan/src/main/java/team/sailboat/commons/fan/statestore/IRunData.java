package team.sailboat.commons.fan.statestore;

import java.util.Map;

/**
 * 
 * 运行数据保存工具		<br />
 * 一般用在一个应用范围内，不会跨应用去并发写入。	<br />
 * 它没有域和版本，相比于IStateStore会简化很多
 *
 * @author yyl
 * @since 2024年11月20日
 */
public interface IRunData
{
	/**
     * 根据指定的键从运行数据中获取对应的字符串值。
     *
     * @param aKey 要检索的键
     * @return 如果键存在，则返回对应的值；否则返回null
     */
	String get(String aKey) ;
	
	/**
     * 根据指定的键从运行数据中获取对应的长整型值。
     * 如果键不存在，则返回默认值。
     *
     * @param aKey       要检索的键
     * @param aDefault   如果键不存在时返回的默认值
     * @return 如果键存在，则返回对应的值；否则返回aDefault
     */
	long getLong(String aKey , long aDefault) ;
	
	/**
     * 将一个长整型值存入运行数据中，与该值关联的键由参数指定。
     *
     * @param aKey   要存储的键
     * @param aValue 要存储的值
     * @throws Exception 如果在存储过程中发生异常，则抛出该异常
     */
	void put(String aKey , long aValue) throws Exception ;
	
	/**
     * 将一个字符串值存入运行数据中，与该值关联的键由参数指定。
     *
     * @param aKey   要存储的键
     * @param aValue 要存储的值
     * @throws Exception 如果在存储过程中发生异常，则抛出该异常
     */
	void put(String aKey , String aValue) throws Exception  ;
	
	
	/**
     * 将一个包含键值对的Map中的所有数据存入运行数据中。
     *
     * @param aDataMap 要存储的键值对数据
     * @throws Exception 如果在存储过程中发生异常，则抛出该异常
     */
	void putAll(Map<String, String> aDataMap) throws Exception ;
}
