package team.sailboat.ms.ac.dbean;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dpa.SerDeFactory;
import team.sailboat.commons.fan.dpa.anno.BColumn;
import team.sailboat.commons.fan.dpa.anno.BDataType;
import team.sailboat.commons.fan.dpa.anno.BFeature;
import team.sailboat.commons.fan.dpa.anno.BTable;
import team.sailboat.commons.fan.dtool.DBType;
import team.sailboat.commons.fan.dtool.mysql.MySQLFeatures;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.ms.xca.IClientApp;
import team.sailboat.dplug.anno.DBean;
import team.sailboat.ms.ac.AppConsts;

@BTable(name="ac_client_app" , comment="app客户端"  , features = {
		@BFeature(type = DBType.MySQL , name = MySQLFeatures.TABLE__ENGINE , value = "InnoDB") , 
		@BFeature(type = DBType.MySQL , name = MySQLFeatures.TABLE__CHARACTER_SET , value = "utf8")} ,
	id_category = "ClientApp" , id_prefix = "ca"
)
@DBean(genBean = true , recordCreate = true , recordEdit = true)
public class ClientApp implements IClientApp
{
	public static final AuthorizationGrantType sAGT_AppOnly = new AuthorizationGrantType("app_only") ;
	/**
	 * 缺省的ClientApp排序器
	 */
	public static final Comparator<ClientApp> sDefaultComp = (c1 , c2)->{
		if(ClientApp.isWebApp(c1))
		{
			if(ClientApp.isWebApp(c2))
				return c1.getName().compareTo(c2.getName()) ;
			else
				return -1 ;
		}
		else
		{
			if(ClientApp.isWebApp(c2))
				return 1 ;
			else
				return c1.getName().compareTo(c2.getName()) ;
		}
	} ;
	static Map<String, AuthorizationGrantType> sGrantTypeMap = XC.hashMap() ;
	static
	{
		sGrantTypeMap.put(AuthorizationGrantType.AUTHORIZATION_CODE.getValue() , AuthorizationGrantType.AUTHORIZATION_CODE) ;
		sGrantTypeMap.put(AuthorizationGrantType.REFRESH_TOKEN.getValue() , AuthorizationGrantType.REFRESH_TOKEN) ;
		/**
		 * OAuth2.0废除密码授权认证的原因：		<br />
		 * The resource owner password credentials grant MUST NOT be used. 
		 * This grant type insecurely exposes the credentials of the resource owner to the client.
		 * Even if the client is benign, this results in an increased attack surface 
		 * (credentials can leak in more places than just the AS) and users are trained to enter their credentials in places other than the AS.
		 * 
		 * Furthermore, adapting the resource owner password credentials grant to two-factor authentication, 
		 * authentication with cryptographic credentials (cf. WebCrypto [webcrypto], WebAuthn [webauthn]),
		 * and authentication processes that require multiple steps can be hard or impossible.
		 */
//		sGrantTypeMap.put(AuthorizationGrantType.PASSWORD.getValue() , AuthorizationGrantType.PASSWORD) ;
		
		sGrantTypeMap.put(sAGT_AppOnly.getValue() , sAGT_AppOnly) ;
	}
	
	@BColumn(name="id" , dataType = @BDataType(name="string" , length = 32) , comment="唯一性标识" , seq = 0 , primary = true)
	String id ;
	
	@BColumn(name="name" , dataType = @BDataType(name="string" , length = 32) , comment="App名称" , seq = 1)
	String name ;

	@BColumn(name="description" , dataType = @BDataType(name="string" , length = 256) , comment="描述" , seq = 2)
	String description ;
	
	@BColumn(name="simple_name" , dataType = @BDataType(name="string" , length = 8) , comment="应用的简化名，用2个或3个英文/中文字符来表示" , seq = 3)
	String simpleName ; 
	
	@BColumn(name="company" , dataType = @BDataType(name="string" , length = 256) , comment="厂家" , seq = 4)
	String company ;
	
	@BColumn(name="app_key" , dataType = @BDataType(name="string" , length = 32) , comment="访问凭据-appkey" , seq = 5)
	String appKey ;
	
