# Authentication & Authorization Setup

## Keycloak Configuration

Keycloak is now integrated into the Docker Compose setup for all environments (default, qa, prod).

### Local Development
```bash
docker run -d -p 127.0.0.1:7080:8080 -e KC_BOOTSTRAP_ADMIN_USERNAME=admin -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:26.6.3 start-dev
```

### Docker Compose
Keycloak is included in the docker-compose files:
- **Port**: 7080 (mapped to container port 8080)
- **Admin Credentials**: admin/admin
- **Realm**: master
- **JWKS Endpoint**: http://localhost:7080/realms/master/protocol/openid-connect/certs

## Gateway Server Security Configuration

### Dependencies Added
- `spring-boot-starter-security`
- `spring-security-oauth2-resource-server`
- `spring-security-oauth2-jose`

### Security Configuration
- **JWT Authentication**: Configured with Keycloak as the OAuth2 Resource Server
- **Role-Based Authorization**: 
  - `/vybercoders/accounts/**` requires `ROLE_ACCOUNTS`
  - `/vybercoders/cards/**` requires `ROLE_CARDS`
  - `/vybercoders/loans/**` requires `ROLE_LOANS`
- **GET Requests**: Permitted without authentication for all endpoints
- **CSRF**: Disabled for API endpoints

### Keycloak Role Converter
Custom converter extracts roles from JWT token's `realm_access` claim and converts them to Spring Security `GrantedAuthority` with `ROLE_` prefix.

## Environment Variables

### Gateway Server
- `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK-SET-URI`: JWKS endpoint for Keycloak
  - Local: `http://localhost:7080/realms/master/protocol/openid-connect/certs`
  - Docker: `http://keycloak:8080/realms/master/protocol/openid-connect/certs`

## Observability Stack
Added comprehensive observability with:
- **Loki**: Log aggregation
- **Prometheus**: Metrics collection
- **Tempo**: Distributed tracing
- **Grafana**: Visualization dashboard
- **Alloy**: Agent for collecting telemetry data

## Changes Summary
- Added Spring Security with OAuth2 JWT authentication to gateway
- Integrated Keycloak for identity management
- Implemented role-based access control (RBAC)
- Added observability stack (Loki, Prometheus, Tempo, Grafana, Alloy)
- Updated all service image tags to s12
- Added OpenTelemetry service names to all services
- Removed Redis dependency from gateway (rate limiting moved to gateway level)
- Disabled config server import in eurekaserver for local development
