package freelms

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

/**
 * Smartup LMS Load Testing Suite
 *
 * Target metrics:
 * - Clients: 200
 * - Total users: 100,000
 * - Concurrent users: 1,000
 * - Response time p95: < 500ms
 * - Error rate: < 1%
 */
class FreeLmsLoadTest extends Simulation {

  // Configuration
  val baseUrl = System.getProperty("baseUrl", "http://localhost:8080")
  val users = Integer.getInteger("users", 1000)
  val rampUpDuration = Integer.getInteger("rampUp", 300) // 5 minutes
  val testDuration = Integer.getInteger("duration", 1800) // 30 minutes

  val httpProtocol = http
    .baseUrl(baseUrl)
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Gatling/FREE-LMS-LoadTest")

  // Test data feeders
  val userFeeder = csv("users.csv").random
  val courseFeeder = csv("courses.csv").random

  // ============================================
  // AUTHENTICATION SCENARIOS
  // ============================================

  val login = exec(
    http("Login")
      .post("/api/v1/auth/login")
      .body(StringBody(
        """{"email": "${email}", "password": "${password}"}"""
      ))
      .check(status.is(200))
      .check(jsonPath("$.data.accessToken").saveAs("token"))
  )

  val refreshToken = exec(
    http("Refresh Token")
      .post("/api/v1/auth/refresh")
      .header("Authorization", "Bearer ${token}")
      .check(status.is(200))
  )

  // ============================================
  // COURSE BROWSING SCENARIOS
  // ============================================

  val browseCourses = exec(
    http("Get Course List")
      .get("/api/v1/courses")
      .queryParam("page", "0")
      .queryParam("size", "20")
      .header("Authorization", "Bearer ${token}")
      .check(status.is(200))
      .check(jsonPath("$.data.content[0].id").optional.saveAs("courseId"))
  )

  val getCourseDetails = exec(
    http("Get Course Details")
      .get("/api/v1/courses/${courseId}")
      .header("Authorization", "Bearer ${token}")
      .check(status.is(200))
  )

  val searchCourses = exec(
    http("Search Courses")
      .get("/api/v1/courses/search")
      .queryParam("q", "programming")
      .queryParam("page", "0")
      .queryParam("size", "10")
      .header("Authorization", "Bearer ${token}")
      .check(status.is(200))
  )

  // ============================================
  // ENROLLMENT SCENARIOS
  // ============================================

  val getEnrollments = exec(
    http("Get My Enrollments")
      .get("/api/v1/enrollments/my")
      .header("Authorization", "Bearer ${token}")
      .check(status.is(200))
  )

  val getProgress = exec(
    http("Get Course Progress")
      .get("/api/v1/enrollments/${courseId}/progress")
      .header("Authorization", "Bearer ${token}")
      .check(status.in(200, 404))
  )

  val updateProgress = exec(
    http("Update Lesson Progress")
      .post("/api/v1/enrollments/${courseId}/lessons/1/complete")
      .header("Authorization", "Bearer ${token}")
      .check(status.in(200, 404))
  )

  // ============================================
  // GAMIFICATION SCENARIOS
  // ============================================

  val getLeaderboard = exec(
    http("Get Weekly Leaderboard")
      .get("/api/v1/gamification/leaderboard")
      .queryParam("period", "WEEKLY")
      .header("Authorization", "Bearer ${token}")
      .check(status.is(200))
  )

  val getMyAchievements = exec(
    http("Get My Achievements")
      .get("/api/v1/gamification/achievements/my")
      .header("Authorization", "Bearer ${token}")
      .check(status.is(200))
  )

  val getMyPoints = exec(
    http("Get My Points")
      .get("/api/v1/gamification/points/my")
      .header("Authorization", "Bearer ${token}")
      .check(status.is(200))
  )

  // ============================================
  // LEARNING PATH SCENARIOS
  // ============================================

  val getLearningPaths = exec(
    http("Get Learning Paths")
      .get("/api/v1/learning-paths")
      .header("Authorization", "Bearer ${token}")
      .check(status.is(200))
  )

  val getMyLearningPath = exec(
    http("Get My Learning Path Progress")
      .get("/api/v1/learning-paths/my")
      .header("Authorization", "Bearer ${token}")
      .check(status.is(200))
  )

  // ============================================
  // SKILLS SCENARIOS
  // ============================================

  val getSkillMatrix = exec(
    http("Get Skill Matrix")
      .get("/api/v1/skills/matrix")
      .header("Authorization", "Bearer ${token}")
      .check(status.is(200))
  )

  val getMySkills = exec(
    http("Get My Skills")
      .get("/api/v1/skills/my")
      .header("Authorization", "Bearer ${token}")
      .check(status.is(200))
  )

  // ============================================
  // NOTIFICATION SCENARIOS
  // ============================================

  val getNotifications = exec(
    http("Get Notifications")
      .get("/api/v1/notifications")
      .queryParam("page", "0")
      .queryParam("size", "10")
      .header("Authorization", "Bearer ${token}")
      .check(status.is(200))
  )

