package team.sailboat.ms.crane.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;

/**
 * 页面跳转前端控制器 
 *
 * @author ForTower
 * @since 2024年10月11日
 */

@Controller
public class ViewController
{
	/**
	 * 跳转首页展视图
	 */
	@RequestMapping({ "/", "/home", "/index" })
	public String goHome()
	{
		return "biz/index";
	}
}