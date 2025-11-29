# FREE LMS — Шпаргалка по командам

Краткий справочник самых нужных команд для работы с проектом.

---

## Docker команды

### Запуск и остановка

```bash
# Запустить все сервисы
docker-compose -f docker-compose.monolith.yml up -d

# Остановить все сервисы
docker-compose -f docker-compose.monolith.yml down

# Перезапустить все сервисы
docker-compose -f docker-compose.monolith.yml restart

# Перезапустить приложение
docker-compose -f docker-compose.monolith.yml restart app
```

### Просмотр статуса

```bash
# Список всех контейнеров
docker-compose -f docker-compose.monolith.yml ps

# Использование ресурсов
docker stats
```

### Логи

```bash
# Логи приложения
docker-compose -f docker-compose.monolith.yml logs app

# Следить за логами в реальном времени
docker-compose -f docker-compose.monolith.yml logs -f app

# Последние 100 строк
docker-compose -f docker-compose.monolith.yml logs --tail=100 app
```

### Очистка

```bash
# Удалить остановленные контейнеры
docker-compose -f docker-compose.monolith.yml down

# Удалить контейнеры и volumes (ОСТОРОЖНО: удалит данные!)
docker-compose -f docker-compose.monolith.yml down -v

# Удалить неиспользуемые образы
docker image prune -a

# Полная очистка (освободить место)
docker system prune -a
```

---

## Maven команды

### Сборка

```bash
cd backend-java/monolith

# Собрать проект
mvn clean package

# Собрать без тестов (быстрее)
mvn clean package -DskipTests

# Собрать с обновлением зависимостей
mvn clean package -U
```

### Запуск

```bash
# Запустить Spring Boot приложение
mvn spring-boot:run

# Запустить с профилем
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Запустить JAR
java -jar target/free-lms-monolith-1.0.0-SNAPSHOT.jar
```

### Тестирование

```bash
# Запустить все тесты
mvn test

# Запустить один тест
mvn test -Dtest=UserServiceTest

# Запустить тесты с отчётом о покрытии
mvn test jacoco:report
```

### Зависимости

```bash
# Показать дерево зависимостей
mvn dependency:tree

# Обновить зависимости
mvn versions:display-dependency-updates

# Скачать зависимости
mvn dependency:go-offline
```

---

## База данных

### PostgreSQL через Docker

```bash
# Подключиться к PostgreSQL
docker exec -it freelms-postgres psql -U lms_user -d freelms

# Выполнить SQL файл
docker exec -i freelms-postgres psql -U lms_user -d freelms < script.sql
```

### Полезные SQL команды

```sql
-- Список таблиц
\dt

-- Описание таблицы
\d users

-- Количество пользователей
SELECT count(*) FROM users;

-- Выход
\q
```

### Redis через Docker

```bash
# Подключиться к Redis
docker exec -it freelms-redis redis-cli

# Проверить соединение
PING

# Показать все ключи
KEYS *

# Очистить кэш
FLUSHALL
```

---

## API тестирование (curl)

### Авторизация

```bash
# Регистрация
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@test.com","password":"Test123!","firstName":"Test","lastName":"User"}'

# Вход
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@test.com","password":"Test123!"}'

# Использование токена
curl -X GET http://localhost:8080/api/v1/auth/me \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Health проверки

```bash
# Health check
curl http://localhost:8080/actuator/health

# Liveness
curl http://localhost:8080/actuator/health/liveness

# Readiness
curl http://localhost:8080/actuator/health/readiness
```

---

## Мониторинг

### Логи в реальном времени

```bash
# Все ошибки
docker-compose -f docker-compose.monolith.yml logs -f | grep -i error

# Логи приложения
docker-compose -f docker-compose.monolith.yml logs -f app | grep -i "login\|error"
```

### Метрики

```bash
# Prometheus метрики
curl http://localhost:8080/actuator/prometheus

# JVM память
curl http://localhost:8080/actuator/metrics/jvm.memory.used
```

---

## Git команды

```bash
# Статус изменений
git status

# Скачать обновления
git pull origin main

# Создать ветку
git checkout -b feature/my-feature

# Зафиксировать изменения
git add .
git commit -m "Описание изменений"

# Отправить на сервер
git push origin feature/my-feature
```

---

## Быстрые действия

### Полный перезапуск

```bash
docker-compose -f docker-compose.monolith.yml down
docker-compose -f docker-compose.monolith.yml up -d
docker-compose -f docker-compose.monolith.yml logs -f
```

### Пересборка приложения

```bash
docker-compose -f docker-compose.monolith.yml build app
docker-compose -f docker-compose.monolith.yml up -d app
```

### Проверить что всё работает

```bash
# Быстрая проверка
curl -s http://localhost:8080/actuator/health | jq .

# Детальная проверка
docker-compose -f docker-compose.monolith.yml ps
curl http://localhost:8080/actuator/health
```

### Освободить ресурсы

```bash
# Остановить всё
docker-compose -f docker-compose.monolith.yml down

# Удалить неиспользуемое
docker system prune -f
```

---

## Экстренные команды

### Всё сломалось — начать сначала

```bash
# Остановить и удалить всё
docker-compose -f docker-compose.monolith.yml down -v

# Удалить все образы проекта
docker images | grep freelms | awk '{print $3}' | xargs docker rmi -f

# Запустить заново
docker-compose -f docker-compose.monolith.yml up -d --build
```

### Посмотреть что занимает порт

```bash
# Linux/macOS
lsof -i :8080

# Windows
netstat -ano | findstr :8080
```

### Проверить место на диске

```bash
# Docker использование
docker system df

# Детально
docker system df -v
```

---

## Полезные URL

| Сервис | URL |
|--------|-----|
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Health Check | http://localhost:8080/actuator/health |
| Prometheus Metrics | http://localhost:8080/actuator/prometheus |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |

---

<div align="center">

**Сохраните эту шпаргалку!**

</div>
