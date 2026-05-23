# Changes Summary - May 23, 2026

## Overview
This document summarizes the changes made to the microservices architecture on May 23, 2026, focusing on implementing circuit breakers, retries, rate limiting, and Redis integration for resilience patterns.

## Changes by Service

### 1. Gateway Server
**File: `gatewayserver/src/main/java/com/vybercoders/gatewayserver/GatewayserverApplication.java`**
- Added circuit breaker customizer with 4-second timeout
- Added Redis rate limiter bean (1 request per second)
- Added user key resolver for rate limiting (uses "user" header or "anonymous")
- Fixed loans route rewrite path from `/eazybank/loans/` to `/vybercoders/loans/`
- Added retry configuration for loans route (3 retries with exponential backoff)
- Added rate limiter to cards route
- Renamed route config method from `eazyBankRouteConfig` to `vyberCodersRouteConfig`

**File: `gatewayserver/src/main/java/com/vybercoders/gatewayserver/controller/FallbackController.java`**
- Created new fallback controller with `/contactSupport` endpoint
- Returns user-friendly error message when circuit breaker triggers

**File: `gatewayserver/src/main/java/com/vybercoders/gatewayserver/filters/ResponseTraceFilter.java`**
- Added check to prevent duplicate correlation ID headers in response
- Only adds correlation ID if not already present

**File: `gatewayserver/src/main/resources/application.yml`**
- Added Redis configuration (localhost:6379, 2s connect timeout, 1s timeout)
- Added HTTP client configuration (1s connect timeout, 2s response timeout)

**File: `gatewayserver/pom.xml`**
- Added `spring-boot-starter-data-redis-reactive` dependency

### 2. Accounts Service
**File: `accounts/src/main/java/com/vybercoders/accounts/controller/AccountsController.java`**
- Added `@Retry` annotation to `/build-info` endpoint with fallback method
- Added `@RateLimiter` annotation to `/java-version` endpoint with fallback method
- Added logger instance for debugging
- Implemented fallback methods that return default values when circuit breaker triggers

**File: `accounts/src/main/java/com/vybercoders/accounts/service/client/CardsFeignClient.java`**
- Added `fallback = CardsFallback.class` to `@FeignClient` annotation

**File: `accounts/src/main/java/com/vybercoders/accounts/service/client/LoansFeignClient.java`**
- Added `fallback = LoonsFallback.class` to `@FeignClient` annotation

**File: `accounts/src/main/java/com/vybercoders/accounts/service/client/CardsFallback.java`**
- Created new fallback class for Cards Feign client
- Returns null when cards service is unavailable

**File: `accounts/src/main/java/com/vybercoders/accounts/service/client/LoonsFallback.java`**
- Created new fallback class for Loans Feign client
- Returns null when loans service is unavailable

**File: `accounts/src/main/java/com/vybercoders/accounts/service/impl/CustomersServiceImpl.java`**
- Fixed null handling for circuit breaker fallbacks
- Added null checks before setting loans and cards DTOs
- Removed duplicate lines that caused NullPointerException when fallback returns null

**File: `accounts/src/main/resources/application.yml`**
- Added OpenFeign circuit breaker configuration (`enabled: true`)
- Added Resilience4j circuit breaker configuration (sliding window: 10, failure rate: 50%, wait duration: 10s)
- Added Resilience4j retry configuration (3 attempts, 500ms wait, exponential backoff)
- Added Resilience4j rate limiter configuration (1 request per 5 seconds)
- Added config server import for local development

**File: `accounts/pom.xml`**
- Added `spring-cloud-starter-circuitbreaker-resilience4j` dependency

### 3. Eureka Server
**File: `eurekaserver/src/main/resources/application.yml`**
- Commented out config server import for local development
- Added comment indicating it's only needed for local runs

### 4. Loans Service
**File: `loans/src/main/java/com/vybercoders/loans/controller/LoansController.java`**
- Added debug log statement to `/contact-info` endpoint

### 5. Docker Compose
**File: `docker-compose/default/docker-compose.yml`**
- Added Redis service with health check
- Added Redis dependency to gateway server
- Added Redis environment variables to gateway server (host, port, timeouts)
- Fixed Redis connect timeout from "redis" to "2s"

## Key Features Implemented

### Circuit Breaker Pattern
- Gateway level: Circuit breaker for accounts route with fallback to `/contactSupport`
- Service level: Circuit breakers for Feign clients (loans and cards) in accounts service
- Configured with 50% failure rate threshold and 10-second wait duration in open state

### Retry Pattern
- Gateway level: Retry configuration for loans route (3 retries with exponential backoff)
- Service level: Retry configuration in accounts service (3 attempts, 500ms wait, exponential backoff)
- Ignores NullPointerException, retries on TimeoutException

### Rate Limiting
- Gateway level: Redis-based rate limiting for cards route (1 request per second per user)
- Service level: Rate limiting in accounts service (1 request per 5 seconds)
- Uses "user" header for identification, falls back to "anonymous"

### Fallback Mechanisms
- Gateway: Fallback controller returns user-friendly error messages
- Service: Fallback classes for Feign clients return null when downstream services are unavailable
- Controller: Fallback methods return default values when circuit breaker triggers

### Redis Integration
- Added Redis service to docker-compose for distributed rate limiting
- Configured gateway server to use Redis for rate limiting
- Added Redis reactive dependency to gateway server

## Configuration Changes

### Timeout Configurations
- Gateway HTTP client: 1s connect timeout, 2s response timeout
- Gateway Redis: 2s connect timeout, 1s timeout
- Circuit breaker time limiter: 4s timeout

### Path Fixes
- Fixed gateway loans route rewrite path from `/eazybank/loans/` to `/vybercoders/loans/`
- This was causing 404 errors when accessing loans service through gateway

## Bug Fixes
1. **NullPointerException in CustomersServiceImpl**: Removed duplicate lines that attempted to call `.getBody()` on null ResponseEntity when fallback returned null
2. **Duplicate correlation ID headers**: Added check in ResponseTraceFilter to prevent adding duplicate correlation ID headers
3. **Port 6379 conflict**: Killed local Redis process to allow docker-compose Redis to start

## Testing Recommendations
1. Test circuit breaker by stopping loans/cards services and verifying fallback behavior
2. Test retry mechanism by temporarily making loans service unavailable
3. Test rate limiting by sending multiple requests rapidly
4. Verify gateway routes work correctly after path fix
5. Test Redis rate limiting with different user headers

## Next Steps
- Consider adding circuit breakers to loans and cards routes in gateway
- Add monitoring and alerting for circuit breaker state changes
- Consider implementing bulkhead pattern for resource isolation
- Add integration tests for fallback scenarios
