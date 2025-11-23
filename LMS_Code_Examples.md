# Примеры кода для LMS-системы
## Java Spring Backend + Angular Frontend

## 1. BACKEND - ПРИМЕРЫ КОДА

### 1.1 User Service - Управление пользователями

```java
// UserController.java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody RegisterRequest request) {
        UserDTO user = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or #id == authentication.principal.id")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
    
    @PutMapping("/{id}/profile")
    @PreAuthorize("#id == authentication.principal.id")
    public ResponseEntity<UserDTO> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(id, request));
    }
    
    @GetMapping("/organization/{orgId}")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Page<UserDTO>> getOrganizationUsers(
            @PathVariable Long orgId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(userService.getOrganizationUsers(orgId, pageable));
    }
}

// UserService.java
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrganizationRepository organizationRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UserMapper userMapper;
    
    public UserDTO registerUser(RegisterRequest request) {
        // Проверка на существование email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }
        
        // Создание пользователя
        User user = User.builder()
            .email(request.getEmail())
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(UserRole.STUDENT)
            .isActive(true)
            .createdAt(LocalDateTime.now())
            .build();
        
        // Привязка к организации, если указана
        if (request.getOrganizationId() != null) {
            Organization org = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
            user.setOrganization(org);
        }
        
        user = userRepository.save(user);
        
        // Публикуем событие регистрации
        eventPublisher.publishEvent(new UserRegisteredEvent(user.getId()));
        
        return userMapper.toDTO(user);
    }
    
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toDTO(user);
    }
}

// User Entity
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_user_org", columnList = "organization_id")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String firstName;
    
    @Column(nullable = false)
    private String lastName;
    
    @Column(nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;
    
    private String phoneNumber;
    private String avatarUrl;
    private Boolean isActive = true;
    private LocalDateTime lastLoginAt;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserProfile profile;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Enrollment> enrollments;
}
```

### 1.2 Course Service - Управление курсами

```java
// CourseController.java
@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {
    
    private final CourseService courseService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<CourseDTO> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        CourseDTO course = courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(course);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CourseDetailDTO> getCourse(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseDetail(id));
    }
    
    @GetMapping
    public ResponseEntity<Page<CourseDTO>> getCourses(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) CourseLevel level,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        
        CourseSearchCriteria criteria = CourseSearchCriteria.builder()
            .search(search)
            .categoryId(categoryId)
            .level(level)
            .build();
        
        return ResponseEntity.ok(courseService.searchCourses(criteria, pageable));
    }
    
    @PostMapping("/{courseId}/modules")
    @PreAuthorize("@courseSecurityService.isInstructor(#courseId, authentication.principal.id)")
    public ResponseEntity<ModuleDTO> addModule(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateModuleRequest request) {
        return ResponseEntity.ok(courseService.addModule(courseId, request));
    }
    
    @PostMapping("/{courseId}/modules/{moduleId}/lessons")
    @PreAuthorize("@courseSecurityService.isInstructor(#courseId, authentication.principal.id)")
    public ResponseEntity<LessonDTO> addLesson(
            @PathVariable Long courseId,
            @PathVariable Long moduleId,
            @Valid @RequestBody CreateLessonRequest request) {
        return ResponseEntity.ok(courseService.addLesson(moduleId, request));
    }
    
    @PutMapping("/{id}/publish")
    @PreAuthorize("@courseSecurityService.isInstructor(#id, authentication.principal.id)")
    public ResponseEntity<CourseDTO> publishCourse(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.publishCourse(id));
    }
}

// CourseService.java
@Service
@RequiredArgsConstructor
@Transactional
public class CourseService {
    
    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final CourseMapper courseMapper;
    private final SearchService searchService;
    
    public CourseDTO createCourse(CreateCourseRequest request) {
        Course course = Course.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .level(request.getLevel())
            .price(request.getPrice())
            .status(CourseStatus.DRAFT)
            .instructor(getCurrentUser())
            .organization(getCurrentUser().getOrganization())
            .createdAt(LocalDateTime.now())
            .build();
        
        course = courseRepository.save(course);
        
        // Индексируем курс для поиска
        searchService.indexCourse(course);
        
        return courseMapper.toDTO(course);
    }
    
    @Transactional(readOnly = true)
    public CourseDetailDTO getCourseDetail(Long id) {
        Course course = courseRepository.findByIdWithModulesAndLessons(id)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        
        return courseMapper.toDetailDTO(course);
    }
    
    @Transactional(readOnly = true)
    public Page<CourseDTO> searchCourses(CourseSearchCriteria criteria, Pageable pageable) {
        // Используем Elasticsearch для быстрого поиска
        if (StringUtils.hasText(criteria.getSearch())) {
            return searchService.searchCourses(criteria, pageable);
        }
        
        // Обычный поиск через БД
        return courseRepository.findByCriteria(criteria, pageable)
            .map(courseMapper::toDTO);
    }
    
    public ModuleDTO addModule(Long courseId, CreateModuleRequest request) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        
        // Определяем порядковый номер модуля
        int orderIndex = course.getModules().size();
        
        CourseModule module = CourseModule.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .course(course)
            .orderIndex(orderIndex)
            .build();
        
        module = moduleRepository.save(module);
        return courseMapper.toModuleDTO(module);
    }
    
    public CourseDTO publishCourse(Long id) {
        Course course = courseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        
        // Валидация перед публикацией
        validateCourseForPublish(course);
        
        course.setStatus(CourseStatus.PUBLISHED);
        course.setPublishedAt(LocalDateTime.now());
        course = courseRepository.save(course);
        
        // Уведомляем подписчиков об инструкторе
        notificationService.notifySubscribers(course);
        
        return courseMapper.toDTO(course);
    }
    
    private void validateCourseForPublish(Course course) {
        if (course.getModules().isEmpty()) {
            throw new ValidationException("Course must have at least one module");
        }
        
        boolean hasLessons = course.getModules().stream()
            .anyMatch(m -> !m.getLessons().isEmpty());
        
        if (!hasLessons) {
            throw new ValidationException("Course must have at least one lesson");
        }
    }
}
```

