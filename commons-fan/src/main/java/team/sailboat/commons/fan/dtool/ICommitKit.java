package team.sailboat.commons.fan.dtool;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.fan.time.XTime;

public interface ICommitKit
{
	String getSql() ;
	
	void setAutoCommitSize(int aAutoCommitSize) ;
	
	void prepare(Connection aConn) throws SQLException ;
	
	default void add(Object...aColVals) throws SQLException
	{
		add_0(aColVals) ;
	}
	
	long add_0(Object...aColVals) throws SQLException ;
	
	void setStatement(PreparedStatement aPstm , Object...aColVals) throws SQLException ;
	
	int finish() throws SQLException ;
	
	
	
	public static interface IPStmSetter
	{
		void set(PreparedStatement aPStm , Object[] aRow) throws SQLException ;
		
		void setP(PreparedStatement aPStm , Object aCell) throws SQLException ;
	}
	
	public static class PStmSetter_Object implements IPStmSetter
	{
		int mPlaceIndex ;
		
		int mCellIndex ;
		
		public PStmSetter_Object(int aPlaceIndex , int aCellIndex)
		{
			mPlaceIndex = aPlaceIndex ;
			mCellIndex = aCellIndex ;
		}

		@Override
		public void set(PreparedStatement aPStm, Object[] aRow) throws SQLException
		{
			aPStm.setObject(mPlaceIndex, aRow[mCellIndex]) ;
		}
		
		@Override
		public void setP(PreparedStatement aPStm, Object aCell) throws SQLException
		{
			aPStm.setObject(mPlaceIndex, aCell) ;
		}
	}
	
	static class PStmSetter_String implements IPStmSetter
	{
		int mPlaceIndex ;
		
		int mCellIndex ;
		
		public PStmSetter_String(int aPlaceIndex , int aCellIndex)
		{
			mPlaceIndex = aPlaceIndex ;
			mCellIndex = aCellIndex ;
		}

		@Override
		public void set(PreparedStatement aPStm, Object[] aRow) throws SQLException
		{
			aPStm.setObject(mPlaceIndex ,  XClassUtil.toString(aRow[mCellIndex]));
		}
		
		@Override
		public void setP(PreparedStatement aPStm, Object aCell) throws SQLException
		{
			aPStm.setObject(mPlaceIndex ,  XClassUtil.toString(aCell));
		}
	}
	
	static class PStmSetter_Boolean implements IPStmSetter
	{
		int mPlaceIndex ;
		
		int mCellIndex ;
		
		public PStmSetter_Boolean(int aPlaceIndex , int aCellIndex)
		{
			mPlaceIndex = aPlaceIndex ;
			mCellIndex = aCellIndex ;
		}

		@Override
		public void set(PreparedStatement aPStm, Object[] aRow) throws SQLException
		{
			aPStm.setObject(mPlaceIndex, XClassUtil.toBoolean(aRow[mCellIndex]));
		}
		@Override
		public void setP(PreparedStatement aPStm, Object aCell) throws SQLException
		{
			aPStm.setObject(mPlaceIndex, XClassUtil.toBoolean(aCell));
		}
	}
	
	static class PStmSetter_Double implements IPStmSetter
	{
		int mPlaceIndex ;
		
		int mCellIndex ;
		
		public PStmSetter_Double(int aPlaceIndex , int aCellIndex)
		{
			mPlaceIndex = aPlaceIndex ;
			mCellIndex = aCellIndex ;
		}

		@Override
		public void set(PreparedStatement aPStm, Object[] aRow) throws SQLException
		{
			try
			{
				aPStm.setObject(mPlaceIndex, XClassUtil.toDouble(aRow[mCellIndex]));
			}
			catch(SQLException e)
			{
				throw new SQLException(String.format("序号:%1$d ， 值：%2$s" , mPlaceIndex , aRow[mCellIndex]==null?"<NULL>":aRow[mCellIndex].toString() )
						, e) ;
			}
		}
		
