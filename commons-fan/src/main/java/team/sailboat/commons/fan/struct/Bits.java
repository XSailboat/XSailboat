package team.sailboat.commons.fan.struct;

import java.util.Arrays;

import team.sailboat.commons.fan.collection.XC;

/**
 * 
 *
 * @author yyl
 * @since 2024年9月29日
 */
public class Bits
{
	
	long[] mData ;
	
	public Bits()
	{	
	}
	
	public void ensureLength(int aLen)
	{
		if(aLen <= 0)
			return ;
		if(mData == null)
			mData = new long[(int)Math.ceil(aLen/64.0d)] ;
		else if(mData.length*64 < aLen)
		{
			mData = Arrays.copyOf(mData, (int)Math.ceil(aLen/64.0d)) ;
		}
	}
	
	public void set(int aPos , boolean aHold)
	{
		ensureLength(aPos) ;
		int i = aPos/64 ;
		int j = aPos%64 ;
		if(aHold)
		{
			mData[i] |= (1<<j) ; 
		}
		else
		{
			mData[i] &= ~(1<<j) ;
		}
	}
	
	public boolean get(int aPos)
	{
		if(mData == null || aPos >= mData.length*64)
			return false ;
		int i = aPos/64 ;
		int j = aPos%64 ;
		return (mData[i] & (1<<j)) > 0 ;
	}

//	/**
//	 * 1字节
//	 * @param b
//	 * @param off
//	 * @return
//	 */
//	public static boolean getBoolean(byte[] b, int off)
//	{
//		return b[off] != 0;
//	}
//
//	public static char getChar(byte[] b, int off)
//	{
//		return (char) (((b[off + 1] & 0xFF) << 0) + ((b[off + 0]) << 8));
//	}
//	
//	/**
//	 * 
//	 * @param b
//	 * @param off
//	 * @param aCharNum				<b>注意：</b>是字符的数量，不是字节数
//	 * @return
//	 */
//	public static char[] getChars(byte[] b , int off  , int aCharNum)
//	{
//		char[] chars = new char[aCharNum] ;
//		for(int i = 0 ; i<aCharNum ; i++)
//		{
//			chars[i] = getChar(b, off) ;
//			off += 2 ;
//		}
//		return chars ;
//	}
//


//
//	/**
//	 * 4字节
//	 * @param b
//	 * @param off
//	 * @return
//	 */
//	public static int getInt(byte[] b, int off)
//	{
//		return ((b[off + 3] & 0xFF) << 0) +
//				((b[off + 2] & 0xFF) << 8) +
//				((b[off + 1] & 0xFF) << 16) +
//				((b[off + 0]) << 24);
//	}
//	
//	public static int getInt(ByteBuffer aByteBuf)
//	{
//		return (aByteBuf.get() << 24)
//				+ ((aByteBuf.get() & 0xFF) << 16)
//				+ ((aByteBuf.get() & 0xFF) << 8) +
//				+ ((aByteBuf.get() & 0xFF) << 0) ;
//				
//				
//	}
//	
//	/**
//	 * 长度(2字节short)
//	 * 数据
//	 * @return
//	 */
//	public static String getUTF(byte[] b , int aOffset)
//	{
//		//		int len = Bits.toUnsigned(Bits.getShort(mBuf , mPoint)) ;
//		//		mPoint += 2 ;
//		//		String str = new String(mBuf , mPoint, len, sCharset) ;
//		//		mPoint += len ;
//		//		return str ;
//
//		int utflen = getUnsignedShort(b, aOffset);
//		char[] chararr = new char[utflen];
//
//		int point = aOffset+2 ;
//		int c, char2, char3;
//		int chararr_count = 0 , end = point+utflen ;
//
//		while (point < end)
//		{
//			c = (int) b[point] & 0xff;
//			if (c > 127)
//				break;
//			point++;
//			chararr[chararr_count++] = (char) c;
//		}
//
//		while (point < end)
//		{
//			c = (int) b[point] & 0xff;
//			switch (c >> 4)
//			{
//			case 0:
//			case 1:
//			case 2:
//			case 3:
//			case 4:
//			case 5:
//			case 6:
//			case 7:
//				/* 0xxxxxxx*/
//				point++;
//				chararr[chararr_count++] = (char) c;
//				break;
//			case 12:
//			case 13:
//				/* 110x xxxx   10xx xxxx*/
//				point += 2;
//				if (point > end)
//					throw new IllegalStateException(
//								"malformed input: partial character at end");
//				char2 = (int) b[point - 1];
//				if ((char2 & 0xC0) != 0x80)
//					throw new IllegalStateException(
//								"malformed input around byte " + point);
//				chararr[chararr_count++] = (char) (((c & 0x1F) << 6) |
//														(char2 & 0x3F));
//				break;
//			case 14:
//				/* 1110 xxxx  10xx xxxx  10xx xxxx */
//				point += 3;
//				if (point > end)
//					throw new IllegalStateException(
//								"malformed input: partial character at end");
//				char2 = (int) b[point - 2];
//				char3 = (int) b[point - 1];
//				if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
//					throw new IllegalStateException(
//								"malformed input around byte " + (point - 1));
//				chararr[chararr_count++] = (char) (((c & 0x0F) << 12) |
//														((char2 & 0x3F) << 6) |
//														((char3 & 0x3F) << 0));
//				break;
//			default:
//				/* 10xx xxxx,  1111 xxxx */
//				throw new IllegalStateException(
//							"malformed input around byte " + point);
//			}
//		}
//		// The number of chars produced may be less than utflen
//		return new String(chararr, 0, chararr_count);
//
//	}
//
//	/**
//	 * 4字节
//	 * @param b
//	 * @param off
//	 * @return
//	 */
//	public static float getFloat(byte[] b, int off)
//	{
//		int i = ((b[off + 3] & 0xFF) << 0) +
//				((b[off + 2] & 0xFF) << 8) +
//				((b[off + 1] & 0xFF) << 16) +
//				((b[off + 0]) << 24);
//		return Float.intBitsToFloat(i);
//	}
//
//	/**
//	 * 8字节
//	 * @param b
//	 * @param off
//	 * @return
//	 */
//	public static long getLong(byte[] b, int off)
//	{
//		return ((b[off + 7] & 0xFFL) << 0) +
//				((b[off + 6] & 0xFFL) << 8) +
//				((b[off + 5] & 0xFFL) << 16) +
//				((b[off + 4] & 0xFFL) << 24) +
//				((b[off + 3] & 0xFFL) << 32) +
//				((b[off + 2] & 0xFFL) << 40) +
//				((b[off + 1] & 0xFFL) << 48) +
//				(((long) b[off + 0]) << 56);
//	}
//
//	/**
//	 * 8字节
//	 * @param b
//	 * @param off
//	 * @return
//	 */
//	public static double getDouble(byte[] b, int off)
//	{
//		long j = ((b[off + 7] & 0xFFL) << 0) +
//				((b[off + 6] & 0xFFL) << 8) +
//				((b[off + 5] & 0xFFL) << 16) +
//				((b[off + 4] & 0xFFL) << 24) +
//				((b[off + 3] & 0xFFL) << 32) +
//				((b[off + 2] & 0xFFL) << 40) +
//				((b[off + 1] & 0xFFL) << 48) +
//				(((long) b[off + 0]) << 56);
//		return Double.longBitsToDouble(j);
//	}
//
//	/*
//	 * Methods for packing primitive values into byte arrays starting at given
//	 * offsets.
//	 */
//
//	public static void putBoolean(byte[] b, int off, boolean val)
//	{
//		b[off] = (byte) (val ? 1 : 0);
//	}
//	
//	public static byte get(boolean val)
//	{
//		return (byte)(val?1:0) ;
//	}
//
//	public static void putChar(byte[] b, int off, char val)
//	{
//		b[off + 1] = (byte) (val >>> 0);
//		b[off + 0] = (byte) (val >>> 8);
//	}
//	
//	/**
//	 * 2字节
//	 * @param aChar
//	 * @return
//	 */
//	public static byte[] get(char aChar)
//	{
//		return new byte[]{(byte) (aChar >>> 8)
//				, (byte) (aChar >>> 0)} ;
//	}
//	
//	public static byte[] get(char[] aChars)
//	{
//		if(XC.isEmpty(aChars))
//			return new byte[0] ;
//		byte[] buf = new byte[aChars.length*2] ;
//		int i = 0 ;
//		for(char ch : aChars)
//		{
//			putChar(buf, i , ch);
//			i+=2 ;
//		}
//		return buf ;
//	}
//
//	public static void putShort(byte[] b, int off, short val)
//	{
//		b[off + 1] = (byte) (val >>> 0);
//		b[off + 0] = (byte) (val >>> 8);
//	}
//	
//	public static void putUnsignedShort(byte[] b, int off, int val)
//	{
//		b[off + 1] = (byte) (val >>> 0);
//		b[off + 0] = (byte) (val >>> 8);
//	}
//
//	public static void putFloat(byte[] b, int off, float val)
//	{
//		int i = Float.floatToIntBits(val);
//		b[off + 3] = (byte) (i >>> 0);
//		b[off + 2] = (byte) (i >>> 8);
//		b[off + 1] = (byte) (i >>> 16);
//		b[off + 0] = (byte) (i >>> 24);
//	}

//	
//	public static long readLong(InputStream aIns) throws IOException
//	{
//		byte[] buf = new byte[8] ; 
//		int len = aIns.read(buf, 0, buf.length) ;
//		if(len != buf.length)
//			throw new EOFException(String.format("读取long类型数据，从流中读到%d字节，没有读到8字节" , len)) ;
//		return Bits.getLong(buf, 0) ;
//	}
//	

//	
//	public static float readFloat(InputStream aIns) throws IOException
//	{
//		int i = (aIns.read()<<24)|(aIns.read()<<16)
//				|(aIns.read()<<8)|aIns.read() ;
//		return Float.intBitsToFloat(i) ;
//	}
//	

//	
//	public static int readInt(InputStream aIns) throws IOException
//	{
//		return (aIns.read()<<24)|(aIns.read()<<16)|(aIns.read()<<8)
//				|aIns.read() ;
//	}
//	
//	public static byte[] readByteArray(InputStream aIns) throws IOException
//	{
//		int len = readInt(aIns) ;
//		if(len<0)
//			throw new StreamCorruptedException("错误的Byte[]长度："+len) ;
//		byte[] data = new byte[len] ;
//		aIns.read(data) ;
//		return data ;
//	}
//	
//	public static byte[] get(float val)
//	{
//		int i = Float.floatToIntBits(val);
//		return new byte[]{(byte) (i >>> 24) , (byte) (i >>> 16)
//				, (byte) (i >>> 8) , (byte) i} ;
//	}
//
//	public static void putLong(byte[] b, int off, long val)
//	{
//		b[off + 7] = (byte) (val >>> 0);
//		b[off + 6] = (byte) (val >>> 8);
//		b[off + 5] = (byte) (val >>> 16);
//		b[off + 4] = (byte) (val >>> 24);
//		b[off + 3] = (byte) (val >>> 32);
//		b[off + 2] = (byte) (val >>> 40);
//		b[off + 1] = (byte) (val >>> 48);
//		b[off + 0] = (byte) (val >>> 56);
//	}
//
//	public static void putDouble(byte[] b, int off, double val)
//	{
//		long j = Double.doubleToLongBits(val);
//		b[off + 7] = (byte) (j >>> 0);
//		b[off + 6] = (byte) (j >>> 8);
//		b[off + 5] = (byte) (j >>> 16);
//		b[off + 4] = (byte) (j >>> 24);
//		b[off + 3] = (byte) (j >>> 32);
//		b[off + 2] = (byte) (j >>> 40);
//		b[off + 1] = (byte) (j >>> 48);
//		b[off + 0] = (byte) (j >>> 56);
//	}
//	
//	public static byte[] get(double val)
//	{
//		long j = Double.doubleToLongBits(val);
//		return new byte[]{(byte) (j >>> 56) ,
//				(byte) (j >>> 48) ,
//				(byte) (j >>> 40) ,
//				(byte) (j >>> 32) ,
//				(byte) (j >>> 24) ,
//				(byte) (j >>> 16) ,
//				(byte) (j >>> 8) ,
//				(byte) (j >>> 0)} ;
//	}
//	

//	
//	public static short readShort(InputStream aIns) throws IOException
//	{
//		return (short) (((aIns.read() & 0xFF) << 8) + (aIns.read() & 0xFF)) ;
//	}
//	
//	public static int readUnsignedShort(InputStream aIns) throws IOException
//	{
//		return toUnsigned(readShort(aIns)) ;
//	}
//	
//	public static boolean readBoolean(InputStream aIns) throws IOException
//	{
//		return aIns.read() != 0 ;
//	}
//	
//	/**
//	 * 长度(2字节short)
//	 * 数据
//	 * @return
//	 * @throws IOException 
//	 */
//	public static String readUTF(InputStream aIns) throws IOException
//	{
//		//		int len = Bits.toUnsigned(Bits.getShort(mBuf , mPoint)) ;
//		//		mPoint += 2 ;
//		//		String str = new String(mBuf , mPoint, len, sCharset) ;
//		//		mPoint += len ;
//		//		return str ;
//
//		int utflen = readUnsignedShort(aIns);
//		byte[] buf = new byte[utflen] ;
//		aIns.read(buf) ;
//		char[] chararr = new char[utflen];
//
//		int c, char2, char3;
//		int point = 0 ;
//		int chararr_count = 0 , end = utflen ;
//
//		while (point < end)
//		{
//			c = (int) buf[point] & 0xff;
//			if (c > 127)
//				break;
//			point++;
//			chararr[chararr_count++] = (char) c;
//		}
//
//		while (point < end)
//		{
//			c = (int) buf[point] & 0xff;
//			switch (c >> 4)
//			{
//			case 0:
//			case 1:
//			case 2:
//			case 3:
//			case 4:
//			case 5:
//			case 6:
//			case 7:
//				/* 0xxxxxxx*/
//				point++;
//				chararr[chararr_count++] = (char) c;
//				break;
//			case 12:
//			case 13:
//				/* 110x xxxx   10xx xxxx*/
//				point += 2;
//				if (point > end)
//					throw new IllegalStateException(
//								"malformed input: partial character at end");
//				char2 = (int) buf[point - 1];
//				if ((char2 & 0xC0) != 0x80)
//					throw new IllegalStateException(
//								"malformed input around byte " + point);
//				chararr[chararr_count++] = (char) (((c & 0x1F) << 6) |
//														(char2 & 0x3F));
//				break;
//			case 14:
//				/* 1110 xxxx  10xx xxxx  10xx xxxx */
//				point += 3;
//				if (point > end)
//					throw new IllegalStateException(
//								"malformed input: partial character at end");
//				char2 = (int) buf[point - 2];
//				char3 = (int) buf[point - 1];
//				if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
//					throw new IllegalStateException(
//								"malformed input around byte " + (point - 1));
//				chararr[chararr_count++] = (char) (((c & 0x0F) << 12) |
//														((char2 & 0x3F) << 6) |
//														((char3 & 0x3F) << 0));
//				break;
//			default:
//				/* 10xx xxxx,  1111 xxxx */
//				throw new IllegalStateException(
//							"malformed input around byte " + point);
//			}
//		}
//		// The number of chars produced may be less than utflen
//		return new String(chararr, 0, chararr_count);
//	}
//	
//	public static String readUTF(ByteBuffer aBuf)
//	{
//		if(aBuf.remaining()<2)
//			return null ;
//		int p = aBuf.position() ;
//		final int utflen = getUnsignedShort(new byte[] {aBuf.get() , aBuf.get()}, 0);
//		if(aBuf.remaining()<utflen)
//		{
//			aBuf.position(p) ;
//			return null ;
//		}
//		
//		byte[] buf = null ;
//		int point = 0  ;
//		if(aBuf.hasArray())
//		{
//			buf = aBuf.array() ;
//			point = p+2 ; 
//			aBuf.position(point+utflen) ; 
//		}
//		else
//		{
//			buf = new byte[utflen] ;
//			aBuf.get(buf) ;
//		}
//		final int end = point+utflen ;
//		char[] chararr = new char[utflen];
//
//		int c, char2, char3;
//		int chararr_count = 0  ;
//
//		while (point < end)
//		{
//			c = (int) buf[point] & 0xff;
//			if (c > 127)
//				break;
//			point++;
//			chararr[chararr_count++] = (char) c;
//		}
//
//		while (point < end)
//		{
//			c = (int) buf[point] & 0xff;
//			switch (c >> 4)
//			{
//			case 0:
//			case 1:
//			case 2:
//			case 3:
//			case 4:
//			case 5:
//			case 6:
//			case 7:
//				/* 0xxxxxxx*/
//				point++;
//				chararr[chararr_count++] = (char) c;
//				break;
//			case 12:
//			case 13:
//				/* 110x xxxx   10xx xxxx*/
//				point += 2;
//				if (point > end)
//					throw new IllegalStateException(
//								"malformed input: partial character at end");
//				char2 = (int) buf[point - 1];
//				if ((char2 & 0xC0) != 0x80)
//					throw new IllegalStateException(
//								"malformed input around byte " + point);
//				chararr[chararr_count++] = (char) (((c & 0x1F) << 6) |
//														(char2 & 0x3F));
//				break;
//			case 14:
//				/* 1110 xxxx  10xx xxxx  10xx xxxx */
//				point += 3;
//				if (point > end)
//					throw new IllegalStateException(
//								"malformed input: partial character at end");
//				char2 = (int) buf[point - 2];
//				char3 = (int) buf[point - 1];
//				if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
//					throw new IllegalStateException(
//								"malformed input around byte " + (point - 1));
//				chararr[chararr_count++] = (char) (((c & 0x0F) << 12) |
//														((char2 & 0x3F) << 6) |
//														((char3 & 0x3F) << 0));
//				break;
//			default:
//				/* 10xx xxxx,  1111 xxxx */
//				throw new IllegalStateException(
//							"malformed input around byte " + point);
//			}
//		}
//		// The number of chars produced may be less than utflen
//		return new String(chararr, 0, chararr_count);
//	}
//	

//	/**
//	 * 
//	 * @param aBuf
//	 * @param aStr
//	 * @return				aBuf的容量不够将返回false，写入成功将返回true
//	 * @throws IOException
//	 */
//	public static boolean writeUTF(ByteBuffer aBuf , String aStr) throws IOException
//	{
//		int strlen = aStr.length();
//		int utflen = 0;
//		int c;
//
//		for (int i = 0; i < strlen; i++)
//		{
//			c = aStr.charAt(i);
//			if ((c >= 0x0001) && (c <= 0x007F))
//			{
//				utflen++;
//			}
//			else if (c > 0x07FF)
//			{
//				utflen += 3;
//			}
//			else
//			{
//				utflen += 2;
//			}
//		}
//
//		if (utflen > 65535)
//			throw new UTFDataFormatException(
//					"encoded string too long: " + utflen + " bytes");
//		if(aBuf.remaining()<utflen+2)
//			return false ;
//		
//		aBuf.put((byte) ((utflen >>> 8) & 0xFF)) ;
//		aBuf.put((byte) ((utflen >>> 0) & 0xFF)) ;
//
//		int i = 0;
//		for (i = 0; i < strlen; i++)
//		{
//			c = aStr.charAt(i);
//			if (!((c >= 0x0001) && (c <= 0x007F)))
//				break;
//			aBuf.put((byte) c) ;
//		}
//
//		for (; i < strlen; i++)
//		{
//			c = aStr.charAt(i);
//			if ((c >= 0x0001) && (c <= 0x007F))
//			{
//				aBuf.put((byte) c) ;
//
//			}
//			else if (c > 0x07FF)
//			{
//				aBuf.put((byte) (0xE0 | ((c >> 12) & 0x0F))) ;
//				aBuf.put((byte) (0x80 | ((c >> 6) & 0x3F))) ;
//				aBuf.put((byte) (0x80 | ((c >> 0) & 0x3F))) ;
//			}
//			else
//			{
//				aBuf.put((byte) (0xC0 | ((c >> 6) & 0x1F))) ;
//				aBuf.put((byte) (0x80 | ((c >> 0) & 0x3F))) ;
//			}
//		}
//		return true ;
//	}
//	
	/**
	 * 如果aVal为负数，直接返回false			<br>
	 * (aVal&aFragment) == aFragment
	 * @param aVal	
	 * @param aFragment
	 * @return
	 */
	public static boolean hit(int aVal , int aFragment)
	{
		if(aVal>=0)
			return (aVal&aFragment) == aFragment ;
		else
			return false ;
	}
	
	/**
	 * 如果aVal为负数，直接返回false			<br>
	 * (aVal&aFragment) == aFragment
	 * @param aVal
	 * @param aFragments
	 * @return
	 */
	public static boolean hitAny(int aVal , int...aFragments)
	{
		if(aVal<0)
			return false ;
		if(XC.isNotEmpty(aFragments))
		{
			for(int fragment : aFragments)
				if((aVal&fragment) == fragment)
					return true ;
		}
		return false ;
	}
	
	/**
	 * 
	 * @param aVal
	 * @param aFragments
	 * @return
	 */
	public static boolean hitAll(int aVal , int...aFragments)
	{
		if(aVal<0)
			return false ;
		if(XC.isNotEmpty(aFragments))
		{
			for(int fragment : aFragments)
				if((aVal&fragment) != fragment)
					return false ;
		}
		return true ;
	}
}
