package team.sailboat.commons.fan.collection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.file.FileUtils;
import team.sailboat.commons.fan.infc.IStringSerializable;
import team.sailboat.commons.fan.json.JSONException;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.serial.StreamAssist;
import team.sailboat.commons.fan.text.XString;

public class PropertiesEx extends Properties implements IStringSerializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	boolean mTrim = false ;
	
	public PropertiesEx()
	{
		super() ;
	}
	
	public PropertiesEx(Properties aDefault)
	{
		super(aDefault) ;
	}
	
	public PropertiesEx(Map<String, ? extends Object> aMap)
	{
		if(XC.isNotEmpty(aMap))
		{
			for(Map.Entry<String , ? extends Object> entry : aMap.entrySet())
			{
				put(entry.getKey(), entry.getValue()) ;
			}
		}
	}
	
	@Override
	public String getProperty(String aPropertyName)
	{
		return deSecret(super.getProperty(aPropertyName)) ;
	}
	
	public void putSecret(String aPropertyName , String aSecret)
	{
		put(aPropertyName, asSecret(aSecret)) ;
	}
	
	/**
	 * 如果是字符串类型，用","分隔各个数组元素
	 * @param aKey
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String[] getStringArray(String aKey)
	{	
		Object obj = get(aKey) ;
		if(obj == null)
			return null ;
		if(obj instanceof String[])
			return (String[])obj ;
		if(obj instanceof String)
			return split((String)obj) ;
		if(XC.isArray(obj))
		{
			Object[] objs = (Object[])obj ;
			String[] array = new String[objs.length] ;
			for(int i=0 ; i<array.length ; i++)
				array[i] = objs[i]==null?null:objs[i].toString() ;
			return array ;
		}
		else if(obj instanceof List)
		{
			return XC.extract((List<Object>)obj , (ele)->ele!=null?ele.toString():null , String.class) ;
		}
		throw new IllegalStateException(String.format("键[%1$s]的值类型为%2$s，不适合用getInteger()方法来取得"
				, aKey , obj.getClass().getName())) ;
	}
	
	public String getString(String aKey)
	{
		return getProperty(aKey) ;
	}
	
	public <E extends Enum<E>> E getEnum(String aKey , Class<E> aClazz)
	{
		String v = getString(aKey) ;
		return XString.isEmpty(v)?null:Enum.valueOf(aClazz, v) ;
	}
	
	public String getString(String aKey , String aDefaultValue)
	{
		return getProperty(aKey, aDefaultValue) ;
	}
	
	public boolean getBoolean(String aKey , boolean aDefaultValue)
	{
		String value = getString(aKey) ;
		if(value != null && !value.isEmpty())
		{
			try
			{
				return Boolean.parseBoolean(value) ;
			}
			catch(Exception e)
			{
			}
		}
		return aDefaultValue ;
	}
	
	public Boolean getBoolean(String aKey)
	{
		Object obj = get(aKey) ;
		if(obj == null)
			return null ;
		if(obj instanceof Boolean)
			return (Boolean)obj ;
		if(obj instanceof String)
			return Boolean.valueOf((String)obj) ;
		return null ;
	}
	
	public void putStringArray(String aKey , String...aVals)
	{
		if(aVals != null)
		{
			if(aVals.length == 1)
				put(aKey, aVals[0]) ;
			else
			{
				StringBuilder strBld = new StringBuilder() ;
				for(int i=0 ; i<aVals.length ; i++)
				{
					if(i>0)
						strBld.append(',') ;
					strBld.append(aVals[i]) ;
				}
				put(aKey, strBld.toString()) ;
			}
		}
		else
			put(aKey, null) ;
	}
	
	public void putAll(IMultiMap<String, String> aMmap)
	{
		if(aMmap != null)
		{
			for(String key : aMmap.keySet())
			{
				setProperty(key, XString.toString(",", aMmap.get(key))) ;
			}
		}
	}
	
	public void load(File aFile) throws IOException
	{
		try(Reader reader = FileUtils.openBufferedReader(aFile, "UTF-8"))
		{
			super.load(reader) ;
		}
	}
	
	public void load(File aFile , String aCharset) throws IOException
	{
		try(Reader reader = FileUtils.openBufferedReader(aFile, aCharset))
		{
			super.load(reader) ;
		}
	}
	
	public void load(File aFile , Charset aCharset) throws IOException
	{
		try(Reader reader = FileUtils.openBufferedReader(aFile, aCharset))
		{
			super.load(reader) ;
		}
	}
	
	public void store(File aFile , String aCharset) throws IOException
	{
		Writer writer = null ;
		try
		{
			writer = new OutputStreamWriter(new FileOutputStream(aFile), aCharset) ;
			super.store(writer, "") ;
		}
		finally
		{
			if(writer != null)
				writer.close(); 
		}
	}
	
	public void store(File aFile , Charset aCharset) throws IOException
	{
		Writer writer = null ;
		try
		{
			writer = new OutputStreamWriter(new FileOutputStream(aFile), aCharset) ;
			super.store(writer, "") ;
		}
		finally
		{
			if(writer != null)
				writer.close(); 
		}
	}
	
	/**
	 * 缺省用UTF-8编码存储
	 * @param aFile
	 * @throws IOException
	 */
	public void store(File aFile) throws IOException
	{
		store(aFile, "UTF-8") ;
	}
	
	public int getInt(String aKey , int aDefaultValue)
	{
		String val = getProperty(aKey) ;
		if(val != null)
		{
			try
			{
				return Integer.parseInt(val) ;
			}
			catch(Exception e)
			{}
		}
		return aDefaultValue ;
	}
	
	public Integer getInteger(String aKey)
	{
		Object obj = get(aKey) ;
		if(obj == null)
			return null ;
		if(obj instanceof Integer)
			return (Integer)obj ;
		if(obj instanceof String)
			return Integer.valueOf((String)obj) ;
		throw new IllegalStateException(String.format("键[%1$s]的值类型为%2$s，不适合用getInteger()方法来取得"
				, aKey , obj.getClass().getName())) ;
	}
	
	public float getFloat(String aKey , float aDefaultValue)
	{
		String val = getProperty(aKey) ;
		if(val != null)
		{
			try
			{
				return Float.parseFloat(val) ;
			}
			catch(Exception e)
			{}
		}
		return aDefaultValue ;
	}
	
	public long getLong(String aKey , long aDefaultValue)
	{
		String val = getProperty(aKey) ;
		if(val != null)
		{
			try
			{
				return Long.parseLong(val) ;
			}
			catch(Exception e)
			{}
		}
		return aDefaultValue ;
	}
	
	public double getDouble(String aKey , double aDefaultValue)
	{
		String val = getProperty(aKey) ;
		if(val != null)
		{
			try
			{
				return Double.parseDouble(val) ;
			}
			catch(Exception e)
			{}
		}
		return aDefaultValue ;
	}
	
	public static String[] split(String value)
	{
		if(value != null && !value.isEmpty())
		{
			String[] segs = value.split(",") ;
			for(int i =0 ; i<segs.length ; i++)
				segs[i] = segs[i].trim() ;
			return segs ;
		}
		return null ;
	}
	
	public static String concat(String...aArray)
	{
		if(aArray == null || aArray.length == 0)
			return null ;
		else
			return XString.toString(",", aArray) ;
	}
	
	public static PropertiesEx loadFromFile(File aFile) throws IOException
	{
		PropertiesEx propEx = new PropertiesEx() ;
		if(aFile.exists())
			propEx.load(aFile);
		return propEx ;
	}
	
	/**
	 * 读取完成，将关闭Reader
	 * @param aReader
	 * @return
	 * @throws IOException
	 */
	public static PropertiesEx loadFromReader(Reader aReader) throws IOException
	{
		try
		{
			PropertiesEx propEx = new PropertiesEx() ;
			propEx.load(aReader);
			return propEx ;
		}
		finally
		{
			StreamAssist.close(aReader) ;
		}
	}
	
	public static PropertiesEx loadFromReader(Reader aReader , Properties aDefault) throws IOException
	{
		try
		{
			PropertiesEx propEx = new PropertiesEx(aDefault) ;
			propEx.load(aReader);
			return propEx ;
		}
		finally
		{
			StreamAssist.close(aReader) ;
		}
	}
	
	public static PropertiesEx loadFromFile(File aFile , String aEncoding) throws IOException
	{
		return loadFromFile(aFile, aEncoding, true) ;
	}

	public static PropertiesEx loadFromFile(File aFile , String aEncoding , boolean aTrim) throws IOException
	{
		PropertiesEx propEx = new PropertiesEx() ;
		propEx.mTrim = aTrim ;
		if(aFile.exists())
		{
			try(InputStream ins = new FileInputStream(aFile))
			{
				propEx.load(new InputStreamReader(ins , aEncoding));
			}
			propEx.stringPropertyNames() ;
		}
		return propEx ;
	}
	
	@Override
	public synchronized Object put(Object aKey, Object aValue)
	{
		if(mTrim)
		{
			if(aKey instanceof String)
			{
				if(aValue instanceof String)
					return super.put(((String) aKey).trim() , ((String) aValue).trim()) ;
				else
					return super.put(((String) aKey).trim(), aValue) ;
			}
		}
		return super.put(aKey, aValue);
	}

	@Override
	public String squash()
	{
		JSONObject jobj = new JSONObject() ;
		try
		{
			for(Map.Entry<Object , Object> entry : entrySet())
			{
				jobj.put(entry.getKey().toString(), entry.getValue()) ;
			}
			return jobj.toString() ;
		}
		catch (JSONException e)
		{
			throw new IllegalStateException(e) ;
		}
	}

	@Override
	public void inflate(String aStr)
	{
		JSONObject jobj = JSONObject.of(aStr) ;
		jobj.forEach(this::put) ;
	}
	
	public Map<String, String> toStringMap()
	{
		Map<String, String> map = XC.hashMap() ;
		for(Map.Entry<Object , Object> entry : entrySet())
		{
			map.put(JCommon.toString(entry.getKey() , null) 
					, JCommon.toString(entry.getValue() , null)) ;
		}
		return map ;
	}
	
	public static int[] asIntArray(Object aVal)
	{
		if(aVal == null)
			return null ;
		if(aVal instanceof int[])
			return (int[])aVal ;
		if(aVal instanceof String)
		{
			String[] vals = split((String)aVal) ;
			int[] array = new int[vals.length] ;
			for(int i=0 ; i<vals.length ; i++)
				array[i] = Integer.parseInt(vals[i]) ;
			return array ;
		}
		else
			throw new IllegalStateException("无法解析"+aVal.getClass().getName()+"类型的对象为int[]") ;
	}
	
	public static String asSecret(String aText)
	{
		return XString.isEmpty(aText)?aText:"?"+JCommon.encrypt(aText)+"$!" ;
	}
	
	public static String deSecret(String aCiphertext)
	{
		if(XString.isNotEmpty(aCiphertext))
		{
			final int len = aCiphertext.length() ; 
			if(len>3 && aCiphertext.charAt(0) == '?' && aCiphertext.charAt(len-1) =='!' && aCiphertext.charAt(len-2) == '$')
			{
				//说明这是一个密文，需要解密
				try
				{
					return JCommon.decrypt(aCiphertext.substring(1, len-2)) ;
				}
				catch(Throwable e)
				{}
			}
		}
		return aCiphertext ;
	}
	
	public static String toString(Properties aProp)
	{
		StringWriter strWriter = new StringWriter() ;
		try
		{
			aProp.store(strWriter, null);
		}
		catch (IOException e)
		{
			WrapException.wrapThrow(e) ;
		}
		return strWriter.toString() ;
	}
	
	public static String toString(Properties aProp , boolean aSortedKey , boolean aOutputDate)
	{
		String result = toString(aProp) ;
		if(result.length() > 0)
		{
			if(!aOutputDate)
			{
				// 移除第一行日期
				if(result.charAt(0) == '#')
				{
					int i = result.indexOf(XString.sLineSeparator) ;
					if(i != -1)
					{
						result = result.substring(i+XString.sLineSeparator.length()) ;
					}
				}
			}
			if(aSortedKey)
			{
				String[] segs = result.split(XString.sLineSeparator) ;
				java.util.Arrays.sort(segs) ;
				result = XString.toString(XString.sLineSeparator, segs) ;
			}
		}
		return result ;
	}
}
