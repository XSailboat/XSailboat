package team.sailboat.commons.fan.struct;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UTFDataFormatException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.sys.MemoryAssist;

public class Bytes implements Cloneable
{
	public byte[] mData;
	public int mSize;

	long mMaxSizeLimit = MemoryAssist.sGB;

	Bytes(byte[] aData, int aSize)
	{
		mData = aData;
		mSize = aSize;
	}

	public Bytes()
	{
		mData = new byte[8];
		mSize = 0;
	}

	public Bytes(int aCapacity)
	{
		if (aCapacity <= 0)
			throw new IllegalArgumentException();
		mData = new byte[aCapacity];
		mSize = 0;
	}

	public void fill(byte aValue, int aFrom, int aTo)
	{
		if (mData.length < aTo)
			ensureCapacity(aTo);
		Arrays.fill(mData, aFrom, aTo, aValue);
		if (aTo > mSize)
			mSize = aTo;
	}

	public void clear()
	{
		mSize = 0;
	}

	public void add(byte aValue)
	{
		ensureCapacity(mSize + 1);
		mData[mSize++] = aValue;
	}

	public void add(byte... aValues)
	{
		if (aValues.length > 0)
		{
			mSize += aValues.length;
			ensureCapacity(mSize);
			System.arraycopy(aValues, 0, mData, mSize - aValues.length, aValues.length);
		}
	}

	public void add(byte[] aValues, int aOffset, int aLen)
	{
		if (aValues != null && aValues.length >= aOffset + aLen)
		{
			mSize += aLen;
			ensureCapacity(mSize);
			System.arraycopy(aValues, aOffset, mData, mSize - aLen, aLen);
		}
		else
			throw new IllegalArgumentException();
	}

	public void remove(int aIndex)
	{
		if (aIndex >= 0 && aIndex < mSize)
		{
			mSize--;
			if (aIndex < mSize - 1)
				System.arraycopy(mData, aIndex + 1, mData, aIndex, mSize - aIndex);
		}
	}
	
	public byte[] take(int aTo)
	{
		aTo = Math.min(aTo, mSize) ;
		byte[] result = new byte[aTo] ;
		System.arraycopy(mData, 0 , result , 0, aTo) ;
		System.arraycopy(mData, aTo , mData, 0 , mSize-aTo);
		return result ;
	}

	public void ensureCapacity(int aMinCapicity)
	{
		if (aMinCapicity > mMaxSizeLimit)
			throw new IllegalArgumentException("容量越界");
		if (mData.length < aMinCapicity)
			mData = Arrays.copyOf(mData, Math.max(mData.length * 3 / 2 + 1, aMinCapicity));
	}

	public void set(int aIndex, byte aVal)
	{
		Assert.betweenL_R(0, mSize, aIndex);
		mData[aIndex] = aVal;
	}

	public void set(int aStart, byte[] aArray)
	{
		if (aArray != null)
			set(aStart, aArray, 0, aArray.length);
	}

	public void set(int aStart, byte[] aArray, int aOff, int aLen)
	{
		if (XC.isNotEmpty(aArray))
		{
			Assert.betweenL_R(0, mSize, aStart);
			int end = aStart + aArray.length;
			ensureCapacity(end);
			System.arraycopy(aArray, aOff, mData, aStart, aLen);
			if (end > mSize)
				mSize = end;
		}
	}
	
	public ByteBuffer toByteBuffer()
	{
		return ByteBuffer.wrap(mData , 0, mSize) ;
	}

	/**
	 * byte数组的长度等于mSize，里面都是有效元素
	 * @return
	 */
	public byte[] toByteArray()
	{
		if (mSize == mData.length)
			return mData;
		return Arrays.copyOfRange(mData, 0, mSize);
	}

	@Override
	public Bytes clone()
	{
		Bytes clone = new Bytes();
		clone.mSize = mSize;
		clone.mMaxSizeLimit = mMaxSizeLimit;
		if (mSize > 0)
			clone.mData = Arrays.copyOf(mData, mSize);

		return clone;
	}
	
	/*********************************************
	 *
	 ************************************************/

	/**
	 * 
	 * @param aData
	 * @param aSize
	 * @return
	 */
	public static Bytes wrap(byte[] aData, int aSize)
	{
		return new Bytes(aData, aSize);
	}

	public static byte[] toBytes(int aVal)
	{
		return Integer.toString(aVal).getBytes(AppContext.sUTF8);
	}
	
	public static byte[] toBytes(String aVal)
	{
		return aVal == null?null:aVal.getBytes(AppContext.sUTF8) ;
	}

	public static Integer toInteger(byte[] aBytes)
	{
		return aBytes == null ? null : Integer.valueOf(toString(aBytes, 0, aBytes.length));
	}

	public static String toString(final byte[] b, int off, int len)
	{
		if (b == null)
			return null;
		
		if (len == 0)
			return "";
		
		return new String(b, off, len, AppContext.sUTF8);
	}
	
	
	public static byte[] get(short val)
	{
		return new byte[]{(byte) (val >>> 8) ,
				(byte) val} ;
	}
	
	public static byte[] get(int val)
	{
		return new byte[]{(byte) (val >>> 24) , (byte) (val >>> 16)
				, (byte) (val >>> 8) , (byte) val } ;
	}
	