	@BColumn(name="app_secret" , dataType = @BDataType(name="string" , length = 128) , comment="访问凭据-密钥" , seq = 6
			, serDeClass = SerDeFactory.StringSecret.class)
	String appSecret ;
	
	@BColumn(name="enabled" , dataType = @BDataType(name="bool") , comment="是否可用" , seq = 7
			, defaultValue = "true")
	Boolean enabled = true ;
	
	@BColumn(name="home_page_url" , dataType = @BDataType(name="string" , length = 256) , comment="主页地址" , seq = 8)
	String homePageUrl ;
	
	@BColumn(name="code_callback_urls" , dataType = @BDataType(name="string" , length = 2048) , comment="授权码回调地址" , seq = 9)
	String[] codeCallbackUrls ;
	
//	@BColumn(name="logout_notifier_url" , dataType = @BDataType(name="string" , length = 1024) , comment="登出通知url" , seq = 10)
//	String mLogoutNotifierUrl ;
	
	@BColumn(name="refresh_user_auths_notifier_url"  , dataType = @BDataType(name="string" , length = 256) , comment="刷新某用户权限的通知url" , seq = 11)
	String refreshUserAuthsNotifierUrl ;
	
	@BColumn(name="scopes" , dataType = @BDataType(name="string" , length = 256) , comment="可以获取的用户信息范围" , seq = 12)
	String[] scopes ;
	
	@BColumn(name="auth_methods" , dataType = @BDataType(name="string" , length = 256) , comment="认证授权方式" , seq = 13)
	String[] authMethods ;
	
	@BColumn(name="ext_attributes" , dataType = @BDataType(name="string" , length = 2048) , comment="附加信息" , seq = 14)
	String extAttributes ;
	
	@BColumn(name="res_space_types" , dataType = @BDataType(name="string" , length=1024) , comment="App的所有子空间类型" , seq = 19 )
	String[] resSpaceTypes ;
	
	@BColumn(name="grant_types" , dataType = @BDataType(name="string" , length=256) , comment="授权模式，可取值：authorization_code,refresh_token,app_only" , seq = 20 )
	String[] grantTypes ;
	
	@BColumn(name="contacter" , dataType = @BDataType(name="string" , length = 32) , comment="联系人" , seq = 21)
	String contacter ;
	
	@BColumn(name="contact_info" , dataType = @BDataType(name="string" , length = 128) , comment="联系方式，联系人信息" , seq = 22)
	String contactInfo ;
	
	Set<String> mCodeCallbackUrlSet ;
	
	RequestMappingHandlerMapping mInvokableApiMapping ;
	
	RegisteredClient mSecurityClient ;
	
	Field mClientSettingField ;
	
	JSONObject mJObj_ExtAttributes ;
	
	public ClientApp()
	{
	}
	
	public boolean setName(String aName)
	{
		if(JCommon.unequals(name, aName))
		{
			Object oldValue = name ;
			name = aName;
			setChanged("name", name, oldValue);
			mSecurityClient = null ;
			return true ;
		}
		return false ;
	}
	
	public Set<String> getCodeCallbackUrlSet()
	{
		if(mCodeCallbackUrlSet == null)
		{
			mCodeCallbackUrlSet = XC.linkedHashSet(codeCallbackUrls) ;
		}
		return mCodeCallbackUrlSet ;
	}
	public boolean addCodeCallbackUrls(String... aUrls)
	{
		if(XC.isEmpty(aUrls))
			return false ;
		boolean changed = false ;
		for(String url : aUrls)
		{
			if(XString.isEmpty(url))
				continue ;
			changed |= mCodeCallbackUrlSet.add(url) ;
		}
		if(changed)
		{
			Object oldValue = codeCallbackUrls ;
			codeCallbackUrls = mCodeCallbackUrlSet.toArray(JCommon.sEmptyStringArray) ;
			setChanged("codeCallbackUrls", codeCallbackUrls, oldValue) ;
			return true ;
		}
		return false ;
	}
	public boolean setCodeCallbackUrls(String[] aUrls)
	{
		if(!JCommon.equals(codeCallbackUrls, aUrls))
		{
			Object oldValue = codeCallbackUrls ;
			codeCallbackUrls = aUrls ;
			if(mCodeCallbackUrlSet == null)
				mCodeCallbackUrlSet = XC.linkedHashSet() ;
			else
				mCodeCallbackUrlSet.clear() ;
			XC.addAll(mCodeCallbackUrlSet, codeCallbackUrls) ;
			setChanged("codeCallbackUrls", codeCallbackUrls, oldValue) ;
			mSecurityClient = null ;
			return true ;
		}
		return false ;
	}
	public boolean removeCodeCallbackUrls(String[] aUrls)
	{
		if(XC.isEmpty(aUrls))
			return false ;
		boolean changed = false ;
		for(String url : aUrls)
		{
			changed |= mCodeCallbackUrlSet.remove(url) ;
		}
		if(changed)
		{
			Object oldValue = codeCallbackUrls ;
			codeCallbackUrls = mCodeCallbackUrlSet.toArray(JCommon.sEmptyStringArray) ;
			setChanged("codeCallbackUrls", codeCallbackUrls, oldValue) ;
			return true ;
		}
		return false ;
	}

