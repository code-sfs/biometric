#!/bin/bash

# =================================================================
# AttendanceSync Production Build Script
# =================================================================

set -e  # Exit on any error

echo "🚀 Building AttendanceSync for Production..."

# Get the project directory
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BUILD_DIR="$PROJECT_DIR/build"
PROD_DIR="$BUILD_DIR/production"

echo "📁 Project Directory: $PROJECT_DIR"

# Clean previous build
echo "🧹 Cleaning previous build..."
rm -rf "$BUILD_DIR"

# Create build directories
echo "📂 Creating build directories..."
mkdir -p "$BUILD_DIR/classes"
mkdir -p "$PROD_DIR/bin"
mkdir -p "$PROD_DIR/lib"
mkdir -p "$PROD_DIR/config"
mkdir -p "$PROD_DIR/logs"

# Download dependencies if they don't exist
echo "📦 Checking dependencies..."
LIB_DIR="$PROJECT_DIR/lib"
if [ ! -d "$LIB_DIR" ] || [ -z "$(ls -A $LIB_DIR)" ]; then
    echo "⬇️  Downloading dependencies..."
    mkdir -p "$LIB_DIR"
    cd "$LIB_DIR"
    
    # Download JAR files
    wget -q https://repo1.maven.org/maven2/org/apache/httpcomponents/httpclient/4.5.14/httpclient-4.5.14.jar
    wget -q https://repo1.maven.org/maven2/org/apache/httpcomponents/httpcore/4.4.16/httpcore-4.4.16.jar
    wget -q https://repo1.maven.org/maven2/com/googlecode/json-simple/json-simple/1.1.1/json-simple-1.1.1.jar
    wget -q https://repo1.maven.org/maven2/com/microsoft/sqlserver/mssql-jdbc/12.4.2.jre8/mssql-jdbc-12.4.2.jre8.jar
    wget -q https://repo1.maven.org/maven2/commons-logging/commons-logging/1.2/commons-logging-1.2.jar
    
    echo "✅ Dependencies downloaded successfully"
    cd "$PROJECT_DIR"
else
    echo "✅ Dependencies already exist"
fi

