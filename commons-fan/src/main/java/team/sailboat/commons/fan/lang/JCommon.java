package team.sailboat.commons.fan.lang;

import java.lang.reflect.Array;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.http.Base64;
import team.sailboat.commons.fan.struct.XInt;
import team.sailboat.commons.fan.time.XTime;

public class JCommon
{
	
	public static final String[] sEmptyStringArray = new String[0] ;
	
	public static final float[] sEmptyFloatArray = new float[0] ;
	
	public static final int[] sEmptyIntArray = new int[0] ;
	
	public static final double[] sEmptyDoubleArray = new double[0] ;
	
	public static final byte[] sEmptyByteArray = new byte[0] ;
	
	public static final boolean[] sEmptyBooleanArray = new boolean[0] ;
	
	public static final char[] sEmptyCharArray = new char[0] ;
	
	public static final Object[] sEmptyObjectArray = new Object[0] ;
	
	public static final Object sEmptyObject = new Object();

	public static final Object sNullObject = new Object();

	static final String sSecretKey = "Cl9s1LY89jlsfW2q";

	static final Object sMutext = new Object();
	static YClassLoader sYClassLoader;
	static Boolean sIsEclipseApp;

	static Supplier<YClassLoader> sYClassLoaderSupplier;

	private static final String KEY_ALGORITHM = "AES";
	private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding"; //默认的加密算法
	static Cipher sDeCipher = null ;
	static Cipher sEnCipher = null ;
	static SecretKeySpec sKeySpec = null ;

	public static void setYClassLoaderSupplier(Supplier<YClassLoader> aSupplier)
	{
		Assert.isNull(sYClassLoader, "已经构建出了YClassLoader，不应该再设置YClassLoaderSupplier");
		sYClassLoaderSupplier = aSupplier;
	}

	public static Supplier<YClassLoader> getYClassLoaderSupplier()
	{
		if (sYClassLoaderSupplier == null)
		{
			sYClassLoaderSupplier = () -> {
				ClassLoader loader = ClassLoader.getSystemClassLoader();
				if (loader instanceof YClassLoader)
					return (YClassLoader) loader;
				else
					return new YClassLoader(loader);
			};
		}
		return sYClassLoaderSupplier;
	}

	/**
	 * 如果给定的测试值aTestVal为null，则返回默认值aDefaultVal，否则返回aTestVal。
	 * 这是一个泛型方法，适用于任何类型的T。
	 *
	 * @param <T> 泛型类型
	 * @param aTestVal 需要检查的测试值
	 * @param aDefaultVal 默认值
	 * @return aTestVal为null时返回aDefaultVal，否则返回aTestVal
	 */
	public static <T> T defaultIfNull(T aTestVal, T aDefaultVal)
	{
		return aTestVal == null ? aDefaultVal : aTestVal;
	}
	
	/**
	 * 如果给定的测试值aTestVal为null，则通过默认值供应商aDefaultValSupplier获取默认值，否则返回aTestVal。
	 * 这是一个泛型方法，适用于任何类型的T。
	 *
	 * @param <T> 泛型类型
	 * @param aTestVal 需要检查的测试值
	 * @param aDefaultValSupplier 默认值供应商
	 * @return aTestVal为null时返回通过aDefaultValSupplier获取的默认值，否则返回aTestVal
	 */
	public static <T> T defaultIfNull(T aTestVal, Supplier<T> aDefaultValSupplier)
	{
		return aTestVal == null ? aDefaultValSupplier.get(): aTestVal;
	}

	/**
	 * 如果给定的测试值aTestVal为null，则返回默认值aDefaultVal，否则返回aTestVal。
	 * 专门用于Integer类型。
	 *
	 * @param aTestVal 需要检查的测试值
	 * @param aDefaultVal 默认值
	 * @return aTestVal为null时返回aDefaultVal，否则返回aTestVal
	 */
	public static int defaultIfNull(Integer aTestVal, int aDefaultVal)
	{
		return aTestVal == null ? aDefaultVal : aTestVal;
	}
	
	/**
	 * 如果给定的测试值aTestVal为null，则返回默认值aDefaultVal，否则返回aTestVal.get()。
	 * 专门用于自定义的XInt类型，假设XInt是一个包含get()方法的包装类型。
	 *
	 * @param aTestVal 需要检查的测试值
	 * @param aDefaultVal 默认值
	 * @return aTestVal为null时返回aDefaultVal，否则返回aTestVal.get()
	 */
	public static int defaultIfNull(XInt aTestVal , int aDefaultVal)
	{
		return aTestVal == null?aDefaultVal:aTestVal.get() ;
	}
	
