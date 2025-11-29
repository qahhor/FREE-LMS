# Contributing to FREE LMS

Спасибо за интерес к участию в разработке FREE LMS! Этот документ поможет вам начать.

## Содержание

- [Кодекс поведения](#кодекс-поведения)
- [Как помочь проекту](#как-помочь-проекту)
- [Настройка окружения](#настройка-окружения)
- [Стандарты кода](#стандарты-кода)
- [Процесс Pull Request](#процесс-pull-request)

---

## Кодекс поведения

Участвуя в проекте, вы соглашаетесь следовать нашему кодексу поведения:

- Уважайте других участников
- Конструктивная критика приветствуется
- Будьте открыты к различным точкам зрения
- Фокусируйтесь на том, что лучше для проекта

---

## Как помочь проекту

### Сообщить об ошибке

Перед созданием issue проверьте, не сообщали ли об этой ошибке ранее.

**Включите в описание:**
- Чёткое название проблемы
- Шаги для воспроизведения
- Ожидаемое поведение
- Фактическое поведение
- Скриншоты (если применимо)
- Окружение: ОС, версия Java, версия Docker

### Предложить улучшение

**Опишите:**
- Что вы хотите добавить/изменить
- Почему это будет полезно
- Примеры использования
- Возможные альтернативы

### Написать код

Приоритетные области:
- Тесты (покрытие > 80%)
- Документация
- Исправление багов
- Новые функции (обсудите сначала в issue)

---

## Настройка окружения

### Требования

| Инструмент | Версия | Проверка |
|------------|--------|----------|
| Java | 21+ | `java -version` |
| Maven | 3.9+ | `mvn -version` |
| Docker | 24+ | `docker --version` |
| Docker Compose | 2.20+ | `docker compose version` |
| Git | 2.40+ | `git --version` |

### Установка

```bash
# 1. Fork репозитория на GitHub

# 2. Клонируйте свой fork
git clone https://github.com/YOUR_USERNAME/free-lms.git
cd free-lms

# 3. Добавьте upstream
git remote add upstream https://github.com/original/free-lms.git

# 4. Создайте ветку для работы
git checkout -b feature/my-feature

# 5. Запустите инфраструктуру
docker-compose -f docker-compose.monolith.yml up -d postgres redis kafka

# 6. Соберите проект
cd backend-java/monolith
mvn clean install -DskipTests

# 7. Запустите тесты
mvn test
```

### Структура проекта

```
free-lms/
├── backend-java/
│   └── monolith/                  # Модульный монолит
│       ├── src/main/java/
│       │   └── com/freelms/lms/
│       │       ├── auth/          # Модуль аутентификации
│       │       ├── course/        # Модуль курсов
│       │       ├── enrollment/    # Модуль записей
│       │       ├── payment/       # Модуль платежей
│       │       ├── common/        # Общие компоненты
│       │       └── config/        # Конфигурации
│       └── src/test/java/         # Тесты
├── docs/                          # Документация
│   ├── ARCHITECTURE.md
│   ├── MIGRATION_GUIDE.md
│   └── runbooks/
└── docker-compose.monolith.yml    # Docker Compose
```

---

## Стандарты кода

### Java Code Style

Мы используем **Google Java Style Guide** с небольшими модификациями:

```java
// Правильно
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}

// Неправильно
public class user_service {
    @Autowired UserRepository repo;
    public User FindById(Long Id) { return repo.findById(Id).get(); }
}
```

### Правила

1. **Именование:**
   - Классы: `PascalCase` (UserService, CourseController)
   - Методы/переменные: `camelCase` (findById, userName)
   - Константы: `UPPER_SNAKE_CASE` (MAX_RETRIES)
   - Пакеты: `lowercase` (com.freelms.lms.auth)

2. **Структура класса:**
   - Поля (static, final, обычные)
   - Конструкторы
   - Public методы
   - Private методы

3. **Dependency Injection:**
   - Используйте constructor injection (не @Autowired на поля)

4. **Документация:**
   - JavaDoc для public API
   - Комментарии для сложной логики

### Форматирование

```bash
# Проверить форматирование
mvn checkstyle:check

# Автоформатирование в IDE
# IntelliJ: Code → Reformat Code (Ctrl+Alt+L)
```

### Тестирование

```java
@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {
        // Given
        String email = "test@example.com";

        // When
        User user = userService.findByEmail(email);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(email);
    }
}
```

**Требования к тестам:**
- Покрытие > 80% для нового кода
- Unit тесты для сервисов
- Integration тесты для контроллеров
- Naming: `should<Action>When<Condition>`

---

## Процесс Pull Request

### 1. Подготовка

```bash
# Обновите main
git checkout main
git pull upstream main

# Создайте ветку
git checkout -b feature/add-user-export
```

### 2. Разработка

```bash
# Внесите изменения
# ...

# Проверьте код
cd backend-java/monolith
mvn clean verify

# Зафиксируйте
git add .
git commit -m "feat(auth): add user export functionality"
```

### 3. Commit Messages

Используем [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

**Типы:**
- `feat` — новая функциональность
- `fix` — исправление бага
- `docs` — документация
- `style` — форматирование
- `refactor` — рефакторинг
- `test` — тесты
- `chore` — прочее

**Примеры:**
```
feat(course): add course duplication feature
fix(auth): resolve JWT token expiration issue
docs(readme): update installation instructions
test(enrollment): add unit tests for EnrollmentService
```

### 4. Pull Request

```bash
# Отправьте ветку
git push origin feature/add-user-export
```

Создайте PR на GitHub с описанием:

```markdown
## Описание
Краткое описание изменений.

## Тип изменения
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Чеклист
- [ ] Код соответствует стилю проекта
- [ ] Тесты написаны и проходят
- [ ] Документация обновлена
- [ ] Self-review выполнен

## Screenshots (если применимо)
...

## Связанные Issues
Closes #123
```

### 5. Review

- Отвечайте на комментарии
- Вносите запрошенные изменения
- Запрашивайте повторный review

---

## Полезные команды

```bash
# Запуск всех тестов
cd backend-java/monolith
mvn test

# Запуск конкретного теста
mvn test -Dtest=UserServiceTest

# Сборка без тестов
mvn package -DskipTests

# Проверка стиля кода
mvn checkstyle:check

# Генерация отчёта о покрытии
mvn jacoco:report

# Запуск приложения
mvn spring-boot:run

# Запуск с профилем
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## Получение помощи

- GitHub Discussions — вопросы и обсуждения
- GitHub Issues — баги и предложения
- Email: opensource@smartup24.com
- Website: [www.smartup24.com](https://www.smartup24.com)

---

## Признание

Все контрибьюторы будут добавлены в файл CONTRIBUTORS.md и README проекта.

---

**Спасибо за ваш вклад в FREE LMS!**
