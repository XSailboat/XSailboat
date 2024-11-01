package team.sailboat.base.ds;

import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import team.sailboat.commons.fan.collection.PropertiesEx;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.gadget.RSAKeyPairMaker;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.XString;

public abstract class ConnInfo_Pswd extends ConnInfo
{
	private String mPassword ;
	
	/**
	 * 不参与持久化存储到数据库
	 */
	private String mEncryptedPassword ;
	
	Supplier<String> mPasswordSupplier ;
	
	protected ConnInfo_Pswd(String aType)
	{
		super(aType) ;
	}
	
	@JsonIgnore
	@Schema(description = "密码")
	public void setPassword(String aPassword)
	{
		mPasswordSupplier = null ;
		// 如果密码是没有加密的，这么调用也不会有影响
		mPassword = PropertiesEx.deSecret(aPassword) ;
	}
	public String getPassword()
	{
		if(mPassword == null && mPasswordSupplier != null)
			mPassword = mPasswordSupplier.get() ;
		return mPassword;
	}
	public void setPasswordSupplier(Supplier<String> aPasswordSupplier)
	{
		mPasswordSupplier = aPasswordSupplier ;
	}
	
	@Schema(description = "加密后的密码，只在向服务器提交信息时时使用")
	public String getEncryptedPassword()
	{
		return mEncryptedPassword ;
	}
	public void setEncryptedPassword(String aEncryptedPassword)
	{
		mEncryptedPassword = aEncryptedPassword;
	}
	
	public void decryptPassword(String aCodeId)
	{
		if(XString.isNotEmpty(mEncryptedPassword))
		{
			Assert.isNull(mPassword, "解密密码时，密码明文字段必需为null！") ;
			try
			{
				mPassword = RSAKeyPairMaker.getDefault().decrypt(aCodeId, mEncryptedPassword) ;
			}
			catch (Exception e)
			{
				WrapException.wrapThrow(e , mEncryptedPassword) ;
			}
			finally
			{
				mEncryptedPassword = null ;
			}
		}
	}
	
	
	public boolean equals(Object aObj)
	{
		if(!super.equals(aObj))
			return false ;
		ConnInfo_Pswd other = (ConnInfo_Pswd)aObj ;
		return JCommon.equals(other.mPassword, mPassword) ;
	}
	
	@Override
	protected boolean update(ConnInfo aConnInfo, boolean aPartially)
	{
		boolean changed =  super.update(aConnInfo, aPartially);
		ConnInfo_Pswd connInfo = (ConnInfo_Pswd)aConnInfo ;
		if(connInfo.mPassword != null && JCommon.unequals(mPassword, connInfo.mPassword))
		{
			mPassword = connInfo.mPassword ;
			changed = true ;
			closeResource() ;
		}
		return changed ;
	}
	
	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		return super.setTo(aJSONObj)
				.put("password", PropertiesEx.asSecret(mPassword))
//				.put("encryptedPassword" , mEncryptedPassword)		// 不持久化，不传到前台
				;
	}
}
