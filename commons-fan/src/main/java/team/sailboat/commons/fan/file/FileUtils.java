package team.sailboat.commons.fan.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.serial.StreamAssist;
import team.sailboat.commons.fan.struct.Bits;
import team.sailboat.commons.fan.sys.JEnvKit;
import team.sailboat.commons.fan.text.XString;

public class FileUtils
{
	public static final File[] sEmptyFileArray = new File[0] ;
 	
	static FileFilter sRegFileFilter ;
	static FileFilter sDirFileFilter ;
	static File sOsTempDir ;
	
	/**
	 * 级联删除文件和文件夹
	 * @param aFile
	 * @return 返回的是删除的常规文件数量，没有将目录计算在内
	 */
	public static int deleteFile(File aFile)
	{
		int fileCount = 0 ;
		if (aFile.isDirectory())
		{
			File[] files = aFile.listFiles();
			if (files != null && files.length > 0)
			{
				for (File file : files)
					fileCount += deleteFile(file);
			}
			aFile.delete();
		}
		else if(aFile.delete())
			fileCount++ ;
		return fileCount ;
	}
	
	/**
	 * 清空一个目录
	 * @param aDirectory	aDirectory必须是目录
	 */
	public static void clearDirectory(File aDirectory)
	{
		Assert.notNull(aDirectory) ;
		Assert.isTrue(aDirectory.isDirectory());
		for(File file : aDirectory.listFiles())
			deleteFile(file) ;
	}

	/**
	 * 用例：在一个目录下面，知道文件名(不包含扩展名)，但文件的扩展名不能确定，只知道允许的几种情形
	 * @param aDir
	 * @param aName
	 * @param aExtNames
	 * @return
	 */
	public static File getFile(File aDir, String aName, String... aExtNames)
	{
		if (aExtNames != null && aExtNames.length > 0)
		{
			for (String extName : aExtNames)
			{
				if (!extName.isEmpty() && !extName.startsWith("."))
					extName = "." + extName;
				File file = new File(aDir, aName + extName);
				if (file.exists())
					return file;
			}
		}
		return null;
	}

	/**
	 * 
	 * 取得文件：aDir/aNames[0]/aNames[1]/.../aNames[n]		<br>
	 * 如果返回的文件的父目录不存在，将被创建
	 * 
	 * @param aDir
	 * @param aNames		有序的多级子目录名
	 * @return
	 */
	public static File getFile0(File aDir , String... aNames)
	{
		Assert.notNull(aDir , "目录不能为null") ;
		if(aNames == null || aNames.length == 0)
			return aDir ;
		File file = aDir ;
		for (String name : aNames)
		{
			if(name == null)
				continue ;
			file = new File(file, name);
		}
		file.getParentFile().mkdirs() ;
		return file;
	}
	
	/**
	 * 除去文件扩展名的文件名
	 * @param aFile
	 * @return
	 */
	public static String getCleanName(File aFile)
	{
		return getCleanName(aFile.getName()) ;
	}
	
	/**
	 * 取得文件扩展名，返回的扩展名不带“.”							<br>
	 * 如果文件不存在扩展名将返回null									<br>
	 * @param aFile
	 * @return
	 */
	public static String getExtName(File aFile)
	{
		return getExtName(aFile.getName()) ;
	}
	
	/**
	 * 取得文件扩展名，返回的扩展名不带“.”							<br>
	 * 如果文件不存在扩展名将返回null									<br>
	 * 
	 * @param aFileName
	 * @return
	 */
	public static String getExtName(String aFileName)
	{
		int i = aFileName.lastIndexOf('.') ;
		if(i != -1)
			return aFileName.substring(i+1) ;
		return null ;
	}
	
	/**
	 * 取得文件名(不包含扩展名)
	 * @param fn			文件名
	 * @return
	 */
	public static String getCleanName(String fn)
	{
		int index = fn.lastIndexOf('.') ;
		if(index != -1)
			return fn.substring(0, index) ;
		return fn ;
	}
	
