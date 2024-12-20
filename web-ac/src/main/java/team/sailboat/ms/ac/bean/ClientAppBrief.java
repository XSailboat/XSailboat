package team.sailboat.ms.ac.bean;

import java.util.Comparator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import team.sailboat.ms.ac.dbean.ClientApp;

/**
 * 
 * ClientApp的简要信息
 *
 * @author yyl
 * @since 2024年11月5日
 */
@Data
@Schema(description = "ClientApp的简要信息")
public class ClientAppBrief
{
	/**
	 * 缺省的排序器
	 */
	public static Comparator<ClientAppBrief> sDefaultComp = (c1 , c2)->{
		if(c1.isWebApp())
		{
			if(c2.isWebApp())
				return c1.getName().compareTo(c2.getName()) ;
			else
				return -1 ;
		}
		else
		{
			if(c2.isWebApp())
				return 1 ;
			else
				return c1.getName().compareTo(c2.getName()) ;
		}
	} ;
	
	@Schema(description = "ClientApp的id")
	String id;
	
	@Schema(description = "应用名")
	String name;
	
	@Schema(description = "应用简写代号")
	String simpleName;
	
	@Schema(description = "厂家")
	String company;
	
	@Schema(description = "应用描述")
	String description;
	
	@Schema(description = "是否可用")
	boolean enabled ;
	
	@Schema(description = "主页地址")
	String homePageUrl ;
	
	@Schema(description = "AppKey")
	String appKey;
	
	@Schema(description = "是否是WebApp，true表示是WebApp，false表示是中台服务")
	boolean webApp ;

	public static ClientAppBrief of(ClientApp aApp)
	{
		ClientAppBrief brief = new ClientAppBrief() ;
		brief.id = aApp.getId();
		brief.name = aApp.getName();
		brief.simpleName = aApp.getSimpleName();
		brief.company = aApp.getCompany();
		brief.description = aApp.getDescription();
		brief.enabled = aApp.isEnabled();
		brief.homePageUrl = aApp.getHomePageUrl();
		brief.appKey = aApp.getAppKey() ;
		brief.webApp = ClientApp.isWebApp(aApp) ;
		return brief ;
	}
}
