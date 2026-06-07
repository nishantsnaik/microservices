# Spring Cloud Config Implementation

This document covers the implementation of Spring Cloud Config server and client configuration management in our microservices architecture.

## Architecture Overview

### Project Structure
```
v2-spring-cloud-config/
├── configserver/           # Spring Cloud Config Server
├── accounts/              # Accounts Microservice (Config Client)
├── cards/                 # Cards Microservice (Config Client)
└── loans/                 # Loans Microservice (Config Client)
```

### Configuration Flow
```
Config Server (Port 8071)
    ↓
Centralized Configuration Store
    ↓
Microservice Clients (Accounts, Cards, Loans)
```

## Spring Cloud Config Server

### Setup Configuration

#### Main Application Class
```java
@SpringBootApplication
@EnableConfigServer
public class ConfigserverApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigserverApplication.class, args);
    }
}
```

#### Server Configuration (application.yml)
```yaml
spring:
  application:
    name: "configserver"
  profiles:
    active: native
  cloud:
    config:
      server:
        native:
          search-locations: classpath:/config

server:
  port: 8071
```

### Configuration Files Structure

#### Service-Specific Configurations
- `accounts.yml` - Default accounts configuration
- `accounts_prod.yml` - Production accounts configuration  
- `accounts_qa.yml` - QA accounts configuration

- `cards.yml` - Default cards configuration
- `cards_prod.yml` - Production cards configuration
- `cards_qa.yml` - QA cards configuration

- `loans.yml` - Default loans configuration
- `loans_prod.yml` - Production loans configuration
- `loans_qa.yml` - QA loans configuration

#### Example Configuration (accounts.yml)
```yaml
build:
  version: "3.0"

accounts:
  message: "Welcome to VyberCoders accounts related local APIs"
  contactDetails:
    name: "John Doe - Developer"
    email: "john@VyberCoders.com"
  onCallSupport:
    - (555) 555-1234
    - (555) 523-1345
```

#### Environment-Specific Configuration (accounts_prod.yml)
```yaml
build:
  version: "1.0"

accounts:
  message: "Welcome to VyberCoders accounts related prod APIs"
  contactDetails:
    name: "Reine Aishwarya - Product Owner"
    email: "aishwarya@VyberCoders.com"
  onCallSupport:
    - (453) 392-4829
    - (236) 203-0384
```

## Microservice Client Configuration

### Dependencies Added

#### Maven Dependencies
```xml
<properties>
    <spring-cloud.version>2025.1.1</spring-cloud.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-config</artifactId>
    </dependency>
</dependencies>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${spring-cloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### Client Configuration (application.yml)

#### Accounts Service
```yaml
server:
  port: 8081

spring:
  application:
    name: "accounts"
  profiles:
    active: "prod"
  config:
    import: "optional:configserver:http://localhost:8071"
  datasource:
    url: jdbc:h2:mem:testdb
    driverClassName: org.h2.Driver
    username: sa
    password: ''
  h2:
    console:
      enabled: true
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: update
      show-sql: true
```

#### Cards Service
```yaml
server:
  port: 9000

spring:
  application:
    name: "cards"
  profiles:
    active: "prod"
  config:
    import: "optional:configserver:http://localhost:8071"
  datasource:
    url: jdbc:h2:mem:testdb
    driverClassName: org.h2.Driver
    username: sa
    password: ''
  h2:
    console:
      enabled: true
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: update
      show-sql: true
```

#### Loans Service
```yaml
server:
  port: 8090

spring:
  application:
    name: "loans"
  profiles:
    active: "prod"
  config:
    import: "optional:configserver:http://localhost:8071"
  datasource:
    url: jdbc:h2:mem:testdb
    driverClassName: org.h2.Driver
    username: sa
    password: ''
  h2:
    console:
      enabled: true
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: update
      show-sql: true
```

## Configuration Loading Strategy

### Priority Order
1. **Local application.yml** - Service-specific configuration
2. **Config Server** - Centralized configuration
3. **Environment-specific configs** - Profile-based overrides

### Configuration Resolution
```
Service Name + Profile = Configuration File
Example:
- accounts + prod = accounts_prod.yml
- cards + qa = cards_qa.yml
- loans + default = loans.yml
```

## Configuration Management Benefits

### 1. Centralized Configuration
- Single source of truth for all microservices
- Consistent configuration across environments
- Easy to manage and update configurations

### 2. Environment-Specific Overrides
- Separate configurations for dev/qa/prod
- Profile-based configuration loading
- Dynamic configuration updates

### 3. Configuration Hot Reload
- Changes in config server are automatically reflected
- No need to restart services for configuration changes
- Real-time configuration updates

### 4. Configuration Security
- Sensitive data can be encrypted
- Access control to configuration server
- Audit trail for configuration changes

## Startup Sequence

### 1. Config Server Startup
```bash
cd configserver
mvn spring-boot:run
```
- Starts on port 8071
- Loads configuration files from classpath:/config
- Exposes configuration endpoints

### 2. Microservice Startup
```bash
cd accounts
mvn spring-boot:run
```
- Connects to config server at localhost:8071
- Loads configuration based on service name and profile
- Fails gracefully if config server is unavailable (optional: prefix)

## Configuration Endpoints

### Config Server Endpoints
- `http://localhost:8071/accounts/default` - Default accounts config
- `http://localhost:8071/accounts/prod` - Production accounts config
- `http://localhost:8071/cards/qa` - QA cards config
- `http://localhost:8071/loans/prod` - Production loans config

