package team.sailboat.ms.ac.component;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.excep.ExceptionAssist;
import team.sailboat.commons.fan.exec.CommonExecutor;
import team.sailboat.commons.fan.http.HttpClient;
import team.sailboat.commons.fan.http.ISigner;
import team.sailboat.commons.fan.http.Request;
import team.sailboat.commons.fan.http.xca.XAppSigner;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.ms.ac.data.LoginAppRecord;
import team.sailboat.ms.ac.dbean.ClientApp;
import team.sailboat.ms.ac.dbean.User;
import team.sailboat.ms.ac.frame.IUserAuthoritiesChangeListener;
import team.sailboat.ms.ac.frame.UserAuthoritiesChangeEvent;
import team.sailboat.ms.ac.server.IClientAppDataManager;
import team.sailboat.ms.ac.server.ResourceManageServer;

/**
 * 
 * 接受用户权限改变通知，异步地将它们通知给各个注册应用
 *
 * @author yyl
 * @since 2024年10月31日
 */
@Component
public class UserAuthoritiesChangeMonitor implements IUserAuthoritiesChangeListener , Runnable
		, LogoutSuccessHandler
{
	final Logger mLogger = LoggerFactory.getLogger(getClass()) ;
	
	@Autowired
	ResourceManageServer mResMngServer ;
	
	@Autowired
	OAuth2AuthorizationService mTokenService ;
	
	@Autowired
	LoginUserRegisterRepo mLoginRegisterRepo ;
	
	/**
	 * 键是appId，值是用户Id
	 */
	final Map<String , Set<String>> mNotifyTasks = XC.concurrentHashMap() ;
	
	final ReentrantLock mLock = new ReentrantLock() ;
	final Condition mHasTaskCnd = mLock.newCondition() ;
	
	final ISigner mSigner = new XAppSigner() ;
	
	boolean mInterruptted = false ;
	
	public UserAuthoritiesChangeMonitor()
	{
	}
	
	@PostConstruct
	void _init()
	{
		CommonExecutor.execInSelfThread(this , "监控并通知用户权限变化") ;
		mResMngServer.getClientAppDataMng().addListener(this) ;
	}

	@Override
	public void accept(UserAuthoritiesChangeEvent aT)
	{
		if(aT.getAppId().equals(mResMngServer.getClientAppId_SailAC()))
			return ;			// 如果是认证中心的权限发生改变，不用异步通知,能直接生效
		
		Set<String> userIds = mLoginRegisterRepo.filterNotExpiredUsersInApp(aT.getAppId() , aT.getUserIds()) ;
		if(XC.isNotEmpty(userIds))
		{
			mLock.lock();
			try
			{
				mNotifyTasks.merge(aT.getAppId() , userIds, (s1 , s2)->{
					s1.addAll(s2) ;
					return s1 ;
				}) ;
				mHasTaskCnd.signal() ;
			}
			finally
			{
				mLock.unlock();
			}
		}
	}

	@Override
	public void run()
	{
		while(!mInterruptted)
		{
			mLock.lock();
			try
			{
				while(mNotifyTasks.isEmpty())
					mHasTaskCnd.await();
				String[] appIds = mNotifyTasks.keySet().toArray(JCommon.sEmptyStringArray) ;
				for(String appId : appIds)
				{
					Set<String> userIds = mNotifyTasks.remove(appId) ;
					ClientApp app = mResMngServer.getClientAppDataMng().getClientApp(appId) ;
					if(app != null)
					{
						String urlStr = app.getRefreshUserAuthsNotifierUrl() ;
						if(XString.isNotEmpty(urlStr))
						{
							URI uri = URI.create(urlStr) ;
							HttpClient client = HttpClient.ofURI(uri , app.getAppKey() , app.getAppSecret() , mSigner) ;
							try
							{
								client.execute(Request.POST(uri).setJsonEntity(JSONArray.of(userIds))) ;
								mLogger.info("已经向应用[{}]通知刷新 {} 个用户的权限" , app.getName()
										, userIds.size()) ;
							}
							catch (Exception e)
							{
								mLogger.error("向应用[{}]通知刷新用户权限出现异常！异常消息：{}" , app.getName()
										, e.getMessage()) ;
							}
 						}
						else
						{
							mLogger.info("应用[{}]没有设置权限刷新通知地址，现用户权限发生改变，无法通知它刷新权限！" , app.getName()) ;
						}
					}
				}
			}
			catch (InterruptedException e)
			{
				mLogger.error(ExceptionAssist.getStackTrace(e)) ;
			}
			finally
			{
				mLock.unlock(); 
			}
		}
	}
	
	@Override
	public void onLogoutSuccess(HttpServletRequest aRequest, HttpServletResponse aResponse
			, Authentication aAuthentication) throws IOException, ServletException
	{
		// 向各个登录的app，发出用户登出通知
		if(aAuthentication != null)
		{
			User user = (User)aAuthentication.getPrincipal() ;
			List<LoginAppRecord> appList = mLoginRegisterRepo.getNotExpiredLoginAppsOfUser(user.getId()) ;
			if(XC.isNotEmpty(appList))
			{
				IClientAppDataManager clientAppDataMng = mResMngServer.getClientAppDataMng() ;
				for(LoginAppRecord appRcd : appList)
				{
					OAuth2Authorization authorization = mTokenService.findById(appRcd.getOAuth2AuthorizationId()) ;
					if(authorization != null)
						mTokenService.remove(authorization);
					ClientApp app =	clientAppDataMng.getClientApp(appRcd.getAppId()) ;
					if(app != null)
					{
						mLock.lock();
						try
						{
							mNotifyTasks.merge(app.getId() , XC.hashSet(user.getId()) , (s1 , s2)->{
								s1.addAll(s2) ;
								return s1 ;
							}) ;
							mHasTaskCnd.signal() ;
						}
						finally
						{
							mLock.unlock();
						}
					
					}
				}
			}
		}
		aResponse.sendRedirect(aRequest.getContextPath()) ;
	}
}
