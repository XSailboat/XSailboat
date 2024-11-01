package team.sailboat.commons.ms.crypto;

import java.security.NoSuchAlgorithmException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RSAKeyPairMaker4JSConfigurer
{

	@Bean
	RSAKeyPairMaker4JS _rsaMaker() throws NoSuchAlgorithmException
	{
		return RSAKeyPairMaker4JS.getDefault() ;
	}
}
