# FREE LMS - Корпоративная Система Обучения

<div align="center">

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-green)
![Architecture](https://img.shields.io/badge/Architecture-Modular%20Monolith-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

**Enterprise-grade Learning Management System**

[Демо](docs/landing/index.html) | [Документация](#документация) | [Быстрый старт](#быстрый-старт) | [API](#api-документация)

</div>

---

## О проекте

FREE LMS — это современная корпоративная система управления обучением, построенная на модульной монолитной архитектуре. Платформа разработана для масштабирования до **100 000+ пользователей** и **200+ организаций**.

### Ключевые метрики

| Показатель | Значение |
|------------|----------|
| Архитектура | Модульный монолит |
| Целевые пользователи | 100,000+ |
| Concurrent users | 1,000+ |
| Target RPS | 1,000+ |
| Uptime SLA | 99.9% |
| Время запуска | ~30 секунд |
| Потребление памяти | ~1-2 GB |

---

## Возможности

### Обучение
- **Курсы и уроки** — видео, тексты, документы, SCORM
- **Квизы и тесты** — множественный выбор, свободный ответ
- **Траектории обучения** — связанные курсы с пререквизитами
- **Карьерные треки** — Junior → Senior с требованиями

### Геймификация
- **Баллы и XP** — за прохождение курсов и активность
- **Достижения и бейджи** — система наград
- **Лидерборды** — недельные, месячные, годовые
- **Командные челленджи** — соревнования между отделами
- **Виртуальная валюта** — обмен на награды
- **Стрики** — непрерывные дни обучения

### Аналитика
- **Дашборды** — персональные и для менеджеров
- **ROI обучения** — оценка эффективности инвестиций
- **Heatmap активности** — визуализация вовлечённости
- **Воронки курсов** — анализ отсева
- **BI-экспорт** — Excel, PDF, CSV, JSON

### Социальное обучение
- **Q&A форум** — вопросы и ответы
- **Учебные группы** — совместное обучение
- **Менторинг** — подбор менторов, сессии
- **Peer-контент** — UGC от сотрудников

### Compliance
- **Обязательные курсы** — автоназначение
- **Сертификаты** — с датой истечения
- **Аудит-лог** — все действия пользователей
- **Электронные подписи** — для документов

### Интеграции
- **HR-системы** — синхронизация сотрудников
- **Календари** — Google, Outlook, CalDAV
- **SSO** — LDAP, Active Directory, OAuth2
- **Видеоконференции** — Zoom, Teams, Jitsi

---

## Архитектура

### Модульный монолит

FREE LMS использует модульную монолитную архитектуру — золотую середину между монолитом и микросервисами:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         FREE LMS MONOLITH                                │
│                                                                          │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │
│  │    Auth     │  │   Course    │  │ Enrollment  │  │   Payment   │    │
│  │   Module    │  │   Module    │  │   Module    │  │   Module    │    │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘    │
│         │                │                │                │            │
│         └────────────────┴────────────────┴────────────────┘            │
│                                   │                                      │
│                          ┌────────▼────────┐                            │
│                          │  Common Module  │                            │
│                          │  (Security, DTO,│                            │
│                          │   Exceptions)   │                            │
│                          └─────────────────┘                            │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
        ┌───────────────────────────┼───────────────────────────┐
        │                           │                           │
   ┌────▼────┐                 ┌────▼────┐                 ┌────▼────┐
   │PostgreSQL│                │  Redis  │                 │  Kafka  │
   │    16   │                 │    7    │                 │ (async) │
   └─────────┘                 └─────────┘                 └─────────┘
```

### Преимущества модульного монолита

| Аспект | Микросервисы | Модульный монолит |
|--------|--------------|-------------------|
| Время запуска | 3-5 минут | ~30 секунд |
| Потребление памяти | 8-16 GB | 1-2 GB |
| Latency вызовов | 5-50ms (сеть) | <1ms (память) |
| Количество контейнеров | 35+ | 7 |
| Сложность деплоя | Высокая | Низкая |
| Отладка | Сложная | Простая |

### Технологический стек

| Уровень | Технологии |
|---------|-----------|
| **Backend** | Java 21, Spring Boot 3.2.5 |
| **Базы данных** | PostgreSQL 16, Redis 7, MongoDB 7 |
| **Messaging** | Apache Kafka |
| **Search** | Elasticsearch 8.11 |
| **Storage** | MinIO (S3-compatible) |
| **Infrastructure** | Docker, Kubernetes |
| **Monitoring** | Prometheus, Grafana, ELK |

### Модули приложения

```
backend-java/monolith/
├── src/main/java/com/freelms/lms/
│   ├── auth/           # Аутентификация, пользователи, JWT
│   ├── course/         # Курсы, модули, уроки, тесты
│   ├── enrollment/     # Записи, прогресс, сертификаты
│   ├── payment/        # Платежи, подписки
│   ├── common/         # Общие компоненты
│   │   ├── dto/        # ApiResponse, PagedResponse
│   │   ├── entity/     # BaseEntity
│   │   ├── enums/      # UserRole, CourseStatus, etc.
│   │   ├── exception/  # GlobalExceptionHandler
│   │   └── security/   # JWT, Filters
│   └── config/         # SecurityConfig, RedisConfig, etc.
```

---

## Быстрый старт

### Требования

- Java 21+
- Maven 3.9+
- Docker & Docker Compose
- 4GB+ RAM (рекомендуется 8GB)

### Вариант 1: Docker Compose (рекомендуется)

```bash
# Клонирование репозитория
git clone https://github.com/your-org/free-lms.git
cd free-lms

# Запуск всей системы
docker-compose -f docker-compose.monolith.yml up -d

# Проверка статуса
docker-compose -f docker-compose.monolith.yml ps

# Просмотр логов
docker-compose -f docker-compose.monolith.yml logs -f app
```

### Вариант 2: Локальный запуск

```bash
# Запуск инфраструктуры
docker-compose -f docker-compose.monolith.yml up -d postgres redis kafka

# Сборка проекта
cd backend-java/monolith
mvn clean package -DskipTests

# Запуск приложения
java -jar target/free-lms-monolith-1.0.0-SNAPSHOT.jar
```

### Проверка работы

После запуска доступны:

| Сервис | URL |
|--------|-----|
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Health Check | http://localhost:8080/actuator/health |
| Prometheus Metrics | http://localhost:8080/actuator/prometheus |

---

## Документация

### Структура проекта

```
free-lms/
├── backend-java/
│   ├── monolith/               # Основное приложение
│   │   ├── src/
│   │   │   ├── main/java/      # Java код
│   │   │   └── test/           # Тесты
│   │   ├── Dockerfile
│   │   └── pom.xml
│   ├── bots/                   # Messenger боты
│   │   ├── telegram/           # Telegram бот (Java)
│   │   └── whatsapp/           # WhatsApp бот (Twilio)
│   ├── monitoring/             # Prometheus, Grafana, Alertmanager
│   ├── backup/                 # Скрипты резервного копирования
│   └── pom.xml
├── docs/
│   ├── QUICK_START_GUIDE.md    # Пошаговое руководство
│   ├── TROUBLESHOOTING.md      # Решение проблем
│   └── runbooks/               # Операционные руководства
├── docker-compose.monolith.yml # Production compose
├── CONTRIBUTING.md             # Руководство контрибьютора
├── SECURITY.md                 # Политика безопасности
└── README.md
```

### API Документация

После запуска доступна по адресу:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### Основные API Endpoints

```http
# Аутентификация
POST   /api/v1/auth/register    # Регистрация
POST   /api/v1/auth/login       # Вход
POST   /api/v1/auth/refresh     # Обновление токена
GET    /api/v1/auth/me          # Текущий пользователь

# Курсы
GET    /api/v1/courses          # Список курсов
GET    /api/v1/courses/{id}     # Детали курса
POST   /api/v1/courses          # Создать курс
PUT    /api/v1/courses/{id}     # Обновить курс
DELETE /api/v1/courses/{id}     # Удалить курс

# Записи на курсы
POST   /api/v1/enrollments/courses/{id}  # Записаться
GET    /api/v1/enrollments/my            # Мои записи
PUT    /api/v1/enrollments/{id}/progress # Обновить прогресс

# Сертификаты
GET    /api/v1/certificates/my           # Мои сертификаты
GET    /api/v1/certificates/{id}/verify  # Проверить сертификат

# Платежи
POST   /api/v1/payments                  # Создать платёж
GET    /api/v1/payments/{id}             # Статус платежа
```

---

## Безопасность

### Реализовано

- JWT аутентификация с refresh tokens
- Rate limiting
- Security headers (CSP, HSTS, X-Frame-Options)
- Input validation и sanitization
- SQL injection protection (JPA)
- Audit logging

### Production Checklist

- [ ] Изменить все пароли в конфигурации
- [ ] Сгенерировать 256-bit JWT secret
- [ ] Настроить HTTPS/TLS
- [ ] Включить WAF
- [ ] Настроить backup баз данных

---

## Мониторинг

### Endpoints

| Endpoint | Описание |
|----------|----------|
| `/actuator/health` | Health check |
| `/actuator/metrics` | Prometheus metrics |
| `/actuator/info` | Application info |

### Логирование

```bash
# Просмотр логов в Docker
docker-compose -f docker-compose.monolith.yml logs -f app

# Логи с фильтрацией
docker-compose -f docker-compose.monolith.yml logs -f app | grep ERROR
```

---

## База данных

### Единая схема

Все данные хранятся в единой PostgreSQL базе данных с Flyway миграциями:

```
freelms/
├── users                  # Пользователи
├── refresh_tokens         # Refresh токены
├── courses                # Курсы
├── course_modules         # Модули курсов
├── lessons                # Уроки
├── categories             # Категории
├── enrollments            # Записи на курсы
├── lesson_progress        # Прогресс уроков
├── certificates           # Сертификаты
├── payments               # Платежи
├── quizzes                # Тесты
├── quiz_attempts          # Попытки прохождения тестов
├── notifications          # Уведомления
├── badges                 # Бейджи
├── user_badges            # Бейджи пользователей
└── audit_logs             # Аудит-логи
```

### Миграции

```bash
# Миграции применяются автоматически при запуске
# Для ручного применения:
mvn flyway:migrate
```

---

## Разработка

### Code Style

- Google Java Style Guide
- Constructor injection (не @Autowired на поля)
- Lombok для сокращения boilerplate

### Тестирование

```bash
# Unit tests
cd backend-java/monolith
mvn test

# Integration tests
mvn verify -Pintegration

# Тест с покрытием
mvn test jacoco:report
```

---

## Roadmap

### Completed
- [x] Модульная монолитная архитектура
- [x] JWT аутентификация
- [x] Управление курсами
- [x] Система записей и прогресса
- [x] Сертификаты
- [x] Платежи (Stripe)
- [x] Redis кэширование
- [x] Kafka события

### Planned
- [ ] Mobile apps (iOS, Android)
- [ ] AI-powered recommendations
- [ ] xAPI (Tin Can) integration
- [ ] GraphQL API
- [ ] Multi-tenancy improvements

---

## Участие в разработке

1. Fork репозитория
2. Создайте feature branch (`git checkout -b feature/amazing`)
3. Commit изменения (`git commit -m 'Add amazing feature'`)
4. Push в branch (`git push origin feature/amazing`)
5. Создайте Pull Request

Подробнее в [CONTRIBUTING.md](CONTRIBUTING.md)

---

## Лицензия

MIT License - см. [LICENSE](LICENSE) файл.

---

## Контакты

- Website: [www.smartup24.com](https://www.smartup24.com)
- Email: opensource@smartup24.com
- GitHub Issues: [Issues](https://github.com/your-org/free-lms/issues)

---

<div align="center">

**FREE LMS** — Made with care for Enterprise Learning

</div>