## Best Practices

### 1. Configuration Organization
- Keep common configurations in base files
- Use profiles for environment-specific settings
- Group related properties under prefixes

### 2. Configuration Security
- Never store sensitive data in plain text
- Use encryption for passwords and API keys
- Implement proper access controls

### 3. Configuration Validation
- Use @ConfigurationProperties with validation
- Implement configuration health checks
- Test configuration loading in all environments

### 4. Configuration Versioning
- Version configuration files
- Maintain backward compatibility
- Document configuration changes

## Migration from Local Configuration

### Before (v1-springboot)
- Configuration files in each service
- Profile-specific files in service resources
- Local configuration management

### After (v2-spring-cloud-config)
- Centralized configuration server
- Service-specific configurations in config server
- Dynamic configuration management

## Troubleshooting

### Common Issues
1. **Config Server Connection Failed**
   - Check config server is running on port 8071
   - Verify network connectivity
   - Check firewall settings

2. **Configuration Not Loading**
   - Verify service name matches configuration file name
   - Check profile activation
   - Review config server logs

3. **Configuration Priority Issues**
   - Understand configuration loading order
   - Check for local configuration overrides
   - Review bootstrap vs application configuration

### Debug Configuration
```yaml
logging:
  level:
    org.springframework.cloud.config: DEBUG
    org.springframework.boot.context.config: DEBUG
```

## Spring Cloud Bus Implementation

### Overview
Spring Cloud Bus provides a messaging layer for broadcasting configuration changes across all connected microservices using RabbitMQ.

### Architecture
```
Config Server
    ↓ (broadcasts changes via RabbitMQ)
Spring Cloud Bus (RabbitMQ)
    ↓ (pushes updates to all clients)
Microservice Clients (Accounts, Cards, Loans)
```

### Dependencies Added

#### Config Server Dependencies
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-bus-amqp</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config-monitor</artifactId>
</dependency>
```

#### Client Dependencies (Accounts, Cards, Loans)
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-bus-amqp</artifactId>
</dependency>
```

### Configuration Updates

#### Config Server (application.yml)
```yaml
spring:
  application:
    name: "configserver"
  profiles:
    active: git
  cloud:
    config:
      server:
        git:
          uri: https://github.com/nishantsnaik/vybercoders-config.git
          default-label: main
          timeout: 5
          clone-on-start: true
          force-pull: true
  rabbitmq:
    host: "localhost"
    port: 5672
    username: "guest"
    password: "guest"

management:
  endpoints:
    web:
      exposure:
        include: "*"

encrypt:
  key: "ef124b53bd7ad5d8af4702b7f2a3ce1c2d30fd06122705ab2a3ff23348d0e1f9"

server:
  port: 8071
```

#### Client Configuration (application.yml)
```yaml
spring:
  application:
    name: "accounts"  # or "cards", "loans"
  profiles:
    active: "prod"
  config:
    import: "optional:configserver:http://localhost:8071"
  rabbitmq:
    host: "localhost"
    port: 5672
    username: "guest"
    password: "guest"

management:
  endpoints:
    web:
      exposure:
        include: "*"
```

### Configuration Properties Updates

#### DTO Classes (Updated from Records to Classes)
```java
@ConfigurationProperties(prefix = "accounts")
@Getter @Setter
public class AccountsContactInfoDto {
    String message;
    Map<String, String> contactDetails;
    List<String> onCallSupport;
}
```

#### Enable Configuration Properties
```java
@SpringBootApplication
@EnableConfigurationProperties(value = {AccountsContactInfoDto.class})
@EnableJpaAuditing(auditorAwareRef = "auditAwareImpl")
public class AccountsApplication {
    public static void main(String[] args) {
        SpringApplication.run(AccountsApplication.class, args);
    }
}
```

### Endpoints Added

All services now expose:
- `/api/build-info` - Build version from config
- `/api/java-version` - Java version details
- `/api/contact-info` - Contact information from config
- `/actuator/busrefresh` - Trigger configuration refresh
- `/actuator/env` - Environment properties
- `/actuator/configprops` - Configuration properties