### 1.3 Enrollment Service - Запись на курсы

```java
// EnrollmentController.java
@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {
    
    private final EnrollmentService enrollmentService;
    
    @PostMapping
    public ResponseEntity<EnrollmentDTO> enrollInCourse(
            @Valid @RequestBody EnrollmentRequest request) {
        EnrollmentDTO enrollment = enrollmentService.enrollUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollment);
    }
    
    @GetMapping("/my-courses")
    public ResponseEntity<Page<EnrollmentDTO>> getMyEnrollments(
            @RequestParam(required = false) EnrollmentStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(
            enrollmentService.getUserEnrollments(getCurrentUserId(), status, pageable)
        );
    }
    
    @GetMapping("/{enrollmentId}/progress")
    public ResponseEntity<CourseProgressDTO> getCourseProgress(
            @PathVariable Long enrollmentId) {
        return ResponseEntity.ok(enrollmentService.getCourseProgress(enrollmentId));
    }
    
    @PostMapping("/{enrollmentId}/lessons/{lessonId}/complete")
    public ResponseEntity<LessonProgressDTO> completeLesson(
            @PathVariable Long enrollmentId,
            @PathVariable Long lessonId) {
        return ResponseEntity.ok(
            enrollmentService.markLessonCompleted(enrollmentId, lessonId)
        );
    }
}

// EnrollmentService.java
@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentService {
    
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final PaymentService paymentService;
    private final GamificationService gamificationService;
    
    public EnrollmentDTO enrollUser(EnrollmentRequest request) {
        User user = getCurrentUser();
        Course course = courseRepository.findById(request.getCourseId())
            .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        
        // Проверка на дублирование
        if (enrollmentRepository.existsByUserAndCourse(user, course)) {
            throw new DuplicateResourceException("Already enrolled in this course");
        }
        
        // Обработка оплаты, если курс платный
        if (course.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            Payment payment = paymentService.createPayment(user, course);
            // Если оплата не прошла, выбрасываем исключение
            if (!payment.isSuccessful()) {
                throw new PaymentFailedException("Payment failed");
            }
        }
        
        Enrollment enrollment = Enrollment.builder()
            .user(user)
            .course(course)
            .status(EnrollmentStatus.ACTIVE)
            .enrolledAt(LocalDateTime.now())
            .progress(BigDecimal.ZERO)
            .build();
        
        enrollment = enrollmentRepository.save(enrollment);
        
        // Награждаем баллами за запись на курс
        gamificationService.awardPoints(
            user, 
            PointsEvent.COURSE_ENROLLED, 
            10
        );
        
        return enrollmentMapper.toDTO(enrollment);
    }
    
    public LessonProgressDTO markLessonCompleted(Long enrollmentId, Long lessonId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));
        
        Lesson lesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));
        
        // Проверяем, что урок принадлежит этому курсу
        if (!lesson.getModule().getCourse().getId().equals(enrollment.getCourse().getId())) {
            throw new ValidationException("Lesson doesn't belong to this course");
        }
        
        // Создаем или обновляем прогресс
        LessonProgress progress = lessonProgressRepository
            .findByEnrollmentAndLesson(enrollment, lesson)
            .orElse(new LessonProgress());
        
        progress.setEnrollment(enrollment);
        progress.setLesson(lesson);
        progress.setStatus(ProgressStatus.COMPLETED);
        progress.setCompletedAt(LocalDateTime.now());
        
        progress = lessonProgressRepository.save(progress);
        
        // Обновляем общий прогресс по курсу
        updateCourseProgress(enrollment);
        
        // Геймификация
        gamificationService.awardPoints(
            enrollment.getUser(),
            PointsEvent.LESSON_COMPLETED,
            5
        );
        
        return lessonProgressMapper.toDTO(progress);
    }
    
    private void updateCourseProgress(Enrollment enrollment) {
        Course course = enrollment.getCourse();
        long totalLessons = course.getModules().stream()
            .mapToLong(m -> m.getLessons().size())
            .sum();
        
        long completedLessons = lessonProgressRepository
            .countCompletedByEnrollment(enrollment);
        
        BigDecimal progress = totalLessons > 0
            ? BigDecimal.valueOf(completedLessons)
                .divide(BigDecimal.valueOf(totalLessons), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
            : BigDecimal.ZERO;
        
        enrollment.setProgress(progress);
        
        // Если курс завершен
        if (progress.compareTo(BigDecimal.valueOf(100)) == 0) {
            enrollment.setStatus(EnrollmentStatus.COMPLETED);
            enrollment.setCompletedAt(LocalDateTime.now());
            
            // Выдаем сертификат
            certificateService.generateCertificate(enrollment);
            
            // Большая награда за завершение курса
            gamificationService.awardPoints(
                enrollment.getUser(),
                PointsEvent.COURSE_COMPLETED,
                100
            );
        }
        
        enrollmentRepository.save(enrollment);
    }
}
```

