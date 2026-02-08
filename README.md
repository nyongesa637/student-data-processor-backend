# Student Data Processor - Backend

Spring Boot 3.4.5 REST API for student data generation, Excel/CSV processing, database upload, and reporting with pagination, filtering, and multi-format export.

## Tech Stack

- **Java 17** with **Spring Boot 3.4.5**
- **Maven** for build management
- **PostgreSQL** for data persistence
- **Apache POI** (SXSSFWorkbook) for memory-efficient Excel generation/reading (handles 1M+ rows)
- **OpenCSV** for CSV processing
- **OpenPDF** for PDF report generation
- **Spring Data JPA** with Hibernate for ORM

## Prerequisites

| Tool       | Version  | Purpose                  |
|------------|----------|--------------------------|
| Java JDK   | 17+      | Runtime and compilation  |
| Maven      | 3.8+     | Build and dependency mgmt|
| PostgreSQL | 13+      | Database                 |

## Quick Start

```bash
# 1. Clone the repository
git clone https://github.com/nyongesa637/student-data-processor-backend.git
cd student-data-processor-backend

# 2. Run the setup script (installs prerequisites, creates DB, builds & runs)
chmod +x run.sh
./run.sh

# OR if you already have everything installed:
./run.sh --skip-setup
```

## Manual Setup

```bash
# 1. Create the PostgreSQL database
psql -U postgres -c "CREATE DATABASE student_data_processor;"

# 2. Create the output directory
sudo mkdir -p /var/log/applications/API/dataprocessing/
sudo chmod 777 /var/log/applications/API/dataprocessing/

# 3. Build and run
mvn spring-boot:run
```

The server starts on **http://localhost:8080**.

## Configuration

Edit `src/main/resources/application.properties`:

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | `8080` | Server port |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/student_data_processor` | Database URL |
| `spring.datasource.username` | `postgres` | Database username |
| `spring.datasource.password` | `postgres` | Database password |
| `app.output.directory` | `/var/log/applications/API/dataprocessing/` | File output directory |
| `spring.servlet.multipart.max-file-size` | `500MB` | Max upload file size |

## API Endpoints

### Feature A: Data Generation (Excel)

| Method | Endpoint | Params | Description |
|--------|----------|--------|-------------|
| `POST` | `/api/generate` | `count` (int) | Generate Excel file with random student records |

**Example:**
```bash
curl -X POST "http://localhost:8080/api/generate?count=1000"
```

**Response:**
```json
{ "message": "Generated successfully", "filename": "students_20260209_143022.xlsx", "recordCount": 1000 }
```

Generated Excel contains columns: Student ID, First Name, Last Name, DOB, Class (A-J), Score (0-100).

### Feature B: Data Processing (Excel to CSV)

| Method | Endpoint | Body | Description |
|--------|----------|------|-------------|
| `POST` | `/api/process` | Multipart file (`.xlsx`) | Convert Excel to CSV with score +10 |

**Example:**
```bash
curl -X POST -F "file=@students.xlsx" "http://localhost:8080/api/process"
```

**Response:**
```json
{ "message": "Processed successfully", "csvFile": "students_processed.csv" }
```

Uses SAX-based streaming parser for memory-efficient processing of large files. Each student's score is increased by 10.

### Feature C: Data Upload (CSV to Database)

| Method | Endpoint | Body | Description |
|--------|----------|------|-------------|
| `POST` | `/api/upload` | Multipart file (`.csv`) | Upload CSV to database with score +5 |

**Example:**
```bash
curl -X POST -F "file=@students_processed.csv" "http://localhost:8080/api/upload"
```

**Response:**
```json
{ "message": "Upload successful", "recordsInserted": 1000 }
```

Uses batch inserts (1000 records per batch) for optimal database performance. Each student's score is increased by 5. Total score transformation: original + 10 (processing) + 5 (upload) = original + 15.

### Feature D: Reporting

| Method | Endpoint | Params | Description |
|--------|----------|--------|-------------|
| `GET` | `/api/students` | `page`, `size`, `search`, `studentClass` | Paginated student list |
| `GET` | `/api/students/classes` | - | List of distinct classes |
| `GET` | `/api/students/export/excel` | `search`, `studentClass` | Export filtered data as Excel |
| `GET` | `/api/students/export/csv` | `search`, `studentClass` | Export filtered data as CSV |
| `GET` | `/api/students/export/pdf` | `search`, `studentClass` | Export filtered data as PDF |

**Pagination example:**
```bash
curl "http://localhost:8080/api/students?page=0&size=25&studentClass=A"
```

**Export example:**
```bash
curl -o students.pdf "http://localhost:8080/api/students/export/pdf?studentClass=B"
```

## Project Structure

```
src/main/java/com/studentdata/
├── StudentDataProcessorApplication.java    # Spring Boot entry point
├── config/
│   └── CorsConfig.java                     # CORS for Angular (localhost:4200)
├── controller/
│   ├── DataGenerationController.java       # POST /api/generate
│   ├── DataProcessingController.java       # POST /api/process
│   ├── DataUploadController.java           # POST /api/upload
│   └── ReportController.java              # GET /api/students + exports
├── dto/
│   └── StudentDto.java                     # Data transfer object
├── entity/
│   └── Student.java                        # JPA entity (students table)
├── repository/
│   └── StudentRepository.java             # JPA queries with pagination
└── service/
    ├── DataGenerationService.java          # Excel generation (SXSSFWorkbook)
    ├── DataProcessingService.java          # Excel→CSV (SAX parser, score+10)
    ├── DataUploadService.java             # CSV→DB (batch insert, score+5)
    └── ReportService.java                 # Pagination + Excel/CSV/PDF export
```

## Database Schema

**Table: `students`** (auto-created by JPA)

| Column | Type | Description |
|--------|------|-------------|
| `id` | `BIGINT` (PK, auto) | Primary key |
| `student_id` | `VARCHAR` | Student identifier (e.g., STU-000001) |
| `first_name` | `VARCHAR` | First name |
| `last_name` | `VARCHAR` | Last name |
| `dob` | `VARCHAR` | Date of birth (YYYY-MM-DD) |
| `student_class` | `VARCHAR` | Class (A through J) |
| `score` | `INTEGER` | Score (original + 15 after full pipeline) |

## Design Decisions

- **SXSSFWorkbook** for Excel generation: Streams rows to disk, enabling 1M+ row generation without OutOfMemoryError
- **SAX-based Excel reader** for processing: Event-driven parsing avoids loading entire file into memory
- **Batch inserts** (1000 per batch): Balances memory usage with database round-trip efficiency
- **Server-side pagination**: Essential for handling 1M+ records without browser memory issues
- **CORS**: Configured to allow requests from Angular dev server at `http://localhost:4200`

## Related

- **Frontend**: [student-data-processor-frontend](https://github.com/nyongesa637/student-data-processor-frontend)
