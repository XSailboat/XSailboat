package team.sailboat.commons.fan.dpa;

import java.sql.Connection;

public interface IDRWTransaction extends AutoCloseable
{
	Connection getConnection() ;
	
	@Override
	void close() ;
}
