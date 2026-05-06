# Docker Image Generation Approaches

This document outlines three different approaches for generating Docker images for Spring Boot microservices, with examples from this project.

## Overview

This project demonstrates three popular methods for containerizing Java Spring Boot applications:

1. **Native Docker** - Traditional approach using Dockerfile
2. **Buildpacks** - Cloud-native build system
3. **Google Jib** - Java containerizer from Google

---

## 1. Native Docker Approach

### Description
The traditional method using a `Dockerfile` to define the container image build process. This approach gives you full control over the image layers and configuration.

### Example Dockerfile
```dockerfile
# Start with a base image containing Java runtime
FROM eclipse-temurin:25-jdk

# Information around who maintains the image
LABEL "org.opencontainers.image.authors"="vybercoders.com"

# Add the application's jar to the image
COPY target/accounts-0.0.1-SNAPSHOT.jar accounts-0.0.1-SNAPSHOT.jar

# Execute the application
ENTRYPOINT ["java", "-jar", "accounts-0.0.1-SNAPSHOT.jar"]
```

### Build Commands
```bash
# Build the Spring Boot application first
mvn clean package

# Build the Docker image
docker build -t nishantsnaik/accounts:latest .

# Run the container
docker run -p 8080:8080 nishantsnaik/accounts:latest
```

### Pros
- Full control over the build process
- Transparent and easy to understand
- Customizable for specific requirements
- Works with any build system

### Cons
- Requires Docker daemon to be running
- Manual optimization of image layers
- Larger image sizes if not optimized properly
- Requires writing and maintaining Dockerfile

---

## 2. Buildpacks Approach

### Description
Cloud Native Buildpacks transform your source code into OCI-compatible container images. Spring Boot integrates with buildpacks through the Maven plugin.

### Configuration
Add to your `pom.xml`:
```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <image>
            <name>nishantsnaik/${project.artifactId}:s2</name>
        </image>
    </configuration>
</plugin>
```

### Build Commands
```bash
# Build and create Docker image using buildpacks
mvn spring-boot:build-image

# Run the container
docker run -p 8080:8080 nishantsnaik/${project.artifactId}:s2
```

### Pros
- No Dockerfile required
- Optimized multi-layered images
- Automatic base image updates
- Security scanning and patching
- Reproducible builds
- Works without Docker daemon (for some platforms)

### Cons
- Less control over build process
- Requires internet connection during build
- Larger learning curve for customization
- Plugin dependency

---

## 3. Google Jib Approach

### Description
Google Jib is a Java containerizer that lets you build Docker and OCI images directly from Maven or Gradle without writing a Dockerfile or requiring Docker daemon.

### Configuration
Add to your `pom.xml`:
```xml
<plugin>
    <groupId>com.google.cloud.tools</groupId>
    <artifactId>jib-maven-plugin</artifactId>
    <version>3.5.1</version>
    <configuration>
        <to>
            <image>nishantsnaik/${project.artifactId}:s2</image>
        </to>
    </configuration>
</plugin>
```

### Build Commands
```bash
# Build and push to Docker registry
mvn compile jib:build

# Or build to local Docker daemon
mvn compile jib:dockerBuild

# Run the container
docker run -p 8080:8080 nishantsnaik/${project.artifactId}:s2
```

### Pros
- No Docker daemon required
- Fast and reproducible builds
- Optimized image layers (dependencies, resources, classes)
- Better caching and layer separation
- Works in CI/CD environments without Docker
- No need to maintain Dockerfile

### Cons
- Java-specific (only works for JVM applications)
- Less flexible than Dockerfile for complex scenarios
- Additional plugin dependency
- Learning curve for advanced configurations

---

## Comparison Summary

| Feature | Native Docker | Buildpacks | Google Jib |
|---------|---------------|------------|------------|
| **Dockerfile Required** | Yes | No | No |
| **Docker Daemon Required** | Yes | Yes | No |
| **Build Speed** | Medium | Medium | Fast |
| **Image Size** | Variable | Optimized | Optimized |
| **Layer Optimization** | Manual | Automatic | Automatic |
| **Customization** | Full | Medium | Limited |
| **Learning Curve** | Low | Medium | Medium |
| **CI/CD Friendly** | Yes | Yes | Excellent |
| **Language Support** | Any | Many | Java only |

