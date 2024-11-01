package team.sailboat.commons.fan.serial;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.attribute.FileTime;
import java.util.function.Predicate;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.file.FileUtils;
import team.sailboat.commons.fan.lang.Assert;

public class ZlibUtil
{
	/**
	 * 将数据压缩后，写入到输出流
	 * @param aData
	 * @param aOuts
	 * @throws IOException
	 */
	public static void compress(byte[] aData , OutputStream aOuts) throws IOException
	{
		DeflaterOutputStream douts = new DeflaterOutputStream(aOuts) ;
		douts.write(aData);
		douts.close();
	}
	
	public static byte[] compress(byte[] aData) throws IOException
	{
		ByteArrayOutputStream bouts = new ByteArrayOutputStream() ;
		DeflaterOutputStream douts = new DeflaterOutputStream(bouts) ;
		douts.write(aData);
		douts.close();
		return bouts.toByteArray() ;
	}
	
	/**
	 * 解压GZ，不会关闭输入流和输出流
	 * @param aIns
	 * @param aOuts
	 * @throws IOException
	 */
	public static void ungz(InputStream aIns ,  OutputStream aOuts) throws IOException
	{
		GZIPInputStream zins = new GZIPInputStream(aIns) ;
		byte[] buf = new byte[102400] ;
		int size = 0 ;
		while((size = zins.read(buf)) != -1)
			aOuts.write(buf , 0 , size);
	}
	
	/**
	 * 压缩完以后会关闭输入流
	 * @param aIns
	 * @return
	 * @throws IOException
	 */
	public static byte[] compress(InputStream aIns) throws IOException
	{
		ByteArrayOutputStream bouts = new ByteArrayOutputStream() ;
		DeflaterOutputStream douts = new DeflaterOutputStream(bouts) ;
		StreamAssist.transfer_cc(aIns, douts) ;
		return bouts.toByteArray() ;
	}
	
	public static byte[] uncompress(byte[] aData) throws IOException
	{
		ByteArrayOutputStream bouts = new ByteArrayOutputStream() ;
		InflaterOutputStream iouts = new InflaterOutputStream(bouts) ;
		iouts.write(aData);
		iouts.close();
		return bouts.toByteArray() ;
	}
	
	public static byte[] uncompress(InputStream aIns) throws IOException
	{
		ByteArrayOutputStream bouts = new ByteArrayOutputStream() ;
		InflaterOutputStream iouts = new InflaterOutputStream(bouts) ;
		StreamAssist.transfer_cc(aIns, iouts);
		return bouts.toByteArray() ;
	}
	
	/**
	 * 执行结束，将关闭输入流和输出流
	 * @param aIns
	 * @param aOuts
	 * @throws IOException
	 */
	public static void uncompress(InputStream aIns , OutputStream aOuts) throws IOException
	{
		InflaterOutputStream iouts = new InflaterOutputStream(aOuts) ;
		StreamAssist.transfer_cc(aIns, iouts);
	}
	
	/**
	 * 解压Zip流里面符合条件的文件，从aStartIndex开始，共aMaxAmount个			<br>
	 * 这主要用在zip文件夹中存在一个文件夹下压缩有太多的文件的情况
	 * @param aZins
	 * @param aOutDir
	 * @param aFilter
	 * @param aStartIndex
	 * @param aMaxAmount
	 * @throws IOException
	 */
	public static void uncompress(ZipInputStream aZins , File aOutDir , Predicate<ZipEntry> aFilter 
			, int aStartIndex , int aMaxAmount) throws IOException
	{
		ZipEntry ze = null ;
		int i=0 ;
		while((ze = aZins.getNextEntry()) != null)
		{
			if(aFilter.test(ze))
			{
				if(i>=aStartIndex)
				{
					if(i<aStartIndex+aMaxAmount)
					{
						File file = new File(aOutDir , ze.getName()) ;
						if(ze.isDirectory())
							file.mkdirs() ;
						else
						{
							StreamAssist.transfer_nc(aZins, FileUtils.openBufferedOStream(file));
						}
					}
					else
						break ;
				}
				i++ ;
			}
		}
	}
	
	/**
	 * 
	 * @param aInZipDir			要压缩的文件或目录
	 * @param aOutsZip			Zip文件输出流
	 * @param aIgnoreRootDir	是否忽略指定的根文件夹
	 * @param aBuildDir			是否要在压缩文件内部构造出层次结构
	 * @throws IOException
	 */
	public static void compress(File aInZipDir , OutputStream aOutsZip 
			, boolean aIgnoreRootDir , boolean aBuildDir) throws IOException
	{
		try(ZipOutputStream outs = new ZipOutputStream(aOutsZip))
		{
			File inputFile = aInZipDir ;
			Assert.isTrue(inputFile.exists() , "绝对路径文件不存在：%s" , inputFile.getAbsolutePath()) ;
		
			if(inputFile.isDirectory())
			{
				if(!aIgnoreRootDir && aBuildDir)
				{
					String path = inputFile.getName()+"/" ;
					outs.putNextEntry(new ZipEntry(path));
				}
				recursionCompress(outs, inputFile, aIgnoreRootDir?inputFile.getAbsolutePath():inputFile.getParentFile().getAbsolutePath() , true);
			}
			else
			{
				String path = inputFile.getName() ;
				ZipEntry zentry = new ZipEntry(path) ;
				zentry.setSize(inputFile.length()) ;
				zentry.setLastModifiedTime(FileTime.fromMillis(inputFile.lastModified())) ;
				outs.putNextEntry(zentry) ;
				StreamAssist.transfer_cn(new FileInputStream(inputFile) , outs) ;
			}
		}
	}
	
	static void recursionCompress(ZipOutputStream aZouts , File aDirectory , String aBaseDir 
			, boolean aBuildDir) throws IOException
	{
		File[] files = aDirectory.listFiles() ;
		if(XC.isEmpty(files))
			return ;
		int len = aBaseDir.length() ;
		for(File file : files)
		{
			String path = file.getAbsolutePath() ;
			Assert.isTrue(path.startsWith(aBaseDir)) ;
			path = FileUtils.toCommonPath(path.substring(aBaseDir.endsWith("/")||aBaseDir.endsWith("\\")?len:len+1)) ;
			if(file.isDirectory())
			{
				if(aBuildDir)
				{
					if(!path.endsWith("/"))
						path += "/" ;
					aZouts.putNextEntry(new ZipEntry(path)) ;
				}
				recursionCompress(aZouts, file, aBaseDir, aBuildDir);
			}
			else
			{
				ZipEntry zentry = new ZipEntry(path) ;
				zentry.setSize(file.length());
				zentry.setLastModifiedTime(FileTime.fromMillis(file.lastModified())) ;
				aZouts.putNextEntry(zentry) ;
				StreamAssist.transfer_cn(new FileInputStream(file) , aZouts) ;
			}
		}
	}
}
