package team.sailboat.commons.fan.dpa;

import java.sql.ResultSet;

public interface IDBeanFactory
{
	DBean create(Class<? extends DBean> aClass , DTableDesc aTblDesc , ResultSet aRs) ;
}
