package team.sailboat.ms.ac.dbean;

import java.util.Collection;
import java.util.Date;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import jakarta.validation.constraints.NotBlank;
import team.sailboat.commons.fan.dpa.anno.BColumn;
import team.sailboat.commons.fan.dpa.anno.BDataType;
import team.sailboat.commons.fan.dpa.anno.BFeature;
import team.sailboat.commons.fan.dpa.anno.BTable;
import team.sailboat.commons.fan.dtool.DBType;
import team.sailboat.commons.fan.dtool.mysql.MySQLFeatures;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.ms.bean.UserBrief;
import team.sailboat.dplug.anno.DBean;
import team.sailboat.ms.ac.server.IUserAuthsProviderInAuthCenter;

/**
 * 用户信息表
 *
 * @author yyl
 * @since 2024年10月30日
 */
@BTable(name="ac_user" , comment="用户"  , features = {
		@BFeature(type = DBType.MySQL , name = MySQLFeatures.TABLE__ENGINE , value = "InnoDB") , 
		@BFeature(type = DBType.MySQL , name = MySQLFeatures.TABLE__CHARACTER_SET , value = "utf8")} ,
	id_category = "User" , id_prefix = "us"
)
@DBean(genBean = true , recordCreate = true , recordEdit = true)
public class User implements UserDetails
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@BColumn(name="id" , dataType = @BDataType(name="string" , length = 32) , comment="唯一性标识" , seq = 0 , primary = true)
	String id ;
	
	@NotBlank
	@BColumn(name="username" , dataType = @BDataType(name="string" , length = 64) , comment="用户名" , seq = 1)
	String username ;

	@JsonProperty(access = Access.WRITE_ONLY)		// 序列化成JSON对象时，不要输出password
	@BColumn(name="password" , dataType = @BDataType(name="string" , length = 128) , comment="密码" , seq = 2)
	String password ;
	
	@BColumn(name="real_name" , dataType = @BDataType(name="string" , length = 32) , comment="真实姓名" , seq = 3)
	String realName ;

	@BColumn(name="sex" , dataType = @BDataType(name="string" , length = 16) , comment="性别。可取值：男、女" , seq = 4)
	String sex ;
	
	@BColumn(name="mobile" , dataType = @BDataType(name="string" , length = 32) , comment="手机号" , seq = 5)
	String mobile ;
	
	@BColumn(name="email" , dataType = @BDataType(name="string" , length = 64) , comment="电子邮件" , seq = 6)
	String email ;

	@BColumn(name="department" , dataType = @BDataType(name="string" , length = 64) , comment="所属部门名称。显示用，和组织单元名未必相等" , seq = 7)
	String department ;
	
	@BColumn(name="ding_open_id" , dataType = @BDataType(name="string" , length = 64) , comment="定用户OpenID" , seq = 10)
	String dingOpenId ;

	@BColumn(name="ext_attributes" , dataType = @BDataType(name="string" , length = 2048) , comment="附加信息" , seq = 12)
	String extAttributes ;
	
	@BColumn(name="account_expired_time" , dataType = @BDataType(name="datetime") , comment="账户过期时间，有效期至" , seq = 14)
	Date accountExpiredTime ;
	
	@BColumn(name="locked" , dataType = @BDataType(name="bool") , comment="账户是否被锁。0表示没被锁，1表示被锁" , seq = 16
			, defaultValue = "false")
	Boolean locked ;
	
	@BColumn(name="credentials_expired_time" , dataType = @BDataType(name="datetime") , comment="密码或其它登陆凭据过期时间，有效期至" , seq = 18)
	Date credentialsExpiredTime ;
	
	@BColumn(name="enabled" , dataType = @BDataType(name="bool") , comment="账户是否可用" , seq = 20
			, defaultValue = "true")
	Boolean enabled = Boolean.TRUE ;
	
	JSONObject mExtAttribute_JSONObj ;
	
	IUserAuthsProviderInAuthCenter mUserAuthsProviderInAuthCenter ;
	
	public User()
	{
	}
	
	public void setUserAuthsProviderInAuthCenter(IUserAuthsProviderInAuthCenter aUserAuthsProviderInAuthCenter)
	{
		mUserAuthsProviderInAuthCenter = aUserAuthsProviderInAuthCenter;
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities()
	{
		Assert.notNull(mUserAuthsProviderInAuthCenter , "没有设置UserAuthsProviderInAuthCenter!") ;
		return mUserAuthsProviderInAuthCenter.getAuthoritysOfUserInClientApp(id) ;
	}

	/**
	 * 账户是否没过期
	 */
	@Override
	public boolean isAccountNonExpired()
	{
		return accountExpiredTime == null || accountExpiredTime.after(new Date()) ;
	}
	
	public boolean setExtAttributes(String aExtAttributes)
	{
		if(JCommon.unequals(extAttributes, aExtAttributes))
		{
			Object oldValue = extAttributes ;
			extAttributes = aExtAttributes;
			setChanged("extAttributes", extAttributes, oldValue);
			mExtAttribute_JSONObj = null ;
			return true ;
		}
		return false ;
	}
	public JSONObject getExtAttributes_JSONObject()
	{
		if(mExtAttribute_JSONObj == null)
		{
			mExtAttribute_JSONObj = JSONObject.of(extAttributes) ;
		}
		return mExtAttribute_JSONObj ;
	}

	/**
	 * 账户是否没有被锁
	 */
	@Override
	public boolean isAccountNonLocked()
	{
		return !Boolean.TRUE.equals(locked) ;
	}

	@Override
	public boolean isCredentialsNonExpired()
	{
		return credentialsExpiredTime == null || credentialsExpiredTime.after(new Date()) ;
	}

	@Override
	public boolean isEnabled()
	{
		return !Boolean.FALSE.equals(enabled) ;
	}
	
	
	public String getDisplayName()
	{
		return XString.isNotEmpty(department)?XString.splice(department , "-" , realName)
				: realName ;
	}
	
	public UserBrief toBrief()
	{
		return new UserBrief(id , realName , department) ;
	}

}
