package team.sailboat.bd.base.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.http.impl.io.EmptyInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 将节点的资源文件存储到HDFS		<br>
 *
 * @author yyl
 * @since 2021年3月11日
 */
public class NodeResourceProxy_HDFS implements INodeResourceProxy
{
	final Logger mLogger = LoggerFactory.getLogger(getClass()) ;
	
	FileSystem mFS ;
	
	String mDirPath ;
	
	public NodeResourceProxy_HDFS(FileSystem aFS , String aDirPath) throws IOException
	{
		mFS = aFS ;
		mDirPath = aDirPath ;
		Path path = new Path(mDirPath) ;
		if(!mFS.exists(path))
		{
			mFS.mkdirs(path) ;
			mLogger.info("在HDFS上创建原先不存在的目录：{}" , mDirPath) ;
		}
	}
	
	@Override
	public InputStream openDataReader(String aNodeId) throws IOException
	{
		Path path = new Path(mDirPath , aNodeId) ;
		if(mFS.exists(path))
			return mFS.open(path) ;
		else
			return EmptyInputStream.INSTANCE ;
	}

	@Override
	public OutputStream openDataWriter(String aNodeId) throws IOException
	{
		return mFS.createFile(new Path(mDirPath , aNodeId)).build() ;
	}
	
	@Override
	public String getPath(String aNodeId) throws IOException
	{
		return new Path(mDirPath , aNodeId).toString() ;
	}
	
	@Override
	public void delete(String aNodeId) throws IOException
	{
		mFS.delete(new Path(mDirPath , aNodeId) , false) ;
	}

}