### 1.4 Gamification Service - Геймификация

```java
// GamificationController.java
@RestController
@RequestMapping("/api/v1/gamification")
@RequiredArgsConstructor
public class GamificationController {
    
    private final GamificationService gamificationService;
    
    @GetMapping("/my-stats")
    public ResponseEntity<UserGamificationStatsDTO> getMyStats() {
        return ResponseEntity.ok(
            gamificationService.getUserStats(getCurrentUserId())
        );
    }
    
    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardEntryDTO>> getLeaderboard(
            @RequestParam(defaultValue = "GLOBAL") LeaderboardType type,
            @RequestParam(defaultValue = "ALL_TIME") LeaderboardPeriod period,
            @PageableDefault(size = 100) Pageable pageable) {
        return ResponseEntity.ok(
            gamificationService.getLeaderboard(type, period, pageable)
        );
    }
    
    @GetMapping("/achievements")
    public ResponseEntity<List<AchievementDTO>> getAllAchievements() {
        return ResponseEntity.ok(gamificationService.getAllAchievements());
    }
    
    @GetMapping("/my-achievements")
    public ResponseEntity<List<UserAchievementDTO>> getMyAchievements() {
        return ResponseEntity.ok(
            gamificationService.getUserAchievements(getCurrentUserId())
        );
    }
}

// GamificationService.java - Расширенная версия
@Service
@RequiredArgsConstructor
@Transactional
public class GamificationService {
    
    private final UserPointsRepository pointsRepository;
    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final BadgeRepository badgeRepository;
    private final LeaderboardService leaderboardService;
    private final NotificationService notificationService;
    
    public UserGamificationStatsDTO getUserStats(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        UserPoints points = pointsRepository.findByUser(user)
            .orElse(new UserPoints(user, 0));
        
        List<UserAchievement> achievements = userAchievementRepository
            .findByUser(user);
        
        List<Badge> badges = badgeRepository.findByUser(user);
        
        UserLevel level = calculateLevel(points.getTotalPoints());
        
        return UserGamificationStatsDTO.builder()
            .totalPoints(points.getTotalPoints())
            .level(level.getLevel())
            .levelName(level.getName())
            .pointsToNextLevel(level.getPointsToNext())
            .achievementCount(achievements.size())
            .badgeCount(badges.size())
            .rank(leaderboardService.getUserRank(userId))
            .streak(calculateStreak(user))
            .build();
    }
    
    @Transactional
    public void awardPoints(User user, PointsEvent event, int amount) {
        UserPoints points = pointsRepository.findByUser(user)
            .orElse(UserPoints.builder()
                .user(user)
                .totalPoints(0)
                .monthlyPoints(0)
                .weeklyPoints(0)
                .build());
        
        points.addPoints(amount);
        pointsRepository.save(points);
        
        // Обновляем leaderboard в Redis
        leaderboardService.updateUserScore(user.getId(), points.getTotalPoints());
        
        // Проверяем достижения
        checkAndAwardAchievements(user, event);
        
        // Проверяем level up
        UserLevel newLevel = calculateLevel(points.getTotalPoints());
        if (newLevel.getLevel() > user.getLevel()) {
            levelUp(user, newLevel);
        }
        
        // Real-time уведомление
        notificationService.sendRealtimeNotification(
            user.getId(),
            NotificationType.POINTS_AWARDED,
            Map.of("points", amount, "event", event.name())
        );
    }
    
    private void checkAndAwardAchievements(User user, PointsEvent event) {
        List<Achievement> potentialAchievements = achievementRepository
            .findByTriggerEvent(event);
        
        for (Achievement achievement : potentialAchievements) {
            // Проверяем, не получил ли уже пользователь это достижение
            boolean alreadyAwarded = userAchievementRepository
                .existsByUserAndAchievement(user, achievement);
            
            if (!alreadyAwarded && achievement.isCriteriaMetFor(user)) {
                awardAchievement(user, achievement);
            }
        }
    }
    
    @Transactional
    public void awardAchievement(User user, Achievement achievement) {
        UserAchievement userAchievement = UserAchievement.builder()
            .user(user)
            .achievement(achievement)
            .awardedAt(LocalDateTime.now())
            .build();
        
        userAchievementRepository.save(userAchievement);
        
        // Награждаем бонусными баллами за достижение
        if (achievement.getBonusPoints() > 0) {
            awardPoints(user, PointsEvent.ACHIEVEMENT_EARNED, achievement.getBonusPoints());
        }
        
        // Если достижение дает бейдж, выдаем его
        if (achievement.getBadge() != null) {
            awardBadge(user, achievement.getBadge());
        }
        
        // Уведомление пользователя
        notificationService.sendAchievementNotification(user, achievement);
    }
    
    private UserLevel calculateLevel(int totalPoints) {
        // Прогрессивная система уровней
        int level = (int) Math.floor(Math.sqrt(totalPoints / 100.0));
        int currentLevelPoints = level * level * 100;
        int nextLevelPoints = (level + 1) * (level + 1) * 100;
        
        return UserLevel.builder()
            .level(level)
            .name(getLevelName(level))
            .currentPoints(totalPoints)
            .pointsToNext(nextLevelPoints - totalPoints)
            .build();
    }
    
    private String getLevelName(int level) {
        if (level < 5) return "Новичок";
        if (level < 10) return "Ученик";
        if (level < 20) return "Эксперт";
        if (level < 30) return "Мастер";
        return "Гуру";
    }
    
    private int calculateStreak(User user) {
        // Вычисляем streak - количество последовательных дней обучения
        List<LocalDate> activityDates = userActivityRepository
            .findRecentActivityDates(user.getId(), LocalDate.now().minusDays(365));
        
        int streak = 0;
        LocalDate checkDate = LocalDate.now();
        
        for (int i = 0; i < activityDates.size(); i++) {
            if (activityDates.contains(checkDate)) {
                streak++;
                checkDate = checkDate.minusDays(1);
            } else {
                break;
            }
        }
        
        return streak;
    }
}

// LeaderboardService.java - Используем Redis для быстрых leaderboards
@Service
@RequiredArgsConstructor
public class LeaderboardService {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    private static final String GLOBAL_LEADERBOARD_KEY = "leaderboard:global";
    private static final String WEEKLY_LEADERBOARD_KEY = "leaderboard:weekly";
    private static final String MONTHLY_LEADERBOARD_KEY = "leaderboard:monthly";
    
    public void updateUserScore(Long userId, int totalPoints) {
        String userKey = String.valueOf(userId);
        
        // Обновляем глобальный рейтинг
        redisTemplate.opsForZSet().add(
            GLOBAL_LEADERBOARD_KEY,
            userKey,
            totalPoints
        );
        
        // Обновляем недельный рейтинг
        redisTemplate.opsForZSet().add(
            WEEKLY_LEADERBOARD_KEY,
            userKey,
            totalPoints
        );
    }
    
    public List<LeaderboardEntryDTO> getLeaderboard(
            LeaderboardType type,
            LeaderboardPeriod period,
            Pageable pageable) {
        
        String key = getLeaderboardKey(type, period);
        
        Set<ZSetOperations.TypedTuple<String>> topUsers = 
            redisTemplate.opsForZSet()
                .reverseRangeWithScores(key, 0, pageable.getPageSize() - 1);
        
        List<LeaderboardEntryDTO> leaderboard = new ArrayList<>();
        int rank = 1;
        
        for (ZSetOperations.TypedTuple<String> tuple : topUsers) {
            Long userId = Long.parseLong(tuple.getValue());
            int points = tuple.getScore().intValue();
            
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                leaderboard.add(LeaderboardEntryDTO.builder()
                    .rank(rank++)
                    .userId(userId)
                    .userName(user.getFullName())
                    .avatarUrl(user.getAvatarUrl())
                    .points(points)
                    .level(calculateLevel(points))
                    .build());
            }
        }
        
        return leaderboard;
    }
    
    public Long getUserRank(Long userId) {
        String userKey = String.valueOf(userId);
        Long rank = redisTemplate.opsForZSet()
            .reverseRank(GLOBAL_LEADERBOARD_KEY, userKey);
        return rank != null ? rank + 1 : null;
    }
    
    @Scheduled(cron = "0 0 0 * * MON") // Каждый понедельник в полночь
    public void resetWeeklyLeaderboard() {
        redisTemplate.delete(WEEKLY_LEADERBOARD_KEY);
    }
    
    @Scheduled(cron = "0 0 0 1 * *") // 1-го числа каждого месяца
    public void resetMonthlyLeaderboard() {
        redisTemplate.delete(MONTHLY_LEADERBOARD_KEY);
    }
}
```