---

## Recommendations

### Use Native Docker when:
- You need complete control over the build process
- You have complex custom requirements
- You're working with multiple languages/frameworks
- You want to understand every step of the build

### Use Buildpacks when:
- You want optimized images without Dockerfile maintenance
- You need automatic security updates
- You're building cloud-native applications
- You want reproducible builds across environments

### Use Google Jib when:
- You're building only Java applications
- You want fast builds without Docker daemon
- You're working in CI/CD environments
- You want optimal layer separation for Java apps

---

## Current Project Implementation

This project demonstrates all three approaches:

- **accounts/**: Uses Native Docker with `Dockerfile`
- **loans/**: Uses Buildpacks via Spring Boot Maven plugin
- **cards/**: Uses Google Jib Maven plugin

Each service can be built using its respective approach, allowing you to compare the methods and choose the best fit for your use case.

---

## Docker Hub Operations

This section covers how to push and pull Docker images to/from Docker Hub for each approach.

### Prerequisites

1. **Install Docker Desktop** and ensure it's running
2. **Create a Docker Hub account** at [hub.docker.com](https://hub.docker.com)
3. **Login to Docker Hub**:
```bash
docker login
# Enter your Docker Hub username and password when prompted
```

### 1. Native Docker - Docker Hub Operations

#### Push to Docker Hub
```bash
# Navigate to accounts service
cd accounts

# Build the Spring Boot application
mvn clean package

# Build Docker image with proper naming convention
docker build -t nishantsnaik/accounts:latest .

# Tag the image for Docker Hub
docker tag nishantsnaik/accounts:latest nishantsnaik/accounts:v1.0.0

# Push to Docker Hub
docker push nishantsnaik/accounts:latest
docker push nishantsnaik/accounts:v1.0.0
```

#### Pull from Docker Hub
```bash
# Pull the image from Docker Hub
docker pull nishantsnaik/accounts:latest

# Run the pulled image
docker run -p 8080:8080 nishantsnaik/accounts:latest
```

### 2. Buildpacks - Docker Hub Operations

#### Push to Docker Hub
```bash
# Navigate to loans service
cd loans

# Build and push directly to Docker Hub using buildpacks
mvn spring-boot:build-image -Dspring-boot.build-image.imageName=nishantsnaik/loans:latest

# Push to Docker Hub (if not automatically pushed)
docker push nishantsnaik/loans:latest

# For versioned builds
mvn spring-boot:build-image -Dspring-boot.build-image.imageName=nishantsnaik/loans:v1.0.0
docker push nishantsnaik/loans:v1.0.0
```

#### Pull from Docker Hub
```bash
# Pull the image from Docker Hub
docker pull nishantsnaik/loans:latest

# Run the pulled image
docker run -p 8080:8080 nishantsnaik/loans:latest
```

### 3. Google Jib - Docker Hub Operations

#### Push to Docker Hub (Direct)
```bash
# Navigate to cards service
cd cards

# Configure Jib for Docker Hub (add to pom.xml if not already present)
# The image name should include your Docker Hub username
# Example: <image>nishantsnaik/cards:latest</image>

# Build and push directly to Docker Hub (no Docker daemon required)
mvn compile jib:build

# For versioned builds, update pom.xml or use -Dimage parameter
mvn compile jib:build -Dimage=nishantsnaik/cards:v1.0.0
```

#### Build Locally and Push
```bash
# Build to local Docker daemon first
mvn compile jib:dockerBuild

# Tag and push to Docker Hub
docker tag nishantsnaik/cards:s2 nishantsnaik/cards:latest
docker push nishantsnaik/cards:latest
```

#### Pull from Docker Hub
```bash
# Pull the image from Docker Hub
docker pull nishantsnaik/cards:latest

# Run the pulled image
docker run -p 8080:8080 nishantsnaik/cards:latest
```

---

## Docker Hub Best Practices

### Image Tagging Strategy
```bash
# Use semantic versioning
docker tag myapp:latest myusername/myapp:1.0.0
docker tag myapp:latest myusername/myapp:1.0
docker tag myapp:latest myusername/myapp:1

# Use environment-specific tags
docker tag myapp:latest myusername/myapp:production
docker tag myapp:latest myusername/myapp:staging
docker tag myapp:latest myusername/myapp:development
```

### Multi-Architecture Support
```bash
# Build for multiple architectures (requires docker buildx)
docker buildx build --platform linux/amd64,linux/arm64 -t nishantsnaik/accounts:latest --push .
```

### Automated Builds
```bash
# Script for automated builds
#!/bin/bash
VERSION=$1
SERVICE=$2

echo "Building $SERVICE version $VERSION"

cd $SERVICE
mvn clean package

case $SERVICE in
  "accounts")
    docker build -t nishantsnaik/$SERVICE:$VERSION .
    docker push nishantsnaik/$SERVICE:$VERSION
    ;;
  "loans")
    mvn spring-boot:build-image -Dspring-boot.build-image.imageName=nishantsnaik/$SERVICE:$VERSION
    docker push nishantsnaik/$SERVICE:$VERSION
    ;;
  "cards")
    mvn compile jib:build -Dimage=nishantsnaik/$SERVICE:$VERSION
    ;;
esac
```

### Docker Hub Repository Management
```bash
# List your repositories
curl -H "Authorization: Bearer $DOCKER_HUB_TOKEN" https://hub.docker.com/v2/repositories/nishantsnaik/

# Delete an image tag
curl -X DELETE -H "Authorization: Bearer $DOCKER_HUB_TOKEN" https://hub.docker.com/v2/repositories/nishantsnaik/accounts/tags/v1.0.0/
```

---

## Security Considerations

### Use .dockerignore Files
Create `.dockerignore` in each service directory:
```
target/
.git/
.gitignore
README.md
.DS_Store
mvnw
mvnw.cmd
.mvn/
HELP.md
```

### Scan Images for Vulnerabilities
```bash
# Use Docker Scout (built into Docker Desktop)
docker scout cves nishantsnaik/accounts:latest

# Or use Trivy
trivy image nishantsnaik/accounts:latest
```

### Private Repositories
```bash
# For private repositories, ensure you're logged in
docker login

# Pull private images
docker pull nishantsnaik/private-service:latest
```

---

## Troubleshooting

### Common Issues
1. **Authentication Errors**: Ensure you're logged into Docker Hub
2. **Permission Denied**: Check repository permissions and naming
3. **Image Not Found**: Verify the image name and tag are correct
4. **Push Timeout**: Check internet connection and image size

### Debug Commands
```bash
# Check Docker daemon status
docker info

# List local images
docker images

# Check image layers
docker history nishantsnaik/accounts:latest

# Inspect image
docker inspect nishantsnaik/accounts:latest
```

---

## Docker Compose Operations

Docker Compose allows you to define and run multi-container Docker applications using a single YAML file. This project includes a `docker-compose.yml` file in the `accounts/` directory that orchestrates all three microservices.

### Docker Compose File Structure

The `docker-compose.yml` file defines:
- **3 Services**: accounts, loans, and cards microservices
- **Port Mappings**: Each service runs on different ports (8081, 8090, 9000)
- **Resource Limits**: Each container limited to 700m memory
- **Network**: All services connected to a shared bridge network

### Basic Docker Compose Commands

#### Start All Services
```bash
# Navigate to the directory containing docker-compose.yml
cd accounts

# Start all services in detached mode (background)
docker-compose up -d

# Start all services in foreground (see logs)
docker-compose up

# Start specific services
docker-compose up -d accounts loans
```

#### Stop All Services
```bash
# Stop and remove containers, networks
docker-compose down

# Stop but keep containers and networks
docker-compose stop

# Stop and remove containers, networks, and volumes
docker-compose down -v

# Stop and remove images as well
docker-compose down --rmi all
```

#### View Status and Logs
```bash
# List running containers
docker-compose ps

# View logs for all services
docker-compose logs

# View logs for specific service
docker-compose logs accounts

# Follow logs in real-time
docker-compose logs -f

# View logs with timestamps
docker-compose logs -t
```

#### Service Management
```bash
# Restart specific service
docker-compose restart accounts

# Rebuild and restart specific service
docker-compose up -d --build accounts

# Scale services (run multiple instances)
docker-compose up -d --scale accounts=2 --scale loans=2

# Update service configuration
docker-compose up -d --force-recreate
```

### Advanced Docker Compose Operations

#### Environment Variables
```bash
# Use environment file
docker-compose --env-file .env up -d

# Override environment variables
docker-compose up -d -e SPRING_PROFILES_ACTIVE=prod
```

#### Health Checks
```bash
# Wait for services to be healthy
docker-compose up -d --wait

# Check health status
docker-compose ps --filter "status=running"
```

#### Network Operations
```bash
# List networks
docker network ls

# Inspect the vybercoders network
docker network inspect accounts_vybercoders

# Connect to a container
docker exec -it accounts-ms bash
```

### Development Workflow

#### Full Development Cycle
```bash
# 1. Build all services
cd accounts && mvn clean package
cd ../loans && mvn clean package  
cd ../cards && mvn clean package

# 2. Build Docker images
cd accounts && docker build -t nishantsnaik/accounts:s2 .
cd ../loans && mvn spring-boot:build-image -Dspring-boot.build-image.imageName=nishantsnaik/loans:s2
cd ../cards && mvn compile jib:dockerBuild -Dimage=nishantsnaik/cards:s2

# 3. Start all services
cd accounts && docker-compose up -d

# 4. Verify services are running
docker-compose ps

# 5. Test endpoints
curl http://localhost:8081/accounts
curl http://localhost:8090/loans
curl http://localhost:9000/cards
```

#### Hot Reload Development
```bash
# Start with volume mounts for development
docker-compose -f docker-compose.dev.yml up -d

# Watch for changes and rebuild
docker-compose watch
```

### Production Deployment

#### Production Docker Compose
```yaml
# docker-compose.prod.yml
services:
  accounts:
    image: "nishantsnaik/accounts:latest"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
    restart: unless-stopped
    deploy:
      replicas: 2
```

#### Deployment Commands
```bash
# Deploy to production
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d

# Rolling update
docker-compose up -d --no-deps accounts
```

### Troubleshooting Docker Compose

#### Common Issues
```bash
# Check container logs for errors
docker-compose logs service-name

# Inspect container configuration
docker-compose config

# Validate compose file
docker-compose config --quiet

# Clean up orphaned containers
docker-compose down --remove-orphans
```

#### Debug Commands
```bash
# Run one-off commands
docker-compose run accounts bash

# Execute commands in running container
docker-compose exec accounts ps aux

# Check resource usage
docker stats $(docker-compose ps -q)
```

### Docker Compose Best Practices

#### File Organization
```
microservices/
├── docker-compose.yml          # Base configuration
├── docker-compose.dev.yml      # Development overrides
├── docker-compose.prod.yml     # Production overrides
├── .env                        # Environment variables
└── services/
    ├── accounts/
    ├── loans/
    └── cards/
```

#### Naming Conventions
- Use descriptive service names
- Version your images properly
- Use consistent port mapping
- Label your containers appropriately

#### Resource Management
```yaml
services:
  accounts:
    deploy:
      resources:
        limits:
          memory: 700m
          cpus: '0.5'
        reservations:
          memory: 512m
          cpus: '0.25'
```

### Integration with CI/CD

#### GitHub Actions Example
```yaml
name: Deploy Services
on:
  push:
    branches: [main]
jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Deploy to production
        run: |
          docker-compose -f docker-compose.prod.yml up -d
```

#### Automated Testing
```bash
# Run tests against running services
docker-compose up -d
docker-compose exec accounts mvn test
docker-compose down
```