### Spring Cloud Bus Features

#### 1. Configuration Broadcasting
- Changes in config server automatically broadcast to all clients
- No need to restart services for configuration updates
- Real-time configuration synchronization

#### 2. Refresh Endpoint
```bash
# Refresh all services
curl -X POST http://localhost:8071/actuator/busrefresh

# Refresh specific service
curl -X POST http://localhost:8081/actuator/refresh
```

#### 3. Monitoring and Management
- All actuator endpoints exposed for monitoring
- Health checks for RabbitMQ connectivity
- Configuration change tracking

### RabbitMQ Setup

#### Docker Configuration
```bash
# Run RabbitMQ with port mapping
docker run -d --name some-rabbit \
  -p 5672:5672 -p 15672:15672 -p 25672:25672 \
  rabbitmq:3
```

#### Connection Details
- **Host**: localhost
- **Port**: 5672
- **Username**: guest
- **Password**: guest
- **Management UI**: http://localhost:15672

### Configuration Refresh Workflow

#### Manual Refresh
1. Update configuration in Git repository
2. Trigger refresh: `curl -X POST http://localhost:8071/actuator/busrefresh`
3. All connected services receive updated configuration
4. Services reload @ConfigurationProperties beans

#### Automatic Refresh (with Webhooks)
1. Git webhook triggers config server refresh
2. Config server broadcasts changes via RabbitMQ
3. All connected services automatically update

### Benefits of Spring Cloud Bus

#### 1. Centralized Configuration Management
- Single source of truth in Git
- Automatic distribution to all services
- Version-controlled configuration history

#### 2. Dynamic Configuration Updates
- No service restarts required
- Real-time configuration changes
- Zero-downtime configuration updates

#### 3. Monitoring and Observability
- Configuration change tracking
- Health monitoring
- Comprehensive actuator endpoints

#### 4. Scalability
- Supports unlimited number of services
- Efficient message broadcasting
- Fault-tolerant message delivery

### Troubleshooting Spring Cloud Bus

#### Common Issues

1. **RabbitMQ Connection Failed**
   ```bash
   # Check RabbitMQ status
   docker ps | grep rabbitmq
   
   # Test connectivity
   telnet localhost 5672
   ```

2. **Configuration Not Refreshing**
   ```bash
   # Check bus refresh endpoint
   curl -X POST http://localhost:8071/actuator/busrefresh
   
   # Verify service logs for refresh events
   ```

3. **@ConfigurationProperties Not Updating**
   - Ensure @RefreshScope annotation on beans
   - Check that beans are not final
   - Verify proper dependency injection

#### Debug Configuration
```yaml
logging:
  level:
    org.springframework.cloud.bus: DEBUG
    org.springframework.amqp: DEBUG
    org.springframework.cloud.config: DEBUG
```

### Best Practices

#### 1. Configuration Organization
- Keep configurations in Git repository
- Use meaningful branch names for environments
- Document configuration changes

#### 2. Security Considerations
- Use encryption for sensitive data
- Secure RabbitMQ connections in production
- Implement proper access controls

#### 3. Performance Optimization
- Use @RefreshScope judiciously
- Monitor RabbitMQ queue sizes
- Implement proper error handling

#### 4. Production Readiness
- Set up proper monitoring
- Implement circuit breakers
- Plan for RabbitMQ high availability

This Spring Cloud Bus implementation provides a robust, scalable solution for dynamic configuration management across all microservices in the system.

## Containerization with Docker and JIB

### Container Image Configuration

All microservices have been containerized using Google JIB (Java Image Builder) for efficient, secure Docker image creation. The containerization strategy uses Amazon Corretto 25 as the base JVM for optimal performance and security.

#### JIB Maven Plugin Configuration
```xml
<plugin>
    <groupId>com.google.cloud.tools</groupId>
    <artifactId>jib-maven-plugin</artifactId>
    <version>3.5.1</version>
    <configuration>
        <from>
            <!-- Amazon Corretto 25 su Alpine è la scelta più affidabile per Java 25 -->
            <image>amazoncorretto:25</image>
        </from>
        <to>
            <image>nishantsnaik/${project.artifactId}:s2</image>
        </to>
    </configuration>
</plugin>
```

### Docker Compose Configuration

Multi-environment Docker Compose configurations have been implemented for different deployment stages:

#### Environment-Specific Configurations
- `docker-compose/default/` - Development environment
- `docker-compose/qa/` - QA/testing environment  
- `docker-compose/prod/` - Production environment