# Compile Java source files
echo "🔨 Compiling Java source files..."
javac -cp "$LIB_DIR/*" -source 8 -target 8 -d "$BUILD_DIR/classes" src/main/java/com/attendance/sync/*.java src/main/java/com/attendance/sync/gui/*.java

if [ $? -eq 0 ]; then
    echo "✅ Compilation successful"
else
    echo "❌ Compilation failed"
    exit 1
fi

# Create MANIFEST.MF
echo "📄 Creating MANIFEST.MF..."
cat > "$BUILD_DIR/MANIFEST.MF" << EOF
Main-Class: com.attendance.sync.AttendanceSync
Class-Path: lib/httpclient-4.5.14.jar lib/httpcore-4.4.16.jar lib/json-simple-1.1.1.jar lib/mssql-jdbc-12.4.2.jre8.jar lib/commons-logging-1.2.jar
Implementation-Title: AttendanceSync
Implementation-Version: 2.0
Implementation-Vendor: AttendanceSync Team
Built-By: Production Build
Build-Date: $(date +%Y-%m-%d)
Build-Time: $(date +%H:%M:%S)
EOF

# Create JAR file
echo "📦 Creating JAR file..."
cd "$PROJECT_DIR"
jar cfm "$PROD_DIR/bin/AttendanceSync.jar" "$BUILD_DIR/MANIFEST.MF" -C "$BUILD_DIR/classes" .

if [ $? -eq 0 ]; then
    echo "✅ JAR file created successfully"
else
    echo "❌ JAR creation failed"
    exit 1
fi

# Copy dependencies
echo "📋 Copying dependencies..."
cp -r "$LIB_DIR"/* "$PROD_DIR/lib/"

# Copy and update configuration
echo "⚙️  Setting up configuration..."
if [ -f "$PROJECT_DIR/config/application.properties" ]; then
    cp "$PROJECT_DIR/config/application.properties" "$PROD_DIR/config/"
    echo "✅ Configuration copied"
else
    echo "⚠️  Warning: No configuration file found, creating default..."
fi

# Create startup scripts
echo "📜 Creating startup scripts..."

# Create Linux/Unix bash script
cat > "$PROD_DIR/bin/attendancesync.sh" << 'EOF'
#!/bin/bash

# =================================================================
# AttendanceSync Production Startup Script
# =================================================================

# Set the application directory
APP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"
LOG_DIR="$APP_DIR/logs"
PID_FILE="$APP_DIR/logs/attendancesync.pid"

# Ensure logs directory exists
mkdir -p "$LOG_DIR"

# Function to start the application
start() {
    echo "Starting AttendanceSync..."
    
    # Check if already running
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p $PID > /dev/null 2>&1; then
            echo "AttendanceSync is already running (PID: $PID)"
            return 1
        else
            echo "Removing stale PID file..."
            rm -f "$PID_FILE"
        fi
    fi
    
    # Set environment variables if they exist
    if [ -f "$APP_DIR/config/environment.env" ]; then
        echo "Loading environment variables from config/environment.env"
        export $(grep -v '^#' "$APP_DIR/config/environment.env" | xargs)
    fi
    
    # Start the application in background
    cd "$APP_DIR"
    nohup java $JAVA_OPTS -cp "bin/AttendanceSync.jar:lib/*" com.attendance.sync.AttendanceSync > logs/application.out 2>&1 &
    
    # Save PID
    echo $! > "$PID_FILE"
    echo "AttendanceSync started with PID: $!"
    echo "Logs: $LOG_DIR/attendance-sync.log"
    echo "Application output: $LOG_DIR/application.out"
    echo ""
    echo "Waiting for application to initialize..."
    sleep 3

    # Show startup logs automatically
    echo "==============================================="
    echo "Showing live startup logs (Press Ctrl+C to exit):"
    echo "==============================================="
    
    # Show initial application output if it exists
    if [ -f "$LOG_DIR/application.out" ]; then
        sleep 2
        echo "--- Initial Application Output ---"
        cat "$LOG_DIR/application.out"
        echo ""
    fi
    
    # Wait for log file to be created and show live logs
    echo "--- Live Application Logs ---"
    timeout_count=0
    while [ ! -f "$LOG_DIR/attendance-sync.log" ] && [ $timeout_count -lt 10 ]; do
        echo "Waiting for log file to be created... ($((timeout_count + 1))/10)"
        sleep 1
        timeout_count=$((timeout_count + 1))
    done
    
    if [ -f "$LOG_DIR/attendance-sync.log" ]; then
        echo "Log file created. Showing live logs (Press Ctrl+C to exit):"
        echo "==============================================="
        # Follow the log file in real-time
        tail -f "$LOG_DIR/attendance-sync.log"
    else
        echo "Log file not created within 10 seconds."
        echo "Check application.out for startup messages:"
        if [ -f "$LOG_DIR/application.out" ]; then
            cat "$LOG_DIR/application.out"
        fi
        echo ""
        echo "==============================================="
        echo "AttendanceSync started. To view logs later, use:"
        echo "  $(basename "$0") logs"
        echo "==============================================="
    fi
}

# Function to stop the application
stop() {
    echo "Stopping AttendanceSync..."
    
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p $PID > /dev/null 2>&1; then
            kill $PID
            echo "Waiting for process to stop..."
            
            # Wait up to 30 seconds for graceful shutdown
            for i in {1..30}; do
                if ! ps -p $PID > /dev/null 2>&1; then
                    echo "AttendanceSync stopped successfully"
                    rm -f "$PID_FILE"
                    return 0
                fi
                sleep 1
            done
            
            # Force kill if still running
            echo "Force killing process..."
            kill -9 $PID
            rm -f "$PID_FILE"
            echo "AttendanceSync force stopped"
        else
            echo "AttendanceSync is not running"
            rm -f "$PID_FILE"
        fi
    else
        echo "PID file not found. AttendanceSync may not be running."
    fi
}

# Function to check status
status() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p $PID > /dev/null 2>&1; then
            echo "AttendanceSync is running (PID: $PID)"
            return 0
        else
            echo "AttendanceSync is not running (stale PID file found)"
            return 1
        fi
    else
        echo "AttendanceSync is not running"
        return 1
    fi
}

# Function to restart the application
restart() {
    stop
    sleep 2
    start
}

# Function to show logs
logs() {
    if [ -f "$LOG_DIR/attendance-sync.log" ]; then
        tail -f "$LOG_DIR/attendance-sync.log"
    else
        echo "Log file not found: $LOG_DIR/attendance-sync.log"
    fi
}

# Function to test database connection
test_connection() {
    echo "Testing database connection..."
    cd "$APP_DIR"
    java $JAVA_OPTS -cp "bin/AttendanceSync.jar:lib/*" com.attendance.sync.AttendanceSync --test-connection
}

# Function to test API connectivity
test_api() {
    echo "Testing API connectivity..."
    cd "$APP_DIR"
    java $JAVA_OPTS -cp "bin/AttendanceSync.jar:lib/*" com.attendance.sync.AttendanceSync --test-api
}

# Main script logic
case "$1" in
    start)
        start
        ;;
    stop)
        stop
        ;;
    restart)
        restart
        ;;
    status)
        status
        ;;
    logs)
        logs
        ;;
    test-connection)
        test_connection
        ;;
    test-api)
        test_api
        ;;
    *)
        echo "Usage: $0 {start|stop|restart|status|logs|test-connection|test-api}"
        echo ""
        echo "Commands:"
        echo "  start           Start AttendanceSync service"
        echo "  stop            Stop AttendanceSync service"
        echo "  restart         Restart AttendanceSync service"
        echo "  status          Show service status"
        echo "  logs            Show real-time logs"
        echo "  test-connection Test database connection"
        echo "  test-api        Test API connectivity"
        exit 1
        ;;
esac

exit $?
EOF

# Create Windows batch script
cat > "$PROD_DIR/bin/attendancesync.bat" << 'EOF'
@echo off
REM =================================================================
REM AttendanceSync Production Startup Script for Windows
REM =================================================================

setlocal enabledelayedexpansion

REM Set the application directory
set "APP_DIR=%~dp0.."
set "JAVA_OPTS=-Xms256m -Xmx512m -XX:+UseG1GC"
set "LOG_DIR=%APP_DIR%\logs"
set "PID_FILE=%APP_DIR%\logs\attendancesync.pid"

REM Ensure logs directory exists
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

REM Check command line argument - default to start if no argument provided
if "%1"=="" goto start
if "%1"=="start" goto start
if "%1"=="stop" goto stop
if "%1"=="restart" goto restart
if "%1"=="status" goto status
if "%1"=="logs" goto logs
if "%1"=="test-connection" goto test_connection
if "%1"=="test-api" goto test_api
if "%1"=="help" goto usage
goto usage

:start
echo Starting AttendanceSync...

REM Check if already running
if exist "%PID_FILE%" (
    set /p PID=<"%PID_FILE%"
    tasklist /fi "PID eq !PID!" 2>nul | find /i "java.exe" >nul
    if !errorlevel! equ 0 (
        echo AttendanceSync is already running (PID: !PID!)
        goto end
    ) else (
        echo Removing stale PID file...
        del "%PID_FILE%" 2>nul
    )
)

REM Load environment variables if they exist
if exist "%APP_DIR%\config\environment.env" (
    echo Loading environment variables from config\environment.env
    for /f "usebackq tokens=1,2 delims==" %%a in ("%APP_DIR%\config\environment.env") do (
        if not "%%a"=="" if not "%%a:~0,1%"=="#" (
            set "%%a=%%b"
        )
    )
)

REM Start the application in background
cd /d "%APP_DIR%"
start /b java %JAVA_OPTS% -cp "bin\AttendanceSync.jar;lib\*" com.attendance.sync.AttendanceSync > logs\application.out 2>&1

REM Get the PID of the started process
for /f "tokens=2" %%i in ('tasklist /fi "imagename eq java.exe" /fo csv ^| find "java.exe"') do (
    set "NEW_PID=%%~i"
    goto found_pid
)
:found_pid

REM Save PID
echo !NEW_PID! > "%PID_FILE%"
echo AttendanceSync started with PID: !NEW_PID!
echo Logs: %LOG_DIR%\attendance-sync.log
echo Application output: %LOG_DIR%\application.out
echo.
echo Waiting for application to initialize...
timeout /t 3 /nobreak >nul

REM Show startup logs automatically
echo ===============================================
echo Showing live startup logs (Press Ctrl+C to exit):
echo ===============================================

REM Show initial application output if it exists
if exist "%LOG_DIR%\application.out" (
    timeout /t 2 /nobreak >nul
    echo --- Initial Application Output ---
    type "%LOG_DIR%\application.out"
    echo.
)

REM Wait for log file to be created and show live logs
echo --- Live Application Logs ---
set timeout_count=0
:wait_for_log
if exist "%LOG_DIR%\attendance-sync.log" goto show_live_logs
if !timeout_count! geq 10 goto log_timeout
set /a timeout_count+=1
echo Waiting for log file to be created... (!timeout_count!/10)
timeout /t 1 /nobreak >nul
goto wait_for_log

:show_live_logs
echo Log file created. Showing live logs (Press Ctrl+C to exit):
echo ===============================================
REM Use PowerShell Get-Content -Wait for live log following (equivalent to tail -f)
powershell -Command "Get-Content '%LOG_DIR%\attendance-sync.log' -Wait"
goto end

:log_timeout
echo Log file not created within 10 seconds.
echo Check application.out for startup messages:
if exist "%LOG_DIR%\application.out" (
    type "%LOG_DIR%\application.out"
)
echo.
echo ===============================================
echo AttendanceSync started. To view logs later, use:
echo   %~nx0 logs
echo ===============================================
goto end

:stop
echo Stopping AttendanceSync...

if exist "%PID_FILE%" (
    set /p PID=<"%PID_FILE%"
    tasklist /fi "PID eq !PID!" 2>nul | find /i "java.exe" >nul
    if !errorlevel! equ 0 (
        echo Terminating process !PID!...
        taskkill /pid !PID! /f >nul
        echo Waiting for process to stop...
        timeout /t 3 /nobreak >nul
        echo AttendanceSync stopped successfully
        del "%PID_FILE%" 2>nul
    ) else (
        echo AttendanceSync is not running
        del "%PID_FILE%" 2>nul
    )
) else (
    echo PID file not found. AttendanceSync may not be running.
)
goto end

:status
if exist "%PID_FILE%" (
    set /p PID=<"%PID_FILE%"
    tasklist /fi "PID eq !PID!" 2>nul | find /i "java.exe" >nul
    if !errorlevel! equ 0 (
        echo AttendanceSync is running (PID: !PID!)
    ) else (
        echo AttendanceSync is not running (stale PID file found)
    )
) else (
    echo AttendanceSync is not running
)
goto end

:restart
call :stop
timeout /t 2 /nobreak >nul
call :start
goto end

:logs
if exist "%LOG_DIR%\attendance-sync.log" (
    echo Showing last 50 lines of log file. Press Ctrl+C to exit.
    type "%LOG_DIR%\attendance-sync.log" | more
) else (
    echo Log file not found: %LOG_DIR%\attendance-sync.log
)
goto end

:test_connection
echo Testing database connection...
cd /d "%APP_DIR%"
java %JAVA_OPTS% -cp "bin\AttendanceSync.jar;lib\*" com.attendance.sync.AttendanceSync --test-connection
goto end

:test_api
echo Testing API connectivity...
cd /d "%APP_DIR%"
java %JAVA_OPTS% -cp "bin\AttendanceSync.jar;lib\*" com.attendance.sync.AttendanceSync --test-api
goto end

:usage
echo Usage: %0 {start^|stop^|restart^|status^|logs^|test-connection^|test-api}
echo.
echo Commands:
echo   start           Start AttendanceSync service
echo   stop            Stop AttendanceSync service
echo   restart         Restart AttendanceSync service
echo   status          Show service status
echo   logs            Show application logs
echo   test-connection Test database connection
echo   test-api        Test API connectivity
echo.
echo Example: attendancesync.bat start

:end
REM Keep console window open when run by double-clicking
if "%1"=="" (
    echo.
    echo ===============================================
    echo AttendanceSync session ended.
    echo Press any key to close this window...
    echo ===============================================
    pause >nul
)
endlocal
EOF

# Create Windows GUI startup script
cat > "$PROD_DIR/bin/AttendanceSync-GUI.bat" << 'EOF'
@echo off
REM =================================================================
REM AttendanceSync GUI Desktop Application
REM =================================================================

setlocal enabledelayedexpansion

REM Set the application directory
set "APP_DIR=%~dp0.."
set "JAVA_OPTS=-Xms256m -Xmx512m"

echo ===============================================
echo   AttendanceSync Desktop Application
echo ===============================================
echo.
echo Starting GUI application...

REM Change to the correct directory
cd /d "%APP_DIR%"

REM Start the GUI application
java %JAVA_OPTS% -cp "bin\AttendanceSync.jar;lib\*" com.attendance.sync.gui.AttendanceSyncLauncher

REM Keep window open if there's an error
if %ERRORLEVEL% neq 0 (
    echo.
    echo ===============================================
    echo Application ended with error code %ERRORLEVEL%
    echo Press any key to close this window...
    echo ===============================================
    pause >nul
)
EOF

# Create Linux GUI startup script
cat > "$PROD_DIR/bin/attendancesync-gui.sh" << 'EOF'
#!/bin/bash

# =================================================================
# AttendanceSync GUI Desktop Application
# =================================================================

# Set the application directory
APP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
JAVA_OPTS="-Xms256m -Xmx512m"

echo "==============================================="
echo "  AttendanceSync Desktop Application"
echo "==============================================="
echo ""
echo "Starting GUI application..."

# Set environment variables if they exist
if [ -f "$APP_DIR/config/environment.env" ]; then
    echo "Loading environment variables from config/environment.env"
    export $(grep -v '^#' "$APP_DIR/config/environment.env" | xargs)
fi

# Start the GUI application
cd "$APP_DIR"
java $JAVA_OPTS -cp "bin/AttendanceSync.jar:lib/*" com.attendance.sync.gui.AttendanceSyncLauncher

exit $?
EOF

# Create Linux Server Mode script
cat > "$PROD_DIR/bin/AttendanceSync-Server.sh" << 'EOF'
#!/bin/bash

# =================================================================
# AttendanceSync Server Mode Launcher
# Continuous GUI operation with enhanced protection
# =================================================================

# Set the application directory
APP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
JAVA_OPTS="-Xms256m -Xmx512m"

echo "==============================================="
echo "  AttendanceSync Server Mode v2.0"
echo "  Continuous Biometric Sync Operation"
echo "==============================================="
echo ""
echo "🖥️ Starting server mode with GUI monitoring..."
echo "⚠️ This mode runs continuously until manually stopped"
echo ""

# Set environment variables if they exist
if [ -f "$APP_DIR/config/environment.env" ]; then
    echo "📄 Loading environment variables from config/environment.env"
    export $(grep -v '^#' "$APP_DIR/config/environment.env" | xargs)
fi

cd "$APP_DIR"

# Main server loop for continuous operation
while true; do
    echo "🚀 Starting AttendanceSync Server GUI..."
    echo "📅 $(date '+%Y-%m-%d %H:%M:%S') - Server session starting"
    
    # Launch GUI in background to minimize terminal interference
    java $JAVA_OPTS -cp "bin/AttendanceSync.jar:lib/*" com.attendance.sync.gui.AttendanceSyncGUI server &
    GUI_PID=$!
    
    echo "📱 GUI launched with PID: $GUI_PID"
    
    # Wait for a moment to let GUI initialize
    sleep 5
    
    # Try to minimize this terminal window
    if [ -n "$DISPLAY" ]; then
        # Try to minimize terminal if possible
        if command -v wmctrl >/dev/null 2>&1; then
            wmctrl -r "$(ps -p $$ -o comm= | tr -d '\n')" -b add,hidden 2>/dev/null || true
        elif command -v xdotool >/dev/null 2>&1; then
            WINDOW_ID=$(xdotool getactivewindow 2>/dev/null)
            if [ -n "$WINDOW_ID" ]; then
                xdotool windowminimize "$WINDOW_ID" 2>/dev/null || true
            fi
        fi
    fi
    
    # Wait for GUI process to complete or fail
    wait $GUI_PID
    EXIT_CODE=$?
    
    echo ""
    echo "📅 $(date '+%Y-%m-%d %H:%M:%S') - GUI session ended with code: $EXIT_CODE"
    
    if [ $EXIT_CODE -eq 0 ]; then
        echo "✅ GUI closed normally"
        echo "🔄 Will restart in 5 seconds..."
        sleep 5
    elif [ $EXIT_CODE -eq 99 ]; then
        echo "🛑 Server shutdown requested - exiting"
        break
    else
        echo "❌ GUI crashed or ended unexpectedly"
        echo "🔄 Will restart in 10 seconds..."
        sleep 10
    fi
done

echo ""
echo "🏁 AttendanceSync Server Mode stopped"
echo "📅 $(date '+%Y-%m-%d %H:%M:%S') - Server session ended"
EOF

# Create Windows quick-start script for double-clicking
cat > "$PROD_DIR/bin/Start-AttendanceSync.bat" << 'EOF'
@echo off
REM =================================================================
REM AttendanceSync Quick Start Script
REM Double-click this file to start AttendanceSync
REM =================================================================

echo ===============================================
echo   AttendanceSync - Quick Start
echo ===============================================
echo.
echo Starting AttendanceSync application...
echo.

REM Change to the correct directory
cd /d "%~dp0"

REM Start AttendanceSync with default settings
call attendancesync.bat start

REM Keep window open
echo.
echo ===============================================
echo Press any key to close this window...
echo ===============================================
pause >nul
EOF

# Set proper permissions for Linux scripts
chmod +x "$PROD_DIR/bin/attendancesync.sh"
chmod +x "$PROD_DIR/bin/attendancesync-gui.sh"
chmod +x "$PROD_DIR/bin/AttendanceSync-Server.sh"
echo "✅ Startup scripts created (Linux and Windows - Console & GUI)"

# Create environment template
echo "🔐 Creating environment template..."
cat > "$PROD_DIR/config/environment.env.template" << 'EOF'
# =================================================================
# AttendanceSync Environment Configuration Template
# =================================================================
# Copy this file to environment.env and edit with your settings
# These environment variables will override application.properties

# Database Configuration
DB_HOST=localhost
DB_PORT=1433
DB_NAME=Realtime
DB_USERNAME=sa
DB_PASSWORD=your_database_password

# School Configuration
SCHOOL_CODE=your_school_code
SCHOOL_NAME=Your School Name

# API Configuration
API_PRIMARY_URL=https://api.example.com/v1/attendance
API_FALLBACK_URL=
API_TIMEOUT=30000

# Application Configuration
APP_SLEEP_INTERVAL=60000
APP_DEBUG_ENABLED=true
APP_LOG_LEVEL=INFO

# Machine IDs (comma-separated)
MACHINE_IDS=101,102,103,104,105,106

# Security Note: Keep this file secure and never commit passwords to version control
EOF

chmod 600 "$PROD_DIR/config/environment.env.template"
echo "✅ Environment template created"

# Create README for production
echo "📖 Creating production README..."
cat > "$PROD_DIR/README.md" << EOF
# AttendanceSync Production Build

## Quick Start

1. **Configure the application:**
   \`\`\`bash
   cd config
   cp environment.env.template environment.env
   # Edit environment.env with your settings
   \`\`\`

2. **Test the configuration:**
   \`\`\`bash
   ./bin/attendancesync.sh test-connection
   ./bin/attendancesync.sh test-api
   \`\`\`

3. **Start the application:**
   \`\`\`bash
   ./bin/attendancesync.sh start
   \`\`\`

4. **Monitor logs:**
   \`\`\`bash
   ./bin/attendancesync.sh logs
   \`\`\`

## Available Commands

- \`./bin/attendancesync.sh start\` - Start the service
- \`./bin/attendancesync.sh stop\` - Stop the service
- \`./bin/attendancesync.sh restart\` - Restart the service
- \`./bin/attendancesync.sh status\` - Check service status
- \`./bin/attendancesync.sh logs\` - View real-time logs
- \`./bin/attendancesync.sh test-connection\` - Test database connection
- \`./bin/attendancesync.sh test-api\` - Test API connectivity

## Directory Structure

- \`bin/\` - Executable files (JAR and scripts)
- \`lib/\` - Dependencies
- \`config/\` - Configuration files
- \`logs/\` - Application logs

## Configuration

Edit \`config/application.properties\` or use environment variables in \`config/environment.env\`

Built on: $(date)
Version: 2.0
EOF

# Create documentation files
echo "📚 Creating documentation..."

# Copy QUICK-START guide if it exists
if [ -f "$PROD_DIR/QUICK-START.md" ]; then
    echo "✅ QUICK-START.md already exists"
else
    echo "⚠️  QUICK-START.md not found, should be created manually"
fi

# Copy DESKTOP-APP-GUIDE if it exists  
if [ -f "$PROD_DIR/DESKTOP-APP-GUIDE.md" ]; then
    echo "✅ DESKTOP-APP-GUIDE.md already exists"
else
    echo "⚠️  DESKTOP-APP-GUIDE.md not found, should be created manually"
fi

# Set proper permissions
echo "🔒 Setting permissions..."
chmod +x "$PROD_DIR/bin/attendancesync.sh" 2>/dev/null || true
chmod 600 "$PROD_DIR/config/environment.env.template" 2>/dev/null || true

# Display build summary
echo ""
echo "🎉 Production Build Complete!"
echo "============================================"
echo "📍 Build Location: $PROD_DIR"
echo "📦 JAR File: $PROD_DIR/bin/AttendanceSync.jar"
echo "🔧 Linux Console: $PROD_DIR/bin/attendancesync.sh"
echo "🔧 Windows Console: $PROD_DIR/bin/attendancesync.bat"
echo "🖥️ Linux GUI: $PROD_DIR/bin/attendancesync-gui.sh"
echo "�️ Windows GUI: $PROD_DIR/bin/AttendanceSync-GUI.bat"
echo "🚀 Quick Start: $PROD_DIR/bin/Start-AttendanceSync.bat"
echo "⚙️  Configuration: $PROD_DIR/config/"
echo ""
echo "🚀 To deploy:"
echo "   1. Copy the entire '$PROD_DIR' directory to your production server"
echo "   2. Configure settings in config/application.properties or environment.env"
echo "   3. Linux/Unix: ./bin/attendancesync.sh start"
echo "   4. Windows: Double-click Start-AttendanceSync.bat or AttendanceSync-GUI.bat"
echo ""
echo "💡 For help:"
echo "   Linux/Unix: ./bin/attendancesync.sh"
echo "   Windows: See QUICK-START.md for detailed instructions"
