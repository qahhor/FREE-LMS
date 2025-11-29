# FREE LMS — Руководство по устранению неполадок

Подробное руководство по решению типичных проблем при развертывании и работе с FREE LMS.

---

## Содержание

1. [Проблемы с Docker](#1-проблемы-с-docker)
2. [Проблемы с базой данных](#2-проблемы-с-базой-данных)
3. [Проблемы с приложением](#3-проблемы-с-приложением)
4. [Проблемы с памятью](#4-проблемы-с-памятью)
5. [Проблемы с сетью](#5-проблемы-с-сетью)
6. [Проблемы со сборкой](#6-проблемы-со-сборкой)
7. [Проблемы с авторизацией](#7-проблемы-с-авторизацией)

---

## 1. Проблемы с Docker

### Docker не запускается

**Симптомы:**
```
Cannot connect to the Docker daemon at unix:///var/run/docker.sock
```

**Решение для Windows/macOS:**
1. Откройте Docker Desktop
2. Дождитесь, пока иконка в трее станет зелёной
3. Если не помогло — перезапустите Docker Desktop

**Решение для Linux:**
```bash
# Проверить статус
sudo systemctl status docker

# Запустить Docker
sudo systemctl start docker

# Включить автозапуск
sudo systemctl enable docker

# Добавить себя в группу docker
sudo usermod -aG docker $USER
# После этого — выйдите и войдите в систему
```

---

### Ошибка "Permission denied"

**Симптомы:**
```
Got permission denied while trying to connect to the Docker daemon socket
```

**Решение:**
```bash
# Добавить пользователя в группу docker
sudo usermod -aG docker $USER

# Применить изменения без перезагрузки
newgrp docker

# Или просто перезагрузите компьютер
```

---

### Контейнер не запускается

**Симптомы:**
```
Container freelms-app exited with code 1
```

**Диагностика:**
```bash
# Посмотреть логи проблемного контейнера
docker-compose -f docker-compose.monolith.yml logs app

# Посмотреть последние события
docker events --since="5m"
```

**Частые причины:**
1. Не хватает памяти — см. раздел "Проблемы с памятью"
2. Порт занят — см. раздел "Проблемы с сетью"
3. База данных не готова — подождите и перезапустите

---

## 2. Проблемы с базой данных

### PostgreSQL не запускается

**Симптомы:**
```
FATAL: password authentication failed for user "lms_user"
```

**Решение:**
```bash
# Удалить старые данные и пересоздать
docker-compose -f docker-compose.monolith.yml down -v
docker-compose -f docker-compose.monolith.yml up -d postgres

# Подождать 30 секунд
sleep 30

# Проверить
docker-compose -f docker-compose.monolith.yml logs postgres
```

---

### Ошибка подключения к БД

**Симптомы:**
```
Connection refused to postgres:5432
```

**Диагностика:**
```bash
# Проверить статус PostgreSQL
docker-compose -f docker-compose.monolith.yml ps postgres

# Проверить, слушает ли порт
docker exec freelms-postgres pg_isready -U lms_user

# Посмотреть логи
docker-compose -f docker-compose.monolith.yml logs postgres | tail -50
```

**Решение:**
```bash
# Перезапустить PostgreSQL
docker-compose -f docker-compose.monolith.yml restart postgres

# Подождать и проверить
sleep 30
docker exec freelms-postgres pg_isready -U lms_user
```

---

### Redis не работает

**Симптомы:**
```
Error connecting to Redis on localhost:6379
```

**Диагностика:**
```bash
# Проверить статус
docker-compose -f docker-compose.monolith.yml ps redis

# Попробовать подключиться
docker exec -it freelms-redis redis-cli PING
```

**Решение:**
```bash
docker-compose -f docker-compose.monolith.yml restart redis
```

---

## 3. Проблемы с приложением

### Приложение не запускается

**Симптомы:**
- В логах нет `Started FreeLmsApplication`
- HTTP 503 на всех endpoints

**Диагностика:**
```bash
# Посмотреть логи
docker-compose -f docker-compose.monolith.yml logs app | tail -100

# Проверить health
curl http://localhost:8080/actuator/health
```

**Частые причины и решения:**

1. **База данных не готова:**
```bash
# Подождать запуска postgres
sleep 30
docker-compose -f docker-compose.monolith.yml restart app
```

2. **Неверные настройки:**
```bash
# Проверить переменные окружения
docker-compose -f docker-compose.monolith.yml config
```

---

### Приложение запускается долго

**Симптомы:**
- Контейнер в статусе "starting" более 2 минут

**Причины и решения:**

1. **Мало памяти:**
```bash
# Проверить использование памяти
docker stats --no-stream
```

2. **Медленный диск:**
- Используйте SSD вместо HDD

---

### HTTP 500 ошибки

**Диагностика:**
```bash
# Посмотреть логи ошибок
docker-compose -f docker-compose.monolith.yml logs app | grep -i "error\|exception"
```

**Решение:**
- Проверьте stack trace в логах
- Убедитесь, что все зависимости (postgres, redis, kafka) работают

---

## 4. Проблемы с памятью

### Контейнеры убиваются (OOM Killed)

**Симптомы:**
```
Container killed due to memory limit
```

**Диагностика:**
```bash
# Проверить использование памяти
docker stats
```

**Решение для Docker Desktop (Windows/macOS):**
1. Откройте Docker Desktop
2. Перейдите в Settings → Resources
3. Увеличьте Memory до 4-8 GB
4. Нажмите "Apply & Restart"

**Решение для Linux:**
```bash
# Проверить доступную память
free -h

# Создать swap файл (если нет)
sudo fallocate -l 4G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
```

---

### Java OutOfMemoryError

**Симптомы:**
```
java.lang.OutOfMemoryError: Java heap space
```

**Решение:**

Установите переменные окружения в docker-compose:
```yaml
services:
  app:
    environment:
      JAVA_OPTS: "-Xmx1g -Xms512m"
```

Или для локальной разработки:
```bash
export JAVA_OPTS="-Xmx2g -Xms1g"
mvn spring-boot:run
```

---

## 5. Проблемы с сетью

### Порт уже занят

**Симптомы:**
```
Bind for 0.0.0.0:8080 failed: port is already allocated
```

**Диагностика:**

**Windows:**
```powershell
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

**macOS/Linux:**
```bash
lsof -i :8080
kill -9 <PID>
```

**Решение через Docker:**
```bash
# Остановить все контейнеры
docker-compose -f docker-compose.monolith.yml down

# Проверить, не остались ли контейнеры
docker ps -a | grep freelms
```

---

### Не работает localhost

**Симптомы:**
- В браузере "Не удается получить доступ к сайту"

**Проверки:**

1. **Docker запущен:**
```bash
docker ps
```

2. **Контейнер работает:**
```bash
docker-compose -f docker-compose.monolith.yml ps
```

3. **Проверить порт:**
```bash
curl http://localhost:8080/actuator/health
```

---

## 6. Проблемы со сборкой

### Maven не находит зависимости

**Симптомы:**
```
Could not resolve dependencies for project
```

**Решение:**
```bash
# Очистить кэш Maven
rm -rf ~/.m2/repository/com/freelms

# Пересобрать с обновлением
mvn clean install -DskipTests -U
```

---

### Ошибка компиляции Java

**Симптомы:**
```
error: release version 21 not supported
```

**Решение:**
```bash
# Проверить версию Java
java -version

# Должно быть 21+
# Если нет — установите Java 21

# Убедитесь, что JAVA_HOME указывает на Java 21
echo $JAVA_HOME
```

---

### Lombok не работает

**Симптомы:**
```
cannot find symbol: method getEmail()
```

**Решение:**
1. Убедитесь, что Lombok установлен в IDE
2. Включите Annotation Processing:
   - IntelliJ: Settings → Build → Compiler → Annotation Processors → Enable
3. Пересоберите:
```bash
mvn clean compile
```

---

## 7. Проблемы с авторизацией

### 401 Unauthorized

**Симптомы:**
```json
{"error": "Unauthorized", "status": 401}
```

**Причины:**
1. Токен истёк
2. Токен неверный
3. Токен не передан

**Решение:**
```bash
# Получите новый токен
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@test.com","password":"Test123!"}'

# Используйте токен из ответа
curl http://localhost:8080/api/v1/auth/me \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

---

### 403 Forbidden

**Симптомы:**
```json
{"error": "Forbidden", "status": 403}
```

**Причина:** Недостаточно прав доступа.

**Решение:**
1. Проверьте роль пользователя
2. Для admin endpoints нужна роль ADMIN

---

### Токен не работает после перезапуска

**Причина:** JWT secret изменился.

**Решение:**
```bash
# Убедитесь, что JWT_SECRET постоянный
# Получите новый токен после перезапуска
```

---

## Последний resort — полный сброс

Если ничего не помогает:

```bash
# 1. Остановить всё
docker-compose -f docker-compose.monolith.yml down -v

# 2. Удалить все образы проекта
docker images | grep freelms | awk '{print $3}' | xargs docker rmi -f 2>/dev/null

# 3. Очистить Docker
docker system prune -af
docker volume prune -f

# 4. Удалить кэш Maven
rm -rf ~/.m2/repository/com/freelms

# 5. Собрать заново
cd backend-java/monolith
mvn clean package -DskipTests

# 6. Запустить
cd ../..
docker-compose -f docker-compose.monolith.yml up -d --build

# 7. Следить за логами
docker-compose -f docker-compose.monolith.yml logs -f
```

---

## Всё ещё не работает?

1. **Соберите информацию:**
```bash
docker-compose -f docker-compose.monolith.yml logs > logs.txt
docker-compose -f docker-compose.monolith.yml ps > status.txt
java -version > versions.txt
docker --version >> versions.txt
```

2. **Создайте issue на GitHub** с приложенными файлами

3. **Напишите на opensource@smartup24.com**

---

<div align="center">

**Удачи в решении проблем!**

</div>