	/**
	 * 如果给定的测试值aTestVal为null，则返回默认值aDefaultVal，否则返回aTestVal。
	 * 专门用于Long类型。
	 *
	 * @param aTestVal 需要检查的测试值
	 * @param aDefaultVal 默认值
	 * @return aTestVal为null时返回aDefaultVal，否则返回aTestVal
	 */
	public static long defaultIfNull(Long aTestVal, long aDefaultVal)
	{
		return aTestVal == null ? aDefaultVal : aTestVal;
	}
	
	/**
	 * 如果给定的测试值aTestVal为null，则返回默认值aDefaultVal，否则返回aTestVal。
	 * 专门用于Boolean类型。
	 *
	 * @param aTestVal 需要检查的测试值
	 * @param aDefaultVal 默认值
	 * @return aTestVal为null时返回aDefaultVal，否则返回aTestVal
	 */
	public static boolean defaultIfNull(Boolean aTestVal, boolean aDefaultVal)
	{
		return aTestVal == null ? aDefaultVal : aTestVal;
	}
	
	/**
	 * 如果给定的测试值aTestVal为null或为空字符串（长度为0），则返回默认值aDefaultVal，否则返回aTestVal。
	 * 这是一个泛型方法，适用于任何实现了CharSequence接口的类型T。
	 *
	 * @param <T> 泛型类型，必须是CharSequence的子类型
	 * @param aTestVal 需要检查的测试值
	 * @param aDefaultVal 默认值
	 * @return aTestVal为null或为空字符串时返回aDefaultVal，否则返回aTestVal
	 */
	public static <T extends CharSequence> T defaultIfEmpty(T aTestVal , T aDefaultVal)
	{
		return aTestVal == null || aTestVal.length() == 0?aDefaultVal:aTestVal ;
	}
	
	/**
	 * 如果给定的测试值aTestVal为null或为空字符串（长度为0），则通过默认值供应商aDefaultVal获取默认值，否则返回aTestVal。
	 * 这是一个泛型方法，适用于任何实现了CharSequence接口的类型T。
	 *
	 * @param <T> 泛型类型，必须是CharSequence的子类型
	 * @param aTestVal 需要检查的测试值
	 * @param aDefaultVal 默认值供应商
	 * @return aTestVal为null或为空字符串时返回通过aDefaultVal获取的默认值，否则返回aTestVal
	 */
	public static <T extends CharSequence> T defaultIfEmpty_0(T aTestVal , Supplier<T> aDefaultVal)
	{
		return aTestVal == null || aTestVal.length() == 0?aDefaultVal.get():aTestVal ;
	}

	/**
	 * 如果aObj==null，将返回""
	 * @param aObj
	 * @return
	 */
	public static String toString(Object aObj)
	{
		return toString(aObj, "");
	}

	public static String[] toStringsIgnoreNull(Object... aArray)
	{
		if (XC.isEmpty(aArray))
			return sEmptyStringArray;
		int i = 0;
		String[] strs = new String[aArray.length];
		for (Object ele : aArray)
		{
			if (ele != null)
				strs[i++] = ele.toString();
		}
		return i == strs.length ? strs : Arrays.copyOf(strs, i);

	}

	public static String toString(Object aObj, String aDefaultVal)
	{
		return aObj == null ? aDefaultVal : aObj.toString();
	}

	public static void coutInfo(String aMsgFmt, Object... aArgs)
	{
		coutWithTip("消息", aMsgFmt, aArgs);
	}

	public static void coutDebug(String aMsgFmt, Object... aArgs)
	{
		coutWithTip("调试", aMsgFmt, aArgs);
	}

	public static void coutWarn(String aMsgFmt, Object... aArgs)
	{
		coutWithTip("警告", aMsgFmt, aArgs);
	}

	public static void coutError(String aMsgFmt, Object... aArgs)
	{
		coutWithTip("错误", aMsgFmt, aArgs);
	}

	static void coutWithTip(String aTip, String aMsgFmt, Object... aArgs)
	{
		cout("%1$s[%2$s]：%3$s",
				aTip,
				XTime.current$yyyyMMddHHmmssSSS(),
				XC.isEmpty(aArgs) ? aMsgFmt : String.format(aMsgFmt, aArgs));
	}

	public static void cout(Object aMsg)
	{
		if (aMsg != null)
			System.out.println(aMsg.toString());
	}

	/**
	 * 用System.out.println()输出
	 * @param aMsgFmt
	 * @param aArgs
	 */
	public static void cout(String aMsgFmt, Object... aArgs)
	{
		System.out.println(XC.isEmpty(aArgs) ? aMsgFmt : String.format(aMsgFmt, aArgs));
	}

