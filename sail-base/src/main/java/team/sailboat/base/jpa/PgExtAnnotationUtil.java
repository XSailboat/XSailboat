package team.sailboat.base.jpa;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.Column;
import jakarta.persistence.Table;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dtool.DBHelper;
import team.sailboat.commons.fan.dtool.IDBTool;
import team.sailboat.commons.fan.dtool.IndexSchema;
import team.sailboat.commons.fan.dtool.pg.Pg_TextSearch;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.text.XString;

/**
 * PostgreSQL数据库的扩展注解工具
 *
 * @author yyl
 * @since 2024年12月19日
 */
public class PgExtAnnotationUtil
{
	static final Logger sLogger = LoggerFactory.getLogger(PgExtAnnotationUtil.class) ;
	
	public static void applyTextSearch(DataSource aDB
			, String... aPkgs) throws SQLException
	{
		if(XC.isEmpty(aPkgs))
			return ;
		Set<Class<?>> classes= XC.hashSet() ; 
		for(String pkg : aPkgs)
		{
			XC.addAll(classes , XClassUtil.getAllClassByAnnotation(Table.class, pkg)) ;
		}
		if(!classes.isEmpty())
		{
			try(Connection conn = aDB.getConnection())
			{
				IDBTool dbTool = DBHelper.getDBTool(conn) ;
				for(Class<?> clazz : classes)
				{
					Table annoTbl = clazz.getAnnotation(Table.class) ;
					String tableName = annoTbl.name() ;
					IndexSchema[] schemas = dbTool.getIndexSchemas(conn, null, tableName) ;
					Field[] fields = clazz.getDeclaredFields() ;
					if(XC.isNotEmpty(fields))
					{
						for(Field field : fields)
						{
							Pg_TextSearch annoTs = field.getAnnotation(Pg_TextSearch.class) ;
							if(annoTs !=null)
							{
								Column annoCol = field.getAnnotation(Column.class) ;
								if(annoCol == null)
								{
									sLogger.warn("类[{}]的字段[{}]没有Column注解，Pg_TextSearch注解失效！"
											, clazz.getName() , field.getName()) ;
									continue ;
								}
								String indexName = annoTs.name() ;
					    		boolean exists = XC.isNotEmpty(schemas) && XC.findFirstIndex(schemas, idx->indexName.equals(idx.getName()), 0) != -1 ;
					    		if(!exists)
					    		{
					    			// 创建这个索引
					    			String sql = XString.msgFmt("CREATE INDEX {} ON {} USING gin({} public.gin_bigm_ops)"
					    					, indexName , tableName , annoCol.name()) ;
					    			DBHelper.execute(conn , true, sql);
					    			sLogger.info("执行创建搜索索引的SQL:{}" , sql) ;
					    		}
							}
						}
					}
				}
	    		
			}
			
		}
	}
}
