package team.sailboat.commons.fan.eazi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.management.RuntimeErrorException;

import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.serial.FlexibleDataOutputStream;

public abstract class EaziOutputAdapter implements SerialConstants , EntryOutput
{
	static final int MAX_KEY_LEN = 255 ;
	
	protected final KeyChecker mKeyChecker = new KeyChecker() ;
	protected final HandleTable_Out mHandleTable = new HandleTable_Out(500 , 3) ;
	
	protected FlexibleDataOutputStream mFDOStream ;
	/**
	 * 键为类全名，值为id，id采用顺序标号的方式
	 */
	protected Map<String , Short> mClassName_IdMap = new HashMap<>() ;
	/**
	 * 从1开始
	 */
	protected short mSeq = 1 ;
	
	public EaziOutputAdapter(FlexibleDataOutputStream aFDOStream)
	{
		mFDOStream = aFDOStream ;
	}
	
	protected final void writeKey(String aKey) throws IOException
	{
		checkKey(aKey) ;
		if(aKey.length()>MAX_KEY_LEN)
			throw new IllegalArgumentException("键太长") ;
		mFDOStream.writeUTF(aKey) ;
	}
	
	protected final void checkKey(String aKey)
	{
		if(aKey == null || aKey.isEmpty())
			throw new IllegalArgumentException() ;
		mKeyChecker.check(aKey) ;
	}
	
	protected final void writeByte(byte aValue) throws IOException
	{
		mFDOStream.write(TC_Byte) ;
		mFDOStream.write(aValue) ;
	}
	
	protected final void writeBytes(byte[] aArray) throws IOException
	{
		if(aArray == null)
		{
			mFDOStream.write(TC_NULL) ;
			return ;
		}
		int handle = mHandleTable.lookup(aArray) ;
		if(handle == -1)
		{
			handle = mHandleTable.assign(aArray) ;
			mFDOStream.write(TC_ByteArray) ;
			mFDOStream.writeInt(handle) ;
			mFDOStream.writeInt(aArray.length) ;
			mFDOStream.write(aArray);
		}
		else
		{
			mFDOStream.write(TC_Reference) ;
			mFDOStream.writeInt(handle) ;	
		}
	}
	
	protected final void writeInt(int aValue) throws IOException
	{
		mFDOStream.write(TC_Int) ;
		mFDOStream.writeInt(aValue) ;
	}
	
	protected final void writeInt(int aValue , int aAssertVal) throws IOException
	{
		mFDOStream.write(TC_Int) ;
		mFDOStream.writeInt(aValue) ;
	}
	
	protected final void writeInts(int[] aValues) throws IOException
	{
		if(aValues == null)
		{
			mFDOStream.write(TC_NULL) ;
			return ;
		}
		int handle = mHandleTable.lookup(aValues) ;
		if(handle == -1)
		{
			handle = mHandleTable.assign(aValues) ;
			mFDOStream.write(TC_IntArray) ;
			mFDOStream.writeInt(handle) ;
			mFDOStream.writeInt(aValues.length) ;
			for(int i : aValues)
				mFDOStream.writeInt(i) ;
		}
		else
		{
			mFDOStream.write(TC_Reference) ;
			mFDOStream.writeInt(handle) ;	
		}
	}
	
	protected final void writeLong(long aValue) throws IOException
	{
		mFDOStream.write(TC_Long) ;
		mFDOStream.writeLong(aValue) ;
	}
	
	protected final void writeLongs(long[] aValues) throws IOException
	{
		if(aValues == null)
		{
			mFDOStream.write(TC_NULL) ;
			return ;
		}
		int handle = mHandleTable.lookup(aValues) ;
		if(handle == -1)
		{
			handle = mHandleTable.assign(aValues) ;
			mFDOStream.write(TC_LongArray) ;
			mFDOStream.writeInt(handle) ;
			mFDOStream.writeInt(aValues.length) ;
			for(long val : aValues)
				mFDOStream.writeLong(val) ;
		}
		else
		{
			mFDOStream.write(TC_Reference) ;
			mFDOStream.writeInt(handle) ;	
		}
	}
	
