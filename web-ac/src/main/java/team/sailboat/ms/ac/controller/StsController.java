package team.sailboat.ms.ac.controller;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.struct.XInt;
import team.sailboat.commons.fan.time.XTime;
import team.sailboat.commons.ms.ac.InnerProtectedApi;
import team.sailboat.commons.web.ac.ResId;
import team.sailboat.ms.ac.IAppAuths;
import team.sailboat.ms.ac.bean.AppAmounts;
import team.sailboat.ms.ac.component.AccessStatistics;
import team.sailboat.ms.ac.dbean.ClientApp;
import team.sailboat.ms.ac.dbean.User;
import team.sailboat.ms.ac.server.IUserDataManager;
import team.sailboat.ms.ac.server.ResourceManageServer;
import team.sailboat.ms.ac.utils.SecurityUtils;

/**
 *
 * 统计数据
 *
 * @author yyl
 * @since 2024年11月4日
 */
@RequestMapping("/sts")
@RestController
public class StsController
{

	final Logger mLogger = LoggerFactory.getLogger(getClass()) ;

	@Autowired
	ResourceManageServer mResMngServer ;
	
	@Autowired
	AccessStatistics mAccessSts;

	@Operation(description = "认证中心的对象数量统计。包括用户数量、Web应用数量、中台微服务数量")
	@PreAuthorize(IAppAuths.sHasAuthority_View_GlobalStsData)
	@GetMapping(value = "/object/amount", produces = MediaType.APPLICATION_JSON_VALUE)
	public String getObjectAmounts()
	{
		AppAmounts appAmounts = mResMngServer.getClientAppDataMng().getAppAmounts();
		return JCommon.toString(new JSONObject().put("userAmount", mResMngServer.getUserDataMng().getUserAmount())
												.put("msAppAmount", appAmounts.getMsAppAmount())
												.put("webAppAmount", appAmounts.getWebAppAmount()));
	}
	
	@Operation(description = "取得最近30天从认证中心登录次数最多的N个用户及它们的登录次数")
	@Parameter(name="topN" , description = "排行前N")
	@PreAuthorize(IAppAuths.sHasAuthority_View_GlobalStsData)
	@GetMapping(value = "/visitTimes/user/30d/topN" , produces = MediaType.APPLICATION_JSON_VALUE)
	public String getUsersVisitTimes30d(@RequestParam(name="topN" , required = false , defaultValue = "30") int aTopN)
	{
		List<Map.Entry<String , XInt>> entryList = XC.getTopNEntries(mAccessSts.getUserVisitTimes30d() , aTopN , (a , b)->a.i-b.i) ;
		JSONArray ja = JSONArray.one() ;
		IUserDataManager userDataMng = mResMngServer.getUserDataMng() ;
		for(Map.Entry<String , XInt> entry : entryList)
		{
			User user = userDataMng.getUser(entry.getKey()) ;
			ja.put(new JSONObject().put("userId" , entry.getKey())
					.put("displayName" , user == null?"已删除的用户":user.getDisplayName())
					.put("times" , entry.getValue().i)) ;
		}
		return ja.toJSONString() ;
	}

	@Operation(description = "最近30天，各个应用和总体的访问次数")
	@PreAuthorize(IAppAuths.sHasAuthority_View_GlobalStsData)
	@GetMapping(value = "/visitTimes/clientApp/30d", produces = MediaType.APPLICATION_JSON_VALUE)
	public String getClientAppVistTimes30d()
	{
		JSONArray ja = new JSONArray();
		// 取出所有应用名
		ja.put(new JSONArray().put("date").put("total"));
		Date startDate = XTime.plusDays(XTime.today(), -29);
		Date day = startDate;
		for (int i = 0; i < 30; i++)
		{
			String dateStr = XTime.format$yyyyMMdd(day);
			ja.put(new JSONArray()	.put(dateStr)
									.put(mAccessSts.getTotalTimes(dateStr, 0)));
			day = XTime.plusDays(day, 1);
		}

		List<ClientApp> apps = mResMngServer.getClientAppDataMng().getClientApps();
		apps.sort((app1, app2) -> JCommon.compare(app1.getName(), app2.getName()));
		JSONArray firstRow = ja.optJSONArray(0);
		for (ClientApp app : apps)
		{
			firstRow.put(app.getName());
		}
		final int len = ja.size();
		for (int i = 1; i < len; i++)
		{
			JSONArray row_i = ja.optJSONArray(i);
			String dateStr = row_i.optString(0);
			for (ClientApp app : apps)
			{
				row_i.put(mAccessSts.getAppTimes(dateStr, app.getId(), 0));
			}
		}

		return ja.toJSONString();
	}
	
