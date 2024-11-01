package team.sailboat.commons.fan.gadget;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.BiFunction;

import team.sailboat.commons.fan.collection.AutoCleanHashMap;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.file.FileUtils;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.struct.Tuples;

public class RSAKeyPairMaker
{
	static RSAKeyPairMaker sDefault ;
	
	public static RSAKeyPairMaker getDefault()
	{
		if(sDefault == null)
		{
			try
			{
				sDefault = new RSAKeyPairMaker() ;
			}
			catch (NoSuchAlgorithmException e)
			{
				WrapException.wrapThrow(e) ;
			}
		}
		return sDefault ;
	}
	
	/**
	 * 
	 */
	final AutoCleanHashMap<String, RSAPrivateKey> mRSAKeyCache = AutoCleanHashMap.withExpired_Created(1);
	
	final Map<String, BiFunction<PrivateKey , String, String>> mSpecialDecipherers = XC.hashMap() ;

	public RSAKeyPairMaker() throws NoSuchAlgorithmException
	{
	}

	public Entry<String, KeyPair> newOne() throws NoSuchAlgorithmException
	{
		KeyPair keyPair = RSAUtils.genKeyPair() ;
		String id = UUID.randomUUID().toString();
		mRSAKeyCache.put(id, (RSAPrivateKey) keyPair.getPrivate());
		return Tuples.of(id, keyPair);
	}
	
	public String decrypt(String aId, String aSecretText) throws Exception
	{
		RSAPrivateKey pk = mRSAKeyCache.get(aId);
		Assert.notNull(pk, "无效的id[%s]", aId);
		String extName = FileUtils.getExtName(aSecretText) ;
		if(extName != null)
		{
			BiFunction<PrivateKey , String , String> func = mSpecialDecipherers.get(extName) ;
			if(func != null)
				return func.apply(pk, aSecretText.substring(0, aSecretText.length()-extName.length()-1)) ;
		}
		return RSAUtils.decrypt(pk, aSecretText);
	}
	
	public RSAPrivateKey getPrivateKey(String aCode)
	{
		return mRSAKeyCache.get(aCode) ;
	}
	
	public void registerDecipherer(String aType , BiFunction<PrivateKey, String, String> aDecipherer)
	{
		mSpecialDecipherers.put(aType, aDecipherer) ;
	}
}
