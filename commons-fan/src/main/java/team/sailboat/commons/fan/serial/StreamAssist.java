package team.sailboat.commons.fan.serial;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.LongConsumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import team.sailboat.commons.fan.collection.PropertiesEx;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.file.FileUtils;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.log.Log;
import team.sailboat.commons.fan.md5.MD5OutputStream;
import team.sailboat.commons.fan.struct.Bytes;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.fan.text.XStringReader;

public class StreamAssist
{
	public static byte[] toBytes(File aFile)
	{
		ByteArrayOutputStream outs = new ByteArrayOutputStream() ;
		try(FileInputStream ins = new FileInputStream(aFile))
		{
			byte[] bs = new byte[1024] ;
			int len = 0 ;
			while((len=ins.read(bs))>0)
			{
				outs.write(bs, 0, len) ;
			}
			outs.flush() ;
			return outs.toByteArray() ;
		}
		catch (FileNotFoundException e)
		{
			return null ;
		}
		catch (IOException e)
		{
			WrapException.wrapThrow(e) ;
			return null ;		// dead code
		}
	}
	
	/**
	 * 读取流中的数据，返回byte数组			<br>
	 * <b>注意：</b>此方法没有关闭输入流
	 * @param aIns
	 * @return
	 * @throws IOException
	 */
	public static byte[] toBytes(InputStream aIns) throws IOException
	{
		byte[] buf = new byte[1024] ;
		int len = -1 ;
		Bytes bytes = new Bytes(1024) ;
		while((len = aIns.read(buf)) != -1)
			bytes.add(buf, 0, len) ;
		return bytes.toByteArray() ;
	}
	
	/**
	 * 读取流中的数据，返回byte数组			<br>
	 * <b>注意：</b>此方法会关闭输入流
	 * @param aIns
	 * @return
	 * @throws IOException
	 */
	public static byte[] load(InputStream aIns) throws IOException
	{
		try
		{
			return loadAsBytes(aIns).toByteArray() ;
		}
		finally
		{
			StreamAssist.close(aIns);
		}
	}
	
	public static Bytes loadAsBytes(InputStream aIns) throws IOException
	{
		try
		{
			byte[] buf = new byte[1024] ;
			int len = -1 ;
			Bytes bytes = new Bytes(1024) ;
			while((len = aIns.read(buf)) != -1)
				bytes.add(buf, 0, len) ;
			return bytes ;
		}
		finally
		{
			StreamAssist.close(aIns);
		}
	}
	
	public static void save(String aFilePath , byte[] aData) throws IOException
	{
		FileOutputStream fouts = new FileOutputStream(aFilePath) ;
		fouts.write(aData) ;
		fouts.close() ;
	}
	
	public static void save(File aFile , byte[] aData) throws IOException
	{
		FileOutputStream fouts = new FileOutputStream(aFile) ;
		fouts.write(aData) ;
		fouts.close() ;
	}
	
	public static String readAll(Reader aReader) throws IOException
	{
		StringBuilder strBld = new StringBuilder() ;
		char[] buf = new char[1024] ;
		int len = 0 ;
		while((len = aReader.read(buf)) != -1)
			strBld.append(buf, 0, len) ;
		return strBld.toString() ;
	}
	
	public static String readAll(Reader aReader , long aWaitTime)
	{
		char[] buf = new char[512] ;
		int len = 0 ;
		StringBuilder strBld = new StringBuilder() ;
		try
		{
			boolean waited = false ;
			while(true)
			{
				while(aReader.ready() && (len = aReader.read(buf)) != -1)
				{
					if(len>0)
					{
						strBld.append(buf, 0, len) ;
						waited = false ;
					}
				}
				if(!waited)
				{
					try
					{
						Thread.sleep(aWaitTime) ;
					}
					catch(Exception e)
					{}
					waited = true ;
				}
				else
					break ;
			}
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace() ;
		}
		return strBld.toString() ;
	}
	
	public static ByteBuffer load(File aFile) throws IOException
	{
		try(InputStream ins = new FileInputStream(aFile))
		{
			byte[] buf = new byte[1024] ;
			int len ;
			ByteBuffer bbuf = ByteBuffer.allocate((int)aFile.length()) ;
			while((len=ins.read(buf)) != -1)
				bbuf.put(buf, 0, len) ;
			return bbuf ;
		}
	}
	
	public static byte[] load_1(File aFile) throws IOException
	{
		try(InputStream ins = new FileInputStream(aFile))
		{
			byte[] buf = new byte[1024] ;
			int len ;
			Bytes bytes = new Bytes(1024) ;
			while((len=ins.read(buf)) != -1)
				bytes.add(buf, 0, len) ;
			return bytes.toByteArray() ;
		}
	}
	
