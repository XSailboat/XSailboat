package team.sailboat.commons.fan.dpa;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.sql.DataSource;

import team.sailboat.commons.fan.collection.CLinkedHashMap;
import team.sailboat.commons.fan.collection.HashMultiMap;
import team.sailboat.commons.fan.collection.IMultiMap;
import team.sailboat.commons.fan.collection.SizeIter;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dpa.anno.BColumn;
import team.sailboat.commons.fan.dpa.anno.BFeature;
import team.sailboat.commons.fan.dpa.anno.BIndex;
import team.sailboat.commons.fan.dpa.anno.BTable;
import team.sailboat.commons.fan.dpa.anno.GenId;
import team.sailboat.commons.fan.dtool.DBHelper;
import team.sailboat.commons.fan.dtool.DBType;
import team.sailboat.commons.fan.dtool.IDBTool;
import team.sailboat.commons.fan.dtool.TableSchemaBuilder;
import team.sailboat.commons.fan.dtool.TableSchemaBuilder.ColumnBuilder;
import team.sailboat.commons.fan.dtool.TableSchemaBuilder.IndexBuilder;
import team.sailboat.commons.fan.dtool.mysql.MySQLFeatures;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.exec.CommonExecutor;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.log.Log;
import team.sailboat.commons.fan.struct.Tuples;
import team.sailboat.commons.fan.text.IDGen;
import team.sailboat.commons.fan.text.XString;

/**
 * 必需得有主键
 * @param aClass
 * @return
 * @throws SQLException
 */
public class DRepository implements Closeable
{
	int mCommitSizeLimt = 500 ;
	String mName ;
	DataSource mDS ;
	
	IDBTool mDBTool ;
	
	final Map<String, CLinkedHashMap<String, DBean>> mTableMap = XC.hashMap() ;
	
	final Map<String, DBeanCommitter> mCommitMap = XC.concurrentHashMap() ;
	
	final IMultiMap<String, Listener> mLsnMap = new HashMultiMap<>() ;
	
	final Map<String , MapIndex<? extends DBean>> mMapIndexMap = XC.hashMap() ;
	
	boolean mDisposed = false ;
	
	/**
	 * 优先级 低于 类上注解定义 更低于 代码设定
	 */
	String mGlobalIdPrefix ;
	/**
	 * id的生成策略，如果设置了，将优先于类上的注解
	 */
	final Map<Class<?>, Tuples.T2<String, String>> mIdGenStrategyMap = XC.concurrentHashMap() ;

	IDRWProxy mRWProxy ;
	
	final Set<Class<?>> mProxyClasses = XC.hashSet() ;
	
	/**
	 * 是否开启了事务
	 */
	final ThreadLocal<Transaction> mTransactionTL = new ThreadLocal<>() ;

	protected DRepository(String aName , DataSource aDS)
	{
		mName = aName ;
		mDS = aDS ;
		CommonExecutor.execInSelfThread(()->{
			final List<Tuples.T2<DOper, Object[]>> operList = XC.arrayList(mCommitSizeLimt) ;
			long lastTime = System.currentTimeMillis() ;
			while(!mDisposed)
			{
				long current = System.currentTimeMillis() ;
				long diff = current - lastTime ;
				if(diff<2000)
					JCommon.sleep((int)Math.max(100 , diff)) ;
				lastTime = System.currentTimeMillis() ;
				DBeanCommitter[] committers = mCommitMap.values().toArray(new DBeanCommitter[0]) ;
				if(XC.isNotEmpty(committers))
				{
					for(DBeanCommitter committer : committers)
					{
						DOper lastOper = null ;
						DBean[] beans = committer.getAllBeans() ;
						if(XC.isNotEmpty(beans))
						{
							DTableDesc tblDesc = DBean.getTableDesc(committer.mClass) ;
							for(DBean bean : beans)
							{
								Tuples.T2<DOper, Object[]> operManual = bean.flush() ;
								if(!(lastOper == null || lastOper == operManual.getEle_1() || operList.size()>=mCommitSizeLimt))
								{
									//先执行
									commitOpers(lastOper, tblDesc, operList) ;
									lastOper = null ;
								}
								if(operManual != null)
								{
									lastOper = operManual.getEle_1() ;
									operList.add(operManual) ;
								}
							}
							if(!operList.isEmpty())
								commitOpers(lastOper, tblDesc, operList) ;
						}
					}
				}
			}
		}, mName , "DRepository自动提交");
		
		mRWProxy = new DRWProxy(this) ;
	}
	