	public static void cerr(String aMsgFmt, Object... aArgs)
	{
		System.err.println(XC.isEmpty(aArgs) ? aMsgFmt : String.format(aMsgFmt, aArgs));
	}

	/**
	 * 使当前线程等待指定毫秒数
	 * @param aMillis			单位毫秒
	 */
	public static final void sleep(int aMillis)
	{
		try
		{
			Thread.sleep(aMillis);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 是不是Eclipse应用程序
	 * @return
	 */
	public static boolean isEclipseApp()
	{
		if (sIsEclipseApp == null)
		{
			sIsEclipseApp = JCommon.class.getClassLoader().getClass().getName().contains("org.eclipse.osgi");
		}
		return sIsEclipseApp;
	}

	public static final void sleepInSeconds(int aSeconds)
	{
		sleep(aSeconds * 1000);
	}

	public static final void sleepInMinutes(int aMinutes)
	{
		sleep(aMinutes * 60 * 1000);
	}

	public static YClassLoader getYClassLoader()
	{
		if (sYClassLoader == null)
		{
			synchronized (sMutext)
			{
				if (sYClassLoader == null)
					sYClassLoader = getYClassLoaderSupplier().get();
			}
		}
		return sYClassLoader;
	}

	/**
	 * AES 加密操作
	 *
	 * @param content 待加密内容
	 * @param key 加密密钥
	 * @return 返回Base64转码后的加密数据
	 */
	public static String encrypt(String aContent)
	{
		return encrypt(aContent.getBytes(AppContext.sDefaultEncoding)) ;
	}
	
	public static byte[] encrypt_0(byte[] aData , int aOffset , int aLen)
	{
		try
		{
			return getDefaultEnCipher().doFinal(aData , aOffset , aLen) ;
		}
		catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException
				| NoSuchAlgorithmException | NoSuchPaddingException e)
		{
			WrapException.wrapThrow(e) ;
			return null ;		//dead code
		}
	}
	
	public static String encrypt(byte[] aData)
	{
		try
		{
			return Base64.encodeBase64StringUnChunked(getDefaultEnCipher().doFinal(aData));
		}
		catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException
				| NoSuchAlgorithmException | NoSuchPaddingException e)
		{
			WrapException.wrapThrow(e) ;
			return null ;		//dead code
		}
	}
	
