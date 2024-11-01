package team.sailboat.commons.fan.eazi;

import java.io.IOException;

public interface EntryOutput
{
	EntryOutput write(String aKey , boolean aValue) throws IOException ;
	EntryOutput write(String aKey , boolean[] aArray) throws IOException ;
	
	EntryOutput write(String aKey , byte aValue) throws IOException ;
	EntryOutput write(String aKey , byte[] aArray) throws IOException ;
	
	EntryOutput write(String aKey , int aValue) throws IOException ;
	EntryOutput write(String aKey , int[] aArray) throws IOException ;
	
	EntryOutput write(String aKey , float aValue) throws IOException ;
	EntryOutput write(String aKey , float[] aArray) throws IOException ;
	
	EntryOutput write(String aKey , String aValue) throws IOException ;
	EntryOutput write(String aKey , String[] aArray) throws IOException ;
	
	/**
	 * 
	 * @param aKey
	 * @param aValue
	 * @throws IOException
	 */
	EntryOutput writeIntern(String aKey , String aValue) throws IOException ;
	
	/**
	 * 
	 * @param aKey
	 * @param aValue
	 * @throws IOException
	 */
	EntryOutput writeIntern(String aKey , String[] aValue) throws IOException ;
	
	EntryOutput write(String aKey , long aValue) throws IOException ;
	EntryOutput write(String aKey , long[] aArray) throws IOException ;
	
	EntryOutput write(String aKey , double aValue) throws IOException ;
	EntryOutput write(String aKey , double[] aArray) throws IOException ;
	
	EntryOutput write(String aKey , Eazialiable aObj) throws IOException ;
	EntryOutput write(String aKey , Eazialiable[] aObjs) throws IOException ;
	
	/**
	 * 
	 * @param aKey
	 * @param aValue
	 * <br>
	 * <dd>		
	 *   <dt>注意--Map：</dt>
	 *   <dl>
	 *   	<ul>
	 *        <li>如果aValue是Map，且键的类型不是String，那么最好不要调用此方法，而采用输出键数组和值数组的方式。
	 * 在反序列化的时候再重构Map</li>
	 * 		  <li>Map只支持HashMap，LinkedHashMap，XSimpleMap</li>
	 *        <li>SerialAssist类中有些方法为此提供支持</li>
	 *      </ul>
	 *   </dl>
	 * </dd>
	 * <dd>		
	 *   <dt>注意--List：</dt>
	 *   <dl>
	 *   	<ul>
	 * 		  <li>List只支持ArrayList，LinkedList</li>
	 *      </ul>
	 *   </dl>
	 * </dd>
	 * @throws IOException
	 */
	EntryOutput write(String aKey , Object aValue) throws IOException ;
	
	EntryOutput write(String aKey, Object[] aObjs) throws IOException ;
}
