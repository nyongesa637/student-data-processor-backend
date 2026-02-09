# Backend Changelog

## [3.0.0] - 2026-02-09

### Added
- `component` column on `changelog_entries` table (GENERAL, FRONTEND, BACKEND)
- ChangelogService with SSE (SseEmitter) subscriber management
- SSE streaming endpoint `GET /api/changelog/stream`
- `POST /api/changelog` endpoint for creating entries with SSE broadcast
- Component filter on `GET /api/changelog?component=`
- DataSeeder (CommandLineRunner) for idempotent changelog seeding
- Repository methods: `findByComponentOrderByReleaseDateDesc`, `existsByVersionAndComponent`

### Changed
- ChangelogController now delegates to ChangelogService

## [2.0.0] - 2026-02-09

### Added
- Analytics endpoint with class distribution and score statistics
- Changelog system with backend storage and REST API
- Notification system with backend persistence
- Feature request submission endpoint
- Database indexes on student_class and student_id columns

### Fixed
- Student search query parameter mismatch

## [1.0.0] - 2026-01-01

### Added
- Spring Boot application with PostgreSQL
- Excel data generation with configurable record count
- Excel to CSV processing with score transformation
- CSV to database upload
- Student REST API with pagination, search, and filtering
- Export endpoints for Excel, CSV, and PDF
