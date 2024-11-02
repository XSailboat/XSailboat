package team.sailboat.bd.base.yarn;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.yarn.api.records.ApplicationAttemptId;
import org.apache.hadoop.yarn.api.records.ApplicationAttemptReport;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.LocalResource;
import org.apache.hadoop.yarn.api.records.LocalResourceType;
import org.apache.hadoop.yarn.api.records.LocalResourceVisibility;
import org.apache.hadoop.yarn.api.records.URL;
import org.apache.hadoop.yarn.api.records.YarnApplicationAttemptState;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import team.sailboat.commons.fan.file.FileUtils;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.XString;

public class YarnUtils
{
	static final Logger sLogger = LoggerFactory.getLogger(YarnUtils.class) ;
	
	// 应用启动，最多等20秒
	static final long AM_STATE_WAIT_TIMEOUT_MS = 20_000;
	
	public static void addToLocalResources(FileSystem aFs , String aFilePath , String aTargetPath , ApplicationId aAppId ,
		    Map<String, LocalResource> aLocResMap , String aCmd) throws IllegalArgumentException, IOException
	{
		addToLocalResources(aFs, aFilePath, aTargetPath, aAppId, aLocResMap, aCmd, null) ;
	}
	
	public static void addToLocalResources(FileSystem aFs , final String aFilePath , String aTargetPath , ApplicationId aAppId ,
		    Map<String, LocalResource> aLocResMap , String aCmd
		    , Predicate<FileStatus> aPred
		    ) throws IllegalArgumentException, IOException
	{
		addToLocalResources(aFs, aFilePath, aTargetPath, aAppId, aLocResMap, aCmd, aPred, (lfs)->lfs.getPath().getName());
	}
	
	public static void addToLocalResources(FileSystem aFs , final String aFilePath , String aTargetPath , ApplicationId aAppId ,
		    Map<String, LocalResource> aLocResMap , String aCmd
		    , Predicate<FileStatus> aPred
		    , Function<FileStatus, String> aFileNameFunc) throws IllegalArgumentException, IOException
	{
		final Path cpath = new Path(aFilePath) ;
		if(!aFs.exists(cpath))
			return ;
		FileStatus fstatus = aFs.getFileStatus(cpath) ;
		if(fstatus.isDirectory())
		{
			RemoteIterator<LocatedFileStatus> it = aFs.listFiles(cpath , true) ;
			String ctxPath = cpath.toUri().getPath() ;
			while(it.hasNext())
			{
				LocatedFileStatus lfs = it.next() ;
				if(lfs.isFile())
				{
					if(aPred != null && !aPred.test(lfs))
						continue ;
					String fileName = lfs.getPath().getName() ;
					LocalResourceType lresType = fileName.endsWith(".zip") || fileName.endsWith(".gz") || fileName.endsWith(".tar")
							?LocalResourceType.ARCHIVE:LocalResourceType.FILE ;
					String targetPath = getTargetPath(lfs, ctxPath, aTargetPath , aFileNameFunc) ;
					aLocResMap.put(targetPath, LocalResource.newInstance(URL.fromPath(lfs.getPath()),
							lresType ,
							LocalResourceVisibility.PUBLIC ,
							lfs.getLen(),
							lfs.getModificationTime(),
							null,
							true));
					sLogger.info("添加文件（相对路径）：{} 到 {}，资源类型：{}" , lfs.getPath().toUri().getPath(), targetPath , lresType.name());
				}
			}
		}
		else
		{
			if(aPred != null && !aPred.test(fstatus))
				return ;
			// 把文件复制上去
			String fileName = aFileNameFunc.apply(fstatus) ;
			LocalResourceType lresType = fileName.endsWith(".zip") || fileName.endsWith(".gz") || fileName.endsWith(".tar")
					?LocalResourceType.ARCHIVE:LocalResourceType.FILE ;
			String targetPath = null ;
			if(XString.isEmpty(aTargetPath))
				targetPath = fileName ;
			else
			{
				targetPath = aTargetPath + "/" + fileName ;
			}
			aLocResMap.put(targetPath, LocalResource.newInstance(URL.fromPath(fstatus.getPath()),
					lresType ,
					LocalResourceVisibility.PUBLIC ,
					fstatus.getLen(),
					fstatus.getModificationTime(),
					null,
					true));
			sLogger.info("添加文件（相对路径）：{} 到 {}，资源类型：{}" , fstatus.getPath().toUri().getPath() , targetPath , lresType.name());
		}
	}
	