## 2. FRONTEND - ПРИМЕРЫ ANGULAR КОМПОНЕНТОВ

### 2.1 Course Catalog Component

```typescript
// course-catalog.component.ts
@Component({
  selector: 'app-course-catalog',
  templateUrl: './course-catalog.component.html',
  styleUrls: ['./course-catalog.component.scss']
})
export class CourseCatalogComponent implements OnInit, OnDestroy {
  courses$ = new BehaviorSubject<Course[]>([]);
  loading$ = new BehaviorSubject<boolean>(false);
  totalCourses = 0;
  
  searchForm: FormGroup;
  categories: Category[] = [];
  
  pageSize = 12;
  currentPage = 0;
  
  private destroy$ = new Subject<void>();
  
  constructor(
    private courseService: CourseService,
    private categoryService: CategoryService,
    private fb: FormBuilder,
    private router: Router
  ) {
    this.searchForm = this.fb.group({
      search: [''],
      categoryId: [null],
      level: [null],
      priceRange: [null]
    });
  }
  
  ngOnInit(): void {
    this.loadCategories();
    this.loadCourses();
    
    // Debounce поиска
    this.searchForm.get('search')?.valueChanges
      .pipe(
        debounceTime(500),
        distinctUntilChanged(),
        takeUntil(this.destroy$)
      )
      .subscribe(() => {
        this.currentPage = 0;
        this.loadCourses();
      });
    
    // Фильтры без debounce
    merge(
      this.searchForm.get('categoryId')?.valueChanges || EMPTY,
      this.searchForm.get('level')?.valueChanges || EMPTY,
      this.searchForm.get('priceRange')?.valueChanges || EMPTY
    ).pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.currentPage = 0;
        this.loadCourses();
      });
  }
  
  loadCourses(): void {
    this.loading$.next(true);
    
    const params = {
      ...this.searchForm.value,
      page: this.currentPage,
      size: this.pageSize
    };
    
    this.courseService.searchCourses(params)
      .pipe(
        finalize(() => this.loading$.next(false)),
        takeUntil(this.destroy$)
      )
      .subscribe({
        next: (response) => {
          this.courses$.next(response.content);
          this.totalCourses = response.totalElements;
        },
        error: (error) => {
          console.error('Error loading courses:', error);
          // Показываем toast с ошибкой
        }
      });
  }
  
  loadCategories(): void {
    this.categoryService.getAll()
      .pipe(takeUntil(this.destroy$))
      .subscribe(categories => this.categories = categories);
  }
  
  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadCourses();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
  
  onCourseClick(course: Course): void {
    this.router.navigate(['/courses', course.id]);
  }
  
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}

// course-catalog.component.html
<div class="course-catalog">
  <div class="filters-section">
    <form [formGroup]="searchForm" class="filters">
      <mat-form-field appearance="outline" class="search-field">
        <mat-label>Поиск курсов</mat-label>
        <input matInput formControlName="search" placeholder="Название, описание...">
        <mat-icon matPrefix>search</mat-icon>
      </mat-form-field>
      
      <mat-form-field appearance="outline">
        <mat-label>Категория</mat-label>
        <mat-select formControlName="categoryId">
          <mat-option [value]="null">Все категории</mat-option>
          <mat-option *ngFor="let category of categories" [value]="category.id">
            {{ category.name }}
          </mat-option>
        </mat-select>
      </mat-form-field>
      
      <mat-form-field appearance="outline">
        <mat-label>Уровень</mat-label>
        <mat-select formControlName="level">
          <mat-option [value]="null">Все уровни</mat-option>
          <mat-option value="BEGINNER">Начальный</mat-option>
          <mat-option value="INTERMEDIATE">Средний</mat-option>
          <mat-option value="ADVANCED">Продвинутый</mat-option>
        </mat-select>
      </mat-form-field>
    </form>
  </div>
  
  <div class="results-section">
    <div class="results-header">
      <h2>Найдено курсов: {{ totalCourses }}</h2>
    </div>
    
    <div *ngIf="loading$ | async" class="loading-spinner">
      <mat-spinner></mat-spinner>
    </div>
    
    <div *ngIf="!(loading$ | async)" class="course-grid">
      <app-course-card
        *ngFor="let course of courses$ | async"
        [course]="course"
        (click)="onCourseClick(course)">
      </app-course-card>
    </div>
    
    <mat-paginator
      [length]="totalCourses"
      [pageSize]="pageSize"
      [pageIndex]="currentPage"
      (page)="onPageChange($event.pageIndex)">
    </mat-paginator>
  </div>
</div>
```

