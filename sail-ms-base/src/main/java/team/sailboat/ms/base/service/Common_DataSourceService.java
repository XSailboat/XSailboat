package team.sailboat.ms.base.service;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import team.sailboat.base.HttpClientProvider;
import team.sailboat.base.def.IApis_Core;
import team.sailboat.base.def.WorkEnv;
import team.sailboat.base.ds.ConnInfo;
import team.sailboat.base.ds.ConnInfo_Pswd;
import team.sailboat.base.ds.DSHelper_JDBC;
import team.sailboat.base.ds.DataSource;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.excep.ExceptionAssist;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.gadget.RSAKeyPairMaker;
import team.sailboat.commons.fan.gadget.RSAUtils;
import team.sailboat.commons.fan.http.Request;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.ms.MSApp;
import team.sailboat.commons.ms.jackson.JacksonUtils;

@Service
public class Common_DataSourceService
{
	final Logger mLogger = org.slf4j.LoggerFactory.getLogger(getClass()) ;
	
	@Qualifier("SailMSCore_Client")
	@Autowired
	HttpClientProvider mDiClientPvd ;
	
	final AtomicLong mRefreshTime = new AtomicLong(0L) ;
	
	long mVersion ;
	
	JSONArray mJaDataSource ;
	
	final Map<String, DataSource> mIdDSMap = XC.linkedHashMap() ;
	
	final ObjectMapper mDataSourceReader = new ObjectMapper() ;
	
	JavaType mJType ;
	
	public Common_DataSourceService()
	{
	}
	
	@PostConstruct
	void _init()
	{
		mJType = mDataSourceReader.getTypeFactory().constructParametricType(ArrayList.class, DataSource.class) ; 
		_refreshDataSources();
	}
	
	void _refreshDataSources()
	{
		long refreshTime = mRefreshTime.get() ;
		long now = System.currentTimeMillis() ;
		if(now-refreshTime>=2000 && mRefreshTime.compareAndSet(refreshTime, now))
		{
			try
			{
				JSONObject jo = (JSONObject) mDiClientPvd.get().ask(Request.GET().path(IApis_Core.sGET_dataSourceAllVersion).queryParam("version", mVersion)) ;
				mVersion = jo.optLong("version") ;
				JSONArray ja = jo.optJSONArray("data") ;
				if(ja != null)
				{
					mJaDataSource = ja ;
					ArrayList<DataSource> dsList = mDataSourceReader.readValue(mJaDataSource.toJSONString(), mJType) ;
					Set<String> oldDsIds = new HashSet<String>(mIdDSMap.keySet()) ;
					for(DataSource ds : dsList)
					{
						DataSource oldDs = mIdDSMap.get(ds.getId()) ;
						if(oldDs != null)
						{
							oldDs.updateFrom(ds) ;
							oldDsIds.remove(oldDs.getId()) ;
						}
						else
						{
							ds.setManaged(true) ;
							mIdDSMap.put(ds.getId(), ds) ;
						}
						
					}
					if(!oldDsIds.isEmpty())
					{
						for(String id : oldDsIds)
						{
							mIdDSMap.remove(id).setManaged(false) ;
						}
					}
				}
			}
			catch (Exception e)
			{
				mLogger.error(ExceptionAssist.getClearMessage(getClass(), e , "数据源信息读取失败！")) ;
			}
		}
	}
	
	/**
	 * 取得所有可用的数据源
	 * @return
	 * @throws Exception 
	 */
	public JSONArray getAllDataSources_JSON() throws Exception
	{
		_refreshDataSources();
		return mJaDataSource ;
	}
	
	public DataSource getDataSourceById(String aDataSourceId)
	{
		_refreshDataSources();
		return mIdDSMap.get(aDataSourceId) ;
	}
	
	public DataSource getDataSourceByName(String aName)
	{
		_refreshDataSources();
		DataSource[] dses = mIdDSMap.values().toArray(new DataSource[0]) ;
		for(int i=0 ; i<dses.length ; i++)
		{
			if(JCommon.equals(dses[i].getName() , aName))
				return dses[i] ;
		}
		return null ;
	}
	
