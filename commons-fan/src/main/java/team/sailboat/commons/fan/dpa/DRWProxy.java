package team.sailboat.commons.fan.dpa;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import team.sailboat.commons.fan.collection.WRHashMap;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.struct.Wrapper;
import team.sailboat.commons.fan.text.XString;

public class DRWProxy implements IDRWProxy
{
	
	final WRHashMap<String , DBean> mProxyMngBeanMap = new WRHashMap<String, DBean>() ; 
	
	final DRepository mRepo ;
	
	public DRWProxy(DRepository aRepo)
	{
		mRepo = aRepo ;
	}
	
	public <T extends DBean> T getByPrimaryKeys(Class<T> aClass , Object... aPKs) throws SQLException
	{
		DTableDesc tblDesc = DBean.getTableDesc(aClass) ;
		String key = tblDesc.getTableName()+XString.toString(",",aPKs) ;
		DBean bean = mProxyMngBeanMap.get(key) ;
		if(bean != null)
			return (T)bean ;
		
		List<ColumnMeta> cols = tblDesc.getPKColumns() ;
		if(cols.size() != aPKs.length)
		{
			throw new IllegalStateException("指定的主键参数个数与实际的主键数量不符！") ;
		}
		StringBuilder sqlBld = new StringBuilder() ;
		int i=0 ; 
		for(ColumnMeta col : cols)
		{
			if(i > 0 )
				sqlBld.append(" AND ") ;
			if(XClassUtil.sCSN_String.equalsIgnoreCase(col.getAnnotation().dataType().name()))
			{
				sqlBld.append(col.getAnnotation().name()).append("='").append(aPKs[i]).append("'") ;
			}
			else
				sqlBld.append(col.getAnnotation().name()).append("=").append(aPKs[i]).append("") ;
			i++ ;
		}
		return getFirst(aClass, sqlBld.toString()) ;
	}
	
	public <T extends DBean> T  getFirst(Class<T> aClass , String aSqlCnd) throws SQLException
	{
		DTableDesc tblDesc = DBean.getTableDesc(aClass) ;
		try(Connection conn = mRepo.mDS.getConnection()
				; Statement stm = conn.createStatement())
		{
			ResultSet rs = stm.executeQuery(XString.msgFmt("SELECT * FROM {} WHERE {} LIMIT 1 OFFSET 0" , tblDesc.getTableName() , aSqlCnd)) ;
			if(rs.next())
			{
				IDBeanFactory fac = tblDesc.getBeanFactory() ;
				DBean bean = fac.create(aClass, tblDesc, rs) ;
				String bid = DBean.getBID(bean) ;
				String key = tblDesc.getTableName()+bid ;
				DBean cachedBean = mProxyMngBeanMap.get(key) ;
				if(cachedBean == null)
				{
					bean._setRepository(mRepo) ;
					mProxyMngBeanMap.put(key, bean) ;
					return (T) bean ;
				}
				else
				{
					return (T) cachedBean ;
				}
			}
		}
		return null ;
	}
	
	@Override
	public <T extends DBean> List<T> getAll(Class<T> aClass, String aSqlCnd) throws SQLException
	{
		DTableDesc tblDesc = DBean.getTableDesc(aClass) ;
		try(Connection conn = mRepo.mDS.getConnection()
				; Statement stm = conn.createStatement())
		{
			List<T>  dataList = XC.arrayList() ;
			ResultSet rs = stm.executeQuery(XString.msgFmt("SELECT * FROM {} WHERE {}" , tblDesc.getTableName() , aSqlCnd)) ;
			while(rs.next())
			{
				IDBeanFactory fac = tblDesc.getBeanFactory() ;
				DBean bean = fac.create(aClass, tblDesc, rs) ;
				String bid = DBean.getBID(bean) ;
				String key = tblDesc.getTableName()+bid ;
				DBean cachedBean = mProxyMngBeanMap.get(key) ;
				if(cachedBean == null)
				{
					bean._setRepository(mRepo) ;
					mProxyMngBeanMap.put(key, bean) ;
					dataList.add((T)bean) ;
				}
				else
				{
					dataList.add((T) cachedBean) ;
				}
			}
			return dataList ;
		}
	}
	
