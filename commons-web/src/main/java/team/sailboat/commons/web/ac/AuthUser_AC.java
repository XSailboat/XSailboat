package team.sailboat.commons.web.ac;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.nimbusds.jose.util.Base64URL;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.http.HttpClient;
import team.sailboat.commons.fan.http.Request;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.struct.Tuples;
import team.sailboat.commons.fan.text.XString;

/**
 * 
 * WebClientApp进行登录，得到的用户对象
 *
 * @author yyl
 * @since 2024年12月5日
 */
public class AuthUser_AC implements UserDetails, OAuth2User
{
	/**
	 * 认证中心获取用户信息的路径
	 */
	public static final String sGET_userInfo = "/SailAC/oauth2/user/info" ;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Attribute Key		<br />
	 * 用户id
	 */
	public static final String sAK_userId = "userId";

	/**
	 * Attribute Key		<br />
	 * 用户名
	 */
	public static final String sAK_username = "username";
	
	/**
	 * Attribute Key		<br />
	 * 密码
	 */
	public static final String sAK_password = "password";
	
	/**
	 * Attribute Key		<br />
	 * 真实姓名
	 */
	public static final String sAK_realName = "realName";
	
	/**
	 * Attribute Key		<br />
	 * 部门
	 */
	public static final String sAK_department = "department";
	
	/**
	 * Attribute Key		<br />
	 * 在组织中的职位
	 */
	public static final String sAK_job = "job";
	
	/**
	 * Attribute Key		<br />
	 * 性别
	 */
	public static final String sAK_sex = "sex";
	
	/**
	 * Attribute Key		<br />
	 * 手机号
	 */
	public static final String sAK_mobile = "mobile";
	
	/**
	 * Attribute Key		<br />
	 * 邮箱
	 */
	public static final String sAK_email = "email";
	
	/**
	 * Attribute Key		<br />
	 * corsToken
	 */
	public static final String sAK_corsToken = IAuthCenterConst.sTokenReply_corsToken ;

	private Set<GrantedAuthority> mAuthorities;

	private final Map<String, Object> mAttributes;

	public AuthUser_AC(Collection<? extends GrantedAuthority> aAuthorities, Map<String, Object> aAttributes)
	{
		Assert.notEmpty(aAttributes, "用户信息[参数：aAttributes]不能为空！") ;
		setAuthorities(aAuthorities) ;
		mAttributes = Collections.unmodifiableMap(new LinkedHashMap<>(aAttributes));
	}
	
	void setAuthorities(Collection<? extends GrantedAuthority> aAuthorities)
	{
		mAuthorities = (aAuthorities != null)
				? Collections.unmodifiableSet(new LinkedHashSet<>(sortAuthorities(aAuthorities)))
				: Collections.unmodifiableSet(new LinkedHashSet<>(AuthorityUtils.NO_AUTHORITIES));
	}

	Set<GrantedAuthority> sortAuthorities(Collection<? extends GrantedAuthority> aAuthorities)
	{
		SortedSet<GrantedAuthority> sortedAuthorities = new TreeSet<>(
				Comparator.comparing(GrantedAuthority::getAuthority));
		sortedAuthorities.addAll(aAuthorities);
		return sortedAuthorities;
	}

	@Override
	public Map<String, Object> getAttributes()
	{
		return mAttributes ;
	}
	
	public String getId()
	{
		return (String)mAttributes.get(sAK_userId) ;
	}

