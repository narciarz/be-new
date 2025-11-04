# REST API Plan

## 1. Resources

- **Users**: Maps to the `app_user` table. Holds user details (email, password_hash, first_name, last_name, position_name) and a recursive relationship for managers.
- **Templates**: Maps to the `template` table. Contains checklist template metadata identified primarily by a unique `position_name`.
- **Template Tasks**: Maps to the `template_task` table. Stores tasks for each template including title, description, task_order, and owner_role; linked to their parent template.
- **Onboarding Processes**: Maps to the `onboarding_process` table. Represents active onboarding instances with attributes like status and denormalized counters (total_tasks_count, completed_tasks_count) and foreign keys relating to user, manager, and source template.
- **Onboarding Tasks**: Maps to the `onboarding_task` table. Maintains task copies for onboarding processes, tracking task details and completion status.
- **Authentication**: Not a database table but essential for managing user login/session operations.

---

## 2. Endpoints

### 2.1 Users

- **GET /users**
  - **Description**: Retrieve a paginated, sortable, and filterable list of users.
  - **Query Parameters**: 
    - `page`: Page number.
    - `size`: Number of items per page.
    - `sort`: Field(s) to sort by.
    - `filter`: Filters (e.g., role, manager).
  - **Response**: JSON array of user objects.
  - **Success Codes**: 200 OK.
  - **Errors**: 400 Bad Request (invalid parameters).

- **GET /users/{userId}**
  - **Description**: Retrieve detailed information about a specific user.
  - **Response**: JSON object representing the user.
  - **Success Codes**: 200 OK.
  - **Errors**: 404 Not Found if the user does not exist.

- **POST /users**
  - **Description**: Create a new user. Assign roles and, optionally, a manager.
  - **Request Payload**:
    ```json
    {
      "email": "string",
      "password": "string",
      "firstName": "string",
      "lastName": "string",
      "positionName": "string",
      "managerId": "UUID (optional)",
      "role": "ADMIN | MANAGER | USER"
    }
    ```
  - **Response**: JSON object representing the newly created user.
  - **Success Codes**: 201 Created.
  - **Errors**: 400 Bad Request (validation failures, e.g., duplicate email).

- **PUT /users/{userId}**
  - **Description**: Update an existing user’s details.
  - **Request Payload**: Similar structure to the POST payload (password omitted unless changed).
  - **Response**: Updated user JSON object.
  - **Success Codes**: 200 OK.
  - **Errors**: 400 or 404 for invalid data or non-existent user.

- **DELETE /users/{userId}**
  - **Description**: Remove or archive a user. Must respect foreign key constraints (e.g., a manager with active subordinates).
  - **Response**: No content.
  - **Success Codes**: 204 No Content.
  - **Errors**: 400/404 if deletion violates business logic.

### 2.2 Templates

- **GET /templates**
  - **Description**: List all onboarding checklist templates. Supports pagination, filtering, and sorting by properties (like positionName).
  - **Response**: JSON array of template objects.
  - **Success Codes**: 200 OK.
  - **Errors**: 400 Bad Request.

- **GET /templates/{templateId}**
  - **Description**: Retrieve details of a specific template.
  - **Response**: JSON object with template details.
  - **Success Codes**: 200 OK.
  - **Errors**: 404 Not Found.

- **POST /templates**
  - **Description**: Create a new checklist template.
  - **Request Payload**:
    ```json
    {
      "positionName": "string",
      "description": "string (optional)"
    }
    ```
  - **Response**: JSON object of the created template.
  - **Success Codes**: 201 Created.
  - **Errors**: 400 Bad Request (e.g., duplicate positionName).

- **PUT /templates/{templateId}**
  - **Description**: Update fields of an existing template.
  - **Response**: Updated template object in JSON.
  - **Success Codes**: 200 OK.
  - **Errors**: 400/404 as applicable.

- **DELETE /templates/{templateId}**
  - **Description**: Remove a checklist template. Must consider foreign key dependencies with template tasks.
  - **Response**: No content.
  - **Success Codes**: 204 No Content.
  - **Errors**: 400/404 if deletion is disallowed.

### 2.3 Template Tasks

*(Managed as a nested resource under Templates)*

- **GET /templates/{templateId}/tasks**
  - **Description**: Retrieve all tasks for a specified template.
  - **Response**: JSON array of task objects.
  - **Success Codes**: 200 OK.
  - **Errors**: 404 Not Found if template does not exist.

- **POST /templates/{templateId}/tasks**
  - **Description**: Create a new task for the specified template.
  - **Request Payload**:
    ```json
    {
      "title": "string",
      "description": "string",
      "taskOrder": "integer",
      "ownerRole": "string"
    }
    ```
  - **Response**: Created task object.
  - **Success Codes**: 201 Created.
  - **Errors**: 400 Bad Request (invalid input).

- **PUT /templates/{templateId}/tasks/{taskId}**
  - **Description**: Update an existing template task.
  - **Response**: Updated task object.
  - **Success Codes**: 200 OK.
  - **Errors**: 400/404 if update fails.

- **DELETE /templates/{templateId}/tasks/{taskId}**
  - **Description**: Delete a task from the template.
  - **Response**: No content.
  - **Success Codes**: 204 No Content.
  - **Errors**: 400/404 in case of dependency violations.

### 2.4 Onboarding Processes

