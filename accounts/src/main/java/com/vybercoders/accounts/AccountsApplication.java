package com.vybercoders.accounts;

import com.vybercoders.accounts.dto.AccountsContactInfoDto;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
/*@ComponentScans({ @ComponentScan("com.vybercoders.accounts.controller") })
@EnableJpaRepositories("com.vybercoders.accounts.repository")
@EntityScan("com.vybercoders.accounts.model")*/
@EnableConfigurationProperties(value = {AccountsContactInfoDto.class})
@EnableJpaAuditing(auditorAwareRef = "auditAwareImpl")
@OpenAPIDefinition(
        info = @Info(
                title = "Accounts microservice REST API Documentation",
                description = "Vybercoders Accounts microservice REST API Documentation",
                version = "v1",
                contact = @Contact(
                        name = "Nishant Naik",
                        email = "tutor@vybercoders.com",
                        url = "https://www.vybercoders.com"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.vybercoders.com"
                )
        ),
        externalDocs = @ExternalDocumentation(
                description =  "Vybercoders Accounts microservice REST API Documentation",
                url = "https://www.vybercoders.com/swagger-ui.html"
        )
)
public class AccountsApplication {

	public static void main(String[] args) {
		SpringApplication.run(AccountsApplication.class, args);
	}

}