	protected final void writeFloat(float aValue) throws IOException
	{
		mFDOStream.write(TC_Float) ;
		mFDOStream.writeFloat(aValue) ;
	}
	
	protected final void writeFloats(float[] aValues) throws IOException
	{
		if(aValues == null)
		{
			mFDOStream.write(TC_NULL) ;
			return ;
		}
		int handle = mHandleTable.lookup(aValues) ;
		if(handle == -1)
		{
			handle = mHandleTable.assign(aValues) ;
			mFDOStream.write(TC_FloatArray) ;
			mFDOStream.writeInt(handle) ;
			mFDOStream.writeInt(aValues.length) ;
			for(float val : aValues)
				mFDOStream.writeFloat(val) ;
		}
		else
		{
			mFDOStream.write(TC_Reference) ;
			mFDOStream.writeInt(handle) ;	
		}
	}
	
	protected final void writeDouble(double aValue) throws IOException
	{
		mFDOStream.write(TC_Double) ;
		mFDOStream.writeDouble(aValue) ;
	}
	
	protected final void writeDoubles(double[] aValues) throws IOException
	{
		if(aValues == null)
		{
			mFDOStream.write(TC_NULL) ;
			return ;
		}
		int handle = mHandleTable.lookup(aValues) ;
		if(handle == -1)
		{
			handle = mHandleTable.assign(aValues) ;
			mFDOStream.write(TC_DoubleArray) ;
			mFDOStream.writeInt(handle) ;
			mFDOStream.writeInt(aValues.length) ;
			for(double val : aValues)
				mFDOStream.writeDouble(val) ;
		}
		else
		{
			mFDOStream.write(TC_Reference) ;
			mFDOStream.writeInt(handle) ;	
		}
	}
	
	protected final void writeString(String aStr) throws IOException
	{
		mFDOStream.write(TC_String) ;
		mFDOStream.writeUTF(aStr) ;
	}
	
	protected final void writeStrings(String[] aValues) throws IOException
	{
		if(aValues == null)
		{
			mFDOStream.write(TC_NULL) ;
			return ;
		}
		int handle = mHandleTable.lookup(aValues) ;
		if(handle == -1)
		{
			handle = mHandleTable.assign(aValues) ;
			mFDOStream.write(TC_StringArray) ;
			mFDOStream.writeInt(handle) ;
			mFDOStream.writeInt(aValues.length) ;
			for(String val : aValues)
				mFDOStream.writeUTF(val) ;
		}
		else
		{
			mFDOStream.write(TC_Reference) ;
			mFDOStream.writeInt(handle) ;	
		}
	}
	
	protected final void writeStringsIntern(String[] aValues) throws IOException
	{
		if(aValues == null)
		{
			mFDOStream.write(TC_NULL) ;
			return ;
		}
		int handle = mHandleTable.lookup(aValues) ;
		if(handle == -1)
		{
			handle = mHandleTable.assign(aValues) ;
			mFDOStream.write(TC_StringInternArray) ;
			mFDOStream.writeInt(handle) ;
			mFDOStream.writeInt(aValues.length) ;
			for(String val : aValues)
			{
				writeStringIntern(val) ;
			}
		}
		else
		{
			mFDOStream.write(TC_Reference) ;
			mFDOStream.writeInt(handle) ;	
		}
	}
	
	protected final void writeBoolean(boolean aValue) throws IOException
	{
		mFDOStream.write(TC_Boolean) ;
		mFDOStream.writeBoolean(aValue) ;
	}
	