	/**
	 * 取得指定目录下指定扩展名的文件		<br>
	 * 如果指定目录下还有子目录，不会深入去获取，这是和listAll方法的区别 
	 * @param aDir
	 * @param aExtName   扩展名不用带"."，如“xml”,"svg"
	 * @return
	 */
	public static File[] getFiles(File aDir , String aExtName)
	{
		return aDir.listFiles(new FileExtNameFilter(aExtName)) ;
	}
	
	/**
	 * 取得指定目录下面指定扩展名的文件					<br>
	 * 如果指定目录下面有子目录，他会递归深入去获取
	 * @param aDir
	 * @param aExtName
	 * @return
	 */
	public static File[] listAll(File aDir , String aExtName)
	{
		List<File> result = new ArrayList<>() ;
		listAll(aDir, new FileExtNameFilter(aExtName) , result);
		return result.toArray(new File[result.size()]) ;
	}
	
	/**
	 * 只适合于提取文件（非目录）
	 */
	private static void listAll(File aDir , FileFilter aFilter , List<File> aResult)
	{
		for(File file : aDir.listFiles())
		{
			if(file.isDirectory())
				listAll(file, aFilter, aResult);
			else if(aFilter.accept(file))
				aResult.add(file) ;
		}
	}
	
	/**
	 * 获取常规文件过滤器
	 * @return
	 */
	public static FileFilter getRegFileFilter()
	{
		if(sRegFileFilter == null)
			sRegFileFilter = new FileFilter()
			{
				@Override
				public boolean accept(File aPathname)
				{
					return aPathname.isFile() ;
				}
			};
		return sRegFileFilter ;
	}
	
	/**
	 * 取得目录过滤器
	 * @return
	 */
	public static FileFilter getDirectoryFileFilter()
	{
		if(sDirFileFilter == null)
		{
			sDirFileFilter = new FileFilter()
			{
				@Override
				public boolean accept(File aPathname)
				{
					return aPathname.isDirectory();
				}
			};
		}
		return sDirFileFilter ;
	}
	
	/**
	 * 举例：对于aPath == /home/cimstech/yyl/abc.txt		<br />
	 * aParents == 0 ，返回 abc.txt
	 * aParents == 1 ， 返回yyl/abc.txt
	 * 
	 * @param aPath
	 * @param aParents 			aParents>=0
	 * @return
	 */
	public static String getPath(String aPath , int aParents)
	{
		aPath = toCommonPath(aPath) ;
		int index = XString.lastIndexOf(aPath, '/', aParents) ;
		if(index != -1)
			return aPath.substring(index+1) ;
		else
			return aPath ;
	}
	
	/**
	 * 将aParentPath和aFileName拼接成路径
	 * @param aParentPath
	 * @param aChildPath
	 * @return
	 */
	public static String getPath(String aParentPath , String aChildPath)
	{
		if(aParentPath == null)
			return toCommonPath(aChildPath) ;
		else
		{
			if(XString.isEmpty(aChildPath))
				return toCommonPath(aParentPath) ;
			String childPath = aChildPath ;
			if(childPath.startsWith("./"))
				childPath =childPath.substring(2) ;
			String parentPath = aParentPath ;
			while(childPath.startsWith("../"))
			{
				childPath = childPath.substring(3) ;
				parentPath = getParent(parentPath) ;
			}
			return toCommonPath(XString.splice(parentPath , "/" , childPath)) ;
		}
	}
	
	/**
	 * 取得文件名			<br>
	 * 如果aPath是“/”,将返回null
	 * @param aPath
	 * @return
	 */
	public static String getFileName(String aPath)
	{
		if(aPath == null || aPath.length()<=1)
			return null ;
		while(XString.endWith(aPath, '/' , '\\'))
			aPath = aPath.substring(0, aPath.length()-1) ;
 		int index = XString.lastIndexOf_Or(aPath, '/' , '\\') ;
		if(index != -1)
			return aPath.substring(index+1) ;
		else
			return aPath ;
	}
	
