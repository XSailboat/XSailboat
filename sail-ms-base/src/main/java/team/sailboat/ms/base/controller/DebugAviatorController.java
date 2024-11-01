package team.sailboat.ms.base.controller ;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.ms.base.service.DebugAviatorService;

@Tag(name = "Aviator表达式调试")
@RestController
@RequestMapping("/common/aviator/debug")
@Import(DebugAviatorService.class)
public class DebugAviatorController
{
	@Autowired
	DebugAviatorService mService ;
	
	@Operation(description = "执行指定的Aviator表达式")
	@Parameters({
		@Parameter(name="expr" , description = "Aviator表达式" , required = true) ,
		@Parameter(name="args" , description = "表达式的输入参数，JSONObject格式") ,
		@Parameter(name="userId" , description = "用户Id" , required = true)
	})
	@PostMapping(value="/expr/_exec" , produces = MediaType.APPLICATION_JSON_VALUE)
	public String execAviatorExpr(@RequestParam("expr") String aExpr
			, @RequestParam("args") String aArgs
			, @RequestParam("userId") String aUserId)
	{
		Map<String , Object> argMap = null ;
		if(XString.isNotEmpty(aArgs))
		{
			argMap = JSONObject.of(aArgs).toMap() ;
		}
		return JCommon.toString(mService.execAviatorExpr(aExpr, argMap , aUserId)) ;
	}
}
