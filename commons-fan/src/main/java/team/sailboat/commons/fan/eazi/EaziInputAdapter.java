package team.sailboat.commons.fan.eazi;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.function.Function;

import team.sailboat.commons.fan.collection.ExtensibleList;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.serial.PositionDataInputStream;
import team.sailboat.commons.fan.struct.Bytes;

public abstract class EaziInputAdapter implements SerialConstants
{	
	Map<Short , Class<?>> mClassMap = new HashMap<>() ;
	String mVersion ;
	PositionDataInputStream mDataIns ;
	
	List<Object> mObjs = new ExtensibleList<Object>(256) ;
	ClassLoader mClassLoader ;
	
	Stack<Runnable> mRunnables = new Stack<Runnable>() ;
	
	Function<String, Class<?>> mClassProvider ;
	
	public EaziInputAdapter(PositionDataInputStream aDataIns , ClassLoader aClassLoader)
	{
		mDataIns = aDataIns ;
		mClassLoader = aClassLoader ;
	}
	
	public void setClassProvider(Function<String, Class<?>> aClassProvider)
	{
		mClassProvider = aClassProvider;
	}
	
	/**
	 * 长度(1字节byte)
	 * 数据
	 * @return
	 * @throws IOException 
	 */
	protected String readKey() throws IOException
	{
		try
		{
			return mDataIns.readUTF() ;
		}
		catch (EOFException e)
		{
			return null ;
		}
	}
	
	protected Object readObject() throws IOException
	{
		byte tag = mDataIns.readByte() ; 
		switch(tag)
		{
		case TC_Int:
			return mDataIns.readInt() ;
		case TC_String:
			return mDataIns.readUTF() ;
		case TC_Float:
			return mDataIns.readFloat() ;
		case TC_Double:
			return mDataIns.readDouble() ;
		case TC_Boolean :
			return mDataIns.readBoolean() ;
		case TC_Byte:
			return mDataIns.readByte() ;
		case TC_Long:
			return mDataIns.readLong() ;
		case TC_NULL:
			return null ;
		case TC_StringIntern:
			return readStringIntern() ;
		case TC_Eazialiable:
			return readEazialiable() ;
		case TC_Reference :
			return readReference() ;
		case TC_Array :
			return readArray() ;
		case TC_List:
			return readList() ;
		case TC_Map:
			return readMap() ;
		case TC_FloatArray:
			return readFloats() ;
		case TC_IntArray:
			return readInts() ;
		case TC_ByteArray:
			return readBytes() ;
		case TC_DoubleArray:
			return readDoubles() ;
		case TC_StringArray:
			return readStrings() ;
		case TC_StringInternArray:
			return readStringInternArray() ;
		case TC_JavaSerializable:
			return readJavaSerializable() ;
		case TC_Set:
			return readSet() ;
		default:
			throw new IllegalStateException("无法识别的类型标记"+Integer.toHexString(tag)) ;
		}
	}
	
	/**
	 * 句柄(4字节int)
	 * 数组长度(4字节int)
	 * 数据
	 * @return
	 * @throws IOException 
	 */
	protected double[] readDoubles() throws IOException
	{
		int handle = mDataIns.readInt() ;
		int len = mDataIns.readInt() ;
		double[] vals = new double[len] ;
		for(int i=0 ; i<len ; i++)
		{
			vals[i] = mDataIns.readDouble() ;
		}
		mObjs.set(handle, vals) ;
		return vals ;
	}
	
	protected String[] readStrings() throws IOException
	{
		int handle = mDataIns.readInt() ;
		int len = mDataIns.readInt() ;
		String[] vals = new String[len] ;
		for(int i=0 ; i<len ; i++)
		{
			vals[i] = mDataIns.readUTF() ;
		}
		mObjs.set(handle, vals) ;
		return vals ;
	}
	
	/**
	 * 句柄(4字节int)
	 * 数组长度(4字节int)
	 * 数据
	 * @return
	 * @throws IOException 
	 */
	protected byte[] readBytes() throws IOException
	{
		int handle = mDataIns.readInt() ;
		int len = mDataIns.readInt() ;
		byte[] vals = new byte[len] ;
		mDataIns.readFully(vals);
		mObjs.set(handle, vals) ;
		return vals ;
	}
	
	/**
	 * 句柄(4字节int)
	 * 数组长度(4字节int)
	 * 数据
	 * @return
	 * @throws IOException 
	 */
	protected int[] readInts() throws IOException
	{
		int handle = mDataIns.readInt() ;
		int len = mDataIns.readInt() ;
		int[] vals = new int[len] ;
		for(int i=0 ; i<len ; i++)
		{
			vals[i] = mDataIns.readInt() ;
		}
		mObjs.set(handle, vals) ;
		return vals ;
	}
	
