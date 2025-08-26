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