- **GET /onboarding**
  - **Description**: Get a list of onboarding processes. Supports filtering (by status, manager, user) and pagination.
  - **Response**: JSON array of onboarding process objects.
  - **Success Codes**: 200 OK.
  - **Errors**: 400 Bad Request.

- **GET /onboarding/{processId}**
  - **Description**: Retrieve details for a single onboarding process.
  - **Response**: JSON object with process details.
  - **Success Codes**: 200 OK.
  - **Errors**: 404 Not Found.

- **POST /onboarding**
  - **Description**: Create a new onboarding process. Typically triggered on new user creation, copying template tasks.
  - **Request Payload**:
    ```json
    {
      "userId": "UUID",
      "managerId": "UUID",
      "sourceTemplateId": "UUID"
    }
    ```
  - **Response**: Created onboarding process object.
  - **Success Codes**: 201 Created.
  - **Errors**: 400 Bad Request.

- **PUT /onboarding/{processId}**
  - **Description**: Update process status (e.g., marking as ARCHIVED) or update denormalized counters.
  - **Request Payload** (example):
    ```json
    {
      "status": "ARCHIVED",
      "totalTasksCount": "integer",
      "completedTasksCount": "integer"
    }
    ```
  - **Response**: Updated process object.
  - **Success Codes**: 200 OK.
  - **Errors**: 400/404 as necessary.

- **DELETE /onboarding/{processId}**
  - **Description**: Soft delete or archive the onboarding process to preserve history.
  - **Response**: No content.
  - **Success Codes**: 204 No Content.
  - **Errors**: 400/404 if the action is disallowed.

### 2.5 Onboarding Tasks

*(Nested under Onboarding Processes)*

- **GET /onboarding/{processId}/tasks**
  - **Description**: List all tasks for a given onboarding process.
  - **Response**: JSON array of tasks.
  - **Success Codes**: 200 OK.
  - **Errors**: 404 if the process is not found.

- **GET /onboarding/{processId}/tasks/{taskId}**
  - **Description**: Retrieve detailed information about a specific task.
  - **Response**: JSON object of the task.
  - **Success Codes**: 200 OK.
  - **Errors**: 404 if not found.

- **PUT /onboarding/{processId}/tasks/{taskId}**
  - **Description**: Update a task (for example, marking it as completed).
  - **Request Payload**:
    ```json
    { "isCompleted": true }
    ```
  - **Response**: The updated task object.
  - **Success Codes**: 200 OK.
  - **Errors**: 400/404 if update fails.

### 2.6 Authentication

- **POST /auth/login**
  - **Description**: Authenticate and obtain a JWT for further API calls.
  - **Request Payload**:
    ```json
    {
      "email": "string",
      "password": "string"
    }
    ```
  - **Response**: JSON containing the JWT token and user role details.
  - **Success Codes**: 200 OK.
  - **Errors**: 401 Unauthorized on invalid credentials.

- **POST /auth/logout**
  - **Description**: Invalidate the user session.
  - **Response**: Confirmation of logout.
  - **Success Codes**: 200 OK.

- **POST /auth/password-reset** *(optional)*
  - **Description**: For Admin-initiated password reset actions.
  - **Response**: Status message regarding the reset operation.
  - **Success Codes**: 200 OK.
  - **Errors**: 400 for invalid requests.

### 2.7 CSV Import for Templates

- **POST /templates/import**
  - **Description**: Import a CSV file containing tasks to create a new checklist template. Restricted to Admin users.
  - **Request**: Multipart/form-data (CSV file upload).
  - **Response**: JSON summary of the import process.
  - **Success Codes**: 200 or 201.
  - **Errors**: 400 Bad Request for file format or validation issues.

---

## 3. Authentication and Authorization

- **Mechanism**: JWT-based authentication.
  - Users login via `/auth/login` and receive a JWT token.
  - Subsequent requests include the token in the `Authorization` header (Bearer token).
- **Role-Based Access Control**:
  - **Admin**: Full access to all endpoints (user management, template creation, CSV imports, etc.).
  - **Manager**: Limited access, primarily to view and update onboarding processes of users they manage.
  - **User**: Access limited to viewing and updating their own onboarding tasks.

---

## 4. Validation and Business Logic

- **Data Validation**:
  - Enforce database constraints: Non-nullable fields (email, firstName, lastName, positionName) and unique constraints (email, template's positionName).
  - Validate input formats (e.g., proper emails) and ensure numeric fields (e.g., taskOrder) are valid integers.

- **Business Logic**:
  - **User Creation**: When a new user (especially with role USER) is created, automatically trigger an onboarding process by copying tasks from the matching template.
  - **Template Integrity**: CRUD operations on templates should not retroactively affect in-progress onboarding processes.
  - **Manager Restrictions**: Managers can only modify onboarding processes for users they oversee.
  - **Task Completion**: Updating onboarding tasks (e.g., marking as completed) should recalculate the progress counters in the parent onboarding process.
  - **Archiving**: Favor archiving (soft deletion) over hard deletion to maintain historical data.

- **Security & Performance**:
  - Implement rate limiting to protect against brute-force and DoS attacks.
  - Require authentication on all endpoints except `/auth/login`.
  - Handle errors gracefully with clear success/error responses.

---

## Assumptions

- CSV import features are restricted to Admin users.
- Onboarding process creation can be triggered automatically upon user registration or manually by an admin.
- Soft deletion (archiving) is preferred over hard deletion for preserving historical records.
- Standard RESTful practices are followed with clear mappings between HTTP methods and CRUD operations.