	private void commitOpers(DOper aOper ,DTableDesc aTblDesc  ,List<Tuples.T2<DOper, Object[]>> aOperList )
	{
		//先执行
		switch(aOper)
		{
		case INSERT :
			commit_multiTry(aTblDesc.getSql_insert() , aOperList) ;
			break ;
		case UPDATE:
			commit_multiTry(aTblDesc.getSql_update() , aOperList) ;
			break ;
		case DELETE:
			commit_multiTry(aTblDesc.getSql_delete() , aOperList) ;
			break ;
		}
		aOperList.clear();
	}
	
	private void commitOpers(DOper aOper ,DTableDesc aTblDesc  ,List<Tuples.T2<DOper, Object[]>> aOperList
			, Connection aConn) throws SQLException
	{
		//先执行
		switch(aOper)
		{
		case INSERT :
			commit(aTblDesc.getSql_insert() , aOperList , aConn) ;
			break ;
		case UPDATE:
			commit(aTblDesc.getSql_update() , aOperList , aConn) ;
			break ;
		case DELETE:
			commit(aTblDesc.getSql_delete() , aOperList , aConn) ;
			break ;
		}
		aOperList.clear();
	}
	
	private void commit_multiTry(String aSql , List<Tuples.T2<DOper, Object[]>> aOperList)
	{
		try
		{
			commit(aSql, aOperList) ;
		}
		catch(SQLException e)
		{
			JCommon.sleepInSeconds(10) ;
			try
			{
				commit(aSql, aOperList);
			}
			catch (SQLException e1)
			{
				Log.error(DRepository.class , e) ;
			}
		}
		
	}
	
	private void commit(String aSql , List<Tuples.T2<DOper, Object[]>> aOperList , Connection aConn) throws SQLException
	{
		try(PreparedStatement pstm = aConn.prepareStatement(aSql))
		{
			for(Tuples.T2<DOper, Object[]> oper : aOperList)
			{
				int i=0 ;
				for(Object pval : oper.getEle_2())
				{
					if(pval != null && java.util.Date.class.equals(pval.getClass()))
					{
						// 有的jdbc驱动是不支持java.utils.Date类型对象通过setObject方式设置进去的
						pstm.setObject(++i , new Timestamp(((java.util.Date)pval).getTime())) ;
					}
					else
						pstm.setObject(++i , pval) ;
				}
				pstm.addBatch(); 
			}
			pstm.executeBatch() ;
		}
	}
	
	private void commit(String aSql , List<Tuples.T2<DOper, Object[]>> aOperList) throws SQLException
	{
		try(Connection conn = mDS.getConnection())
		{
			conn.setAutoCommit(false) ;
			commit(aSql, aOperList, conn) ;
			conn.commit();
		}
	}
	
	public void setIdGenStrategy(Class<?> aClass , String aCategory , String aPrefix)
	{
		mIdGenStrategyMap.put(aClass , Tuples.of(aCategory , aPrefix)) ;
	}
	
	public void setGlobalIdPrefix(String aPrefix)
	{
		mGlobalIdPrefix = aPrefix ;
	}
	
	/**
	 * 代理这些类的接口查询
	 * @param aClasses
	 * @return
	 * @throws SQLException 
	 */
	public DRepository proxy(Class<? extends DBean>... aClasses) throws SQLException
	{
		if(XC.isNotEmpty(aClasses))
		{
			for(Class<? extends DBean> clazz : aClasses)
			{
				if(mProxyClasses.add(clazz))
				{
					String key = getKey(clazz) ;
					Assert.isNotTrue(mTableMap.containsKey(key) , "%s已经加载，不能试图代理这些类查询" , key) ;
					
					DTableDesc tblDesc = DBean.getTableDesc(clazz) ;
					String tableName = tblDesc.getAnnoTable().name() ;
					//1.检查表是否存在
					try(Connection conn = mDS.getConnection())
					{
						if(mDBTool == null)
							mDBTool = DBHelper.getDBTool(conn) ;
						if(mDBTool.isTableExists(conn, tableName , null))
						{
							// 不加载数据
						}
						else
						{
							//表不存在，创建表
							createDBTable(tblDesc, conn) ;
						}
					}
				}
			}
		}
		return this ;
	}
	
