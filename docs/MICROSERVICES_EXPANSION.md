# FREE LMS - Расширение микросервисной архитектуры

## Обзор

Данный документ описывает архитектуру **10 новых микросервисов**, которые расширяют функциональность FREE LMS до полноценной Enterprise-платформы.

После внедрения общее количество сервисов составит **32 микросервиса**.

---

## Содержание

1. [Приоритеты внедрения](#приоритеты-внедрения)
2. [Обзор новых сервисов](#обзор-новых-сервисов)
3. [Детальное описание сервисов](#детальное-описание-сервисов)
4. [Интеграционная карта](#интеграционная-карта)
5. [Требования к инфраструктуре](#требования-к-инфраструктуре)

---

## Приоритеты внедрения

### Фаза 1 (Критично)
| Сервис | Порт | Причина приоритета |
|--------|------|-------------------|
| **search-service** | 8100 | Критично для UX в микросервисной архитектуре |
| **media-processing-service** | 8101 | Критично для производительности видео |
| **event-service** | 8102 | Критично для B2B (blended learning) |

### Фаза 2 (Важно)
| Сервис | Порт | Причина приоритета |
|--------|------|-------------------|
| **authoring-service** | 8103 | Интерактивный контент (H5P, SCORM) |
| **proctoring-service** | 8104 | Контроль честности на экзаменах |
| **assignment-review-service** | 8105 | Масштабирование проверки ДЗ |

### Фаза 3 (Расширение)
| Сервис | Порт | Причина приоритета |
|--------|------|-------------------|
| **resource-booking-service** | 8106 | Офлайн-обучение и ресурсы |
| **audit-logging-service** | 8107 | SOC2/GDPR compliance |
| **lti-service** | 8108 | Интероперабельность с внешними LMS |
| **bot-platform-service** | 8109 | Micro-learning через мессенджеры |

---

## Обзор новых сервисов

### Распределение по категориям

```
├── Media & Content (2)
│   ├── search-service              :8100
│   └── media-processing-service    :8101
│
├── Live Learning (2)
│   ├── event-service               :8102
│   └── resource-booking-service    :8106
│
├── Assessment & Proctoring (3)
│   ├── authoring-service           :8103
│   ├── proctoring-service          :8104
│   └── assignment-review-service   :8105
│
├── Compliance & Audit (1)
│   └── audit-logging-service       :8107
│
└── Integrations (2)
    ├── lti-service                 :8108
    └── bot-platform-service        :8109
```

### Архитектурная диаграмма расширения

```
                        ┌─────────────────────────────────────────┐
                        │            Load Balancer                │
                        └───────────────────┬─────────────────────┘
                                            │
                        ┌───────────────────▼─────────────────────┐
                        │         API Gateway (:8000)             │
                        └───────────────────┬─────────────────────┘
                                            │
    ┌───────────────────────────────────────┼───────────────────────────────────────┐
    │                                       │                                       │
    │  ┌────────────────────────────────────┼────────────────────────────────────┐  │
    │  │            EXISTING SERVICES (22)                                       │  │
    │  │  auth, course, enrollment, payment, notification, analytics, ...        │  │
    │  └────────────────────────────────────┼────────────────────────────────────┘  │
    │                                       │                                       │
    │  ┌────────────────────────────────────┼────────────────────────────────────┐  │
    │  │                    NEW SERVICES (10)                                    │  │
    │  │                                                                         │  │
    │  │   ┌─────────────┐   ┌──────────────┐   ┌─────────────┐                 │  │
    │  │   │   Search    │   │    Media     │   │    Event    │                 │  │
    │  │   │   Service   │   │  Processing  │   │   Service   │                 │  │
    │  │   │   :8100     │   │    :8101     │   │    :8102    │                 │  │
    │  │   └──────┬──────┘   └──────┬───────┘   └──────┬──────┘                 │  │
    │  │          │                 │                  │                         │  │
    │  │   ┌─────────────┐   ┌──────────────┐   ┌─────────────┐                 │  │
    │  │   │ Authoring   │   │  Proctoring  │   │ Assignment  │                 │  │
    │  │   │  Service    │   │   Service    │   │   Review    │                 │  │
    │  │   │   :8103     │   │    :8104     │   │    :8105    │                 │  │
    │  │   └─────────────┘   └──────────────┘   └─────────────┘                 │  │
    │  │                                                                         │  │
    │  │   ┌─────────────┐   ┌──────────────┐   ┌─────────────┐   ┌───────────┐ │  │
    │  │   │  Resource   │   │    Audit     │   │     LTI     │   │    Bot    │ │  │
    │  │   │  Booking    │   │   Logging    │   │   Service   │   │  Platform │ │  │
    │  │   │   :8106     │   │    :8107     │   │    :8108    │   │   :8109   │ │  │
    │  │   └─────────────┘   └──────────────┘   └─────────────┘   └───────────┘ │  │
    │  │                                                                         │  │
    │  └─────────────────────────────────────────────────────────────────────────┘  │
    │                                       │                                       │
    └───────────────────────────────────────┼───────────────────────────────────────┘
                                            │
          ┌─────────────────────────────────┼─────────────────────────────────┐
          │                                 │                                 │
    ┌─────▼─────┐   ┌───────────┐   ┌───────▼───────┐   ┌───────────┐   ┌─────▼─────┐
    │PostgreSQL │   │   Redis   │   │     Kafka     │   │   MinIO   │   │Elasticsearch│
    │  Cluster  │   │  Cluster  │   │    Cluster    │   │  Storage  │   │   Cluster   │
    └───────────┘   └───────────┘   └───────────────┘   └───────────┘   └─────────────┘
```

---

## Детальное описание сервисов

---

## 1. Search Service (Unified Search)

### Назначение
Единый поисковый сервис, агрегирующий данные из всех микросервисов и обеспечивающий полнотекстовый поиск по всей платформе.

### Технические характеристики

| Параметр | Значение |
|----------|----------|
| Порт | 8100 |
| База данных | Elasticsearch 8.x (primary), PostgreSQL (metadata) |
| Kafka Consumer Groups | `search-indexer` |
| Memory | 2-4 GB (зависит от объёма индекса) |

### Функциональность

#### Индексируемые сущности
- **Курсы** — названия, описания, теги, категории
- **Уроки** — контент, транскрипты видео
- **Пользователи** — имена, email, навыки, роли
- **Документы** — PDF, DOCX, PPTX (извлечение текста)
- **Форумы** — посты, комментарии, Q&A
- **Организации** — названия, департаменты

#### Возможности поиска
- Полнотекстовый поиск с релевантностью
- Фасетный поиск (фильтры по категориям)
- Автодополнение (typeahead)
- Поиск с учётом опечаток (fuzzy search)
- Поиск по синонимам
- Мультиязычный поиск

### API Endpoints

```yaml
# Основной поиск
GET  /api/v1/search
     ?q={query}
     &type={course|lesson|user|document|forum}
     &filters={json}
     &page={page}
     &size={size}
     &sort={relevance|date|popularity}

# Автодополнение
GET  /api/v1/search/suggest?q={query}&limit={limit}

# Расширенный поиск
POST /api/v1/search/advanced
     Body: { query, filters, facets, highlight }

# Поиск внутри курса
GET  /api/v1/search/course/{courseId}?q={query}

# Поиск в документах
GET  /api/v1/search/documents?q={query}&format={pdf|docx}

# Администрирование индексов
POST /api/v1/search/reindex/{entityType}
GET  /api/v1/search/stats
```

### Kafka Events (Consumer)

```java
// Слушает события для обновления индекса
@KafkaListener(topics = {
    "course-events",
    "user-events",
    "forum-events",
    "document-events"
}, groupId = "search-indexer")
```

### Схема базы данных (PostgreSQL - метаданные)

```sql
-- Статистика поиска
CREATE TABLE search_analytics (
    id BIGSERIAL PRIMARY KEY,
    query VARCHAR(500) NOT NULL,
    user_id BIGINT,
    results_count INT,
    clicked_result_id VARCHAR(100),
    clicked_result_type VARCHAR(50),
    search_duration_ms INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Синонимы
CREATE TABLE search_synonyms (
    id BIGSERIAL PRIMARY KEY,
    term VARCHAR(100) NOT NULL,
    synonyms TEXT[], -- Array of synonyms
    language VARCHAR(10) DEFAULT 'ru',
    is_active BOOLEAN DEFAULT true
);

-- Стоп-слова
CREATE TABLE search_stopwords (
    id BIGSERIAL PRIMARY KEY,
    word VARCHAR(50) NOT NULL,
    language VARCHAR(10) DEFAULT 'ru'
);
```

### Elasticsearch Index Mappings

```json
{
  "courses": {
    "mappings": {
      "properties": {
        "id": { "type": "keyword" },
        "title": {
          "type": "text",
          "analyzer": "russian_analyzer",
          "fields": {
            "keyword": { "type": "keyword" },
            "suggest": { "type": "completion" }
          }
        },
        "description": { "type": "text", "analyzer": "russian_analyzer" },
        "tags": { "type": "keyword" },
        "category": { "type": "keyword" },
        "instructor_name": { "type": "text" },
        "organization_id": { "type": "keyword" },
        "difficulty": { "type": "keyword" },
        "rating": { "type": "float" },
        "enrollments_count": { "type": "integer" },
        "created_at": { "type": "date" },
        "updated_at": { "type": "date" }
      }
    }
  }
}
```

### Интеграции

```
┌─────────────────┐     Kafka Events      ┌────────────────┐
│  course-service │ ─────────────────────▶│                │
│  auth-service   │                       │ search-service │
│  social-service │ ─────────────────────▶│                │
│  course-service │                       │                │
└─────────────────┘                       └───────┬────────┘
                                                  │
                                                  ▼
                                          ┌───────────────┐
                                          │ Elasticsearch │
                                          │    Cluster    │
                                          └───────────────┘
```

---

## 2. Media Processing Service

### Назначение
Асинхронная обработка медиафайлов: транскодирование видео, сжатие изображений, генерация превью и извлечение метаданных.

### Технические характеристики

| Параметр | Значение |
|----------|----------|
| Порт | 8101 |
| База данных | PostgreSQL (lms_media) |
| Kafka Consumer Groups | `media-processor` |
| Технологии обработки | FFmpeg, ImageMagick |
| Memory | 4-8 GB (для транскодирования) |
| CPU | 4+ cores (рекомендуется) |

### Функциональность

#### Обработка видео
- **Транскодирование** в HLS/DASH для адаптивного стриминга
- **Качество**: 360p, 480p, 720p, 1080p (автоматически)
- **Форматы входа**: MP4, MOV, AVI, MKV, WebM
- **Формат выхода**: HLS (m3u8 + ts сегменты)
- **Генерация превью**: Thumbnail каждые N секунд
- **Извлечение субтитров**: из встроенных треков
- **Транскрипция**: интеграция с Whisper/Google Speech API

#### Обработка изображений
- **Ресайзинг**: адаптивные размеры для разных экранов
- **Сжатие**: WebP, AVIF конвертация
- **Thumbnails**: автогенерация для галерей
- **Watermark**: наложение логотипа организации

#### Обработка документов
- **Извлечение текста**: PDF, DOCX, PPTX → plain text
- **Конвертация**: PDF → изображения для превью
- **OCR**: распознавание сканов

### API Endpoints

```yaml
# Загрузка файла на обработку
POST /api/v1/media/upload
     Content-Type: multipart/form-data
     Body: file, processingOptions

# Статус обработки
GET  /api/v1/media/jobs/{jobId}

# Получение обработанного файла
GET  /api/v1/media/{mediaId}
GET  /api/v1/media/{mediaId}/stream        # HLS playlist
GET  /api/v1/media/{mediaId}/thumbnail     # Preview image
GET  /api/v1/media/{mediaId}/transcript    # Extracted text/subtitles

# Batch операции
POST /api/v1/media/batch/process
GET  /api/v1/media/batch/{batchId}/status

# Управление
DELETE /api/v1/media/{mediaId}
GET    /api/v1/media/stats                 # Storage statistics
POST   /api/v1/media/cleanup               # Remove orphaned files
```

### Kafka Events

```java
// Consumer - получение заданий на обработку
@KafkaListener(topics = "media-upload-events", groupId = "media-processor")
public void processMediaUpload(MediaUploadEvent event) {
    // Запуск обработки
}

// Producer - уведомление о завершении
@Autowired
private KafkaTemplate<String, MediaProcessedEvent> kafkaTemplate;

// Topics:
// - media-upload-events (consumer)
// - media-processed-events (producer)
// - media-failed-events (producer)
```

### Схема базы данных

```sql
-- Медиа файлы
CREATE TABLE media_files (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    original_filename VARCHAR(500) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    storage_path VARCHAR(1000) NOT NULL,

    -- Метаданные
    duration_seconds INT,           -- для видео/аудио
    width INT,                      -- для видео/изображений
    height INT,
    bitrate INT,
    codec VARCHAR(50),

    -- Обработка
    processing_status VARCHAR(50) DEFAULT 'pending',
    processing_started_at TIMESTAMP,
    processing_completed_at TIMESTAMP,
    processing_error TEXT,

    -- Связи
    uploaded_by BIGINT NOT NULL,
    organization_id BIGINT,
    course_id BIGINT,
    lesson_id BIGINT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Варианты обработанных файлов (качество, формат)
CREATE TABLE media_variants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    media_id UUID REFERENCES media_files(id),
    variant_type VARCHAR(50) NOT NULL,  -- 'hls_1080p', 'thumbnail', 'webp_800'
    storage_path VARCHAR(1000) NOT NULL,
    file_size BIGINT,
    width INT,
    height INT,
    bitrate INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Очередь обработки
CREATE TABLE processing_jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    media_id UUID REFERENCES media_files(id),
    job_type VARCHAR(50) NOT NULL,      -- 'transcode', 'thumbnail', 'ocr'
    priority INT DEFAULT 5,
    status VARCHAR(50) DEFAULT 'queued',
    progress INT DEFAULT 0,             -- 0-100
    worker_id VARCHAR(100),
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    error_message TEXT,
    retry_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Транскрипции
CREATE TABLE media_transcripts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    media_id UUID REFERENCES media_files(id),
    language VARCHAR(10) DEFAULT 'ru',
    transcript_type VARCHAR(50),        -- 'auto', 'manual', 'imported'
    content TEXT,
    vtt_content TEXT,                   -- WebVTT format
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Индексы
CREATE INDEX idx_media_files_status ON media_files(processing_status);
CREATE INDEX idx_media_files_course ON media_files(course_id);
CREATE INDEX idx_processing_jobs_status ON processing_jobs(status, priority);
```

### Конфигурация обработки

```yaml
media-processing:
  video:
    output-format: hls
    segment-duration: 6
    qualities:
      - name: 1080p
        width: 1920
        height: 1080
        bitrate: 5000k
      - name: 720p
        width: 1280
        height: 720
        bitrate: 2500k
      - name: 480p
        width: 854
        height: 480
        bitrate: 1000k
      - name: 360p
        width: 640
        height: 360
        bitrate: 500k

  image:
    formats: [webp, jpg]
    sizes:
      thumbnail: 320x180
      medium: 800x450
      large: 1280x720
    quality: 85

  storage:
    type: minio
    bucket: lms-processed-media
    cdn-url: https://cdn.example.com
```

### Архитектура обработки

```
┌──────────────┐     ┌─────────────────┐     ┌──────────────┐
│ Course       │     │ Media Processing │     │    MinIO     │
│ Service      │────▶│ Service          │────▶│   Storage    │
└──────────────┘     └────────┬────────┘     └──────────────┘
                              │
              ┌───────────────┼───────────────┐
              │               │               │
        ┌─────▼─────┐   ┌─────▼─────┐   ┌─────▼─────┐
        │  FFmpeg   │   │ImageMagick│   │  Whisper  │
        │  Worker   │   │  Worker   │   │  Worker   │
        └───────────┘   └───────────┘   └───────────┘
              │               │               │
              └───────────────┼───────────────┘
                              │
                    ┌─────────▼─────────┐
                    │  Kafka Events     │
                    │ (media-processed) │
                    └───────────────────┘
```

---

## 3. Event Service (Вебинары и события)

### Назначение
Управление синхронным обучением: вебинары, живые сессии, календарь событий с интеграцией видеоконференций.

### Технические характеристики

| Параметр | Значение |
|----------|----------|
| Порт | 8102 |
| База данных | PostgreSQL (lms_events) |
| Kafka Topics | `event-events` |
| WebSocket | Поддерживается для real-time обновлений |

### Функциональность

#### Управление событиями
- **Типы событий**: вебинар, воркшоп, тренинг, консультация
- **Серии событий**: повторяющиеся (ежедневно, еженедельно)
- **Регистрация**: с подтверждением, лист ожидания
- **Напоминания**: email, push, SMS
- **Часовые пояса**: поддержка для глобальных команд

#### Интеграции видеоконференций
- **Zoom** — создание встреч через API
- **Microsoft Teams** — интеграция с Graph API
- **Google Meet** — через Google Calendar API
- **BigBlueButton** — self-hosted опция
- **Jitsi** — open-source альтернатива

#### Аналитика посещаемости
- **Трекинг присутствия**: время входа/выхода
- **Engagement метрики**: активность в чате, реакции
- **Записи**: автоматическое сохранение в media-service
- **Отчёты**: кто был, кто пропустил

### API Endpoints

```yaml
# События
POST   /api/v1/events                      # Создать событие
GET    /api/v1/events                      # Список событий
GET    /api/v1/events/{id}                 # Детали события
PUT    /api/v1/events/{id}                 # Обновить
DELETE /api/v1/events/{id}                 # Удалить
POST   /api/v1/events/{id}/cancel          # Отменить

# Серии событий
POST   /api/v1/events/series               # Создать серию
GET    /api/v1/events/series/{id}          # Серия с событиями
PUT    /api/v1/events/series/{id}          # Обновить серию

# Регистрация
POST   /api/v1/events/{id}/register        # Записаться
DELETE /api/v1/events/{id}/register        # Отменить запись
GET    /api/v1/events/{id}/attendees       # Список участников
POST   /api/v1/events/{id}/waitlist        # В лист ожидания

# Календарь
GET    /api/v1/events/calendar             # Календарь пользователя
GET    /api/v1/events/calendar/ical        # iCal экспорт
POST   /api/v1/events/calendar/sync        # Синхронизация с внешним

# Видеоконференции
POST   /api/v1/events/{id}/meeting/create  # Создать комнату
GET    /api/v1/events/{id}/meeting/join    # Получить ссылку
GET    /api/v1/events/{id}/meeting/recording # Запись

# Посещаемость
POST   /api/v1/events/{id}/attendance/checkin
POST   /api/v1/events/{id}/attendance/checkout
GET    /api/v1/events/{id}/attendance/report
```

### Kafka Events

```java
// Producer topics
- event-created
- event-updated
- event-cancelled
- event-started
- event-ended
- attendance-recorded

// Consumer topics (слушает)
- user-events (для автоприглашений)
- course-events (привязка к курсам)
```

### Схема базы данных

```sql
-- События
CREATE TABLE events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(500) NOT NULL,
    description TEXT,
    event_type VARCHAR(50) NOT NULL,       -- 'webinar', 'workshop', 'training', 'consultation'

    -- Время
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    timezone VARCHAR(50) DEFAULT 'UTC',

    -- Место/Платформа
    location_type VARCHAR(50) NOT NULL,    -- 'online', 'offline', 'hybrid'
    physical_location VARCHAR(500),
    meeting_platform VARCHAR(50),          -- 'zoom', 'teams', 'meet', 'bbb', 'jitsi'
    meeting_url VARCHAR(1000),
    meeting_id VARCHAR(100),
    meeting_password VARCHAR(100),

    -- Настройки
    max_attendees INT,
    registration_required BOOLEAN DEFAULT true,
    registration_deadline TIMESTAMP WITH TIME ZONE,
    allow_waitlist BOOLEAN DEFAULT true,
    is_recorded BOOLEAN DEFAULT false,
    recording_url VARCHAR(1000),

    -- Связи
    organizer_id BIGINT NOT NULL,
    organization_id BIGINT,
    course_id BIGINT,
    series_id UUID,

    -- Статус
    status VARCHAR(50) DEFAULT 'scheduled', -- 'scheduled', 'live', 'completed', 'cancelled'

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Серии событий (повторяющиеся)
CREATE TABLE event_series (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(500) NOT NULL,
    recurrence_rule VARCHAR(500),          -- RFC 5545 RRULE
    start_date DATE NOT NULL,
    end_date DATE,
    template_data JSONB,                   -- Шаблон для создания событий
    organizer_id BIGINT NOT NULL,
    organization_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Регистрации
CREATE TABLE event_registrations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES events(id),
    user_id BIGINT NOT NULL,
    registration_status VARCHAR(50) DEFAULT 'registered', -- 'registered', 'waitlisted', 'cancelled'
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    cancelled_at TIMESTAMP,
    reminder_sent BOOLEAN DEFAULT false,

    UNIQUE(event_id, user_id)
);

-- Посещаемость
CREATE TABLE event_attendance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES events(id),
    user_id BIGINT NOT NULL,
    joined_at TIMESTAMP WITH TIME ZONE,
    left_at TIMESTAMP WITH TIME ZONE,
    duration_minutes INT,
    attendance_percentage DECIMAL(5,2),
    participation_score INT,               -- Активность: чат, реакции
    source VARCHAR(50),                    -- 'zoom_webhook', 'manual', 'browser'

    UNIQUE(event_id, user_id)
);

-- Напоминания
CREATE TABLE event_reminders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES events(id),
    remind_before_minutes INT NOT NULL,    -- 60, 1440 (day), etc.
    reminder_type VARCHAR(50) NOT NULL,    -- 'email', 'push', 'sms'
    is_sent BOOLEAN DEFAULT false,
    sent_at TIMESTAMP
);

-- Индексы
CREATE INDEX idx_events_start_time ON events(start_time);
CREATE INDEX idx_events_organizer ON events(organizer_id);
CREATE INDEX idx_events_organization ON events(organization_id);
CREATE INDEX idx_events_status ON events(status);
CREATE INDEX idx_registrations_user ON event_registrations(user_id);
CREATE INDEX idx_attendance_event ON event_attendance(event_id);
```

### Интеграция с видеоконференциями

```java
// Абстракция для разных провайдеров
public interface MeetingProvider {
    MeetingInfo createMeeting(MeetingRequest request);
    void updateMeeting(String meetingId, MeetingRequest request);
    void deleteMeeting(String meetingId);
    String getJoinUrl(String meetingId, UserInfo user);
    List<AttendanceRecord> getAttendanceReport(String meetingId);
    String getRecordingUrl(String meetingId);
}

// Реализации
- ZoomMeetingProvider
- TeamsMeetingProvider
- GoogleMeetProvider
- BigBlueButtonProvider
- JitsiMeetProvider
```

---

## 4. Authoring Service (Конструктор контента)

### Назначение
Создание интерактивного образовательного контента: H5P, SCORM-пакеты, интерактивные презентации.

### Технические характеристики

| Параметр | Значение |
|----------|----------|
| Порт | 8103 |
| База данных | PostgreSQL (lms_authoring) |
| Kafka Topics | `content-events` |
| Хранилище | MinIO (lms-content-packages) |

### Функциональность

#### Типы контента
- **H5P** — интерактивные видео, презентации, timeline
- **SCORM 1.2/2004** — создание и импорт пакетов
- **xAPI (Tin Can)** — современные learning records
- **Rich Text Editor** — статьи как в Notion/Medium
- **Quiz Builder** — визуальный конструктор тестов

#### Возможности редактора
- Drag & drop интерфейс
- Шаблоны контента
- Совместное редактирование (Collaboration)
- Версионирование контента
- Предварительный просмотр
- Импорт/экспорт

### API Endpoints

```yaml
# Проекты контента
POST   /api/v1/authoring/projects          # Создать проект
GET    /api/v1/authoring/projects          # Список проектов
GET    /api/v1/authoring/projects/{id}     # Детали
PUT    /api/v1/authoring/projects/{id}     # Сохранить
DELETE /api/v1/authoring/projects/{id}     # Удалить

# H5P контент
POST   /api/v1/authoring/h5p               # Создать H5P
GET    /api/v1/authoring/h5p/libraries     # Доступные типы H5P
POST   /api/v1/authoring/h5p/{id}/export   # Экспорт .h5p файла
POST   /api/v1/authoring/h5p/import        # Импорт .h5p файла

# SCORM
POST   /api/v1/authoring/scorm/import      # Импорт SCORM пакета
POST   /api/v1/authoring/scorm/{id}/export # Экспорт SCORM пакета
GET    /api/v1/authoring/scorm/{id}/manifest

# Шаблоны
GET    /api/v1/authoring/templates         # Библиотека шаблонов
POST   /api/v1/authoring/templates         # Создать шаблон
POST   /api/v1/authoring/templates/{id}/use # Использовать шаблон

# Версии
GET    /api/v1/authoring/projects/{id}/versions
POST   /api/v1/authoring/projects/{id}/versions  # Создать версию
POST   /api/v1/authoring/projects/{id}/versions/{v}/restore

# Публикация
POST   /api/v1/authoring/projects/{id}/publish   # Опубликовать в course-service
```

### Схема базы данных

```sql
-- Проекты контента
CREATE TABLE authoring_projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(500) NOT NULL,
    description TEXT,
    content_type VARCHAR(50) NOT NULL,     -- 'h5p', 'scorm', 'article', 'quiz'

    -- Контент (JSON структура)
    content JSONB NOT NULL,

    -- Метаданные
    thumbnail_url VARCHAR(1000),
    duration_minutes INT,
    difficulty VARCHAR(20),
    tags TEXT[],

    -- Статус
    status VARCHAR(50) DEFAULT 'draft',    -- 'draft', 'review', 'published', 'archived'
    published_version INT,

    -- Связи
    author_id BIGINT NOT NULL,
    organization_id BIGINT,
    course_id BIGINT,                      -- Если привязан к курсу

    -- Коллаборация
    collaborators BIGINT[],                -- Соавторы

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Версии контента
CREATE TABLE content_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID REFERENCES authoring_projects(id),
    version_number INT NOT NULL,
    content JSONB NOT NULL,
    change_description TEXT,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(project_id, version_number)
);

-- H5P библиотеки
CREATE TABLE h5p_libraries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    machine_name VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    major_version INT NOT NULL,
    minor_version INT NOT NULL,
    patch_version INT NOT NULL,
    runnable BOOLEAN DEFAULT true,
    fullscreen BOOLEAN DEFAULT false,
    embed_types TEXT[],
    preloaded_js TEXT[],
    preloaded_css TEXT[],
    drop_library_css TEXT[],
    semantics JSONB,

    UNIQUE(machine_name, major_version, minor_version)
);

-- SCORM манифесты
CREATE TABLE scorm_packages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID REFERENCES authoring_projects(id),
    scorm_version VARCHAR(20) NOT NULL,    -- '1.2', '2004_3rd', '2004_4th'
    manifest_xml TEXT,
    entry_point VARCHAR(500),
    storage_path VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Шаблоны
CREATE TABLE content_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(500) NOT NULL,
    description TEXT,
    content_type VARCHAR(50) NOT NULL,
    template_content JSONB NOT NULL,
    thumbnail_url VARCHAR(1000),
    category VARCHAR(100),
    is_public BOOLEAN DEFAULT false,
    organization_id BIGINT,                -- NULL = системный шаблон
    usage_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Индексы
CREATE INDEX idx_projects_author ON authoring_projects(author_id);
CREATE INDEX idx_projects_status ON authoring_projects(status);
CREATE INDEX idx_projects_type ON authoring_projects(content_type);
```

---

## 5. Proctoring Service (Контроль экзаменов)

### Назначение
Автоматический контроль честности при прохождении онлайн-экзаменов с использованием AI-анализа.

### Технические характеристики

| Параметр | Значение |
|----------|----------|
| Порт | 8104 |
| База данных | PostgreSQL (lms_proctoring) |
| ML модели | TensorFlow/ONNX для анализа |
| WebSocket | Real-time мониторинг |
| Хранилище | MinIO (lms-proctoring-recordings) |

### Функциональность

#### Мониторинг
- **Веб-камера**: анализ лица, направления взгляда
- **Экран**: запись и детекция подозрительных действий
- **Аудио**: обнаружение разговоров
- **Браузер**: блокировка вкладок, copy-paste

#### AI-детекция
- Множественные лица в кадре
- Отсутствие лица
- Взгляд в сторону (продолжительный)
- Посторонние звуки/голоса
- Использование телефона
- Подмена личности

#### Режимы прокторинга
- **AI-only**: полностью автоматический
- **Live**: проктор наблюдает в реальном времени
- **Record & Review**: запись для последующей проверки

### API Endpoints

```yaml
# Сессии прокторинга
POST   /api/v1/proctoring/sessions                # Создать сессию
GET    /api/v1/proctoring/sessions/{id}           # Статус сессии
POST   /api/v1/proctoring/sessions/{id}/start     # Начать
POST   /api/v1/proctoring/sessions/{id}/end       # Завершить
DELETE /api/v1/proctoring/sessions/{id}           # Отменить

# Верификация личности
POST   /api/v1/proctoring/sessions/{id}/verify-identity
       Body: { photo, documentPhoto }

# Стриминг данных
WS     /ws/proctoring/{sessionId}/stream          # WebSocket для видео
POST   /api/v1/proctoring/sessions/{id}/frame     # Отправить кадр
POST   /api/v1/proctoring/sessions/{id}/screenshot

# Инциденты
GET    /api/v1/proctoring/sessions/{id}/incidents
POST   /api/v1/proctoring/sessions/{id}/incidents # Зафиксировать инцидент
PUT    /api/v1/proctoring/incidents/{id}/review   # Рассмотреть инцидент

# Записи
GET    /api/v1/proctoring/sessions/{id}/recording
GET    /api/v1/proctoring/sessions/{id}/timeline  # Таймлайн событий

# Live прокторинг (для прокторов)
GET    /api/v1/proctoring/live/queue              # Очередь на проверку
POST   /api/v1/proctoring/live/{sessionId}/claim  # Взять сессию
POST   /api/v1/proctoring/live/{sessionId}/flag   # Отметить нарушение
```

### Схема базы данных

```sql
-- Сессии прокторинга
CREATE TABLE proctoring_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    exam_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,

    -- Режим
    proctoring_mode VARCHAR(50) NOT NULL,  -- 'ai', 'live', 'record_review'

    -- Верификация
    identity_verified BOOLEAN DEFAULT false,
    identity_photo_url VARCHAR(1000),
    document_photo_url VARCHAR(1000),
    verification_score DECIMAL(5,2),

    -- Время
    scheduled_start TIMESTAMP WITH TIME ZONE,
    actual_start TIMESTAMP WITH TIME ZONE,
    actual_end TIMESTAMP WITH TIME ZONE,

    -- Статус
    status VARCHAR(50) DEFAULT 'pending',  -- 'pending', 'in_progress', 'completed', 'flagged', 'invalidated'

    -- Записи
    video_recording_url VARCHAR(1000),
    screen_recording_url VARCHAR(1000),
    audio_recording_url VARCHAR(1000),

    -- Результаты
    trust_score DECIMAL(5,2),              -- 0-100
    incidents_count INT DEFAULT 0,
    review_status VARCHAR(50),             -- 'pending', 'clean', 'suspicious', 'cheating'
    reviewed_by BIGINT,
    reviewed_at TIMESTAMP,
    review_notes TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Инциденты (нарушения)
CREATE TABLE proctoring_incidents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID REFERENCES proctoring_sessions(id),

    -- Тип инцидента
    incident_type VARCHAR(100) NOT NULL,
    -- 'face_not_visible', 'multiple_faces', 'looking_away',
    -- 'talking', 'phone_detected', 'tab_switch', 'copy_paste',
    -- 'browser_blur', 'suspicious_audio'

    severity VARCHAR(20) NOT NULL,         -- 'low', 'medium', 'high', 'critical'
    confidence_score DECIMAL(5,2),         -- AI уверенность

    -- Время
    timestamp_seconds INT NOT NULL,        -- Секунда от начала сессии
    duration_seconds INT,

    -- Доказательства
    screenshot_url VARCHAR(1000),
    video_clip_url VARCHAR(1000),

    -- Рассмотрение
    is_false_positive BOOLEAN,
    reviewed_by BIGINT,
    reviewed_at TIMESTAMP,
    review_notes TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Настройки прокторинга для экзаменов
CREATE TABLE proctoring_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    exam_id BIGINT UNIQUE NOT NULL,

    -- Включённые проверки
    enable_webcam BOOLEAN DEFAULT true,
    enable_screen_recording BOOLEAN DEFAULT true,
    enable_audio_detection BOOLEAN DEFAULT true,
    enable_tab_lock BOOLEAN DEFAULT true,
    enable_copy_paste_block BOOLEAN DEFAULT true,

    -- AI настройки
    face_detection_sensitivity VARCHAR(20) DEFAULT 'medium',
    gaze_detection_threshold INT DEFAULT 5, -- секунд

    -- Требования
    require_identity_verification BOOLEAN DEFAULT true,
    allow_bathroom_breaks BOOLEAN DEFAULT false,
    max_incidents_before_flag INT DEFAULT 5,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Live прокторинг - очередь
CREATE TABLE proctoring_queue (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID REFERENCES proctoring_sessions(id),
    priority INT DEFAULT 5,
    claimed_by BIGINT,
    claimed_at TIMESTAMP,
    status VARCHAR(50) DEFAULT 'waiting',  -- 'waiting', 'claimed', 'completed'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Индексы
CREATE INDEX idx_sessions_exam ON proctoring_sessions(exam_id);
CREATE INDEX idx_sessions_user ON proctoring_sessions(user_id);
CREATE INDEX idx_sessions_status ON proctoring_sessions(status);
CREATE INDEX idx_incidents_session ON proctoring_incidents(session_id);
CREATE INDEX idx_incidents_type ON proctoring_incidents(incident_type);
CREATE INDEX idx_queue_status ON proctoring_queue(status, priority);
```

---

## 6. Assignment Review Service (Проверка заданий)

### Назначение
Централизованная система проверки домашних заданий с поддержкой автоматической и ручной проверки.

### Технические характеристики

| Параметр | Значение |
|----------|----------|
| Порт | 8105 |
| База данных | PostgreSQL (lms_assignments) |
| Kafka Topics | `assignment-events` |
| Интеграции | Code runners, OCR, Plagiarism check |

### Функциональность

#### Типы заданий
- **Текстовые ответы** — эссе, развёрнутые ответы
- **Файлы** — документы, презентации, архивы
- **Код** — с автоматическим запуском тестов
- **Рукописные работы** — сканы с OCR

#### Автоматическая проверка
- **Code execution** — запуск в sandbox (Docker)
- **Unit tests** — проверка по тест-кейсам
- **Plagiarism detection** — проверка на плагиат
- **OCR** — распознавание рукописного текста
- **AI grading** — предварительная оценка AI

#### Workflow проверки
- Очередь заданий для проверяющих
- Распределение по преподавателям
- Рубрики оценивания
- Комментарии и фидбек
- Апелляции

### API Endpoints

```yaml
# Задания (submissions)
POST   /api/v1/assignments/submit              # Сдать задание
GET    /api/v1/assignments/submissions         # Список сданных
GET    /api/v1/assignments/submissions/{id}    # Детали
PUT    /api/v1/assignments/submissions/{id}    # Обновить (до дедлайна)

# Очередь проверки
GET    /api/v1/assignments/review/queue        # Очередь для преподавателя
POST   /api/v1/assignments/review/{id}/claim   # Взять на проверку
POST   /api/v1/assignments/review/{id}/grade   # Поставить оценку
POST   /api/v1/assignments/review/{id}/feedback # Добавить комментарий
POST   /api/v1/assignments/review/{id}/return  # Вернуть на доработку

# Автопроверка
POST   /api/v1/assignments/{id}/autograde      # Запустить автопроверку
GET    /api/v1/assignments/{id}/autograde/result
POST   /api/v1/assignments/{id}/run-code       # Запустить код
GET    /api/v1/assignments/{id}/plagiarism     # Проверка на плагиат

# Рубрики
GET    /api/v1/assignments/rubrics             # Шаблоны рубрик
POST   /api/v1/assignments/rubrics             # Создать рубрику
GET    /api/v1/assignments/rubrics/{id}

# Апелляции
POST   /api/v1/assignments/submissions/{id}/appeal
GET    /api/v1/assignments/appeals             # Список апелляций
PUT    /api/v1/assignments/appeals/{id}/resolve

# Статистика
GET    /api/v1/assignments/stats/course/{courseId}
GET    /api/v1/assignments/stats/reviewer/{reviewerId}
```

### Схема базы данных

```sql
-- Сданные задания
CREATE TABLE assignment_submissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assignment_id BIGINT NOT NULL,         -- Из course-service
    user_id BIGINT NOT NULL,

    -- Контент
    submission_type VARCHAR(50) NOT NULL,  -- 'text', 'file', 'code', 'url'
    text_content TEXT,
    file_urls TEXT[],
    code_content TEXT,
    code_language VARCHAR(50),
    url VARCHAR(1000),

    -- Метаданные
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_late BOOLEAN DEFAULT false,
    attempt_number INT DEFAULT 1,

    -- Статус
    status VARCHAR(50) DEFAULT 'submitted',
    -- 'submitted', 'in_review', 'graded', 'returned', 'resubmitted'

    -- Автопроверка
    autograde_status VARCHAR(50),          -- 'pending', 'running', 'completed', 'failed'
    autograde_score DECIMAL(5,2),
    autograde_feedback JSONB,
    plagiarism_score DECIMAL(5,2),
    plagiarism_report_url VARCHAR(1000),

    -- Ручная проверка
    reviewer_id BIGINT,
    reviewed_at TIMESTAMP,
    final_score DECIMAL(5,2),
    max_score DECIMAL(5,2),
    feedback TEXT,
    rubric_scores JSONB,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Рубрики оценивания
CREATE TABLE grading_rubrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    organization_id BIGINT,

    -- Критерии (JSON array)
    criteria JSONB NOT NULL,
    /* Example:
    [
        {
            "id": "clarity",
            "title": "Ясность изложения",
            "description": "...",
            "maxPoints": 20,
            "levels": [
                {"score": 20, "description": "Отлично"},
                {"score": 15, "description": "Хорошо"},
                {"score": 10, "description": "Удовлетворительно"},
                {"score": 0, "description": "Неудовлетворительно"}
            ]
        }
    ]
    */

    total_points DECIMAL(5,2),
    is_template BOOLEAN DEFAULT false,

    created_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Комментарии к заданиям
CREATE TABLE submission_comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    submission_id UUID REFERENCES assignment_submissions(id),
    author_id BIGINT NOT NULL,
    author_role VARCHAR(50) NOT NULL,      -- 'student', 'reviewer', 'instructor'

    comment_type VARCHAR(50) DEFAULT 'general', -- 'general', 'inline', 'code_review'
    content TEXT NOT NULL,

    -- Для inline комментариев
    line_number INT,
    file_path VARCHAR(500),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Очередь проверки
CREATE TABLE review_queue (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    submission_id UUID REFERENCES assignment_submissions(id),
    priority INT DEFAULT 5,
    assigned_to BIGINT,                    -- Назначенный проверяющий
    assigned_at TIMESTAMP,
    due_date TIMESTAMP,
    status VARCHAR(50) DEFAULT 'pending',  -- 'pending', 'assigned', 'in_progress', 'completed'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Апелляции
CREATE TABLE grade_appeals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    submission_id UUID REFERENCES assignment_submissions(id),
    student_id BIGINT NOT NULL,

    reason TEXT NOT NULL,
    original_score DECIMAL(5,2),
    requested_score DECIMAL(5,2),

    status VARCHAR(50) DEFAULT 'pending',  -- 'pending', 'under_review', 'approved', 'rejected'
    resolution TEXT,
    resolved_by BIGINT,
    resolved_at TIMESTAMP,
    final_score DECIMAL(5,2),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Результаты запуска кода
CREATE TABLE code_execution_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    submission_id UUID REFERENCES assignment_submissions(id),

    execution_status VARCHAR(50),          -- 'success', 'compile_error', 'runtime_error', 'timeout'
    stdout TEXT,
    stderr TEXT,
    exit_code INT,
    execution_time_ms INT,
    memory_used_kb INT,

    -- Тесты
    tests_total INT,
    tests_passed INT,
    test_results JSONB,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Индексы
CREATE INDEX idx_submissions_assignment ON assignment_submissions(assignment_id);
CREATE INDEX idx_submissions_user ON assignment_submissions(user_id);
CREATE INDEX idx_submissions_status ON assignment_submissions(status);
CREATE INDEX idx_submissions_reviewer ON assignment_submissions(reviewer_id);
CREATE INDEX idx_queue_status ON review_queue(status, priority);
CREATE INDEX idx_appeals_status ON grade_appeals(status);
```

### Архитектура Code Runner

```
┌────────────────────┐
│ Assignment Review  │
│     Service        │
└─────────┬──────────┘
          │
          ▼
┌─────────────────────┐     ┌─────────────────┐
│   Code Execution    │────▶│  Docker Sandbox │
│      Manager        │     │    (isolated)   │
└─────────────────────┘     └─────────────────┘
          │
          ├── Python Runner
          ├── Java Runner
          ├── JavaScript Runner
          ├── C/C++ Runner
          └── Go Runner
```

---

## 7. Resource Booking Service (Бронирование ресурсов)

### Назначение
Управление физическими ресурсами для офлайн-обучения: аудитории, оборудование, преподаватели.

### Технические характеристики

| Параметр | Значение |
|----------|----------|
| Порт | 8106 |
| База данных | PostgreSQL (lms_resources) |
| Kafka Topics | `booking-events` |

### Функциональность

#### Типы ресурсов
- **Помещения**: аудитории, переговорки, лаборатории
- **Оборудование**: проекторы, ноутбуки, камеры
- **Люди**: преподаватели, тренеры, консультанты
- **Транспорт**: корпоративные автомобили

#### Возможности
- Календарь доступности
- Конфликты и автоматическое разрешение
- Правила бронирования (мин/макс время, предупреждение)
- Подтверждение/отклонение заявок
- Интеграция с event-service

### API Endpoints

```yaml
# Ресурсы
POST   /api/v1/resources                       # Создать ресурс
GET    /api/v1/resources                       # Список ресурсов
GET    /api/v1/resources/{id}                  # Детали
PUT    /api/v1/resources/{id}                  # Обновить
DELETE /api/v1/resources/{id}                  # Удалить

# Доступность
GET    /api/v1/resources/{id}/availability     # Календарь доступности
GET    /api/v1/resources/available             # Поиск свободных
       ?type={type}&from={datetime}&to={datetime}&capacity={min}

# Бронирования
POST   /api/v1/bookings                        # Создать бронь
GET    /api/v1/bookings                        # Мои брони
GET    /api/v1/bookings/{id}                   # Детали
PUT    /api/v1/bookings/{id}                   # Изменить
DELETE /api/v1/bookings/{id}                   # Отменить

# Подтверждение
POST   /api/v1/bookings/{id}/approve           # Одобрить
POST   /api/v1/bookings/{id}/reject            # Отклонить
GET    /api/v1/bookings/pending                # Ожидающие одобрения

# Расписание тренеров
GET    /api/v1/resources/trainers/{id}/schedule
POST   /api/v1/resources/trainers/{id}/availability  # Установить доступность
```

### Схема базы данных

```sql
-- Ресурсы
CREATE TABLE resources (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    resource_type VARCHAR(50) NOT NULL,    -- 'room', 'equipment', 'trainer', 'vehicle'

    -- Для помещений
    capacity INT,
    location VARCHAR(500),
    floor VARCHAR(50),
    building VARCHAR(255),
    amenities TEXT[],                      -- ['projector', 'whiteboard', 'video_conf']

    -- Для оборудования
    serial_number VARCHAR(100),
    model VARCHAR(255),

    -- Для тренеров (ссылка на user_id)
    trainer_user_id BIGINT,
    specializations TEXT[],
    hourly_rate DECIMAL(10,2),

    -- Общее
    organization_id BIGINT NOT NULL,
    is_active BOOLEAN DEFAULT true,
    requires_approval BOOLEAN DEFAULT false,

    -- Правила бронирования
    min_booking_minutes INT DEFAULT 30,
    max_booking_minutes INT DEFAULT 480,
    advance_booking_days INT DEFAULT 30,

    -- Изображения
    photo_urls TEXT[],

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Бронирования
CREATE TABLE bookings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    resource_id UUID REFERENCES resources(id),
    booked_by BIGINT NOT NULL,

    -- Время
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    timezone VARCHAR(50) DEFAULT 'UTC',

    -- Детали
    title VARCHAR(255),
    purpose TEXT,
    attendees_count INT,

    -- Связи
    event_id UUID,                         -- Связь с event-service
    course_id BIGINT,

    -- Статус
    status VARCHAR(50) DEFAULT 'pending',  -- 'pending', 'approved', 'rejected', 'cancelled', 'completed'
    approved_by BIGINT,
    approved_at TIMESTAMP,
    rejection_reason TEXT,

    -- Повторение
    is_recurring BOOLEAN DEFAULT false,
    recurrence_rule VARCHAR(500),          -- RFC 5545 RRULE
    parent_booking_id UUID,                -- Для повторяющихся

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Доступность тренеров
CREATE TABLE trainer_availability (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trainer_user_id BIGINT NOT NULL,

    -- Регулярное расписание
    day_of_week INT,                       -- 0=Sunday, 6=Saturday
    start_time TIME,
    end_time TIME,

    -- Или конкретные даты
    specific_date DATE,
    is_available BOOLEAN DEFAULT true,     -- false = выходной

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Блокировки (техобслуживание, отпуск)
CREATE TABLE resource_blocks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    resource_id UUID REFERENCES resources(id),

    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    reason VARCHAR(500),
    block_type VARCHAR(50),                -- 'maintenance', 'holiday', 'private'

    created_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Индексы
CREATE INDEX idx_resources_type ON resources(resource_type);
CREATE INDEX idx_resources_org ON resources(organization_id);
CREATE INDEX idx_bookings_resource ON bookings(resource_id);
CREATE INDEX idx_bookings_time ON bookings(start_time, end_time);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_trainer_avail ON trainer_availability(trainer_user_id, day_of_week);
```

---

## 8. Audit Logging Service (Глобальный аудит)

### Назначение
Централизованный сбор, хранение и анализ логов действий пользователей для безопасности и compliance (SOC2, GDPR, ISO 27001).

### Технические характеристики

| Параметр | Значение |
|----------|----------|
| Порт | 8107 |
| База данных | PostgreSQL (lms_audit) + TimescaleDB |
| Хранилище | S3/MinIO для архивов |
| Kafka Consumer | `audit-collector` |
| Retention | Hot: 90 дней, Cold: 7 лет |

### Функциональность

#### Типы событий
- **Authentication**: login, logout, password change, MFA
- **Authorization**: permission changes, role assignments
- **Data Access**: read, export, download sensitive data
- **Data Modification**: create, update, delete
- **Administrative**: user management, settings changes
- **Security**: suspicious activity, failed attempts

#### Возможности
- **Immutable logs** — защита от изменения
- **Digital signatures** — подпись каждой записи
- **Real-time streaming** — мгновенная запись
- **Search & Filter** — поиск по любым параметрам
- **Reports** — compliance отчёты
- **Alerts** — уведомления о подозрительной активности
- **Retention policies** — автоматическая архивация

### API Endpoints

```yaml
# Поиск и просмотр
GET    /api/v1/audit/logs                  # Поиск логов
       ?userId={id}&action={action}&from={date}&to={date}
       &resource={type}&severity={level}&page={p}&size={s}
GET    /api/v1/audit/logs/{id}             # Детали записи

# Пользовательская активность
GET    /api/v1/audit/users/{userId}/activity
GET    /api/v1/audit/users/{userId}/sessions
GET    /api/v1/audit/users/{userId}/timeline

# Ресурсы
GET    /api/v1/audit/resources/{type}/{id}/history

# Отчёты
POST   /api/v1/audit/reports/generate
       Body: { reportType, dateRange, filters }
GET    /api/v1/audit/reports/{id}
GET    /api/v1/audit/reports/{id}/download

# Compliance
GET    /api/v1/audit/compliance/gdpr/user/{userId}  # Все данные пользователя
POST   /api/v1/audit/compliance/gdpr/export/{userId}
GET    /api/v1/audit/compliance/access-report

# Алерты
GET    /api/v1/audit/alerts
POST   /api/v1/audit/alerts/rules          # Создать правило алерта
PUT    /api/v1/audit/alerts/rules/{id}

# Администрирование
POST   /api/v1/audit/archive               # Архивировать старые логи
GET    /api/v1/audit/stats                 # Статистика
```

### Kafka Events (Consumer)

```java
// Слушает ВСЕ события из системы
@KafkaListener(topics = {
    "user-events",
    "course-events",
    "enrollment-events",
    "payment-events",
    "auth-events",
    "admin-events",
    "security-events"
}, groupId = "audit-collector")
```

### Схема базы данных

```sql
-- Основная таблица логов (TimescaleDB hypertable)
CREATE TABLE audit_logs (
    id UUID DEFAULT gen_random_uuid(),
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    -- Кто
    user_id BIGINT,
    username VARCHAR(255),
    user_email VARCHAR(255),
    user_role VARCHAR(100),
    organization_id BIGINT,

    -- Что
    action VARCHAR(100) NOT NULL,          -- 'user.login', 'course.create', 'grade.update'
    action_category VARCHAR(50) NOT NULL,  -- 'auth', 'data', 'admin', 'security'

    -- На чём
    resource_type VARCHAR(100),            -- 'user', 'course', 'enrollment'
    resource_id VARCHAR(100),
    resource_name VARCHAR(500),

    -- Детали
    description TEXT,
    old_value JSONB,                       -- Предыдущее состояние
    new_value JSONB,                       -- Новое состояние
    metadata JSONB,                        -- Дополнительные данные

    -- Контекст
    ip_address INET,
    user_agent TEXT,
    session_id VARCHAR(100),
    request_id VARCHAR(100),

    -- Классификация
    severity VARCHAR(20) DEFAULT 'info',   -- 'info', 'warning', 'critical'
    is_sensitive BOOLEAN DEFAULT false,    -- GDPR-relevant

    -- Целостность
    checksum VARCHAR(64),                  -- SHA-256 хеш записи

    PRIMARY KEY (id, timestamp)
);

-- Конвертация в hypertable (TimescaleDB)
SELECT create_hypertable('audit_logs', 'timestamp');

-- Сессии пользователей
CREATE TABLE user_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id BIGINT NOT NULL,
    session_token_hash VARCHAR(64),

    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ended_at TIMESTAMP WITH TIME ZONE,
    last_activity_at TIMESTAMP WITH TIME ZONE,

    ip_address INET,
    user_agent TEXT,
    device_type VARCHAR(50),
    location_country VARCHAR(100),
    location_city VARCHAR(100),

    is_suspicious BOOLEAN DEFAULT false,
    termination_reason VARCHAR(100)
);

-- Правила алертов
CREATE TABLE audit_alert_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,

    -- Условия
    condition_type VARCHAR(50) NOT NULL,   -- 'threshold', 'pattern', 'anomaly'
    conditions JSONB NOT NULL,
    /* Example:
    {
        "action": "user.login",
        "threshold": 5,
        "window_minutes": 10,
        "group_by": "ip_address"
    }
    */

    severity VARCHAR(20) NOT NULL,
    is_active BOOLEAN DEFAULT true,

    -- Действия
    notify_channels TEXT[],                -- ['email', 'slack', 'webhook']
    notify_recipients JSONB,

    created_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Алерты
CREATE TABLE audit_alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rule_id UUID REFERENCES audit_alert_rules(id),

    triggered_at TIMESTAMP WITH TIME ZONE NOT NULL,
    description TEXT,
    affected_user_id BIGINT,
    affected_resource JSONB,

    status VARCHAR(50) DEFAULT 'open',     -- 'open', 'acknowledged', 'resolved', 'false_positive'
    resolved_by BIGINT,
    resolved_at TIMESTAMP,
    resolution_notes TEXT,

    -- Связанные логи
    related_log_ids UUID[]
);

-- Архивные метаданные
CREATE TABLE audit_archives (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    archive_date DATE NOT NULL,
    date_from TIMESTAMP WITH TIME ZONE NOT NULL,
    date_to TIMESTAMP WITH TIME ZONE NOT NULL,

    records_count BIGINT,
    file_size_bytes BIGINT,
    storage_path VARCHAR(1000),
    checksum VARCHAR(64),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Индексы
CREATE INDEX idx_audit_user ON audit_logs(user_id, timestamp DESC);
CREATE INDEX idx_audit_action ON audit_logs(action, timestamp DESC);
CREATE INDEX idx_audit_resource ON audit_logs(resource_type, resource_id, timestamp DESC);
CREATE INDEX idx_audit_severity ON audit_logs(severity, timestamp DESC);
CREATE INDEX idx_audit_org ON audit_logs(organization_id, timestamp DESC);
CREATE INDEX idx_sessions_user ON user_sessions(user_id, started_at DESC);
CREATE INDEX idx_alerts_status ON audit_alerts(status, triggered_at DESC);
```

### Архитектура

```
┌─────────────────────────────────────────────────────────────────┐
│                     All Microservices                           │
│   auth, course, enrollment, payment, admin, ...                 │
└──────────────────────────┬──────────────────────────────────────┘
                           │ Events
                           ▼
              ┌─────────────────────────┐
              │         Kafka           │
              │    (multiple topics)    │
              └────────────┬────────────┘
                           │
                           ▼
              ┌─────────────────────────┐
              │   Audit Logging Service │
              │        :8107            │
              └────────────┬────────────┘
                           │
         ┌─────────────────┼─────────────────┐
         │                 │                 │
         ▼                 ▼                 ▼
   ┌───────────┐    ┌───────────┐    ┌───────────┐
   │TimescaleDB│    │   MinIO   │    │   Alert   │
   │ (hot data)│    │ (archives)│    │  Manager  │
   └───────────┘    └───────────┘    └───────────┘
```

---

## 9. LTI Service (Learning Tools Interoperability)

### Назначение
Реализация стандарта LTI для интеграции с внешними образовательными инструментами и платформами.

### Технические характеристики

| Параметр | Значение |
|----------|----------|
| Порт | 8108 |
| База данных | PostgreSQL (lms_lti) |
| Стандарты | LTI 1.1, LTI 1.3, LTI Advantage |

### Функциональность

#### LTI Provider (Tool)
FREE LMS как инструмент, встраиваемый в другие LMS:
- Размещение курсов FREE LMS в Canvas, Moodle, Blackboard
- Передача оценок обратно в основную LMS
- Single Sign-On через LTI

#### LTI Consumer (Platform)
FREE LMS как платформа, использующая внешние инструменты:
- Интеграция H5P, Kahoot, Padlet
- Виртуальные лаборатории
- Внешние тестовые системы
- Proctoring tools

#### Поддерживаемые сервисы LTI Advantage
- **Assignment and Grade Services** (AGS)
- **Names and Role Provisioning Services** (NRPS)
- **Deep Linking**

### API Endpoints

```yaml
# LTI 1.3 Launch (Provider)
POST   /lti/launch                         # Точка входа LTI
GET    /lti/jwks                           # Public keys
POST   /lti/token                          # OAuth2 token endpoint

# LTI Consumer (внешние инструменты)
POST   /api/v1/lti/tools                   # Зарегистрировать инструмент
GET    /api/v1/lti/tools                   # Список инструментов
GET    /api/v1/lti/tools/{id}              # Детали
PUT    /api/v1/lti/tools/{id}              # Обновить
DELETE /api/v1/lti/tools/{id}              # Удалить

# Размещения
POST   /api/v1/lti/placements              # Создать размещение
GET    /api/v1/lti/placements              # Список
GET    /api/v1/lti/placements/{id}/launch  # URL для запуска

# Grade passback
POST   /api/v1/lti/grades                  # Принять оценку от инструмента
GET    /api/v1/lti/grades/{launchId}       # Получить оценки

# Deep Linking
POST   /api/v1/lti/deep-link/select        # Выбор контента из инструмента
POST   /api/v1/lti/deep-link/return        # Возврат выбранного

# Администрирование
GET    /api/v1/lti/platforms               # Зарегистрированные платформы
POST   /api/v1/lti/platforms               # Добавить платформу
```

### Схема базы данных

```sql
-- Зарегистрированные платформы (для Provider mode)
CREATE TABLE lti_platforms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    platform_id VARCHAR(500) NOT NULL,     -- issuer URL
    client_id VARCHAR(255) NOT NULL,

    -- Ключи
    public_keyset_url VARCHAR(1000),
    access_token_url VARCHAR(1000),
    authorization_url VARCHAR(1000),

    -- Deployment
    deployment_id VARCHAR(255),

    is_active BOOLEAN DEFAULT true,
    organization_id BIGINT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(platform_id, client_id)
);

-- Зарегистрированные инструменты (для Consumer mode)
CREATE TABLE lti_tools (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,

    -- LTI 1.3 конфигурация
    lti_version VARCHAR(10) DEFAULT '1.3',
    client_id VARCHAR(255),
    issuer VARCHAR(500),

    -- URLs
    launch_url VARCHAR(1000) NOT NULL,
    login_url VARCHAR(1000),
    keyset_url VARCHAR(1000),
    redirect_urls TEXT[],

    -- Для LTI 1.1
    consumer_key VARCHAR(255),
    shared_secret VARCHAR(255),

    -- Возможности
    supports_deep_linking BOOLEAN DEFAULT false,
    supports_grades BOOLEAN DEFAULT false,
    supports_nrps BOOLEAN DEFAULT false,

    -- Настройки
    custom_parameters JSONB,
    icon_url VARCHAR(1000),

    is_active BOOLEAN DEFAULT true,
    organization_id BIGINT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Размещения инструментов в курсах
CREATE TABLE lti_placements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tool_id UUID REFERENCES lti_tools(id),

    course_id BIGINT NOT NULL,
    lesson_id BIGINT,

    title VARCHAR(255) NOT NULL,
    description TEXT,

    -- Параметры запуска
    custom_parameters JSONB,

    -- Оценки
    is_graded BOOLEAN DEFAULT false,
    max_score DECIMAL(5,2),
    resource_link_id VARCHAR(255),

    created_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Запуски LTI
CREATE TABLE lti_launches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Контекст
    tool_id UUID REFERENCES lti_tools(id),
    platform_id UUID REFERENCES lti_platforms(id),
    placement_id UUID REFERENCES lti_placements(id),

    -- Пользователь
    user_id BIGINT NOT NULL,

    -- LTI данные
    resource_link_id VARCHAR(255),
    context_id VARCHAR(255),

    -- Время
    launched_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Состояние (для grade passback)
    state VARCHAR(100)
);

-- Оценки от инструментов
CREATE TABLE lti_grades (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    launch_id UUID REFERENCES lti_launches(id),
    placement_id UUID REFERENCES lti_placements(id),

    user_id BIGINT NOT NULL,
    score DECIMAL(5,2),
    max_score DECIMAL(5,2),

    -- LTI Grade данные
    score_given DECIMAL(10,4),
    score_maximum DECIMAL(10,4),
    activity_progress VARCHAR(50),         -- 'Initialized', 'Started', 'InProgress', 'Submitted', 'Completed'
    grading_progress VARCHAR(50),          -- 'FullyGraded', 'Pending', 'PendingManual', 'Failed', 'NotReady'

    comment TEXT,

    received_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Индексы
CREATE INDEX idx_tools_org ON lti_tools(organization_id);
CREATE INDEX idx_placements_course ON lti_placements(course_id);
CREATE INDEX idx_launches_user ON lti_launches(user_id);
CREATE INDEX idx_grades_placement ON lti_grades(placement_id);
```

---

## 10. Bot Platform Service (Микрообучение через мессенджеры)

### Назначение
Платформа для доставки микро-уроков и интерактивного обучения через мессенджеры: Telegram, Slack, WhatsApp, Microsoft Teams.

### Технические характеристики

| Параметр | Значение |
|----------|----------|
| Порт | 8109 |
| База данных | PostgreSQL (lms_bots) |
| Redis | Состояние диалогов |
| Kafka Topics | `bot-events` |

### Функциональность

#### Поддерживаемые платформы
- **Telegram** — полная поддержка
- **Slack** — корпоративные команды
- **WhatsApp Business** — через Cloud API
- **Microsoft Teams** — интеграция с Office 365
- **Discord** — community/education

#### Типы контента
- **Микро-уроки** — карточки с теорией (≤3 минуты)
- **Флеш-карты** — spaced repetition
- **Квизы** — тесты с мгновенной обратной связью
- **Daily challenges** — ежедневные задания
- **Напоминания** — reminder о курсах
- **Прогресс** — отчёт о достижениях

#### Возможности
- Персонализированные learning paths
- A/B тестирование контента
- Push-напоминания
- Геймификация (стрики, баллы)
- Аналитика engagement

### API Endpoints

```yaml
# Webhook endpoints для мессенджеров
POST   /webhooks/telegram                  # Telegram updates
POST   /webhooks/slack                     # Slack events
POST   /webhooks/whatsapp                  # WhatsApp webhooks
POST   /webhooks/teams                     # MS Teams webhooks

# Боты
POST   /api/v1/bots                        # Создать бота
GET    /api/v1/bots                        # Список ботов
GET    /api/v1/bots/{id}                   # Детали
PUT    /api/v1/bots/{id}                   # Обновить
DELETE /api/v1/bots/{id}                   # Удалить

# Контент для ботов
POST   /api/v1/bots/content                # Создать микро-урок
GET    /api/v1/bots/content                # Список контента
PUT    /api/v1/bots/content/{id}           # Обновить
GET    /api/v1/bots/content/{id}/preview   # Предпросмотр в разных платформах

# Кампании (рассылки)
POST   /api/v1/bots/campaigns              # Создать кампанию
GET    /api/v1/bots/campaigns              # Список
POST   /api/v1/bots/campaigns/{id}/start   # Запустить
POST   /api/v1/bots/campaigns/{id}/stop    # Остановить

# Подписчики
GET    /api/v1/bots/{botId}/subscribers    # Список подписчиков
POST   /api/v1/bots/{botId}/subscribers/import  # Импорт из LMS
GET    /api/v1/bots/{botId}/subscribers/{id}/history  # История диалога

# Прямые сообщения
POST   /api/v1/bots/send                   # Отправить сообщение
POST   /api/v1/bots/broadcast              # Массовая рассылка

# Аналитика
GET    /api/v1/bots/{botId}/analytics      # Статистика бота
GET    /api/v1/bots/campaigns/{id}/analytics  # Статистика кампании
```

### Схема базы данных

```sql
-- Боты
CREATE TABLE bots (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,

    platform VARCHAR(50) NOT NULL,         -- 'telegram', 'slack', 'whatsapp', 'teams'

    -- Credentials
    api_token VARCHAR(500),                -- Encrypted
    webhook_secret VARCHAR(255),

    -- Platform-specific
    telegram_username VARCHAR(255),
    slack_workspace_id VARCHAR(255),
    whatsapp_phone_id VARCHAR(255),
    teams_app_id VARCHAR(255),

    organization_id BIGINT NOT NULL,
    is_active BOOLEAN DEFAULT true,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Подписчики
CREATE TABLE bot_subscribers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bot_id UUID REFERENCES bots(id),

    -- Platform user ID
    platform_user_id VARCHAR(255) NOT NULL,
    platform_username VARCHAR(255),

    -- Связь с LMS
    lms_user_id BIGINT,

    -- Состояние
    is_subscribed BOOLEAN DEFAULT true,
    subscribed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    unsubscribed_at TIMESTAMP,

    -- Предпочтения
    timezone VARCHAR(50) DEFAULT 'UTC',
    language VARCHAR(10) DEFAULT 'ru',
    notification_preferences JSONB,

    -- Текущее состояние диалога
    current_state VARCHAR(100),
    state_data JSONB,

    -- Статистика
    messages_received INT DEFAULT 0,
    last_interaction_at TIMESTAMP,
    streak_days INT DEFAULT 0,
    total_xp INT DEFAULT 0,

    UNIQUE(bot_id, platform_user_id)
);

-- Микро-контент
CREATE TABLE bot_content (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    content_type VARCHAR(50) NOT NULL,     -- 'lesson', 'quiz', 'flashcard', 'challenge'
    title VARCHAR(255) NOT NULL,

    -- Контент (адаптивный под платформу)
    content JSONB NOT NULL,
    /* Example for lesson:
    {
        "text": "Сегодня изучим...",
        "media_url": "https://...",
        "buttons": [
            {"text": "Понятно!", "callback": "understood"},
            {"text": "Хочу больше", "callback": "more"}
        ]
    }
    */

    -- Для квизов
    correct_answer VARCHAR(100),
    explanation TEXT,

    -- Метаданные
    course_id BIGINT,
    lesson_id BIGINT,
    difficulty VARCHAR(20),
    estimated_time_seconds INT,
    xp_reward INT DEFAULT 10,

    organization_id BIGINT NOT NULL,
    is_active BOOLEAN DEFAULT true,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Кампании (последовательности сообщений)
CREATE TABLE bot_campaigns (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bot_id UUID REFERENCES bots(id),

    name VARCHAR(255) NOT NULL,
    description TEXT,

    campaign_type VARCHAR(50) NOT NULL,    -- 'drip', 'scheduled', 'triggered'

    -- Аудитория
    target_audience JSONB,                 -- Фильтры: курс, роль, активность

    -- Расписание
    schedule_type VARCHAR(50),             -- 'daily', 'weekly', 'custom'
    schedule_time TIME,
    schedule_days INT[],                   -- [1,2,3,4,5] = Mon-Fri
    schedule_cron VARCHAR(100),

    -- Контент
    content_ids UUID[],                    -- Последовательность контента

    -- Статус
    status VARCHAR(50) DEFAULT 'draft',    -- 'draft', 'active', 'paused', 'completed'
    started_at TIMESTAMP,
    ended_at TIMESTAMP,

    created_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- История сообщений
CREATE TABLE bot_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bot_id UUID REFERENCES bots(id),
    subscriber_id UUID REFERENCES bot_subscribers(id),

    direction VARCHAR(10) NOT NULL,        -- 'inbound', 'outbound'
    message_type VARCHAR(50),              -- 'text', 'image', 'quiz', 'callback'

    content JSONB NOT NULL,

    -- Для исходящих
    content_id UUID REFERENCES bot_content(id),
    campaign_id UUID REFERENCES bot_campaigns(id),

    -- Для callback/ответов
    callback_data VARCHAR(255),
    is_correct_answer BOOLEAN,

    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delivered_at TIMESTAMP,
    read_at TIMESTAMP
);

-- Прогресс пользователя
CREATE TABLE bot_user_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subscriber_id UUID REFERENCES bot_subscribers(id),
    content_id UUID REFERENCES bot_content(id),

    status VARCHAR(50) DEFAULT 'sent',     -- 'sent', 'viewed', 'completed', 'skipped'

    -- Для квизов
    answer VARCHAR(255),
    is_correct BOOLEAN,
    attempts INT DEFAULT 0,

    xp_earned INT DEFAULT 0,

    started_at TIMESTAMP,
    completed_at TIMESTAMP,

    UNIQUE(subscriber_id, content_id)
);

-- Индексы
CREATE INDEX idx_bots_org ON bots(organization_id);
CREATE INDEX idx_subscribers_bot ON bot_subscribers(bot_id);
CREATE INDEX idx_subscribers_lms ON bot_subscribers(lms_user_id);
CREATE INDEX idx_content_course ON bot_content(course_id);
CREATE INDEX idx_messages_subscriber ON bot_messages(subscriber_id, sent_at DESC);
CREATE INDEX idx_campaigns_status ON bot_campaigns(status);
```

### Архитектура бота

```
┌───────────────────────────────────────────────────────────────────┐
│                      Messenger Platforms                          │
│   Telegram     Slack      WhatsApp     Teams      Discord         │
└───────────┬───────┬───────────┬─────────┬──────────┬──────────────┘
            │       │           │         │          │
            ▼       ▼           ▼         ▼          ▼
┌───────────────────────────────────────────────────────────────────┐
│                    Webhook Handler Layer                          │
│              (Validates, normalizes messages)                     │
└───────────────────────────────┬───────────────────────────────────┘
                                │
                                ▼
┌───────────────────────────────────────────────────────────────────┐
│                    Bot Platform Service                           │
│                          :8109                                    │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐               │
│  │  Message    │  │   State     │  │  Campaign   │               │
│  │  Router     │  │   Machine   │  │  Scheduler  │               │
│  └─────────────┘  └─────────────┘  └─────────────┘               │
└───────────────────────────────┬───────────────────────────────────┘
                                │
        ┌───────────────────────┼───────────────────────┐
        │                       │                       │
        ▼                       ▼                       ▼
┌───────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  PostgreSQL   │     │     Redis       │     │     Kafka       │
│  (data)       │     │ (state/cache)   │     │ (events)        │
└───────────────┘     └─────────────────┘     └─────────────────┘
```

---

## Интеграционная карта

### Взаимодействие новых сервисов с существующими

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            EXISTING SERVICES                                │
└─────────────────────────────────────────────────────────────────────────────┘

  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
  │    auth      │    │    course    │    │  enrollment  │    │  analytics   │
  │   service    │    │   service    │    │   service    │    │   service    │
  └──────┬───────┘    └──────┬───────┘    └──────┬───────┘    └──────┬───────┘
         │                   │                   │                   │
         │                   │                   │                   │
  ═══════╪═══════════════════╪═══════════════════╪═══════════════════╪═════════
         │                   │                   │                   │
         │    INTEGRATIONS   │                   │                   │
         │                   │                   │                   │
         │    ┌──────────────▼──────────────┐    │                   │
         │    │       search-service        │◀───┼───────────────────┤
         │    │          :8100              │    │                   │
         │    └─────────────────────────────┘    │                   │
         │                   │                   │                   │
         │    ┌──────────────▼──────────────┐    │                   │
         ├───▶│   media-processing-service  │◀───┤                   │
         │    │          :8101              │    │                   │
         │    └─────────────────────────────┘    │                   │
         │                   │                   │                   │
         │    ┌──────────────▼──────────────┐    │                   │
         ├───▶│       event-service         │◀───┼───────────────────┤
         │    │          :8102              │    │                   │
         │    └─────────────────────────────┘    │                   │
         │                   │                   │                   │
         │    ┌──────────────▼──────────────┐    │                   │
         │    │     authoring-service       │◀───┤                   │
         │    │          :8103              │    │                   │
         │    └─────────────────────────────┘    │                   │
         │                   │                   │                   │
         │    ┌──────────────▼──────────────┐    │                   │
         ├───▶│     proctoring-service      │◀───┼───────────────────┤
         │    │          :8104              │    │                   │
         │    └─────────────────────────────┘    │                   │
         │                   │                   │                   │
         │    ┌──────────────▼──────────────┐    │                   │
         ├───▶│ assignment-review-service   │◀───┤                   │
         │    │          :8105              │    │                   │
         │    └─────────────────────────────┘    │                   │
         │                   │                   │                   │
         │    ┌──────────────▼──────────────┐    │                   │
         │    │  resource-booking-service   │◀───┤                   │
         │    │          :8106              │    │                   │
         │    └─────────────────────────────┘    │                   │
         │                   │                   │                   │
         │    ┌─────────────────────────────┐    │                   │
         ├───▶│   audit-logging-service     │◀───┼───────────────────┤
         │    │          :8107              │◀───────────────────────┤
         │    └─────────────────────────────┘    │                   │
         │                   │                   │                   │
         │    ┌──────────────▼──────────────┐    │                   │
         ├───▶│       lti-service           │◀───┼───────────────────┤
         │    │          :8108              │    │                   │
         │    └─────────────────────────────┘    │                   │
         │                   │                   │                   │
         │    ┌──────────────▼──────────────┐    │                   │
         └───▶│   bot-platform-service      │◀───┴───────────────────┘
              │          :8109              │
              └─────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                            NEW SERVICES (10)                                │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Kafka Topics (новые)

| Topic | Producer | Consumers |
|-------|----------|-----------|
| `media-upload-events` | course-service | media-processing-service |
| `media-processed-events` | media-processing-service | course-service, search-service |
| `event-events` | event-service | notification-service, analytics-service |
| `content-events` | authoring-service | course-service, search-service |
| `proctoring-events` | proctoring-service | analytics-service, compliance-service |
| `assignment-events` | assignment-review-service | notification-service, gamification-service |
| `booking-events` | resource-booking-service | notification-service, event-service |
| `audit-events` | * (все сервисы) | audit-logging-service |
| `lti-events` | lti-service | course-service, analytics-service |
| `bot-events` | bot-platform-service | analytics-service, gamification-service |

---

## Требования к инфраструктуре

### Дополнительные компоненты

| Компонент | Назначение | Сервисы |
|-----------|------------|---------|
| **Elasticsearch 8.x** | Полнотекстовый поиск | search-service |
| **TimescaleDB** | Time-series данные аудита | audit-logging-service |
| **FFmpeg** | Транскодирование видео | media-processing-service |
| **ImageMagick** | Обработка изображений | media-processing-service |
| **Docker Sandbox** | Запуск пользовательского кода | assignment-review-service |
| **TensorFlow/ONNX** | ML модели прокторинга | proctoring-service |

### Обновлённый docker-compose (дополнения)

```yaml
version: '3.8'

services:
  # Elasticsearch для поиска
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - "ES_JAVA_OPTS=-Xms1g -Xmx1g"
    ports:
      - "9200:9200"
    volumes:
      - elasticsearch-data:/usr/share/elasticsearch/data
    networks:
      - lms-network

  # TimescaleDB для аудита
  timescaledb:
    image: timescale/timescaledb:latest-pg16
    environment:
      POSTGRES_DB: lms_audit
      POSTGRES_USER: audit_user
      POSTGRES_PASSWORD: ${TIMESCALE_PASSWORD}
    ports:
      - "5433:5432"
    volumes:
      - timescale-data:/var/lib/postgresql/data
    networks:
      - lms-network

  # Новые сервисы
  search-service:
    build: ./services/search-service
    ports:
      - "8100:8100"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - ELASTICSEARCH_HOST=elasticsearch
    depends_on:
      - elasticsearch
      - kafka
    networks:
      - lms-network

  media-processing-service:
    build: ./services/media-processing-service
    ports:
      - "8101:8101"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    volumes:
      - /tmp/media-processing:/tmp/processing
    depends_on:
      - postgres
      - minio
      - kafka
    networks:
      - lms-network

  event-service:
    build: ./services/event-service
    ports:
      - "8102:8102"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    depends_on:
      - postgres
      - kafka
    networks:
      - lms-network

  authoring-service:
    build: ./services/authoring-service
    ports:
      - "8103:8103"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    depends_on:
      - postgres
      - minio
      - kafka
    networks:
      - lms-network

  proctoring-service:
    build: ./services/proctoring-service
    ports:
      - "8104:8104"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    depends_on:
      - postgres
      - redis
      - minio
    networks:
      - lms-network

  assignment-review-service:
    build: ./services/assignment-review-service
    ports:
      - "8105:8105"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    depends_on:
      - postgres
      - kafka
    networks:
      - lms-network

  resource-booking-service:
    build: ./services/resource-booking-service
    ports:
      - "8106:8106"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    depends_on:
      - postgres
      - kafka
    networks:
      - lms-network

  audit-logging-service:
    build: ./services/audit-logging-service
    ports:
      - "8107:8107"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    depends_on:
      - timescaledb
      - kafka
      - minio
    networks:
      - lms-network

  lti-service:
    build: ./services/lti-service
    ports:
      - "8108:8108"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    depends_on:
      - postgres
      - kafka
    networks:
      - lms-network

  bot-platform-service:
    build: ./services/bot-platform-service
    ports:
      - "8109:8109"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    depends_on:
      - postgres
      - redis
      - kafka
    networks:
      - lms-network

volumes:
  elasticsearch-data:
  timescale-data:

networks:
  lms-network:
    driver: bridge
```

### Требования к ресурсам (production)

| Сервис | CPU | RAM | Диск | Replicas |
|--------|-----|-----|------|----------|
| search-service | 2 | 4GB | 100GB (ES) | 2 |
| media-processing-service | 4 | 8GB | 500GB | 2-4 (autoscale) |
| event-service | 1 | 2GB | 10GB | 2 |
| authoring-service | 1 | 2GB | 50GB | 2 |
| proctoring-service | 2 | 4GB | 200GB | 2-4 (autoscale) |
| assignment-review-service | 2 | 4GB | 50GB | 2 |
| resource-booking-service | 0.5 | 1GB | 5GB | 2 |
| audit-logging-service | 1 | 2GB | 1TB (TimescaleDB) | 2 |
| lti-service | 0.5 | 1GB | 5GB | 2 |
| bot-platform-service | 1 | 2GB | 10GB | 2 |

---

## Заключение

Добавление этих 10 микросервисов превратит FREE LMS в полноценную Enterprise-платформу, способную конкурировать с такими решениями как:
- Cornerstone OnDemand
- SAP SuccessFactors Learning
- Docebo
- Absorb LMS

### Итоговая архитектура: 32 микросервиса

```
Infrastructure (3):     service-registry, config-server, gateway-service
Core Services (7):      auth, course, enrollment, payment, notification, analytics, organization
Feature Services (10):  learning-path, skills, gamification, idp, feedback, mentoring,
                       social-learning, compliance, reporting, integration
Platform Services (2):  marketplace, onboarding
NEW - Media (2):        search, media-processing
NEW - Live (2):         event, resource-booking
NEW - Assessment (3):   authoring, proctoring, assignment-review
NEW - Compliance (1):   audit-logging
NEW - Integration (2):  lti, bot-platform
```

---

*Документация создана: 2025-11-27*
*Версия: 1.0*
