-- =====================================================
-- Smartup LMS - Performance Optimization Indexes
-- Version: 2.0
-- Target: 100,000 users, 1000 concurrent users
-- =====================================================

-- =====================================================
-- CRITICAL PRIORITY INDEXES (Immediate)
-- =====================================================

-- Auth Service
CREATE INDEX IF NOT EXISTS idx_users_organization_active
    ON users(organization_id, is_active);
CREATE INDEX IF NOT EXISTS idx_users_role_active
    ON users(role, is_active);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expires
    ON refresh_tokens(expires_at);

-- Course Service
CREATE INDEX IF NOT EXISTS idx_courses_status_published
    ON courses(status, published_at DESC);
CREATE INDEX IF NOT EXISTS idx_courses_instructor_status
    ON courses(instructor_id, status);
CREATE INDEX IF NOT EXISTS idx_courses_category_status
    ON courses(category_id, status);
CREATE INDEX IF NOT EXISTS idx_lessons_quiz
    ON lessons(quiz_id);
CREATE INDEX IF NOT EXISTS idx_quizzes_lesson
    ON quizzes(lesson_id);

-- Enrollment Service
CREATE INDEX IF NOT EXISTS idx_enrollments_user_status
    ON enrollments(user_id, status);
CREATE INDEX IF NOT EXISTS idx_enrollments_course_status
    ON enrollments(course_id, status);
CREATE INDEX IF NOT EXISTS idx_lesson_progress_lesson
    ON lesson_progress(lesson_id);
CREATE INDEX IF NOT EXISTS idx_lesson_progress_status
    ON lesson_progress(enrollment_id, status);

-- Payment Service
CREATE INDEX IF NOT EXISTS idx_payments_subscription
    ON payments(subscription_id);
CREATE INDEX IF NOT EXISTS idx_payments_course
    ON payments(course_id);
CREATE INDEX IF NOT EXISTS idx_payments_created
    ON payments(created_at DESC);

-- =====================================================
-- FEEDBACK SERVICE INDEXES
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_feedback_responses_request_question
    ON feedback_responses(request_id, question_id);
CREATE INDEX IF NOT EXISTS idx_feedback_requests_cycle
    ON feedback_requests(cycle_id);
CREATE INDEX IF NOT EXISTS idx_feedback_requests_requester
    ON feedback_requests(requester_id);
CREATE INDEX IF NOT EXISTS idx_feedback_cycles_organization
    ON feedback_cycles(organization_id);