	public boolean setScopes(String[] aScopes)
	{
		if(!JCommon.equals(aScopes, scopes))
		{
			Object oldValue = scopes ;
			scopes = aScopes;
			setChanged("scopes" , scopes , oldValue) ;
			mSecurityClient = null ;
			return true ;
		}
		return false ;
	}
	
	/**
	 * 
	 * @return		返回结果不为null
	 */
	public JSONObject getExtAttributesInJSONObject()
	{
		if(mJObj_ExtAttributes == null)
		{
			mJObj_ExtAttributes = JSONObject.of(extAttributes) ;
		}
		return mJObj_ExtAttributes ;
	}
	
	public boolean isEnabled()
	{
		return JCommon.defaultIfNull(enabled , true) ;
	}
	
	public RequestMappingHandlerMapping getInvokableApiMapping()
	{
		return mInvokableApiMapping;
	}
	public void setInvokableApiMapping(RequestMappingHandlerMapping aInvokableApiMapping)
	{
		mInvokableApiMapping = aInvokableApiMapping;
	}
	
	public RegisteredClient getSecurityClient(PasswordEncoder aPasswordEncoder
			, int aTokenTimeToLiveInSeconds)
	{
		if (mSecurityClient == null)
		{
			RegisteredClient.Builder builder = RegisteredClient.withId(getId())
					.clientId(getAppKey())
					.clientName(getName())
					.clientSecret(aPasswordEncoder.encode(getAppSecret()))
					.clientAuthenticationMethod(AppConsts.sXAppSign)
					// 支持以authorization basic方式认证
					.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
					.authorizationGrantTypes((types)->{
						String[] grantTypes = getGrantTypes() ;
						if(XC.isNotEmpty(grantTypes))
						{
							for(String grantType : grantTypes)
								types.add(sGrantTypeMap.get(grantType)) ;
						}
					})
					.tokenSettings(TokenSettings.builder()
							.refreshTokenTimeToLive(Duration.ofSeconds(aTokenTimeToLiveInSeconds))
							.accessTokenTimeToLive(Duration.ofSeconds(aTokenTimeToLiveInSeconds))
							.build())
					;

			Set<String> urls = getCodeCallbackUrlSet();
			if (XC.isNotEmpty(urls))
			{
				for (String url : urls)
				{
					builder.redirectUri(url);
				}
			}
			String[] scopes = getScopes();
			if (XC.isNotEmpty(scopes))
			{
				for (String scope : scopes)
				{
					builder.scope(scope);
				}
			}
			RegisteredClient securityClient = builder
					.clientAuthenticationMethods((methods)->{
						String[] array = getAuthMethods() ;
						if(XC.isNotEmpty(array))
						{
							for(String method : array)
								methods.add(new ClientAuthenticationMethod(method)) ;
 						}
					})
					.clientSettings(ClientSettings.builder()
							.requireAuthorizationConsent(true)
							.build())
					.build() ;
			mSecurityClient = securityClient;
		}
		return mSecurityClient;
	}
	
	/**
	 * 是否是一个WebApp应用，false表示是一个中台微服务
	 * @param aClientApp
	 * @return
	 */
	public static boolean isWebApp(ClientApp aClientApp)
	{
		return !XC.contains(aClientApp.getGrantTypes() , AppConsts.sAppGrantType_app_only) ; 
	}
}