	/**
	 * 取得文件的父路径				<br>
	 * 对于路径"/"，它的父路径是null
	 * @param aPath
	 * @return
	 */
	public static String getParent(String aPath)
	{
		if(aPath == null)
			return null ;
		if(aPath.length() == 1 && aPath.charAt(0) == '/')
			return null ;
		while(XString.endWith(aPath, '/' , '\\'))
			aPath = aPath.substring(0, aPath.length()-1) ;
 		int index = XString.lastIndexOf_Or(aPath, '/' , '\\') ;
		if(index != -1)
		{
			if(index == 0)
				return "/" ;
			else
				return aPath.substring(0 , index) ;
		}
		else
			return null ;
	}
	
	/**
	 * 
	 * @param aPath
	 * @param aGenerations	0将返回aPath，1将返回parent的Path,2将返回parent的parent的路径
	 * @return
	 */
	public static String getAncestorPath(String aPath , int aGenerations)
	{
		return getAncestorPath(aPath, aGenerations, true) ;
	}
	
	/**
	 * 
	 * @param aPath
	 * @param aGenerations
	 * @param aStandardsizePath			在确定路径是规范格式的前提下，可以将此参数设置为false
	 * @return
	 */
	public static String getAncestorPath(String aPath , int aGenerations , boolean aStandardsizePath)
	{
		if(aPath == null)
			return "" ;
		if(aStandardsizePath)
			aPath = toCommonPath(aPath) ;
		if(aGenerations<=0)
			return aPath ;
		int index = XString.lastIndexOf(aPath, '/', aGenerations-1) ;
		if(index != -1)
			return aPath.substring(0, index) ;
		return "" ;
	}
	
	/**
	 * 确保path是以“/”分隔，而非windows的"\"分隔，并且确保如果path不是“/”，则不会以"/"结尾
	 * @return
	 */
	public static String toCommonPath(String aPath)
	{
		if(XString.isEmpty(aPath))
			return aPath ;
		aPath = aPath.replaceAll("(\\\\)+", "/").replaceAll("(/{2,})", "/").trim() ;
		if(aPath.length()>=2)
		{
			//最后一个字符是'/'的话，去掉
			if(aPath.charAt(aPath.length()-1) == '/')
				aPath = aPath.substring(0, aPath.length()-1) ;
			//如果是/c:/...这种格式的去掉开头的/，变成c:/...
			if(aPath.length()>=3)
			{
				int i = aPath.indexOf(':') ;
				if(i != -1)
				{
					int count = XString.count(aPath.substring(0 , i) , '/' , 0) ;
					
					if(count == 1
							&& aPath.startsWith("/"))
					{
						aPath = aPath.substring(1) ;
						count -- ;
						i-- ;
					}
					if(count==0 && i<aPath.length()-1 && aPath.charAt(i+1) != '/')
					{
						//如果后面又紧跟一个冒号：，那很可能是类似jar:file:这种协议，那么这里就跳过
						int k = aPath.indexOf(':', i+1) ;
						if(k != -1 && !aPath.substring(i+1 , k).contains("/"))
						{
							//i到k之间没有/
							
						}
						else
							//说明它是一个windows目录 ，如果“:”后面不是跟"/"，那么加上它
							aPath = XString.splice(aPath.substring(0, i+1) , "/" , aPath.substring(i+1)) ;
					}
				}
			}
		}
		return aPath ;
	}
	
	/**
	 * 取得系统的缓存目录
	 * @return
	 */
	public static File getOsTempDir()
	{
		if(sOsTempDir == null)
			sOsTempDir = new File(JEnvKit.getTempDir()) ;
		return sOsTempDir ;
	}
	