	void createDBTable(DTableDesc aTblDesc , Connection aConn) throws SQLException
	{
		BTable tblInfo = aTblDesc.getAnnoTable() ;
		TableSchemaBuilder tblSchemaBld = mDBTool.builder_tableSchema()
					.name(tblInfo.name())
					.comment(tblInfo.comment()) ;
		List<String> primaryCols = XC.arrayList() ;
		for(BColumn colInfo : aTblDesc.getAnnoColumns())
		{
			String dataType = colInfo.dataType().name() ;
			Object param = null ;
			if(XClassUtil.sCSN_DateTime.equals(dataType))
			{
				if(colInfo.dataType().dateTimeOnCreate())
					param = "on_create" ;
				else if(colInfo.dataType().dateTimeOnUpdate())
					param = "on_update" ;
			}
			else if(colInfo.dataType().length()>0)
				param = colInfo.dataType().length() ;
			ColumnBuilder colBld = tblSchemaBld.column(colInfo.name())
					.dataType(dataType ,param)
					.comment(colInfo.comment()) ;
			if(XString.isNotEmpty(colInfo.defaultValue()))
				colBld.defaultValue(colInfo.defaultValue()) ;
			
			if(aTblDesc.isPrimaryKeyColumn(colInfo.name()))
			{
				primaryCols.add(colInfo.name()) ;
				// 大小写敏感
				if(XClassUtil.sCSN_String.equals(dataType))
					colBld.featureFor(MySQLFeatures.COLUMN__COLLATION , "utf8_bin", DBType.MySQL) ;
			}
			
			// 必需
			colBld.and() ;
		}
		tblSchemaBld.withPrimaryKey(primaryCols.toArray(JCommon.sEmptyStringArray)) ;
		BFeature[] features = tblInfo.features() ;
		if(XC.isNotEmpty(features))
		{
			for(BFeature feature : features)
				tblSchemaBld.featureFor(feature.name(), feature.value(), feature.type()) ;
		}
		BIndex[] indexes = tblInfo.indexes() ;
		if(XC.isNotEmpty(indexes))
		{
			for(BIndex index : indexes)
			{
				IndexBuilder indexBld = tblSchemaBld.index(index.name()).on(index.columns()) ;
				if(index.unique())
					indexBld.unique() ;
			}
		}
		mDBTool.createTables(aConn , tblSchemaBld.build()) ;
	}
	
	public IDRWProxy getRWProxy()
	{
		return mRWProxy ;
	}
	
	public DRepository loadAsync(Class<? extends DBean>... aClasses)
	{
		CommonExecutor.exec(()->{
			for(Class<? extends DBean> clazz : aClasses)
			{
				try
				{
					load(clazz) ;
				}
				catch (SQLException e)
				{
					Log.error(DRepository.class ,  e) ;
				}
			}
		}) ;
		return this ;
	}
	
	public DRepository load(Class<? extends DBean> aClass) throws SQLException
	{
		String key = getKey(aClass) ;
		Assert.isNotTrue(mTableMap.containsKey(key) , "%s已经加载，不能试图重复加载" , key) ;
		
		DTableDesc tblDesc = DBean.getTableDesc(aClass) ;
		String tableName = tblDesc.getAnnoTable().name() ;
		mTableMap.put(key, new CLinkedHashMap<>()) ;
		//1.检查表是否存在
		try(Connection conn = mDS.getConnection())
		{
			if(mDBTool == null)
				mDBTool = DBHelper.getDBTool(conn) ;
			if(mDBTool.isTableExists(conn, tableName , null))
			{
				//检查表的定义是否和声明的一致
				//----待实现
				//
				//加载数据
				CLinkedHashMap<String, DBean> map = mTableMap.get(key) ;
				IDBeanFactory fac = tblDesc.getBeanFactory() ;
				DBHelper.executeQuery(conn, "SELECT * FROM "+tableName , (rs)->{
					DBean bean = fac.create(aClass, tblDesc, rs) ;
					map.put(DBean.getBID(bean) , bean) ;
					bean._setRepository(this) ;
				});
			}
			else
			{
				//表不存在，创建表
				createDBTable(tblDesc, conn) ;
			}
		}
			
		return this ;
	}
//	
//	protected DBean build(Class<? extends DBean> aClass , DTableDesc aTblDesc , ResultSet aRs) throws SQLException
//	{
//		try
//		{
//			DBean bean = newBean(aClass) ;
//			for(Tuples.T2<BColumn, Field> col : aTblDesc.getColumns())
//			{
//				col.getEle_2().set(bean, XClassUtil.typeAdapt(aRs.getObject(col.getEle_1().name()) , col.getEle_2().getType())) ;
//			}
//			bean.setLoaded();
//			return bean ;
//		}
//		catch (Exception e)
//		{
//			WrapException.wrapThrow(e);
//			return null ;			//dead code
//		}
//	}
	
	/**
	 * 开启线程内的事务
	 */
	public void beginTLTransaction()
	{
		mTransactionTL.set(new Transaction()) ;
	}
	
	public Transaction setTLTransaction(Transaction aTransaction)
	{
		Transaction originalTrans = mTransactionTL.get() ;
		mTransactionTL.set(aTransaction) ;
		return originalTrans ;
	}
	