	@Override
	public String getName()
	{
		String name = JCommon.defaultIfEmpty((String)mAttributes.get(sAK_department) , "") ;
		if(!name.isEmpty())
			name +=  "-" ;
		return name += JCommon.defaultIfEmpty_0((String)mAttributes.get(sAK_realName) , this::getUsername);
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities()
	{
		return mAuthorities ;
	}

	@Override
	public String getPassword()
	{
		return (String)mAttributes.get(sAK_password) ;
	}

	@Override
	public String getUsername()
	{
		return (String)mAttributes.get(sAK_username) ;
	}

	/**
	 * 
	 * @param aUser							有用户信息
	 * @param aAccessTokenJwtStr			AccessToken里面有权限信息
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static AuthUser_AC from(OAuth2User aUser , String aAccessTokenJwtStr) throws UnsupportedEncodingException
	{
		Tuples.T2<JSONObject , Collection<? extends GrantedAuthority>> t = buildAuthoritiesFromAccessToken(aAccessTokenJwtStr) ;
		String corsToken = t.getEle_1().optString(IAuthCenterConst.sTokenReply_corsToken) ;
		Map<String , Object> attrMap = aUser.getAttributes() ;
		if(XString.isNotEmpty(corsToken))
		{
			attrMap = XC.linkedHashMap(attrMap) ;
			attrMap.put(sAK_corsToken , corsToken) ;
		}
		return new AuthUser_AC(t.getEle_2() , attrMap) ;
	}
	/**
	 * 
	 * 根据AccessToken刷新用户的权限
	 * 
	 * @param aUser
	 * @param aAccessTokenJwtStr
	 * @throws UnsupportedEncodingException
	 */
	public static void refreshAuthorities(AuthUser_AC aUser , String aAccessTokenJwtStr) throws UnsupportedEncodingException
	{
		aUser.setAuthorities(buildAuthoritiesFromAccessToken(aAccessTokenJwtStr)
				.getEle_2()) ;
	}
	
	static Tuples.T2<JSONObject , Collection<? extends GrantedAuthority>> buildAuthoritiesFromAccessToken(String aAccessTokenJwtStr) throws UnsupportedEncodingException
	{
		String payload = XString.seg_i(aAccessTokenJwtStr , '.' , 1) ;
		JSONObject payload_jobj = JSONObject.of(new String(Base64URL.from(payload).decode() , "UTF-8")) ;
		JSONArray ja = payload_jobj.optJSONArray("auths") ;
		return Tuples.of(payload_jobj, buildAuthorities(ja!=null?ja.toStringArray():null)) ;
	}
	
	static Collection<? extends GrantedAuthority> buildAuthorities(String... aAuthorities)
	{
		Set<GrantedAuthority> authorities = XC.hashSet() ;
		if(XC.isNotEmpty(aAuthorities))
		{
			for(String authStr : aAuthorities)
			{
				authorities.add(new SimpleGrantedAuthority(authStr)) ;
			}
		}
		return authorities ;
	}
	
	/**
	 * 
	 * AccessToken里面只有权限和用户id信息，还需要从认证中心读取用户信息
	 * 
	 * @param aAccessTokenJwtStr
	 * @return
	 * @throws Exception 
	 */
	public static AuthUser_AC loadFromAC(String aAccessTokenJwtStr
			, HttpClient aACClient) throws Exception
	{
		String payload = XString.seg_i(aAccessTokenJwtStr , '.' , 1) ;
		JSONObject payload_jobj = JSONObject.of(new String(Base64URL.from(payload).decode() , "UTF-8")) ;
		JSONArray ja = payload_jobj.optJSONArray("auths") ;
		// 获取用户信息的接口是通过accessToken认证的，不是XAppSign，需要授权的方式
		HttpClient tokenClient = HttpClient.ofURI(aACClient.getUri()) ;
		tokenClient.setFearure_encodeHeader(false) ;
		JSONObject userInfoJo = tokenClient.askJo(Request.GET().path(sGET_userInfo)
				.header("Authorization" , "Bearer " + aAccessTokenJwtStr)) ;
		userInfoJo.put(sAK_corsToken , payload_jobj.optString(IAuthCenterConst.sTokenReply_corsToken)) ;
		
		return new AuthUser_AC(buildAuthorities(ja!=null?ja.toStringArray():null)
				, userInfoJo) ;
	}
}