  val getUnreadCount = exec(
    http("Get Unread Count")
      .get("/api/v1/notifications/unread/count")
      .header("Authorization", "Bearer ${token}")
      .check(status.is(200))
  )

  // ============================================
  // SOCIAL LEARNING SCENARIOS
  // ============================================

  val getQuestions = exec(
    http("Get Q&A Questions")
      .get("/api/v1/social/questions")
      .queryParam("page", "0")
      .queryParam("size", "20")
      .header("Authorization", "Bearer ${token}")
      .check(status.is(200))
  )

  val getStudyGroups = exec(
    http("Get Study Groups")
      .get("/api/v1/social/groups")
      .header("Authorization", "Bearer ${token}")
      .check(status.is(200))
  )

  // ============================================
  // USER JOURNEY SCENARIOS
  // ============================================

  // Typical learner journey: login -> browse -> learn -> check progress
  val learnerJourney = scenario("Learner Journey")
    .feed(userFeeder)
    .exec(login)
    .pause(1, 3)
    .exec(browseCourses)
    .pause(2, 5)
    .exec(getCourseDetails)
    .pause(5, 15) // Reading course content
    .exec(getEnrollments)
    .pause(1, 2)
    .exec(getProgress)
    .pause(10, 30) // Learning
    .exec(updateProgress)
    .pause(1, 2)
    .exec(getNotifications)
    .exec(getLeaderboard)

  // Power user: checks everything
  val powerUserJourney = scenario("Power User Journey")
    .feed(userFeeder)
    .exec(login)
    .pause(1)
    .exec(browseCourses)
    .exec(searchCourses)
    .exec(getEnrollments)
    .exec(getLearningPaths)
    .exec(getMyLearningPath)
    .exec(getSkillMatrix)
    .exec(getMySkills)
    .exec(getLeaderboard)
    .exec(getMyAchievements)
    .exec(getMyPoints)
    .exec(getNotifications)
    .exec(getQuestions)
    .exec(getStudyGroups)

  // Light user: just checks notifications and progress
  val lightUserJourney = scenario("Light User Journey")
    .feed(userFeeder)
    .exec(login)
    .pause(2, 5)
    .exec(getUnreadCount)
    .pause(1, 2)
    .exec(getNotifications)
    .pause(2, 5)
    .exec(getEnrollments)
    .pause(5, 10)
    .exec(getProgress)

  // API stress test
  val apiStressTest = scenario("API Stress Test")
    .feed(userFeeder)
    .exec(login)
    .repeat(10) {
      exec(browseCourses)
        .pause(100.milliseconds, 500.milliseconds)
    }

  // ============================================
  // LOAD TEST CONFIGURATIONS
  // ============================================

  // Standard load test: Gradual ramp-up to 1000 users
  val standardLoadTest = Seq(
    learnerJourney.inject(
      rampUsers(users * 70 / 100).during(rampUpDuration.seconds)  // 70% learners
    ),
    powerUserJourney.inject(
      rampUsers(users * 20 / 100).during(rampUpDuration.seconds)  // 20% power users
    ),
    lightUserJourney.inject(
      rampUsers(users * 10 / 100).during(rampUpDuration.seconds)  // 10% light users
    )
  )

  // Spike test: Sudden load increase
  val spikeTest = Seq(
    learnerJourney.inject(
      nothingFor(60.seconds),
      atOnceUsers(500),
      nothingFor(120.seconds),
      atOnceUsers(500)
    )
  )

  // Endurance test: Constant load for extended period
  val enduranceTest = Seq(
    learnerJourney.inject(
      constantUsersPerSec(10).during(testDuration.seconds)
    )
  )

  // Stress test: Find breaking point
  val stressTest = Seq(
    apiStressTest.inject(
      incrementUsersPerSec(50)
        .times(20)
        .eachLevelLasting(60.seconds)
        .separatedByRampsLasting(10.seconds)
        .startingFrom(50)
    )
  )

  // ============================================
  // ASSERTIONS
  // ============================================

  val assertions = Seq(
    global.responseTime.percentile(95).lt(500),     // p95 < 500ms
    global.responseTime.percentile(99).lt(2000),    // p99 < 2s
    global.successfulRequests.percent.gt(99),       // > 99% success
    global.requestsPerSec.gt(100)                   // > 100 RPS
  )

  // ============================================
  // RUN CONFIGURATION
  // ============================================

  setUp(standardLoadTest: _*)
    .protocols(httpProtocol)
    .assertions(assertions: _*)
}

/**
 * Smoke test - Quick validation
 */
class SmokeTest extends Simulation {

  val baseUrl = System.getProperty("baseUrl", "http://localhost:8080")

  val httpProtocol = http
    .baseUrl(baseUrl)
    .acceptHeader("application/json")

  val healthCheck = scenario("Health Check")
    .exec(
      http("Gateway Health")
        .get("/actuator/health")
        .check(status.is(200))
    )

  setUp(
    healthCheck.inject(atOnceUsers(1))
  ).protocols(httpProtocol)
}