		@Override
		public void setP(PreparedStatement aPStm, Object aCell) throws SQLException
		{
			try
			{
				aPStm.setObject(mPlaceIndex, XClassUtil.toDouble(aCell));
			}
			catch(SQLException e)
			{
				throw new SQLException(String.format("序号:%1$d ， 值：%2$s" , mPlaceIndex , aCell==null?"<NULL>":aCell.toString() )
						, e) ;
			}
		}
	}
	
	static class PStmSetter_Integer implements IPStmSetter
	{
		int mPlaceIndex ;
		
		int mCellIndex ;
		
		public PStmSetter_Integer(int aPlaceIndex , int aCellIndex)
		{
			mPlaceIndex = aPlaceIndex ;
			mCellIndex = aCellIndex ;
		}

		@Override
		public void set(PreparedStatement aPStm, Object[] aRow) throws SQLException
		{
			aPStm.setObject(mPlaceIndex, XClassUtil.toInteger(aRow[mCellIndex])) ;
		}
		
		@Override
		public void setP(PreparedStatement aPStm, Object aCell) throws SQLException
		{
			aPStm.setObject(mPlaceIndex, XClassUtil.toInteger(aCell)) ;
		}
	}
	
	static class PStmSetter_Long implements IPStmSetter
	{
		int mPlaceIndex ;
		
		int mCellIndex ;
		
		public PStmSetter_Long(int aPlaceIndex , int aCellIndex)
		{
			mPlaceIndex = aPlaceIndex ;
			mCellIndex = aCellIndex ;
		}

		@Override
		public void set(PreparedStatement aPStm, Object[] aRow) throws SQLException
		{
			aPStm.setObject(mPlaceIndex, XClassUtil.toLong(aRow[mCellIndex])) ;
		}
		
		@Override
		public void setP(PreparedStatement aPStm, Object aCell) throws SQLException
		{
			aPStm.setObject(mPlaceIndex, XClassUtil.toLong(aCell)) ;
		}
	}
	
	static class PStmSetter_Bytes implements IPStmSetter
	{
		int mPlaceIndex ;
		
		int mCellIndex ;
		
		public PStmSetter_Bytes(int aPlaceIndex , int aCellIndex)
		{
			mPlaceIndex = aPlaceIndex ;
			mCellIndex = aCellIndex ;
		}

		@Override
		public void set(PreparedStatement aPStm, Object[] aRow) throws SQLException
		{
			Object val = aRow[mCellIndex] ;
			byte[] barray = null ;
			if(val instanceof String)
			{
				barray = XString.toBytesOfHex((String)val) ;
			}
			else
				barray = (byte[])val ;
			aPStm.setBlob(mPlaceIndex, new ByteArrayInputStream(barray)) ;
		}
		
		@Override
		public void setP(PreparedStatement aPStm, Object aCell) throws SQLException
		{
			byte[] barray = null ;
			if(aCell instanceof String)
			{
				barray = XString.toBytesOfHex((String)aCell) ;
			}
			else
				barray = (byte[])aCell ;
			aPStm.setBlob(mPlaceIndex, new ByteArrayInputStream(barray)) ;
		}
	}
	
	public static class PStmSetter_DateTime implements IPStmSetter
	{
		static final java.util.Date sLowLimit = XTime.of(0 ,1, 1) ;
		static final java.util.Date sUpLimit = XTime.of(9999, 12, 31) ;
		java.util.Date mUpLimit ;
		int mPlaceIndex ;
		
		int mCellIndex ;
		boolean mCheckDate ;
		
		/**
		 * 
		 * @param aIndex
		 * @param aCheckDate	如果为true，将检查时间范围，在公元元年1月1日至公元9999年12月31日之间认为合法
		 */
		public PStmSetter_DateTime(int aPlaceIndex , int aCellIndex , boolean aCheckDate)
		{
			mPlaceIndex = aPlaceIndex ;
			mCellIndex = aCellIndex ;
			mCheckDate = aCheckDate ;
		}

