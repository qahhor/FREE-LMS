# FREE LMS - Messenger Bots

Java-based messenger bots for FREE LMS platform.

## Bots

### Telegram Bot
- **Technology**: Spring Boot + TelegramBots library
- **Port**: 8081
- **Features**:
  - Multi-language support (EN, RU, UZ, AR, KK, TR)
  - Course browsing
  - User courses display
  - Inline and reply keyboards
  - Long polling

### WhatsApp Bot
- **Technology**: Spring Boot + Twilio API
- **Port**: 8082
- **Features**:
  - Multi-language support (EN, RU, UZ, AR, KK, TR)
  - Course browsing
  - User courses display
  - Webhook-based message handling

## Quick Start

### Prerequisites
- Java 21
- Maven 3.9+
- Docker (optional)

### Telegram Bot

1. Create a bot with [@BotFather](https://t.me/BotFather) and get the token
2. Set environment variables:
```bash
export TELEGRAM_BOT_TOKEN=your-bot-token
export TELEGRAM_BOT_USERNAME=YourBotName
```

3. Run the bot:
```bash
cd telegram
mvn spring-boot:run
```

### WhatsApp Bot

1. Create a Twilio account at [twilio.com](https://www.twilio.com)
2. Set up WhatsApp Sandbox or Business API
3. Set environment variables:
```bash
export TWILIO_ACCOUNT_SID=your-account-sid
export TWILIO_AUTH_TOKEN=your-auth-token
export TWILIO_WHATSAPP_NUMBER=+14155238886
```

4. Configure webhook URL in Twilio Console:
   - Incoming messages: `https://your-domain/webhook`
   - Status callbacks: `https://your-domain/webhook/status`

5. Run the bot:
```bash
cd whatsapp
mvn spring-boot:run
```

## Docker Deployment

### Build and Run All Bots

```bash
# Create network (if not exists)
docker network create freelms-network

# Create .env file
cat > .env << EOF
TELEGRAM_BOT_TOKEN=your-telegram-token
TELEGRAM_BOT_USERNAME=YourBotName
TWILIO_ACCOUNT_SID=your-twilio-sid
TWILIO_AUTH_TOKEN=your-twilio-token
TWILIO_WHATSAPP_NUMBER=+14155238886
LMS_API_URL=http://app:8080/api/v1
EOF

# Build and start
docker-compose up -d --build
```

### Individual Bot

```bash
# Telegram
docker build -t freelms-telegram-bot ./telegram
docker run -d -p 8081:8081 \
  -e TELEGRAM_BOT_TOKEN=your-token \
  -e TELEGRAM_BOT_USERNAME=YourBot \
  freelms-telegram-bot

# WhatsApp
docker build -t freelms-whatsapp-bot ./whatsapp
docker run -d -p 8082:8082 \
  -e TWILIO_ACCOUNT_SID=your-sid \
  -e TWILIO_AUTH_TOKEN=your-token \
  freelms-whatsapp-bot
```

## API Integration

Both bots integrate with the FREE LMS API for:
- Fetching available courses
- Getting user enrolled courses
- User authentication (TODO)

Default API URL: `http://localhost:8080/api/v1`

## Supported Languages

| Code | Language | Flag |
|------|----------|------|
| en | English | 🇺🇸 |
| ru | Русский | 🇷🇺 |
| uz | O'zbek | 🇺🇿 |
| ar | العربية | 🇸🇦 |
| kk | Қазақ | 🇰🇿 |
| tr | Türkçe | 🇹🇷 |

## Project Structure

```
bots/
├── telegram/
│   ├── src/main/java/com/freelms/bot/telegram/
│   │   ├── TelegramBotApplication.java
│   │   ├── config/
│   │   │   ├── BotConfig.java
│   │   │   └── WebClientConfig.java
│   │   ├── handler/
│   │   │   └── FreeLmsTelegramBot.java
│   │   ├── model/
│   │   │   ├── Course.java
│   │   │   ├── Language.java
│   │   │   ├── Translations.java
│   │   │   └── UserState.java
│   │   └── service/
│   │       ├── LmsApiService.java
│   │       └── UserStateService.java
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── Dockerfile
│   └── pom.xml
├── whatsapp/
│   ├── src/main/java/com/freelms/bot/whatsapp/
│   │   ├── WhatsAppBotApplication.java
│   │   ├── config/
│   │   │   ├── TwilioConfig.java
│   │   │   └── WebClientConfig.java
│   │   ├── controller/
│   │   │   └── WebhookController.java
│   │   ├── handler/
│   │   │   └── MessageHandler.java
│   │   ├── model/
│   │   │   ├── Course.java
│   │   │   ├── Language.java
│   │   │   ├── Translations.java
│   │   │   └── UserState.java
│   │   └── service/
│   │       ├── LmsApiService.java
│   │       ├── UserStateService.java
│   │       └── WhatsAppMessageService.java
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── Dockerfile
│   └── pom.xml
├── docker-compose.yml
└── README.md
```

## Health Checks

- Telegram Bot: `http://localhost:8081/actuator/health`
- WhatsApp Bot: `http://localhost:8082/actuator/health`

## Monitoring

Both bots expose Actuator endpoints:
- `/actuator/health` - Health status
- `/actuator/info` - Application info
- `/actuator/metrics` - Metrics

## Migration from Node.js

These Java bots replace the original Node.js implementations:
- `bots/telegram/` (TypeScript + Telegraf) → `backend-java/bots/telegram/` (Spring Boot + TelegramBots)
- `bots/whatsapp/` (TypeScript + whatsapp-web.js) → `backend-java/bots/whatsapp/` (Spring Boot + Twilio)

Key differences:
1. **WhatsApp**: Changed from browser-based whatsapp-web.js to Twilio Business API for production reliability
2. **Architecture**: Spring Boot provides better enterprise integration with the LMS monolith
3. **Scalability**: Docker-ready with health checks and graceful shutdown