	public static byte[] get(long val)
	{
		return new byte[]{(byte) (val >>> 56) , (byte) (val >>> 48)
				, (byte) (val >>> 40) , (byte) (val >>> 32)
				, (byte) (val >>> 24) , (byte) (val >>> 16)
				, (byte) (val >>> 8) , (byte) val } ;
	}
	
	public static int getUnsignedShort(byte[] b, int off)
	{
		return toUnsigned(getShort(b, off)) ;
	}
	
	/**
	 * 2字节
	 * @param b
	 * @param off
	 * @return
	 */
	public static short getShort(byte[] b, int off)
	{
		return (short) (((b[off] & 0xFF) << 8) + (b[off+1] & 0xFF));
	}
	
	public static int toUnsigned(byte aByte)
	{
		return aByte<0?256+aByte:aByte ;
	}
	
	public static int toUnsigned(short aVal)
	{
		return aVal<0?65536+aVal:aVal ;
	}
	
	public static void writeShort(OutputStream aOuts , int aVal) throws IOException
	{
		aOuts.write(aVal>>>8) ;
		aOuts.write(aVal) ;
	}
	
	public static void writeInt(OutputStream aOuts , int aVal) throws IOException
	{
		aOuts.write(aVal>>>24) ;
		aOuts.write(aVal>>>16) ;
		aOuts.write(aVal>>>8) ;
		aOuts.write(aVal) ;
	}
	
	public static void writeBoolean(OutputStream aOuts , boolean aBool) throws IOException
	{
		aOuts.write(aBool?1:0) ;
	}
	
	public static void writeChar(OutputStream aOuts , char aCh) throws IOException
	{
		aOuts.write(aCh>>>8) ;
		aOuts.write(aCh) ;
	}
	
	public static void writeChars(OutputStream aOuts , String aStr) throws IOException
	{
		int len = aStr.length();
		for (int i = 0; i < len; i++)
			writeChar(aOuts , aStr.charAt(i));
	}
	
	public static void writeFloat(OutputStream aOuts , float aVal) throws IOException
	{
		writeInt(aOuts, Float.floatToIntBits(aVal)) ;
	}
	
	public static void writeDouble(OutputStream aOuts , double aVal) throws IOException
	{
		long j = Double.doubleToLongBits(aVal);
		aOuts.write((byte)(j>>>56)) ;
		aOuts.write((byte)(j>>>48)) ;
		aOuts.write((byte)(j>>>40)) ;
		aOuts.write((byte)(j>>>32)) ;
		aOuts.write((byte)(j>>>24)) ;
		aOuts.write((byte)(j>>>16)) ;
		aOuts.write((byte)(j>>>8)) ;
		aOuts.write((byte)j) ;
	}
	
	public static void writeLong(OutputStream aOuts , long aVal) throws IOException
	{
		aOuts.write((byte)(aVal>>>56)) ;
		aOuts.write((byte)(aVal>>>48)) ;
		aOuts.write((byte)(aVal>>>40)) ;
		aOuts.write((byte)(aVal>>>32)) ;
		aOuts.write((byte)(aVal>>>24)) ;
		aOuts.write((byte)(aVal>>>16)) ;
		aOuts.write((byte)(aVal>>>8)) ;
		aOuts.write((byte)aVal) ;
	}
	
	/**
	 * 字符串用UTF-8编码长度不能超过65535
	 * @param aStr
	 * @throws IOException
	 */
	public static void writeUTF(OutputStream aOuts , String aStr) throws IOException
	{
		//			byte[] bytes0 = aStr.getBytes(sCharset) ;
		//			if(bytes0.length>65535)
		//				throw new IllegalArgumentException("String值太长，UTF-8编码超过65535") ;
		//			Bits.writeShort(mBOuts, (short)bytes0.length) ;
		//			mBOuts.write(bytes0) ;

		int strlen = aStr.length();
		int utflen = 0;
		int c;

		/* use charAt instead of copying String to char array */
		for (int i = 0; i < strlen; i++)
		{
			c = aStr.charAt(i);
			if ((c >= 0x0001) && (c <= 0x007F))
			{
				utflen++;
			}
			else if (c > 0x07FF)
			{
				utflen += 3;
			}
			else
			{
				utflen += 2;
			}
		}

		if (utflen > 65535)
			throw new UTFDataFormatException(
					"encoded string too long: " + utflen + " bytes");

		byte[] buf = new byte[utflen + 2];
		int k = 0;
		buf[k++] = (byte) ((utflen >>> 8) & 0xFF);
		buf[k++] = (byte) ((utflen >>> 0) & 0xFF);

		int i = 0;
		for (i = 0; i < strlen; i++)
		{
			c = aStr.charAt(i);
			if (!((c >= 0x0001) && (c <= 0x007F)))
				break;
			buf[k++] = (byte) c;
		}

		for (; i < strlen; i++)
		{
			c = aStr.charAt(i);
			if ((c >= 0x0001) && (c <= 0x007F))
			{
				buf[k++] = (byte) c;

			}
			else if (c > 0x07FF)
			{
				buf[k++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
				buf[k++] = (byte) (0x80 | ((c >> 6) & 0x3F));
				buf[k++] = (byte) (0x80 | ((c >> 0) & 0x3F));
			}
			else
			{
				buf[k++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
				buf[k++] = (byte) (0x80 | ((c >> 0) & 0x3F));
			}
		}
		aOuts.write(buf);
	}
	
}
