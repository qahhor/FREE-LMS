# 🎓 FREE LMS - Learning Management System

Современная платформа для онлайн-обучения, созданная на **Node.js + NestJS** (Backend) и **Angular** (Frontend).

## 🌟 Особенности

- ✅ **Аутентификация и авторизация** (JWT)
- 📚 **Управление курсами** (создание, редактирование, публикация)
- 👥 **Управление пользователями** (роли: Admin, Instructor, Student)
- 📊 **Отслеживание прогресса** обучения
- 🎮 **Геймификация** (баллы, уровни, достижения)
- 📈 **Аналитика** для студентов и преподавателей
- 🎥 **Видео-контент** с поддержкой HLS стриминга
- 💳 **Интеграция платежей** (Payme, Click для Узбекистана)

## 🛠 Технологический стек

### Backend
- **NestJS** - прогрессивный Node.js framework
- **TypeScript** - типизированный JavaScript
- **TypeORM** - ORM для работы с БД
- **PostgreSQL** - основная база данных
- **Redis** - кэширование и сессии
- **JWT** - аутентификация
- **Swagger** - API документация
- **MinIO** - хранилище файлов (S3-compatible)

### Frontend
- **Angular 17+** - современный фреймворк
- **TypeScript** - типизированный JavaScript
- **Angular Material** - UI компоненты
- **RxJS** - реактивное программирование
- **NgRx** (опционально) - state management

### DevOps
- **Docker** & **Docker Compose** - контейнеризация
- **GitHub Actions** - CI/CD
- **PostgreSQL** - реляционная БД
- **Redis** - кэш и очереди
- **MinIO** - файловое хранилище

## 📋 Предварительные требования

Установите следующее ПО:

