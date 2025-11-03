--liquibase formatted sql

--changeset benew:20251103100005_create_search_indexes
--comment: Create indexes for search and filtering operations

/*
 * Migration: Create Search Indexes
 * Purpose: Create indexes to optimize search, filtering, and uniqueness checks
 * Author: Database Architect
 * Date: 2025-11-03
 * 
 * This migration creates indexes for:
 * - Case-insensitive email lookups and uniqueness
 * - Name-based user search (first_name, last_name)
 * - Position-based user and template search
 * - Process status filtering (ACTIVE vs ARCHIVED)
 * 
 * Key Design Decisions:
 * - Functional indexes on LOWER() for case-insensitive uniqueness/search
 * - Application must normalize (trim + toLowerCase) before search/insert
 * - Partial indexes exclude NULLs where appropriate to save space
 * - These indexes complement the basic UNIQUE constraints with case-insensitivity
 * 
 * Performance Impact:
 * - Email lookup: O(n) → O(log n) with case-insensitive search
 * - User search: O(n) → O(log n) for name-based filtering
 * - Template lookup: O(n) → O(log n) for position-based search
 * - Dashboard filtering: O(n) → O(log n) for status filtering
 * 
 * Tables Affected: 
 * - benew.app_user (email, first_name, last_name, position_name)
 * - benew.template (position_name)
 * - benew.onboarding_process (status)
 * 
 * Dependencies: All table creation migrations (20251103100001-20251103100003)
 * 
 * Special Notes:
 * - LOWER() functional indexes enable case-insensitive searches
 * - Application MUST use LOWER() in WHERE clauses to leverage these indexes
 * - UNIQUE indexes enforce case-insensitive uniqueness
 * - Partial indexes (WHERE ... IS NOT NULL) save space for nullable columns
 */

-- ============================================================================
-- APP_USER SEARCH INDEXES
-- ============================================================================

-- case-insensitive unique index on email
-- prevents duplicate emails with different cases (e.g., User@example.com vs user@example.com)
-- application must use LOWER(email) in WHERE clause to leverage this index
-- this is in addition to uk_app_user_email constraint for case-insensitive uniqueness
create unique index idx_app_user_email_lower 
    on benew.app_user(lower(email));

comment on index benew.idx_app_user_email_lower is 
    'Enforces case-insensitive email uniqueness. Prevents: user@example.com and User@example.com from both existing. Application must use LOWER(email) in WHERE clauses.';

-- index on last_name for user search and sorting
-- optimizes queries like: "find users with last name starting with X"
-- used in admin panel user search and manager assignment dropdowns
create index idx_app_user_last_name 
    on benew.app_user(last_name);

comment on index benew.idx_app_user_last_name is 
    'Optimizes user search by last name. Used in: admin user management, manager assignment dropdown, employee search.';

-- index on first_name for user search and sorting
-- optimizes queries like: "find users with first name starting with X"
-- used in admin panel user search and reporting
create index idx_app_user_first_name 
    on benew.app_user(first_name);

comment on index benew.idx_app_user_first_name is 
    'Optimizes user search by first name. Used in: admin user management, reporting, employee search.';

-- partial index on position_name for filtering users by position
-- partial index excludes NULLs (admins without positions)
-- optimizes queries like: "find all users with position X"
create index idx_app_user_position_name 
    on benew.app_user(position_name) 
    where position_name is not null;

comment on index benew.idx_app_user_position_name is 
    'Optimizes filtering users by position/job title. Partial index excludes NULLs (admins). Used in: reporting, position-based user lists.';

-- ============================================================================
-- TEMPLATE SEARCH INDEXES
-- ============================================================================

-- case-insensitive unique index on template position_name
-- prevents duplicate templates with different cases (e.g., "Engineer" vs "engineer")
-- application must normalize (trim + toLowerCase) before insert/search
-- this is in addition to uk_template_position_name constraint for case-insensitive uniqueness
create unique index idx_template_position_name_lower 
    on benew.template(lower(position_name));

comment on index benew.idx_template_position_name_lower is 
    'Enforces case-insensitive position_name uniqueness for templates. Prevents: "Software Engineer" and "software engineer" from both existing. Application must normalize (trim + toLowerCase) before insert/search.';

-- ============================================================================
-- ONBOARDING_PROCESS FILTERING INDEXES
-- ============================================================================

-- index on status for filtering active vs archived processes
-- optimizes queries like: "find all ACTIVE processes" (manager dashboard)
-- critical for separating active onboarding from archive view
create index idx_onboarding_process_status 
    on benew.onboarding_process(status);

comment on index benew.idx_onboarding_process_status is 
    'Optimizes filtering processes by status (ACTIVE vs ARCHIVED). Critical for: manager dashboard (show only active), archive view.';

--rollback drop index if exists benew.idx_onboarding_process_status;
--rollback drop index if exists benew.idx_template_position_name_lower;
--rollback drop index if exists benew.idx_app_user_position_name;
--rollback drop index if exists benew.idx_app_user_first_name;
--rollback drop index if exists benew.idx_app_user_last_name;
--rollback drop index if exists benew.idx_app_user_email_lower;

