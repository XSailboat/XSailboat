package team.sailboat.bd.base.utils;

import java.util.Date;

import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;

import team.sailboat.bd.base.BdConst;
import team.sailboat.bd.base.hbase.HBaseUtils;
import team.sailboat.commons.fan.json.JSONArray;

/**
 * 等想清楚资源场景和界面之后再实现
 *
 * @author yyl
 * @since 2021年5月26日
 */
public class EditorTextRecycle
{
	static final byte[] sB_FN_baseInfo = HBaseUtils.toBytes(BdConst.sHBase_FN_baseInfo) ;
	
	static final byte[] sB_CN_editorId = HBaseUtils.toBytes(BdConst.sHBase_CN_editorId) ;
	static final byte[] sB_CN_version = HBaseUtils.toBytes(BdConst.sHBase_CN_version) ;
	
	/**
	 * 
	 * @param aTable
	 * @param aStartTime
	 * @param aEndTime
	 * @return
	 */
	public JSONArray getRecycleDescriptors(Table aTable , Date aStartTime , Date aEndTime
			, String aEditorId)
	{
		Scan scan = new Scan().withStartRow(HBaseUtils.toBytes(aStartTime.getTime()+"#"))
				.withStopRow(HBaseUtils.toBytes(aEndTime.getTime()+"#") , false)
				.addColumn(sB_FN_baseInfo , sB_CN_version)
				.addColumn(sB_FN_baseInfo , sB_CN_editorId) ;
		if(aEditorId != null)
		{
			
		}
		
		return null ;
	}
}
