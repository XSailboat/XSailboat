package team.sailboat.commons.fan.dtool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.concurrent.atomic.AtomicLong;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.event.IXListener;
import team.sailboat.commons.fan.event.XListenerAssist;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.struct.Wrapper;

public class DBRecordWriter implements ICommitKit
{
	String mSql ;
	int mColAmount ;
	Connection mConn ;
	PreparedStatement mPStm ;

	Wrapper<SQLException> mExcepWrapper ;
	IPStmSetter[] mSetters ;
	
	int mCacheSize = 0 ;
	AtomicLong mAddAmount = new AtomicLong(0) ;
	int mTipCount = 0 ;
	
	Object[] mLastRow ;
	final XListenerAssist mLsnAssist = new XListenerAssist() ;
	
	int mCommitTimeoutSeconds = -1 ;
	
	public DBRecordWriter(String aSql , int...aColTypes)
	{
		mSql = aSql ;
		mColAmount = aColTypes.length ;
		mSetters = new IPStmSetter[mColAmount] ;
		for(int i=0 ; i<mColAmount ; i++)
		{
			if(aColTypes[i] == 0)
			{
				mSetters[i] = new PStmSetter_Object(i+1 , i) ;
			}
			else
			{
				switch(aColTypes[i])
				{
					case Types.VARCHAR:
					case Types.NVARCHAR:
					case Types.NCHAR:
					case Types.CHAR:
						mSetters[i] = new PStmSetter_String(i+1 , i) ;
		 				break ;
					case Types.BLOB:
					case Types.BINARY:
					case Types.VARBINARY:
						mSetters[i] = new PStmSetter_Bytes(i+1 , i) ;
						break ;
					case Types.DATE:
					case Types.TIME:
					case Types.TIMESTAMP:
						mSetters[i] = new PStmSetter_DateTime(i+1 , i , true) ;
						break ;
					case Types.BOOLEAN:
						mSetters[i] = new PStmSetter_Boolean(i+1 , i) ;
						break ;
					case Types.INTEGER:
						mSetters[i] = new PStmSetter_Integer(i+1 , i) ;
						break ;
					case Types.DECIMAL:
					case Types.NUMERIC:
					case Types.DOUBLE:
					case Types.FLOAT:
						mSetters[i] = new PStmSetter_Double(i+1 , i) ;
						break ;
					default:
						throw new IllegalStateException("还没有实现"+aColTypes[i]+"类型的PresparedStatement数据注入接口") ;
				}
			}
		}
	}
	
	public DBRecordWriter(String aSql , String...aColTypes)
	{
		this(aSql , null , aColTypes) ;
	}
	
	public void setCommitTimeoutSeconds(int aCommitTimeoutSeconds)
	{
		mCommitTimeoutSeconds = aCommitTimeoutSeconds;
	}
	
	/**
	 * 
	 * @return		返回-1表示无超时限制
	 */
	public int getCommitTimeoutSeconds()
	{
		return mCommitTimeoutSeconds;
	}
	
	/**
	 * 
	 * @param aSql
	 * @param aColAmount
	 * @param aTypes		取XClassUtils.sCSN_*
	 */
	public DBRecordWriter(String aSql , int[] aAddiArgIndex , String...aColTypes)
	{
		mSql = aSql ;
		mColAmount = aColTypes.length ;
		mSetters = new IPStmSetter[mColAmount + XC.count(aAddiArgIndex)] ;
		for(int i=0 ; i<mSetters.length ; i++)
		{
			int cellIndex = i ;
			if(i >= mColAmount)
			{
				cellIndex = aAddiArgIndex[i - mColAmount] ;
			}
			if(aColTypes[cellIndex] == null)
			{
				mSetters[i] = new PStmSetter_Object(i+1 , cellIndex) ;
			}
			else
			{
				switch(aColTypes[cellIndex])
				{
					case XClassUtil.sCSN_String:
						mSetters[i] = new PStmSetter_String(i+1 , cellIndex) ;
		 				break ;
					case XClassUtil.sCSN_Long:
						mSetters[i] = new PStmSetter_Long(i+1 , cellIndex) ;
						break ;
					case XClassUtil.sCSN_Integer:
						mSetters[i] = new PStmSetter_Integer(i+1 , cellIndex) ;
						break ;
					case XClassUtil.sCSN_Double:
						mSetters[i] = new PStmSetter_Double(i+1 , cellIndex) ;
						break ;
					case XClassUtil.sCSN_DateTime:
						mSetters[i] = new PStmSetter_DateTime(i+1 , cellIndex , true) ;
						break ;
					case XClassUtil.sCSN_Bool:
						mSetters[i] = new PStmSetter_Boolean(i+1 , cellIndex) ;
						break ;
					case XClassUtil.sCSN_Bytes:
						mSetters[i] = new PStmSetter_Bytes(i+1 , cellIndex) ;
						break ;
					default:
						throw new IllegalStateException("还没有实现"+aColTypes[cellIndex]+"类型的PresparedStatement数据注入接口") ;
				}
			}
		}
	}
	
	@Override
	public String getSql()
	{
		return mSql ;
	}
	
	/**
	 * Event里面的tag为0表示是提交动作之前的事件
	 * Event里面的tag为1表示是提交动作之后的事件
	 * @param aLsn
	 */
	public void addCommitListener(IXListener aLsn)
	{
		mLsnAssist.addListener(aLsn) ;
	}
	
	public void prepare(Connection aConn) throws SQLException
	{
		mConn = aConn ;
		mConn.setAutoCommit(false);
		mPStm = mConn.prepareStatement(mSql) ;
		mExcepWrapper = new Wrapper<>() ;
	}
	
	public Connection getConnection()
	{
		return mConn ;
	}
	
	public boolean isPrepared()
	{
		return mPStm != null ;
	}
	
	@Override
	public void setAutoCommitSize(int aAutoCommitSize)
	{
	}
	
	@Override
	public long add_0(Object...aColVals) throws SQLException
	{
		setStatement(mPStm, aColVals) ;
		mPStm.addBatch();
		++mCacheSize ;
		return mAddAmount.incrementAndGet() ;
	}
	
	@Override
	public void setStatement(PreparedStatement aPstm , Object...aColVals) throws SQLException
	{
		Assert.isTrue(mColAmount == aColVals.length) ;
		for(IPStmSetter setter : mSetters)
			setter.set(aPstm, aColVals) ;
		mLastRow = aColVals ;
	}
	
	/**
	 * 提交缓存着还没提交的数据，并且释放资源
	 * @return
	 * @throws SQLException
	 */
	public int finish() throws SQLException
	{
		mPStm.executeBatch() ;
		return mCacheSize ;
	}
}