	public static String decrypt(String aContent)
	{
		try
		{
			return new String(getDefaultDeCipher().doFinal(Base64.decodeBase64(aContent)) , AppContext.sDefaultEncoding);
		}
		catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException
				| NoSuchPaddingException e)
		{
			WrapException.wrapThrow(e) ;
			return null ;			//dead code
		}
	}
	
	public static byte[] decrypt(byte[] aData)
	{
		try
		{
			return getDefaultDeCipher().doFinal(aData) ;
		}
		catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException
				| NoSuchPaddingException e)
		{
			WrapException.wrapThrow(e) ;
			return null ;			//dead code
		}
	}
	
	static Cipher getDefaultDeCipher() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException
	{
		if(sDeCipher == null)
		{
			//实例化
			sDeCipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
			//使用密钥初始化，设置为解密模式
			sDeCipher.init(Cipher.DECRYPT_MODE, getSecretKey(sSecretKey));
		}
		return sDeCipher ;
	}
	
	static Cipher getDefaultEnCipher() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException
	{
		if(sEnCipher == null)
		{
			sEnCipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
			sEnCipher.init(Cipher.ENCRYPT_MODE , getSecretKey(sSecretKey));
		}
		return sEnCipher ;
	}

	/**
	 * 生成加密秘钥
	 *
	 * @return
	 * @throws NoSuchAlgorithmException 
	 */
	private static SecretKeySpec getSecretKey(final String key) throws NoSuchAlgorithmException
	{
		if(sKeySpec == null)
		{
			//返回生成指定算法密钥生成器的 KeyGenerator 对象
			KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM);
	
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG") ;
			random.setSeed(key.getBytes(AppContext.sDefaultEncoding)) ;
			//AES 要求密钥长度为 128
			kg.init(128, random);
			//生成一个密钥
			SecretKey secretKey = kg.generateKey();
	
			sKeySpec = new SecretKeySpec(secretKey.getEncoded(), KEY_ALGORITHM);// 转换为AES专用密钥
		}
		return sKeySpec ;
	}
	
	public static boolean equals(Object aObj0 , Object aObj1)
	{
		return aObj0!=null?aObj0.equals(aObj1):aObj1==null ;
	}
	
	/**
	 * 此方法认为空集合和null是相等的
	 * @param aC1
	 * @param aC2
	 * @return
	 */
	public static boolean equals(Collection<?> aC1 , Collection<?> aC2)
	{
		if(aC1 == aC2 || (aC1 == null && aC2.isEmpty()) || (aC2 == null && aC1.isEmpty()))
			return true ;
		return aC1!=null?aC1.equals(aC2):aC2==null ;
	}
	
	/**
	 * 此方法认为空集合和null是相等的
	 * @param aC1
	 * @param aC2
	 * @return
	 */
	public static boolean equalsC(Object aC1 , Object aC2)
	{
		if(aC1 == aC2)
			return true ;
		if(aC1 instanceof Collection)
		{
			if(aC2 == null)
				return ((Collection<?>) aC1).isEmpty() ;
		}
		else if(aC2 instanceof Collection)
		{
			if(aC1 == null)
				return ((Collection<?>) aC2).isEmpty() ;
		}
		return equals(aC1, aC2) ;
	}
	
	/**
	 * 判定null和长度为0的数组相等			<br>
	 * 非空数组的元素间依次比较，当且仅当数组长度相同，各元素逐位比较相同时，才返回true
	 * @param aArray0
	 * @param aArray1
	 * @return
	 */
	public static boolean equals(Object[] aArray0 , Object[] aArray1)
	{
		if(aArray0 != null && aArray1 != null && aArray0.length == aArray1.length)
		{
			for(int i=0 ; i<aArray0.length ; i++)
				if(!equals(aArray0[i] , aArray1[i])) return false ;
			return true ;
		}
		return aArray0 == aArray1 || (aArray0==null&&aArray1.length==0) 
				|| (aArray1==null&&aArray0.length == 0);
	}
	
	public static boolean unequals(Object[] aArray0 , Object[] aArray1)
	{
		return !equals(aArray0 , aArray1) ;
	}
	
	/**
	 * 
	 * @param aBytes0
	 * @param aBytes1
	 * @return
	 */
	public static boolean equals(byte[] aBytes0 , byte[] aBytes1)
	{
		if(aBytes0 != null && aBytes1 != null && aBytes0.length == aBytes1.length)
		{
			for(int i=0 ; i<aBytes0.length ; i++)
				if(aBytes0[i] != aBytes1[i])
					return false ;
			return true ;
		}
		return aBytes0 == aBytes1 || (aBytes0==null&&aBytes1.length==0) 
			|| (aBytes1==null&&aBytes0.length == 0) ;
	}
	
	/**
	 * 
	 * @param aArray0
	 * @param aArray1
	 * @return
	 */
	public static boolean equals(int[] aArray0 , int[] aArray1)
	{
		if(aArray0 != null && aArray1 != null && aArray0.length == aArray1.length)
		{
			for(int i=0 ; i<aArray0.length ; i++)
				if(aArray0[i] != aArray1[i]) return false ;
			return true ;
		}
		return aArray0 == aArray1 || (aArray0==null&&aArray1.length==0) 
			|| (aArray1==null&&aArray0.length == 0) ;
	}
	
	/**
	 * 
	 * @param aArray0
	 * @param aArray1
	 * @return
	 */
	public static boolean equals(float[] aArray0 , float[] aArray1)
	{
		if(aArray0 != null && aArray1 != null && aArray0.length == aArray1.length)
		{
			for(int i=0 ; i<aArray0.length ; i++)
				if(aArray0[i] != aArray1[i]) return false ;
			return true ;
		}
		return aArray0 == aArray1 || (aArray0==null&&aArray1.length==0) 
			|| (aArray1==null&&aArray0.length == 0) ;
	}
	
	/**
	 * null和空数组相等
	 * 
	 * @param aA1
	 * @param aA2
	 * @return
	 */
	public static boolean equalsA(Object aA1 , Object aA2)
	{
		if(aA1 == aA2)
			return true ;
		if(aA1 != null && aA1.getClass().isArray())
		{
			if(aA2 == null)
				return Array.getLength(aA1) == 0 ;
		}
		else if(aA2 != null && aA2.getClass().isArray())
		{
			if(aA1 == null)
				return Array.getLength(aA2) == 0 ;
		}
		return equals(aA1, aA2) ;
	}
	
	public static boolean unequals(Object aObj0 , Object aObj1)
	{
		return !equals(aObj0, aObj1) ;
	}
	
	public static boolean unequals(Collection<?> aObj0 , Collection<?> aObj1)
	{
		return !equals(aObj0, aObj1) ;
	}
	
	/**
	 * 此方法认为空集合和null是相等的
	 * 
	 * @param aObj0
	 * @param aObj1
	 * @return
	 */
	public static boolean unequalsC(Object aObj0 , Object aObj1)
	{
		return !equalsC(aObj0, aObj1) ;
	}
	/**
	 * 比较两个具有可比较性的对象。null higher			<br />
	 * 此方法接受两个泛型参数，这两个参数必须是实现了 {@link Comparable} 接口的类的实例。
	 * 方法会根据这两个对象的自然顺序进行比较，并且处理了参数为 null 的情况。
	 *
	 * @param <T> 泛型类型，必须继承自 {@link Comparable} 接口。
	 * @param aVal0 第一个待比较的对象，可以是 null。
	 * @param aVal1 第二个待比较的对象，可以是 null。
	 * @return 返回一个基本类型的整数值，表示比较的结果：
	 *         - 如果 aVal0 和 aVal1 都是 null，则返回 0；
	 *         - 如果 aVal0 是 null 但 aVal1 不是 null，则返回 1；
	 *         - 如果 aVal0 不是 null 但 aVal1 是 null，则返回 -1；
	 *         - 否则，返回 aVal0.compareTo(aVal1) 的结果。
	 */
	public static <T extends Comparable<T>> int compare(T aVal0 , T aVal1)
	{
		return aVal0==null?(aVal1==null?0:1):(aVal1==null?-1:aVal0.compareTo(aVal1)) ;
	}
	
	/**
	 * 比较两个泛型数组指定范围内的元素是否相等。
	 *
	 * @param aArray_1 第一个数组
	 * @param aFrom_1 第一个数组开始比较的起始索引
	 * @param aArray_2 第二个数组
	 * @param aFrom_2 第二个数组开始比较的起始索引
	 * @param aLen 要比较的元素个数
	 * @return 如果两个数组在指定范围内的元素都相等，则返回true；否则返回false
	 * 注意：此方法依赖于一个未显示的equals方法来判断元素是否相等，可能是一个工具方法或Java自带的Object.equals。
	 */
	public static <T> boolean equalsOfRange(T[] aArray_1 , int aFrom_1 , T[] aArray_2 , int aFrom_2 , int aLen)
	{
		if(aFrom_1+aLen<=aArray_1.length && aFrom_2+aLen<=aArray_2.length)
		{
			for(int i=0 ; i<aLen ; i++)
			{
				if(!equals(aArray_1[aFrom_1+i] , aArray_2[aFrom_2+i]))
					return false ;
			}
			return true ;
		}
		return false ;
	}
	
	/**
	 * 比较两个字符数组指定范围内的元素是否相等。
	 *
	 * @param aArray_1 第一个字符数组
	 * @param aFrom_1 第一个字符数组开始比较的起始索引
	 * @param aArray_2 第二个字符数组
	 * @param aFrom_2 第二个字符数组开始比较的起始索引
	 * @param aLen 要比较的元素个数
	 * @return 如果两个字符数组在指定范围内的元素都相等，则返回true；否则返回false
	 */
	public static boolean equalsOfRange(char[] aArray_1 , int aFrom_1 , char[] aArray_2 , int aFrom_2 , int aLen)
	{
		if(aFrom_1+aLen<=aArray_1.length && aFrom_2+aLen<=aArray_2.length)
		{
			for(int i=0 ; i<aLen ; i++)
			{
				if(aArray_1[aFrom_1+i] != aArray_2[aFrom_2+i])
					return false ;
			}
			return true ;
		}
		return false ;
	}
	
	/**
	 * 比较两个字节数组指定范围内的元素是否相等。
	 *
	 * @param aArray_1 第一个字节数组
	 * @param aFrom_1 第一个字节数组开始比较的起始索引
	 * @param aArray_2 第二个字节数组
	 * @param aFrom_2 第二个字节数组开始比较的起始索引
	 * @param aLen 要比较的元素个数
	 * @return 如果两个字节数组在指定范围内的元素都相等，则返回true；否则返回false
	 * 注意：此方法直接使用 '!=' 来比较字节值。
	 */
	public static boolean equalsOfRange(byte[] aArray_1 , int aFrom_1 , byte[] aArray_2 , int aFrom_2 , int aLen)
	{
		if(aFrom_1+aLen<=aArray_1.length && aFrom_2+aLen<=aArray_2.length)
		{
			for(int i=0 ; i<aLen ; i++)
			{
				if(!equals(aArray_1[aFrom_1+i] , aArray_2[aFrom_2+i]))
					return false ;
			}
			return true ;
		}
		return false ;
	}
}
