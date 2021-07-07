package com.tecnalia.datausage;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;



@SpringBootApplication

@ComponentScan(basePackages = { "com.tecnalia.datausage", "com.tecnalia.datausage.api" , "com.tecnalia.datausage.configuration", 
    "com.tecnalia.datausage.utils", "de.fraunhofer.isst.dataspaceconnector.services.usagecontrol", "io.dataspaceconnector.services.usagecontrol"})
public class Swagger2SpringBoot {



    public static void main(String[] args) throws Exception {
        new SpringApplication(Swagger2SpringBoot.class).run(args);
    }

     @Bean
    public OpenAPI openApi(@Value("${server.servlet.context-path}") String contextPath,
			@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri) {
        return new OpenAPI()
            .info(new Info()
                .title("Platoon Data Usage")
                .description("Api Documentation")
                .termsOfService("")
                .version("1.0")
                .license(new License()
                    .name("Apache 2.0")
                    .url("http://www.apache.org/licenses/LICENSE-2.0"))
                .contact(new io.swagger.v3.oas.models.info.Contact()
                    .email("")))    
                .addServersItem(new Server().url(contextPath))
				.components(new Components()
						.addSecuritySchemes("openid-connect",
								new SecurityScheme().type(SecurityScheme.Type.OPENIDCONNECT)
										.openIdConnectUrl(issuerUri + "/.well-known/openid-configuration"))
						.addSecuritySchemes("bearer-jwt",
								new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer")
										.bearerFormat("JWT")))
				.addSecurityItem(new SecurityRequirement().addList("bearer-jwt", Arrays.asList("read", "write")))
				.addSecurityItem(new SecurityRequirement().addList("openid-connect", Arrays.asList("read", "write")));
	}

            
    
    
    
}