	/**
	 * 指定目录下面的文件采用指定过滤器过滤。返回的结果按文件名升序排列
	 *  
	 * @param aDir
	 * @param aFilter
	 * @return
	 */
	public static File[] getSortedFiles(File aDir, FileFilter aFilter)
	{
		return getSortedFiles(aDir, aFilter , new FileNameComparator()) ;
	}
	
	/**
	 * 指定目录下面的文件采用指定过滤器过滤。返回的结果按指定排序器排序
	 * @param aDir
	 * @param aFilter
	 * @param aComparator
	 * @return
	 */
	public static File[] getSortedFiles(File aDir, FileFilter aFilter, Comparator<File> aComparator)
	{
		File[] files = aDir.listFiles(aFilter) ;		
		if(!XC.isNotEmpty(files))
			Arrays.sort(files, aComparator) ;
		return files ;
	}
	
	/**
	 * 如果aPath是aRootPath的子路径，将返回其相对路径(不以"/"开头)			<br>
	 * 如果aPath不是aRootPOath的子路径，将返回原路径						<br>
	 * 
	 * @param aPath			要求是以"/"分隔的规范路径
	 * @param aRootPath		要求是以"/"分隔的规范路径
	 * @return
	 */
	public static String getSubPath(String aPath , String aRootPath)
	{
		String path = aPath.replace(aRootPath, "") ;
		if(path.length() != aPath.length() && path.startsWith("/"))
			path = path.substring(1) ;
		return path ;
	}
	
	public static Reader openReader(File aFile , String aEncoding) throws IOException
	{
		return new InputStreamReader(new FileInputStream(aFile), aEncoding) ;
	}
	
	public static Writer openWriter(File aFile , String aEncoding) throws IOException
	{
		return new OutputStreamWriter(new FileOutputStream(aFile), aEncoding) ;
	}
	
	public static Writer openWriter(File aFile , String aEncoding , boolean aAppend) throws IOException
	{
		return new OutputStreamWriter(new FileOutputStream(aFile , aAppend), aEncoding) ;
	}
	
	public static OutputStream openOStream(File aFile) throws FileNotFoundException
	{
		aFile.getParentFile().mkdirs() ;
		return new FileOutputStream(aFile) ;
	}
	
	public static OutputStream openBufferedOStream(File aFile) throws FileNotFoundException
	{
		aFile.getParentFile().mkdirs() ;
		return new BufferedOutputStream(new FileOutputStream(aFile) , 102400) ; 
	}
	
	public static InputStream openBufferedInStream(File aFile) throws FileNotFoundException
	{
		return new BufferedInputStream(new FileInputStream(aFile) , 102400) ; 
	}
	
	public static BufferedReader openBufferedReader(File aFile , String aEncoding) throws IOException
	{
		return new BufferedReader(new InputStreamReader(new FileInputStream(aFile), aEncoding) , 10240) ;
	}
	
	public static BufferedReader openBufferedReader(File aFile , Charset aEncoding) throws IOException
	{
		return new BufferedReader(new InputStreamReader(new FileInputStream(aFile), aEncoding) , 10240) ;
	}
	
	public static LineNumberReader openLineNumberReader(File aFile , String aEncoding) throws IOException
	{
		return new LineNumberReader(new InputStreamReader(new FileInputStream(aFile), aEncoding) , 10240) ;
	}
	
