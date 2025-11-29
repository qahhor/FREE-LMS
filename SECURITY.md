# Security Policy — FREE LMS

## Поддерживаемые версии

| Версия | Поддержка |
|--------|-----------|
| 2.x.x (Monolith) | Активная поддержка |
| 1.x.x (Microservices) | Только критические патчи |

---

## Функции безопасности

### Аутентификация и авторизация

| Функция | Описание | Статус |
|---------|----------|--------|
| JWT Authentication | Access + Refresh токены | Реализовано |
| Token Rotation | Автоматическая ротация refresh токенов | Реализовано |
| Password Hashing | BCrypt (cost factor 10) | Реализовано |
| RBAC | Role-Based Access Control | Реализовано |
| OAuth2/OIDC | Внешняя аутентификация | Планируется |
| LDAP/AD | Корпоративная интеграция | Планируется |
| MFA | Multi-Factor Authentication | Планируется |

### Защита данных

| Функция | Описание | Статус |
|---------|----------|--------|
| TLS/HTTPS | Шифрование в транзите | Реализовано |
| Database Encryption | Шифрование PostgreSQL | Реализовано |
| Secrets Management | Environment variables | Реализовано |
| Data Masking | Маскирование в логах | Реализовано |
| CORS | Настраиваемая политика | Реализовано |
| GDPR Compliance | Право на удаление данных | Реализовано |

### API Security

| Функция | Описание | Статус |
|---------|----------|--------|
| Rate Limiting | Настраиваемые лимиты | Реализовано |
| Input Validation | Jakarta Validation | Реализовано |
| SQL Injection | JPA Parameterized Queries | Реализовано |
| XSS Prevention | Content Security Policy | Реализовано |
| CSRF Protection | Stateless JWT | Реализовано |
| Security Headers | HSTS, X-Frame-Options, etc. | Реализовано |

### Аудит и мониторинг

| Функция | Описание | Статус |
|---------|----------|--------|
| Audit Logging | Все действия пользователей | Реализовано |
| Login Attempts | Отслеживание попыток входа | Реализовано |
| IP Tracking | Логирование IP адресов | Реализовано |

---

## Конфигурация безопасности

### Security Headers

```java
// SecurityConfig.java
http.headers(headers -> headers
    .contentSecurityPolicy(csp -> csp
        .policyDirectives("default-src 'self'; " +
            "script-src 'self'; " +
            "style-src 'self' 'unsafe-inline'; " +
            "img-src 'self' data: https:; " +
            "frame-ancestors 'self'")
    )
    .referrerPolicy(referrer -> referrer
        .policy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
    )
    .httpStrictTransportSecurity(hsts -> hsts
        .includeSubDomains(true)
        .maxAgeInSeconds(31536000)
    )
);
```

### JWT Configuration

```yaml
jwt:
  secret: ${JWT_SECRET}  # Минимум 256 бит
  access-token-expiration: 15m
  refresh-token-expiration: 7d
```

---

## Production Security Checklist

### Перед развертыванием

- [ ] **Secrets**: Сгенерированы криптографически стойкие секреты
  ```bash
  openssl rand -base64 32  # JWT Secret
  openssl rand -base64 24  # DB Password
  ```

- [ ] **JWT Secret**: Минимум 256 бит, уникальный для каждой среды

- [ ] **Database**:
  - [ ] Сильные пароли
  - [ ] SSL/TLS подключения
  - [ ] Ограничение по IP
  - [ ] Read-only пользователи где возможно

- [ ] **Network**:
  - [ ] HTTPS только (redirect HTTP → HTTPS)
  - [ ] Firewall настроен
  - [ ] DDoS защита

- [ ] **Docker**:
  - [ ] Non-root пользователь
  - [ ] Resource limits
  - [ ] Security scanning образов

- [ ] **Kubernetes**:
  - [ ] Network Policies
  - [ ] Secrets encryption at rest
  - [ ] RBAC для кластера

---

## Сообщить об уязвимости

### Responsible Disclosure

**НЕ** сообщайте об уязвимостях через публичные GitHub Issues.

**Как сообщить:**

1. Email: security@smartup24.com

### Что включить в отчёт

```
Subject: [SECURITY] Brief description

1. Vulnerability Type: (XSS, SQL Injection, Auth Bypass, etc.)
2. Affected Component: (auth module, course module, etc.)
3. Steps to Reproduce:
   - Step 1
   - Step 2
   - ...
4. Impact Assessment: (Low/Medium/High/Critical)
5. Proof of Concept: (if available)
6. Suggested Fix: (if any)
```

### SLA ответа

| Severity | Response Time | Fix Time |
|----------|--------------|----------|
| Critical | 24 часа | 72 часа |
| High | 48 часов | 7 дней |
| Medium | 7 дней | 30 дней |
| Low | 14 дней | 90 дней |

---

## Security Best Practices

### Для разработчиков

1. **Input Validation**
   ```java
   @PostMapping("/users")
   public User createUser(@Valid @RequestBody CreateUserRequest request) {
       // @Valid обеспечивает валидацию
   }
   ```

2. **Parameterized Queries**
   ```java
   // Правильно
   @Query("SELECT u FROM User u WHERE u.email = :email")
   User findByEmail(@Param("email") String email);

   // Неправильно - SQL injection!
   @Query("SELECT u FROM User u WHERE u.email = '" + email + "'")
   ```

3. **Secrets Management**
   ```java
   // Правильно
   @Value("${jwt.secret}")
   private String jwtSecret;

   // Неправильно
   private String jwtSecret = "hardcoded-secret";
   ```

### Для операторов

1. **Регулярные обновления**
   ```bash
   # Проверка уязвимостей зависимостей
   mvn dependency-check:check
   ```

2. **Мониторинг**
   - Настройте алерты на неудачные входы
   - Отслеживайте аномальный трафик

3. **Backup**
   - Ежедневные бэкапы БД
   - Тестирование восстановления
   - Шифрование бэкапов

---

## Compliance

### Поддерживаемые стандарты

| Стандарт | Статус |
|----------|--------|
| OWASP Top 10 | Все уязвимости адресованы |
| GDPR | Право на удаление, экспорт данных |

---

## Контакты

- **Security Team**: security@smartup24.com
- **Website**: [www.smartup24.com](https://www.smartup24.com)
- **Bug Reports**: GitHub Issues (non-security)

---

**Последнее обновление**: 2024-11-29

**Версия документа**: 3.0
