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