		@Override
		public void set(PreparedStatement aPStm, Object[] aRow) throws SQLException
		{
			Timestamp date = XClassUtil.toSqlDateTime(aRow[mCellIndex]) ;
			if(mCheckDate && date != null && !(date.after(sLowLimit) && date.before(sUpLimit)))
				aPStm.setTimestamp(mPlaceIndex , null) ;
			else
				aPStm.setTimestamp(mPlaceIndex, date) ;
		}
		
		@Override
		public void setP(PreparedStatement aPStm, Object aCell) throws SQLException
		{
			Timestamp date = XClassUtil.toSqlDateTime(aCell) ;
			if(mCheckDate && date != null && !(date.after(sLowLimit) && date.before(sUpLimit)))
				aPStm.setTimestamp(mPlaceIndex , null) ;
			else
				aPStm.setTimestamp(mPlaceIndex, date) ;
		}
	}
	
	public static IPStmSetter[] getPStmSetters(ParameterMetaData aPmd) throws SQLException
	{
		final int len = aPmd.getParameterCount() ;
		IPStmSetter[] setters = new IPStmSetter[len] ;
		for(int i=0 ; i<len ; i++)
		{
			int type = aPmd.getParameterType(i+1) ;
			if(type == 0)
			{
				setters[i] = new PStmSetter_Object(i+1 , i) ;
			}
			else
			{
				switch(type)
				{
					case Types.VARCHAR:
					case Types.NVARCHAR:
					case Types.NCHAR:
					case Types.CHAR:
						setters[i] = new PStmSetter_String(i+1 , i) ;
		 				break ;
					case Types.BLOB:
					case Types.BINARY:
					case Types.VARBINARY:
						setters[i] = new PStmSetter_Bytes(i+1 , i) ;
						break ;
					case Types.DATE:
					case Types.TIME:
					case Types.TIMESTAMP:
						setters[i] = new PStmSetter_DateTime(i+1 , i , true) ;
						break ;
					case Types.BOOLEAN:
					case Types.BIT:
						setters[i] = new PStmSetter_Boolean(i+1 , i) ;
						break ;
					case Types.INTEGER:
						setters[i] = new PStmSetter_Integer(i+1 , i) ;
						break ;
					case Types.DECIMAL:
					case Types.NUMERIC:
					case Types.DOUBLE:
					case Types.FLOAT:
						setters[i] = new PStmSetter_Double(i+1 , i) ;
						break ;
					default:
						throw new IllegalStateException("还没有实现"+type+"类型的PresparedStatement数据注入接口") ;
				}
			}
		}
		return setters ;
	}
	
	public static IPStmSetter[] getPStmSetters(String[] aCommonDataTypes)
	{
		IPStmSetter[] setters = new IPStmSetter[aCommonDataTypes.length] ;
		for(int i=0 ; i<aCommonDataTypes.length ; i++)
		{
			switch(aCommonDataTypes[i])
			{
				case XClassUtil.sCSN_String:
					setters[i] = new PStmSetter_String(i+1 , i) ;
	 				break ;
				case XClassUtil.sCSN_Long:
					setters[i] = new PStmSetter_Long(i+1 , i) ;
					break ;
				case XClassUtil.sCSN_Integer:
					setters[i] = new PStmSetter_Integer(i+1 , i) ;
					break ;
				case XClassUtil.sCSN_Double:
					setters[i] = new PStmSetter_Double(i+1 , i) ;
					break ;
				case XClassUtil.sCSN_DateTime:
					setters[i] = new PStmSetter_DateTime(i+1 , i , true) ;
					break ;
				case XClassUtil.sCSN_Bool:
					setters[i] = new PStmSetter_Boolean(i+1 , i) ;
					break ;
				case XClassUtil.sCSN_Bytes:
					setters[i] = new PStmSetter_Bytes(i+1 , i) ;
					break ;
				default:
					throw new IllegalStateException("还没有实现"+aCommonDataTypes[i]+"类型的PresparedStatement数据注入接口") ;
			}
		}
		return setters ;
	}
}