	public static StringBuilder load(File aFile , String aEncoding) throws IOException
	{
		try(Reader reader = FileUtils.openReader(aFile, aEncoding))
		{
			char[] buf = new char[5120] ;
			StringBuilder strBld = new StringBuilder() ;
			int len = 0 ;
			while((len = reader.read(buf)) != -1)
				strBld.append(buf, 0, len) ;
			return strBld ;
		}
	}
	
	public static StringBuilder load(InputStream aIns , Charset aEncoding) throws IOException
	{
		try(Reader reader = new InputStreamReader(aIns , aEncoding))
		{
			char[] buf = new char[5120] ;
			StringBuilder strBld = new StringBuilder() ;
			int len = 0 ;
			while((len = reader.read(buf)) != -1)
				strBld.append(buf, 0, len) ;
			return strBld ;
		}
	}
	
	/**
	 * 
	 * @param aIns			会关闭
	 * @param aEncoding
	 * @return
	 * @throws IOException
	 */
	public static StringBuilder load(InputStream aIns , String aEncoding) throws IOException
	{
		return load(aIns, Charset.forName(aEncoding)) ;
	}
	
	public static void save(File aFile , String aEncoding , String aStr) throws IOException
	{
		try(Writer writer = FileUtils.openWriter(aFile, aEncoding))
		{
			writer.write(aStr);
		}
	}
	
	public static void writeTo(OutputStream aOuts , CharSequence aContent , Charset aCharset , boolean aClose) throws IOException
	{
		OutputStreamWriter outsW = new OutputStreamWriter(aOuts , aCharset) ;
		try
		{
			outsW.write(aContent.toString());
		}
		finally
		{
			if(aClose)
				StreamAssist.close(outsW) ;
		}
	}
	
	public static void save(File aFile , String aEncoding , List<String> aLines , boolean aAppend) throws IOException
	{
		try(Writer writer = FileUtils.openWriter(aFile, aEncoding , aAppend))
		{
			if(XC.isNotEmpty(aLines))
			{
				for(String line : aLines)
					writer.append(JCommon.defaultIfNull(line, "")).append(XString.sLineSeparator) ;
			}
		}
	}
	
