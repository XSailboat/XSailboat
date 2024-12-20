package team.sailboat.commons.fan.dpa;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dpa.anno.BColumn;
import team.sailboat.commons.fan.dpa.anno.BTable;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.struct.Tuples;
import team.sailboat.commons.fan.text.XString;

public class DTableDesc
{
	final Function<DBean, String> mBidGenerator ;
	
	Tuples.T2<BTable , Class<? extends DBean>> mTable ;
	
	List<ColumnMeta> mColumns ;
	
	final List<ColumnMeta> mPKCols = XC.arrayList(2) ;
	
	String mSql_insert ;
	
	String mSql_update ;
	
	String mSql_delete ;
	
	JSONObject mColumnsMeta = new JSONObject() ;
	
	IDBeanFactory mBeanFac ;

	private DTableDesc(Tuples.T2<BTable , Class<? extends DBean>> aTable 
			, List<ColumnMeta> aColumns) throws Exception
	{
		mTable = aTable ;
		mBeanFac = mTable.getEle_1().factory().getConstructor().newInstance() ;
		mColumns = aColumns ;
		Collections.sort(mColumns , (t1 , t2)->((ColumnMeta)t1).getAnnotation().seq() - ((ColumnMeta)t2).getAnnotation().seq()) ;
		String[] pkNames = mTable.getEle_1().primaryKeys() ;
		for(ColumnMeta tuple : mColumns)
		{
			if((XC.isEmpty(pkNames) && tuple.getAnnotation().primary())
					|| XC.contains(pkNames, tuple.getAnnotation().name()))
				mPKCols.add(tuple) ;
			
			String aliasName = tuple.getAnnotation().comment() ;
			if(XString.isNotEmpty(aliasName))
			{
				int i = XString.indexOf(aliasName , ',' , ' ') ;
				if(i != -1)
					aliasName = aliasName.substring(0, i) ;
				
				mColumnsMeta.put(tuple.getAnnotation().name() , new JSONObject().put("aliasName", aliasName)) ;
			}		
		}
		if(mPKCols.size() == 1)
		{
			final Field field = mPKCols.get(0).getField() ;
			mBidGenerator = (bean)->{
				try
				{
					return JCommon.toString(field.get(bean)) ;
				}
				catch (IllegalArgumentException | IllegalAccessException e)
				{
					WrapException.wrapThrow(e) ;
					return null ;
				}
			} ;
		}
		else if(mPKCols.size()>1)
		{
			mBidGenerator = (bean)->{
				StringBuilder strBld = new StringBuilder() ;
				int i=0 ;
				for(ColumnMeta tuple : mPKCols)
				{
					if(i>0)
						strBld.append(',') ;
					try
					{
						strBld.append(JCommon.toString(tuple.getSerDe().forward(bean))) ;
					}
					catch (IllegalArgumentException e)
					{
						WrapException.wrapThrow(e) ;
					}
					i++ ;
				}
				return strBld.toString() ;
			} ;
		}
		else
			mBidGenerator = null ;
		String[] posArray = new String[mColumns.size()] ;
		Arrays.fill(posArray, "?") ;
		mSql_insert = XString.msgFmt("INSERT INTO {} ({}) VALUES ({})" , mTable.getEle_1().name() 
				, XString.toString(" , " , mColumns , (col)->col.getAnnotation().name()) , XString.toString(" , ", posArray)) ;
		
		StringBuilder sqlSetterBld = new StringBuilder() ;
		StringBuilder sqlCndBld = new StringBuilder() ;
		StringBuilder sqlBld = null ; 
		for(ColumnMeta col : mColumns)
		{
			sqlBld = col.getAnnotation().primary()?sqlCndBld:sqlSetterBld ;
			if(sqlBld.length()>0)
				sqlBld.append(" , ") ;
			sqlBld.append(col.getAnnotation().name()).append('=')
				.append("?") ;	
		}
		
		mSql_update = XString.msgFmt("UPDATE {} SET {} WHERE ({})" , mTable.getEle_1().name() 
				, sqlSetterBld.toString() , sqlCndBld.toString()) ;
		
		mSql_delete = XString.msgFmt("DELETE FROM {} WHERE {}" , mTable.getEle_1().name() 
				, sqlCndBld.toString()) ;
	}
	
	public boolean isPrimaryKeyColumn(String aColName)
	{
		final int size = mPKCols.size() ;
		for(int i=0 ; i<size ; i++)
		{
			if(mPKCols.get(i).getAnnotation().name().equals(aColName))
				return true ;
		}
		return false ;
	}
	
	public String getTableName()
	{
		return mTable.getEle_1().name() ;
	}
	
	public JSONObject getColumnsMeta()
	{
		return mColumnsMeta;
	}
	
	public BTable getAnnoTable()
	{
		return mTable.getEle_1() ;
	}
	
	public Collection<BColumn> getAnnoColumns()
	{
		return mColumns.stream().map(ColumnMeta::getAnnotation).collect(Collectors.toList()) ;
	}
	
	public Collection<ColumnMeta> getColumns()
	{
		return mColumns ;
	}
	
	public List<ColumnMeta> getPKColumns()
	{
		return mPKCols ;
	}
	
	public Function<DBean, String> getBidGenerator()
	{
		return mBidGenerator;
	}
	
	public String getSql_insert()
	{
		return mSql_insert ;
	}
	
	public String getSql_update()
	{
		return mSql_update ;
	}
	
	public String getSql_delete()
	{
		return mSql_delete ;
	}
	
	public IDBeanFactory getBeanFactory()
	{
		return mBeanFac;
	}
	
	@SuppressWarnings("unchecked")
	public static DTableDesc build(Class<? extends DBean> aBeanClass)
	{
		Class<?> tblClass = aBeanClass ;
		BTable btable = tblClass.getAnnotation(BTable.class) ;
		if(btable == null)
		{
			do
			{
				tblClass = tblClass.getSuperclass() ;
				if(!Object.class.equals(tblClass) && tblClass != null)
					btable = tblClass.getAnnotation(BTable.class) ;
			}
			while(btable == null && tblClass != null  && !tblClass.equals(DBean.class)) ;
		}
		Assert.notNull(btable , "DBean的子类[%s]必须带有@BTable注解" , aBeanClass.getName()) ;
		
		List<ColumnMeta> cols = XC.arrayList() ;
		for(Field field : XClassUtil.getAllFields(tblClass))
		{
			XC.addIfNotNull(cols , ColumnMeta.as(field)) ;
		}
		try
		{
			return new DTableDesc(Tuples.of(btable , (Class<? extends DBean>)tblClass) , cols) ;
		}
		catch (Exception e)
		{
			WrapException.wrapThrow(e) ;
			return null ;		//dead code
		}
	}

}