	/**
	 * 句柄(4字节int)
	 * 数组长度(4字节int)
	 * 数据
	 * @return
	 * @throws IOException 
	 */
	protected float[] readFloats() throws IOException
	{
		int handle = mDataIns.readInt() ;
		int len = mDataIns.readInt() ;
		float[] vals = new float[len] ;
		for(int i=0 ; i<len ; i++)
		{
			vals[i] = mDataIns.readFloat() ;
		}
		mObjs.set(handle, vals) ;
		return vals ;
	}
	
	/**
	 * 句柄(4字节int)
	 * 名称(String)
	 * 长度(4字节int0
	 * 数据
	 * @return
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	protected Object readJavaSerializable() throws IOException
	{
		int handle = mDataIns.readInt() ;
		mDataIns.readUTF() ;
		int len = mDataIns.readInt() ;
		byte[] data = new byte[len] ;
		mDataIns.read(data) ;
		ObjectInputStream ins = new ObjectInputStream(mDataIns) ;
		Object obj = null ;
		try
		{
			obj = ins.readObject() ;
		}
		catch(ClassNotFoundException e)
		{
			throw new IllegalStateException(e) ;
		}
		mObjs.set(handle, obj) ;
		return obj ;
	}
	
	/**
	 * 句柄(4字节int)
	 * 类型名称(String)
	 * 数组长度
	 * 数组元素
	 * @return
	 * @throws IOException 
	 */
	protected List<Object> readList() throws IOException
	{
		int handle = mDataIns.readInt() ;
		String name = mDataIns.readUTF() ;
		List<Object> list = null ;
		if("ArrayList".equals(name))
		{
			list = new ArrayList<Object>() ;
		}
		else if("LinkedList".equals(name))
		{
			list = new LinkedList<Object>() ;
		}
		else
		{
//			Class<?> clazz = mClassMap.get(handle) ;
//			if(clazz != null)
//			{
//				try
//				{
//					list = (List) clazz.getConstructor().newInstance() ;
//				}
//				catch(Exception e)
//				{
//					new IllegalStateException(e) ;
//				}
//			}
//			else
				throw new IllegalStateException("不支持的数组类型"+name) ;
		}
		int size = mDataIns.readInt() ;
		mObjs.set(handle, list) ;
		for(int i=0 ; i<size ; i++)
		{
			list.add(readObject()) ;
		}
		return list ;
	}
	
	protected Set<Object> readSet() throws IOException
	{
		int handle = mDataIns.readInt() ;
		String name = mDataIns.readUTF() ;
		Set<Object> set = null ;
		if("HashSet".equals(name))
		{
			set = new HashSet<>() ; 
		}
		else if("TreeSet".equals(name))
		{
			set = new TreeSet<>() ;
		}
		else
		{
//			Class<?> clazz = mClassMap.get(name) ;
//			if(clazz != null)
//			{
//				try
//				{
//					set = (Set) clazz.getConstructor().newInstance() ;
//				}
//				catch(Exception e)
//				{
//					new IllegalStateException(e) ;
//				}
//			}
//			else
				throw new IllegalStateException("不支持的Set类型"+name) ;
		}
		int size = mDataIns.readInt() ;
		mObjs.set(handle, set) ;
		for(int i=0 ; i<size ; i++)
		{
			set.add(readObject()) ;
		}
		return set ;
	}
	
	/**
	 * 句柄(4字节int)
	 * 类型名称(String)
	 * 键值对数量(4字节int)
	 * 键值对数据
	 * @return
	 * @throws IOException 
	 */
	protected Map<String , Object> readMap() throws IOException
	{
		int handle = mDataIns.readInt() ;
		String name = mDataIns.readUTF() ;
		Map<String , Object> map = null ;
		if("HashMap".equals(name))
		{
			map = new HashMap<String, Object>() ;
		}
		else if("LinkedHashMap".equals(name))
		{
			map = new LinkedHashMap<String, Object>() ;
		}
		else
			throw new IllegalStateException("不支持的Map类型"+name) ;
		mObjs.set(handle, map) ;
		int size = mDataIns.readInt() ;
		for(int i=0 ; i<size ; i++)
		{
			String key = readKey() ;
//			System.out.println(key);
			map.put(key, readObject()) ;
		}
		return map ;
	}
	
