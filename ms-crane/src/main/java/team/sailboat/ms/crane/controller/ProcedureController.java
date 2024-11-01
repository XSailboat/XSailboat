package team.sailboat.ms.crane.controller;

import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.ms.crane.bean.Procedure;
import team.sailboat.ms.crane.service.ProcedureService;

/**
 * 程式过程相关的接口门面
 *
 * @author yyl
 * @since 2024年10月11日
 */
@Tag(name = "程式过程")
@RestController
@RequestMapping(value="/procedure")
public class ProcedureController
{
	@Autowired
	ProcedureService mService ;
	
	@Operation(description = "取得所有程式过程")
	@GetMapping(value="/all" , produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String , Procedure> getAllProcedures()
	{
		return mService.getAllProcedures() ;
	}
	
	@Operation(description = "按分类目录(catalog)分类取得所有程式过程。")
	@GetMapping(value="/all/grouped" , produces = MediaType.APPLICATION_JSON_VALUE)
	public TreeMap<String , TreeSet<Procedure>> getAllProceduresGrouped()
	{
		return mService.getAllProceduresGrouped() ;
	}
	
	@Operation(description = "设置程式过程中的某一个操作项是否启用")
	@Parameters({
		@Parameter(name="procedureFileName" , description = "程式过程文件名") ,
		@Parameter(name="operationName" , description = "操作名") ,
		@Parameter(name="enabled" ,description = "是否启用")
	})
	@PostMapping(value="/one/operation/enabled" , produces = MediaType.APPLICATION_JSON_VALUE)
	public void setOperationEnabled(@RequestParam("procedureFileName") String aProcedureFileName
			, @RequestParam("operationName") String aOperationName
			, @RequestParam("enabled") boolean aEnabled) throws Exception
	{
		mService.setOperationEnabled(aProcedureFileName, aOperationName, aEnabled) ;
	}
	
	@Operation(description = "执行程式过程")
	@Parameter(name="procedureFileName" , description = "程式过程文件名")
	@PostMapping(value="/one/_exec")
	public void executeProcedure(@RequestParam("procedureFileName") String aProcedureFileName)
	{
		mService.executeProcedure(aProcedureFileName) ;
	}
	
	@Operation(description = "获取指定程式过程的执行日志")
	@Parameters({
		@Parameter(name="procedureFileName" , description = "程式过程文件名") ,
		@Parameter(name="seq" , description = "日志序号。第一次获取为0，否则传取得的最新日志的序号")
	})
	@GetMapping(value="/one/execLog/many" , produces = MediaType.APPLICATION_JSON_VALUE)
	public String getProcedureExecLogs(@RequestParam("procedureFileName") String aProcedureFileName
			, @RequestParam(name="seq" , required = false , defaultValue = "0") int aSeq)
	{
		return JCommon.toString(mService.getProcedureExecLogs(aProcedureFileName, aSeq)) ;
	}
}
