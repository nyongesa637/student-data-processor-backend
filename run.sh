#!/usr/bin/env bash
# =============================================================================
# Student Data Processor - Backend Setup & Run Script
# Works on Linux, macOS, and Windows (Git Bash / WSL / MSYS2)
#
# Usage:
#   ./run.sh                  Full setup: install deps, create DB, build & run
#   ./run.sh --skip-setup     Skip installation, validate & run only
#   ./run.sh -s               Short alias for --skip-setup
#   ./run.sh --check          Only validate environment, don't run
# =============================================================================
set -euo pipefail

# --- Colors (disabled on dumb terminals) ---
if [[ "${TERM:-dumb}" != "dumb" ]] && command -v tput &>/dev/null; then
    RED=$(tput setaf 1) GREEN=$(tput setaf 2) YELLOW=$(tput setaf 3)
    BLUE=$(tput setaf 4) BOLD=$(tput bold) NC=$(tput sgr0)
else
    RED="" GREEN="" YELLOW="" BLUE="" BOLD="" NC=""
fi

info()  { echo "${BLUE}[INFO]${NC}  $*"; }
ok()    { echo "${GREEN}[OK]${NC}    $*"; }
warn()  { echo "${YELLOW}[WARN]${NC}  $*"; }
fail()  { echo "${RED}[FAIL]${NC}  $*"; }
header(){ echo ""; echo "${BOLD}=== $* ===${NC}"; }

# --- Detect OS ---
detect_os() {
    case "$(uname -s)" in
        Linux*)  OS=linux ;;
        Darwin*) OS=mac ;;
        MINGW*|MSYS*|CYGWIN*) OS=windows ;;
        *) OS=unknown ;;
    esac

    if [[ "$OS" == "linux" ]]; then
        if command -v dnf &>/dev/null; then PKG=dnf
        elif command -v apt-get &>/dev/null; then PKG=apt
        elif command -v pacman &>/dev/null; then PKG=pacman
        elif command -v zypper &>/dev/null; then PKG=zypper
        else PKG=unknown; fi
    elif [[ "$OS" == "mac" ]]; then
        PKG=brew
    else
        PKG=manual
    fi
}

# --- Parse arguments ---
SKIP_SETUP=false
CHECK_ONLY=false
for arg in "$@"; do
    case "$arg" in
        --skip-setup|-s) SKIP_SETUP=true ;;
        --check|-c) CHECK_ONLY=true ;;
        --help|-h)
            echo "Usage: ./run.sh [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  (no args)        Full setup: install prerequisites, create DB, build & run"
            echo "  --skip-setup, -s Skip installation, only validate environment & run"
            echo "  --check, -c      Only validate environment, don't run"
            echo "  --help, -h       Show this help"
            exit 0 ;;
    esac
done

# --- Validation functions ---
has_cmd()   { command -v "$1" &>/dev/null; }

check_java() {
    if has_cmd java; then
        local ver
        ver=$(java -version 2>&1 | head -1 | sed -E 's/.*"([0-9]+).*/\1/')
        if [[ "$ver" -ge 17 ]]; then
            ok "Java $ver found"
            return 0
        else
            warn "Java $ver found, but Java 17+ is required"
            return 1
        fi
    else
        fail "Java not found"
        return 1
    fi
}

check_maven() {
    if has_cmd mvn; then
        ok "Maven $(mvn -v 2>/dev/null | head -1 | sed -E 's/.*Maven ([0-9.]+).*/\1/') found"
        return 0
    else
        fail "Maven not found"
        return 1
    fi
}

check_postgres() {
    if has_cmd psql; then
        ok "PostgreSQL client found"
        return 0
    else
        fail "PostgreSQL not found"
        return 1
    fi
}

check_pg_running() {
    if pg_isready -q 2>/dev/null; then
        ok "PostgreSQL is running"
        return 0
    elif has_cmd psql && psql -U postgres -c "SELECT 1" &>/dev/null; then
        ok "PostgreSQL is running"
        return 0
    else
        warn "PostgreSQL does not appear to be running"
        return 1
    fi
}

check_database() {
    if psql -U postgres -lqt 2>/dev/null | cut -d\| -f1 | grep -qw student_data_processor; then
        ok "Database 'student_data_processor' exists"
        return 0
    else
        warn "Database 'student_data_processor' not found"
        return 1
    fi
}

check_output_dir() {
    local dir="/var/log/applications/API/dataprocessing"
    if [[ "$OS" == "windows" ]]; then
        dir="$HOME/dataprocessing"
    fi
    if [[ -d "$dir" && -w "$dir" ]]; then
        ok "Output directory exists: $dir"
        return 0
    else
        warn "Output directory missing or not writable: $dir"
        return 1
    fi
}

# --- Install functions ---
install_java() {
    header "Installing Java 17"
    case "$OS" in
        linux)
            case "$PKG" in
                dnf) sudo dnf install -y java-17-openjdk-devel ;;
                apt) sudo apt-get update && sudo apt-get install -y openjdk-17-jdk ;;
                pacman) sudo pacman -S --noconfirm jdk17-openjdk ;;
                zypper) sudo zypper install -y java-17-openjdk-devel ;;
                *) fail "Cannot auto-install Java on this Linux distro. Install Java 17+ manually."; exit 1 ;;
            esac ;;
        mac)
            if ! has_cmd brew; then
                fail "Homebrew not found. Install it from https://brew.sh then re-run."
                exit 1
            fi
            brew install openjdk@17
            sudo ln -sfn "$(brew --prefix)/opt/openjdk@17/libexec/openjdk.jdk" /Library/Java/JavaVirtualMachines/openjdk-17.jdk 2>/dev/null || true
            ;;
        windows)
            if has_cmd winget; then
                winget install --id Microsoft.OpenJDK.17 --accept-source-agreements --accept-package-agreements
            elif has_cmd choco; then
                choco install openjdk17 -y
            else
                fail "Install Java 17 manually: https://adoptium.net/temurin/releases/?version=17"
                fail "Then add JAVA_HOME to your PATH and re-run this script."
                exit 1
            fi ;;
    esac
}

