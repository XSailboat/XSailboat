package team.sailboat.bd.base.hdfs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Predicate;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.file.FileUtils;
import team.sailboat.commons.fan.serial.StreamAssist;

public class HdfsUtils
{
	
	public static FsPermission sDefaultDirPermission = new FsPermission(FsAction.ALL , FsAction.ALL , FsAction.READ_EXECUTE) ;
	public static FsPermission sDefaultFilePermission = new FsPermission(FsAction.READ_WRITE , FsAction.READ_WRITE , FsAction.READ) ;
	
	public static void copyFilesTo(FileSystem aFs , Path aSourceDir , Path aTargetDir 
			, Predicate<FileStatus> aPred) throws IOException
	{
		copyFilesTo(aFs, aSourceDir, aTargetDir, aPred
				, sDefaultDirPermission
				, sDefaultFilePermission) ;
	}
	
	public static void copyFilesTo(FileSystem aFs , Path aSourceDir , Path aTargetDir , Predicate<FileStatus> aPred
			, FsPermission aDirPerm
			, FsPermission aFilePerm) throws IOException
	{
		if(!aFs.exists(aSourceDir))
			return ;
		FileStatus fstatus = aFs.getFileStatus(aSourceDir) ;
		if(fstatus.isDirectory())
		{
			String ctxPath = aSourceDir.toString() ;
			String targetDir = aTargetDir.toString() ;
			RemoteIterator<LocatedFileStatus> it = aFs.listFiles(aSourceDir , true) ;
			while(it.hasNext())
			{
				LocatedFileStatus lfs = it.next() ;
				if(lfs.isFile())
				{
					if(aPred != null && !aPred.test(lfs))
						continue ;
					Path targetPath = getTargetPath(lfs, ctxPath, targetDir) ;
					aFs.mkdirs(targetPath.getParent() , aDirPerm) ;
					StreamAssist.transfer_cc(aFs.open(lfs.getPath()) , aFs.create(targetPath, true)) ;
					aFs.setPermission(targetPath, aFilePerm) ;
				}
			}
		}
		else
		{
			if(aPred != null && !aPred.test(fstatus))
				return ;
			// 把文件复制上去
			String fileName = fstatus.getPath().getName() ;
			Path targetPath = new Path(aTargetDir, fileName) ;
			StreamAssist.transfer_cc(aFs.open(fstatus.getPath()) , aFs.create(targetPath, true)) ;
			aFs.setPermission(targetPath, aFilePerm) ;
		}
	}
	
	public static Path getTargetPath(FileStatus aFStatus , String aCPath , String aTargetPath)
	{
		String relativePath = aFStatus.getPath().toUri().getPath() ;
		relativePath = relativePath.substring(aCPath.length()) ;
		return new Path(aTargetPath + relativePath) ;
	}
	
	public static void uploadFiles(FileSystem aFs , File aLocalDir , Path aTargetDir 
			, Predicate<File> aPred) throws FileNotFoundException, IOException
	{
		uploadFiles(aFs, aLocalDir, aTargetDir, aPred, sDefaultDirPermission, sDefaultFilePermission) ;
	}
	
	/**
	 * 把aLocalDir目录下的目录及文件（不包括aLocalDir本身）上传到目标目录aTargetDir下
	 * @param aFs
	 * @param aLocalDir
	 * @param aTargetDir
	 * @param aPred
	 * @param aDirPerm
	 * @param aFilePerm
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void uploadFiles(FileSystem aFs , File aLocalDir , Path aTargetDir 
			, Predicate<File> aPred
			, FsPermission aDirPerm
			, FsPermission aFilePerm) throws FileNotFoundException, IOException
	{
		if(!aLocalDir.exists())
			return ;
		File[] files = aLocalDir.listFiles() ;
		if(XC.isEmpty(files))
			return ;
		for(File file : files)
		{
			if(aPred != null && !aPred.test(file))
				continue ;
			Path target = new Path(aTargetDir, file.getName()) ;
			if(file.isFile())
			{
				StreamAssist.transfer_cc(FileUtils.openBufferedInStream(file) , aFs.create(target, true)) ;
				aFs.setPermission(target , aFilePerm) ;
			}
			else if(file.isDirectory())
			{
				aFs.mkdirs(target , aDirPerm) ;
				aFs.setPermission(target , aDirPerm) ;
				uploadFiles(aFs, file, target , aPred, aDirPerm, aFilePerm) ;
			}
		}
	}
	
	public static void clearDirectory()
	{
		throw new IllegalStateException("尚未实现！") ;
	}
	
	public static void uploadFile(FileSystem aFs , Path aTargetFilePath
			, InputStream aIns) throws FileNotFoundException, IOException
	{
		uploadFile(aFs, aTargetFilePath, aIns , sDefaultFilePermission) ;
	}
	
	public static void uploadFile(FileSystem aFs , Path aTargetFilePath
			, InputStream aIns
			, FsPermission aFilePerm) throws FileNotFoundException, IOException
	{
		StreamAssist.transfer_cc(aIns , aFs.create(aTargetFilePath , true)) ;
		aFs.setPermission(aTargetFilePath , aFilePerm) ;
	}
}
