package com.vybercoders.cards;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
/*@ComponentScans({ @ComponentScan("com.vybercoders.cards.controller") })
@EnableJpaRepositories("com.vybercoders.cards.repository")
@EntityScan("com.vybercoders.cards.model")*/
@EnableJpaAuditing(auditorAwareRef = "auditAwareImpl")
@OpenAPIDefinition(
		info = @Info(
				title = "Cards microservice REST API Documentation",
				description = "Vybercoders Cards microservice REST API Documentation",
				version = "v1",
				contact = @Contact(
						name = "Madan Reddy",
						email = "tutor@vybercoders.com",
						url = "https://www.vybercoders.com"
				),
				license = @License(
						name = "Apache 2.0",
						url = "https://www.vybercoders.com"
				)
		),
		externalDocs = @ExternalDocumentation(
				description = "Vybercoders Cards microservice REST API Documentation",
				url = "https://www.vybercoders.com/swagger-ui.html"
		)
)
public class CardsApplication {

	public static void main(String[] args) {
		SpringApplication.run(CardsApplication.class, args);
	}
}
