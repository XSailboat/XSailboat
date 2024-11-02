package team.sailboat.bd.base;

import team.sailboat.base.bean.AppConfCommon;
import org.springframework.beans.factory.annotation.Value;

public class AppConfigBase extends AppConfCommon
{

	@Value("${sys.hdfs.dir.home}")
	String mHomeDirOnHdfs;

	@Value("${sys.hive.conn.url}")
	String mHiveDBConnUrl;

	@Value("${sys.hive.conn.key}")
	String mHiveDBConnKey;

	@Value("${sys.hive.conn.secret}")
	String mHiveDBConnSecret;

	public String getHomeDirOnHdfs()
	{
		return mHomeDirOnHdfs;
	}

	public String getHiveDBConnUrl()
	{
		return mHiveDBConnUrl;
	}

	public String getHiveDBConnKey()
	{
		return mHiveDBConnKey;
	}

	public String getHiveDBConnSecret()
	{
		return mHiveDBConnSecret;
	}
}