	protected final void writeBooleans(boolean[] aValues) throws IOException
	{
		if(aValues == null)
		{
			mFDOStream.write(TC_NULL) ;
			return ;
		}
		int handle = mHandleTable.lookup(aValues) ;
		if(handle == -1)
		{
			handle = mHandleTable.assign(aValues) ;
			mFDOStream.write(TC_BooleanArray) ;
			mFDOStream.writeInt(handle) ;
			mFDOStream.writeInt(aValues.length) ;
			for(boolean val : aValues)
				mFDOStream.writeBoolean(val) ;
		}
		else
		{
			mFDOStream.write(TC_Reference) ;
			mFDOStream.writeInt(handle) ;	
		}
	}
	
	protected final void writeShort(short aVal) throws IOException
	{
		mFDOStream.write(TC_Short) ;
		mFDOStream.writeShort(aVal) ;
	}
	
	protected final void writeShorts(short[] aValues) throws IOException
	{
		if(aValues == null)
		{
			mFDOStream.write(TC_NULL) ;
			return ;
		}
		int handle = mHandleTable.lookup(aValues) ;
		if(handle == -1)
		{
			handle = mHandleTable.assign(aValues) ;
			mFDOStream.write(TC_ShortArray) ;
			mFDOStream.writeInt(handle) ;
			mFDOStream.writeInt(aValues.length) ;
			for(short val : aValues)
				mFDOStream.writeShort(val) ;
		}
		else
		{
			mFDOStream.write(TC_Reference) ;
			mFDOStream.writeInt(handle) ;	
		}
	}
	
	/**
	 * 标记(TC_Array)
	 * 句柄(4字节整型)
	 * 类路径名(String)
	 * 数组长度(4字节int)
	 * 成员
	 * @param aObjs
	 * @throws IOException
	 */
	protected final void writeArray(Object[] aObjs) throws IOException
	{
		int handle = mHandleTable.lookup(aObjs) ;
		if(handle == -1)
		{
			handle = mHandleTable.assign(aObjs) ;
			mFDOStream.write(TC_Array) ;
			mFDOStream.writeInt(handle) ;
			String className = aObjs.getClass().getComponentType().getName() ;
			Short classId = mClassName_IdMap.get(className) ;
			if(classId == null)
			{
				classId = mSeq++ ;
				mClassName_IdMap.put(className, classId) ;
				mFDOStream.write(TC_ClassName);
				mFDOStream.writeUTF(className);
			}
			mFDOStream.write(TC_ClassId);
			mFDOStream.writeShort(classId);
			//输出数组长度
			mFDOStream.writeInt(aObjs.length) ;
			//输出每个数组成员
			for(Object obj : aObjs)
				writeObject(obj) ;
		}
		else
		{
			mFDOStream.write(TC_Reference) ;
			mFDOStream.writeInt(handle) ;
		}
	}
	
	protected final void writeChar(char aChar) throws IOException
	{
		mFDOStream.write(TC_Char) ;
		mFDOStream.writeChar(aChar) ;
	}
	
	protected final void writeChars(char[] aValues) throws IOException
	{
		if(aValues == null)
		{
			mFDOStream.write(TC_NULL) ;
			return ;
		}
		int handle = mHandleTable.lookup(aValues) ;
		if(handle == -1)
		{
			handle = mHandleTable.assign(aValues) ;
			mFDOStream.write(TC_CharArray) ;
			mFDOStream.writeInt(handle) ;
			mFDOStream.writeInt(aValues.length) ;
			for(char ch : aValues)
				mFDOStream.writeChar(ch) ;
		}
		else
		{
			mFDOStream.write(TC_Reference) ;
			mFDOStream.writeInt(handle) ;	
		}
	}
	
