package team.sailboat.commons.fan.dpa;

import java.sql.SQLException;
import java.util.List;

/**
 * 
 * DPA的代理模式接口
 *
 * @author yyl
 * @since 2024年11月22日
 */
public interface IDRWProxy
{
	/**
	 * 
	 * 开启一个事务。为了让接下来的数据，在一个数据库事务上提交
	 * 
	 * @return
	 */
	IDRWTransaction beginTransaction() ;
	
	/**
	 * 
	 * 通过主键字段值获取对象
	 * 
	 * @param <T>
	 * @param aClass
	 * @param aPKs		参数值的顺序应该和类上按注解的seq排序的声明顺序一致
	 * @return
	 * @throws SQLException
	 */
	<T extends DBean> T getByPrimaryKeys(Class<T> aClass , Object... aPKs) throws SQLException ;
	
	/**
	 * 
	 * 获取符合条件的数据中的第一个对象
	 * 
	 * @param <T>
	 * @param aClass
	 * @param aSqlCnd		SQL条件段。WHERE语句后面的判断条件（不用带WHERE）
	 * @return
	 * @throws SQLException
	 */
	<T extends DBean> T  getFirst(Class<T> aClass , String aSqlCnd) throws SQLException ;
	
	/**
	 * 
	 * 获取所有符合条件的数据对象
	 * 
	 * @param <T>
	 * @param aClass
	 * @param aSqlCnd		SQL条件段。WHERE语句后面的判断条件（不用带WHERE）
	 * @return
	 * @throws SQLException
	 */
	<T extends DBean> List<T>  getAll(Class<T> aClass , String aSqlCnd) throws SQLException ;
	
	/**
	 * 
	 * 分页查询数据对象
	 * 
	 * @param <T>
	 * @param aClass
	 * @param aSqlCnd		SQL条件段。WHERE语句后面的判断条件（不用带WHERE）
	 * @param aPage			页码。从0开始
	 * @param aPageSize
	 * @return
	 * @throws SQLException
	 */
	<T extends DBean> DPage<T> getPage(Class<T> aClass , String aSqlCnd
			, int aPage , int aPageSize) throws SQLException ;
	
	/**
	 * 
	 * 删除符合条件的数据
	 * 
	 * @param <T>
	 * @param aClass
	 * @param aSqlCnd
	 * @throws SQLException
	 */
	<T extends DBean> void delete(Class<T> aClass , String aSqlCnd) throws SQLException ;
	
	/**
	 * 
	 * 在指定事务中删除符合条件的数据
	 * 
	 * @param <T>
	 * @param aClass
	 * @param aSqlCnd
	 * @param aTransaction
	 * @throws SQLException
	 */
	<T extends DBean> void delete(Class<T> aClass , String aSqlCnd , IDRWTransaction aTransaction) throws SQLException ;
}