	/**
	 * 如果遇到FAILED或者FINISHED状态，也会返回
	 * @param aYarnClient
	 * @param appId
	 * @param aAttemptState
	 * @return
	 * @throws YarnException
	 * @throws IOException
	 */
	public static ApplicationAttemptReport monitorCurrentAppAttempt(YarnClient aYarnClient
			, ApplicationId appId, YarnApplicationAttemptState aAttemptState)
			throws YarnException, IOException
	{
		long startTime = System.currentTimeMillis();
		ApplicationAttemptId attemptId = null;
		while (true)
		{
			if (attemptId == null)
			{
				attemptId = aYarnClient.getApplicationReport(appId)
									.getCurrentApplicationAttemptId();
			}
			ApplicationAttemptReport attemptReport = null;
			if (attemptId != null)
			{
				attemptReport = aYarnClient.getApplicationAttemptReport(attemptId);
				if (aAttemptState.equals(attemptReport.getYarnApplicationAttemptState())
						|| aAttemptState == YarnApplicationAttemptState.FAILED
						|| aAttemptState == YarnApplicationAttemptState.FINISHED)
				{
					return attemptReport;
				}
			}
			sLogger.info("应用[{}]的此次尝试状态是{}，等待此次尝试达到{}状态"  , appId 
					, (attemptReport == null ? " N/A " : attemptReport.getYarnApplicationAttemptState())
					, aAttemptState);
			JCommon.sleepInSeconds(1) ;
			if (System.currentTimeMillis() - startTime > AM_STATE_WAIT_TIMEOUT_MS)
			{
				String msg = XString.msgFmt("超过{}秒，仍未达到{}状态!!" , AM_STATE_WAIT_TIMEOUT_MS/1000d , aAttemptState) ;
				sLogger.error(msg) ;
				throw new RuntimeException(msg);
			}
		}
	}
	
	public static String getTargetPath(FileStatus aFStatus , String aCPath , String aTargetPath
			, Function<FileStatus, String> aFileNameFunc)
	{
		String fileName = aFStatus.getPath().getName() ;
		LocalResourceType lresType = fileName.endsWith(".zip") || fileName.endsWith(".gz") || fileName.endsWith(".tar")
				?LocalResourceType.ARCHIVE:LocalResourceType.FILE ;
		String relativePath = aFStatus.getPath().getParent().toUri().getPath() ;
		relativePath = relativePath.substring(aCPath.length()) ;
		if(lresType == LocalResourceType.ARCHIVE)
		{
			relativePath += "/" + FileUtils.getCleanName(fileName) ;
		}
		else
		{
			relativePath += "/" + aFileNameFunc.apply(aFStatus) ;
		}
		String targetPath = aTargetPath + relativePath ;
		if(targetPath.startsWith("/"))
			targetPath = targetPath.substring(1) ;
		return targetPath ;
	}
	
	public static JSONObject toJSONObject(ApplicationReport aReport)
	{
		return new JSONObject().put("appId" , aReport.getApplicationId())
				.put("currentAppAttemptId" , aReport.getCurrentApplicationAttemptId())
				.put("startTime" , aReport.getStartTime())
				.put("launchTime" , aReport.getLaunchTime())
				.put("finishTime" , aReport.getFinishTime())
				.put("submitTime" , aReport.getSubmitTime())
				.put("amNodeLabelExpression" , aReport.getAmNodeLabelExpression())
				.put("appType" , aReport.getApplicationType())
				.put("appState" , aReport.getYarnApplicationState())
				.put("queue" , aReport.getQueue())
				.put("trackingUrl" , aReport.getTrackingUrl())
				.put("host" , aReport.getHost())
				.put("rpcPort", aReport.getRpcPort())
				.put("finalAppStatus" , aReport.getFinalApplicationStatus())
				.put("diagnostics" , aReport.getDiagnostics())
				.put("user", aReport.getUser())
				;
	}
}
