package team.sailboat.bd.base.hbase;

import java.io.IOException;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Table;

import team.sailboat.commons.fan.excep.BuildFaildException;
import team.sailboat.commons.fan.res.IResourceBundle;
import team.sailboat.commons.fan.res.IResourceCreator;
import team.sailboat.commons.fan.res.ResourceStore;
import team.sailboat.commons.fan.serial.StreamAssist;

public class HBaseTableBundle implements IResourceBundle<Table>
{
	org.apache.hadoop.hbase.client.Connection mConn ;
	TableName mTableName ;
	Table mTable ;
	
	public HBaseTableBundle(org.apache.hadoop.hbase.client.Connection aConn , TableName aTableName)
	{
		mConn = aConn ;
		mTableName = aTableName ;
	}
	
	@Override
	public Table get()
	{
		return mTable ;
	}

	@Override
	public void close()
	{
		StreamAssist.close(mTable) ;
		mTable = null ;
	}

	@Override
	public boolean prepareForUse()
	{
		try
		{
			if(mTable == null)
				mTable = mConn.getTable(mTableName) ;	
			return true ;
		}
		catch (IOException e)
		{
			return false ;
		}
	}

	@Override
	public void rebuild() throws BuildFaildException
	{
		try
		{
			mTable.close();
			mTable = mConn.getTable(mTableName) ;
		}
		catch (IOException e)
		{
			mTable = null ;
			throw new BuildFaildException(e) ;
		}
	}
	
	static class HBaseTableBundleCreator implements IResourceCreator<HBaseTableBundle>
	{
		
		Connection mConn ;
		TableName mTableName ;
		
		public HBaseTableBundleCreator(Connection aConn , TableName aTableName)
		{
			mConn = aConn ;
			mTableName = aTableName ;
		}

		@Override
		public HBaseTableBundle create() throws Exception
		{
			return new HBaseTableBundle(mConn , mTableName);
		}
	}
	
	
	public static ResourceStore<HBaseTableBundle> of(Connection aConn , TableName aTableName)
	{
		return new ResourceStore<>(aTableName.getNameAsString()+"表资源仓库" 
				, new HBaseTableBundleCreator(aConn, aTableName)) ;
	}
}
