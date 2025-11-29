# FREE LMS - Makefile
# Production commands for building and running the application

.PHONY: help build build-all docker run test clean

# Default target
help:
	@echo "FREE LMS - Available commands:"
	@echo ""
	@echo "  make build       - Build monolith JAR"
	@echo "  make build-all   - Build monolith + bots JARs"
	@echo "  make docker      - Build Docker images"
	@echo "  make run         - Run the application locally"
	@echo "  make test        - Run tests"
	@echo "  make clean       - Clean build artifacts"
	@echo "  make up          - Start with Docker Compose"
	@echo "  make down        - Stop Docker Compose"
	@echo "  make logs        - View application logs"

# Build monolith JAR
build:
	cd backend-java/monolith && mvn clean package -DskipTests

# Build all JARs (monolith + bots)
build-all:
	./build-production.sh --with-bots

# Build Docker images
docker:
	docker build -t freelms/monolith:latest backend-java/monolith/
	docker build -t freelms/telegram-bot:latest backend-java/bots/telegram/
	docker build -t freelms/whatsapp-bot:latest backend-java/bots/whatsapp/

# Run locally
run:
	java -jar backend-java/monolith/target/free-lms-monolith-*.jar

# Run tests
test:
	cd backend-java/monolith && mvn test

# Clean build artifacts
clean:
	cd backend-java/monolith && mvn clean
	cd backend-java/bots/telegram && mvn clean
	cd backend-java/bots/whatsapp && mvn clean

# Docker Compose commands
up:
	docker-compose -f docker-compose.monolith.yml up -d

down:
	docker-compose -f docker-compose.monolith.yml down

logs:
	docker-compose -f docker-compose.monolith.yml logs -f app

# Production deployment
deploy-prod:
	./build-production.sh --docker
	docker-compose -f docker-compose.monolith.yml up -d --build

# Health check
health:
	curl -s http://localhost:8080/actuator/health | jq .