### 2.2 Video Player Component

```typescript
// video-player.component.ts
@Component({
  selector: 'app-video-player',
  templateUrl: './video-player.component.html',
  styleUrls: ['./video-player.component.scss']
})
export class VideoPlayerComponent implements OnInit, OnDestroy, AfterViewInit {
  @Input() lessonId!: number;
  @Input() enrollmentId!: number;
  @Output() completed = new EventEmitter<void>();
  
  @ViewChild('videoElement', { static: false }) videoElement!: ElementRef<HTMLVideoElement>;
  
  player?: VideoJsPlayer;
  videoUrl = '';
  isCompleted = false;
  watchTime = 0;
  totalDuration = 0;
  
  private watchTimeInterval?: any;
  private lastPosition = 0;
  
  constructor(
    private lessonService: LessonService,
    private progressService: ProgressService
  ) {}
  
  ngOnInit(): void {
    this.loadVideo();
  }
  
  ngAfterViewInit(): void {
    this.initializePlayer();
  }
  
  loadVideo(): void {
    this.lessonService.getVideoUrl(this.lessonId)
      .subscribe(url => {
        this.videoUrl = url;
        if (this.player) {
          this.player.src({
            src: url,
            type: 'application/x-mpegURL' // HLS
          });
        }
      });
  }
  
  initializePlayer(): void {
    const options: VideoJsPlayerOptions = {
      controls: true,
      autoplay: false,
      preload: 'auto',
      fluid: true,
      controlBar: {
        volumePanel: { inline: false },
        pictureInPictureToggle: false
      },
      html5: {
        hls: {
          enableLowInitialPlaylist: true,
          smoothQualityChange: true,
          overrideNative: true
        }
      }
    };
    
    this.player = videojs(this.videoElement.nativeElement, options);
    
    // Загружаем последнюю позицию просмотра
    this.loadLastPosition();
    
    // Слушаем события плеера
    this.setupPlayerEvents();
    
    // Защита от скачивания
    this.preventDownload();
  }
  
  setupPlayerEvents(): void {
    if (!this.player) return;
    
    // Обновляем позицию каждые 10 секунд
    this.player.on('timeupdate', () => {
      const currentTime = this.player?.currentTime() || 0;
      this.watchTime = currentTime;
      
      if (Math.abs(currentTime - this.lastPosition) >= 10) {
        this.saveProgress(currentTime);
        this.lastPosition = currentTime;
      }
    });
    
    // Когда загрузились метаданные
    this.player.on('loadedmetadata', () => {
      this.totalDuration = this.player?.duration() || 0;
    });
    
    // Когда видео закончилось
    this.player.on('ended', () => {
      this.markAsCompleted();
    });
    
    // Отслеживаем, если пользователь досмотрел 90%
    this.player.on('timeupdate', () => {
      const progress = (this.watchTime / this.totalDuration) * 100;
      if (progress >= 90 && !this.isCompleted) {
        this.markAsCompleted();
      }
    });
  }
  
  preventDownload(): void {
    if (!this.player) return;
    
    // Отключаем контекстное меню
    this.player.el().addEventListener('contextmenu', (e) => {
      e.preventDefault();
    });
    
    // Отключаем горячие клавиши для скачивания
    this.player.el().addEventListener('keydown', (e) => {
      if ((e.ctrlKey || e.metaKey) && (e.key === 's' || e.key === 'S')) {
        e.preventDefault();
      }
    });
  }
  
  loadLastPosition(): void {
    this.progressService.getLessonProgress(this.enrollmentId, this.lessonId)
      .subscribe(progress => {
        if (progress?.lastPosition && this.player) {
          this.player.currentTime(progress.lastPosition);
        }
      });
  }
  
  saveProgress(position: number): void {
    this.progressService.updateLessonProgress(
      this.enrollmentId,
      this.lessonId,
      { lastPosition: position }
    ).subscribe();
  }
  
  markAsCompleted(): void {
    if (this.isCompleted) return;
    
    this.isCompleted = true;
    this.progressService.markLessonCompleted(
      this.enrollmentId,
      this.lessonId
    ).subscribe(() => {
      this.completed.emit();
      // Показываем уведомление о завершении
      this.showCompletionNotification();
    });
  }
  
  showCompletionNotification(): void {
    // Можно показать snackbar или модальное окно
    // с поздравлением и заработанными баллами
  }
  
  ngOnDestroy(): void {
    // Сохраняем последнюю позицию перед уходом
    if (this.player) {
      this.saveProgress(this.player.currentTime());
      this.player.dispose();
    }
    
    if (this.watchTimeInterval) {
      clearInterval(this.watchTimeInterval);
    }
  }
}
```

