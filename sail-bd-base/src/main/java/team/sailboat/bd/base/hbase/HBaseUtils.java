package team.sailboat.bd.base.hbase ;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.concurrent.TimeUnit;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CompareOperator;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptor;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptorBuilder;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.serial.StreamAssist;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.fan.time.XTime;

public class HBaseUtils
{	
	static final long sTimeUpper = 9999999999999L ;
	
	public static String toString(byte[] aBytes)
	{
		return Bytes.toString(aBytes) ;
	}
	
	public static byte[] toBytes(long aVal)
	{
		return Bytes.toBytes(Long.toString(aVal)) ;
	}
	
	public static byte[] toBytes(boolean aVal)
	{
		return Bytes.toBytes(Boolean.toString(aVal)) ;
	}
	
	public static byte[] toBytes_currentTimeAsStr()
	{
		return Bytes.toBytes(XTime.current$yyyyMMddHHmmssSSS()) ;
	}
	
	public static byte[] toBytes(double aVal)
	{
		return Bytes.toBytes(Double.toString(aVal)) ;
	}
	
	public static byte[] toBytes(String aVal)
	{
		return XString.isEmpty(aVal)?null:Bytes.toBytes(aVal) ;
	}
	
	public static byte[] toBytes(Date aDate)
	{
		return toBytes(aDate == null?0:aDate.getTime()) ;
	}
	
	public static Date getDate(Result aResult , byte[] aFamilyName , byte[] aColumnName)
	{
		Long time = getLong(aResult, aFamilyName, aColumnName) ;
		return time == null || time.longValue() == 0?null:new Date(time.longValue()) ;
	}
	
	public static String getString(Result aResult , byte[] aFamilyName , byte[] aColumnName)
	{
		return getString(aResult.getColumnLatestCell(aFamilyName , aColumnName)) ;
	}
	
	public static String getColumnName(Cell aCell)
	{
		return Bytes.toString(aCell.getFamilyArray() , aCell.getFamilyOffset() , aCell.getFamilyLength())
				+ ":" + Bytes.toString(aCell.getQualifierArray() , aCell.getQualifierOffset() , aCell.getQualifierLength()) ;
	}
	
	public static String getString(Cell aCell)
	{
		return aCell == null?null:Bytes.toString(aCell.getValueArray() , aCell.getValueOffset() , aCell.getValueLength()) ;
	}
	
	public static Long getLong(Result aResult , byte[] aFamilyName , byte[] aColumnName)
	{
		String val = getString(aResult, aFamilyName, aColumnName) ;
		return XString.isNotEmpty(val)?Long.valueOf(val):null ;
	}
	
	public static Double getDouble(Result aResult , byte[] aFamilyName , byte[] aColumnName)
	{
		String val = getString(aResult, aFamilyName, aColumnName) ;
		return XString.isNotEmpty(val)?Double.valueOf(val):null ;
	}
	
	public static double getDouble(Result aResult , byte[] aFamilyName , byte[] aColumnName , double aDefaultVal)
	{
		String val = getString(aResult, aFamilyName, aColumnName) ;
		return XString.isNotEmpty(val)?Double.valueOf(val):aDefaultVal ;
	}
	
	public static Integer getInteger(Result aResult , byte[] aFamilyName , byte[] aColumnName)
	{
		String val = getString(aResult, aFamilyName, aColumnName) ;
		return XString.isNotEmpty(val)?Integer.valueOf(val):null ;
	}
	
	public static JSONArray getJSONArray(Result aResult , byte[] aFamilyName , byte[] aColumnName)
	{
		return new JSONArray(getString(aResult, aFamilyName, aColumnName)) ;
	}
	
	public static String[] getStringArray(Result aResult , byte[] aFamilyName , byte[] aColumnName)
	{
		return getJSONArray(aResult, aFamilyName, aColumnName).toStringArray() ;
	}
	
	public static boolean getBoolean(Result aResult , byte[] aFamilyName , byte[] aColumnName , boolean aDefaultValue)
	{
		String value = getString(aResult, aFamilyName, aColumnName) ;
		if(XString.isEmpty(value))
			return aDefaultValue ;
		if("true".equalsIgnoreCase(value))
			return true ;
		else if("false".equalsIgnoreCase(value))
			return false ;
		else if("1".equals(value))
			return true ;
		else if("0".equals(value))
			return false ;
		else
			throw new IllegalStateException(XString.msgFmt("无法将\"{}\"转成boolean" , value)) ;
	}
	
	public static boolean existsNamespace(Admin aAdmin , String aWsName) throws IOException
	{
		return XC.contains(aAdmin.listNamespaces() , aWsName) ;
	}
	
	public static ColumnFamilyDescriptor ofColumnFamily(String aName , int aTimeToLive , TimeUnit aTimeUnit)
	{
		return ColumnFamilyDescriptorBuilder.newBuilder(Bytes.toBytes(aName))
				.setTimeToLive((int)aTimeUnit.toSeconds(aTimeToLive))
				.build();
	}
	
	public static ColumnFamilyDescriptor ofColumnFamily(String aName , int aTimeToLive)
	{
		return ColumnFamilyDescriptorBuilder.newBuilder(Bytes.toBytes(aName))
				.setTimeToLive(aTimeToLive)
				.build();
	}
	
