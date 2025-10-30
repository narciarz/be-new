# Be New

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-20-red.svg)](https://angular.io/)
[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.java.net/)

## Table of Contents

- [Project Description](#project-description)
- [Tech Stack](#tech-stack)
- [Getting Started Locally](#getting-started-locally)
- [Available Scripts](#available-scripts)
- [Project Scope](#project-scope)
- [Project Status](#project-status)
- [License](#license)

## Project Description

**Be New** is a web application designed to simplify and standardize the employee onboarding process within organizations. The system provides centralized management of onboarding checklists, visibility of progress for managers, and complete control over task templates for administrators. The product is delivered as a SaaS solution running in a browser with secure authentication and granular role-based access control.

### Key Features

- **User Management**: CRUD operations for Manager and User accounts with role-based access
- **Checklist Templates**: Create, edit, and manage templates assigned to specific positions with versioning support
- **Automated Onboarding**: Automatic checklist generation when a new employee account is created
- **Manager Dashboard**: Overview of assigned employees with progress tracking and search functionality
- **Progress Tracking**: Visual progress bar and task completion status for both managers and employees
- **Security**: Session-based authentication with 30-minute timeout and role-based authorization

### User Roles

- **Administrator**: Full system access, manages users and templates
- **Manager**: Oversees assigned employees, tracks progress, and manages onboarding processes
- **User**: Views and completes assigned onboarding tasks

## Tech Stack

### Frontend

- **Angular 20**: Modern web framework with standalone components and signals
- **TypeScript 5**: Static typing for improved development experience
- **Angular Material**: Pre-built UI components for consistent design
- **CSS**: Custom styling

### Backend

- **Spring Boot 3.5.7**: Enterprise-grade Java framework
- **Java 25**: Latest Java version with modern language features
- **Spring Security**: Authentication and authorization
- **Spring Data JPA**: Database access and ORM
- **PostgreSQL 16**: Production database (planned)
- **H2 Database**: In-memory database for development and testing

### Build & Development Tools

- **Maven**: Multi-module project management and dependency resolution
- **Node.js v22.21.1**: JavaScript runtime for frontend build
- **Frontend Maven Plugin**: Integration of Angular build into Maven lifecycle
- **GitHub Actions**: CI/CD pipeline automation

## Getting Started Locally

### Prerequisites

- **Java 25** or higher
- **Maven 3.6+**
- **Node.js 22.21.1** (will be installed automatically by Maven)
- **PostgreSQL 16** (for production setup)

### Installation Steps

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/be-new.git
cd be-new
```

2. **Build the entire project**
```bash
mvn clean install
```

This command will:
- Install Node.js and npm automatically in the `benew-frontend/target` directory
- Install frontend dependencies
- Build the Angular application
- Package the frontend into a JAR file
- Build the backend services
- Integrate the frontend with the backend

3. **Run the application**
```bash
cd benew-services
mvn spring-boot:run
```

Alternatively, run the packaged JAR:
```bash
java -jar benew-services/target/benew-services-0.0.1-SNAPSHOT.jar
```

4. **Access the application**

Open your browser and navigate to:
```
http://localhost:8080
```

### Development Mode

For frontend development with hot reload:

```bash
cd benew-frontend
npm install
npm start
```

The Angular development server will start at `http://localhost:4200`

## Available Scripts

### Root Project

| Command | Description |
|---------|-------------|
| `mvn clean` | Removes all generated files and build artifacts |
| `mvn install` | Builds all modules and installs them in the local repository |
| `mvn package` | Packages the application into JAR files |

### Backend (benew-services)

| Command | Description |
|---------|-------------|
| `mvn spring-boot:run` | Runs the Spring Boot application in development mode |
| `mvn test` | Runs all backend unit and integration tests |
| `mvn spring-boot:build-image` | Creates a Docker image of the application |

### Frontend (benew-frontend)

| Command | Description |
|---------|-------------|
| `npm start` | Starts the Angular development server at `localhost:4200` |
| `npm run build` | Builds the Angular app for production |
| `npm run watch` | Builds the app in watch mode for development |
| `npm test` | Runs unit tests with Karma and Jasmine |
| `npm run ng` | Runs Angular CLI commands |

## Project Scope

### MVP Features (In Scope)

#### Administrator Functions
- Create, read, update, and delete user accounts (Manager and User roles)
- Assign users to managers
- Password reset functionality
- Create and manage checklist templates for different positions
- Add, edit, delete, and reorder tasks within templates
- One-time CSV import for bulk task creation
- Template versioning (changes don't affect active onboarding processes)

#### Manager Functions
- View dashboard with list of assigned employees
- Track onboarding progress with visual indicators
- Search and filter employees
- Mark tasks as complete
- Archive and restore onboarding processes
- Undo task completion

#### User Functions
- View personal onboarding checklist
- Mark assigned tasks as complete
- Track personal progress

#### Security & Authentication
- Password-based login
- Session timeout after 30 minutes of inactivity
- Role-based access control (Administrator, Manager, User)

### Out of Scope (Post-MVP)

- Task comments and discussions
- Email notifications
- Mobile application
- Integration with HR systems or other corporate tools
- Self-service password change (only admin reset available)
- Automated GDPR data deletion (to be considered post-MVP)

### Success Metrics

- **Adoption Rate**: At least 90% of new employees in covered positions use Be New
- **Process Standardization**: 100% of employees with the same role receive identical base checklists
- **Dashboard Clarity**: Managers rate dashboard usability at minimum 4/5 in surveys
- **Time Efficiency**: 20% reduction in median onboarding completion time compared to manual process

## Project Status

🚧 **Current Status**: In Development (MVP Phase)

### Completed
- [x] Project structure setup with Maven multi-module configuration
- [x] Spring Boot backend initialization
- [x] Angular 20 frontend initialization
- [x] Frontend-backend integration via Maven
- [x] Basic security configuration

### In Progress
- [ ] Database schema design and implementation
- [ ] User authentication and authorization
- [ ] REST API development
- [ ] Frontend components and routing
- [ ] Manager dashboard
- [ ] Checklist management

### Planned
- [ ] CSV import functionality
- [ ] Template versioning system
- [ ] Progress tracking and archiving
- [ ] Comprehensive testing suite
- [ ] CI/CD pipeline setup
- [ ] Production deployment configuration

## License

This project is licensed under the **GNU General Public License v3.0** - see the [LICENSE](LICENSE) file for details.

### Summary

- You can copy, modify, and distribute this software
- You must include the original copyright and license notice
- You must disclose the source code when distributing
- You must use the same license for derivative works
- There is no warranty for the software

For more information, visit [GNU GPL v3.0](https://www.gnu.org/licenses/gpl-3.0.en.html)

---

**Made with ❤️ for better employee onboarding experiences**
