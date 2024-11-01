package team.sailboat.commons.ms.controller;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import team.sailboat.commons.fan.gadget.RSAKeyPairMaker;
import team.sailboat.commons.fan.gadget.ScrollQuerySite;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.sys.MemoryAssist;

@Tag(name = "框架通用接口")
@RestController
public class CommonController
{
	
	public CommonController()
	{
	}
	
	@Operation(description = "按指定的句柄滚动获取下一批")
	@Parameters({
		@Parameter(name="handle" , description = "查询句柄" , required = true) ,
		@Parameter(name="size" , description = "一次查询返回数量" , example = "1000")
	})
	@GetMapping(value="/common/scrollNext" , produces = MediaType.APPLICATION_JSON_VALUE)
	public String scrollNext(@RequestParam("handle") String aHandle , @RequestParam(name="size" , required = false , defaultValue = "-1") int aSize) throws Throwable
	{
		return JCommon.toString(ScrollQuerySite.getInstance().scrollNext(aHandle, aSize)) ;
	}
	
	@Operation(description = "服务状态，如果返回1表示服务正常。用来检测服务可用性")
	@GetMapping(value="/status" , produces = MediaType.TEXT_PLAIN_VALUE)
	public String status()
	{
		return "1" ;
	}
	
	/**
	 * 公钥1分钟内有效
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	@ResponseBody
	@GetMapping(value="/common/security/rsa-publickey" , produces = MediaType.APPLICATION_JSON_VALUE)
	public String getRSAPubliscKey() throws NoSuchAlgorithmException
	{
		Entry<String, KeyPair> keyPair = RSAKeyPairMaker.getDefault().newOne() ;
	    //生成公钥和私钥    
	    RSAPublicKey publicKey = (RSAPublicKey) keyPair.getValue().getPublic() ;    

	    String publicKeyExponent = publicKey.getPublicExponent().toString(16);  
	    String publicKeyModulus = publicKey.getModulus().toString(16);  
	    return new JSONObject().put("codeId" , keyPair.getKey())
	    		.put("publicKeyExponent" , publicKeyExponent)
	    		.put("publicKeyModulus" , publicKeyModulus)
	    		.toString() ;
	}
	
	@Operation(description = "取得应用的线程堆栈")
	@GetMapping(value="/common/thread/all/stackTraces" , produces = MediaType.TEXT_PLAIN_VALUE)
	public String getAllThreadStackTraces()
	{
		Map<Thread , StackTraceElement[]> traceMap = Thread.getAllStackTraces() ;
		StringBuilder strBld = new StringBuilder(10240) ;
		for(Entry<Thread, StackTraceElement[]> entry : traceMap.entrySet())
		{
			Thread thread = entry.getKey() ;
			strBld.append(thread.getName())
				.append(' ')
				.append(thread.threadId())
				.append(' ')
				.append(thread.getPriority())
				.append("\n   ")
				.append("java.lang.Thread.State: ")
				.append(thread.getState().name())
				.append('\n') ;
			for(StackTraceElement e : entry.getValue())
			{
				strBld.append("       ").append("at ").append(e.getClassName())
					.append(".")
					.append(e.getMethodName())
					.append("(")
					.append(e.getFileName()).append(':').append(e.getLineNumber())
					.append(")\n") ;
			}
		}
		return strBld.toString() ;
	}
	
	@Operation(description = "取得内存的使用情况")
	@GetMapping(value="/common/memory/usage" , produces = MediaType.APPLICATION_JSON_VALUE)
	public String getMemoryUsage()
	{
		Runtime rt = Runtime.getRuntime() ;
		long maximum =  rt.maxMemory() ;
		long total = rt.totalMemory() ;
		long free = rt.freeMemory() ;
		return new JSONObject().put("max" , maximum)
				.put("total" , total)
				.put("free", free)
				.put("maxDisplay", MemoryAssist.toAutoB(maximum, 2))
				.put("totalDisplay",  MemoryAssist.toAutoB(total , 2))
				.put("freeDisplay" , MemoryAssist.toAutoB(free, 2))
				.put("usageDisplay" , MemoryAssist.toAutoB(total - free , 2))
				.toJSONString() ;
	}
}
