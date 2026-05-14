# Spring Boot Configuration Management

This document covers the three main approaches to manage configuration in Spring Boot applications, with examples from our microservices project.

## 1. @Value Annotation

**Purpose**: Inject single property values directly into fields

### Usage Example
```java
@RestController
public class AccountsController {
    
    @Value("${build.version}")
    private String buildVersion;
    
    @Value("${JAVA_HOME}")
    private String javaHome;
    
    @GetMapping("/build-info")
    public ResponseEntity<String> getBuildInfo() {
        return ResponseEntity.status(HttpStatus.OK).body(buildVersion);
    }
}
```

### Properties Files
```yaml
# application.yml
build:
  version: "3.0"

# application_qa.yml
build:
  version: "2.0"

# application_prod.yml
build:
  version: "1.0"
```

## 2. @ConfigurationProperties

**Purpose**: Type-safe binding of configuration properties to POJOs/Records

### Usage Example
```java
@ConfigurationProperties(prefix = "accounts")
public record AccountsContactInfoDto(
    String message, 
    Map<String, String> contactDetails, 
    List<String> onCallSupport
) {
}

@RestController
public class AccountsController {
    
    @Autowired
    private AccountsContactInfoDto accountsContactInfoDto;
    
    @GetMapping("/contact-info")
    public ResponseEntity<AccountsContactInfoDto> getContactInfo() {
        return ResponseEntity.status(HttpStatus.OK).body(accountsContactInfoDto);
    }
}
```

### Properties Files
```yaml
# application.yml
accounts:
  message: "Welcome to VyberCoders accounts related local APIs"
  contactDetails:
    name: "John Doe - Developer"
    email: "john@VyberCoders.com"
  onCallSupport:
    - (555) 555-1234
    - (555) 523-1345

# application_qa.yml
accounts:
  message: "Welcome to VyberCoders accounts related QA APIs"
  contactDetails:
    name: "Smitha Ray - QA Lead"
    email: "smitha@VyberCoders.com"
  onCallSupport:
    - (666) 265-3765
    - (666) 734-8371

# application_prod.yml
accounts:
  message: "Welcome to VyberCoders accounts related prod APIs"
  contactDetails:
    name: "Reine Aishwarya - Product Owner"
    email: "aishwarya@VyberCoders.com"
  onCallSupport:
    - (453) 392-4829
    - (236) 203-0384
```

### Enable Configuration Properties
```java
@SpringBootApplication
@EnableConfigurationProperties(value = {AccountsContactInfoDto.class})
public class AccountsApplication {
    public static void main(String[] args) {
        SpringApplication.run(AccountsApplication.class, args);
    }
}
```

## 3. Profile-Based Configuration

**Purpose**: Environment-specific configuration management

### Profile Activation in application.yml
```yaml
spring:
  config:
    import:
      - "application_qa.yml"
      - "application_prod.yml"
  profiles:
    active:
      - "qa"  # Can be "qa", "prod", or default
```

### Profile-Specific Files
- `application.yml` - Default configuration
- `application_qa.yml` - QA environment
- `application_prod.yml` - Production environment

### Profile Activation Methods

#### 1. In application.yml
```yaml
spring:
  profiles:
    active:
      - "qa"
```

#### 2. Command Line Arguments
```bash
java -jar accounts.jar --spring.profiles.active=prod
```

#### 3. Environment Variables
```bash
export SPRING_PROFILES_ACTIVE=prod
java -jar accounts.jar
```

#### 4. JVM System Properties
```bash
java -Dspring.profiles.active=prod -jar accounts.jar
```

## External Configuration Sources

Spring Boot loads configuration in the following order (highest priority first):

### 1. Command Line Arguments
```bash
java -jar accounts.jar --server.port=9090 --build.version=custom
```

### 2. Environment Variables
```bash
export SERVER_PORT=9090
export BUILD_VERSION=custom
java -jar accounts.jar
```

### 3. JVM System Properties
```bash
java -Dserver.port=9090 -Dbuild.version=custom -jar accounts.jar
```

## Configuration Priority Order

**Highest Priority → Lowest Priority:**

1. **Command Line Arguments** (`--server.port=9090`)
2. **Environment Variables** (`SERVER_PORT=9090`)
3. **JVM System Properties** (`-Dserver.port=9090`)
4. **Profile-specific properties** (`application-prod.yml`)
5. **Default properties** (`application.yml`)

## Best Practices

### 1. Use @Value for Simple Properties
- Single values
- Environment-specific settings
- System properties

### 2. Use @ConfigurationProperties for Complex Objects
- Nested configurations
- Type safety
- Validation support

### 3. Use Profiles for Environments
- Separate configs for dev/qa/prod
- Feature toggles
- Environment-specific beans

### 4. Externalize Sensitive Data
```yaml
# Never commit sensitive data
spring:
  datasource:
    password: ${DB_PASSWORD}
```

### 5. Use Environment Variables for Deployment
```yaml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:default}
```

## Implementation in Our Project

### Services Configuration
- **Accounts Service**: Port 8081, Profile: "prod"
- **Cards Service**: Port 9000, Profile: "qa"  
- **Loans Service**: Port 8090, Profile: "default"

### Common Endpoints
All services expose:
- `/api/build-info` - Build version information
- `/api/java-version` - Java version details
- `/api/contact-info` - Service contact information

### Configuration Architecture
```
src/main/resources/
├── application.yml           # Default configuration
├── application_qa.yml       # QA environment
├── application_prod.yml      # Production environment
└── application_dev.yml       # Development environment (if needed)
```

## Advanced Configuration

### Property Validation
```java
@ConfigurationProperties(prefix = "app")
@Validated
public record AppProperties(
    @NotBlank String name,
    @Min(1) @Max(65535) int port
) {
}
```

### Conditional Configuration
```java
@Configuration
@Profile("prod")
public class ProductionConfig {
    @Bean
    public DataSource prodDataSource() {
        // Production datasource configuration
    }
}
```

### Configuration Encryption
```yaml
jasypt:
  encryptor:
    password: ${JASYPT_PASSWORD}
    
spring:
  datasource:
    password: ENC(encrypted_password_here)
```

## Summary

| Approach | Use Case | Example |
|----------|----------|---------|
| @Value | Simple properties, system values | `@Value("${build.version}")` |
| @ConfigurationProperties | Complex objects, type safety | `@ConfigurationProperties(prefix = "accounts")` |
| Profiles | Environment-specific configs | `spring.profiles.active: qa` |
| External Sources | Deployment flexibility | Environment variables, command line args |

This configuration management approach ensures our microservices are flexible, maintainable, and deployment-ready across different environments.
