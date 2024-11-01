package team.sailboat.commons.fan.dtool;

import java.sql.Connection;
import java.sql.SQLException;

import team.sailboat.commons.fan.infc.ESupplier;

class DBConnectionFactory implements ESupplier<Connection , SQLException>
{
	Class<?> mDriverClass ;
	String mConnStr ;
	String mUsername ;
	String mPassword ;
	
	public DBConnectionFactory(Class<?> aDriverClass , String aConnStr , String aUsername , String aPassword)
	{
		mDriverClass = aDriverClass ;
		mConnStr = aConnStr ;
		mUsername = aUsername ;
		mPassword = aPassword ;
	}
	
	@Override
	public Connection get() throws SQLException
	{
		return DBHelper.connect(mDriverClass , mConnStr , mUsername , mPassword) ;
	}
}
