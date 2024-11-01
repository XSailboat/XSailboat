package team.sailboat.base.def;

import team.sailboat.commons.fan.dtool.DBType;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.struct.Bits;
import team.sailboat.commons.fan.text.XString;

public enum DataSourceType implements DataSourceFeatures
{
	Hive(sF_RDB , "jdbc:hive2" , "Hive") ,
	MySql5(sF_RDB , "jdbc:mysql" , "MySQL v5.x") ,
	MySql(sF_RDB , "jdbc:mysql" , "MySQL") ,
	PostgreSQL(sF_RDB , "jdbc:postgresql" , "PostgreSQL") ,
	MSSqlServer(sF_RDB , "jdbc:sqlserver" , "微软SQLServer") ,
	Oracle(sF_RDB , "jdbc:oracle:thin" , "Oracle") ,
	DM(sF_RDB , "jdbc:dm" , "达梦") ,
	SFTP(sF_FileSystem , "sftp" , "SFTP") ,
	FTP(sF_FileSystem , "ftp" , "FTP") ,
	LocalFile(sF_FileSystem , "file" , "本地文件") ,
	TDengine(sF_TSDB | sF_JDBC , "jdbc:TAOS" , "涛思") ,
	Kafka(sF_MQ , "" , "Kafka") ,
	HttpService(sF_HttpService , "http" , "HTTP") ,
	HttpsService(sF_HttpService , "https" , "HTTPS") ,
	;
	
	int mCategory ;
	String mProtocol ;
	final String displayName ;
	
	private DataSourceType(int aCategory , String aProtocol
			, String aDisplayName)
	{
		mCategory = aCategory ;
		mProtocol = aProtocol ;
		displayName = aDisplayName ;
	}
	
	public String getDisplayName()
	{
		return displayName;
	}
	
	public boolean isRDB()
	{
		return Bits.hit(mCategory , sF_RDB) ;
	}
	
	public boolean isSupportJDBC()
	{
		return Bits.hit(mCategory, sF_JDBC) ;
	}
	
	public boolean isFileSystem()
	{
		return Bits.hit(mCategory , sF_FileSystem) ;
	}
	
	public boolean isTSDB()
	{
		return Bits.hit(mCategory , sF_TSDB) ;
	}
	
	public boolean hasFeature(int aFeatureCode)
	{
		return Bits.hit(mCategory, aFeatureCode) ;
	}
	
	/**
	 * 消息队列
	 */
	public boolean isMQ()
	{
		return Bits.hit(mCategory , sF_MQ) ;
	}
	
	public String getProtocol()
	{
		return mProtocol ;
	}
	
	/**
	 * Http服务
	 * @return
	 */
	public boolean isHttpService()
	{
		return Bits.hit(mCategory, sF_HttpService) ;
	}
	
	public static DBType toDBType(DataSourceType aDsType)
	{
		Assert.isTrue(aDsType.isRDB() || aDsType == TDengine , "指定的类型[%s]不是关系数据库，不能使用此方法！" , aDsType.name());
		switch(aDsType)
		{
		case MySql:
			return DBType.MySQL ;
		case PostgreSQL:
			return DBType.PostgreSQL ;
		case MySql5:
			return DBType.MySQL5 ;
		case Hive:
			return DBType.Hive ;
		case MSSqlServer:
			return DBType.SQLServer ;
		case Oracle:
			return DBType.Oracle ;
		case DM:
			return DBType.DM ;
		case TDengine:
			return DBType.TDengine ;
		default:
			throw new IllegalArgumentException(XString.msgFmt("未支持转成DBType的类型：{}" , aDsType.name())) ;
		}
	}
}
