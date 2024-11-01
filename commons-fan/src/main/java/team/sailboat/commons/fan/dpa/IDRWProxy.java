package team.sailboat.commons.fan.dpa;

import java.sql.SQLException;
import java.util.List;

public interface IDRWProxy
{
	IDRWTransaction beginTransaction() ;
	
	<T extends DBean> T getByPrimaryKeys(Class<T> aClass , Object... aPKs) throws SQLException ;
	
	<T extends DBean> T  getFirst(Class<T> aClass , String aSqlCnd) throws SQLException ;
	
	<T extends DBean> List<T>  getAll(Class<T> aClass , String aSqlCnd) throws SQLException ;
	
	<T extends DBean> DPage<T> getPage(Class<T> aClass , String aSqlCnd
			, int aPage , int aPageSize) throws SQLException ;
	
	<T extends DBean> void delete(Class<T> aClass , String aSqlCnd) throws SQLException ;
	
	<T extends DBean> void delete(Class<T> aClass , String aSqlCnd , IDRWTransaction aTransaction) throws SQLException ;
}