	@InnerProtectedApi
	@Operation(description = "取得当前用户对各个应用最近30田的访问次数")
	@GetMapping(value = "/visitTimes/ofSelf/30d", produces = MediaType.APPLICATION_JSON_VALUE)
	public String getSelfVistTimes30d()
	{
		User user = SecurityUtils.checkUser() ;
		return getUserVisitTimes30d(user.getId()) ;
	}

	@Operation(description = "取得指定用户对各个应用最近30田的访问次数")
	@Parameter(name="userId" , description = "用户id")
	@PreAuthorize(IAppAuths.sHasAuthority_View_GlobalStsData
			+ " or " + IAppAuths.sHasAuthority_CDU_UserData
			+ " or " + IAppAuths.sHasAuthority_View_AllUsers
			+ " or hasAuthority('USER_ID_' + #_resId_)" )
	@GetMapping(value = "/visitTimes/ofUser/30d", produces = MediaType.APPLICATION_JSON_VALUE)
	public String getUserVisitTimes30d(@ResId @RequestParam("userId") String aUserId)
	{
		JSONArray ja = new JSONArray();
		// 取出所有应用名
		ja.put(new JSONArray().put("date").put("total"));
		Date startDate = XTime.plusDays(XTime.today(), -29);
		Date day = startDate;
		for (int i = 0; i < 30; i++)
		{
			String dateStr = XTime.format$yyyyMMdd(day);
			ja.put(new JSONArray()	.put(dateStr)
									.put(mAccessSts.getUserTimes(dateStr, aUserId, 0)));
			day = XTime.plusDays(day, 1);
		}

		List<ClientApp> apps = mResMngServer.getClientAppDataMng().getClientApps() ;
		apps.sort((app1, app2) -> JCommon.compare(app1.getName(), app2.getName()));
		JSONArray firstRow = ja.optJSONArray(0);
		for (ClientApp app : apps)
		{
			firstRow.put(app.getName());
		}
		final int len = ja.size();
		for (int i = 1; i < len; i++)
		{
			JSONArray row_i = ja.optJSONArray(i);
			String dateStr = row_i.optString(0);
			for (ClientApp app : apps)
			{
				row_i.put(mAccessSts.getAppUserTimes(dateStr, app.getId(), aUserId, 0));
			}
		}

		return ja.toJSONString();
	}
	
	@Operation(description = "取得所有用户的最近登录时间")
	@PreAuthorize(IAppAuths.sHasAuthority_View_GlobalStsData
			+ " or " +IAppAuths.sHasAuthority_CDU_UserData
			+ " or " +IAppAuths.sHasAuthority_View_AllUsers)
	@GetMapping(value="/user/all/loginTime/latest" , produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, String> getUsersLatestLoginTime() throws SQLException
	{
		return mAccessSts.getUsersLatestLoginTime();
	}
	
	@Operation(description = "取得ClientApp的数量统计信息。包括总数量(totalAmount)、可用的ClientApp数量(enabledAmount)"
			+ "、不可用的ClientApp数量(unenabledAmount)")
	@PreAuthorize(IAppAuths.sHasAuthority_View_GlobalStsData)
	@GetMapping(value="/clientApp/amount" , produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Integer> getClientAppAmounts()
	{
		Map<String, Integer> sts = new HashMap<>();
		List<ClientApp> apps = mResMngServer.getClientAppDataMng().getClientApps() ;
		int enabledAmount = 0 ;
		int unenabledAmount = 0 ;
		for(ClientApp clientApp : apps)
		{
			if (clientApp.isEnabled())
			{
				++enabledAmount ;
			}
			else
			{
				++unenabledAmount ;
			}
		}
		sts.put("totalAmount" , apps.size());
		sts.put("enabledAmount" , enabledAmount);
		sts.put("unenabledAmount" , unenabledAmount) ;
		return sts;
	}
}
