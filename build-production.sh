#!/bin/bash
# FREE LMS - Production Build Script
# Builds the monolith JAR and optionally Docker images

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${GREEN}================================${NC}"
echo -e "${GREEN}  FREE LMS Production Build    ${NC}"
echo -e "${GREEN}================================${NC}"

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 21 ]; then
    echo -e "${RED}Error: Java 21+ is required. Current version: $JAVA_VERSION${NC}"
    exit 1
fi
echo -e "${GREEN}Java version: OK (v$JAVA_VERSION)${NC}"

# Check Maven
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Error: Maven is not installed${NC}"
    exit 1
fi
echo -e "${GREEN}Maven: OK${NC}"

# Build Monolith
echo -e "\n${YELLOW}Building Monolith...${NC}"
cd backend-java/monolith
mvn clean package -DskipTests -Dspring.profiles.active=prod

if [ -f target/free-lms-monolith-*.jar ]; then
    JAR_FILE=$(ls target/free-lms-monolith-*.jar | head -1)
    JAR_SIZE=$(du -h "$JAR_FILE" | cut -f1)
    echo -e "${GREEN}Monolith JAR built: $JAR_FILE ($JAR_SIZE)${NC}"
else
    echo -e "${RED}Error: Monolith JAR not found${NC}"
    exit 1
fi
cd ../..

# Build Telegram Bot (optional)
if [ "$1" == "--with-bots" ] || [ "$1" == "-b" ]; then
    echo -e "\n${YELLOW}Building Telegram Bot...${NC}"
    cd backend-java/bots/telegram
    mvn clean package -DskipTests
    cd ../../..

    echo -e "\n${YELLOW}Building WhatsApp Bot...${NC}"
    cd backend-java/bots/whatsapp
    mvn clean package -DskipTests
    cd ../../..
fi

# Build Docker images (optional)
if [ "$1" == "--docker" ] || [ "$1" == "-d" ]; then
    echo -e "\n${YELLOW}Building Docker images...${NC}"

    # Monolith
    docker build -t freelms/monolith:latest -t freelms/monolith:1.0.0 backend-java/monolith/

    # Bots
    docker build -t freelms/telegram-bot:latest backend-java/bots/telegram/
    docker build -t freelms/whatsapp-bot:latest backend-java/bots/whatsapp/

    echo -e "${GREEN}Docker images built successfully${NC}"
fi

echo -e "\n${GREEN}================================${NC}"
echo -e "${GREEN}  Build completed successfully!  ${NC}"
echo -e "${GREEN}================================${NC}"

echo -e "\nArtifacts:"
echo -e "  Monolith JAR: backend-java/monolith/target/free-lms-monolith-1.0.0-SNAPSHOT.jar"

if [ "$1" == "--with-bots" ] || [ "$1" == "-b" ]; then
    echo -e "  Telegram Bot JAR: backend-java/bots/telegram/target/telegram-bot-1.0.0.jar"
    echo -e "  WhatsApp Bot JAR: backend-java/bots/whatsapp/target/whatsapp-bot-1.0.0.jar"
fi

echo -e "\nTo run:"
echo -e "  java -jar backend-java/monolith/target/free-lms-monolith-1.0.0-SNAPSHOT.jar"
echo -e "\nTo run with Docker Compose:"
echo -e "  docker-compose -f docker-compose.monolith.yml up -d"