- **Node.js** v20+ ([скачать](https://nodejs.org/))
- **Docker** & **Docker Compose** ([скачать](https://www.docker.com/))
- **Git** ([скачать](https://git-scm.com/))

## 🚀 Быстрый старт

### 1. Клонирование репозитория

```bash
git clone <repository-url>
cd FREE_LMS
```

### 2. Настройка Backend

```bash
cd backend

# Установка зависимостей
npm install

# Копирование .env файла
cp .env.example .env

# Настройте переменные окружения в .env файле
```

### 3. Настройка Frontend

```bash
cd frontend

# Установка зависимостей
npm install
```

### 4. Запуск с Docker (рекомендуется)

Из корневой директории проекта:

```bash
# Запуск всех сервисов (PostgreSQL, Redis, MinIO, Backend, Frontend)
docker-compose up -d

# Просмотр логов
docker-compose logs -f

# Остановка сервисов
docker-compose down
```

Доступ к приложению:
- **Frontend**: http://localhost:4200
- **Backend API**: http://localhost:3000/api/v1
- **API Docs (Swagger)**: http://localhost:3000/api/docs
- **MinIO Console**: http://localhost:9001

### 5. Запуск без Docker (для разработки)

#### Запуск базы данных

```bash
# Запуск только PostgreSQL, Redis и MinIO
docker-compose up -d postgres redis minio
```

#### Запуск Backend

```bash
cd backend
npm run start:dev
```

Backend будет доступен на http://localhost:3000

#### Запуск Frontend

```bash
cd frontend
npm start
```

Frontend будет доступен на http://localhost:4200

## 📁 Структура проекта

```
FREE_LMS/
├── backend/                 # NestJS Backend
│   ├── src/
│   │   ├── common/         # Общие компоненты
│   │   │   ├── decorators/ # Декораторы
│   │   │   ├── guards/     # Guards для авторизации
│   │   │   ├── interceptors/ # HTTP Interceptors
│   │   │   └── enums/      # Перечисления
│   │   ├── config/         # Конфигурация
│   │   ├── modules/        # Feature модули
│   │   │   ├── auth/       # Аутентификация
│   │   │   ├── users/      # Пользователи
│   │   │   ├── courses/    # Курсы
│   │   │   ├── enrollments/ # Записи на курсы
│   │   │   ├── gamification/ # Геймификация
│   │   │   └── analytics/  # Аналитика
│   │   ├── app.module.ts
│   │   └── main.ts
│   ├── package.json
│   └── tsconfig.json
│
├── frontend/               # Angular Frontend
│   ├── src/
│   │   ├── app/
│   │   │   ├── core/      # Singleton сервисы
│   │   │   │   ├── auth/
│   │   │   │   ├── guards/
│   │   │   │   └── services/
│   │   │   ├── shared/    # Переиспользуемые компоненты
│   │   │   └── features/  # Feature модули
│   │   │       ├── auth/
│   │   │       ├── dashboard/
│   │   │       ├── courses/
│   │   │       └── profile/
│   │   ├── assets/
│   │   └── environments/
│   ├── package.json
│   └── angular.json
│
├── database/              # SQL скрипты
│   └── init.sql
│
├── docker-compose.yml     # Docker конфигурация
├── package.json           # Root package.json
└── README.md
```

## 🔐 API Endpoints

### Аутентификация

```
POST   /api/v1/auth/register          # Регистрация
POST   /api/v1/auth/login             # Вход
POST   /api/v1/auth/change-password   # Смена пароля
```

### Пользователи

```
GET    /api/v1/users                  # Список пользователей (Admin)
GET    /api/v1/users/me               # Текущий пользователь
GET    /api/v1/users/:id              # Получить пользователя
PUT    /api/v1/users/me               # Обновить профиль
PUT    /api/v1/users/:id              # Обновить пользователя (Admin)
DELETE /api/v1/users/:id              # Удалить пользователя (Admin)
```

### Курсы

```
GET    /api/v1/courses                # Список курсов
GET    /api/v1/courses/:id            # Получить курс
POST   /api/v1/courses                # Создать курс (Instructor)
PUT    /api/v1/courses/:id            # Обновить курс
PUT    /api/v1/courses/:id/publish    # Опубликовать курс
DELETE /api/v1/courses/:id            # Удалить курс
```

### Записи на курсы

```
GET    /api/v1/enrollments/my-courses # Мои курсы
```

## 🔑 Переменные окружения

### Backend (.env)

```env
# Application
NODE_ENV=development
PORT=3000
API_PREFIX=api/v1

# Database
DATABASE_HOST=localhost
DATABASE_PORT=5432
DATABASE_NAME=lms_db
DATABASE_USER=lms_user
DATABASE_PASSWORD=lms_password

# JWT
JWT_SECRET=your-super-secret-jwt-key-change-in-production
JWT_EXPIRES_IN=7d

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# MinIO
MINIO_ENDPOINT=localhost
MINIO_PORT=9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin
```

## 🧪 Тестирование

### Backend Tests

```bash
cd backend

# Unit tests
npm run test

# E2E tests
npm run test:e2e

# Test coverage
npm run test:cov
```

### Frontend Tests

```bash
cd frontend

# Unit tests
npm run test

# E2E tests
npm run e2e
```

## 📦 Сборка для production

### Backend

```bash
cd backend
npm run build
npm run start:prod
```

### Frontend

```bash
cd frontend
npm run build
# Файлы будут в dist/lms-frontend
```

## 🤝 Вклад в проект

1. Fork проекта
2. Создайте feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit изменения (`git commit -m 'Add some AmazingFeature'`)
4. Push в branch (`git push origin feature/AmazingFeature`)
5. Откройте Pull Request

## 📄 Лицензия

Этот проект лицензирован под MIT License.

## 👥 Авторы

- Ваше имя - [@your-username](https://github.com/your-username)

## 🙏 Благодарности

- Вдохновлено [Exode.biz](https://exode.biz/)
- Документация по архитектуре в файлах:
  - `LMS_Technical_Architecture.md`
  - `LMS_Development_Roadmap.md`
  - `LMS_Code_Examples.md`

## 📞 Поддержка

Если у вас возникли вопросы или проблемы:

- 📧 Email: support@yourlms.com
- 💬 Telegram: @your_telegram
- 🐛 [Issues](https://github.com/your-username/FREE_LMS/issues)

---

**Создано с ❤️ для образования**