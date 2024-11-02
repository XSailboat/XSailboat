package team.sailboat.bd.base.utils;

import java.io.IOException;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptorBuilder;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.TableDescriptorBuilder;
import org.slf4j.Logger;

import team.sailboat.bd.base.BdConst;
import team.sailboat.bd.base.hbase.HBaseUtils;
import team.sailboat.commons.fan.app.App;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.sys.XNet;
import team.sailboat.commons.fan.time.XTime;

public class AppActiveRecordHelper implements BdConst
{
	final static TableName mTN_app_activity_record = TableName.valueOf(sSysCode , sHBase_TN_app_activity_record) ;
	
	public static void makeSureTableExists(Admin aAdmin , Logger aLogger) throws IOException
	{
		if(!aAdmin.tableExists(mTN_app_activity_record))
		{
			aAdmin.createTable(TableDescriptorBuilder.newBuilder(mTN_app_activity_record)
					.setColumnFamily(HBaseUtils.ofColumnFamily(sHBase_FN_baseInfo , sHBase_TTL_app_activity_record__baseInfo , 3))
					.setColumnFamily(ColumnFamilyDescriptorBuilder.of(sHBase_FN_extendInfo))
					.setValue(sHBase_TVK_colDataType+sHBase_FN_extendInfo+":"+sHBase_CN_startTimes , XClassUtil.sCSN_Long)
					.setValue(sHBase_TVK_description , "系统应用程序活动记录")
					.build()) ;
			aLogger.info("HBase的表{}不存在，创建成功。" , mTN_app_activity_record.toString()) ;
		}
	}
	
	public static void recordAppStart(Connection aConn , Logger aLogger) throws IOException
	{
		long ts = System.currentTimeMillis() ;
		App app = App.instance() ;
		String sysEnv = app.getSysEnv() ;
		
		byte[] rowKey = HBaseUtils.toBytes(sysEnv+"#"+app.getName()) ;
		Put put = new Put(rowKey)
				.addColumn(sHBase_FNB_baseInfo , sHBase_CNB_appName , ts , HBaseUtils.toBytes(app.getName()))
				.addColumn(sHBase_FNB_baseInfo , sHBase_CNB_sysEnv , ts , HBaseUtils.toBytes(sysEnv))
				.addColumn(sHBase_FNB_baseInfo , sHBase_CNB_host , ts , HBaseUtils.toBytes(XNet.getHostName()))
				.addColumn(sHBase_FNB_baseInfo , sHBase_CNB_startTime , ts , HBaseUtils.toBytes(XTime.format$yyyyMMddHHmmssSSS(app.getStartTime() , null))) ;
		long startNum = 0 ;
		try(Table tbl = aConn.getTable(mTN_app_activity_record))
		{
			tbl.put(put);
			startNum = tbl.incrementColumnValue(rowKey, sHBase_FNB_extendInfo , sHBase_CNB_startTimes, 1) ;
		}
		app.setStartNum(startNum) ;
		aLogger.info("这是应用{}在环境{}下的第{}次启动" , app.getName() , app.getSysEnv() , startNum);
	}
}