install_maven() {
    header "Installing Maven"
    case "$OS" in
        linux)
            case "$PKG" in
                dnf) sudo dnf install -y maven ;;
                apt) sudo apt-get update && sudo apt-get install -y maven ;;
                pacman) sudo pacman -S --noconfirm maven ;;
                zypper) sudo zypper install -y maven ;;
                *) fail "Cannot auto-install Maven. Install it manually."; exit 1 ;;
            esac ;;
        mac) brew install maven ;;
        windows)
            if has_cmd choco; then
                choco install maven -y
            else
                fail "Install Maven manually: https://maven.apache.org/download.cgi"
                exit 1
            fi ;;
    esac
}

install_postgres() {
    header "Installing PostgreSQL"
    case "$OS" in
        linux)
            case "$PKG" in
                dnf)
                    sudo dnf install -y postgresql-server postgresql
                    sudo postgresql-setup --initdb 2>/dev/null || true
                    sudo systemctl enable --now postgresql ;;
                apt)
                    sudo apt-get update && sudo apt-get install -y postgresql postgresql-client
                    sudo systemctl enable --now postgresql ;;
                pacman)
                    sudo pacman -S --noconfirm postgresql
                    sudo -u postgres initdb -D /var/lib/postgres/data 2>/dev/null || true
                    sudo systemctl enable --now postgresql ;;
                zypper)
                    sudo zypper install -y postgresql-server postgresql
                    sudo systemctl enable --now postgresql ;;
                *) fail "Cannot auto-install PostgreSQL. Install it manually."; exit 1 ;;
            esac ;;
        mac)
            brew install postgresql@15
            brew services start postgresql@15 ;;
        windows)
            if has_cmd choco; then
                choco install postgresql15 -y
            else
                fail "Install PostgreSQL manually: https://www.postgresql.org/download/windows/"
                exit 1
            fi ;;
    esac
}

create_database() {
    header "Creating database"
    if check_database 2>/dev/null; then
        info "Database already exists, skipping"
        return 0
    fi
    if psql -U postgres -c "CREATE DATABASE student_data_processor;" 2>/dev/null; then
        ok "Database 'student_data_processor' created"
    else
        warn "Could not create database automatically."
        info "Run manually: psql -U postgres -c \"CREATE DATABASE student_data_processor;\""
    fi
}

create_output_dir() {
    local dir="/var/log/applications/API/dataprocessing"
    if [[ "$OS" == "windows" ]]; then
        dir="$HOME/dataprocessing"
        mkdir -p "$dir"
        ok "Output directory created: $dir"
        warn "Update app.output.directory in application.properties to: $dir"
    else
        if [[ ! -d "$dir" ]]; then
            sudo mkdir -p "$dir"
            sudo chmod 777 "$dir"
            ok "Output directory created: $dir"
        fi
    fi
}

# --- Main logic ---
main() {
    header "Student Data Processor - Backend"
    detect_os
    info "Detected OS: $OS (package manager: $PKG)"

    SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    cd "$SCRIPT_DIR"
    info "Working directory: $(pwd)"

    if [[ "$SKIP_SETUP" == true || "$CHECK_ONLY" == true ]]; then
        header "Validating Environment"
        ERRORS=0
        check_java    || ERRORS=$((ERRORS + 1))
        check_maven   || ERRORS=$((ERRORS + 1))
        check_postgres || ERRORS=$((ERRORS + 1))
        check_pg_running || ERRORS=$((ERRORS + 1))
        check_database || ERRORS=$((ERRORS + 1))
        check_output_dir || ERRORS=$((ERRORS + 1))

        if [[ $ERRORS -gt 0 ]]; then
            echo ""
            fail "$ERRORS check(s) failed. Fix the issues above or run without --skip-setup for auto-install."
            if [[ "$CHECK_ONLY" == true ]]; then exit 1; fi
            warn "Continuing anyway..."
        else
            ok "All checks passed!"
        fi

        if [[ "$CHECK_ONLY" == true ]]; then
            exit 0
        fi
    else
        header "Setting Up Environment"

        # Java
        if ! check_java 2>/dev/null; then
            install_java
            check_java || { fail "Java installation failed"; exit 1; }
        fi

        # Maven
        if ! check_maven 2>/dev/null; then
            install_maven
            check_maven || { fail "Maven installation failed"; exit 1; }
        fi

        # PostgreSQL
        if ! check_postgres 2>/dev/null; then
            install_postgres
            check_postgres || { fail "PostgreSQL installation failed"; exit 1; }
        fi

        # Ensure PostgreSQL is running
        if ! check_pg_running 2>/dev/null; then
            info "Attempting to start PostgreSQL..."
            if [[ "$OS" == "mac" ]]; then
                brew services start postgresql@15 2>/dev/null || brew services start postgresql 2>/dev/null || true
            elif [[ "$OS" == "linux" ]]; then
                sudo systemctl start postgresql 2>/dev/null || true
            fi
            sleep 2
            check_pg_running || warn "Could not start PostgreSQL automatically. Start it manually."
        fi

        # Database
        create_database

        # Output directory
        create_output_dir

        # Build
        header "Building Project"
        info "Running: mvn clean compile -q"
        mvn clean compile -q
        ok "Build successful"
    fi

    # Run
    header "Starting Backend Server"
    info "URL: http://localhost:8080"
    info "Press Ctrl+C to stop"
    echo ""
    mvn spring-boot:run
}

main "$@"
