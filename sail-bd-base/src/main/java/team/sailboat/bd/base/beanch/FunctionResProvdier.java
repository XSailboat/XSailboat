package team.sailboat.bd.base.beanch;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import team.sailboat.bd.base.ZBDException;
import team.sailboat.bd.base.infc.IFunctionResProvider;
import team.sailboat.commons.fan.collection.XC;

public class FunctionResProvdier implements IFunctionResProvider
{
	protected Path mJarsDir ;
	protected IWSRepo mParent ;
	
	protected FunctionResProvdier(IWSRepo aParent)
	{
		mParent = aParent ;
	}
	
	public FunctionResProvdier(IWSRepo aParent , Path aJarsDir)
	{
		mParent = aParent ;
		mJarsDir = aJarsDir ;
	}
	
	protected void setJarsDir(Path aJarsDir)
	{
		mJarsDir = aJarsDir;
	}
	
	public Path getJarsPath()
	{
		return mJarsDir ;
	}

	/**
	 * 如果工作空间中没有已经提交的jar资源，将返回0
	 * @return
	 */
	@Override
	public long getJarResourcesVersion()
	{
		return mParent.getRunData().getLong("jarResourcesVersion" , 0) ;
	}

	@Override
	public void setJarResourcesVersion(long aTime) throws ZBDException
	{
		if(aTime > getJarResourcesVersion())
		{
			try
			{
				mParent.getRunData().put("jarResourcesVersion" , aTime) ;
			}
			catch (SQLException e)
			{
				throw new ZBDException(e, "记录functionsVersion出现异常！", e) ;
			}
		}
	}

	@Override
	public long getFunctionsVersion()
	{
		return mParent.getRunData().getLong("functionsVersion" , 0) ;
	}

	@Override
	public void setFunctionsVersion(long aTime) throws ZBDException
	{
		if(aTime > getFunctionsVersion())
		{
			try
			{
				mParent.getRunData().put("functionsVersion" , aTime) ;
			}
			catch (SQLException e)
			{
				throw new ZBDException(e, "记录functionsVersion出现异常！", e) ;
			}
		}
	}

	@Override
	public List<String> getJarResourcesUrlPaths() throws ZBDException
	{
		FileSystem fs = mParent.getWsHdfs() ;
		try
		{
			if(!fs.exists(mJarsDir))
				return Collections.emptyList() ;
			FileStatus[] fstatuses = fs.listStatus(mJarsDir) ;
			if(XC.isEmpty(fstatuses))
				return Collections.emptyList() ;
			List<String> pathList = XC.arrayList() ;
			for(FileStatus fstatus : fstatuses)
			{
				pathList.add(fstatus.getPath().toString()) ;
			}
			return pathList ;
		}
		catch (IOException e)
		{
			throw new ZBDException(e, "查询hdfs出现异常！") ;
		}
	}

}