#### Production Docker Compose Configuration
```yaml
services:
  rabbit:
    image: rabbitmq:4-management
    hostname: rabbitmq
    ports:
      - "5672:5672"
      - "15672:15672"
    healthcheck:
      test: rabbitmq-diagnostics check_port_connectivity
      interval: 10s
      timeout: 10s
      retries: 10
      start_period: 5s

  configserver:
    image: "nishantsnaik/configserver:s2"
    container_name: configserver-ms
    ports:
      - "8071:8071"
    depends_on:
      rabbit:
        condition: service_healthy
    healthcheck:
      test: "curl --fail --silent localhost:8071/actuator/health/readiness | grep UP || exit 1"
      interval: 10s
      timeout: 10s
      retries: 10
      start_period: 10s

  accounts:
    image: "nishantsnaik/accounts:s2"
    container_name: accounts-ms
    ports:
      - "8081:8081"
    depends_on:
      configserver:
        condition: service_healthy
    environment:
      SPRING_APPLICATION_NAME: "accounts"
```

### Container Health Checks

#### Health Check Implementation
Each service implements health checks using Spring Boot Actuator endpoints:

- **Config Server**: `/actuator/health/readiness`
- **Microservices**: Dependency-based health checks
- **RabbitMQ**: Built-in diagnostics

#### Health Check Command
```bash
curl --fail --silent localhost:8071/actuator/health/readiness | grep UP || exit 1
```

### Container Startup Sequence

#### Dependency Management
Services are configured with proper dependency chains:
1. **RabbitMQ** starts first (no dependencies)
2. **Config Server** starts after RabbitMQ is healthy
3. **Microservices** (Accounts, Cards, Loans) start after Config Server is healthy

#### Service Dependencies
```yaml
depends_on:
  configserver:
    condition: service_healthy
```

### Container Network Configuration

#### Common Configuration (common-config.yml)
```yaml
services:
  network-deploy-service:
    networks:
      - vybercoders

  microservice-base-config:
    extends:
      service: network-deploy-service
    deploy:
      resources:
        limits:
          memory: 700m
    environment:
      SPRING_RABBITMQ_HOST: "rabbit"

  microservice-configserver-config:
    extends:
      service: microservice-base-config
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_CONFIG_IMPORT: configserver:http://configserver:8071/
```

### Container Deployment Commands

#### Build and Deploy
```bash
# Build all Docker images using JIB
mvn clean compile jib:build -DskipTests

# Deploy to production
docker-compose -f docker-compose/prod/docker-compose.yml up -d

# Check container status
docker-compose -f docker-compose/prod/docker-compose.yml ps

# View logs
docker-compose -f docker-compose/prod/docker-compose.yml logs -f configserver
```

#### Environment-Specific Deployment
```bash
# Development
docker-compose -f docker-compose/default/docker-compose.yml up -d

# QA
docker-compose -f docker-compose/qa/docker-compose.yml up -d

# Production
docker-compose -f docker-compose/prod/docker-compose.yml up -d
```

### Container Troubleshooting

#### Common Container Issues

1. **Health Check Failures**
   ```bash
   # Check container health
   docker ps
   
   # Inspect health check logs
   docker inspect configserver-ms | grep Health -A 10
   
   # Test health endpoint manually
   docker exec configserver-ms curl --fail --silent localhost:8071/actuator/health/readiness
   ```

2. **Dependency Issues**
   ```bash
   # Check dependency status
   docker-compose -f docker-compose/prod/docker-compose.yml ps
   
   # Restart specific service
   docker-compose -f docker-compose/prod/docker-compose.yml restart configserver
   ```

3. **Network Connectivity**
   ```bash
   # Test service connectivity
   docker exec accounts-ms curl http://configserver:8071/accounts/prod
   
   # Check network configuration
   docker network ls
   docker network inspect microservices_vybercoders
   ```

### Container Benefits

#### 1. Consistency Across Environments
- Same container images run in all environments
- Eliminates "it works on my machine" issues
- Immutable infrastructure

#### 2. Resource Efficiency
- Optimized container layers with JIB
- Amazon Corretto 25 for better performance
- Memory limits enforced via Docker Compose

#### 3. Scalability
- Easy horizontal scaling with Docker Swarm/Kubernetes
- Service discovery through container networking
- Load balancer integration ready

#### 4. Operational Excellence
- Health checks for automatic failover
- Dependency management for proper startup order
- Centralized logging and monitoring

### Container Security Considerations

#### 1. Base Image Security
- Amazon Corretto 25 provides regular security updates
- Minimal attack surface with Alpine-based images
- No package managers in final containers

#### 2. Runtime Security
- Non-root user execution where possible
- Resource limits to prevent DoS attacks
- Network isolation between services

#### 3. Configuration Security
- Sensitive data injected via environment variables
- No hardcoded credentials in images
- Encrypted configuration support via Spring Cloud Config

This containerization strategy provides a production-ready, scalable deployment solution for the Spring Cloud microservices architecture.