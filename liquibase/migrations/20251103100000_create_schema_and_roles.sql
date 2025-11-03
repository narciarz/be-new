--liquibase formatted sql

--changeset benew:20251103100000_create_schema_and_roles
--comment: Initial database setup - create roles and schema for Be New application

/*
 * Migration: Create Schema and Roles
 * Purpose: Set up PostgreSQL roles and dedicated schema for the Be New application
 * Author: Database Architect
 * Date: 2025-11-03
 * 
 * This migration creates:
 * 1. app_owner role - for schema management and DDL operations (migrations)
 * 2. app_spring role - for application DML operations (SELECT, INSERT, UPDATE, DELETE)
 * 3. benew schema - dedicated namespace for all application tables
 * 
 * Security Strategy:
 * - Separation of concerns: DDL operations use app_owner, DML operations use app_spring
 * - No Row-Level Security (RLS) - data filtering is handled in Spring application logic
 * - app_spring has no DDL privileges for enhanced security
 * 
 * Tables Affected: None (initial setup)
 * Dependencies: None
 */

-- create app_owner role for schema management and migrations
-- this role will own the schema and execute all DDL operations
-- password should be changed in production via secure configuration management
create role app_owner with login password 'change_me_in_production_owner';

comment on role app_owner is 
    'Owner role for benew schema. Used for migrations and DDL operations. Should only be used by Liquibase/Flyway.';

-- create app_spring role for application runtime operations
-- this role will be used by Spring Boot application for DML operations only
-- password should be changed in production via secure configuration management
create role app_spring with login password 'change_me_in_production_app';

comment on role app_spring is 
    'Application role for Spring Boot. Has DML privileges (SELECT, INSERT, UPDATE, DELETE) but no DDL access.';

-- create dedicated schema for Be New application
-- all application tables will be created within this schema
create schema if not exists benew authorization app_owner;

comment on schema benew is 
    'Dedicated schema for Be New onboarding application. Contains all application tables, indexes, and constraints.';

-- grant usage on schema to app_spring role
-- this allows the application to access objects within the schema
grant usage on schema benew to app_spring;

-- grant DML privileges on all current tables to app_spring
-- this is needed if any tables already exist in the schema
grant select, insert, update, delete on all tables in schema benew to app_spring;

-- set default privileges for future tables
-- ensures that any new tables created by app_owner will automatically
-- grant DML privileges to app_spring
alter default privileges in schema benew 
    grant select, insert, update, delete on tables to app_spring;

comment on schema benew is 
    'Be New application schema. Owner: app_owner. Application runtime role: app_spring.';

--rollback drop schema if exists benew cascade;
--rollback drop role if exists app_spring;
--rollback drop role if exists app_owner;

