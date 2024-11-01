package team.sailboat.commons.fan.jquery;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import javax.sql.DataSource;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dtool.ColumnInfo;
import team.sailboat.commons.fan.dtool.DBHelper;
import team.sailboat.commons.fan.dtool.IDBTool;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.infc.EConsumer;
import team.sailboat.commons.fan.infc.EFunction;
import team.sailboat.commons.fan.infc.EFunction2;
import team.sailboat.commons.fan.infc.EPredicate;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;

public abstract class JQueryBase extends JSqlBuilderImpl implements JQuery
{
	
	protected DataSource mDataSource ;
	
	protected IDBTool mDBTool ;
	
	protected List<EPredicate<ResultSet, Throwable>> mEPredList ;
	
	protected EConsumer<ResultSetMetaData , SQLException> mRsmdConsumer ;
	
	protected Comparator<Object> mComparator ;
	
	protected EFunction2<JSONArray , QueryContext , Object, SQLException> mFac ;
	
	protected Consumer<JSONObject> mPageQueryMetaConsumer ;
	
	/**
	 * 分页查询的时候，是否关心总条目数
	 */
	protected boolean mCareTotalAmount ;
	
	protected final ThreadLocal<QueryContext> mQueryContextTL = new ThreadLocal<QueryContext>() ;
	
	protected JQueryBase(DataSource aDataSource)
	{
		mDataSource = aDataSource ;
	}
	
	public List<ColumnInfo> getColumnInfos(ResultSetMetaData aRsmd
			, List<ColumnInfo> aColInfos) throws SQLException
	{
		Assert.notNull(mDBTool , "只有当JQuery执行过查询方法以后，才能调用此方法！") ;
		return mDBTool.getColumnInfos(aRsmd, aColInfos) ;
	}
	
	@Override
	public JQuery filter(EPredicate<ResultSet, Throwable> aEPred)
	{
		if(mEPredList == null)
			mEPredList = XC.arrayList() ;
		mEPredList.add(aEPred) ;
		return this ;
	}
	
	boolean doFilter(ResultSet aRs) throws Throwable
	{
		if(mEPredList == null || mEPredList.isEmpty())
			return true ;
		for(EPredicate<ResultSet, Throwable> pred : mEPredList)
		{
			if(!pred.test(aRs))
				return false ;
		}
		return true ;
	}
	
	public JQuery resultArrayComparator(boolean aCnd , Comparator<Object> aComparator)
	{
		if(aCnd)
			mComparator = aComparator ;
		return this ;
	}
	
	@Override
	public JQuery resultFactory(EFunction<JSONArray, Object, SQLException> aFac)
	{
		mFac = (ja , ctx)->aFac.apply(ja) ;
		return this ;
	}
	
	@Override
	public JQuery resultFactory(EFunction2<JSONArray, QueryContext , Object, SQLException> aFac)
	{
		mFac = aFac ;
		return this ;
	}
	
	@Override
	public JQuery careResultSetMetadata(EConsumer<ResultSetMetaData, SQLException> aRsmdConsumer)
	{
		mRsmdConsumer = aRsmdConsumer ;
		return this ;
	}
	
	@Override
	public JQuery carePageQueryMeta(Consumer<JSONObject> aMetaConsumer , boolean aCareTotalAmount)
	{
		mPageQueryMetaConsumer = aMetaConsumer ;
		mCareTotalAmount = aCareTotalAmount ;
		return this ;
	}

	@Override
	public Object queryCustom() throws SQLException
	{
		JSONArray ja = query() ;
		return mFac != null?mFac.apply(ja , mQueryContextTL.get()):ja ;
	}
	
	@Override
	public Object queryCustom(int aAmountLimit) throws SQLException
	{
		JSONArray ja = query(aAmountLimit) ;
		return mFac != null?mFac.apply(ja , mQueryContextTL.get()):ja ;
	}
	
	@Override
	public Object queryPageCustom(int aPageSize , int aPage) throws SQLException
	{
		JSONArray ja = query(aPageSize , aPage) ;
		return mFac != null?mFac.apply(ja , mQueryContextTL.get()):ja ;
	}
	
	@Override
	public JQuery append(boolean aWhen, String aSqlSeg, Object... aArgs)
	{
		return (JQuery)super.append(aWhen, aSqlSeg, aArgs);
	}
	
	@Override
	public JQuery appendMsgFmt(boolean aWhen, String aSqlSeg, Object... aArgs)
	{
		return (JQuery) super.appendMsgFmt(aWhen, aSqlSeg, aArgs);
	}
	
	@Override
	public JQuery appendIn(boolean aWhen, String aSqlSeg, Object... aVals)
	{
		return (JQuery)super.appendIn(aWhen, aSqlSeg, aVals);
	}
	
	@Override
	public JQuery append(String aSqlSeg)
	{
		return (JQuery)super.append(aSqlSeg);
	}
	
	@Override
	public JQuery checkAppend(boolean aWhen, String aSqlSeg, Object... aArgs)
	{
		return (JQuery)super.checkAppend(aWhen, aSqlSeg, aArgs);
	}
	
	@Override
	public JQuery appendOrderBy(boolean aWhen, Object... aArgs)
	{
		return (JQuery)super.appendOrderBy(aWhen, aArgs);
	}
	
	@Override
	public JQuery replace(String aPlaceHolder, boolean aCnd, String aElseSeg, String aSqlSeg, Object... aArgs)
	{
		return (JQuery)super.replace(aPlaceHolder, aCnd, aElseSeg, aSqlSeg, aArgs);
	}
	
	protected IDBTool getDBTool(Connection aConn) throws SQLException
	{
		if(mDBTool == null)
		{
			mDBTool = DBHelper.getDBTool(aConn) ;
		}
		return mDBTool ;
	}
	
	@Override
	public String getSchemaName(ResultSetMetaData aRsmd, int aColIndex)
	{
		Assert.notNull(mDBTool , "尚未执行查询，不能调用此方法！") ;
		try
		{
			return mDBTool.getDBSchemaName(aRsmd, aColIndex) ;
		}
		catch(SQLException e)
		{
			WrapException.wrapThrow(e) ;
			return null ;			// dead code
		}
	}
}