	@Override
	public <T extends DBean> DPage<T> getPage(Class<T> aClass, String aSqlCnd, int aPage, int aPageSize)
			throws SQLException
	{
		DTableDesc tblDesc = DBean.getTableDesc(aClass) ;
		String sql = XString.msgFmt("SELECT * FROM {} WHERE {}" , tblDesc.getTableName() , aSqlCnd) ;
		Wrapper<JSONObject> resultWrapper = new Wrapper<JSONObject>(new JSONObject()) ;
		try(Connection conn = mRepo.mDS.getConnection())
		{
			List<T>  dataList = XC.arrayList() ;
			mRepo.mDBTool.queryPage(conn, sql , aPageSize, aPage, rs->{
				IDBeanFactory fac = tblDesc.getBeanFactory() ;
				DBean bean = fac.create(aClass, tblDesc, rs) ;
				String bid = DBean.getBID(bean) ;
				String key = tblDesc.getTableName()+bid ;
				DBean cachedBean = mProxyMngBeanMap.get(key) ;
				if(cachedBean == null)
				{
					bean._setRepository(mRepo) ;
					mProxyMngBeanMap.put(key, bean) ;
					dataList.add((T)bean) ;
				}
				else
				{
					dataList.add((T) cachedBean) ;
				}
			}, resultWrapper);
			
			return DPage.of(aClass, aPageSize, aPage, dataList , resultWrapper.get().optInteger("totalAmount")) ;
		}
	}
	
	@Override
	public <T extends DBean> void delete(Class<T> aClass, String aSqlCnd) throws SQLException
	{
		DTableDesc tblDesc = DBean.getTableDesc(aClass) ;
		String sql = XString.msgFmt("DELETE FROM {} WHERE {}" , tblDesc.getTableName() , aSqlCnd) ;
		try(Connection conn = mRepo.mDS.getConnection()
				; Statement stm = conn.createStatement())
		{
			conn.setAutoCommit(false) ;
			stm.execute(sql) ;
			conn.commit();
		}
	}
	
	@Override
	public <T extends DBean> void delete(Class<T> aClass, String aSqlCnd, IDRWTransaction aTransaction)
			throws SQLException
	{
		DTableDesc tblDesc = DBean.getTableDesc(aClass) ;
		String sql = XString.msgFmt("DELETE FROM {} WHERE {}" , tblDesc.getTableName() , aSqlCnd) ;
		try(Statement stm = aTransaction.getConnection().createStatement())
		{
			stm.execute(sql) ;
		}
	}
	
	@Override
	public IDRWTransaction beginTransaction()
	{
		try
		{
			return new DRWTransaction(mRepo.mDS.getConnection()) ;
		}
		catch (SQLException e)
		{
			WrapException.wrapThrow(e) ;
			return null ;		// dead code
		}
	}
	
	static class DRWTransaction implements IDRWTransaction
	{
		Connection mConn = null ;
		boolean mOriginalAutoCommit ;
		
		public DRWTransaction(Connection aConn) throws SQLException
		{
			mConn = aConn ;
			mOriginalAutoCommit = mConn.getAutoCommit() ;
		}
		
		@Override
		public Connection getConnection()
		{
			return mConn ;
		}
		
		@Override
		public void close()
		{
			try
			{
				mConn.commit();
			}
			catch(Exception e)
			{
				WrapException.wrapThrow(e) ;
			}
			finally
			{
				try
				{
					mConn.close();
				}
				catch(Exception e)
				{
					WrapException.wrapThrow(e) ;
				}
			}
		}
	}
}
