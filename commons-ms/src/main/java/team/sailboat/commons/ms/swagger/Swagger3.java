package team.sailboat.commons.ms.swagger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import team.sailboat.commons.fan.app.App;

@Configuration
public class Swagger3
{
	
	final Logger mLogger = LoggerFactory.getLogger(getClass()) ;
	
	@Bean
	public OpenAPI createResetApi()
	{
		mLogger.info("swagger3 created");
		App app = App.instance() ;
		return new OpenAPI()
				.info(new Info()
						.title(app.getName() + " API文档")
						.description(app.getDescription())
						.version(app.getVersion())
						)
//				.externalDocs(new ExternalDocumentation()
//						.description("AAAAA")
//						.url("/"))
				;
	}
	
}