	/**
	 * 返回的数据源的指定环境下的连接信息中，包含了密码
	 * @param aDataSourceId
	 * @param aWorkEnv
	 * @return
	 * @throws Exception
	 */
	public DataSource getDataSource_passwd(String aDataSourceId , WorkEnv aWorkEnv) throws Exception
	{
		DataSource ds = getDataSourceById(aDataSourceId) ;
		if(ds != null)
		{
			ConnInfo connInfo = ds.getConnInfo(aWorkEnv) ;
			if(connInfo instanceof ConnInfo_Pswd
					&& ((ConnInfo_Pswd) connInfo).getPassword() == null)
			{
				// 获取密钥对
				((ConnInfo_Pswd) connInfo).setPasswordSupplier(()->{
					String appName = MSApp.instance().getName() ;
					try
					{
			            Map.Entry<String, KeyPair> keyPair = RSAKeyPairMaker.getDefault().newOne();
			            String encryptedPswd = mDiClientPvd.get().askForString(Request.GET().path(IApis_Core.sGET_dataSourcePswd)
			                    .queryParam("id", aDataSourceId)
			                    .queryParam("clientAppId" , appName)
			                    .queryParam("env", aWorkEnv.name())
			                    .queryParam("publicKey", RSAUtils.toString(keyPair.getValue().getPublic()))
			                    .queryParam("usage", appName + "-"+getClass().getSimpleName()));
			            return XString.isNotEmpty(encryptedPswd)
			            				? RSAUtils.decrypt(keyPair.getValue().getPrivate(), encryptedPswd)
			            				: null ;
					}
					catch(Exception e)
					{
						WrapException.wrapThrow(e) ;
						return null ;		// dead code
					}
				});
			}
		}
		return ds ;
	}
	
	/**
	 * 创建数据源
	 * @param aDs
	 * @param aUserId
	 * @throws Exception 
	 */
	public void createDataSource(DataSource aDs , String aUserId) throws Exception
	{
		// 如果带有密码，需要加密传输到MSCore中
		ConnInfo connInfo = aDs.getDevConnInfo() ;
		String codeId = null ;
		RSAPublicKey pkey = null ;
		if(connInfo instanceof ConnInfo_Pswd
				&& XString.isNotEmpty(((ConnInfo_Pswd)connInfo).getPassword()))
		{
			// 需要加密
			JSONObject pkeyJo = mDiClientPvd.get().askJo(Request.POST().path(IApis_Core.sGET_RSAPublicKey)) ;
			pkey = RSAUtils.getPublicKey(pkeyJo.optString("publicKeyModulus") 
					, pkeyJo.optString("publicKeyExponent")) ;
			codeId = pkeyJo.optString("codeId") ;
			String encodedPassword = RSAUtils.encrypt(pkey, ((ConnInfo_Pswd)connInfo).getPassword()) ;
			((ConnInfo_Pswd)connInfo).setEncryptedPassword(encodedPassword) ;
			
		}
		connInfo = aDs.getProdConnInfo() ;
		if(connInfo instanceof ConnInfo_Pswd
				&& XString.isNotEmpty(((ConnInfo_Pswd)connInfo).getPassword()))
		{
			if(pkey == null)
			{
				JSONObject pkeyJo = mDiClientPvd.get().askJo(Request.POST().path(IApis_Core.sGET_RSAPublicKey)) ;
				pkey = RSAUtils.getPublicKey(pkeyJo.optString("publicKeyModulus") 
						, pkeyJo.optString("publicKeyExponent")) ;
				codeId = pkeyJo.optString("codeId") ;
			}
			String encodedPassword = RSAUtils.encrypt(pkey, ((ConnInfo_Pswd)connInfo).getPassword()) ;
			((ConnInfo_Pswd)connInfo).setEncryptedPassword(encodedPassword) ;
		}
		mDiClientPvd.get().ask(Request.POST().path(IApis_Core.sPOST_createDataSource)
				.queryParam("userId", aUserId)
				.queryParam("codeId" , codeId)
				.setJsonEntity(JacksonUtils.toString(aDs))) ;
	}
	
	public javax.sql.DataSource getDataSource_prod(String aDataSourceId)
	{
		try
		{
			DataSource ds = getDataSource_passwd(aDataSourceId , WorkEnv.prod) ;
			return DSHelper_JDBC.getDataSource(ds, WorkEnv.prod) ;
		}
		catch (Exception e)
		{
			WrapException.wrapThrow(e) ;
			return null ;				// dead code
		}
	}
}
