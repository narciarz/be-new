--liquibase formatted sql

--changeset benew:20251103100004_create_foreign_key_indexes
--comment: Create B-tree indexes on all foreign key columns for JOIN performance

/*
 * Migration: Create Foreign Key Indexes
 * Purpose: Create B-tree indexes on all foreign key columns to optimize JOIN operations
 * Author: Database Architect
 * Date: 2025-11-03
 * 
 * This migration creates indexes on all foreign key columns across the schema.
 * 
 * Why These Indexes are Critical:
 * - PostgreSQL does NOT automatically create indexes on foreign key columns
 * - Without these indexes, JOINs on foreign keys require sequential scans
 * - UUID foreign keys especially benefit from explicit indexes
 * - These indexes dramatically improve query performance for:
 *   - Dashboard queries (manager viewing their employees' processes)
 *   - Process detail queries (loading tasks for a process)
 *   - Template detail queries (loading tasks for a template)
 *   - Referential integrity checks (ON DELETE RESTRICT validation)
 * 
 * Performance Impact:
 * - Dashboard query: O(n) → O(log n) for manager's processes lookup
 * - Task loading: O(n) → O(log n) for process/template tasks
 * - User deletion validation: O(n) → O(log n) for checking assigned employees
 * 
 * Tables Affected: 
 * - benew.app_user (manager_id)
 * - benew.template_task (template_id)
 * - benew.onboarding_process (user_id, manager_id, source_template_id)
 * - benew.onboarding_task (onboarding_process_id)
 * 
 * Dependencies: All table creation migrations (20251103100001-20251103100003)
 * 
 * Special Notes:
 * - manager_id index uses partial index (WHERE manager_id IS NOT NULL) to save space
 * - All indexes use default B-tree structure (optimal for equality and range queries)
 * - Index names follow convention: idx_{table}_{column}
 */

-- index on app_user.manager_id for manager-employee hierarchy queries
-- partial index excludes rows where manager_id is null (admins and top-level managers)
-- optimizes queries like: "find all employees of manager X"
-- also improves ON DELETE RESTRICT validation when deleting managers
create index idx_app_user_manager_id 
    on benew.app_user(manager_id) 
    where manager_id is not null;

comment on index benew.idx_app_user_manager_id is 
    'Optimizes queries for manager-employee hierarchy. Partial index excludes NULLs (admins/top-level managers). Improves: finding employees of manager, manager deletion validation.';

-- index on template_task.template_id for loading template tasks
-- optimizes queries like: "find all tasks for template X"
-- critical for template detail view and CSV import validation
create index idx_template_task_template_id 
    on benew.template_task(template_id);

comment on index benew.idx_template_task_template_id is 
    'Optimizes loading all tasks for a template. Critical for: template detail view, CSV import, template deletion validation.';

-- index on onboarding_process.user_id for user's processes lookup
-- optimizes queries like: "find all onboarding processes for user X"
-- used when displaying employee's own onboarding progress
create index idx_onboarding_process_user_id 
    on benew.onboarding_process(user_id);

comment on index benew.idx_onboarding_process_user_id is 
    'Optimizes finding all onboarding processes for a user. Used for: employee viewing own progress, user deletion validation.';

-- index on onboarding_process.manager_id for manager dashboard
-- optimizes queries like: "find all processes managed by manager X"
-- CRITICAL for manager dashboard performance (most frequent query)
create index idx_onboarding_process_manager_id 
    on benew.onboarding_process(manager_id);

comment on index benew.idx_onboarding_process_manager_id is 
    'CRITICAL for manager dashboard performance. Optimizes: finding all employees onboarding under manager X, manager deletion validation.';

-- index on onboarding_process.source_template_id for template usage tracking
-- optimizes queries like: "find all processes created from template X"
-- used for template usage statistics and template deletion validation
create index idx_onboarding_process_template_id 
    on benew.onboarding_process(source_template_id);

comment on index benew.idx_onboarding_process_template_id is 
    'Optimizes finding all processes created from a template. Used for: template usage statistics, template deletion validation.';

-- index on onboarding_task.onboarding_process_id for loading checklist
-- optimizes queries like: "find all tasks for process X"
-- CRITICAL for checklist display (employee and manager views)
create index idx_onboarding_task_process_id 
    on benew.onboarding_task(onboarding_process_id);

comment on index benew.idx_onboarding_task_process_id is 
    'CRITICAL for checklist display performance. Optimizes: loading all tasks for onboarding process, process deletion validation.';

--rollback drop index if exists benew.idx_onboarding_task_process_id;
--rollback drop index if exists benew.idx_onboarding_process_template_id;
--rollback drop index if exists benew.idx_onboarding_process_manager_id;
--rollback drop index if exists benew.idx_onboarding_process_user_id;
--rollback drop index if exists benew.idx_template_task_template_id;
--rollback drop index if exists benew.idx_app_user_manager_id;