	public void endTLTransaction(Transaction aTrans , Connection aConn) throws SQLException
	{
		DBeanCommitter[] committers = aTrans.mCommitMap.values().toArray(new DBeanCommitter[0]) ;
		if(committers.length > 0)
		{
			final List<Tuples.T2<DOper, Object[]>> operList = XC.arrayList(mCommitSizeLimt) ;
			if(XC.isNotEmpty(committers))
			{
				for(DBeanCommitter committer : committers)
				{
					DOper lastOper = null ;
					DBean[] beans = committer.getAllBeans() ;
					if(XC.isNotEmpty(beans))
					{
						DTableDesc tblDesc = DBean.getTableDesc(committer.mClass) ;
						for(DBean bean : beans)
						{
							Tuples.T2<DOper, Object[]> operManual = bean.flush() ;
							if(!(lastOper == null || lastOper == operManual.getEle_1() || operList.size()>=mCommitSizeLimt))
							{
								//先执行
								commitOpers(lastOper, tblDesc, operList , aConn) ;
								lastOper = null ;
							}
							if(operManual != null)
							{
								lastOper = operManual.getEle_1() ;
								operList.add(operManual) ;
							}
						}
						if(!operList.isEmpty())
							commitOpers(lastOper, tblDesc, operList , aConn) ;
					}
				}
			}
		}
	}
	
