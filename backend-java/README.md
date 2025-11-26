# FREE LMS — Java Spring Boot Backend

<div align="center">

![Java](https://img.shields.io/badge/Java-21_LTS-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-green)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.1-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![Microservices](https://img.shields.io/badge/Microservices-20-purple)

**Enterprise Learning Management System — Microservices Architecture**

</div>

---

## 📋 Содержание

- [Архитектура](#архитектура)
- [Технологический стек](#технологический-стек)
- [Микросервисы](#микросервисы)
- [Быстрый старт](#быстрый-старт)
- [Структура проекта](#структура-проекта)
- [API документация](#api-документация)
- [Конфигурация](#конфигурация)
- [База данных](#база-данных)
- [Тестирование](#тестирование)
- [Развертывание](#развертывание)

---

## 🏗️ Архитектура

### Обзор системы

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                                  CLIENTS                                         │
│                      (Web / Mobile / Third-party API)                           │
└─────────────────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           API GATEWAY (Port 8080)                                │
│                    Spring Cloud Gateway + Rate Limiting + JWT                    │
└─────────────────────────────────────────────────────────────────────────────────┘
                                       │
         ┌────────────────────────────┬┴┬────────────────────────────┐
         ▼                            ▼ ▼                            ▼
┌─────────────────┐         ┌─────────────────┐         ┌─────────────────┐
│  CORE SERVICES  │         │FEATURE SERVICES │         │  INFRASTRUCTURE │
│                 │         │                 │         │                 │
│ • Auth (8081)   │         │ • Learning Path │         │ • Eureka (8761) │
│ • Course (8082) │         │ • Skills        │         │ • Config (8888) │
│ • Enrollment    │         │ • Gamification  │         │                 │
│ • Payment       │         │ • IDP           │         │                 │
│ • Notification  │         │ • Feedback      │         │                 │
│ • Analytics     │         │ • Mentoring     │         │                 │
│ • Organization  │         │ • Social        │         │                 │
│                 │         │ • Compliance    │         │                 │
│                 │         │ • Reporting     │         │                 │
│                 │         │ • Integration   │         │                 │
└────────┬────────┘         └────────┬────────┘         └─────────────────┘
         │                           │
         └───────────────┬───────────┘
                         ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              DATA LAYER                                          │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐   │
│  │ PostgreSQL   │    │    Redis     │    │    Kafka     │    │    MinIO     │   │
│  │   (Данные)   │    │    (Кэш)     │    │  (События)   │    │   (Файлы)    │   │
│  └──────────────┘    └──────────────┘    └──────────────┘    └──────────────┘   │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### Event-Driven Architecture

```
┌────────────┐     ┌────────────┐     ┌────────────┐
│   Course   │────▶│   Kafka    │────▶│ Enrollment │
│  Service   │     │   Topics   │     │  Service   │
└────────────┘     └────────────┘     └────────────┘
                         │
         ┌───────────────┼───────────────┐
         ▼               ▼               ▼
┌────────────┐   ┌────────────┐   ┌────────────┐
│Notification│   │Gamification│   │ Analytics  │
│  Service   │   │  Service   │   │  Service   │
└────────────┘   └────────────┘   └────────────┘
```

---

## 🛠️ Технологический стек

| Категория | Технология | Версия |
|-----------|------------|--------|
| **Language** | Java | 21 LTS |
| **Framework** | Spring Boot | 3.2.5 |
| **Cloud** | Spring Cloud | 2023.0.1 |
| **Database** | PostgreSQL | 16 |
| **Cache** | Redis | 7 |
| **Message Broker** | Apache Kafka | 3.5+ |
| **Service Discovery** | Netflix Eureka | Latest |
| **API Gateway** | Spring Cloud Gateway | Latest |
| **ORM** | Spring Data JPA / Hibernate | 6.x |
| **Security** | Spring Security + JWT | 6.x |
| **API Docs** | SpringDoc OpenAPI | 2.7.0 |
| **Build Tool** | Maven | 3.9+ |
| **Container** | Docker + Docker Compose | Latest |
| **Orchestration** | Kubernetes | 1.28+ |

---

## 🔌 Микросервисы

### Infrastructure Services (3)

| Сервис | Порт | Описание |
|--------|------|----------|
| **service-registry** | 8761 | Eureka Service Discovery |
| **config-server** | 8888 | Centralized Configuration |
| **gateway-service** | 8080 | API Gateway + Rate Limiting |

### Core Services (7)

| Сервис | Порт | Описание |
|--------|------|----------|
| **auth-service** | 8081 | Authentication, Users, JWT, Roles |
| **course-service** | 8082 | Courses, Modules, Lessons, Quizzes |
| **enrollment-service** | 8083 | Enrollments, Progress, Certificates |
| **payment-service** | 8084 | Subscriptions, Stripe, Payme, Click |
| **notification-service** | 8085 | Email, Push, Telegram, WebSocket |
| **analytics-service** | 8086 | AI Recommendations, Smart Search |
| **organization-service** | 8087 | Multi-tenancy, SSO, SCORM, Webinars |

### Feature Services (10)

| Сервис | Порт | Описание |
|--------|------|----------|
| **learning-path-service** | 8088 | Learning Paths, Career Tracks, Prerequisites |
| **skills-service** | 8089 | Skills Matrix, Gap Analysis, Competencies |
| **gamification-service** | 8090 | Leaderboards, Achievements, Streaks, Challenges |
| **idp-service** | 8091 | Individual Development Plans, Goals |
| **feedback-service** | 8092 | 360° Feedback, Surveys, Reviews |
| **mentoring-service** | 8093 | Mentor Matching, Sessions, Tracking |
| **social-learning-service** | 8094 | Q&A Forum, Study Groups, Peer Content |
| **compliance-service** | 8095 | Mandatory Training, Certifications, Audit |
| **reporting-service** | 8096 | Dashboards, BI Export, ROI Analytics |
| **integration-service** | 8097 | HR Systems, Calendar, Video, SSO |

---

## 🚀 Быстрый старт

### Требования

```bash
java -version    # 21+
mvn -version     # 3.9+
docker --version # 24+
docker compose version # 2.20+
```

### Вариант 1: Docker Compose (Рекомендуется)

```bash
# Клонировать репозиторий
git clone https://github.com/your-org/FREE_LMS.git
cd FREE_LMS/backend-java

# Запустить всё
docker compose up -d

# Проверить статус
docker compose ps

# Смотреть логи
docker compose logs -f gateway-service
```

### Вариант 2: Локальная разработка

```bash
# 1. Запустить инфраструктуру
docker compose up -d postgres redis kafka zookeeper minio

# 2. Собрать проект
mvn clean package -DskipTests

# 3. Запустить сервисы по порядку

# Терминал 1: Service Registry
cd services/service-registry && mvn spring-boot:run

# Терминал 2: Config Server (подождите 30 сек)
cd services/config-server && mvn spring-boot:run

# Терминал 3: Gateway
cd services/gateway-service && mvn spring-boot:run

# Терминал 4+: Business Services
cd services/auth-service && mvn spring-boot:run
cd services/course-service && mvn spring-boot:run
# ... и т.д.
```

### Проверка работы

```bash
# Health check
curl http://localhost:8080/actuator/health

# Eureka Dashboard
open http://localhost:8761
# Login: eureka / eureka123

# Swagger UI
open http://localhost:8080/swagger-ui.html
```

---

## 📁 Структура проекта

```
backend-java/
├── pom.xml                              # Parent POM
├── docker-compose.yml                   # Development
├── docker-compose.prod.yml              # Production
├── Dockerfile                           # Multi-stage build
├── DEPLOYMENT.md                        # Deployment guide
│
├── common/                              # Shared library
│   └── src/main/java/com/freelms/common/
│       ├── config/                      # Security, Rate Limiting
│       ├── dto/                         # Common DTOs
│       ├── entity/                      # Base entities
│       ├── enums/                       # Enumerations
│       ├── exception/                   # Exception handling
│       ├── security/                    # JWT, Auth
│       ├── validation/                  # Custom validators
│       └── util/                        # Utilities
│
├── services/                            # 20 Microservices
│   ├── service-registry/                # Eureka Server
│   ├── config-server/                   # Config Server
│   ├── gateway-service/                 # API Gateway
│   ├── auth-service/                    # Authentication
│   ├── course-service/                  # Courses
│   ├── enrollment-service/              # Enrollments
│   ├── payment-service/                 # Payments
│   ├── notification-service/            # Notifications
│   ├── analytics-service/               # Analytics
│   ├── organization-service/            # Organizations
│   ├── learning-path-service/           # Learning Paths
│   ├── skills-service/                  # Skills
│   ├── gamification-service/            # Gamification
│   ├── idp-service/                     # IDP
│   ├── feedback-service/                # Feedback
│   ├── mentoring-service/               # Mentoring
│   ├── social-learning-service/         # Social Learning
│   ├── compliance-service/              # Compliance
│   ├── reporting-service/               # Reporting
│   └── integration-service/             # Integrations
│
├── database/
│   └── migrations/                      # SQL migrations
│       └── V2__add_performance_indexes.sql
│
├── k8s/                                 # Kubernetes manifests
│   ├── namespace.yaml
│   ├── configmap.yaml
│   ├── secrets.yaml
│   ├── gateway-deployment.yaml
│   ├── auth-deployment.yaml
│   └── postgres-statefulset.yaml
│
└── load-testing/                        # Performance tests
    └── gatling/
        ├── pom.xml
        └── src/test/scala/
            └── FreeLmsLoadTest.scala
```

---

## 📖 API документация

### Gateway Endpoints

| Сервис | Swagger UI |
|--------|------------|
| **Gateway** | http://localhost:8080/swagger-ui.html |

### Direct Service Endpoints

| Сервис | URL |
|--------|-----|
| Auth | http://localhost:8081/swagger-ui.html |
| Course | http://localhost:8082/swagger-ui.html |
| Enrollment | http://localhost:8083/swagger-ui.html |
| Payment | http://localhost:8084/swagger-ui.html |
| Notification | http://localhost:8085/swagger-ui.html |
| Analytics | http://localhost:8086/swagger-ui.html |
| Organization | http://localhost:8087/swagger-ui.html |

### Основные API Endpoints

```http
# Authentication
POST   /api/v1/auth/register
POST   /api/v1/auth/login
POST   /api/v1/auth/refresh
GET    /api/v1/auth/me

# Courses
GET    /api/v1/courses
GET    /api/v1/courses/{id}
POST   /api/v1/courses
PUT    /api/v1/courses/{id}
DELETE /api/v1/courses/{id}

# Enrollments
POST   /api/v1/enrollments/courses/{id}
GET    /api/v1/enrollments/my
PUT    /api/v1/enrollments/{id}/progress

# Gamification
GET    /api/v1/gamification/leaderboard
GET    /api/v1/gamification/achievements/my
GET    /api/v1/gamification/streaks/my

# Learning Paths
GET    /api/v1/learning-paths
POST   /api/v1/learning-paths/{id}/enroll
GET    /api/v1/learning-paths/my/progress

# Skills
GET    /api/v1/skills/matrix
GET    /api/v1/skills/gaps
POST   /api/v1/skills/{id}/endorse
```

---

## ⚙️ Конфигурация

### Environment Variables

```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_USER=lms_user
DB_PASSWORD=lms_password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# Kafka
KAFKA_SERVERS=localhost:9092

# JWT (минимум 256 бит)
JWT_SECRET=your-256-bit-secret-key-change-in-production

# Eureka
EUREKA_HOST=localhost
EUREKA_PORT=8761
EUREKA_USER=eureka
EUREKA_PASSWORD=eureka123

# Config Server
CONFIG_HOST=localhost
CONFIG_PORT=8888
CONFIG_USER=config
CONFIG_PASSWORD=config123
```

### Application Profiles

| Profile | Использование |
|---------|---------------|
| `default` | Локальная разработка |
| `docker` | Docker Compose |
| `production` | Production deployment |
| `test` | Тестирование |

```bash
# Запуск с профилем
mvn spring-boot:run -Dspring-boot.run.profiles=docker
```

---

## 🗃️ База данных

### Схема баз данных

Каждый сервис имеет отдельную базу данных:

| База данных | Сервис |
|-------------|--------|
| freelms_auth | auth-service |
| freelms_courses | course-service |
| freelms_enrollments | enrollment-service |
| freelms_payments | payment-service |
| freelms_notifications | notification-service |
| freelms_analytics | analytics-service |
| freelms_organizations | organization-service |
| freelms_learning_paths | learning-path-service |
| freelms_skills | skills-service |
| freelms_gamification | gamification-service |
| freelms_idp | idp-service |
| freelms_feedback | feedback-service |
| freelms_mentoring | mentoring-service |
| freelms_social | social-learning-service |
| freelms_compliance | compliance-service |
| freelms_reporting | reporting-service |
| freelms_integrations | integration-service |

### Индексы

```bash
# Применение индексов для оптимизации
psql -f database/migrations/V2__add_performance_indexes.sql
```

Включает 100+ индексов для:
- Foreign Key relationships
- Composite queries
- Partial indexes for active records

---

## 🧪 Тестирование

### Unit Tests

```bash
# Все тесты
mvn test

# Конкретный сервис
mvn test -pl services/auth-service

# С покрытием
mvn test jacoco:report
```

### Integration Tests

```bash
mvn verify -Pintegration
```

### Load Testing (Gatling)

```bash
cd load-testing/gatling

# Standard test (1000 users)
mvn gatling:test -DbaseUrl=http://localhost:8080 -Dusers=1000

# Smoke test
mvn gatling:test -Dgatling.simulationClass=freelms.SmokeTest
```

**Target Metrics:**
- Response time p95: < 500ms
- Error rate: < 1%
- RPS: 1000+

---

## 🚢 Развертывание

### Docker

```bash
# Development
docker compose up -d

# Production
docker compose -f docker-compose.prod.yml up -d

# Rebuild specific service
docker compose build auth-service
docker compose up -d auth-service
```

### Kubernetes

```bash
# Apply all manifests
kubectl apply -f k8s/

# Check status
kubectl get pods -n freelms
kubectl get services -n freelms

# View logs
kubectl logs -n freelms -l app=gateway-service -f
```

### Scaling

```bash
# Docker
docker compose up -d --scale auth-service=3

# Kubernetes (HPA автоматически)
kubectl get hpa -n freelms
```

---

## 📊 Мониторинг

### Health Endpoints

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/metrics
curl http://localhost:8080/actuator/prometheus
```

### Eureka Dashboard

- URL: http://localhost:8761
- Credentials: eureka / eureka123

---

## 📄 Лицензия

MIT License — см. [LICENSE](../LICENSE)

---

<div align="center">

**FREE LMS Backend** — Enterprise-grade Learning Management System

</div>
