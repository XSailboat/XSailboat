package team.sailboat.ms.crane.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import team.sailboat.ms.crane.bean.AppPkg;
import team.sailboat.ms.crane.service.AppStoreService;

/**
 * 应用仓库相关的接口门面
 *
 * @author yyl
 * @since 2024年10月11日
 */
@Tag(name = "应用仓库")
@RestController
@RequestMapping(value="/appstore")
public class AppStoreController
{
	@Autowired
	AppStoreService mService ;
	
	@Operation(description = "取得应用仓库中的所有应用软件包")
	@GetMapping(value = "/appPkg/all" , produces = MediaType.APPLICATION_JSON_VALUE)
	public List<AppPkg> getAppPkgs()
	{
		return mService.getAppPkgs() ;
	}
	
	@Operation(description = "取得指定名称的引用软件包")
	@Parameter(name = "name" , description = "应用软件包名" , required = true)
	@GetMapping(value="/appPkg/one" , produces = MediaType.APPLICATION_JSON_VALUE)
	public AppPkg getAppPkgByName(@RequestParam("name") String aName)
	{
		return mService.getAppPkgByName(aName) ;
	}
	
	@Operation(description = "上传程序包。请求的body以流的方式传输安装包数据。如果软件包已经存在会覆盖")
	@Parameter(name = "name" , description = "应用软件包名" , required = true)
	@PostMapping(value="/appPkg/one" , consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public void uploadAppPkg(HttpServletRequest aReq
			, @RequestParam("name") String aName) throws Exception
	{
		mService.uploadAppPkg(aReq.getInputStream() , aName) ;
	}
	
	@Operation(description = "删除指定的应用软件包")
	@Parameter(name = "name" , description = "应用软件包名" , required = true)
	@DeleteMapping(value="/appPkg/one")
	public void deleteAppPkg(@RequestParam("name") String aName)
	{
		mService.deleteAppPkg(aName) ;
	}
}
