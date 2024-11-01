package team.sailboat.commons.fan.gadget;

import java.math.BigInteger;
import java.security.InvalidKeyException;
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

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import team.sailboat.commons.fan.app.AppContext;

public class RSAUtils
{

	static final String sAlgorithm = "RSA";

	static KeyPairGenerator mGen = null;

	public static KeyPair genKeyPair() throws NoSuchAlgorithmException
	{
		if (mGen == null)
		{
			mGen = KeyPairGenerator.getInstance(sAlgorithm);
			mGen.initialize(1024);
		}
		return mGen.genKeyPair();
	}

	public static String toString(PublicKey aPubKey)
	{
		return Base64.getUrlEncoder().encodeToString(aPubKey.getEncoded());
	}

	public static String decrypt(PrivateKey aPriKey, String aText) throws InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException
	{
		Cipher cipher = Cipher.getInstance(sAlgorithm);
		cipher.init(Cipher.DECRYPT_MODE, aPriKey);
		byte[] encryptedBytes = cipher.doFinal(Base64.getDecoder().decode(aText));
		return new String(encryptedBytes, AppContext.sUTF8);
	}

	/**  
	 * 使用模和指数生成RSA公钥  
	 *   用于登录验证模块
	 *   
	 * @param modulus  
	 *            模,16进制表示 
	 * @param exponent  
	 *            指数,16进制表示
	 * @return  
	 */
	public static RSAPublicKey getPublicKey(String modulus, String exponent)
	{
		return getPublicKey(modulus, exponent, null);
	}

	/**
	 * 
	 * @param modulus			模,16进制表示 
	 * @param exponent			 指数,16进制表示
	 * @param aProvider
	 * @return
	 */
	public static RSAPublicKey getPublicKey(String modulus, String exponent, Provider aProvider)
	{
		try
		{
			BigInteger b1 = new BigInteger(modulus , 16);
			BigInteger b2 = new BigInteger(exponent , 16);
			KeyFactory keyFactory = aProvider == null ? KeyFactory.getInstance(sAlgorithm)
					: KeyFactory.getInstance(sAlgorithm, aProvider);
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
	 * 使用模和指数生成RSA私钥  
	  
	 * /None/NoPadding】  
	 *   
	 * @param modulus  
	 *            模,16进制表示
	 * @param exponent  
	 *            指数,16进制表示
	 * @return  
	 */
	public static RSAPrivateKey getPrivateKey(String modulus, String exponent)
	{
		return getPrivateKey(modulus, exponent, null) ;
	}
	
	/**
	 * 
	 * @param modulus			模,16进制表示
	 * @param exponent			指数,16进制表示
	 * @param aProvider
	 * @return
	 */
	public static RSAPrivateKey getPrivateKey(String modulus, String exponent , Provider aProvider)
	{
		try
		{
			BigInteger b1 = new BigInteger(modulus , 16);
			BigInteger b2 = new BigInteger(exponent , 16);
			KeyFactory keyFactory = aProvider ==null?KeyFactory.getInstance(sAlgorithm)
					: KeyFactory.getInstance(sAlgorithm  , aProvider) ;
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
	 * 用于通用对称加密-公钥
	 * @param publicKeyText
	 * @return
	 */
	public static PublicKey getPublicKey(String aPublicKeyText)
	{
		return getPublicKey(aPublicKeyText, Base64.getUrlDecoder());
	}

	public static PublicKey getPublicKey(String publicKeyText, Decoder aDecoder)
	{
		return getPublicKey(publicKeyText, aDecoder, null);
	}

	public static PublicKey getPublicKey(String publicKeyText, Decoder aDecoder, Provider aProvider)
	{
		try
		{
			byte[] publicKeyBytes = aDecoder.decode(publicKeyText);
			X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKeyBytes);
			KeyFactory keyFactory = aProvider == null ? KeyFactory.getInstance(sAlgorithm)
					: KeyFactory.getInstance(sAlgorithm, aProvider);
			return keyFactory.generatePublic(x509KeySpec);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("获取public key失败", e);
		}
	}

	/**
	 * 使用指定的RSA公钥加密
	 * @param publicKeyText
	 * @param plainText
	 * @return
	 * @throws Exception
	 */
	public static String encrypt(String aPublicKeyText, String aPlainText) throws Exception
	{
		PublicKey publicKey = getPublicKey(aPublicKeyText);
		return encrypt(publicKey, aPlainText);
	}

	/**
	 * 通用对称加密方法<br/>
	 * 公钥加密
	 * @param publicKey
	 * @param plainText
	 * @return
	 * @throws Exception 
	 */
	public static String encrypt(PublicKey publicKey, String plainText) throws Exception
	{
		Cipher cipher = Cipher.getInstance(sAlgorithm);
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(AppContext.sUTF8));
		String encryptedString = Base64.getEncoder().encodeToString(encryptedBytes);
		return encryptedString;
	}

	public static void main(String[] args) throws Exception
	{
		KeyPair keyPair = genKeyPair();
		String cipherText = encrypt(toString(keyPair.getPublic()), "hello world!");
		System.out.println(decrypt(keyPair.getPrivate(), cipherText));
	}
}
