package com.freelms.lms;

import com.freelms.lms.auth.entity.RefreshToken;
import com.freelms.lms.auth.entity.User;
import com.freelms.lms.common.enums.CourseLevel;
import com.freelms.lms.common.enums.CourseStatus;
import com.freelms.lms.common.enums.EnrollmentStatus;
import com.freelms.lms.common.enums.UserRole;
import com.freelms.lms.course.entity.Course;
import com.freelms.lms.course.entity.Category;
import com.freelms.lms.course.entity.CourseModule;
import com.freelms.lms.enrollment.entity.Enrollment;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;

/**
 * Utility class for creating test data objects
 */
public class TestUtils {

    private static final String TEST_SECRET = "test-secret-key-for-testing-purposes-minimum-32-chars";
    private static final long JWT_EXPIRATION = 3600000L; // 1 hour
    
    /**
     * Creates a test user with default values
     */
    public static User createTestUser() {
        return User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.STUDENT)
                .isActive(true)
                .isEmailVerified(false)
                .level(1)
                .totalPoints(0)
                .failedLoginAttempts(0)
                .build();
    }
    
    /**
     * Creates a test user with specified ID
     */
    public static User createTestUser(Long id) {
        User user = createTestUser();
        user.setId(id);
        return user;
    }
    
    /**
     * Creates a test user with specified role
     */
    public static User createTestUser(UserRole role) {
        User user = createTestUser();
        user.setRole(role);
        return user;
    }
    
    /**
     * Creates a test user with specified email
     */
    public static User createTestUserWithEmail(String email) {
        User user = createTestUser();
        user.setEmail(email);
        return user;
    }
    
    /**
     * Creates a test instructor user
     */
    public static User createTestInstructor() {
        User user = createTestUser();
        user.setId(2L);
        user.setRole(UserRole.INSTRUCTOR);
        user.setEmail("instructor@example.com");
        user.setFirstName("Test");
        user.setLastName("Instructor");
        return user;
    }
    
    /**
     * Creates a test admin user
     */
    public static User createTestAdmin() {
        User user = createTestUser();
        user.setId(3L);
        user.setRole(UserRole.ADMIN);
        user.setEmail("admin@example.com");
        user.setFirstName("Test");
        user.setLastName("Admin");
        return user;
    }
    
    /**
     * Creates a test course with default values
     */
    public static Course createTestCourse() {
        return Course.builder()
                .id(1L)
                .title("Test Course")
                .slug("test-course")
                .description("Test course description")
                .status(CourseStatus.DRAFT)
                .level(CourseLevel.BEGINNER)
                .instructorId(1L)
                .price(BigDecimal.ZERO)
                .isFree(true)
                .studentCount(0)
                .ratingCount(0)
                .rating(BigDecimal.ZERO)
                .durationMinutes(0)
                .language("en")
                .modules(new ArrayList<>())
                .build();
    }
    
    /**
     * Creates a test course with specified ID
     */
    public static Course createTestCourse(Long id) {
        Course course = createTestCourse();
        course.setId(id);
        return course;
    }
    
    /**
     * Creates a test course with specified instructor ID
     */
    public static Course createTestCourse(Long id, Long instructorId) {
        Course course = createTestCourse(id);
        course.setInstructorId(instructorId);
        return course;
    }
    
    /**
     * Creates a published test course
     */
    public static Course createPublishedTestCourse() {
        Course course = createTestCourse();
        course.setStatus(CourseStatus.PUBLISHED);
        course.setPublishedAt(LocalDateTime.now());
        // Add a module so it can be considered valid for publishing
        CourseModule module = CourseModule.builder()
                .id(1L)
                .title("Test Module")
                .course(course)
                .sortOrder(1)
                .build();
        course.getModules().add(module);
        return course;
    }
    
    /**
     * Creates a test category
     */
    public static Category createTestCategory() {
        return Category.builder()
                .id(1L)
                .name("Programming")
                .slug("programming")
                .build();
    }
    
    /**
     * Creates a test enrollment with default values
     */
    public static Enrollment createTestEnrollment() {
        return Enrollment.builder()
                .id(1L)
                .userId(1L)
                .courseId(1L)
                .status(EnrollmentStatus.ACTIVE)
                .progress(0)
                .enrolledAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * Creates a test enrollment with specified IDs
     */
    public static Enrollment createTestEnrollment(Long id, Long userId, Long courseId) {
        Enrollment enrollment = createTestEnrollment();
        enrollment.setId(id);
        enrollment.setUserId(userId);
        enrollment.setCourseId(courseId);
        return enrollment;
    }
    
    /**
     * Creates a test enrollment with specified status
     */
    public static Enrollment createTestEnrollment(EnrollmentStatus status) {
        Enrollment enrollment = createTestEnrollment();
        enrollment.setStatus(status);
        return enrollment;
    }
    
    /**
     * Creates a completed test enrollment
     */
    public static Enrollment createCompletedTestEnrollment() {
        Enrollment enrollment = createTestEnrollment();
        enrollment.setStatus(EnrollmentStatus.COMPLETED);
        enrollment.setProgress(100);
        enrollment.setCompletedAt(LocalDateTime.now());
        return enrollment;
    }
    
    /**
     * Creates a test refresh token
     */
    public static RefreshToken createTestRefreshToken(User user) {
        return RefreshToken.builder()
                .id(1L)
                .token("test-refresh-token")
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .deviceInfo("Test Device")
                .ipAddress("127.0.0.1")
                .build();
    }
    
    /**
     * Creates an expired refresh token
     */
    public static RefreshToken createExpiredRefreshToken(User user) {
        return RefreshToken.builder()
                .id(2L)
                .token("expired-refresh-token")
                .user(user)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .revoked(false)
                .deviceInfo("Test Device")
                .ipAddress("127.0.0.1")
                .build();
    }
    
    /**
     * Creates a revoked refresh token
     */
    public static RefreshToken createRevokedRefreshToken(User user) {
        return RefreshToken.builder()
                .id(3L)
                .token("revoked-refresh-token")
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(true)
                .deviceInfo("Test Device")
                .ipAddress("127.0.0.1")
                .build();
    }
    
    /**
     * Generates a JWT token for testing purposes
     */
    public static String generateJwtToken(User user) {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_EXPIRATION);
        
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }
    
    /**
     * Generates an expired JWT token for testing
     */
    public static String generateExpiredJwtToken(User user) {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expiredDate = new Date(now.getTime() - 3600000L); // 1 hour ago
        
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(expiredDate)
                .expiration(expiredDate)
                .signWith(key)
                .compact();
    }
}
