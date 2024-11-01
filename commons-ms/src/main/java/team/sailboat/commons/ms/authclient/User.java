package team.sailboat.commons.ms.authclient;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.XString;

public class User implements UserDetails
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	String mId ;
	String mUsername ;
	String mRealName ;
	String mSex ;
	
	JSONObject mAdditionProperties ;
	
	Set<GrantedAuthority> mAuthorities ;
	
	Map<String, Set<GrantedAuthority>> mSubspaceAuthsMap ;
	
	public User(String aId , String aUsername
			, String... aAuthorities)
	{
		mId = aId ;
		mUsername = aUsername ;
		setAuthorities(aAuthorities) ;
	}
	
	public void setUsername(String aUsername)
	{
		mUsername = aUsername;
	}
	
	public boolean hasSubspaceAuths()
	{
		return XC.isNotEmpty(mSubspaceAuthsMap) ;
	}
	
	public void setAuthorities(String... aAuthorities)
	{
		Set<GrantedAuthority> authorities = XC.hashSet() ;
		Map<String, Set<GrantedAuthority>> subspaceAuthsMap = null ;
		if(XC.isNotEmpty(aAuthorities))
		{
			Set<String> subspaceIdSet = XC.hashSet() ;
			for(String authStr : aAuthorities)
			{
				int i = authStr.indexOf(':') ;
				if(i != -1)
				{
					if(subspaceAuthsMap == null)
						subspaceAuthsMap = XC.hashMap() ;
					String subspaceId = authStr.substring(0 , i) ;
					subspaceIdSet.add(subspaceId) ;
					Set<GrantedAuthority> auths = subspaceAuthsMap.get(subspaceId) ;
					if(auths == null)
					{
						auths = XC.hashSet() ;
						subspaceAuthsMap.put(subspaceId, auths) ;
					}
					auths.add(new SimpleGrantedAuthority(authStr)) ;
					auths.add(new SimpleGrantedAuthority(authStr.substring(i+1))) ;
				}
				else
					authorities.add(new SimpleGrantedAuthority(authStr)) ;
			}
			if(!authorities.isEmpty() && subspaceAuthsMap != null)
			{
				for(Entry<String, Set<GrantedAuthority>> entry : subspaceAuthsMap.entrySet())
				{
					entry.getValue().addAll(authorities) ;
				}
			}
			if(!subspaceIdSet.isEmpty())
			{
				List<SimpleGrantedAuthority> authList = XC.arrayList() ;
				for(String subspaceId : subspaceIdSet)
					authList.add(new SimpleGrantedAuthority(subspaceId+":")) ;
				
				authorities.addAll(authList) ;
				
				for(Entry<String, Set<GrantedAuthority>> entry : subspaceAuthsMap.entrySet())
				{
					entry.getValue().addAll(authList) ;
 				}
			}
			
			
		}
		mAuthorities = authorities ;
		mSubspaceAuthsMap = subspaceAuthsMap ;
		
	}
	
	public String getId()
	{
		return mId;
	}

	@Override
	public Collection<GrantedAuthority> getAuthorities()
	{
		String subspaceid = (String)AppContext.getThreadLocal("user_subspaceid") ;
		if(XString.isNotEmpty(subspaceid) && mSubspaceAuthsMap != null)
		{
			return JCommon.defaultIfNull(mSubspaceAuthsMap.get(subspaceid) , Collections.emptySet()) ;
		}
		return JCommon.defaultIfNull(mAuthorities , Collections.emptySet()) ;
	}

	public String getUsername()
	{
		return mUsername ;
	}
	
	public String getRealName()
	{
		return mRealName ;
	}
	public void setRealName(String aRealName)
	{
		mRealName = aRealName;
	}
	
	public void setSex(String aSex)
	{
		mSex = aSex;
	}
	public String getSex()
	{
		return mSex;
	}
	
	public void setAdditionProperties(JSONObject aAdditionProperties)
	{
		mAdditionProperties = aAdditionProperties;
	}
	public Object getAdditionProperty(String aName)
	{
		return mAdditionProperties != null ? mAdditionProperties.opt(aName) : null ;
	}

	@Override
	public String getPassword()
	{
		return null;
	}

	@Override
	public boolean isAccountNonExpired()
	{
		return false;
	}

	@Override
	public boolean isAccountNonLocked()
	{
		return false;
	}

	@Override
	public boolean isCredentialsNonExpired()
	{
		return false;
	}

	@Override
	public boolean isEnabled()
	{
		return false;
	}
}
