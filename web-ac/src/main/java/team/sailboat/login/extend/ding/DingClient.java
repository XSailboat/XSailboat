package team.sailboat.login.extend.ding;

import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpMethod;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiGettokenRequest;
import com.dingtalk.api.request.OapiSnsGetuserinfoBycodeRequest;
import com.dingtalk.api.request.OapiUserGetRequest;
import com.dingtalk.api.request.OapiUserGetUseridByUnionidRequest;
import com.dingtalk.api.request.OapiV2DepartmentGetRequest;
import com.dingtalk.api.request.OapiV2DepartmentListparentbyuserRequest;
import com.dingtalk.api.response.OapiGettokenResponse;
import com.dingtalk.api.response.OapiSnsGetuserinfoBycodeResponse;
import com.dingtalk.api.response.OapiSnsGetuserinfoBycodeResponse.UserInfo;
import com.dingtalk.api.response.OapiUserGetResponse;
import com.dingtalk.api.response.OapiUserGetUseridByUnionidResponse;
import com.dingtalk.api.response.OapiV2DepartmentGetResponse;
import com.dingtalk.api.response.OapiV2DepartmentGetResponse.DeptGetResponse;
import com.dingtalk.api.response.OapiV2DepartmentListparentbyuserResponse;
import com.dingtalk.api.response.OapiV2DepartmentListparentbyuserResponse.DeptParentResponse;
import com.taobao.api.ApiException;

import team.sailboat.commons.fan.collection.AutoCleanHashMap;
import team.sailboat.commons.fan.collection.XC;

public class DingClient
{
	String mUrlGetUserInfoByCode = "https://oapi.dingtalk.com/sns/getuserinfo_bycode" ;
	String mUrlGetToken = "https://oapi.dingtalk.com/gettoken" ;
	String mUrlGetOrgTrunk = "https://oapi.dingtalk.com/topapi/org/union/trunk/get" ;
	String mUrlGetDepartment = "https://oapi.dingtalk.com/topapi/v2/department/get" ;
	String mUrlGetUserIdByUnionId = "https://oapi.dingtalk.com/user/getUseridByUnionid" ;
	String mUrlGetDepartmentsOfUser = "https://oapi.dingtalk.com/topapi/v2/department/listparentbyuser" ;
	
	String mAppKey ;
	String mAppSecret ;
	
	final AutoCleanHashMap<String, String> mTokenCache = AutoCleanHashMap.withExpired_Created(120) ;
	final Object mTokenMutex = new Object() ;
	
	public DingClient(String aAppKey , String aAppSecret)
	{
		mAppKey = aAppKey ;
		mAppSecret = aAppSecret ;
	}
	
	public UserInfo getUserInfoByCode(String aCode) throws ApiException
	{
		DefaultDingTalkClient client2 = new DefaultDingTalkClient(mUrlGetUserInfoByCode);
	    OapiSnsGetuserinfoBycodeRequest codeRequest = new OapiSnsGetuserinfoBycodeRequest();
	    // 通过扫描二维码，跳转指定的redirect_uri后，向url中追加的code临时授权码
	    codeRequest.setTmpAuthCode(aCode);
	    OapiSnsGetuserinfoBycodeResponse codeResponse = client2.execute(codeRequest, mAppKey 
					, mAppSecret);
	    return codeResponse.getUserInfo() ;
	}
	
	String getAccessToken() throws ApiException
	{
		String token = mTokenCache.get("TOKEN") ;
		if(token == null)
		{
			synchronized (mTokenMutex)
			{
				token = mTokenCache.get("TOKEN") ;
				if(token == null)
				{
					DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/gettoken");
					OapiGettokenRequest getTokenRequest = new OapiGettokenRequest();
					getTokenRequest.setAppkey(mAppKey);
					getTokenRequest.setAppsecret(mAppSecret);
					getTokenRequest.setHttpMethod(HttpMethod.GET.name());
					OapiGettokenResponse rsp = client.execute(getTokenRequest);
					token = rsp.getAccessToken() ;
					mTokenCache.put("TOKEN" , token) ;
					System.out.println("TOKEN："+token);
				}
			}
		}
		return token ;
	}
	
	public String getUserDetailInfo(String aUserId) throws ApiException
	{
		DingTalkClient clientNew = new DefaultDingTalkClient("https://oapi.dingtalk.com/user/get");
		OapiUserGetRequest request = new OapiUserGetRequest();
		request.setUserid(aUserId);
		request.setHttpMethod(HttpMethod.GET.name());
		OapiUserGetResponse userInfoRsp = clientNew.execute(request, getAccessToken()) ;
		System.out.println(userInfoRsp.getBody());
		return userInfoRsp.getBody() ;
	}
	
	public String getUserIdByUnionid(String aUnionid) throws ApiException
	{
		DingTalkClient defaultDingTalkClient = new DefaultDingTalkClient(mUrlGetUserIdByUnionId);
		OapiUserGetUseridByUnionidRequest getUserIdByUnionIdRequest = new OapiUserGetUseridByUnionidRequest();
		getUserIdByUnionIdRequest.setUnionid(aUnionid);
		getUserIdByUnionIdRequest.setHttpMethod(HttpMethod.GET.name());
		OapiUserGetUseridByUnionidResponse rsp1 = defaultDingTalkClient.execute(getUserIdByUnionIdRequest, getAccessToken());
		return rsp1.getUserid();
	}
	
	public List<DeptParentResponse> getDepartmentsOfUser(String aUserId) throws ApiException
	{
		DingTalkClient client = new DefaultDingTalkClient(mUrlGetDepartment);
		OapiV2DepartmentListparentbyuserRequest req = new OapiV2DepartmentListparentbyuserRequest();
		req.setUserid(aUserId);
		OapiV2DepartmentListparentbyuserResponse rsp = client.execute(req, getAccessToken());
		return rsp.getResult() == null?Collections.emptyList():rsp.getResult().getParentList() ;
	}
	
	public DeptGetResponse getDepartment(Long aId) throws ApiException
	{
		DingTalkClient client = new DefaultDingTalkClient(mUrlGetDepartment) ;
		OapiV2DepartmentGetRequest req = new OapiV2DepartmentGetRequest();
		req.setDeptId(aId);
		req.setLanguage("zh_CN");
		OapiV2DepartmentGetResponse rsp = client.execute(req, getAccessToken());
		return rsp.getResult() ;
	}
	
	public String getMainDepartmentName(String aUnionId) throws ApiException
	{
		String userId = getUserIdByUnionid(aUnionId) ;
		List<DeptParentResponse> deptList = getDepartmentsOfUser(userId) ;
		if(XC.isNotEmpty(deptList))
		{
			Long deptId = null ;
			int deepth = 0 ;
			for(DeptParentResponse dept : deptList)
			{
				List<Long> idList = dept.getParentDeptIdList() ;
				Long deptId_0 = idList.get(0) ;
				if(deptId == null)
				{
					deptId = deptId_0 ;
					deepth = idList.size() ;
				}
				else if(deepth > idList.size())
					deptId = deptId_0 ;
				else if(deepth == idList.size() && deptId>deptId_0)
					deptId =  deptId_0 ;
			}
			return getDepartment(deptId).getName() ;
		}
		return null ;
	}
}
