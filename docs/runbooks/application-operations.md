# Runbook: Application Operations

## Overview

Процедуры управления FREE LMS приложением.

## Компоненты

FREE LMS — модульный монолит с следующими модулями:
- **Auth Module** — аутентификация, пользователи
- **Course Module** — курсы, уроки, тесты
- **Enrollment Module** — записи, прогресс, сертификаты
- **Payment Module** — платежи

---

## Health Check

### Базовая проверка

```bash
# Health endpoint
curl http://localhost:8080/actuator/health

# Ожидаемый ответ
{"status":"UP","components":{"db":{"status":"UP"},"redis":{"status":"UP"}}}
```

### Детальная проверка

```bash
# Все компоненты
curl http://localhost:8080/actuator/health | jq .

# Prometheus metrics
curl http://localhost:8080/actuator/prometheus
```

---

## Restart Procedures

### Docker Compose

```bash
# Перезапуск приложения
docker-compose -f docker-compose.monolith.yml restart app

# Проверка после перезапуска
sleep 30
curl http://localhost:8080/actuator/health
```

### Rolling Restart (Kubernetes)

```bash
# Trigger rolling restart
kubectl rollout restart deployment/freelms-app -n freelms

# Watch progress
kubectl rollout status deployment/freelms-app -n freelms

# Verify
kubectl get pods -n freelms -l app=freelms-app
```

---

## Log Analysis

### Просмотр логов

```bash
# Real-time logs
docker-compose -f docker-compose.monolith.yml logs -f app

# Last 100 lines
docker-compose -f docker-compose.monolith.yml logs --tail=100 app

# Filter errors
docker-compose -f docker-compose.monolith.yml logs app | grep -i error
```

### Частые ошибки

| Error | Причина | Решение |
|-------|---------|---------|
| `Connection refused postgres` | БД не готова | Подождите, перезапустите app |
| `JWT token expired` | Истёкший токен | Клиенту нужен refresh |
| `OutOfMemoryError` | Мало памяти | Увеличить heap |

---

## Scaling

### Docker Compose

Монолит масштабируется через Load Balancer:

```yaml
# nginx.conf
upstream freelms {
    server app1:8080;
    server app2:8080;
    server app3:8080;
}
```

### Kubernetes HPA

```bash
# Проверить текущее состояние
kubectl get hpa -n freelms

# Manual scale
kubectl scale deployment freelms-app --replicas=3 -n freelms
```

---

## Troubleshooting

### Приложение не запускается

1. Проверить логи:
```bash
docker-compose -f docker-compose.monolith.yml logs app | tail -50
```

2. Проверить зависимости:
```bash
docker-compose -f docker-compose.monolith.yml ps
```

3. Проверить конфигурацию:
```bash
docker-compose -f docker-compose.monolith.yml config
```

### High CPU

```bash
# Проверить использование
docker stats

# Проверить thread dump (если JMX включен)
jstack <pid>
```

### High Memory

```bash
# Проверить heap
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Heap dump (если нужен анализ)
docker exec freelms-app jmap -dump:format=b,file=/tmp/heap.hprof <pid>
```

---

## Rollback

### Docker Compose

```bash
# Откатить на предыдущий образ
docker-compose -f docker-compose.monolith.yml pull app
docker-compose -f docker-compose.monolith.yml up -d app
```

### Kubernetes

```bash
# Check rollout history
kubectl rollout history deployment/freelms-app -n freelms

# Rollback to previous version
kubectl rollout undo deployment/freelms-app -n freelms

# Verify
kubectl rollout status deployment/freelms-app -n freelms
```

---

## Configuration Changes

### Environment Variables

```bash
# Изменить в docker-compose.monolith.yml или .env
# Затем перезапустить

docker-compose -f docker-compose.monolith.yml up -d app
```

### Database Migrations

```bash
# Миграции применяются автоматически при запуске
# Для ручного применения:
cd backend-java/monolith
mvn liquibase:update

# Проверка статуса миграций
mvn liquibase:status

# Откат последней миграции
mvn liquibase:rollback -Dliquibase.rollbackCount=1
```

---

## Useful Commands

```bash
# Подключиться к контейнеру
docker exec -it freelms-app /bin/sh

# Проверить конфигурацию Spring
curl http://localhost:8080/actuator/env

# Проверить бины
curl http://localhost:8080/actuator/beans

# Thread dump
curl http://localhost:8080/actuator/threaddump
```