	public static BufferedWriter openBufferedWriter(File aFile , String aEncoding) throws IOException
	{
		aFile.getParentFile().mkdirs() ;
		return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(aFile), aEncoding) , 10240) ;
	}
	
	/**
	 * 
	 * @param aFile
	 * @param aEncoding
	 * @param aAppend				true表示是追加,false表示是覆写
	 * @return
	 * @throws IOException
	 */
	public static BufferedWriter openBufferedWriter(File aFile , String aEncoding , boolean aAppend) throws IOException
	{
		aFile.getParentFile().mkdirs() ;
		return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(aFile , aAppend), aEncoding) , 10240) ;
	}
	
	/**
	 * 
	 * 检查文件的路径是否合法				<br>
	 * 只是初步检查，通过检查并不能保证在某些特定的系统里一定合法
	 * @param aPath			可以为null，但必定返回false
	 * @return
	 */
	public static boolean checkFilePath(String aPath)
	{
		if(aPath == null || XString.containsAny(aPath, '\"' , '\''))
			return false ;
		int i = aPath.indexOf(':') ;
		if(i != -1 && XString.containsAny(aPath.substring(0, i) , '/' , '\\'))
			return false ;

		return true ;
	}
	
	/**
	 * 按文件修改时间排序
	 * @param aDir
	 * @param aFilter
	 * @param aLimitAmount
	 * @return
	 */
	public static int limitFileAmount(File aDir , FileFilter aFilter , int aLimitAmount)
	{
		return limitFileAmount(aDir, aFilter, aLimitAmount, new FileModifiedTimeComparator()) ;
	}
	
	/**
	 * 按aComparator排序，如果文件数量超过限定的文件数量，删除数组中高序号的文件，使得文件数量符合限制
	 * @param aDir
	 * @param aFilter
	 * @param aLimitAmount
	 * @param aComparator
	 * @return
	 */
	public static int limitFileAmount(File aDir , FileFilter aFilter , int aLimitAmount , Comparator<File> aComparator)
	{
		Assert.notNull(aComparator , "必须指定文件比较器") ;
		File[] files = aDir.listFiles(aFilter) ;
		if(XC.isEmpty(files))
			return 0 ;
		Arrays.sort(files, aComparator);
		int count = 0 ;
		if(files.length>aLimitAmount)
		{
			for(int i=aLimitAmount ; i<files.length ; i++)
				count += deleteFile(files[i]) ;
		}
		return count ;
	}
	
	/**
	 * 判定一个文件路径是否是绝对路径
	 * @param aPath
	 * @return
	 */
	public static boolean isAbsolutePath(String aPath)
	{
		if(aPath.startsWith("/"))
			return true ;
		else if(aPath.length()>=2)
		{
			int i = aPath.indexOf(':') ;
			if(i != -1)
				return !XString.containsAny(aPath.substring(0, i) , '/' , '\\') ;
		}
		return false ;
	}
	
	public static boolean isRoot(String aPath)
	{
		String path = toCommonPath(aPath) ;
		if("/".equals(path))
			return true ;
		return path.endsWith(":") && path.indexOf('/') == -1 ;
	}
	
	/**
	 * aUpPath是否是aPath的上层路径			<br>
	 * 路径必须是以"/"分隔
	 * 
	 * @param aUpPath			不能为null
	 * @param aPath				不能为null
	 * @return
	 */
	public static boolean isUpper(String aUpPath , String aPath)
	{
		if(aPath.startsWith(aUpPath))
		{
			return aPath.length() == aUpPath.length() || aPath.charAt(aUpPath.length()) == '/' ;
		}
		return false ;
	}
	
	public static void store(File aFile , InputStream aIns , boolean aAppend) throws FileNotFoundException, IOException
	{
		aFile.getParentFile().mkdirs() ;
		StreamAssist.transfer_cc(aIns , new FileOutputStream(aFile , aAppend));
	}
	
	/**
	 * 不关闭输入流
	 * @param aFile
	 * @param aIns
	 * @param aAppend
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void store_2(File aFile , InputStream aIns , boolean aAppend) throws FileNotFoundException, IOException
	{
		aFile.getParentFile().mkdirs() ;
		StreamAssist.transfer_nc(aIns , new FileOutputStream(aFile , aAppend));
	}
	
	/**
	 * 如果指定文件存在，将按序给文件编号，格式为"XX[seq].XX"，确保不会和已有文件重名
	 * @param aFile
	 * @return
	 */
	public static File getSeqFile(File aFile)
	{
		if(!aFile.exists())
			return aFile ;
		File parent = aFile.getParentFile() ;
		String cleanName = getCleanName(aFile) ;
		String extName = getExtName(aFile) ;
		for(int i=1 ; ;i++)
		{
			File file = new File(parent , String.format("%1$s[%2$d].%3$s", cleanName , i , extName)) ;
			if(!file.exists())
				return file ;
		}
	}
	
	public static File[] filterIn(File aDir , FileFilter[] aFilters)
	{
		if(XC.isEmpty(aFilters))
			return aDir.listFiles() ;
		int floor = 0 ;
		List<File> dirList = XC.arrayList(aDir) ;
		while(floor<aFilters.length && !dirList.isEmpty())
		{
			File[] dirs = dirList.toArray(sEmptyFileArray) ;
			dirList.clear();
			for(File dir : dirs)
				XC.addAll(dirList, dir.listFiles(aFilters[floor])) ;
			floor++ ;
		}
		return dirList.toArray(sEmptyFileArray) ;
	}
	
	/**
	 * 文件是否已经被锁
	 * @param aFile
	 * @return
	 * @throws IOException
	 */
	public static boolean isLocked(File aFile) throws IOException
	{
		FileChannel channel = FileChannel.open(aFile.toPath(), StandardOpenOption.WRITE) ;
		FileLock lock = channel.tryLock() ;
		if(lock != null)
		{
			lock.release();
			return true ;
		}
		return false ;
	}
	
	public static void truncateRange(RandomAccessFile aRAF , long aFrom , long aTo) throws IOException
	{
		long size = aRAF.length() ;
		if(aTo>=size)
			aRAF.setLength(aFrom) ;
		else
		{
			byte[] byteBuf = new byte[1024000] ;
			long readP = aTo ;
			long writeP = aFrom ;
			int n = 0;
			aRAF.seek(readP) ;
			while((n = aRAF.read(byteBuf)) != -1)
			{
				aRAF.seek(writeP);
				aRAF.write(byteBuf , 0 , n) ;
				readP += n ;
				writeP += n ;
				aRAF.seek(readP) ;
			}
			aRAF.setLength(writeP) ;
		}
	}
	
	/**
	 * 
	 * @param aFileType		“-”表示常规文件，“d”表示目录，"l"表示链接
	 * @param aDigits		3个0到7的数字
	 * @return
	 */
	public static String toPermissionSymbol(char aFileType , String aDigits)
	{
		Assert.isTrue(aDigits.length() == 3 , "权限的数字表示只能是3个[0,7]的数字字符！") ;
		StringBuilder strBld = new StringBuilder(10).append(aFileType) ;
		for(int i=0 ; i<3 ; i++)
		{
			int d =  aDigits.charAt(i) - '0' ;
			Assert.isTrue(d>=0 && d<=7) ;
			strBld.append(Bits.hit(d, 4)?'r':'-')
				.append(Bits.hit(d, 2)?'w':'-')
				.append(Bits.hit(d, 1)?'x':'-')
				;
		}
		return strBld.toString() ;
	}
	
	public static String toPermissionOCTAL(String aSymbol)
	{
		Assert.notEmpty(aSymbol , "权限字符串不能为空！");
		Assert.isTrue(aSymbol.length()>=9 , "权限字符串的长度不能小于9！") ;
		String symbol = aSymbol.length()>9?aSymbol.substring(aSymbol.length()-9):aSymbol ;
		StringBuilder strBld = new StringBuilder(3) ;
		int i=0 ;
		while(i<symbol.length())
		{
			int d = 0 ;
			if(symbol.charAt(i++) == 'r')
				d |= 4 ;
			if(symbol.charAt(i++) == 'w')
				d |= 2 ;
			if(symbol.charAt(i++) == 'x')
				d |= 1 ;
			strBld.append(d) ;
		}
		return strBld.toString() ;
 	}
}