	/**
	 * <dd>
	 *   <dt>注意：</dt>
	 *   <dl>如果指定的参数是Map类型的，那么它的键要求必须是String类型的</dl>
	 * </dd>
	 * @param aObj
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	protected final void writeObject(Object aObj) throws IOException
	{
		if(aObj == null)
			mFDOStream.write(TC_NULL) ;
		else if(aObj.getClass().isPrimitive())
		{
			handlePrimitive(aObj) ;
 		}
		else if(XClassUtil.isPrimitiveBox(aObj.getClass()))
		{
			handlePrimitiveBox(aObj) ;
		}
		else if(aObj.getClass().isArray())
		{
			if(aObj.getClass().getComponentType().isPrimitive())
			{
				handlePrimitiveArray(aObj) ;
			}
			else
			{
				writeArray((Object[])aObj) ;
			}
		}
		else if(aObj instanceof String)
		{
			writeString((String)aObj) ;
		}
		else if(aObj instanceof Eazialiable)
		{
			writeEazialiable((Eazialiable)aObj) ;
		}
		else if(aObj instanceof List<?>)
		{
			writeList((List<Object>)aObj) ;
		}
		else if(aObj instanceof Map/* && aObj.getClass().getTypeParameters()[0].equals(String.class)*/)
		{
			writeMap((Map<String, Object>) aObj) ;
		}
		else if(aObj instanceof Set)
		{
			writeSet((Set<Object>)aObj);
		}
		else if(aObj instanceof Serializable)
		{
			writeSerializable((Serializable)aObj) ;
		}
		else
		{
			throw new UnsupportedOperationException(aObj.getClass().getName()+"类型不支持序列化") ;
		}
	}
	
	protected void writeList(List<Object> aValue) throws IOException
	{
		int handle = mHandleTable.lookup(aValue) ;
		if(handle == -1)
		{
			if(aValue == null)
				mFDOStream.write(TC_NULL) ;
			else
			{
				handle = mHandleTable.assign(aValue) ;
				mFDOStream.write(TC_List) ;
				mFDOStream.writeInt(handle) ;
				mFDOStream.writeUTF(aValue.getClass().getSimpleName()) ;
				//数组长度
				mFDOStream.writeInt(aValue.size()) ;
				if(aValue.size()>0)
				{
					//输出数组元素
					for(Object ele : aValue)
					{
						writeObject(ele) ;
					}
				}
			}
		}
		else
		{
			mFDOStream.write(TC_Reference) ;
			mFDOStream.writeInt(handle) ;
		}
	}
	
	protected void writeSet(Set<Object> aValue) throws IOException
	{
		int handle = mHandleTable.lookup(aValue) ;
		if(handle == -1)
		{
			if(aValue == null)
				mFDOStream.write(TC_NULL) ;
			else
			{
				handle = mHandleTable.assign(aValue) ;
				mFDOStream.write(TC_Set) ;
				mFDOStream.writeInt(handle) ;
				mFDOStream.writeUTF(aValue.getClass().getSimpleName()) ;
				//数组长度
				mFDOStream.writeInt(aValue.size()) ;
				if(aValue.size()>0)
				{
					//输出数组元素
					for(Object ele : aValue)
					{
						writeObject(ele) ;
					}
				}
			}
		}
		else
		{
			mFDOStream.write(TC_Reference) ;
			mFDOStream.writeInt(handle) ;
		}
	}
	
	public void writeMap(Map<String , Object> aValue) throws IOException
	{
		int handle = mHandleTable.lookup(aValue) ;
		if(handle == -1)
		{
			if(aValue == null)
				mFDOStream.write(TC_NULL) ;
			else
			{
				handle = mHandleTable.assign(aValue) ;
				mFDOStream.write(TC_Map) ;
				mFDOStream.writeInt(handle) ;
				mFDOStream.writeUTF(aValue.getClass().getSimpleName()) ;
				//键值对数量
				mFDOStream.writeInt(aValue.size()) ;
				if(aValue.size()>0)
				{
					//输出所有键值对
					for(Entry<String, Object> entry : aValue.entrySet())
					{
						write(entry.getKey(), entry.getValue()) ;
					}
				}
			}
		}
		else
		{
			mFDOStream.write(TC_Reference) ;
			mFDOStream.writeInt(handle) ;
		}
	}
	
	protected void writeEazialiable(Eazialiable aObj) throws IOException
	{
		if(aObj == null)
		{
			mFDOStream.write(TC_NULL) ;
			return ;
		}
		int handle = mHandleTable.lookup(aObj) ;
		if(handle == -1)
		{
			handle = mHandleTable.assign(aObj) ;
			mFDOStream.write(TC_Eazialiable) ;
			mFDOStream.writeInt(handle) ;
			String className = aObj.getClass().getName() ;
			Short id = mClassName_IdMap.get(className) ;
			if(id == null)
			{
				id = mSeq++ ;
				mClassName_IdMap.put(className, id) ;
				mFDOStream.write(TC_ClassName);
				mFDOStream.writeUTF(className);
			}
			mFDOStream.write(TC_ClassId);
			mFDOStream.writeShort(id);
			//长度
			long begin = mFDOStream.point() ;
			mFDOStream.writeInt(0) ;
			mKeyChecker.in() ;
			aObj.write(this) ;
			mKeyChecker.out() ;
//			System.out.println("<---------------------------");
			long end = mFDOStream.point() ;
//			System.out.println("当前位置："+end);
			mFDOStream.skipTo(begin) ;
//			System.out.println(String.format("跳转到%1$d，实际位置：%2$d" , begin , mFDOStream.point())) ;
			mFDOStream.writeInt((int)(end-begin-4)) ;
			mFDOStream.skipTo(end);
//			System.out.println(String.format("跳转到%1$d，实际位置：%2$d，当前大小：%3$s" , end , mFDOStream.point()
//					, mFDOStream.size())) ;
//			System.out.println(">---------------------------");
		}
		else
		{
			mFDOStream.write(TC_Reference) ;
			mFDOStream.writeInt(handle) ;
		}
	}
	
	protected final void writeSerializable(Serializable aObj) throws IOException
	{
		int handle = mHandleTable.lookup(aObj) ;
		if(handle == -1)
		{
			ByteArrayOutputStream bouts = new ByteArrayOutputStream() ;
			ObjectOutputStream objOuts = new ObjectOutputStream(bouts) ;
			objOuts.writeObject(aObj) ;
			byte[] data = bouts.toByteArray() ;
			mFDOStream.write(TC_JavaSerializable) ;
			mFDOStream.writeInt(handle) ;
			mFDOStream.writeUTF(aObj.getClass().getSimpleName()) ;
			//长度
			mFDOStream.writeInt(data.length) ;
			//内容
			mFDOStream.write(data) ;
		}
		else
		{
			mFDOStream.write(TC_Reference) ;
			mFDOStream.writeInt(handle) ;
		}
	}
	
	protected void handlePrimitive(Object aObj) throws IOException
	{
		Class<?> type = aObj.getClass() ;
		if(int.class == type)
		{
			writeInt((Integer)aObj) ;
		}
		else if(float.class == aObj.getClass())
		{
			writeFloat((Float)aObj) ;
		}
		else if(double.class == aObj.getClass())
		{
			writeDouble((Double)aObj) ;
		}
		else if(boolean.class == aObj.getClass())
		{
			writeBoolean((Boolean)aObj) ;
		}
		else if(byte.class == aObj.getClass())
		{
			writeByte((Byte)aObj) ;
		}
		else if(short.class == aObj.getClass())
		{
			writeShort((Short)aObj) ;
		}
		else if(char.class == aObj.getClass())
		{
			writeChar((Character)aObj) ;
		}
		else if(long.class == aObj.getClass())
		{
			writeLong((Long)aObj) ;
		}
		else throw new RuntimeErrorException(new Error("类型超出预期"+aObj.getClass().getName())) ;
	}
	
	protected void handlePrimitiveBox(Object aObj) throws IOException
	{
		if(Integer.class.equals(aObj.getClass()))
		{
			writeInt((Integer)aObj) ;
		}
		else if(Float.class.equals(aObj.getClass()))
		{
			writeFloat((Float)aObj) ;
		}
		else if(Double.class.equals(aObj.getClass()))
		{
			writeDouble((Double)aObj) ;
		}
		else if(Boolean.class.equals(aObj.getClass()))
		{
			writeBoolean((Boolean)aObj) ;
		}
		else if(Byte.class.equals(aObj.getClass()))
		{
			writeByte((Byte)aObj) ;
		}
		else if(Short.class.equals(aObj.getClass()))
		{
			writeShort((Short)aObj) ;
		}
		else if(Character.class.equals(aObj.getClass()))
		{
			writeChar((Character)aObj) ;
		}
		else if(Long.class.equals(aObj.getClass()))
		{
			writeLong((Long)aObj) ;
		}
		else throw new RuntimeErrorException(new Error("类型超出预期"+aObj.getClass().getName())) ;
	}
	
	protected final void handlePrimitiveArray(Object aObj) throws IOException
	{
		Class<?> type = aObj.getClass().getComponentType() ;
		if(int.class == type)
		{
			writeInts((int[])aObj) ;
		}
		else if(float.class == type)
		{
			writeFloats((float[])aObj) ;
		}
		else if(double.class == type)
		{
			writeDoubles((double[])aObj) ;
		}
		else if(boolean.class == type)
		{
			writeBooleans((boolean[])aObj) ;
		}
		else if(byte.class == type)
		{
			writeBytes((byte[])aObj) ;
		}
		else if(short.class == type)
		{
			writeShorts((short[])aObj) ;
		}
		else if(char.class == type)
		{
			writeChars((char[])aObj) ;
		}
		else if(long.class == type)
		{
			writeLongs((long[])aObj) ;
		}
		else throw new RuntimeErrorException(new Error("类型超出预期"+type.getName())) ;
	}
	
	@Override
	public EntryOutput write(String aKey , byte aValue) throws IOException
	{
		writeKey(aKey) ;
		writeByte(aValue) ;
		return this ;
	}
	
	@Override
	public EntryOutput write(String aKey, byte[] aArray) throws IOException
	{
		writeKey(aKey);
		writeBytes(aArray); 
		return this ;
	}

	@Override
	public EntryOutput write(String aKey , int aValue) throws IOException
	{
		writeKey(aKey) ;
		writeInt(aValue) ;
		return this ;
	}
	
	@Override
	public EntryOutput write(String aKey, int[] aArray) throws IOException
	{
		writeKey(aKey);
		writeInts(aArray); 
		return this ;
	}
	
	@Override
	public EntryOutput write(String aKey , float aValue) throws IOException
	{
		writeKey(aKey) ;
		writeFloat(aValue) ;
		return this ;
	}
	
	@Override
	public EntryOutput write(String aKey, float[] aArray) throws IOException
	{
		writeKey(aKey);
		writeFloats(aArray);
		return this ;
	}
	
	
	@Override
	public EntryOutput write(String aKey , double aValue) throws IOException
	{
		writeKey(aKey) ;
		writeDouble(aValue) ;
		return this ;
	}
	
	@Override
	public EntryOutput write(String aKey, double[] aArray) throws IOException
	{
		writeKey(aKey) ;
		writeDoubles(aArray) ;
		return this ;
	}
	
	@Override
	public EntryOutput write(String aKey , String aValue) throws IOException
	{
		writeKey(aKey) ;
		if(aValue == null)
			mFDOStream.write(TC_NULL) ;
		else
			writeString(aValue) ;
		return this ;
	}
	
	@Override
	public EntryOutput writeIntern(String aKey, String aValue) throws IOException
	{
		writeKey(aKey) ;
		writeStringIntern(aValue);
		return this ;
	}
	
	protected final void writeStringIntern(String aValue) throws IOException
	{
		if(aValue == null)
			mFDOStream.write(TC_NULL) ;
		else
		{
			int handle = mHandleTable.lookup(aValue) ;
			if(handle == -1)
			{
				mFDOStream.write(TC_StringIntern) ;
				handle = mHandleTable.assign(aValue) ;
				mFDOStream.writeInt(handle) ;
				mFDOStream.writeUTF(aValue) ;
			}
			else
			{
				mFDOStream.write(TC_Reference) ;
				mFDOStream.writeInt(handle) ;
			}
		}
	}
	
	@Override
	public EntryOutput writeIntern(String aKey, String[] aValues) throws IOException
	{
		writeKey(aKey) ;
		writeStringsIntern(aValues);
		return this ;
	}
	
	@Override
	public EntryOutput write(String aKey, String[] aArray) throws IOException
	{
		writeKey(aKey);
		if(aArray == null)
			mFDOStream.write(TC_NULL);
		else
			writeStrings(aArray);
		return this ;
	}
	
	@Override
	public EntryOutput write(String aKey , boolean aValue) throws IOException
	{
		writeKey(aKey) ;
		writeBoolean(aValue) ;
		return this ;
	}
	
	@Override
	public EntryOutput write(String aKey, boolean[] aArray) throws IOException
	{
		writeKey(aKey);
		writeBooleans(aArray) ;
		return this ;
	}
	
	@Override
	public EntryOutput write(String aKey, long aValue) throws IOException
	{
		writeKey(aKey) ;
		writeLong(aValue) ;
		return this ;
	}
	
	@Override
	public EntryOutput write(String aKey, long[] aArray) throws IOException
	{
		writeKey(aKey);
		writeLongs(aArray);
		return this ;
	}
	
	/**
	 * <dd>
	 *   <dt>注意：</dt>
	 *   <dl>如果指定的参数是Map类型的，那么它的键要求必须是String类型的</dl>
	 * </dd>
	 * @param aObj
	 * @throws IOException
	 */
	@Override
	public EntryOutput write(String aKey , Object aValue) throws IOException
	{
		writeKey(aKey) ;
		if(aValue == null)
		{
			mFDOStream.write(TC_NULL) ;
			return this ;
		}
		writeObject(aValue) ;
		return this ;
	}
	
	@Override
	public EntryOutput write(String aKey, Object[] aObjs) throws IOException
	{
		writeKey(aKey) ;
		if(aObjs == null)
		{
			mFDOStream.write(TC_NULL) ;
			return this ;
		}
		writeArray(aObjs) ;
		return this ;
	}

	@Override
	public EntryOutput write(String aKey, Eazialiable aObj) throws IOException
	{
		writeKey(aKey) ;
		writeEazialiable(aObj);
		return this ;
	}

	@Override
	public EntryOutput write(String aKey, Eazialiable[] aObjs) throws IOException
	{
		writeKey(aKey) ;
		if(aObjs == null)
		{
			mFDOStream.write(TC_NULL) ;
			return this ;
		}
		int handle = mHandleTable.lookup(aObjs) ;
		if(handle == -1)
		{
			handle = mHandleTable.assign(aObjs) ;
			mFDOStream.write(TC_Array) ;
			mFDOStream.writeInt(handle) ;
			String className = aObjs.getClass().getComponentType().getName() ;
			Short classId = mClassName_IdMap.get(className) ;
			if(classId == null)
			{
				classId = mSeq++ ;
				mClassName_IdMap.put(className, classId) ;
				mFDOStream.write(TC_ClassName);
				mFDOStream.writeUTF(className);
			}
			mFDOStream.write(TC_ClassId);
			mFDOStream.writeShort(classId);
			
			//输出数组长度
			mFDOStream.writeInt(aObjs.length) ;
			//输出每个数组成员
			for(Eazialiable obj : aObjs)
				writeEazialiable(obj) ;
		}
		else
		{
			mFDOStream.write(TC_Reference) ;
			mFDOStream.writeInt(handle) ;
		}
		return this ;
	}
}