### 2.3 Gamification Dashboard Component

```typescript
// gamification-dashboard.component.ts
@Component({
  selector: 'app-gamification-dashboard',
  templateUrl: './gamification-dashboard.component.html',
  styleUrls: ['./gamification-dashboard.component.scss']
})
export class GamificationDashboardComponent implements OnInit {
  userStats$!: Observable<UserGamificationStats>;
  leaderboard$!: Observable<LeaderboardEntry[]>;
  achievements$!: Observable<UserAchievement[]>;
  
  selectedLeaderboardType: LeaderboardType = 'GLOBAL';
  selectedLeaderboardPeriod: LeaderboardPeriod = 'ALL_TIME';
  
  constructor(
    private gamificationService: GamificationService
  ) {}
  
  ngOnInit(): void {
    this.loadData();
  }
  
  loadData(): void {
    this.userStats$ = this.gamificationService.getUserStats();
    this.loadLeaderboard();
    this.achievements$ = this.gamificationService.getUserAchievements();
  }
  
  loadLeaderboard(): void {
    this.leaderboard$ = this.gamificationService.getLeaderboard(
      this.selectedLeaderboardType,
      this.selectedLeaderboardPeriod
    );
  }
  
  onLeaderboardFilterChange(): void {
    this.loadLeaderboard();
  }
}

// gamification-dashboard.component.html
<div class="gamification-dashboard">
  <!-- User Stats Card -->
  <mat-card *ngIf="userStats$ | async as stats" class="stats-card">
    <mat-card-header>
      <mat-card-title>Ваша статистика</mat-card-title>
    </mat-card-header>
    
    <mat-card-content>
      <div class="stat-item">
        <div class="stat-icon">
          <mat-icon>stars</mat-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalPoints }}</div>
          <div class="stat-label">Всего баллов</div>
        </div>
      </div>
      
      <div class="level-progress">
        <div class="level-info">
          <span class="current-level">Уровень {{ stats.level }}</span>
          <span class="level-name">{{ stats.levelName }}</span>
        </div>
        <mat-progress-bar 
          mode="determinate" 
          [value]="getLevelProgress(stats)">
        </mat-progress-bar>
        <div class="points-to-next">
          {{ stats.pointsToNextLevel }} баллов до следующего уровня
        </div>
      </div>
      
      <div class="stats-grid">
        <div class="stat-item">
          <mat-icon>emoji_events</mat-icon>
          <div>{{ stats.achievementCount }} достижений</div>
        </div>
        <div class="stat-item">
          <mat-icon>military_tech</mat-icon>
          <div>{{ stats.badgeCount }} бейджей</div>
        </div>
        <div class="stat-item">
          <mat-icon>trending_up</mat-icon>
          <div>{{ stats.rank }} место в рейтинге</div>
        </div>
        <div class="stat-item">
          <mat-icon>local_fire_department</mat-icon>
          <div>{{ stats.streak }} дней подряд</div>
        </div>
      </div>
    </mat-card-content>
  </mat-card>
  
  <!-- Leaderboard -->
  <mat-card class="leaderboard-card">
    <mat-card-header>
      <mat-card-title>Таблица лидеров</mat-card-title>
      <div class="leaderboard-filters">
        <mat-button-toggle-group 
          [(value)]="selectedLeaderboardPeriod"
          (change)="onLeaderboardFilterChange()">
          <mat-button-toggle value="ALL_TIME">Всё время</mat-button-toggle>
          <mat-button-toggle value="MONTHLY">Месяц</mat-button-toggle>
          <mat-button-toggle value="WEEKLY">Неделя</mat-button-toggle>
        </mat-button-toggle-group>
      </div>
    </mat-card-header>
    
    <mat-card-content>
      <div class="leaderboard-list" *ngIf="leaderboard$ | async as leaderboard">
        <div 
          *ngFor="let entry of leaderboard; let i = index" 
          class="leaderboard-entry"
          [class.top-3]="entry.rank <= 3">
          
          <div class="rank">
            <span *ngIf="entry.rank <= 3" class="medal">
              <mat-icon>{{ getMedalIcon(entry.rank) }}</mat-icon>
            </span>
            <span *ngIf="entry.rank > 3">{{ entry.rank }}</span>
          </div>
          
          <img 
            [src]="entry.avatarUrl || 'assets/default-avatar.png'" 
            class="avatar"
            [alt]="entry.userName">
          
          <div class="user-info">
            <div class="user-name">{{ entry.userName }}</div>
            <div class="user-level">Уровень {{ entry.level }}</div>
          </div>
          
          <div class="points">
            {{ entry.points }} <mat-icon>stars</mat-icon>
          </div>
        </div>
      </div>
    </mat-card-content>
  </mat-card>
  
  <!-- Achievements -->
  <mat-card class="achievements-card">
    <mat-card-header>
      <mat-card-title>Достижения</mat-card-title>
    </mat-card-header>
    
    <mat-card-content>
      <div class="achievements-grid" *ngIf="achievements$ | async as achievements">
        <div 
          *ngFor="let achievement of achievements" 
          class="achievement-item"
          [matTooltip]="achievement.description"
          [class.earned]="achievement.isEarned">
          
          <img 
            [src]="achievement.iconUrl" 
            [alt]="achievement.name"
            class="achievement-icon">
          
          <div class="achievement-name">{{ achievement.name }}</div>
          
          <div *ngIf="achievement.isEarned" class="earned-date">
            {{ achievement.earnedAt | date:'dd.MM.yyyy' }}
          </div>
          
          <div *ngIf="!achievement.isEarned" class="achievement-progress">
            <mat-progress-bar 
              mode="determinate" 
              [value]="achievement.progress">
            </mat-progress-bar>
            <span>{{ achievement.progress }}%</span>
          </div>
        </div>
      </div>
    </mat-card-content>
  </mat-card>
</div>
```

---

## ПРОДОЛЖЕНИЕ СЛЕДУЕТ...

Этот файл будет дополнен после получения результатов исследования:
- Примеры кода для video streaming
- Примеры кода для payment integration
- Примеры кода для analytics
- Примеры кода для notification system
- И другие компоненты
