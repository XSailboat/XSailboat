package team.sailboat.commons.ms.authclient;

import java.util.Collection;
import java.util.Date;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class CoupleAuthenticationToken extends AbstractAuthenticationToken
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	String mAccessToken ; 
	String mRefreshToken ;
	
	User mUser ;
	
	Date mIssueTime ;
	Date mExpiredTime ;
	
	boolean mForceExpired = false ;
	
	public CoupleAuthenticationToken(String aAccessToken , String aRefreshToken 
			, User aUser , Date aIssueTime , Date aExpiredTime)
	{
		super(null) ;
		mUser = aUser ;
		mAccessToken = aAccessToken ;
		mRefreshToken = aRefreshToken ;
		
		mIssueTime = aIssueTime ;
		mExpiredTime = aExpiredTime ;
	}
	
	@Override
	public Collection<GrantedAuthority> getAuthorities()
	{
		return mUser.getAuthorities();
	}
	
	public void setAccessToken(String aAccessToken)
	{
		mAccessToken = aAccessToken;
	}
	
	public void setRefreshToken(String aRefreshToken)
	{
		mRefreshToken = aRefreshToken;
	}
	
	public void setExpiredTime(Date aExpiredTime)
	{
		mExpiredTime = aExpiredTime;
	}
	
	public void setIssueTime(Date aIssueTime)
	{
		mIssueTime = aIssueTime;
	}

	@Override
	public String getCredentials()
	{
		return mRefreshToken ;
	}

	@Override
	public User getPrincipal()
	{
		return mUser;
	}
	
	public String getAccessToken()
	{
		return mAccessToken;
	}
	
	public String getRefreshToken()
	{
		return mRefreshToken;
	}
	
	public boolean isExpired()
	{
		return mForceExpired || (mExpiredTime != null && mExpiredTime.before(new Date())) ;
	}
	
	public void setForceExpired(boolean aForceExpired)
	{
		mForceExpired = aForceExpired;
	}
	
	public Date getExpiredTime()
	{
		return mExpiredTime;
	}
	
	public Date getIssueTime()
	{
		return mIssueTime;
	}

}