	public static ColumnFamilyDescriptor ofColumnFamilyWithVersions(String aName , int aVersions)
	{
		return ColumnFamilyDescriptorBuilder.newBuilder(Bytes.toBytes(aName))
				.setMaxVersions(aVersions)
				.build();
	}
	
	/**
	 * 
	 * @param aName
	 * @param aTimeToLive
	 * @param aMinVersions			即时超期了，也要保留的版本数
	 * @return
	 */
	public static ColumnFamilyDescriptor ofColumnFamily(String aName , int aTimeToLive , int aMinVersions)
	{
		return ColumnFamilyDescriptorBuilder.newBuilder(Bytes.toBytes(aName))
				.setVersionsWithTimeToLive(aTimeToLive , aMinVersions)
				.build();
	}
	
	/**
	 * 
	 * @param aResult
	 * @param aKeyContainsFN		fn和cn之间用“_”分隔
	 * @param aFamilies
	 * @return
	 */
	public static JSONObject toJSONObject(Result aResult , boolean aKeyContainsFN , String...aFamilies)
	{
		JSONObject jobj = new JSONObject() ;
		setToJSONObject(jobj, aResult , aKeyContainsFN , aFamilies) ;
		return jobj ;
	}
	
	/**
	 * 
	 * @param aJobj
	 * @param aResult
	 * @param aKeyContainsFN		fn和cn之间用“_”分隔
	 * @param aFamilies
	 */
	public static void setToJSONObject(JSONObject aJobj , Result aResult , boolean aKeyContainsFN , String...aFamilies)
	{
		NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> map = aResult.getMap() ;
		Collection<byte[]> families = map.keySet() ;
		if(XC.isNotEmpty(aFamilies))
		{
			families = XC.arrayList() ;
			for(String family : aFamilies)
				families.add(HBaseUtils.toBytes(family)) ;
		}
		for(byte[] fn : families)
		{
			NavigableMap<byte[], NavigableMap<Long, byte[]>> subMap = map.get(fn) ;
			if(subMap != null)
			{
				for(Entry<byte[],NavigableMap<Long, byte[]>> entry : subMap.entrySet())
				{
					String key = aKeyContainsFN?fn+"_"+toString(entry.getKey()):toString(entry.getKey()) ;
					aJobj.put(key , toString(entry.getValue().firstEntry().getValue())) ;
				}
			}
		}
	}
	
//	public static String scrollQuery(Table aTable , Scan aScan , int aMaxSize , Predicate<Result> aPred) throws IOException
//	{
//		try
//		{
//			ResultScanner rs = aTable.getScanner(aScan) ;
//			return _scrollQuery(aTable, rs, aMaxSize, aPred) ;
//		}
//		catch(Throwable e)
//		{
//			aTable.close();
//			throw e ;
//		}
//	}
	
//	static String _scrollQuery(Table aTable , ResultScanner aRs , int aMaxSize , Predicate<Result> aPred) throws IOException
//	{
//		int count = 0 ;
//		Iterator<Result> it = aRs.iterator() ;
//		String handle = null ;
//		while(it.hasNext())
//		{
//			if(count++>=aMaxSize)
//			{
//				handle = UUID.randomUUID().toString() ;
//				if(sResMap == null)
//					sResMap = AutoCleanHashMap.withExpired_Idle(5, true) ;
//				sResMap.put(handle, new DBResource(aTable , aRs , aMaxSize)) ;
//			}
//			if(!aPred.test(it.next()))
//				return null ;
//		}
//		return handle ;
//	}
//	
//	public static String scrollQuery(String aHandle, Predicate<Result> aPred) throws IOException
//	{
//		if(sResMap == null)
//			throw new IllegalArgumentException("不存在查询句柄："+aHandle) ;
//		
//		synchronized (aHandle.intern())
//		{
//			DBResource res = sResMap.remove(aHandle) ;
//			if(res == null)
//				throw new IllegalArgumentException("不存在查询句柄："+aHandle) ;
//			return _scrollQuery(res.mTable , res.mRs, res.mMaxSize, aPred) ;
//		}
//	}
	
	static class DBResource implements AutoCloseable
	{
		Table mTable ;
		ResultScanner mRs ;
		
		String mHandle ;
		final int mMaxSize ;
		
		public DBResource (Table aTable , ResultScanner aRs , int aMaxSize)
		{
			mTable = aTable ;
			mRs = aRs ;
			mMaxSize = aMaxSize ;
		}

		@Override
		public void close() throws Exception
		{
			StreamAssist.closeAll(mRs) ; 
		}
	}
	
	public static final long yu(long aTime)
	{
		return sTimeUpper - aTime ;
	}
	
	public static Filter filter_equals(byte[] aFamily , byte[] aColumn , String aExpectValue
			, boolean aAcceptNull)
	{
		SingleColumnValueFilter filter = new SingleColumnValueFilter(aFamily , aColumn
				, CompareOperator.EQUAL , HBaseUtils.toBytes(aExpectValue)) ;
		if(!aAcceptNull)
			filter.setFilterIfMissing(true) ;
		return filter ;
	}
}
