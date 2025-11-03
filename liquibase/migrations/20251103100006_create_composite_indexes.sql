--liquibase formatted sql

--changeset benew:20251103100006_create_composite_indexes
--comment: Create composite indexes for complex queries with multiple filter conditions

/*
 * Migration: Create Composite Indexes
 * Purpose: Create multi-column indexes for queries that filter/sort by multiple columns
 * Author: Database Architect
 * Date: 2025-11-03
 * 
 * This migration creates composite (multi-column) indexes for:
 * - Manager dashboard: filter by manager + status simultaneously
 * - Template task ordering: filter by template + sort by order
 * - Onboarding task ordering: filter by process + sort by order
 * 
 * Composite Index Benefits:
 * - Single index can satisfy queries with multiple WHERE/ORDER BY columns
 * - Column order matters: most selective column first, then sorting column
 * - Can replace single-column indexes if composite index has same leading column
 * 
 * Index Design Rationale:
 * 1. (manager_id, status): Dashboard query filters by manager AND status
 * 2. (template_id, task_order): Loading template tasks in correct order
 * 3. (onboarding_process_id, task_order): Loading checklist tasks in correct order
 * 
 * Query Examples Using These Indexes:
 * - SELECT * FROM onboarding_process WHERE manager_id = ? AND status = 'ACTIVE'
 * - SELECT * FROM template_task WHERE template_id = ? ORDER BY task_order
 * - SELECT * FROM onboarding_task WHERE onboarding_process_id = ? ORDER BY task_order
 * 
 * Performance Impact:
 * - Manager dashboard: Single index lookup instead of bitmap scan
 * - Checklist loading: Index-only scan with built-in ordering
 * - Reduces need for explicit sorting (ORDER BY optimization)
 * 
 * Tables Affected: 
 * - benew.onboarding_process
 * - benew.template_task
 * - benew.onboarding_task
 * 
 * Dependencies: All table creation migrations (20251103100001-20251103100003)
 * 
 * Special Notes:
 * - These composite indexes can partially replace single-column FK indexes
 *   if queries always filter by both columns
 * - However, we keep single-column FK indexes for referential integrity checks
 *   and queries that filter by only one column
 */

-- ============================================================================
-- MANAGER DASHBOARD COMPOSITE INDEX
-- ============================================================================

-- composite index on (manager_id, status) for manager dashboard
-- optimizes the most critical query: "show me my ACTIVE employee processes"
-- single index can satisfy both WHERE manager_id = ? AND status = 'ACTIVE'
-- eliminates need for bitmap index scan combining two separate indexes
create index idx_onboarding_process_manager_status 
    on benew.onboarding_process(manager_id, status);

comment on index benew.idx_onboarding_process_manager_status is 
    'CRITICAL composite index for manager dashboard. ' ||
    'Optimizes: WHERE manager_id = ? AND status = ''ACTIVE''. ' ||
    'Replaces need for bitmap scan combining separate indexes. ' ||
    'Query: "Show all active onboarding processes for my employees".';

-- ============================================================================
-- TEMPLATE TASK ORDERING INDEX
-- ============================================================================

-- composite index on (template_id, task_order) for loading template tasks in order
-- optimizes: "load all tasks for template X in correct display order"
-- index already contains sorted task_order, no explicit sorting needed
-- critical for template detail view and CSV export
create index idx_template_task_template_order 
    on benew.template_task(template_id, task_order);

comment on index benew.idx_template_task_template_order is 
    'Optimizes loading template tasks in display order. ' ||
    'Query: WHERE template_id = ? ORDER BY task_order. ' ||
    'Index provides pre-sorted results, eliminates explicit sort step. ' ||
    'Used in: template detail view, template edit, CSV export.';

-- ============================================================================
-- ONBOARDING TASK ORDERING INDEX
-- ============================================================================

-- composite index on (onboarding_process_id, task_order) for loading checklist in order
-- optimizes: "load all tasks for process X in correct display order"
-- CRITICAL for checklist display (most frequent query in user/manager views)
-- index already contains sorted task_order, no explicit sorting needed
create index idx_onboarding_task_process_order 
    on benew.onboarding_task(onboarding_process_id, task_order);

comment on index benew.idx_onboarding_task_process_order is 
    'CRITICAL composite index for checklist display. ' ||
    'Optimizes: WHERE onboarding_process_id = ? ORDER BY task_order. ' ||
    'Index provides pre-sorted results, eliminates explicit sort step. ' ||
    'Used in: employee checklist view, manager checklist review.';

--rollback drop index if exists benew.idx_onboarding_task_process_order;
--rollback drop index if exists benew.idx_template_task_template_order;
--rollback drop index if exists benew.idx_onboarding_process_manager_status;

