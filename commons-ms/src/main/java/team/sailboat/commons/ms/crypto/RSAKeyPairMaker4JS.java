package team.sailboat.commons.ms.crypto;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.interfaces.RSAPrivateKey;
import java.util.Map.Entry;
import java.util.function.BiFunction;

import javax.crypto.Cipher;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.gadget.RSAKeyPairMaker;
import team.sailboat.commons.fan.lang.Assert;

public class RSAKeyPairMaker4JS extends RSAKeyPairMaker
{
	static final String sAlgorithm = "RSA" ;
	static final Provider sProvider = new BouncyCastleProvider() ;
	
	static RSAKeyPairMaker4JS sDefault ;
	
	public static RSAKeyPairMaker4JS getDefault()
	{
		if(sDefault == null)
		{
			try
			{
				RSAKeyPairMaker parent = RSAKeyPairMaker.getDefault() ;
				sDefault = new RSAKeyPairMaker4JS(parent) ;
				parent.registerDecipherer("js" , new JSDecipherer());
			}
			catch (NoSuchAlgorithmException e)
			{
				WrapException.wrapThrow(e) ;
			}
		}
		return sDefault ;
	}
	
	RSAKeyPairMaker mDelegate ;

	private RSAKeyPairMaker4JS(RSAKeyPairMaker aDelegate) throws NoSuchAlgorithmException
	{
		mDelegate = aDelegate ;
	}

	@Override
	public Entry<String, KeyPair> newOne() throws NoSuchAlgorithmException
	{
		return mDelegate.newOne() ;
	}

	public String decrypt4js(String aId, String aSecretText) throws Exception
	{
		RSAPrivateKey pk = mDelegate.getPrivateKey(aId);
		Assert.notNull(pk, "无效的id[%s]", aId);
		if(aSecretText.endsWith(".js"))
			aSecretText = aSecretText.substring(0 , aSecretText.length()-3) ;
		return decrypt4js(pk, aSecretText);
	}
	
	@Override
	public String decrypt(String aId, String aSecretText) throws Exception
	{
		return mDelegate.decrypt(aId, aSecretText) ;
	}
	
	static class JSDecipherer implements BiFunction<PrivateKey, String, String>
	{

		@Override
		public String apply(PrivateKey aT1, String aT2)
		{
			try
			{
				return decrypt4js((RSAPrivateKey)aT1, aT2);
			}
			catch (Exception e)
			{
				WrapException.wrapThrow(e) ;
				return null ;
			}
		}
		
	}

	public static String decrypt4js(RSAPrivateKey privateKey, String data)
			throws Exception
	{
		Cipher cipher = Cipher.getInstance(sAlgorithm , sProvider);
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		//模长    
		int key_len = privateKey.getModulus().bitLength() / 8;
		byte[] bytes = data.getBytes();
		byte[] bcd = ASCII_To_BCD(bytes, bytes.length);
		//System.err.println(bcd.length);    
		//如果密文长度大于模长则要分组解密    
		String ming = "";
		byte[][] arrays = splitArray(bcd, key_len);
		for (byte[] arr : arrays)
		{
			ming += new String(cipher.doFinal(arr));
		}
		return ming;
	}

	static byte[] ASCII_To_BCD(byte[] ascii, int asc_len)
	{
		byte[] bcd = new byte[asc_len / 2];
		int j = 0;
		for (int i = 0; i < (asc_len + 1) / 2; i++)
		{
			bcd[i] = asc_to_bcd(ascii[j++]);
			bcd[i] = (byte) (((j >= asc_len) ? 0x00 : asc_to_bcd(ascii[j++])) + (bcd[i] << 4));
		}
		return bcd;
	}

	static byte asc_to_bcd(byte asc)
	{
		byte bcd;

		if ((asc >= '0') && (asc <= '9'))
			bcd = (byte) (asc - '0');
		else if ((asc >= 'A') && (asc <= 'F'))
			bcd = (byte) (asc - 'A' + 10);
		else if ((asc >= 'a') && (asc <= 'f'))
			bcd = (byte) (asc - 'a' + 10);
		else
			bcd = (byte) (asc - 48);
		return bcd;
	}

	static byte[][] splitArray(byte[] data, int len)
	{
		int x = data.length / len;
		int y = data.length % len;
		int z = 0;
		if (y != 0)
		{
			z = 1;
		}
		byte[][] arrays = new byte[x + z][];
		byte[] arr;
		for (int i = 0; i < x + z; i++)
		{
			arr = new byte[len];
			if (i == x + z - 1 && y != 0)
			{
				System.arraycopy(data, i * len, arr, 0, y);
			}
			else
			{
				System.arraycopy(data, i * len, arr, 0, len);
			}
			arrays[i] = arr;
		}
		return arrays;
	}
	
//	public static String encrypt(PublicKey aPk , String aText) throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException
//	{
//		Cipher cipher = Cipher.getInstance(sAlgorithm , sProvider);
//		cipher.init(Cipher.ENCRYPT_MODE ,  aPk);
//		byte[] data = cipher.doFinal(aText.getBytes(AppContext.sUTF8)) ;
//		return Base64.getUrlEncoder().encodeToString(data) ;
//	}
	
//	public static String decrypt(PrivateKey aPk , String aText) throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException
//	{
//		Cipher cipher = Cipher.getInstance("RSA", sProvider);
//		cipher.init(Cipher.DECRYPT_MODE ,  aPk);
//		byte[] data = cipher.doFinal(Base64.getUrlDecoder().decode(aText)) ;
//		return new String(data , AppContext.sUTF8) ;
//	}
	
//	public static PublicKey getPublicKey(String aPublicKeyText)
//	{
//		return RSAUtils.getPublicKey(aPublicKeyText, Base64.getUrlDecoder() , sProvider) ;
//	}
	
//	/**  
//	 * 使用模和指数生成RSA公钥  
//	 *   用于登录验证模块
//	 *   
//	 * @param modulus  
//	 *            模  
//	 * @param exponent  
//	 *            指数  
//	 * @return  
//	 */
//	public static RSAPublicKey getPublicKey(String modulus, String exponent)
//	{
//		return RSAUtils.getPublicKey(modulus, exponent, sProvider) ;
//	}
//
//	/**  
//	 * 使用模和指数生成RSA私钥  
//	  
//	 * /None/NoPadding】  
//	 *   
//	 * @param modulus  
//	 *            模  
//	 * @param exponent  
//	 *            指数  
//	 * @return  
//	 */
//	public static RSAPrivateKey getPrivateKey(String modulus, String exponent)
//	{
//		return RSAUtils.getPrivateKey(modulus, exponent, sProvider) ;
//	}
}
