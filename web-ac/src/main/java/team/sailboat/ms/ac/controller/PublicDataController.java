package team.sailboat.ms.ac.controller;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.spring.application.ImageCaptchaApplication;
import cloud.tianai.captcha.spring.vo.CaptchaResponse;
import cloud.tianai.captcha.spring.vo.ImageCaptchaVO;
import cloud.tianai.captcha.validator.common.model.dto.ImageCaptchaTrack;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import team.sailboat.commons.fan.gadget.RSAKeyPairMaker;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.ms.ac.AppConsts;

/**
 * 
 * 完全开放，无需登录及权限控制的非敏感数据接口
 *
 * @author yyl
 * @since 2024年11月7日
 */
@RequestMapping("/public")
@RestController
public class PublicDataController
{
	final Logger mLogger = LoggerFactory.getLogger(getClass()) ;

	public final static String SESSION_KEY_IMAGE_CODE = "SESSION_KEY_IMAGE_CODE";

	static String sSupportedAuthorizationGrantTypes;

	static final String sScopesJStr;
	static
	{
		sSupportedAuthorizationGrantTypes = new JSONArray()
				.put(new JSONObject()
						.put("code" , AuthorizationGrantType.AUTHORIZATION_CODE)
						.put("name", "授权码模式"))
				.put(new JSONObject()
						.put("code", AuthorizationGrantType.REFRESH_TOKEN)
						.put("name", "刷新令牌模式"))
				.put(new JSONObject()
						.put("code", AuthorizationGrantType.CLIENT_CREDENTIALS)
						.put("name", "客户端凭证模式"))
				.toJSONString();

		sScopesJStr = new JSONArray()
				.put(new JSONObject()
						.put("code", AppConsts.sScope_user_basic)
						.put("description", "用户基本信息（姓名、性别）"))
				.put(new JSONObject()
						.put("code", AppConsts.sScope_user_org_job)
						.put("description", "用户所属组织及职务"))
				.put(new JSONObject()
						.put("code", AppConsts.sScope_user_contact_info)
						.put("description", "用户联系方式（手机、email）"))
				.toJSONString();
	}

	@Autowired
	RSAKeyPairMaker mRSAMaker;

	@Autowired
	PasswordEncoder mPasswordEncoder;

	@Autowired
	ImageCaptchaApplication imageCaptchaApplication;

	@Operation(description = "支持的授权模式")
	@GetMapping(value = "/grantType/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public String getSupportedAuthorizationGrantTypes()
	{
		return sSupportedAuthorizationGrantTypes;
	}

	@Operation(description = "支持的scope")
	@GetMapping(value = "/scope/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public String getScopes()
	{
		return sScopesJStr;
	}
	
	@Operation(description = "取得一个动态的RSA公钥，用来加密前端需要向服务器传递的敏感信息。公钥1分钟内有效")
	@ResponseBody
	@GetMapping(value = "/security/rsa-publickey", produces = MediaType.APPLICATION_JSON_VALUE)
	public String getRSAPubliscKey() throws NoSuchAlgorithmException
	{
		Map.Entry<String, KeyPair> keyPair = mRSAMaker.newOne();
		// 生成公钥和私钥
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getValue().getPublic();

		String publicKeyExponent = publicKey.getPublicExponent().toString(16);
		String publicKeyModulus = publicKey.getModulus().toString(16);
		return new JSONObject().put("codeId", keyPair.getKey())
								.put("publicKeyExponent", publicKeyExponent)
								.put("publicKeyModulus", publicKeyModulus)
								.toString();
	}
	
	@Operation(description = "取得验证码背景图片")
	@ResponseBody
	@RequestMapping("/gen")
	public CaptchaResponse<ImageCaptchaVO> genCaptcha(HttpServletRequest request,
			@RequestParam(value = "type", required = false) String type)
	{
		if (XString.isBlank(type))
		{
			type = CaptchaTypeConstant.SLIDER;
		}
		if ("RANDOM".equals(type))
		{
			int i = ThreadLocalRandom.current().nextInt(0, 4);
			if (i == 0)
			{
				type = CaptchaTypeConstant.SLIDER;
			}
			else if (i == 1)
			{
				type = CaptchaTypeConstant.CONCAT;
			}
			else if (i == 2)
			{
				type = CaptchaTypeConstant.ROTATE;
			}
			else
			{
				type = CaptchaTypeConstant.WORD_IMAGE_CLICK;
			}

		}
		CaptchaResponse<ImageCaptchaVO> response = imageCaptchaApplication.generateCaptcha(type);
		return response;
	}
	
	@ResponseBody
	@PostMapping("/check")
	public cloud.tianai.captcha.common.response.ApiResponse<?> checkCaptcha(@RequestBody Data data,
			HttpServletRequest request)
	{
		cloud.tianai.captcha.common.response.ApiResponse<?> response = imageCaptchaApplication.matching(data.getId(),
				data.getData());
		if (response.isSuccess())
		{
			return cloud.tianai.captcha.common.response.ApiResponse.ofSuccess(
					Collections.singletonMap("id", data.getId()));
		}
		return response;
	}

	@lombok.Data
	public static class Data
	{
		private String id;
		private ImageCaptchaTrack data;
	}
}