	/**
	 * 终止事务，提交数据
	 * @throws SQLException 
	 */
	public void endTLTransaction(boolean aCommit) throws SQLException
	{
		Transaction trans = mTransactionTL.get() ;
		mTransactionTL.remove() ;
		DBeanCommitter[] committers = trans.mCommitMap.values().toArray(new DBeanCommitter[0]) ;
		if(aCommit)
		{
			final List<Tuples.T2<DOper, Object[]>> operList = XC.arrayList(mCommitSizeLimt) ;
			if(XC.isNotEmpty(committers))
			{
				try(Connection conn = mDS.getConnection())
				{
					conn.setAutoCommit(false) ;
					try
					{
						for(DBeanCommitter committer : committers)
						{
							DOper lastOper = null ;
							DBean[] beans = committer.getAllBeans() ;
							if(XC.isNotEmpty(beans))
							{
								DTableDesc tblDesc = DBean.getTableDesc(committer.mClass) ;
								for(DBean bean : beans)
								{
									Tuples.T2<DOper, Object[]> operManual = bean.flush() ;
									if(!(lastOper == null || lastOper == operManual.getEle_1() || operList.size()>=mCommitSizeLimt))
									{
										//先执行
										commitOpers(lastOper, tblDesc, operList , conn) ;
										lastOper = null ;
									}
									if(operManual != null)
									{
										lastOper = operManual.getEle_1() ;
										operList.add(operManual) ;
									}
								}
								if(!operList.isEmpty())
									commitOpers(lastOper, tblDesc, operList) ;
							}
						}
						conn.commit();
					}
					catch(Exception e)
					{
						conn.rollback();
						// 2023-08-22 待实现，恢复bean原来的状态
						throw e ;
					}
				}
			}
			return ;
		}
		else
		{
			// 2023-08-22 待实现，恢复bean原来的状态
			
		}
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends DBean> T newBean(Class<T> aClass ) throws Exception
	{
		return (T)XClassUtil.newInstance(aClass) ;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends DBean> T newBeanIdPrefix(Class<T> aBeanClass , String aIdPrefix)
	{
		Class<? extends DBean> tblClass = aBeanClass ;
		String key = getKey(tblClass) ;
		CLinkedHashMap<String, DBean> map = mTableMap.get(key) ;
		if(map == null)
		{
			do
			{
				tblClass = (Class<? extends DBean>) tblClass.getSuperclass() ;
				key = getKey(tblClass) ;
				map = mTableMap.get(key) ;
			}
			while(map == null && tblClass != null  && !tblClass.equals(DBean.class)) ;
		}
		Assert.notNull(map, "%s对应的表未load，不能创建bean", aBeanClass.getName()) ;
		try
		{
			T bean =  null ;
			DTableDesc tblDesc = DBean.getTableDesc((Class<? extends DBean>) tblClass) ;
			Collection<ColumnMeta> pkCols = tblDesc.getPKColumns() ;
			//自动生成id，先检查一下有这样一个主键字段
			Assert.isTrue(pkCols.size() == 1 , "主键数量为%d，大于1，必须指定主键") ;
			String category = null ;
			ColumnMeta col = XC.getFirst(pkCols) ;
			Tuples.T2<String, String> strategy = mIdGenStrategyMap.get(aBeanClass) ;
			if(strategy != null)
			{
				category = strategy.getEle_1() ;
			}
			else
			{
				BTable atbl = tblDesc.getAnnoTable() ;
				GenId genId =  null ;
				String c = atbl.id_category() ;
				if(XString.isNotEmpty(c))
				{
					category = c ;
				}
				else
				{
					genId = col.getField().getAnnotation(GenId.class) ;
					if(genId != null)
					{
						category = genId.category() ;
					}
					else
						category = null ;
				}
				String id = IDGen.newID(category , aIdPrefix) ;
				bean = newBean(aBeanClass) ;
				col.getField().set(bean , id) ;
			}
			map.put(DBean.getBID(bean), bean) ;
			bean._setRepository(this) ;
			addCommit(bean) ;
			return bean ;
		}
		catch (Exception e)
		{
			WrapException.wrapThrow(e) ;
			return null ;			// dead code
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends DBean> T newBean(Class<T> aBeanClass , Object...aPKs)
	{
		Class<? extends DBean> tblClass = aBeanClass ;
		T bean = null ;
		if(mProxyClasses.contains(aBeanClass))
		{
			bean = createBean(aBeanClass, aPKs) ;
		}
		else
		{
			String key = getKey(tblClass) ;
			CLinkedHashMap<String, DBean> map = mTableMap.get(key) ;
			if(map == null)
			{
				do
				{
					tblClass = (Class<? extends DBean>) tblClass.getSuperclass() ;
					key = getKey(tblClass) ;
					map = mTableMap.get(key) ;
				}
				while(map == null && tblClass != null  && !tblClass.equals(DBean.class)) ;
			}
			Assert.notNull(map, "%s对应的表未load，不能创建bean", aBeanClass.getName()) ;
			bean = createBean(aBeanClass, aPKs) ;
			if(bean != null)
				map.put(DBean.getBID(bean), bean) ;
		}
		return bean ;
	}
	
	<T extends DBean> T createBean(Class<T> aBeanClass, Object...aPKs)
	{
		try
		{
			T bean =  null ;
			DTableDesc tblDesc = DBean.getTableDesc((Class<? extends DBean>)aBeanClass) ;
			Collection<ColumnMeta> pkCols = tblDesc.getPKColumns() ;
			if(XC.isEmpty(aPKs))
			{
				//自动生成id，先检查一下有这样一个主键字段
				int pkAmount = pkCols.size() ;
				Assert.isTrue(pkAmount == 1 , "主键数量为%d，大于1，必须指定主键" , pkAmount) ;
				String category = null ;
				String prefix = mGlobalIdPrefix ;
				ColumnMeta col = XC.getFirst(pkCols) ;
				Tuples.T2<String, String> strategy = mIdGenStrategyMap.get(aBeanClass) ;
				if(strategy != null)
				{
					category = strategy.getEle_1() ;
					prefix = strategy.getEle_2() ;
				}
				else
				{
					BTable atbl = tblDesc.getAnnoTable() ;
					GenId genId =  null ;
					String c = atbl.id_category() ;
					if(XString.isNotEmpty(c))
					{
						category = c ;
					}
					else
					{
						genId = col.getField().getAnnotation(GenId.class) ;
						if(genId != null)
						{
							category = genId.category() ;
						}
						else
							category = null ;
					}
					String p = atbl.id_prefix() ;
					if(XString.isNotEmpty(p))
						prefix = p ;
					else
					{
						if(genId == null)
							genId = col.getField().getAnnotation(GenId.class) ;
						if(genId != null && XString.isNotEmpty(genId.prefix()))
							prefix = genId.prefix() ;
					}
				}
				String id = IDGen.newID(category , prefix) ;
				bean = newBean(aBeanClass) ;
				col.getField().set(bean , id) ;
			}
			else
			{
				//
				int pcount = XC.count(aPKs) ;
				if(pkCols.size() == pcount)
				{
					bean = newBean(aBeanClass) ;
					int i=0 ;
					for(ColumnMeta col : pkCols)
						col.getField().set(bean , aPKs[i++]) ;
				}
				else if(pkCols.size() *2 == pcount)
				{
					Map<String, Field> fieldMap = new HashMap<String, Field>() ;
					for(ColumnMeta col : pkCols)
						fieldMap.put(col.getAnnotation().name(), col.getField()) ;
					for(int i=0 ; i<pcount ; i+=2)
					{
						Field field = fieldMap.get(aPKs[i]) ;
						Assert.notNull(field, "不存在表的主键列名%s对应的字段", aPKs[i]) ;
						field.set(bean, aPKs[i+1]);
					}
				}
				else
					throw new IllegalArgumentException(XString.msgFmt("参数和主键数量不一致。主键数量是{} ，参数数量是{}" , pkCols.size() , pcount)) ;
				
			}
//			map.put(DBean.getBID(bean), bean) ;
			bean._setRepository(this) ;
			addCommit(bean) ;
			return bean ;
		}
		catch (Exception e)
		{
			WrapException.wrapThrow(e) ;
			return null ;			// dead code
		}
	}
	
	void removeDirectly(DBean aBean)
	{
		String name = getKey(aBean.getClass()) ;
		CLinkedHashMap<String , DBean> map = mTableMap.get(name) ;
		if(map != null)
		{
			map.remove(DBean.getBID(aBean)) ;
			aBean._setRepository(null) ;
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends DBean> T getByBid(Class<T> aClass , String aBid)
	{
		String key = getKey(aClass) ;
		CLinkedHashMap<String , DBean> map =  mTableMap.get(key) ;
		if(map != null)
			return (T)map.get(aBid) ;
		else if(mProxyClasses.contains(aClass))
		{
			try
			{
				return getRWProxy().getByPrimaryKeys(aClass, aBid) ;
			}
			catch (SQLException e)
			{
				WrapException.wrapThrow(e) ;
				return null ;			// dead code
			}
		}
		else
			throw new IllegalArgumentException(String.format("类%s对应的表未装载到此贮存器中" , aClass.getName())) ;
	}
	
	public <T extends DBean> T getOrCreateByBid(Class<T> aClass , String aBid)
	{
		T bean = getByBid(aClass, aBid) ;
		if(bean == null)
			return newBean(aClass, aBid) ;
		return bean ;
	}
	
	public <T extends DBean> int getSize(Class<T> aClass)
	{
		String key = getKey(aClass) ;
		CLinkedHashMap<String , DBean> map =  mTableMap.get(key) ;
		if(map != null)
			return map.size() ;
		else
			throw new IllegalArgumentException(String.format("类%s对应的表未装载到此贮存器中" , aClass.getName())) ;
	}
	
	/**
	 * 
	 * @param <T>
	 * @param aClass
	 * @param aPred		返回false，终止遍历
	 */
	public <T extends DBean> void forEach(Class<T> aClass , Predicate<T> aPred)
	{
		String key = getKey(aClass) ;
		@SuppressWarnings("unchecked")
		CLinkedHashMap<String , T> map =  (CLinkedHashMap<String, T>) mTableMap.get(key) ;
		if(map != null)
			map.forEachValues(aPred) ;
	}
	
	/**
	 * 
	 * @param <T>
	 * @param aClass
	 * @param aPageSize
	 * @param aPage				页码，序号从0开始
	 * @return
	 */
	public <T extends DBean> DPage<T> getPage(Class<T> aClass , int aPageSize , int aPage)
	{
		@SuppressWarnings("unchecked")
		CLinkedHashMap<String, T> map = (CLinkedHashMap<String, T>) mTableMap.get(getKey(aClass)) ;
		if(map != null)
		{
			List<Entry<String, T>> list = map.getN(aPageSize*aPage , aPageSize) ;
			return DPage.of(aClass, aPageSize, aPage, Tuples.T2.getValues(list), map.size()) ;
		}
		return null ;
	}
	
	public <T extends DBean> T getFirst(Class<T> aClass)
	{
		@SuppressWarnings("unchecked")
		CLinkedHashMap<String, T> map = (CLinkedHashMap<String, T>) mTableMap.get(getKey(aClass)) ;
		if(map != null)
		{
			return map.getFirst() ;
		}
		return null ;
	}
	
	/**
	 * 性质是一个对象，部分信息修改，不方便提供旧值
	 * @param aBean
	 * @param aPropertyName
	 * @param aNewVal
	 */
	void _propertyChanged(DBean aBean , String aPropertyName , Object aNewVal)
	{
		_propertyChanged(aBean , aPropertyName , new Object[] {aPropertyName , aNewVal}) ;
	}
	
	void _propertyChanged(DBean aBean , String aPropertyName , Object aNewVal , Object aOldVal)
	{
		_propertyChanged(aBean, aPropertyName, new Object[] {aPropertyName , aNewVal , aOldVal}) ;
	}
	
	void _propertyChanged(DBean aBean , String aPropertyName , Object[] aParams)
	{
		String lsnKey = aBean.getClass().getName()+"."+aPropertyName+"#change" ;
		SizeIter<Listener> lsns = mLsnMap.get(lsnKey) ;
		if(lsns != null)
		{
			Event event = new Event(DEventType.PROPERTY_CHANGE , aBean , aParams) ;
			for(Listener lsn : lsns)
				lsn.handle(event);
		}
	}
	
	void beanCreated(DBean aBean)
	{
		String lsnKey = aBean.getClass().getName()+"#create" ;
		SizeIter<Listener> lsns = mLsnMap.get(lsnKey) ;
		if(lsns != null)
		{
			Event event = new Event(DEventType.BEAN_CREATE , aBean) ;
			for(Listener lsn : lsns)
				lsn.handle(event);
		}
	}
	
	void beanDeleted(DBean aBean)
	{
		String lsnKey = aBean.getClass().getName()+"#delete" ;
		SizeIter<Listener> lsns = mLsnMap.get(lsnKey) ;
		if(lsns != null)
		{
			Event event = new Event(DEventType.BEAN_DELETE , aBean) ;
			for(Listener lsn : lsns)
				lsn.handle(event);
		}
	}
	

	public void track(Listener aLsn , Class<? extends DBean> aClass , String... aFieldNames)
	{
		String name = aClass.getName() ;
		String[] lsnKeys ;
		if(XC.isNotEmpty(aFieldNames))
		{
			int len = aFieldNames.length ;
			lsnKeys = new String[len] ;
			for(int i=0 ; i<len ; i++)
				lsnKeys[i] = name+"."+aFieldNames[i]+"#change" ;
		}
		else
		{
			lsnKeys = new String[] {name+"#create" , name+"#delete"} ;	
		}
		for(String lsnKey : lsnKeys)
		{
			mLsnMap.put(lsnKey, aLsn) ;
		}
	}
	
	public <T extends DBean> MapIndex<T> mapIndex(Class<T> aClass , String aFieldName)
	{
		return mapIndex(aClass, aFieldName , null , null) ;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends DBean> MapIndex<T> mapIndex(Class<T> aClass , String aFieldName 
			, String aFuncTag
			, Function<Object, String> aFunc)
	{
		String key = aClass.getName()+"."+aFieldName ;
		if(XString.isNotEmpty(aFuncTag))
		{
			key += "-"+aFuncTag ;
		}
		MapIndex<T> mapIndex = (MapIndex<T>) mMapIndexMap.get(key) ;
		if(mapIndex == null)
		{
			synchronized (key.intern())
			{
				mapIndex = (MapIndex<T>) mMapIndexMap.get(key) ;
				if(mapIndex == null)
				{
					Field field = XClassUtil.getField(aClass, aFieldName) ;
					mapIndex = new MapIndex<T>(aClass) ;
					MapIndexListener<T> lsn = new MapIndexListener<>(mapIndex, field , aFunc) ;
					forEach(aClass, (bean)->{
						lsn.handle(new Event(DEventType.BEAN_CREATE , bean)) ;
						return true ;
					});
					track(lsn , aClass, aFieldName);
					track(lsn , aClass);
					mMapIndexMap.put(key, mapIndex) ;
				}
			}
		}
		return mapIndex ;
	}
	
	public <T extends DBean> void delete(T aEle)
	{
		if(aEle != null)
			aEle.delete() ;
	}
	
	public <T extends DBean> void deleteAll(T... aArray)
	{
		if(XC.isEmpty(aArray))
			return ;
		for(T ele : aArray)
			ele.delete(); 
	}
	
	public <T extends DBean> void deleteAll(Class<T> aClazz , String... aBids)
	{
		if(XC.isEmpty(aBids))
			return ;
		String key = getKey(aClazz) ;
		CLinkedHashMap<String, DBean> map = mTableMap.get(key) ;
		if(map != null)
		{
			for(String bid : aBids)
			{
				DBean bean = map.get(bid) ;
				if(bean != null)
					bean.delete() ;
			}
		}
	}
	
	public <T extends DBean> T delete(Class<T> aClazz , String aId)
	{
		if(XString.isEmpty(aId))
			return null ;
		String key = getKey(aClazz) ;
		CLinkedHashMap<String, DBean> map = mTableMap.get(key) ;
		if(map != null)
		{
			DBean bean = map.get(aId) ;
			if(bean != null)
				bean.delete() ;
			return (T)bean ;
		}
		return null ;
	}
	
	void addCommit(DBean aBean)
	{
		String key = getKey(aBean.getClass()) ;
		final Transaction trans = mTransactionTL.get() ;
		if(trans == null)
		{	
			DBeanCommitter committer = mCommitMap.get(key) ;
			if(committer == null)
			{
				synchronized (key.intern())
				{
					committer = mCommitMap.get(key) ;
					if(committer == null)
					{
						committer = new DBeanCommitter(aBean.getClass()) ;
						mCommitMap.put(key, committer) ;
					}
				}
			}
			committer.add(aBean) ;
		}
		else
		{
			// 当前线程启用了事务
			DBeanCommitter committer = trans.mCommitMap.get(key) ;
			if(committer == null)
			{
				committer = new DBeanCommitter(aBean.getClass()) ;
				trans.mCommitMap.put(key, committer) ;
			}
			committer.add(aBean) ;
		}
	}
	
	void removeCommit(DBean aBean)
	{
		String key = getKey(aBean.getClass()) ;
		DBeanCommitter committer = mCommitMap.get(key) ;
		if(committer != null)
			committer.remove(aBean) ;
	}
	
	@Override
	public void close() throws IOException
	{
		if(!mDisposed)
		{
			mDisposed = true ;
		}
	}
	
	public static DRepository of(String aName , DataSource aDS) 
	{
		return new DRepository(aName , aDS) ;
	}
	
	static String getKey(Class<? extends DBean> aClass)
	{
		DTableDesc tblDesc = DBean.getTableDesc(aClass) ;
		Assert.notNull(tblDesc , "尚未注册类型：%s" , aClass.getName()) ;
		return tblDesc.getTableName() ;
	}
	
	static class DBeanCommitter
	{
		Class<? extends DBean> mClass ;
		Map<String, DBean> mCommitDataMap = XC.linkedHashMap() ;
		
		
		public DBeanCommitter(Class<? extends DBean> aClass)
		{
			mClass = aClass ;
		}
		
		synchronized void add(DBean aBean)
		{
			String bid = DBean.getBID(aBean) ;
			mCommitDataMap.remove(bid) ;
			mCommitDataMap.put(bid, aBean) ;
		}
		
		synchronized void remove(DBean aBean)
		{
			mCommitDataMap.remove(DBean.getBID(aBean)) ;
		}
		
		
		synchronized DBean[] getAllBeans()
		{
			return mCommitDataMap.values().toArray(new DBean[0]) ;
		}
	}
	
	static class MapIndexListener<T extends DBean> extends BaseListener
	{
		MapIndex<T> mMapIndex ;
		Field mField ;
		Function<Object, String> mFunc ;
		
		public MapIndexListener(MapIndex<T> aMapIndex , Field aField , Function<Object , String> aFunc)
		{
			mMapIndex = aMapIndex ;
			mFunc = aFunc ;
			mField = aField ;
			mField.setAccessible(true) ;
		}

		@SuppressWarnings("unchecked")
		@Override
		public synchronized void handle(Event aEvent)
		{
			try
			{
				if(aEvent.type == DEventType.BEAN_CREATE)
				{
					Object value = mField.get(aEvent.source) ;
					if(mFunc != null)
						value = mFunc.apply(value) ;
					if(value != null)
					{
						for(Object obj : asArray(value))
							mMapIndex.add(obj, (T)aEvent.source) ;
					}
				}
				else if(aEvent.type == DEventType.BEAN_DELETE)
				{
					Object value = mField.get(aEvent.source) ;
					if(mFunc != null)
						value = mFunc.apply(value) ;
					if(value != null)
					{
						for(Object obj : asArray(value))
							mMapIndex.remove(obj , (T)aEvent.source) ;
					}
				}
				else if(aEvent.type == DEventType.PROPERTY_CHANGE)
				{
					/**
					 * 这里我们认为支持建立索引的，肯定是java基本对象，或者是Array数组,枚举值
					 */
					Assert.isTrue(aEvent.params.length == 3) ;
					
					Object params_2 = aEvent.params[2] ;
					Object params_1 = aEvent.params[1] ;
					
					if(mFunc != null)
					{
						params_2 = mFunc.apply(params_2) ;
						params_1 = mFunc.apply(params_1) ;
						if(JCommon.equals(params_2, params_1))
							return ;
					}
					
					Set<Object> removeSet = XC.hashSet(asArray(params_2)) ;
					Set<Object> addSet = XC.hashSet(asArray(params_1)) ;
					removeSet.removeAll(addSet) ;		//此时removeSet和addSet已经没有交集
//					addSet.removeAll(removeSet) ;
//					mMapIndex.remove(params_2, (T)aEvent.source) ;
//					mMapIndex.add(params_1, (T)aEvent.source) ;
					for(Object obj : removeSet)
						mMapIndex.remove(obj , (T)aEvent.source) ;
					for(Object obj : addSet)
						mMapIndex.add(obj , (T)aEvent.source) ;
				}
			}
			catch (IllegalArgumentException | IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	static Object[] asArray(Object aObj)
	{
		Object[] result = null ;
		if(aObj != null)
		{
			if(aObj.getClass().isArray())
			{
				final int len = Array.getLength(aObj) ;
				result = new Object[len] ;
				for(int i=0 ; i<len ; i++)
				{
					result[i] = Array.get(aObj, i) ;
				}
			}
			else
				result = new Object[] {aObj} ;
		}
		return result ;
	}
	
	public static class Transaction
	{
		boolean mEnabled ;
		
		final Map<String, DBeanCommitter> mCommitMap = XC.concurrentHashMap() ;
		
		public Transaction()
		{
			mEnabled = true ;
		}
	}
}
