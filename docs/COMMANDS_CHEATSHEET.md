# 📋 Smartup LMS — Шпаргалка по командам

Краткий справочник самых нужных команд для работы с проектом.

---

## 🐳 Docker команды

### Запуск и остановка

```bash
# Запустить все сервисы
docker compose up -d

# Остановить все сервисы
docker compose down

# Перезапустить все сервисы
docker compose restart

# Перезапустить один сервис
docker compose restart auth-service
```

### Просмотр статуса

```bash
# Список всех контейнеров
docker compose ps

# Список только запущенных
docker compose ps --filter "status=running"

# Использование ресурсов
docker stats
```

### Логи

```bash
# Логи всех сервисов
docker compose logs

# Логи конкретного сервиса
docker compose logs auth-service

# Следить за логами в реальном времени
docker compose logs -f gateway-service

# Последние 100 строк
docker compose logs --tail=100 auth-service
```

### Очистка

```bash
# Удалить остановленные контейнеры
docker compose down

# Удалить контейнеры и volumes (ОСТОРОЖНО: удалит данные!)
docker compose down -v

# Удалить неиспользуемые образы
docker image prune -a

# Полная очистка (освободить место)
docker system prune -a
```

---

## 🔨 Maven команды

### Сборка

```bash
# Собрать весь проект
mvn clean package

# Собрать без тестов (быстрее)
mvn clean package -DskipTests

# Собрать конкретный модуль
mvn clean package -pl services/auth-service -am
```

### Запуск

```bash
# Запустить Spring Boot приложение
mvn spring-boot:run

# Запустить с профилем
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Тестирование

```bash
# Запустить все тесты
mvn test

# Запустить тесты конкретного модуля
mvn test -pl services/auth-service

# Запустить один тест
mvn test -Dtest=UserServiceTest
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

## 🗄️ База данных

### PostgreSQL через Docker

```bash
# Подключиться к PostgreSQL
docker exec -it freelms-postgres psql -U lms_user -d freelms_auth

# Выполнить SQL файл
docker exec -i freelms-postgres psql -U lms_user -d freelms_auth < script.sql
```

### Полезные SQL команды

```sql
-- Список баз данных
\l

-- Подключиться к базе
\c freelms_auth

-- Список таблиц
\dt

-- Описание таблицы
\d users

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

## 🌐 API тестирование (curl)

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
curl -X GET http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Health проверки

```bash
# Gateway health
curl http://localhost:8080/actuator/health

# Проверить все сервисы
for port in 8081 8082 8083 8084 8085; do
  echo "Port $port:"
  curl -s http://localhost:$port/actuator/health | jq .status
done
```

---

## 📊 Мониторинг

### Логи в реальном времени

```bash
# Все ошибки
docker compose logs -f | grep -i error

# Конкретный сервис
docker compose logs -f auth-service | grep -i "login\|error"
```

### Метрики

```bash
# Prometheus метрики
curl http://localhost:8080/actuator/prometheus

# JVM память
curl http://localhost:8080/actuator/metrics/jvm.memory.used
```

---

## 🧪 Нагрузочное тестирование

```bash
# Перейти в папку тестов
cd backend-java/load-testing/gatling

# Запустить тесты
mvn gatling:test -DbaseUrl=http://localhost:8080 -Dusers=100

# Smoke test
mvn gatling:test -Dgatling.simulationClass=freelms.SmokeTest
```

---

## 🔧 Git команды

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

## ⚡ Быстрые действия

### Полный перезапуск

```bash
cd backend-java
docker compose down
docker compose up -d
docker compose logs -f
```

### Пересборка одного сервиса

```bash
docker compose build auth-service
docker compose up -d auth-service
```

### Проверить что всё работает

```bash
# Быстрая проверка
curl -s http://localhost:8080/actuator/health | jq .

# Детальная проверка
docker compose ps
curl http://localhost:8761  # Eureka
curl http://localhost:8080/actuator/health  # Gateway
```

### Освободить ресурсы

```bash
# Остановить всё
docker compose down

# Удалить неиспользуемое
docker system prune -f
```

---

## 🆘 Экстренные команды

### Всё сломалось — начать сначала

```bash
cd backend-java

# Остановить и удалить всё
docker compose down -v

# Удалить все образы проекта
docker images | grep freelms | awk '{print $3}' | xargs docker rmi -f

# Запустить заново
docker compose up -d --build
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

## 📱 Полезные URL

| Сервис | URL | Логин/Пароль |
|--------|-----|--------------|
| API Gateway | http://localhost:8080 | - |
| Eureka Dashboard | http://localhost:8761 | eureka / eureka123 |
| Swagger UI | http://localhost:8080/swagger-ui.html | - |
| MinIO Console | http://localhost:9001 | minioadmin / minioadmin |

---

<div align="center">

📌 **Сохраните эту шпаргалку!**

</div>
