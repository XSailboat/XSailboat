package team.sailboat.base.ds;

public interface DataSourceFeatures
{
	static final int sF_JDBC = 1 ;
	static final int sF_RDB = 0b10 | sF_JDBC ;
	
	/**
	 * 时序数据库
	 */
	static final int sF_TSDB = 0b100 ;
	
	static final int sF_FileSystem = 0b1000 ;
	
	/**
	 * 消息中间件
	 */
	static final int sF_MQ = 0b10000 ;
}
