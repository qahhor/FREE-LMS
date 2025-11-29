# Frontend Development Guide

## О Frontend

Этот репозиторий (**FREE-LMS**) содержит только **Backend API** на Java/Spring Boot. Frontend-приложение разрабатывается отдельно и может быть реализовано на любом современном фреймворке.

## Рекомендуемые технологии

| Фреймворк | Описание |
|-----------|----------|
| **Angular 17+** | Enterprise-grade, TypeScript, Material UI |
| **React 18+** | Гибкий, большая экосистема |
| **Vue 3+** | Простой старт, отличная документация |
| **Next.js** | SSR, SEO-оптимизация |

## API Интеграция

### Base URL
```
http://localhost:8080/api/v1
```

### Аутентификация

Backend использует JWT токены:

```typescript
// Login
POST /api/v1/auth/login
Body: { "email": "user@example.com", "password": "password" }
Response: { "accessToken": "...", "refreshToken": "...", "expiresIn": 3600 }

// Все защищённые запросы
Headers: {
  "Authorization": "Bearer <accessToken>"
}

// Refresh token
POST /api/v1/auth/refresh
Body: { "refreshToken": "..." }
```

### Основные Endpoints

```typescript
// Курсы
GET    /api/v1/courses              // Список курсов
GET    /api/v1/courses/{id}         // Детали курса
POST   /api/v1/courses              // Создать курс (admin)

// Записи
POST   /api/v1/enrollments/courses/{id}  // Записаться на курс
GET    /api/v1/enrollments/my            // Мои записи
PUT    /api/v1/enrollments/{id}/progress // Обновить прогресс

// Сертификаты
GET    /api/v1/certificates/my           // Мои сертификаты

// Платежи
POST   /api/v1/payments                  // Создать платёж
```

### OpenAPI/Swagger

Полная документация API доступна:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Пример интеграции (Angular)

### HTTP Interceptor для JWT

```typescript
// auth.interceptor.ts
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.authService.getAccessToken();

    if (token) {
      req = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    return next.handle(req);
  }
}
```

### API Service

```typescript
// api.service.ts
@Injectable({ providedIn: 'root' })
export class ApiService {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getCourses(): Observable<Course[]> {
    return this.http.get<ApiResponse<Course[]>>(`${this.baseUrl}/courses`)
      .pipe(map(res => res.data));
  }

  enrollCourse(courseId: number): Observable<Enrollment> {
    return this.http.post<ApiResponse<Enrollment>>(
      `${this.baseUrl}/enrollments/courses/${courseId}`, {}
    ).pipe(map(res => res.data));
  }
}
```

## Пример интеграции (React)

### Axios Instance

```typescript
// api.ts
import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1'
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;
```

### React Query Hook

```typescript
// useCourses.ts
import { useQuery } from '@tanstack/react-query';
import api from './api';

export function useCourses() {
  return useQuery({
    queryKey: ['courses'],
    queryFn: async () => {
      const { data } = await api.get('/courses');
      return data.data;
    }
  });
}
```

## CORS Configuration

Backend настроен для приёма запросов с любых origins в development режиме. Для production настройте `CORS_ALLOWED_ORIGINS`:

```yaml
# application.yml
cors:
  allowed-origins: https://your-frontend-domain.com
  allowed-methods: GET, POST, PUT, DELETE, OPTIONS
  allowed-headers: "*"
  allow-credentials: true
```

## WebSocket (Real-time)

Для real-time уведомлений используйте WebSocket endpoint:

```
ws://localhost:8080/ws/notifications
```

## Структура рекомендуемого Frontend проекта

```
frontend/
├── src/
│   ├── app/
│   │   ├── core/           # Services, guards, interceptors
│   │   ├── shared/         # Shared components, pipes
│   │   ├── features/       # Feature modules
│   │   │   ├── auth/
│   │   │   ├── courses/
│   │   │   ├── dashboard/
│   │   │   └── profile/
│   │   └── app.component.ts
│   ├── assets/
│   └── environments/
├── package.json
└── README.md
```

## Контакты

По вопросам интеграции: opensource@smartup24.com
