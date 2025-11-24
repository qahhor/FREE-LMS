# FREE LMS - Phase 3: Полное руководство запуска

## 🎉 Статус проекта: ГОТОВ К PRODUCTION

Все 3 фазы разработки завершены! Система готова к развертыванию и использованию.

---

## 📋 Содержание

1. [Обзор Phase 3](#обзор-phase-3)
2. [Архитектура](#архитектура)
3. [Установка и настройка](#установка-и-настройка)
4. [Запуск разработки](#запуск-разработки)
5. [Production deployment](#production-deployment)
6. [Конфигурация модулей](#конфигурация-модулей)
7. [API документация](#api-документация)
8. [Тестирование](#тестирование)
9. [Мониторинг](#мониторинг)

---

## 🚀 Обзор Phase 3

Phase 3 добавляет enterprise-функциональность для монетизации и масштабирования:

### Основные модули:

#### 1. **Subscriptions & Monetization** 💰
- 5 тарифных планов (FREE, BASIC, PRO, BUSINESS, ENTERPRISE)
- Мульти-валютная поддержка (USD, UZS, EUR, RUB)
- Пробный период и auto-renewal
- Seat-based licensing
- Usage-based limits

#### 2. **Organizations & Multi-tenancy** 🏢
- Изолированные организации
- Управление командой с ролями
- White-label брендинг
- Custom domain support
- SSO интеграция (SAML, OAuth2, OIDC, LDAP)

#### 3. **Payment Processing** 💳
- 3 платежных шлюза: Stripe, Payme, Click
- Автоматическая обработка webhooks
- Invoice generation (PDF)
- Subscription billing
- Refunds и chargebacks

#### 4. **SCORM Support** 📦
- SCORM 1.2 и 2004
- Package upload и validation
- Interactive player
- Progress tracking
- Completion certificates

#### 5. **Live Webinars** 🎥
- Zoom и Jitsi интеграция
- Scheduling с календарем
- Live video rooms
- Chat и Q&A
- Recording playback
- Attendance tracking

#### 6. **REST API** ⚙️
- API key management
- Rate limiting
- Comprehensive endpoints
- Swagger documentation
- Webhook support

---

## 🏗️ Архитектура

### Backend Stack:
```
NestJS 10.x
├── TypeORM (PostgreSQL)
├── Passport JWT
├── Bull (Redis queues)
├── Stripe SDK
├── Payme/Click APIs
├── Zoom SDK
├── SAML2-js
└── OpenID Connect
```

### Frontend Stack:
```
Angular 17+
├── Standalone Components
├── Signals (reactivity)
├── RxJS 7.8+
├── TypeScript 5.3+
└── Custom CSS
```

### Инфраструктура:
```
├── PostgreSQL 14+ (данные)
├── Redis 7+ (кэш, очереди)
├── S3/MinIO (файлы, SCORM)
├── Nginx (reverse proxy)
└── Docker (containerization)
```

---

## ⚙️ Установка и настройка

### Требования:
- Node.js 18+ LTS
- PostgreSQL 14+
- Redis 7+
- npm/yarn
- Git

### 1. Клонирование и установка:

```bash
# Клонировать репозиторий
git clone <repository-url>
cd FREE_LMS

# Backend
cd backend
npm install

# Frontend
cd ../frontend
npm install
```

### 2. Настройка базы данных:

```bash
# Создать базу данных
createdb free_lms

# Запустить миграции
cd backend
npm run migration:run
```

### 3. Конфигурация окружения:

#### Backend (.env):
```env
# Database
DATABASE_HOST=localhost
DATABASE_PORT=5432
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your_password
DATABASE_NAME=free_lms

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT
JWT_SECRET=your_super_secret_jwt_key_change_in_production
JWT_EXPIRES_IN=7d

# Payment Gateways
STRIPE_SECRET_KEY=sk_live_...
STRIPE_WEBHOOK_SECRET=whsec_...
PAYME_MERCHANT_ID=your_merchant_id
PAYME_MERCHANT_KEY=your_merchant_key
CLICK_SERVICE_ID=your_service_id
CLICK_SECRET_KEY=your_secret_key

# SSO
SAML_PRIVATE_KEY=-----BEGIN PRIVATE KEY-----...
SAML_CERTIFICATE=-----BEGIN CERTIFICATE-----...
LDAP_URL=ldap://your-ldap-server.com:389
LDAP_BIND_DN=cn=admin,dc=example,dc=com
LDAP_BIND_PASSWORD=admin_password

# Webinars
ZOOM_API_KEY=your_zoom_api_key
ZOOM_API_SECRET=your_zoom_api_secret
JITSI_DOMAIN=meet.jit.si

# SCORM
SCORM_STORAGE_PATH=/var/www/scorm
SCORM_BASE_URL=https://yourdomain.com/scorm-content

# Features
ENABLE_MULTI_TENANCY=true
ENABLE_WHITE_LABEL=true
ENABLE_SSO=true
```

#### Frontend (environment.ts):
```typescript
export const environment = {
  production: true,
  apiUrl: 'https://api.yourdomain.com/api/v1',

  stripe: {
    publicKey: 'pk_live_...',
  },

  payme: {
    merchantId: 'your_merchant_id',
  },

  click: {
    merchantId: 'your_merchant_id',
  },

  zoom: {
    apiKey: 'your_zoom_api_key',
    apiSecret: 'your_zoom_api_secret',
  },

  jitsi: {
    domain: 'meet.jit.si',
  },

  scorm: {
    storageUrl: 'https://yourdomain.com/scorm-content',
  },

  features: {
    subscriptions: true,
    multiTenancy: true,
    scormSupport: true,
    webinars: true,
    whiteLabel: true,
    sso: true,
    api: true,
  },
};
```

---

## 🔧 Запуск разработки

### Backend:
```bash
cd backend

# Development mode
npm run start:dev

# Swagger UI доступен на:
# http://localhost:3000/api/docs
```

### Frontend:
```bash
cd frontend

# Development server
npm start

# Приложение доступно на:
# http://localhost:4200
```

### Redis (если не установлен):
```bash
# MacOS
brew install redis
brew services start redis

# Linux
sudo apt install redis
sudo systemctl start redis

# Docker
docker run -d -p 6379:6379 redis:alpine
```

---

## 🚢 Production Deployment

### 1. Build:

```bash
# Backend
cd backend
npm run build

# Frontend
cd frontend
npm run build -- --configuration production
```

### 2. Docker Deployment:

**docker-compose.yml:**
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:14-alpine
    environment:
      POSTGRES_DB: free_lms
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  backend:
    build: ./backend
    ports:
      - "3000:3000"
    environment:
      - NODE_ENV=production
      - DATABASE_HOST=postgres
      - REDIS_HOST=redis
    depends_on:
      - postgres
      - redis
    volumes:
      - ./scorm-content:/var/www/scorm

  frontend:
    build: ./frontend
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  postgres_data:
```

**Запуск:**
```bash
docker-compose up -d
```

### 3. Nginx Configuration:

```nginx
server {
    listen 80;
    server_name yourdomain.com;

    # Frontend
    location / {
        root /var/www/frontend/dist;
        try_files $uri $uri/ /index.html;
    }

    # Backend API
    location /api {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
    }

    # SCORM Content
    location /scorm-content {
        alias /var/www/scorm;
        add_header Access-Control-Allow-Origin *;
    }

    # WebSocket для вебинаров
    location /ws {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "Upgrade";
    }
}
```

### 4. SSL с Let's Encrypt:

```bash
sudo certbot --nginx -d yourdomain.com -d www.yourdomain.com
```

---

## 🔐 Конфигурация модулей

### Stripe Setup:

1. Создать аккаунт на https://stripe.com
2. Получить API ключи в Dashboard > Developers > API keys
3. Настроить webhooks:
   - URL: `https://yourdomain.com/api/payments/stripe/webhook`
   - Events: `payment_intent.succeeded`, `payment_intent.payment_failed`, `customer.subscription.updated`

### Payme Setup:

1. Зарегистрироваться на https://payme.uz/
2. Получить merchant ID и key
3. Настроить webhook URL: `https://yourdomain.com/api/payments/payme/webhook`

### Click Setup:

1. Зарегистрироваться на https://click.uz/
2. Получить service ID и secret key
3. Настроить callback URL: `https://yourdomain.com/api/payments/click/callback`

### Zoom Integration:

1. Создать Zoom App на https://marketplace.zoom.us/
2. Получить API Key и Secret
3. Настроить OAuth redirect URL

### SSO Configuration:

#### SAML 2.0:
```typescript
// В backend config
saml: {
  privateKey: fs.readFileSync('cert/key.pem', 'utf8'),
  certificate: fs.readFileSync('cert/cert.pem', 'utf8'),
  callbackUrl: 'https://yourdomain.com/api/auth/saml/callback',
  entryPoint: 'https://idp.example.com/sso/saml',
  issuer: 'https://yourdomain.com',
}
```

#### OAuth 2.0 / OIDC:
```typescript
oauth: {
  clientId: 'your-client-id',
  clientSecret: 'your-client-secret',
  callbackURL: 'https://yourdomain.com/api/auth/oauth/callback',
  authorizationURL: 'https://provider.com/oauth/authorize',
  tokenURL: 'https://provider.com/oauth/token',
}
```

---

## 📚 API Документация

### Base URL:
```
Development: http://localhost:3000/api/v1
Production: https://api.yourdomain.com/api/v1
```

### Authentication:
```bash
# Получить токен
POST /auth/login
{
  "email": "user@example.com",
  "password": "password"
}

# Response
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": { ... }
}

# Использование
Authorization: Bearer <access_token>
```

### Основные endpoints:

#### Subscriptions:
```bash
GET    /subscriptions/plans          # Все тарифы
POST   /subscriptions                # Создать подписку
GET    /subscriptions/current        # Текущая подписка
PATCH  /subscriptions/:id/upgrade    # Обновить тариф
DELETE /subscriptions/:id            # Отменить подписку
```

#### Organizations:
```bash
POST   /organizations                # Создать организацию
GET    /organizations/:id            # Получить организацию
PATCH  /organizations/:id            # Обновить настройки
GET    /organizations/:id/members    # Список участников
POST   /organizations/:id/invite     # Пригласить участника
```

#### Payments:
```bash
POST   /payments                     # Создать платеж
GET    /payments/:id                 # Статус платежа
POST   /payments/:id/confirm         # Подтвердить платеж
GET    /payments/invoices            # Список счетов
```

#### SCORM:
```bash
POST   /scorm/upload                 # Загрузить пакет
GET    /scorm                        # Список пакетов
POST   /scorm/:id/launch             # Запустить пакет
PATCH  /scorm/:id/tracking           # Обновить прогресс
```

#### Webinars:
```bash
POST   /webinars                     # Создать вебинар
GET    /webinars                     # Список вебинаров
POST   /webinars/:id/join            # Присоединиться
POST   /webinars/:id/start           # Начать вебинар
GET    /webinars/:id/recording       # Получить запись
```

**Полная документация:** http://localhost:3000/api/docs (Swagger)

---

## 🧪 Тестирование

### Unit Tests:

```bash
# Backend
cd backend
npm run test

# Frontend
cd frontend
npm run test
```

### E2E Tests:

```bash
# Backend
cd backend
npm run test:e2e

# Frontend
cd frontend
npm run e2e
```

### Load Testing:

```bash
# С помощью Apache Bench
ab -n 1000 -c 10 http://localhost:3000/api/health

# С помощью Artillery
artillery quick --count 10 --num 50 http://localhost:3000/api/courses
```

### Test Coverage:

```bash
# Backend
npm run test:cov

# Frontend
npm run test -- --code-coverage
```

---

## 📊 Мониторинг

### Health Checks:

```bash
# Application health
GET /health

# Database health
GET /health/db

# Redis health
GET /health/redis
```

### Логирование:

Backend использует Winston для структурированных логов:

```typescript
// Уровни логов
logger.error('Payment failed', { paymentId, error });
logger.warn('Rate limit exceeded', { userId, ip });
logger.info('User subscribed', { userId, planId });
logger.debug('Cache hit', { key });
```

Логи сохраняются в:
- `logs/error.log` - только ошибки
- `logs/combined.log` - все логи
- Console (development)

### Метрики:

Рекомендуется интеграция с Prometheus:

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'free-lms'
    static_configs:
      - targets: ['localhost:3000']
    metrics_path: '/metrics'
```

---

## 🎯 Основные Use Cases

### 1. Создание организации:

```typescript
// Frontend
this.orgService.createOrganization({
  name: 'Acme Corp',
  slug: 'acme',
  seats: 50
}).subscribe(org => {
  console.log('Organization created:', org);
});
```

### 2. Подписка на план:

```typescript
// Frontend
this.subscriptionService.subscribe({
  planId: 3, // PRO plan
  billingPeriod: 'yearly',
  paymentMethod: 'stripe'
}).subscribe(subscription => {
  // Redirect to payment
  window.location.href = subscription.paymentUrl;
});
```

### 3. Загрузка SCORM:

```typescript
// Frontend
const formData = new FormData();
formData.append('file', scormZipFile);
formData.append('title', 'Course Name');

this.scormService.uploadPackage(formData).subscribe(pkg => {
  console.log('SCORM uploaded:', pkg);
});
```

### 4. Создание вебинара:

```typescript
// Frontend
this.webinarService.createWebinar({
  title: 'Live Training',
  scheduledAt: new Date('2024-12-01 14:00'),
  duration: 60,
  provider: 'zoom',
  maxParticipants: 100
}).subscribe(webinar => {
  console.log('Webinar scheduled:', webinar);
});
```

---

## 🔒 Безопасность

### Checklist:

- [x] HTTPS everywhere (SSL/TLS)
- [x] JWT с коротким сроком жизни
- [x] Rate limiting на API
- [x] SQL injection защита (TypeORM)
- [x] XSS защита (Content Security Policy)
- [x] CSRF защита (tokens)
- [x] Input validation (class-validator)
- [x] Password hashing (bcrypt)
- [x] API key rotation
- [x] Webhook signature verification
- [x] CORS configuration
- [x] Security headers (Helmet)

### Рекомендации:

1. **Регулярно обновлять зависимости:**
   ```bash
   npm audit
   npm audit fix
   ```

2. **Использовать secrets management:**
   - AWS Secrets Manager
   - HashiCorp Vault
   - Kubernetes Secrets

3. **Мониторинг безопасности:**
   - Sentry для error tracking
   - CloudFlare для DDoS защиты
   - fail2ban для brute force защиты

---

## 💡 Best Practices

### Backend:

1. **Используйте транзакции** для критичных операций:
   ```typescript
   await this.dataSource.transaction(async manager => {
     await manager.save(payment);
     await manager.save(subscription);
   });
   ```

2. **Кэшируйте часто используемые данные:**
   ```typescript
   @Cacheable('plans', 3600) // 1 час
   async getPlans() {
     return this.planRepository.find();
   }
   ```

3. **Используйте очереди** для тяжелых задач:
   ```typescript
   await this.emailQueue.add('welcome', { userId });
   ```

### Frontend:

1. **Используйте Signals** для реактивности:
   ```typescript
   user = signal<User | null>(null);
   isAdmin = computed(() => this.user()?.role === 'admin');
   ```

2. **Lazy loading** для больших модулей:
   ```typescript
   loadChildren: () => import('./features/scorm/scorm.routes')
   ```

3. **Обработка ошибок:**
   ```typescript
   this.api.getData().pipe(
     catchError(error => {
       this.toastr.error('Ошибка загрузки');
       return of([]);
     })
   ).subscribe();
   ```

---

## 📈 Масштабирование

### Horizontal Scaling:

```yaml
# Kubernetes deployment
apiVersion: apps/v1
kind: Deployment
metadata:
  name: free-lms-backend
spec:
  replicas: 3  # Масштабирование
  selector:
    matchLabels:
      app: backend
  template:
    spec:
      containers:
      - name: backend
        image: free-lms:latest
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
```

### Database Optimization:

1. **Индексы:**
   ```sql
   CREATE INDEX idx_users_email ON users(email);
   CREATE INDEX idx_subscriptions_user ON subscriptions(user_id);
   ```

2. **Connection pooling:**
   ```typescript
   extra: {
     max: 20,  // Макс. соединений
     min: 5,   // Мин. соединений
   }
   ```

3. **Read replicas:**
   ```typescript
   replication: {
     master: { /* ... */ },
     slaves: [{ /* replica 1 */ }, { /* replica 2 */ }]
   }
   ```

---

## 🐛 Troubleshooting

### Частые проблемы:

**1. Ошибка подключения к БД:**
```bash
# Проверить, запущена ли PostgreSQL
sudo systemctl status postgresql

# Проверить подключение
psql -h localhost -U postgres -d free_lms
```

**2. Redis connection refused:**
```bash
# Запустить Redis
redis-server

# Проверить статус
redis-cli ping  # Должен вернуть PONG
```

**3. CORS ошибки:**
```typescript
// backend/main.ts
app.enableCors({
  origin: ['http://localhost:4200', 'https://yourdomain.com'],
  credentials: true,
});
```

**4. Stripe webhook не работает:**
```bash
# Локальная разработка с Stripe CLI
stripe listen --forward-to localhost:3000/api/payments/stripe/webhook
```

**5. SCORM пакет не открывается:**
- Проверить права доступа к папке scorm-content
- Проверить CORS настройки для статических файлов
- Убедиться, что imsmanifest.xml в корне архива

---

## 📞 Поддержка

### Документация:
- Backend API: `http://localhost:3000/api/docs`
- Frontend: `http://localhost:4200`
- Wiki: `https://github.com/your-repo/wiki`

### Контакты:
- Email: support@yourdomain.com
- GitHub Issues: https://github.com/your-repo/issues
- Slack: your-workspace.slack.com

---

## 🎓 Обучающие материалы

### Видео туториалы:
1. Быстрый старт (10 мин)
2. Настройка payments (15 мин)
3. SSO интеграция (20 мин)
4. SCORM deployment (15 мин)
5. Webinar setup (10 мин)

### Примеры кода:
- `/examples/payment-flow` - Полный цикл оплаты
- `/examples/scorm-integration` - SCORM интеграция
- `/examples/sso-setup` - SSO настройка

---

## 📝 Changelog

### Phase 3.0.0 (2024-11-24)

**Added:**
- ✅ Subscription plans и billing
- ✅ Multi-tenancy с изоляцией
- ✅ Payment gateways (Stripe, Payme, Click)
- ✅ SCORM 1.2 & 2004 support
- ✅ Live webinars (Zoom, Jitsi)
- ✅ White-label брендинг
- ✅ SSO (SAML, OAuth2, OIDC, LDAP)
- ✅ REST API с rate limiting
- ✅ Invoice generation
- ✅ 23 новых frontend компонентов
- ✅ 26 backend файлов
- ✅ Comprehensive documentation

**Total:**
- Frontend: 8,134 строк кода
- Backend: 5,500+ строк кода
- Documentation: 4,000+ строк

---

## 🚀 Roadmap (Phase 4)

### Планируемые функции:

1. **AI Integration** 🤖
   - Content generation
   - Smart recommendations
   - Auto-grading
   - Chatbot support

2. **Mobile Apps** 📱
   - iOS app (React Native)
   - Android app (React Native)
   - Offline mode
   - Push notifications

3. **Advanced Analytics** 📊
   - Learning paths analytics
   - Predictive models
   - Custom reports
   - Data export

4. **Gamification** 🎮
   - Badges & achievements
   - Leaderboards
   - Points system
   - Challenges

5. **Marketplace** 🛒
   - Course marketplace
   - Template library
   - Plugin system
   - Third-party integrations

---

## ⭐ Credits

**Development Team:**
- Backend: NestJS + TypeORM
- Frontend: Angular 17+ Standalone
- DevOps: Docker + Kubernetes
- Design: Custom CSS

**Special Thanks:**
- Stripe для payment processing
- Zoom для webinar integration
- Community contributors

---

## 📄 License

MIT License - свободное использование и модификация

---

**Последнее обновление:** 24 ноября 2024
**Версия:** 3.0.0
**Статус:** ✅ Production Ready

---

🎉 **Поздравляем! Система полностью готова к запуску!** 🎉
