package team.sailboat.ms.ac.data;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 
 * 登录应用的记录
 *
 * @author yyl
 * @since 2024年11月20日
 */
@Data
@AllArgsConstructor
public class LoginAppRecord
{
	/**
	 * 应用id
	 */
	String appId ;
	
	/**
	 * 登陆时间
	 */
	Date loginTime ;
	
	/**
	 * 过期时间
	 */
	Date expiredTime ;
	
	String oAuth2AuthorizationId ;
	
	public boolean isExpired()
	{
		return expiredTime.before(new Date()) ;
	}
}
