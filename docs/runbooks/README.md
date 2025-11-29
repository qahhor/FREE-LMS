# FREE LMS - Operations Runbooks

Операционные руководства для управления платформой FREE LMS.

## Структура

| Runbook | Описание | Severity |
|---------|----------|----------|
| [Application Operations](application-operations.md) | Управление приложением | Medium |
| [Database Operations](database-operations.md) | Операции с БД | Medium |
| [Incident Response](incident-response.md) | Реагирование на инциденты | High |

## Архитектура

FREE LMS использует **модульную монолитную архитектуру**:

```
┌─────────────────────────────────────────┐
│          FREE LMS Application           │
│              (Port 8080)                │
├─────────┬─────────┬─────────┬─────────┤
│  Auth   │ Course  │Enrollment│ Payment │
│ Module  │ Module  │  Module  │ Module  │
└─────────┴────┬────┴─────────┴─────────┘
               │
    ┌──────────┼──────────┐
    │          │          │
┌───▼───┐  ┌───▼───┐  ┌───▼───┐
│Postgres│  │ Redis │  │ Kafka │
│  :5432 │  │ :6379 │  │ :9092 │
└────────┘  └───────┘  └───────┘
```

## Компоненты

| Компонент | Порт | Описание |
|-----------|------|----------|
| Application | 8080 | FREE LMS Monolith |
| PostgreSQL | 5432 | Primary Database |
| Redis | 6379 | Cache |
| Kafka | 9092 | Event Streaming |
| Elasticsearch | 9200 | Search (optional) |
| MongoDB | 27017 | Documents (optional) |
| MinIO | 9000/9001 | Object Storage |

## Быстрые команды

### Health Check

```bash
# Проверка приложения
curl http://localhost:8080/actuator/health

# Liveness probe
curl http://localhost:8080/actuator/health/liveness

# Readiness probe
curl http://localhost:8080/actuator/health/readiness
```

### Логи

```bash
# Логи приложения
docker-compose -f docker-compose.monolith.yml logs -f app

# Логи с фильтрацией ошибок
docker-compose -f docker-compose.monolith.yml logs -f app | grep -i error
```

### Перезапуск

```bash
# Перезапуск приложения
docker-compose -f docker-compose.monolith.yml restart app

# Полный перезапуск
docker-compose -f docker-compose.monolith.yml down
docker-compose -f docker-compose.monolith.yml up -d
```

## On-Call Checklist

Перед началом дежурства:

- [ ] Доступ к Docker/Kubernetes
- [ ] Доступ к логам
- [ ] Доступ к базе данных
- [ ] Runbooks изучены
- [ ] Контакты эскалации известны

## Контакты

| Роль | Контакт |
|------|---------|
| Primary On-Call | PagerDuty rotation |
| Infrastructure Lead | infra@smartup24.com |
| Database Admin | dba@smartup24.com |
