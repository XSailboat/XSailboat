package team.sailboat.commons.fan.gadget;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import javax.crypto.Cipher;

import team.sailboat.commons.fan.app.AppContext;

/**
 * 
 * RSA相关的工具类
 *
 * @author yyl
 * @since 2024年11月20日
 */
public class RSAUtils
{
	/**
	 * 缺省使用的Base64解码器
	 */
	public static final Decoder sDefaultDecoder = Base64.getDecoder() ;
	
	/**
	 * 取胜使用的Base64编码器
	 */
	public static final Encoder sDefaultEncoder = Base64.getEncoder() ;
	
	/**
	 * python下面，可以使用padding.PKCS1v15()
	 */
	public static final String sAlgorithm_PKCS1 = "RSA/ECB/PKCS1Padding"  ;
	
	/**
	 * 意义上是RSA/None/NoPadding	，但这个名字是不可用的
	 */
	public static final String sAlgorithm_default = "RSA" ;
	
	/**
	 * 缺省模式
	 */
 	public static final String sDefaultAlgorithm = sAlgorithm_default ;

	static KeyPairGenerator mGen = null;

	/**
	 * 生成一个密钥对（公钥和私钥）。
	 * 该方法使用预定义的算法（如RSA），并初始化密钥大小为1024位。
	 * 如果密钥生成器（mGen）尚未初始化，则先进行初始化。
	 *
	 * @return 生成的密钥对（包含公钥和私钥）
	 * @throws NoSuchAlgorithmException 如果找不到指定的加密算法，则抛出此异常
	 */
	public static KeyPair genKeyPair() throws NoSuchAlgorithmException
	{
		if (mGen == null)
		{
			mGen = KeyPairGenerator.getInstance(sDefaultAlgorithm);
			mGen.initialize(1024);
		}
		return mGen.genKeyPair();
	}

	/**
	 * 将公钥转换为字符串表示，使用默认的编码器。
	 *
	 * @param aPubKey 要转换的公钥
	 * @return 公钥的字符串表示
	 */
	public static String toString(PublicKey aPubKey)
	{
		return toString(aPubKey, sDefaultEncoder) ;
	}
	
	/**
	 * 将公钥转换为字符串表示，使用指定的编码器。
	 *
	 * @param aPubKey  要转换的公钥
	 * @param aEncoder 用于编码公钥的编码器
	 * @return 公钥的字符串表示
	 */
	public static String toString(PublicKey aPubKey , Encoder aEncoder)
	{
		return aEncoder.encodeToString(aPubKey.getEncoded()) ;
	}
	
	/**
	 * 使用私钥解密字符串数据，使用默认的解码器。
	 *
	 * @param aPriKey  用于解密的私钥
	 * @param aText    要解密的字符串数据
	 * @return 解密后的原始字符串数据
	 * @throws Exception 如果解密过程中发生异常，则抛出此异常
	 */
	public static String decrypt(PrivateKey aPriKey, String aText) throws Exception
	{
		return decrypt(aPriKey, aText, sDefaultDecoder) ;
	}

	/**
	 * 使用私钥解密字符串数据，使用指定的解码器。
	 *
	 * @param aPriKey  用于解密的私钥
	 * @param aText    要解密的字符串数据
	 * @param aDecoder 用于解码字符串数据的解码器
	 * @return 解密后的原始字符串数据
	 * @throws Exception 如果解密过程中发生异常，则抛出此异常
	 */
	public static String decrypt(PrivateKey aPriKey, String aText , Decoder aDecoder) throws Exception
	{
		Cipher cipher = Cipher.getInstance(sDefaultAlgorithm);
		cipher.init(Cipher.DECRYPT_MODE, aPriKey);
		byte[] encryptedBytes = cipher.doFinal(aDecoder.decode(aText));
		return new String(encryptedBytes , AppContext.sUTF8);
	}

