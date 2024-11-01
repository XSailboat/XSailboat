package team.sailboat.ms.crane.controller;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import team.sailboat.ms.crane.bean.HostProfile;
import team.sailboat.ms.crane.bean.HostValidResult;
import team.sailboat.ms.crane.bean.SysProperty;
import team.sailboat.ms.crane.service.SysPlanService;

/**
 * 系统规划的接口门面
 *
 * @author yyl
 * @since 2024年10月11日
 */
@Tag(name = "系统规划")
@RestController
@RequestMapping(value="/sysPlan")
public class SysPlanController
{
	@Autowired
	SysPlanService mService ;
	
	@Operation(description = "取得所有主机信息")
	@GetMapping(value="/host/profile/all" , produces = MediaType.APPLICATION_JSON_VALUE)
	public Collection<HostProfile> getAllHostProfiles()
	{
		return mService.getAllHostProfiles() ;
	}
	

	@Operation(description = "创建一个主机信息")
	@RequestBody(description = "主机配置")
	@PostMapping(value="/host/profile/one")
	public void createHostProfile(@org.springframework.web.bind.annotation.RequestBody HostProfile aHostProfile) throws Exception
	{
		mService.createHostProfile(aHostProfile) ;
	}
	
	@Operation(description = "更新指定主机名的一个主机信息。前端需要注意，如果用户修改了主机名，需要先调用删除操作，再调用创建操作")
	@RequestBody(description = "主机配置")
	@Parameter(name="oldHostName" , description = "旧主机名。如果重命名了主机名，则必需指定")
	@PutMapping(value="/host/profile/one/_update")
	public void updateHostProfile(@org.springframework.web.bind.annotation.RequestBody HostProfile aHostProfile
			, @RequestParam(name="oldHostName" , required = false) String aOldHostName) throws Exception
	{
		mService.updateHostProfile(aHostProfile , aOldHostName) ;
	}
	
	@Operation(description = "删除一个主机信息")
	@Parameter(name="hostName" , description = "主机名")
	@DeleteMapping(value="/host/profile/one")
	public void deleteHostProfile(@RequestParam("hostName") String aHostname) throws Exception
	{
		mService.deleteHostProfile(aHostname) ;
	}
	
	@Operation(description = "从指定的名称的主机规划中取消部署指定的模块")
	@Parameter(name="module" , description = "模块名" , required = true)
	@DeleteMapping(value="/host/profile/one/module")
	public void removeModuleFromHostProfile(@RequestParam("hostName") String aHostName
			, @RequestParam("module") String aModule) throws Exception
	{
		mService.removeModuleFromHostProfile(aHostName, aModule) ;
	}
	
	@Operation(description = "取得系统参数表")
	@GetMapping(value="/property/all" , produces = MediaType.APPLICATION_JSON_VALUE)
	public List<SysProperty> getSysProperties()
	{
		return mService.getSysProperties() ;
	}
	
	@Operation(description = "更新一个系统参数")
	@Parameters({
		@Parameter(name="propertyName" , description = "参数名") ,
		@Parameter(name="propertyValue" , description = "参数值。没设置表示不更新参数值" , required = false) ,
		@Parameter(name="description" , description = "参数的描述信息。没设置表示不更新描述信息" , required = false)
	})
	@PutMapping(value="/property/one")
	public void updateSysProperty(@RequestParam("propertyName") String aPropertyName
			, @RequestParam(name="propertyValue" , required = false) String aPropertyValue
			, @RequestParam(name="description" , required = false) String aDescription)
	{
		mService.updateSysProperty(aPropertyName , aPropertyValue , aDescription) ;
	}
	
	@Operation(description = "创建一个新的系统参数")
	@Parameters({
		@Parameter(name="propertyName" , description = "参数名") ,
		@Parameter(name="propertyValue" , description = "参数值") ,
		@Parameter(name="description" , description = "参数的描述信息")
	})
	@PostMapping(value="/property/one")
	public void createSysProperty(@RequestParam("propertyName") String aPropertyName
			, @RequestParam("propertyValue") String aPropertyValue
			, @RequestParam("description") String aDescription)
	{
		mService.createSysProperty(aPropertyName, aPropertyValue, aDescription) ;
	}
	
	@Operation(description = "取得所有模块")
	@GetMapping(value="/module/all" , produces = MediaType.APPLICATION_JSON_VALUE)
	public Collection<team.sailboat.ms.crane.bean.Module> getAllModules()
	{
		return mService.getAllModules() ;
	}
	
	@Operation(description = "验证主机的连通性，以及管理员账号和密码。返回验证结果")
	@Parameter(name="hostName" , description = "主机名")
	@GetMapping(value="/host/_validate" , produces = MediaType.APPLICATION_JSON_VALUE)
	public HostValidResult validateHostInfo(@RequestParam("hostName") String aHostName)
	{
		return mService.validateHostInfo(aHostName) ;
	}
}