	/**
	 * 句柄(4字节int)
	 * 类型名称(String)
	 * 数组长度(4字节int)
	 * 数组元素
	 * @return
	 * @throws IOException 
	 */
	protected Object[] readArray() throws IOException
	{
		int handle = mDataIns.readInt() ;
		int tag = mDataIns.read() ;
		String className = null ;
		
		if(tag == TC_ClassName)
		{
			className = mDataIns.readUTF() ;
			mDataIns.read() ;
		}
		else if(tag != TC_ClassId)
			throw new IllegalStateException(String.format("不是期望的标识:%d", tag)) ;
		short classId = mDataIns.readShort() ;
		Class<?> clazz = null ;
		if(className != null)
		{
			clazz = loadClass(className) ;
			Assert.notNull(clazz, "无法加载类:%s", className) ;
			mClassMap.put(classId, clazz) ;
		}
		else
		{
			clazz = mClassMap.get(classId) ;
			Assert.notNull(clazz, "无法取得 classId=%d 的类", classId) ;
		}
		int len = mDataIns.readInt() ;
		Object[] objs = (Object[])Array.newInstance(clazz, len) ;
		for(int i=0 ; i<len ; i++)
		{
			objs[i] = readObject() ;
		}
		mObjs.set(handle, objs) ;
		return objs ;
	}
	
	protected Class<?> loadClass(String aClassName)
	{
		if(mClassProvider != null)
		{
			Class<?> clazz = mClassProvider.apply(aClassName) ;
			if(clazz != null)
				return clazz ;
		}
		try
		{
			if(mClassLoader != null)
				return Class.forName(aClassName , true , mClassLoader) ;
			else
				return Class.forName(aClassName) ;
		}
		catch(Exception e)
		{}
		return null ;
	}
	
	protected int readUnsignedShort() throws IOException
	{
		return Bytes.toUnsigned(mDataIns.readShort()) ;
	}

	/**
	 * 标记(1字节)
	 * 句柄(4字节int)
	 * 名称(String)
	 * 长度(4字节int)
	 * key-value对
	 * @return
	 * @throws IOException 
	 */
	protected Object readEazialiable() throws IOException
	{
		int handle = mDataIns.readInt() ;
		int tag = mDataIns.read() ;
		Class<?> clazz = null ;
		if(tag == TC_ClassName)
		{
			String className = mDataIns.readUTF() ;
			tag = mDataIns.read() ;
			Assert.equals(tag, TC_ClassId);
			short classId = mDataIns.readShort() ;
			clazz = loadClass(className) ;
			Assert.notNull(clazz, "类[%s]加载失败!", className) ;
			mClassMap.put(classId, clazz) ;
		}
		else if(tag == TC_ClassId)
		{
			short classId = mDataIns.readShort() ;
			clazz = mClassMap.get(classId) ;
		}
		else
			throw new IllegalStateException(String.format("不能识别的标记%d", tag)) ;
			
		Object obj = null ;
		try
		{
			obj = clazz.getConstructor().newInstance() ;
		}
		catch(Exception e)
		{
			throw new IllegalStateException("无法用"+clazz.getClass().getName()+"类的无参构造函数构造") ;
		}
		int len = mDataIns.readInt() ;
		mObjs.set(handle, obj) ;
		long limit = mDataIns.getPosition()+len ;
		Map<String , Object> map = XC.linkedHashMap() ;
		while(mDataIns.getPosition()<limit)
		{
			String key = readKey() ;
			map.put(key , readObject()) ;
		}
		Runnable run = ((Eazialiable)obj).read(map) ;
		if(run != null)
			mRunnables.push(run) ;
		return obj ;
	}
	
	protected Object readReference() throws IOException
	{
		int handle = mDataIns.readInt() ;
		return mObjs.get(handle) ;
	}
	
	public String getVersion()
	{
		return mVersion ;
	}
	
	protected String readStringIntern() throws IOException
	{
		int handle = mDataIns.readInt() ;
		String str = mDataIns.readUTF() ;
		mObjs.set(handle, str) ;
		return str ;
	}
	
	protected String[] readStringInternArray() throws IOException
	{
		int handle = mDataIns.readInt() ;
		int len = mDataIns.readInt() ;
		String[] vals = new String[len] ;
		for(int i=0 ; i<len ; i++)
		{
			switch(mDataIns.read())
			{
			case TC_Reference:
				vals[i] = (String)mObjs.get(mDataIns.readInt()) ;
				break ;
			case TC_StringIntern:
				int handle_0 = mDataIns.readInt() ;
				vals[i] = mDataIns.readUTF() ;
				mObjs.set(handle_0, vals[i]) ;
				break ;
			default:
				throw new IllegalStateException("") ;
			}
		}
		mObjs.set(handle, vals) ;
		return vals ;
	}
}
