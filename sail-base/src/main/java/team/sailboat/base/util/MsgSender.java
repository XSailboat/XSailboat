package team.sailboat.base.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import team.sailboat.base.HttpClientProvider;
import team.sailboat.base.SysConst;
import team.sailboat.commons.fan.app.App;
import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.http.Request;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.time.XTime;

public class MsgSender
{
	static final Logger sLogger = LoggerFactory.getLogger(MsgSender.class) ;
	
	static HttpClientProvider sMsgCenterClientPvd ;
	
	/**
	 * 发送运维消息
	 * @param aLevel
	 * @param aContent
	 * @param aDataJo
	 * @param aDigest
	 * @param aLocation
	 * @throws Exception
	 */
	public static void sendOM(int aLevel
			, String aContent
			, JSONObject aDataJo
			, String aDigest
			, String aLocation) throws Exception
	{
		send(SysConst.sMsgTopic_sys_opt , aLevel, aContent, aDataJo, aDigest, aLocation) ;
	}
	
	/**
	 * 
	 * @param aCtx
	 * @param aClientId
	 * @param aTopicName
	 * @param aLevel
	 * @param aContent
	 * @param aDataJo
	 * @param aDigest			消息摘要。如果想抑制某一种模式的消息，一天只产生一条，可以设置为：1d;xxxxxxx
	 * @param aLocation
	 * @throws Exception
	 */
	public static void send(String aTopicName
			, int aLevel
			, String aContent
			, JSONObject aDataJo
			, String aDigest
			, String aLocation) throws Exception
	{
		if(sMsgCenterClientPvd == null)
		{
			sMsgCenterClientPvd = HttpClientProvider.ofSysApp(SysConst.sAppName_SailMSMsg) ;
		}
		String producerId = AppContext.get("sys.msg.producer.id" , String.class) ;
		Assert.notEmpty(producerId , "没有设置消息生产者id（sys.msg.producer.id），不能调用消息发送接口！") ;
		
		JSONObject msgJo = new JSONObject()
				.put("partition" , "sys")
				.put("type" , "Event")
				.put("level", aLevel)
				.put("content" , aContent)
				.putIf(aDataJo != null , "data" , ()->aDataJo.toJSONString())
				.put("eventTime" , XTime.current$yyyyMMddHHmmssSSS())
				.putIf(aDigest != null ,  "digest" , aDigest)
				.put("location" , aLocation)
				.put("source" , App.instance().getName())
				;
		// 发送消息
		sMsgCenterClientPvd.get().ask(Request.POST().path("/msg/one")
				.queryParam("clientId" , producerId)
				.queryParam("clientAppId", "__INNER_APP__")
				.queryParam("topicName" , aTopicName)
				.setJsonEntity(msgJo)
				) ;
		sLogger.info("已经向消息中心发送消息：{}", msgJo) ;
	}
}
