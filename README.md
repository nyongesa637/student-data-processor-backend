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

The `run.sh` script automatically detects your OS and package manager, installs missing prerequisites, creates the database, and starts the server. See [Automated Setup Script](#automated-setup-script) for details.

## Manual Setup by Operating System

### Linux

#### Install Java 17

**Fedora / RHEL / CentOS:**
```bash
sudo dnf install -y java-17-openjdk-devel
```

**Ubuntu / Debian:**
```bash
sudo apt-get update
sudo apt-get install -y openjdk-17-jdk
```

**Arch Linux:**
```bash
sudo pacman -S jdk17-openjdk
```

Verify installation:
```bash
java -version
```

#### Install Maven

**Fedora / RHEL / CentOS:**
```bash
sudo dnf install -y maven
```

**Ubuntu / Debian:**
```bash
sudo apt-get install -y maven
```

**Arch Linux:**
```bash
sudo pacman -S maven
```

Verify installation:
```bash
mvn -v
```

#### Install and Configure PostgreSQL

**Fedora / RHEL / CentOS:**
```bash
sudo dnf install -y postgresql-server postgresql
sudo postgresql-setup --initdb
sudo systemctl enable --now postgresql
```

**Ubuntu / Debian:**
```bash
sudo apt-get install -y postgresql postgresql-client
sudo systemctl enable --now postgresql
```

**Arch Linux:**
```bash
sudo pacman -S postgresql
sudo -u postgres initdb -D /var/lib/postgres/data
sudo systemctl enable --now postgresql
```

#### Create the Database (Linux)

```bash
# Switch to the postgres user and create the database
sudo -u postgres psql -c "CREATE DATABASE student_data_processor;"

# OR if using password authentication:
psql -U postgres -h localhost -c "CREATE DATABASE student_data_processor;"
```

#### Create the Output Directory (Linux)

```bash
sudo mkdir -p /var/log/applications/API/dataprocessing/
sudo chmod 777 /var/log/applications/API/dataprocessing/
```

---

### macOS

#### Install Java 17

```bash
# Using Homebrew (install from https://brew.sh if not installed)
brew install openjdk@17

# Symlink so the system finds it
sudo ln -sfn "$(brew --prefix)/opt/openjdk@17/libexec/openjdk.jdk" \
  /Library/Java/JavaVirtualMachines/openjdk-17.jdk
```

Verify installation:
```bash
java -version
```

#### Install Maven

```bash
brew install maven
```

Verify installation:
```bash
mvn -v
```

#### Install and Configure PostgreSQL

```bash
brew install postgresql@15
brew services start postgresql@15
```

#### Create the Database (macOS)

```bash
psql postgres -c "CREATE DATABASE student_data_processor;"
```

#### Create the Output Directory (macOS)

```bash
sudo mkdir -p /var/log/applications/API/dataprocessing/
sudo chmod 777 /var/log/applications/API/dataprocessing/
```

---

### Windows

#### Install Java 17

**Using winget (Windows 10/11):**
```powershell
winget install --id Microsoft.OpenJDK.17
```

**Using Chocolatey:**
```powershell
choco install openjdk17 -y
```

**Manual install:**
Download from [Adoptium Temurin](https://adoptium.net/temurin/releases/?version=17) and add `JAVA_HOME` to your system environment variables.

Verify installation (open a new terminal):
```powershell
java -version
```

#### Install Maven

**Using Chocolatey:**
```powershell
choco install maven -y
```

**Manual install:**
Download from [Apache Maven](https://maven.apache.org/download.cgi), extract, and add the `bin` directory to your `PATH`.

Verify installation:
```powershell
mvn -v
```

#### Install and Configure PostgreSQL

**Using Chocolatey:**
```powershell
choco install postgresql15 -y
```

**Manual install:**
Download from [PostgreSQL for Windows](https://www.postgresql.org/download/windows/) and run the installer. The installer will prompt you to set a password for the `postgres` user.

#### Create the Database (Windows)

Open a terminal and run:
```powershell
psql -U postgres -c "CREATE DATABASE student_data_processor;"
```

You will be prompted for the `postgres` password you set during installation.

#### Create the Output Directory (Windows)

The default output path `/var/log/applications/API/dataprocessing/` does not exist on Windows. Create a directory and update the configuration:

```powershell
mkdir C:\dataprocessing
```

Then edit `src/main/resources/application.properties` and change:
```properties
app.output.directory=C:/dataprocessing/
```

---

### Build and Run

After completing the setup for your OS:

```bash
# Build the project
mvn clean compile

# Run the server
mvn spring-boot:run
```

The server starts on **http://localhost:8080**.

## Automated Setup Script

The `run.sh` script handles the full setup automatically on Linux, macOS, and Windows (Git Bash / WSL / MSYS2):

```bash
./run.sh                  # Full setup: install deps, create DB, build & run
./run.sh --skip-setup     # Skip installation, validate & run only
./run.sh -s               # Short alias for --skip-setup
./run.sh --check          # Only validate environment, don't run
./run.sh --help           # Show help
```

The script:
- Detects your OS and package manager (dnf, apt, pacman, zypper, brew, winget, choco)
- Installs Java 17, Maven, and PostgreSQL if missing
- Starts PostgreSQL if it's not running
- Creates the `student_data_processor` database
- Creates the output directory
- Builds the project with Maven
- Starts the Spring Boot server

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
