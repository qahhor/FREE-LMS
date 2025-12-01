# FREE LMS — Java Spring Boot Backend

<div align="center">

![Java](https://img.shields.io/badge/Java-21_LTS-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-green)
![Architecture](https://img.shields.io/badge/Architecture-Modular%20Monolith-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)

**Enterprise Learning Management System — Modular Monolith Architecture**

</div>

---

## Содержание

- [Архитектура](#архитектура)
- [Технологический стек](#технологический-стек)
- [Структура проекта](#структура-проекта)
- [Быстрый старт](#быстрый-старт)
- [API документация](#api-документация)
- [Конфигурация](#конфигурация)
- [База данных](#база-данных)
- [Тестирование](#тестирование)
- [Развертывание](#развертывание)

---

## Архитектура

### Модульный монолит

FREE LMS использует модульную монолитную архитектуру — оптимальный баланс между простотой монолита и модульностью микросервисов.

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              FREE LMS MONOLITH                                   │
│                                                                                  │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐              │
│  │   Auth Module    │  │  Course Module   │  │ Enrollment Module│              │
│  │                  │  │                  │  │                  │              │
│  │ • User Entity    │  │ • Course Entity  │  │ • Enrollment     │              │
│  │ • JWT Provider   │  │ • Module Entity  │  │ • Progress       │              │
│  │ • Auth Service   │  │ • Lesson Entity  │  │ • Certificate    │              │
│  │ • Auth Controller│  │ • Quiz Entity    │  │ • Quiz Attempts  │              │
│  └────────┬─────────┘  └────────┬─────────┘  └────────┬─────────┘              │
│           │                     │                     │                         │
│           └─────────────────────┼─────────────────────┘                         │
│                                 │                                               │
│                    ┌────────────▼────────────┐                                  │
│                    │     Common Module       │                                  │
│                    │                         │                                  │
│                    │ • BaseEntity            │                                  │
│                    │ • ApiResponse           │                                  │
│                    │ • Security Config       │                                  │
│                    │ • Exception Handlers    │                                  │
│                    └─────────────────────────┘                                  │
│                                                                                  │
│  ┌──────────────────┐                                                           │
│  │  Payment Module  │                                                           │
│  │                  │                                                           │
│  │ • Payment Entity │                                                           │
│  │ • Stripe Service │                                                           │
│  └──────────────────┘                                                           │
└─────────────────────────────────────────────────────────────────────────────────┘
                                       │
          ┌────────────────────────────┼────────────────────────────┐
          │                            │                            │
     ┌────▼────┐                  ┌────▼────┐                  ┌────▼────┐
     │PostgreSQL│                 │  Redis  │                  │  Kafka  │
     │   16    │                  │    7    │                  │(events) │
     └─────────┘                  └─────────┘                  └─────────┘
```

### Преимущества архитектуры

| Аспект | Микросервисы (было) | Модульный монолит (стало) |
|--------|---------------------|---------------------------|
| Время запуска | ~3-5 минут | ~30 секунд |
| Потребление RAM | 8-16 GB | 1-2 GB |
| Latency вызовов | 5-50ms (HTTP) | <1ms (method call) |
| Количество контейнеров | 35+ | 7 |
| Сложность деплоя | Высокая | Низкая |
| Отладка | Distributed tracing | Stack trace |
| Транзакции | Saga pattern | ACID |

---

## Технологический стек

| Категория | Технология | Версия |
|-----------|------------|--------|
| **Language** | Java | 21 LTS |
| **Framework** | Spring Boot | 3.2.5 |
| **Database** | PostgreSQL | 16 |
| **Cache** | Redis | 7 |
| **Message Broker** | Apache Kafka | 3.5+ |
| **Search** | Elasticsearch | 8.11 |
| **Document Store** | MongoDB | 7 |
| **Object Storage** | MinIO | Latest |
| **ORM** | Spring Data JPA / Hibernate | 6.x |
| **Security** | Spring Security + JWT | 6.x |
| **Migration** | Liquibase | 4.x |
| **API Docs** | SpringDoc OpenAPI | 2.7.0 |
| **Build Tool** | Maven | 3.9+ |
| **Container** | Docker | 24+ |

---

## Структура проекта

```
backend-java/
├── monolith/                            # Основное приложение
│   ├── pom.xml                          # Maven конфигурация
│   ├── Dockerfile                       # Multi-stage Docker build
│   ├── README.md                        # Документация монолита
│   │
│   └── src/
│       ├── main/
│       │   ├── java/com/freelms/lms/
│       │   │   │
│       │   │   ├── FreeLmsApplication.java    # Entry point
│       │   │   │
│       │   │   ├── auth/                      # Auth Module
│       │   │   │   ├── controller/
│       │   │   │   │   ├── AuthController.java
│       │   │   │   │   └── UserController.java
│       │   │   │   ├── service/
│       │   │   │   │   ├── AuthService.java
│       │   │   │   │   └── UserService.java
│       │   │   │   ├── entity/
│       │   │   │   │   ├── User.java
│       │   │   │   │   └── RefreshToken.java
│       │   │   │   ├── dto/
│       │   │   │   ├── repository/
│       │   │   │   └── mapper/
│       │   │   │
│       │   │   ├── course/                    # Course Module
│       │   │   │   ├── controller/
│       │   │   │   ├── service/
│       │   │   │   ├── entity/
│       │   │   │   │   ├── Course.java
│       │   │   │   │   ├── CourseModule.java
│       │   │   │   │   ├── Lesson.java
│       │   │   │   │   └── Quiz.java
│       │   │   │   ├── dto/
│       │   │   │   └── repository/
│       │   │   │
│       │   │   ├── enrollment/                # Enrollment Module
│       │   │   │   ├── controller/
│       │   │   │   ├── service/
│       │   │   │   ├── entity/
│       │   │   │   │   ├── Enrollment.java
│       │   │   │   │   ├── LessonProgress.java
│       │   │   │   │   ├── Certificate.java
│       │   │   │   │   └── QuizAttempt.java
│       │   │   │   ├── dto/
│       │   │   │   └── repository/
│       │   │   │
│       │   │   ├── payment/                   # Payment Module
│       │   │   │   ├── controller/
│       │   │   │   ├── service/
│       │   │   │   ├── entity/
│       │   │   │   ├── dto/
│       │   │   │   └── repository/
│       │   │   │
│       │   │   ├── common/                    # Shared Components
│       │   │   │   ├── dto/
│       │   │   │   │   ├── ApiResponse.java
│       │   │   │   │   └── PagedResponse.java
│       │   │   │   ├── entity/
│       │   │   │   │   └── BaseEntity.java
│       │   │   │   ├── enums/
│       │   │   │   │   ├── UserRole.java
│       │   │   │   │   ├── CourseStatus.java
│       │   │   │   │   └── EnrollmentStatus.java
│       │   │   │   ├── exception/
│       │   │   │   │   ├── GlobalExceptionHandler.java
│       │   │   │   │   ├── ResourceNotFoundException.java
│       │   │   │   │   └── BadRequestException.java
│       │   │   │   └── security/
│       │   │   │       ├── JwtTokenProvider.java
│       │   │   │       ├── JwtAuthenticationFilter.java
│       │   │   │       ├── UserPrincipal.java
│       │   │   │       └── CurrentUser.java
│       │   │   │
│       │   │   └── config/                    # Configuration
│       │   │       ├── SecurityConfig.java
│       │   │       ├── RedisConfig.java
│       │   │       ├── KafkaConfig.java
│       │   │       └── OpenApiConfig.java
│       │   │
│       │   └── resources/
│       │       ├── application.yml            # Main config
│       │       └── db/changelog/
│       │           ├── db.changelog-master.xml # Liquibase master
│       │           ├── changes/
│       │           │   └── 001-initial-schema.xml
│       │           └── sql/
│       │               └── V1__initial_schema.sql
│       │
│       └── test/                              # Tests
│           ├── java/com/freelms/lms/
│           │   ├── auth/
│           │   │   ├── service/AuthServiceTest.java
│           │   │   └── controller/AuthControllerTest.java
│           │   ├── course/
│           │   │   └── service/CourseServiceTest.java
│           │   └── enrollment/
│           │       └── service/EnrollmentServiceTest.java
│           └── resources/
│               └── application-test.yml
│
├── services/                            # (legacy) Микросервисы
│   └── ...                              # Для обратной совместимости
│
├── docker-compose.yml                   # Development
└── DEPLOYMENT.md                        # Deployment guide
```

---

## Быстрый старт

### Требования

```bash
java -version    # 21+
mvn -version     # 3.9+
docker --version # 24+
docker compose version # 2.20+
```

### Вариант 1: Docker Compose (Рекомендуется)

```bash
# Перейти в корень проекта
cd free-lms

# Запустить всё
docker-compose -f docker-compose.monolith.yml up -d

# Проверить статус
docker-compose -f docker-compose.monolith.yml ps

# Смотреть логи
docker-compose -f docker-compose.monolith.yml logs -f app
```

### Вариант 2: Локальная разработка

```bash
# 1. Запустить инфраструктуру
docker-compose -f docker-compose.monolith.yml up -d postgres redis kafka

# 2. Собрать проект
cd backend-java/monolith
mvn clean package -DskipTests

# 3. Запустить приложение
mvn spring-boot:run
# или
java -jar target/free-lms-monolith-1.0.0-SNAPSHOT.jar
```

### Проверка работы

```bash
# Health check
curl http://localhost:8080/actuator/health

# Swagger UI
open http://localhost:8080/swagger-ui.html
```

---

## API документация

### Swagger UI

После запуска доступен по адресу: http://localhost:8080/swagger-ui.html

### Основные API Endpoints

```http
# Authentication
POST   /api/v1/auth/register           # Регистрация
POST   /api/v1/auth/login              # Вход
POST   /api/v1/auth/refresh            # Обновление токена
GET    /api/v1/auth/me                 # Текущий пользователь
POST   /api/v1/auth/logout             # Выход

# Users
GET    /api/v1/users                   # Список пользователей (admin)
GET    /api/v1/users/{id}              # Получить пользователя
PUT    /api/v1/users/{id}              # Обновить профиль
PUT    /api/v1/users/{id}/password     # Сменить пароль

# Courses
GET    /api/v1/courses                 # Список курсов
GET    /api/v1/courses/{id}            # Детали курса
POST   /api/v1/courses                 # Создать курс
PUT    /api/v1/courses/{id}            # Обновить курс
DELETE /api/v1/courses/{id}            # Удалить курс
POST   /api/v1/courses/{id}/publish    # Опубликовать

# Categories
GET    /api/v1/categories              # Список категорий
POST   /api/v1/categories              # Создать категорию

# Enrollments
POST   /api/v1/enrollments/courses/{id}     # Записаться на курс
GET    /api/v1/enrollments/my               # Мои записи
GET    /api/v1/enrollments/{id}             # Детали записи
PUT    /api/v1/enrollments/{id}/progress    # Обновить прогресс
DELETE /api/v1/enrollments/{id}             # Отменить запись

# Certificates
GET    /api/v1/certificates/my              # Мои сертификаты
GET    /api/v1/certificates/{id}            # Получить сертификат
GET    /api/v1/certificates/{id}/verify     # Проверить сертификат
GET    /api/v1/certificates/{id}/download   # Скачать PDF

# Payments
POST   /api/v1/payments                     # Создать платёж
GET    /api/v1/payments/{id}                # Статус платежа
GET    /api/v1/payments/my                  # Мои платежи
POST   /api/v1/payments/webhook             # Stripe webhook
```

---

## Конфигурация

### Environment Variables

```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=freelms
DB_USER=lms_user
DB_PASSWORD=lms_password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# Kafka
KAFKA_SERVERS=localhost:9092

# JWT (минимум 256 бит)
JWT_SECRET=your-256-bit-secret-key-change-in-production
JWT_EXPIRATION=900000           # 15 минут в ms
JWT_REFRESH_EXPIRATION=604800000  # 7 дней в ms

# Application
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev
```

### Application Profiles

| Profile | Использование |
|---------|---------------|
| `dev` | Локальная разработка |
| `docker` | Docker Compose |
| `prod` | Production |
| `test` | Тестирование |

```bash
# Запуск с профилем
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Или через переменную окружения
export SPRING_PROFILES_ACTIVE=prod
java -jar app.jar
```

---

## База данных

### Единая схема

Все модули используют единую PostgreSQL базу данных:

```
freelms/
├── users                    # Пользователи
├── refresh_tokens           # Refresh токены
├── courses                  # Курсы
├── course_modules           # Модули курсов
├── lessons                  # Уроки
├── categories               # Категории
├── tags                     # Теги
├── course_tags              # Связь курсов и тегов
├── enrollments              # Записи на курсы
├── lesson_progress          # Прогресс уроков
├── certificates             # Сертификаты
├── payments                 # Платежи
├── quizzes                  # Тесты
├── quiz_questions           # Вопросы тестов
├── quiz_answers             # Ответы
├── quiz_attempts            # Попытки прохождения
├── notifications            # Уведомления
├── badges                   # Бейджи
├── user_badges              # Бейджи пользователей
└── audit_logs               # Аудит-логи
```

### Liquibase миграции

```bash
# Миграции применяются автоматически при запуске

# Ручной запуск миграций
mvn liquibase:update

# Проверка статуса
mvn liquibase:status

# Откат последней миграции
mvn liquibase:rollback -Dliquibase.rollbackCount=1
```

---

## Тестирование

### Unit Tests

```bash
# Все тесты
cd backend-java/monolith
mvn test

# С отчётом о покрытии
mvn test jacoco:report
# Отчёт: target/site/jacoco/index.html

# Конкретный тест
mvn test -Dtest=AuthServiceTest
```

### Integration Tests

```bash
# Запуск интеграционных тестов
mvn verify -Pintegration

# С тестовой БД (H2)
mvn test -Dspring.profiles.active=test
```

### Структура тестов

```
src/test/java/com/freelms/lms/
├── FreeLmsApplicationTests.java    # Context loading
├── auth/
│   ├── service/
│   │   └── AuthServiceTest.java    # Unit tests
│   └── controller/
│       └── AuthControllerTest.java # Integration tests
├── course/
│   └── service/
│       └── CourseServiceTest.java
└── enrollment/
    └── service/
        └── EnrollmentServiceTest.java
```

---

## Развертывание

### Docker

```bash
# Сборка образа
cd backend-java/monolith
docker build -t freelms/app:latest .

# Запуск с Docker Compose
docker-compose -f docker-compose.monolith.yml up -d

# Проверка
docker-compose -f docker-compose.monolith.yml ps
docker-compose -f docker-compose.monolith.yml logs -f app
```

### Kubernetes

```bash
# Применить манифесты
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secrets.yaml
kubectl apply -f k8s/app-deployment.yaml
kubectl apply -f k8s/app-service.yaml

# Проверить
kubectl get pods -n freelms
kubectl logs -n freelms -l app=freelms-app -f
```

### Scaling

```bash
# Docker Compose
# Монолит масштабируется горизонтально через load balancer

# Kubernetes
kubectl scale deployment freelms-app --replicas=3 -n freelms

# Проверить HPA
kubectl get hpa -n freelms
```

---

## Мониторинг

### Health Endpoints

```bash
# Liveness probe
curl http://localhost:8080/actuator/health/liveness

# Readiness probe
curl http://localhost:8080/actuator/health/readiness

# Full health
curl http://localhost:8080/actuator/health

# Prometheus metrics
curl http://localhost:8080/actuator/prometheus
```

### Логирование

```yaml
# application.yml
logging:
  level:
    root: INFO
    com.freelms: DEBUG
    org.springframework.security: DEBUG
```

---

## Безопасность

### Реализованные меры

- JWT аутентификация с access/refresh токенами
- BCrypt хеширование паролей
- CORS настройка
- Rate limiting
- Input validation (Jakarta Validation)
- SQL injection protection (JPA)
- XSS prevention (Content Security Policy)

### Production Checklist

- [ ] Изменить JWT_SECRET на криптографически стойкий
- [ ] Настроить HTTPS
- [ ] Ограничить CORS origins
- [ ] Включить rate limiting
- [ ] Настроить firewall
- [ ] Регулярные бэкапы БД

---

## Лицензия

MIT License — см. [LICENSE](../LICENSE)

---

<div align="center">

**FREE LMS Backend** — Enterprise-grade Learning Management System

</div>
