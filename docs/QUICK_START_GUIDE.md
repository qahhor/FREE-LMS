# FREE LMS — Пошаговое руководство по развертыванию

## Для кого это руководство?

Это руководство написано простым языком для:
- Начинающих DevOps-специалистов
- Студентов и преподавателей
- Менеджеров, желающих понять процесс
- Любого, кто хочет запустить собственную LMS

**Время на развертывание:** 15-30 минут

---

## Содержание

1. [Что такое FREE LMS?](#1-что-такое-free-lms)
2. [Что нужно для запуска?](#2-что-нужно-для-запуска)
3. [Подготовка компьютера](#3-подготовка-компьютера)
4. [Скачивание проекта](#4-скачивание-проекта)
5. [Быстрый запуск (Docker)](#5-быстрый-запуск-docker)
6. [Проверка работы](#6-проверка-работы)
7. [Первоначальная настройка](#7-первоначальная-настройка)
8. [Запуск для разработки](#8-запуск-для-разработки)
9. [Частые проблемы и решения](#9-частые-проблемы-и-решения)
10. [Следующие шаги](#10-следующие-шаги)

---

## 1. Что такое FREE LMS?

**FREE LMS** — это система управления обучением (Learning Management System), которая позволяет:

| Возможность | Описание |
|-------------|----------|
| Создавать курсы | Видео, текст, тесты, задания |
| Управлять пользователями | Студенты, преподаватели, администраторы |
| Отслеживать прогресс | Статистика, отчёты, аналитика |
| Геймификация | Баллы, достижения, рейтинги |
| Мультитенантность | Несколько организаций в одной системе |

### Архитектура простыми словами

```
┌─────────────────────────────────────────────────────────────┐
│                     ВАШ БРАУЗЕР                             │
│              (Chrome, Firefox, Safari)                      │
└─────────────────────────┬───────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                    FREE LMS APPLICATION                      │
│                    (Модульный монолит)                      │
│                       Порт 8080                             │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐       │
│  │  Auth   │  │ Course  │  │Enrollment│  │ Payment │       │
│  │ Module  │  │ Module  │  │  Module  │  │ Module  │       │
│  └─────────┘  └─────────┘  └─────────┘  └─────────┘       │
└─────────────────────────┬───────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                    БАЗА ДАННЫХ                               │
│              (PostgreSQL — хранит все данные)               │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Что нужно для запуска?

### Минимальные требования к компьютеру

| Компонент | Минимум | Рекомендуется |
|-----------|---------|---------------|
| Процессор | 2 ядра | 4+ ядра |
| Оперативная память | 4 GB | 8 GB |
| Свободное место | 10 GB | 20 GB SSD |
| Интернет | Стабильный | Стабильный |

### Операционная система

Поддерживаются:
- **Windows 10/11** (с WSL2)
- **macOS** (10.15+)
- **Linux** (Ubuntu 20.04+, CentOS 8+)

---

## 3. Подготовка компьютера

### Шаг 3.1: Установка Docker

**Docker** — это программа, которая позволяет запускать приложения в изолированных контейнерах.

#### Windows

1. Скачайте Docker Desktop: [docker.com/products/docker-desktop](https://www.docker.com/products/docker-desktop/)
2. Установите и перезагрузите компьютер
3. Проверьте: `docker --version`

#### macOS

1. Скачайте Docker Desktop для Mac
2. Установите и запустите
3. Проверьте: `docker --version`

#### Linux (Ubuntu/Debian)

```bash
# Установка Docker
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER

# Перезагрузите систему
# Проверка
docker --version
```

### Шаг 3.2: Установка Git

```bash
# Windows: скачайте с git-scm.com
# macOS: xcode-select --install
# Linux: sudo apt install -y git

git --version
```

---

## 4. Скачивание проекта

```bash
# Переходим в домашнюю папку
cd ~

# Клонируем репозиторий
git clone https://github.com/your-org/free-lms.git

# Переходим в папку проекта
cd free-lms
```

---

## 5. Быстрый запуск (Docker)

Это самый простой способ запустить проект.

### Шаг 5.1: Запустите все сервисы

```bash
# Запускаем все контейнеры в фоновом режиме
docker-compose -f docker-compose.monolith.yml up -d
```

> Первый запуск займёт 3-5 минут — Docker скачивает образы.

### Шаг 5.2: Проверьте статус контейнеров

```bash
docker-compose -f docker-compose.monolith.yml ps
```

Вы должны увидеть:

```
NAME                    STATUS          PORTS
freelms-app             Up (healthy)    0.0.0.0:8080->8080
freelms-postgres        Up (healthy)    5432
freelms-redis           Up (healthy)    6379
freelms-kafka           Up              9092
```

### Шаг 5.3: Дождитесь полного запуска

```bash
# Проверяем логи приложения
docker-compose -f docker-compose.monolith.yml logs -f app
```

Когда увидите строку `Started FreeLmsApplication` — система готова!

---

## 6. Проверка работы

### Шаг 6.1: Health Check

```bash
curl http://localhost:8080/actuator/health
```

Ожидаемый ответ:
```json
{"status":"UP"}
```

### Шаг 6.2: Swagger UI

Откройте в браузере:
```
http://localhost:8080/swagger-ui.html
```

Здесь можно посмотреть и протестировать все API endpoints.

---

## 7. Первоначальная настройка

### Шаг 7.1: Создайте первого администратора

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "Admin123!",
    "firstName": "Администратор",
    "lastName": "Системы"
  }'
```

### Шаг 7.2: Войдите в систему

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "Admin123!"
  }'
```

В ответе вы получите токен доступа (accessToken).

---

## 8. Запуск для разработки

### Шаг 8.1: Установите Java 21

```bash
# Windows: скачайте с adoptium.net
# macOS: brew install openjdk@21
# Linux: sudo apt install -y openjdk-21-jdk

java -version
```

### Шаг 8.2: Установите Maven

```bash
# macOS: brew install maven
# Linux: sudo apt install -y maven

mvn -version
```

### Шаг 8.3: Запустите только инфраструктуру

```bash
docker-compose -f docker-compose.monolith.yml up -d postgres redis kafka
```

### Шаг 8.4: Соберите и запустите приложение

```bash
cd backend-java/monolith

# Сборка
mvn clean package -DskipTests

# Запуск
mvn spring-boot:run
```

---

## 9. Частые проблемы и решения

### Порт уже занят

```bash
# Найти процесс
lsof -i :8080  # macOS/Linux
netstat -ano | findstr :8080  # Windows

# Остановить процесс
kill -9 <PID>
```

### Docker не запускается

- Убедитесь, что Docker Desktop запущен
- Перезапустите Docker

### Не хватает памяти

Docker Desktop → Settings → Resources → увеличьте Memory до 4-8 GB

### Ошибка при сборке Maven

```bash
# Очистить кэш Maven
rm -rf ~/.m2/repository
mvn clean install -DskipTests -U
```

---

## 10. Следующие шаги

### Изучите документацию

| Документ | Описание |
|----------|----------|
| [README.md](../README.md) | Общее описание проекта |
| [ARCHITECTURE.md](ARCHITECTURE.md) | Архитектура системы |
| [DEPLOYMENT.md](../backend-java/DEPLOYMENT.md) | Production развертывание |

### Настройте под себя

1. Измените пароли в конфигурации
2. Настройте email для уведомлений
3. Добавьте SSL сертификат для HTTPS

### Полезные URL

| Сервис | URL |
|--------|-----|
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Health Check | http://localhost:8080/actuator/health |

---

## Нужна помощь?

- **Документация:** Папка `docs/` в проекте
- **Issues:** Создайте issue на GitHub
- **Email:** opensource@smartup24.com

---

**Поздравляем!** Если вы дочитали до этого момента и успешно запустили проект — у вас есть собственная система управления обучением!

---

<div align="center">

**FREE LMS** — Обучение должно быть доступным каждому

</div>
