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
import team.sailboat.commons.fan.gadget.RSAUtils;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.ms.crypto.RSAKeyPairMaker4JS;
import team.sailboat.ms.crane.bean.HostProfile;
import team.sailboat.ms.crane.bean.SysProperty;
import team.sailboat.ms.crane.bean.ValidResult;
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
	
	@Autowired
	RSAKeyPairMaker4JS mRSAMaker ;
	
	@Operation(description = "取得所有主机信息")
	@Parameter(name="publicKey" , description = "客户端应用的公钥")
	@GetMapping(value="/host/profile/all" , produces = MediaType.APPLICATION_JSON_VALUE)
	public Collection<HostProfile> getAllHostProfiles( @RequestParam(name="publicKey" , required = false) String aPublicKey) throws Exception
	{
		Collection<HostProfile> hostProfiles = mService.getAllHostProfiles() ;
		if(XString.isNotEmpty(aPublicKey))
		{
			for(HostProfile hostProfile : hostProfiles)
			{
				hostProfile.setAdminPswd(RSAUtils.encrypt(aPublicKey , hostProfile.getAdminPswd())) ;
				hostProfile.setSysPswd(RSAUtils.encrypt(aPublicKey , hostProfile.getSysPswd())) ;
			}
		}
		return hostProfiles ;
	}

	@Operation(description = "创建一个主机信息")
	@Parameter(name="codeId" , description = "动态RSA秘钥的标识码。Https协议下，可以不用加密")
	@RequestBody(description = "主机配置")
	@PostMapping(value="/host/profile/one")
	public void createHostProfile(@RequestParam(name="codeId" , required = false) String aCodeId
			, @org.springframework.web.bind.annotation.RequestBody HostProfile aHostProfile) throws Exception
	{
		aHostProfile.setAdminPswd(mRSAMaker.decrypt4js(aCodeId , aHostProfile.getAdminPswd())) ;
		aHostProfile.setSysPswd(mRSAMaker.decrypt4js(aCodeId , aHostProfile.getSysPswd())) ;
		mService.createHostProfile(aHostProfile) ;
	}
	
	@Operation(description = "更新指定主机名的一个主机信息。前端需要注意，如果用户修改了主机名，需要先调用删除操作，再调用创建操作")
	@Parameters({
		@Parameter(name="oldHostName" , description = "旧主机名。如果重命名了主机名，则必需指定") ,
		@Parameter(name="codeId" , description = "动态RSA秘钥的标识码。Https协议下，可以不用加密")
	})
	@RequestBody(description = "主机配置")
	@PutMapping(value="/host/profile/one/_update")
	public void updateHostProfile(@org.springframework.web.bind.annotation.RequestBody HostProfile aHostProfile
			, @RequestParam(name="oldHostName" , required = false) String aOldHostName
			, @RequestParam(name="codeId" , required = false) String aCodeId) throws Exception
	{
		aHostProfile.setAdminPswd(mRSAMaker.decrypt4js(aCodeId , aHostProfile.getAdminPswd())) ;
		aHostProfile.setSysPswd(mRSAMaker.decrypt4js(aCodeId , aHostProfile.getSysPswd())) ;
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
	@PostMapping(value="/host/_validate" , produces = MediaType.APPLICATION_JSON_VALUE)
	public ValidResult validateHostInfo(@RequestParam("hostName") String aHostName)
	{
		return mService.validateHostInfo(aHostName) ;
	}
	
	@Operation(description = "验证指定的用户名密码，在指定的主机上是否正确")
	@Parameters({
		@Parameter(name="ip" , description = "主机的ip地址") ,
		@Parameter(name="port" , description = "SailPyInstaller的服务端口") ,
		@Parameter(name="username" , description = "用户名") ,
		@Parameter(name="codeId" , description = "动态RSA秘钥的标识码。") ,
		@Parameter(name="password" , description = "密码。用动态RSA秘钥的公钥加密过后的密码。") ,
	})
	@PostMapping(value="/host/user_pswd/_validate" , produces = MediaType.APPLICATION_JSON_VALUE)
	public ValidResult validateHostUserPswd(@RequestParam("ip") String aIp
			, @RequestParam("port") int aPort 
			, @RequestParam("username") String aUsername
			, @RequestParam("codeId") String aCodeId
			, @RequestParam("password") String aPassword) throws Exception
	{
		String password = mRSAMaker.decrypt4js(aCodeId , aPassword) ;
		return mService.validateHostUserPswd(null , aIp , aPort , aUsername, password) ;
	}
}
