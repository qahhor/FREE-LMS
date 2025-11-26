# Security Policy — FREE LMS

## 🛡️ Поддерживаемые версии

| Версия | Поддержка |
|--------|-----------|
| 2.x.x (Java) | ✅ Активная поддержка |
| 1.x.x (Legacy) | ⚠️ Только критические патчи |

---

## 🔐 Функции безопасности

### Аутентификация и авторизация

| Функция | Описание | Статус |
|---------|----------|--------|
| JWT Authentication | Access + Refresh токены | ✅ |
| Token Rotation | Автоматическая ротация refresh токенов | ✅ |
| Password Hashing | BCrypt (cost factor 10) | ✅ |
| RBAC | Role-Based Access Control | ✅ |
| MFA | Multi-Factor Authentication | 🔄 В разработке |
| OAuth2/OIDC | Внешняя аутентификация | ✅ |
| LDAP/AD | Корпоративная интеграция | ✅ |
| SSO | Single Sign-On | ✅ |

### Защита данных

| Функция | Описание | Статус |
|---------|----------|--------|
| TLS/HTTPS | Шифрование в транзите | ✅ |
| Database Encryption | Шифрование PostgreSQL | ✅ |
| Secrets Management | Kubernetes Secrets / Vault | ✅ |
| Data Masking | Маскирование в логах | ✅ |
| CORS | Настраиваемая политика | ✅ |
| GDPR Compliance | Право на удаление данных | ✅ |

### API Security

| Функция | Описание | Статус |
|---------|----------|--------|
| Rate Limiting | 100/1000/5000 req/min по ролям | ✅ |
| Input Validation | Jakarta Validation + Custom | ✅ |
| SQL Injection | JPA Parameterized Queries | ✅ |
| XSS Prevention | Content Security Policy | ✅ |
| CSRF Protection | Stateless JWT (disabled) | ✅ |
| Security Headers | HSTS, X-Frame-Options, etc. | ✅ |

### Аудит и мониторинг

| Функция | Описание | Статус |
|---------|----------|--------|
| Audit Logging | Все действия пользователей | ✅ |
| Login Attempts | Отслеживание попыток входа | ✅ |
| IP Tracking | Логирование IP адресов | ✅ |
| E-Signatures | Электронные подписи | ✅ |
| Compliance Reports | GDPR/ФЗ-152 отчёты | ✅ |

---

## 🔧 Конфигурация безопасности

### Production Security Headers

```java
// ProductionSecurityConfig.java
http.headers(headers -> headers
    .contentSecurityPolicy(csp -> csp
        .policyDirectives("default-src 'self'; " +
            "script-src 'self' 'unsafe-inline'; " +
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
        .preload(true)
    )
);
```

### Rate Limiting

```yaml
# Конфигурация по ролям
rate-limit:
  anonymous: 100    # запросов в минуту
  user: 1000        # запросов в минуту
  admin: 5000       # запросов в минуту
  burst-multiplier: 1.25
```

### JWT Configuration

```yaml
jwt:
  secret: ${JWT_SECRET}  # Минимум 256 бит
  access-token-expiration: 15m
  refresh-token-expiration: 7d
  issuer: freelms.io
```

---

## 📋 Production Security Checklist

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
  - [ ] VPN для внутренних сервисов
  - [ ] DDoS защита

- [ ] **Docker**:
  - [ ] Non-root пользователь
  - [ ] Read-only файловая система
  - [ ] Resource limits
  - [ ] Security scanning образов

- [ ] **Kubernetes**:
  - [ ] Network Policies
  - [ ] Pod Security Policies
  - [ ] Secrets encryption at rest
  - [ ] RBAC для кластера

---

## 🚨 Сообщить об уязвимости

### Responsible Disclosure

**НЕ** сообщайте об уязвимостях через публичные GitHub Issues.

**Как сообщить:**

1. 📧 Email: security@freelms.io
2. 🔐 PGP Key: [Download](https://freelms.io/.well-known/security.txt)

### Что включить в отчёт

```
Subject: [SECURITY] Brief description

1. Vulnerability Type: (XSS, SQL Injection, Auth Bypass, etc.)
2. Affected Component: (auth-service, gateway, etc.)
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

### Вознаграждение

Мы признаём вклад исследователей безопасности:
- Упоминание в Hall of Fame
- Благодарственное письмо
- Свяжитесь для обсуждения программы bug bounty

---

## 🔍 Известные уязвимости

### Устранённые

| CVE | Severity | Component | Fixed In |
|-----|----------|-----------|----------|
| - | - | - | - |

### В процессе исправления

Нет известных уязвимостей.

---

## 📚 Security Best Practices

### Для разработчиков

1. **Input Validation**
   ```java
   @PostMapping("/users")
   public User createUser(@Valid @RequestBody CreateUserRequest request) {
       // @Valid обеспечивает валидацию
   }
   ```

2. **Output Encoding**
   ```java
   // Используйте HtmlUtils для пользовательского ввода
   String safe = HtmlUtils.htmlEscape(userInput);
   ```

3. **Parameterized Queries**
   ```java
   // ✅ Правильно
   @Query("SELECT u FROM User u WHERE u.email = :email")
   User findByEmail(@Param("email") String email);

   // ❌ Неправильно
   @Query("SELECT u FROM User u WHERE u.email = '" + email + "'")
   ```

4. **Secrets Management**
   ```java
   // ✅ Правильно
   @Value("${jwt.secret}")
   private String jwtSecret;

   // ❌ Неправильно
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
   - Мониторьте rate limiting срабатывания
   - Отслеживайте аномальный трафик

3. **Backup**
   - Ежедневные бэкапы БД
   - Тестирование восстановления
   - Шифрование бэкапов

---

## 📜 Compliance

### Поддерживаемые стандарты

| Стандарт | Статус | Детали |
|----------|--------|--------|
| GDPR | ✅ | Право на удаление, экспорт данных |
| ФЗ-152 | ✅ | Локализация данных РФ |
| OWASP Top 10 | ✅ | Все уязвимости адресованы |
| SOC 2 | 🔄 | В процессе |
| ISO 27001 | 📋 | Планируется |

---

## 📞 Контакты

- **Security Team**: security@freelms.io
- **Bug Reports**: GitHub Issues (non-security)
- **Emergency**: +7-XXX-XXX-XXXX (24/7)

---

**Последнее обновление**: 2024-11-26

**Версия документа**: 2.0