	/**
	 * 根据模数和指数生成RSA公钥，不使用特定的加密服务提供者。
	 *
	 * @param modulus  公钥的模数字符串（十六进制）
	 * @param exponent 公钥的指数字符串（十六进制）
	 * @return 生成的RSA公钥
	 */
	public static RSAPublicKey getPublicKey(String modulus, String exponent)
	{
		return getPublicKey(modulus, exponent, null);
	}

	/**
	 * 根据模数和指数生成RSA公钥，可以使用指定的加密服务提供者。
	 *
	 * @param aModulus  公钥的模数字符串（十六进制）
	 * @param aExponent 公钥的指数字符串（十六进制）
	 * @param aProvider 用于生成公钥的加密服务提供者（可为null）
	 * @return 生成的RSA公钥
	 */
	public static RSAPublicKey getPublicKey(String aModulus, String aExponent, Provider aProvider)
	{
		try
		{
			BigInteger b1 = new BigInteger(aModulus , 16);
			BigInteger b2 = new BigInteger(aExponent , 16);
			KeyFactory keyFactory = aProvider == null ? KeyFactory.getInstance(sDefaultAlgorithm)
					: KeyFactory.getInstance(sDefaultAlgorithm, aProvider);
			RSAPublicKeySpec keySpec = new RSAPublicKeySpec(b1, b2);
			return (RSAPublicKey) keyFactory.generatePublic(keySpec);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 根据模数和指数生成RSA私钥，不使用特定的加密服务提供者。
	 *
	 * @param modulus  私钥的模数字符串（十六进制）
	 * @param exponent 私钥的指数字符串（十六进制）
	 * @return 生成的RSA私钥
	 */
	public static RSAPrivateKey getPrivateKey(String modulus, String exponent)
	{
		return getPrivateKey(modulus, exponent, null) ;
	}
	
	/**
	 * 根据模数和指数生成RSA私钥，可以使用指定的加密服务提供者。
	 *
	 * @param modulus  私钥的模数字符串（十六进制）
	 * @param exponent 私钥的指数字符串（十六进制）
	 * @param aProvider 用于生成私钥的加密服务提供者（可为null）
	 * @return 生成的RSA私钥
	 */
	public static RSAPrivateKey getPrivateKey(String modulus, String exponent , Provider aProvider)
	{
		try
		{
			BigInteger b1 = new BigInteger(modulus , 16);
			BigInteger b2 = new BigInteger(exponent , 16);
			KeyFactory keyFactory = aProvider ==null?KeyFactory.getInstance(sDefaultAlgorithm)
					: KeyFactory.getInstance(sDefaultAlgorithm  , aProvider) ;
			RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(b1, b2);
			return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 从字符串表示中获取公钥，使用默认的解码器。
	 *
	 * @param aPublicKeyText 公钥的字符串表示
	 * @return 解析后的公钥对象
	 */
	public static PublicKey getPublicKey(String aPublicKeyText)
	{
		return getPublicKey(aPublicKeyText, sDefaultDecoder);
	}

	/**
	 * 从字符串表示中获取公钥，使用指定的解码器。
	 *
	 * @param publicKeyText 公钥的字符串表示
	 * @param aDecoder      用于解码公钥的解码器
	 * @return 解析后的公钥对象
	 */
	public static PublicKey getPublicKey(String publicKeyText, Decoder aDecoder)
	{
		return getPublicKey(publicKeyText, aDecoder, null);
	}

	/**
	 * 从字符串表示中获取公钥，使用指定的解码器和加密服务提供者。
	 *
	 * @param publicKeyText 公钥的字符串表示
	 * @param aDecoder      用于解码公钥的解码器
	 * @param aProvider     用于生成公钥的加密服务提供者（可为null）
	 * @return 解析后的公钥对象
	 */
	public static PublicKey getPublicKey(String publicKeyText, Decoder aDecoder, Provider aProvider)
	{
		try
		{
			byte[] publicKeyBytes = aDecoder.decode(publicKeyText);
			X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKeyBytes);
			KeyFactory keyFactory = aProvider == null ? KeyFactory.getInstance(sDefaultAlgorithm)
					: KeyFactory.getInstance(sDefaultAlgorithm, aProvider);
			return keyFactory.generatePublic(x509KeySpec);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("获取public key失败", e);
		}
	}

	/**
	 * 使用公钥加密字符串数据，使用默认的编码器。
	 *
	 * @param aPublicKeyText 公钥的字符串表示
	 * @param aPlainText     要加密的字符串数据
	 * @return 加密后的字符串数据
	 * @throws Exception 如果加密过程中发生异常，则抛出此异常
	 */
	public static String encrypt(String aPublicKeyText, String aPlainText) throws Exception
	{
		PublicKey publicKey = getPublicKey(aPublicKeyText);
		return encrypt(publicKey, aPlainText , sDefaultEncoder);
	}
	
	/**
	 * 使用公钥加密字符串数据，使用指定的编码器。
	 *
	 * @param aPublicKeyText 公钥的字符串表示
	 * @param aPlainText     要加密的字符串数据
	 * @param aEncoder       用于编码加密数据的编码器
	 * @return 加密后的字符串数据
	 * @throws Exception 如果加密过程中发生异常，则抛出此异常
	 */
	public static String encrypt(String aPublicKeyText, String aPlainText , Encoder aEncoder) throws Exception
	{
		PublicKey publicKey = getPublicKey(aPublicKeyText);
		return encrypt(publicKey, aPlainText , aEncoder);
	}
	
	/**
	 * 使用公钥加密字符串数据，使用默认的编码器。
	 *
	 * @param publicKey  用于加密的公钥对象
	 * @param plainText  要加密的字符串数据
	 * @return 加密后的字符串数据
	 * @throws Exception 如果加密过程中发生异常，则抛出此异常
	 */
	public static String encrypt(PublicKey publicKey, String plainText) throws Exception
	{
 		return encrypt(publicKey, plainText, sDefaultEncoder) ;
 	}

	/**
	 * 使用公钥加密字符串数据，使用指定的编码器。
	 *
	 * @param publicKey  用于加密的公钥对象
	 * @param plainText  要加密的字符串数据
	 * @param aEncoder   用于编码加密数据的编码器
	 * @return 加密后的字符串数据
	 * @throws Exception 如果加密过程中发生异常，则抛出此异常
	 */
	public static String encrypt(PublicKey publicKey, String plainText , Encoder aEncoder) throws Exception
	{
		Cipher cipher = Cipher.getInstance(sDefaultAlgorithm);
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(AppContext.sUTF8));
		String encryptedString = aEncoder.encodeToString(encryptedBytes);
		return encryptedString;
	}
	
	/**
	 * 使用指定的算法和公钥加密字符串数据，使用默认的编码器。
	 *
	 * @param aAlgo      要使用的加密算法
	 * @param publicKey  用于加密的公钥对象
	 * @param plainText  要加密的字符串数据
	 * @return 加密后的字符串数据
	 * @throws Exception 如果加密过程中发生异常，则抛出此异常
	 */
	public static String encrypt(String aAlgo , PublicKey publicKey, String plainText) throws Exception
	{
		return encrypt(aAlgo, publicKey, plainText, sDefaultEncoder) ;
	}
	
	/**
	 * 使用指定的算法和公钥加密字符串数据，使用指定的编码器。
	 *
	 * @param aAlgo      要使用的加密算法
	 * @param publicKey  用于加密的公钥对象
	 * @param plainText  要加密的字符串数据
	 * @param aEncoder   用于编码加密数据的编码器
	 * @return 加密后的字符串数据
	 * @throws Exception 如果加密过程中发生异常，则抛出此异常
	 */
	public static String encrypt(String aAlgo , PublicKey publicKey, String plainText , Encoder aEncoder) throws Exception
	{
		Cipher cipher = Cipher.getInstance(aAlgo);
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(AppContext.sUTF8));
		String encryptedString = aEncoder.encodeToString(encryptedBytes);
		return encryptedString;
	}
}
