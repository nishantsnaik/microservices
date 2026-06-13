# Spring Cloud Function Implementation Guide

This document explains the Spring Cloud Function and Spring Cloud Stream implementation in the v6-event-driven microservices architecture.

## Overview

Spring Cloud Function enables the implementation of business logic as functions that can be exposed as REST endpoints, message handlers, or stream processors. Combined with Spring Cloud Stream, it provides a powerful event-driven architecture with message brokers like RabbitMQ.

## Architecture

The implementation uses a producer-consumer pattern with RabbitMQ as the message broker:

```
Accounts Service (Producer) → RabbitMQ → Message Service (Consumer)
```

### Message Flow

1. **Accounts Service** publishes account details to `send-communication` queue
2. **Message Service** consumes from `send-communication` queue
3. **Message Service** processes email and SMS functions
4. **Message Service** publishes result to `communication-sent` queue
5. **Accounts Service** consumes from `communication-sent` queue to update communication status

## Implementation Details

### 1. Dependencies

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-stream</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-stream-binder-rabbit</artifactId>
</dependency>
```

### 2. Configuration

Configure Spring Cloud Function and Stream in `application.yml`:

```yaml
spring:
  cloud:
    function:
      definition: updateCommunication  # Function bean name
    stream:
      bindings:
        updateCommunication-in-0:
          destination: communication-sent  # Queue to consume from
          group: ${spring.application.name}
        sendCommunication-out-0:
          destination: send-communication  # Queue to publish to
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

### 3. Function Types

#### Consumer Function (Accounts Service)

Consumes messages from a queue:

```java
@Configuration
public class AccountsFunctions {
    
    @Bean
    public Consumer<Long> updateCommunication(IAccountsService accountsService) {
        return accountNumber -> {
            log.info("Updating Communication status for account: " + accountNumber);
            accountsService.updateCommunicationStatus(accountNumber);
        };
    }
}
```

#### Function Composition (Message Service)

Chain multiple functions using the `|` operator:

```yaml
spring:
  cloud:
    function:
      definition: email|sms  # Chains email and sms functions
```

```java
@Configuration
public class MessageFunctions {
    
    @Bean
    public Function<AccountsMsgDto, AccountsMsgDto> email() {
        return accountsMsgDto -> {
            log.info("Sending email with details: " + accountsMsgDto);
            return accountsMsgDto;  // Pass to next function in chain
        };
    }
    
    @Bean
    public Function<AccountsMsgDto, Long> sms() {
        return accountsMsgDto -> {
            log.info("Sending SMS with details: " + accountsMsgDto);
            return accountsMsgDto.accountNumber();  // Final output
        };
    }
}
```

#### Supplier Function

Produces messages to a queue:

```java
@Bean
public Supplier<AccountsMsgDto> sendCommunication() {
    return () -> {
        // Generate and return message
        return new AccountsMsgDto(...);
    };
}
```

### 4. Publishing Messages

Use `StreamBridge` to publish messages programmatically:

```java
@Service
public class AccountsServiceImpl implements IAccountsService {
    
    @Autowired
    private StreamBridge streamBridge;
    
    @Override
    public void sendCommunication(AccountsMsgDto accountsMsgDto) {
        Boolean result = streamBridge.send(
            "sendCommunication-out-0",  // Binding name
            accountsMsgDto
        );
        log.info("Communication sent: " + result);
    }
}
```

### 5. Data Transfer Objects

Create DTOs for message payloads:

```java
public record AccountsMsgDto(
    Long accountNumber,
    String name,
    String email,
    String mobileNumber
) {}
```

## Binding Naming Convention

Spring Cloud Stream uses the following naming pattern for bindings:

- `{functionName}-in-0` - Input binding (consumer)
- `{functionName}-out-0` - Output binding (supplier/function)

For example:
- `updateCommunication-in-0` - Input for updateCommunication function
- `sendCommunication-out-0` - Output for sendCommunication supplier

## Function Composition

You can compose functions using the `|` operator in the function definition:

```yaml
spring:
  cloud:
    function:
      definition: email|sms|log  # Execute in sequence
```

The output of one function becomes the input of the next.

## RabbitMQ Configuration

Ensure RabbitMQ is running and accessible:

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    connection-timeout: 10s
```

For Docker deployment, use the RabbitMQ service from docker-compose:

```yaml
rabbit:
  image: rabbitmq:3.12-management
  ports:
    - "5672:5672"
    - "15672:15672"
```

## Testing

### Unit Testing with Test Binder

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-stream-test-binder</artifactId>
    <scope>test</scope>
</dependency>
```

### Integration Testing

Use `@SpringBootTest` with the actual binder or test binder to verify function behavior.

## Benefits

1. **Decoupling**: Services communicate asynchronously without direct dependencies
2. **Scalability**: Each service can scale independently based on load
3. **Flexibility**: Functions can be exposed as REST endpoints, message handlers, or stream processors
4. **Reusability**: Business logic can be reused across different contexts
5. **Testability**: Functions are easy to unit test in isolation

## Best Practices

1. Keep functions stateless and idempotent
2. Use appropriate DTOs for message payloads
3. Handle errors gracefully with DLQ (Dead Letter Queue)
4. Monitor message throughput and latency
5. Use consumer groups for load balancing
6. Implement proper logging for debugging message flows

## Troubleshooting

### Messages not being consumed
- Check queue names match between producer and consumer
- Verify RabbitMQ connection settings
- Ensure consumer group is configured correctly

### Function not being invoked
- Verify function definition in application.yml
- Check bean name matches the function definition
- Ensure proper binding configuration

### Serialization issues
- Ensure DTO classes are consistent across services
- Verify Jackson serialization/deserialization configuration
- Check for version mismatches in DTOs

## References

- [Spring Cloud Function Documentation](https://spring.io/projects/spring-cloud-function)
- [Spring Cloud Stream Documentation](https://spring.io/projects/spring-cloud-stream)
- [RabbitMQ Binder Documentation](https://docs.spring.io/spring-cloud-stream/reference/rabbit/RabbitMQ.html)
