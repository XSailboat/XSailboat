package team.sailboat.commons.ms.controller;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import team.sailboat.commons.fan.app.App;
import team.sailboat.commons.fan.collection.PropertiesEx;
import team.sailboat.commons.fan.dtool.SqlParams;
import team.sailboat.commons.fan.http.HttpConst;
import team.sailboat.commons.fan.jfilter.JFilterParser;
import team.sailboat.commons.fan.jfilter.SqlFilterBuilder;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.fan.time.XTime;
import team.sailboat.commons.ms.MSApp;
import team.sailboat.commons.ms.util.ITokenGenerator;
import team.sailboat.commons.ms.util.TokenSite;

@Tag(name = "通用工具")
@RestController
public class CommonToolController implements ITokenGenerator
{
	final AtomicLong mEncryptLastTime = new AtomicLong() ;
	final AtomicLong mDecryptLastTime = new AtomicLong() ;
	 
	protected final JFilterParser<SqlParams> mJFilterParser = new JFilterParser<>(SqlFilterBuilder::new);
	
	private TokenSite mTokenSite ;
	
	@PostConstruct
	void _init()
	{
		mTokenSite = new TokenSite("密文揭秘token", 2) ;
 	}
	
	@Override
	public String genToken(String aIp , int aUseTimes)
	{
		return mTokenSite.genTokenFor(aIp, aUseTimes) ;
	}
	
	@Operation(description = "转换JFiler过滤条件转成SQL条件")
	@Parameter(name="jfilter" , description = "JFilter过滤条件，JSON格式" , required = true)
	@GetMapping(value="/tool/common/jfilter/sql" , produces = MediaType.TEXT_PLAIN_VALUE)
	public String convertJFilterToSql(@RequestParam("jfilter") String aJFilter)
	{
		return mJFilterParser.parseFilter(JSONObject.of(aJFilter)).getSql() ;
	}
	
	@Operation(description = "取得加密后的Properties文件的取值")
	@Parameter(name="text" , description = "要加密的内容" , required = true)
	@GetMapping(value="/tool/common/encryptedPropertyValue" , produces = MediaType.TEXT_PLAIN_VALUE)
	public String getEncryptedPropertyValue(@RequestParam("text")String aText)
	{
		long lastTime = mEncryptLastTime.get() ;
		if(XTime.pass(lastTime, 1000) && mEncryptLastTime.compareAndSet(lastTime, System.currentTimeMillis()))
		{
			return XString.isEmpty(aText)?aText:PropertiesEx.asSecret(aText) ;
		}
		else
			throw new IllegalStateException("太过频繁，请稍后再试!") ;
			
	}
	
	@Operation(description = "解密密文")
	@Parameters({
		@Parameter(name="text" , description = "要解密的内容" , required = true) ,
		@Parameter(name="passwd" , description = "密码" , required = true)
	})
	@GetMapping(value="/tool/common/decryptedPropertyValue" , produces = MediaType.TEXT_PLAIN_VALUE)
	public String getDecryptedPropertyValue(@RequestParam("text")String aText 
			, @RequestParam("passwd") String aPasswd
			, HttpServletRequest aReq)
	{
		if(HttpConst.sHeaderValue_UserAgent_x_HttpClient.equalsIgnoreCase(aReq.getHeader(HttpConst.sHeaderName_UserAgent)))
		{
			// 是自己的HttpClient
			if(mTokenSite.useToken(aReq.getRemoteAddr() , aPasswd))
			{
				return JCommon.decrypt(aText) ;
			}
			else
			{
				String sysEnv = App.instance().getSysEnv() ;
				switch(sysEnv)
				{
				case "test":
				case "dev":
					if("imyourdady".equals(aPasswd))
						return JCommon.decrypt(aText) ;
				default:
					throw new IllegalArgumentException("拒绝解密!") ;
				}
			}
		}
		long lastTime = mDecryptLastTime.get() ;	
		if(XTime.pass(lastTime, 1000) && mDecryptLastTime.compareAndSet(lastTime, System.currentTimeMillis()))
		{
			Assert.equals(aPasswd , "whosyourdady", "SecretKey不正确!") ;
			if(XString.isEmpty(aText))
				return aText ;
			final int len = aText.length() ;
			if(aText.charAt(0) == '?' && len>3 && aText.charAt(len-1) == '!' && aText.charAt(len-2) == '$')
			{
				aText = aText.substring(1, len-2) ;
			}
			return JCommon.decrypt(aText) ;
		}
		else
			throw new IllegalStateException("太过频繁，请稍后再试!") ;
	}
	
	@Operation(description = "取得应用名")
	@GetMapping(value="/app/name" , produces = MediaType.TEXT_PLAIN_VALUE)
	public String getAppName()
	{
		return MSApp.instance().getName() ;
	}
}