	/**
	 * 修改下面文件的编码
	 */
	public static void changeEncode(File aDir , FileFilter aFilter , Charset aTargetCharset , Charset... aPossibles)
	{
		File[] files = aDir.listFiles(aFilter) ;
		if(files != null && files.length>0)
		{
			List<File> dirs = new ArrayList<>() ;
			for(File file : files)
			{
				if(file.isDirectory())
					dirs.add(file) ;
				else
				{
					byte[] data = toBytes(file) ;
					ByteBuffer buf = ByteBuffer.wrap(data) ;
					if(!XString.isCompatible(aTargetCharset, buf))
					{
						for(Charset charset : aPossibles)
						{
							if(XString.isCompatible(charset, buf))
							{
								try(Writer writer = new OutputStreamWriter(new FileOutputStream(file) , aTargetCharset)
										; InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(data), charset))
								{
									int ch ;
									while((ch=reader.read()) != -1)
										writer.write(ch) ;
									break ;
								}
								catch(Exception e)
								{
									Log.warn("编码转换（{} --> {}）失败！文件：{}" , charset.name() , aTargetCharset.name() 
											, file.getAbsolutePath()) ;
								}
							}
						}
					}
				}
			}
			if(dirs.size()>0)
			{
				for(File dir : dirs)
					changeEncode(dir, aFilter, aTargetCharset, aPossibles);
			}
		}
	}
	
	/**
	 * 流间转移数据				<br>
	 * 方法执行结束，关闭输入流，关闭输出流
	 * @throws IOException 
	 */
	public static void transfer_cc(InputStream aIn, OutputStream aOut) throws IOException 
	{
		try
		{
			byte by[] = new byte[102400];
			int length = 0;
			while ((length = aIn.read(by)) != -1)
			{
				aOut.write(by, 0, length);
			}
		}
		finally
		{
			StreamAssist.close(aIn);
			StreamAssist.close(aOut);
		}
	}
	
	/**
	 * 关闭输入流，关闭输出流
	 * @param aIn
	 * @param aOut
	 * @param aMaxBytes
	 * @throws IOException
	 */
	public static void transfer_cc(InputStream aIn, OutputStream aOut , long aMaxBytes) throws IOException 
	{
		try
		{
			transfer_nn(aIn, aOut, aMaxBytes) ;
		}
		finally
		{
			StreamAssist.close(aIn);
			StreamAssist.close(aOut);
		}
	}
	
	/**
	 * 既不关闭输入流，也不去关闭输出流
	 * @param aIn
	 * @param aOut
	 * @throws IOException
	 */
	public static void transfer_nn(InputStream aIn, OutputStream aOut) throws IOException 
	{
		transfer_nn(aIn, aOut, Long.MAX_VALUE);
	}
	/**
	 * 既不关闭输入流，也不去关闭输出流
	 * @param aIn
	 * @param aOut
	 * @param aMaxBytes
	 * @throws IOException
	 */
	public static void transfer_nn(InputStream aIn, OutputStream aOut , long aMaxBytes) throws IOException 
	{
		if(aMaxBytes <= 102_400)
		{
			byte[] buf = new byte[(int)aMaxBytes] ;
			final int len = aIn.read(buf) ;
			// 一次读取就可以了
			aOut.write(buf, 0, len);
		}
		else
		{
			byte by[] = new byte[102400];
			int length = 0;
			int count = 0 ;
			while ((length = aIn.read(by)) != -1)
			{
				count += length ;
				if(count >= aMaxBytes)
				{
					aOut.write(by, 0, (int)(length - (count - aMaxBytes)));
					break ;
				}
				else
					aOut.write(by, 0, length);
			}
		}
	}
	
	/**
	 * 关闭输入流，但不关闭输出流
	 * @param aIn
	 * @param aOut
	 * @throws IOException
	 */
	public static long transfer_cn(InputStream aIn, OutputStream aOut) throws IOException 
	{
		return transfer_cn(aIn, aOut,  null) ;
	}
	
	public static long transfer_cn(InputStream aIn, OutputStream aOut , LongConsumer aMonitor) throws IOException 
	{
		try
		{
			byte[] by = new byte[102400];
			int length = 0;
			long totalLen = 0 ;
			while ((length = aIn.read(by)) != -1)
			{
				aOut.write(by, 0, length);
				totalLen += length ;
				if(aMonitor != null)
					aMonitor.accept(totalLen) ;
			}
			return totalLen ;
		}
		finally
		{
			StreamAssist.close(aIn);
		}
	}
	
	/**
	 * 不关闭输入流，关闭输出流
	 * @param aIn
	 * @param aOut
	 * @throws IOException
	 */
	public static void transfer_nc(InputStream aIn, OutputStream aOut) throws IOException 
	{
		try
		{
			byte by[] = new byte[102400];
			int length = 0;
			while ((length = aIn.read(by)) != -1)
				aOut.write(by, 0, length);
		}
		finally
		{
			StreamAssist.close(aOut);
		}
	}
	
	/**
	 * 不关闭输入流，关闭输出流
	 * @param aIn
	 * @param aOut
	 * @return
	 * @throws IOException
	 */
	public static String transfer_nc_md5(InputStream aIn, OutputStream aOut) throws IOException 
	{
		MD5OutputStream mouts = new MD5OutputStream(aOut) ;
		transfer_nc(aIn, mouts) ;
		return mouts.getMD5().asHex() ;
	}
	
	public static void close(Object aCloseable)
	{
		if(aCloseable != null)
		{
			if(aCloseable instanceof AutoCloseable)
			{
				try
				{
					((AutoCloseable)aCloseable).close();
				}
				catch (Exception e)
				{}
			}
		}
	}
	
	public static void close(AutoCloseable aCloseable)
	{
		if(aCloseable != null)
		{
			try
			{
				aCloseable.close();
			}
			catch (Exception e)
			{}
		}
	}
	
	public static void closeAll(AutoCloseable...aCloseables)
	{
		if(XC.isNotEmpty(aCloseables))
		{
			for(AutoCloseable c : aCloseables)
				close(c); 
		}
	}
	
	public static void closeAll(Object...aCloseables)
	{
		if(XC.isNotEmpty(aCloseables))
		{
			for(Object c : aCloseables)
			{
				if(c != null && c instanceof AutoCloseable)
					close((AutoCloseable)c);
			}
		}
	}
	
	/**
	 * 读取index索引文件。一行一个完整的特征值。编码采用UTF-8
	 * @param aIns
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public static List<String> loadLines(InputStream aIns) throws UnsupportedEncodingException, IOException
	{
		return loadLines(aIns , "UTF-8") ;
	}
	
	public static List<String> loadLines(InputStream aIns , String aEncoding) throws UnsupportedEncodingException, IOException
	{
		return loadLines(aIns, aEncoding, true) ;
	}
	
	public static List<String> loadLines(InputStream aIns , String aEncoding , boolean aTrim) throws UnsupportedEncodingException, IOException
	{
		String line = null ;
		List<String> indexLines = new ArrayList<>() ;
		try(LineNumberReader reader = new LineNumberReader(new InputStreamReader(aIns , aEncoding) , 1024))
		{
			while((line=reader.readLine()) != null)
			{
				indexLines.add(aTrim?line.trim():line) ;
			}
		}
		return indexLines ;
	}
	
	public static List<String> loadLines(File aFile , String aEncoding) throws UnsupportedEncodingException, IOException
	{
		return loadLines(aFile, aEncoding, true) ;
	}
	
	public static List<String> loadLines(File aFile , String aEncoding , boolean aTrim) throws UnsupportedEncodingException, IOException
	{
		try(InputStream ins = new FileInputStream(aFile))
		{
			return loadLines(ins , aEncoding , aTrim) ;
		}
	}
	
	/**
	 * 
	 * @param aIns		会关闭
	 * @return
	 */
	public static PropertiesEx loadMF(InputStream aIns)
	{
		try
		{
			StringBuilder content = StreamAssist.load(aIns, "UTF-8") ;
			XStringReader reader = new XStringReader(content.toString()) ;
			StringBuilder valBld = new StringBuilder() ;
			String line = null ;
			String key = null ;
			PropertiesEx prop = new PropertiesEx() ;
			while((line = reader.readLine()) != null)
			{
				int i = line.indexOf(':') ;
				if(i != -1)
				{
					if(valBld.length()>0 && key != null)
					{
						prop.put(key, valBld.toString().trim()) ;
						key = null ;
						valBld = new StringBuilder() ;
					}
					key = line.substring(0, i).trim() ;
					valBld.append(line.substring(i+1)) ;
				}
				else
					valBld.append('\n').append(line) ;
			}
			if(valBld.length()>0 && key != null)
			{
				prop.put(key, valBld.toString().trim()) ;
			}
			return prop ;
		}
		catch (IOException e)
		{
			throw new IllegalStateException(e) ;
		}
	}
	
	public static InputStream getProjectRootResource(String aResourceName , String aClassRootURL) throws MalformedURLException, IOException
	{
		if(aClassRootURL.startsWith("file:/"))
		{
			return URI.create(FileUtils.getPath(FileUtils.getAncestorPath(aClassRootURL, aClassRootURL.endsWith("/target/classes")?2:1) , aResourceName))
					.toURL()
					.openStream() ;
		}
		else
		{
			return URI.create(FileUtils.getPath(aClassRootURL, aResourceName))
					.toURL()
					.openStream() ;
		}
	}
	
	public static File[] unzip(File aZipFile , String aEncoding , File aTargetDir) throws IOException
	{
		return unzip(new FileInputStream(aZipFile) , aEncoding , aTargetDir) ;
	}
	
	public static File[] unzip(InputStream aIns , String aEncoding , File aTargetDir) throws IOException
	{
		List<File> fileList = new ArrayList<>() ;
		try(ZipInputStream zins = new ZipInputStream(aIns , Charset.forName(aEncoding)))
		{
			ZipEntry zentry = null ;
			while((zentry = zins.getNextEntry()) != null)
			{
				if(zentry.isDirectory())
				{
					new File(aTargetDir , zentry.getName()).mkdirs() ;
				}
				else
				{
					File file = new File(aTargetDir , zentry.getName()) ;
					file.getParentFile().mkdirs() ;
					transfer_nc(zins, new FileOutputStream(file));
					fileList.add(file) ;
				}
			}
		}
		return fileList.toArray(new File[0]) ;
	}
	
	public static void appendLines(File aFile , String aEncoding , Collection<String> aLines) throws IOException
	{
		try(Writer writer = FileUtils.openBufferedWriter(aFile, aEncoding , true))
		{
			appendLines(writer, aLines);
		}
	}
	
	public static void appendLines(Writer aWriter , Collection<String> aLines) throws IOException
	{
		if(XC.isEmpty(aLines))
			return ;
		for(String line : aLines)
			aWriter.append(line).write(XString.sLineSeparator) ;
	}
	
	public static void appendLines(Writer aWriter , String... aLines) throws IOException
	{
		if(XC.isNotEmpty(aLines))
			return ;
		for(String line : aLines)
			aWriter.append(line).write(XString.sLineSeparator) ;
	}
	
	public static String readString(InputStream aIns , int aLen , String aEncoding) throws IOException
	{
		byte[] bytes = new byte[aLen] ;
		int len = aIns.read(bytes) ;
		if(len>0)
			return new String(bytes ,  0  , len , aEncoding) ;
		else
			return "" ;
	}
}
