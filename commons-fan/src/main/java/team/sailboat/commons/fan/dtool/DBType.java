package team.sailboat.commons.fan.dtool;

/**
 * 数据库类型
 *
 * @author yyl
 * @since 2017年10月23日
 */
public enum DBType
{
	Oracle("Oracle" , "select status from v$instance") ,
	MySQL("MySQL" , "show status") ,
	MySQL5("MySQL5" , "show status") ,
	SQLServer("SQLServer" , "SELECT state_desc FROM sys.databases") ,
	DM("达梦" , "select instance_name,status$ from v$instance") ,
	Derby("Derby" , "") ,
	Hive("Hive" , "select version()") ,
	PostgreSQL("PostgreSQL" , "show server_version") ,
	H2("H2" , "") ,
	TDengine("涛思" , "select server_version()")
	;
	
	String mAliasName ;
	String mValidationQuery ;
	
	private DBType(String aAliasName , String aValidationQuery)
	{
		mAliasName = aAliasName ;
		mValidationQuery = aValidationQuery ;
	}
	
	public String getAliasName()
	{
		return mAliasName ;
	}
	
	public String getValidationQuery()
	{
		return mValidationQuery;
	}
	
	public static DBType of(String aName)
	{
		switch(aName.toLowerCase())
		{
		case "mysql":
			aName = "MySQL" ;
			break ;
		case "mysql5":
			aName = "MySQL5" ;
			break ;
		}
		return valueOf(aName) ;
	}
}