CREATE INDEX IF NOT EXISTS idx_feedback_cycles_dates
    ON feedback_cycles(start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_feedback_questions_template
    ON feedback_questions(template_id);
CREATE INDEX IF NOT EXISTS idx_feedback_templates_organization
    ON feedback_templates(organization_id);

-- =====================================================
-- SKILLS SERVICE INDEXES
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_skills_category
    ON skills(category_id);
CREATE INDEX IF NOT EXISTS idx_skills_organization
    ON skills(organization_id);
CREATE INDEX IF NOT EXISTS idx_user_skills_user
    ON user_skills(user_id);
CREATE INDEX IF NOT EXISTS idx_user_skills_skill
    ON user_skills(skill_id);
CREATE INDEX IF NOT EXISTS idx_skill_endorsements_user_skill
    ON skill_endorsements(user_skill_id);
CREATE INDEX IF NOT EXISTS idx_skill_endorsements_endorser
    ON skill_endorsements(endorser_id);
CREATE INDEX IF NOT EXISTS idx_skill_assessments_user_skill
    ON skill_assessments(user_skill_id);
CREATE INDEX IF NOT EXISTS idx_skill_gaps_user
    ON skill_gaps(user_id);
CREATE INDEX IF NOT EXISTS idx_skill_gaps_skill
    ON skill_gaps(skill_id);
CREATE INDEX IF NOT EXISTS idx_skill_level_definitions_skill
    ON skill_level_definitions(skill_id);
CREATE INDEX IF NOT EXISTS idx_skill_categories_parent
    ON skill_categories(parent_id);
CREATE INDEX IF NOT EXISTS idx_team_skill_matrix_team
    ON team_skill_matrix(team_id);

-- =====================================================
-- LEARNING PATH SERVICE INDEXES
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_learning_paths_organization
    ON learning_paths(organization_id);
CREATE INDEX IF NOT EXISTS idx_learning_paths_status
    ON learning_paths(status);
CREATE INDEX IF NOT EXISTS idx_learning_paths_created_by
    ON learning_paths(created_by);
CREATE INDEX IF NOT EXISTS idx_learning_path_items_path
    ON learning_path_items(learning_path_id);
CREATE INDEX IF NOT EXISTS idx_learning_path_items_order
    ON learning_path_items(learning_path_id, sort_order);
CREATE INDEX IF NOT EXISTS idx_learning_path_enrollments_user
    ON learning_path_enrollments(user_id);
CREATE INDEX IF NOT EXISTS idx_learning_path_enrollments_path
    ON learning_path_enrollments(learning_path_id);
CREATE INDEX IF NOT EXISTS idx_learning_path_item_progress_user
    ON learning_path_item_progress(user_id);
CREATE INDEX IF NOT EXISTS idx_career_tracks_organization
    ON career_tracks(organization_id);
CREATE INDEX IF NOT EXISTS idx_career_levels_track
    ON career_levels(career_track_id);
CREATE INDEX IF NOT EXISTS idx_user_career_progress_user
    ON user_career_progress(user_id);
CREATE INDEX IF NOT EXISTS idx_course_prerequisites_course
    ON course_prerequisites(course_id);
CREATE INDEX IF NOT EXISTS idx_required_skills_path
    ON required_skills(learning_path_id);

-- =====================================================
-- GAMIFICATION SERVICE INDEXES
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_challenges_organization
    ON challenges(organization_id);
CREATE INDEX IF NOT EXISTS idx_challenges_status
    ON challenges(status);
CREATE INDEX IF NOT EXISTS idx_challenges_dates
    ON challenges(start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_challenge_participants_challenge
    ON challenge_participants(challenge_id);
CREATE INDEX IF NOT EXISTS idx_challenge_participants_user
    ON challenge_participants(user_id);
CREATE INDEX IF NOT EXISTS idx_challenge_teams_challenge
    ON challenge_teams(challenge_id);
CREATE INDEX IF NOT EXISTS idx_leaderboards_organization
    ON leaderboards(organization_id);
CREATE INDEX IF NOT EXISTS idx_leaderboards_period
    ON leaderboards(period_type, period_start);
CREATE INDEX IF NOT EXISTS idx_leaderboard_entries_leaderboard
    ON leaderboard_entries(leaderboard_id);
CREATE INDEX IF NOT EXISTS idx_leaderboard_entries_user
    ON leaderboard_entries(user_id);
CREATE INDEX IF NOT EXISTS idx_leaderboard_entries_rank
    ON leaderboard_entries(leaderboard_id, rank);
CREATE INDEX IF NOT EXISTS idx_user_points_user
    ON user_points(user_id);
CREATE INDEX IF NOT EXISTS idx_user_streaks_user
    ON user_streaks(user_id);
CREATE INDEX IF NOT EXISTS idx_points_transactions_user
    ON points_transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_points_transactions_created
    ON points_transactions(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_achievements_organization
    ON achievements(organization_id);
CREATE INDEX IF NOT EXISTS idx_user_achievements_user
    ON user_achievements(user_id);
CREATE INDEX IF NOT EXISTS idx_rewards_organization
    ON rewards(organization_id);
CREATE INDEX IF NOT EXISTS idx_reward_claims_user
    ON reward_claims(user_id);
CREATE INDEX IF NOT EXISTS idx_reward_claims_reward
    ON reward_claims(reward_id);

-- =====================================================
-- MENTORING SERVICE INDEXES
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_mentor_profiles_user
    ON mentor_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_mentor_profiles_organization
    ON mentor_profiles(organization_id);
CREATE INDEX IF NOT EXISTS idx_mentor_profiles_active
    ON mentor_profiles(is_active);
CREATE INDEX IF NOT EXISTS idx_mentoring_relationships_mentor
    ON mentoring_relationships(mentor_id);
CREATE INDEX IF NOT EXISTS idx_mentoring_relationships_mentee
    ON mentoring_relationships(mentee_id);
CREATE INDEX IF NOT EXISTS idx_mentoring_relationships_status
    ON mentoring_relationships(status);
CREATE INDEX IF NOT EXISTS idx_mentoring_sessions_relationship
    ON mentoring_sessions(relationship_id);
CREATE INDEX IF NOT EXISTS idx_mentoring_sessions_scheduled
    ON mentoring_sessions(scheduled_at);

-- =====================================================
-- SOCIAL LEARNING SERVICE INDEXES
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_study_groups_organization
    ON study_groups(organization_id);
CREATE INDEX IF NOT EXISTS idx_study_groups_course
    ON study_groups(course_id);
CREATE INDEX IF NOT EXISTS idx_study_groups_creator
    ON study_groups(created_by);
CREATE INDEX IF NOT EXISTS idx_study_group_members_group
    ON study_group_members(group_id);
CREATE INDEX IF NOT EXISTS idx_study_group_members_user
    ON study_group_members(user_id);
CREATE INDEX IF NOT EXISTS idx_questions_author
    ON questions(author_id);
CREATE INDEX IF NOT EXISTS idx_questions_course
    ON questions(course_id);
CREATE INDEX IF NOT EXISTS idx_questions_organization
    ON questions(organization_id);
CREATE INDEX IF NOT EXISTS idx_questions_created
    ON questions(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_answers_question
    ON answers(question_id);
CREATE INDEX IF NOT EXISTS idx_answers_author
    ON answers(author_id);
CREATE INDEX IF NOT EXISTS idx_peer_content_creator
    ON peer_content(creator_id);
CREATE INDEX IF NOT EXISTS idx_peer_content_course
    ON peer_content(course_id);

-- =====================================================
-- COMPLIANCE SERVICE INDEXES
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_compliance_trainings_organization
    ON compliance_trainings(organization_id);
CREATE INDEX IF NOT EXISTS idx_compliance_trainings_required
    ON compliance_trainings(is_mandatory);
CREATE INDEX IF NOT EXISTS idx_user_compliance_user
    ON user_compliance(user_id);
CREATE INDEX IF NOT EXISTS idx_user_compliance_training
    ON user_compliance(training_id);
CREATE INDEX IF NOT EXISTS idx_user_compliance_due
    ON user_compliance(due_date);
CREATE INDEX IF NOT EXISTS idx_user_compliance_status
    ON user_compliance(status);
CREATE INDEX IF NOT EXISTS idx_certifications_user
    ON certifications(user_id);
CREATE INDEX IF NOT EXISTS idx_certifications_expiry
    ON certifications(expiry_date);
CREATE INDEX IF NOT EXISTS idx_audit_logs_user
    ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_organization
    ON audit_logs(organization_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_timestamp
    ON audit_logs(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_audit_logs_action
    ON audit_logs(action);
CREATE INDEX IF NOT EXISTS idx_e_signatures_document
    ON e_signatures(document_id);
CREATE INDEX IF NOT EXISTS idx_e_signatures_signer
    ON e_signatures(signer_id);

-- =====================================================
-- IDP SERVICE INDEXES
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_idp_user
    ON individual_development_plans(user_id);
CREATE INDEX IF NOT EXISTS idx_idp_organization
    ON individual_development_plans(organization_id);
CREATE INDEX IF NOT EXISTS idx_idp_status
    ON individual_development_plans(status);
CREATE INDEX IF NOT EXISTS idx_idp_manager
    ON individual_development_plans(manager_id);
CREATE INDEX IF NOT EXISTS idx_development_goals_plan
    ON development_goals(plan_id);
CREATE INDEX IF NOT EXISTS idx_development_goals_status
    ON development_goals(status);
CREATE INDEX IF NOT EXISTS idx_goal_actions_goal
    ON goal_actions(goal_id);
CREATE INDEX IF NOT EXISTS idx_plan_reviews_plan
    ON plan_reviews(plan_id);

-- =====================================================
-- REPORTING SERVICE INDEXES
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_reports_organization
    ON reports(organization_id);
CREATE INDEX IF NOT EXISTS idx_reports_created_by
    ON reports(created_by);
CREATE INDEX IF NOT EXISTS idx_reports_type
    ON reports(report_type);
CREATE INDEX IF NOT EXISTS idx_report_executions_report
    ON report_executions(report_id);
CREATE INDEX IF NOT EXISTS idx_report_executions_status
    ON report_executions(status);
CREATE INDEX IF NOT EXISTS idx_dashboards_organization
    ON dashboards(organization_id);
CREATE INDEX IF NOT EXISTS idx_dashboards_owner
    ON dashboards(owner_id);
CREATE INDEX IF NOT EXISTS idx_dashboard_widgets_dashboard
    ON dashboard_widgets(dashboard_id);

-- =====================================================
-- INTEGRATION SERVICE INDEXES
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_integrations_organization
    ON integrations(organization_id);
CREATE INDEX IF NOT EXISTS idx_integrations_type
    ON integrations(integration_type);
CREATE INDEX IF NOT EXISTS idx_integrations_active
    ON integrations(is_active);
CREATE INDEX IF NOT EXISTS idx_sync_logs_integration
    ON sync_logs(integration_id);
CREATE INDEX IF NOT EXISTS idx_sync_logs_timestamp
    ON sync_logs(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_calendar_events_integration
    ON calendar_events(integration_id);
CREATE INDEX IF NOT EXISTS idx_calendar_events_user
    ON calendar_events(user_id);
CREATE INDEX IF NOT EXISTS idx_calendar_events_start
    ON calendar_events(start_time);

-- =====================================================
-- NOTIFICATION SERVICE INDEXES
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_notifications_user_read
    ON notifications(user_id, is_read);
CREATE INDEX IF NOT EXISTS idx_notifications_created
    ON notifications(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_push_subscriptions_user
    ON push_subscriptions(user_id);

-- =====================================================
-- ANALYTICS SERVICE INDEXES
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_user_interactions_user_type
    ON user_interactions(user_id, type);
CREATE INDEX IF NOT EXISTS idx_user_interactions_course
    ON user_interactions(course_id);
CREATE INDEX IF NOT EXISTS idx_user_interactions_created
    ON user_interactions(created_at DESC);

-- =====================================================
-- COMPOSITE INDEXES FOR COMMON QUERIES
-- =====================================================

-- User's active enrollments with progress
CREATE INDEX IF NOT EXISTS idx_enrollments_user_status_progress
    ON enrollments(user_id, status, progress_percent);

-- Organization's courses by status and rating
CREATE INDEX IF NOT EXISTS idx_courses_org_status_rating
    ON courses(organization_id, status, rating DESC);

-- User's compliance status by due date
CREATE INDEX IF NOT EXISTS idx_user_compliance_user_status_due
    ON user_compliance(user_id, status, due_date);

-- Analytics: User activity by date range
CREATE INDEX IF NOT EXISTS idx_user_interactions_user_date
    ON user_interactions(user_id, created_at DESC);

-- =====================================================
-- PARTIAL INDEXES (PostgreSQL specific)
-- =====================================================

-- Active users only
CREATE INDEX IF NOT EXISTS idx_users_active_only
    ON users(email) WHERE is_active = true;

-- Published courses only
CREATE INDEX IF NOT EXISTS idx_courses_published_only
    ON courses(rating DESC, student_count DESC) WHERE status = 'PUBLISHED';

-- Pending compliance items
CREATE INDEX IF NOT EXISTS idx_user_compliance_pending
    ON user_compliance(user_id, due_date) WHERE status = 'PENDING';

-- Active challenges
CREATE INDEX IF NOT EXISTS idx_challenges_active
    ON challenges(organization_id, end_date) WHERE status = 'ACTIVE';

-- =====================================================
-- VACUUM AND ANALYZE (Run after index creation)
-- =====================================================

-- ANALYZE users;
-- ANALYZE courses;
-- ANALYZE enrollments;
-- ANALYZE lesson_progress;
-- ANALYZE feedback_responses;
-- ANALYZE user_skills;
-- ANALYZE learning_path_enrollments;
-- ANALYZE challenges;
-- ANALYZE leaderboard_entries;
-- ANALYZE audit_logs;
