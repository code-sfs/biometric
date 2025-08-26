#!/bin/bash

# AttendanceSync Live Log Monitoring Demonstration
# This script generates sample log entries to demonstrate the filtering capabilities

LOG_FILE="logs/attendance-sync.log"
mkdir -p logs

echo "🔴 Demonstrating AttendanceSync Live Log Monitoring..."
echo "📝 Generating sample log entries..."

# Create sample log entries that WILL be shown (relevant)
echo "$(date '+%b %d, %Y %I:%M:%S %p') INFO: API request sent to https://api.school.com/attendance" >> $LOG_FILE
sleep 1

echo "$(date '+%b %d, %Y %I:%M:%S %p') INFO: Processing Employee 12345 biomatric code 987654" >> $LOG_FILE
sleep 1

echo "$(date '+%b %d, %Y %I:%M:%S %p') INFO: Database connection established successfully" >> $LOG_FILE
sleep 1

echo "$(date '+%b %d, %Y %I:%M:%S %p') SEVERE: ERROR: API connection failed - timeout after 30 seconds" >> $LOG_FILE
sleep 1

echo "$(date '+%b %d, %Y %I:%M:%S %p') INFO: API response: {\"status\":\"success\",\"employee_id\":\"12345\",\"message\":\"Attendance recorded\"}" >> $LOG_FILE
sleep 1

echo "$(date '+%b %d, %Y %I:%M:%S %p') INFO: SUCCESS: Database updated successfully! Rows: 1" >> $LOG_FILE
sleep 1

echo "$(date '+%b %d, %Y %I:%M:%S %p') WARNING: Employee not found for biomatric code: 55555" >> $LOG_FILE
sleep 1

echo "$(date '+%b %d, %Y %I:%M:%S %p') INFO: Starting sync cycle... Total records to process: 15" >> $LOG_FILE
sleep 1

# Create sample log entries that WILL BE FILTERED OUT (noise)
echo "$(date '+%b %d, %Y %I:%M:%S %p') INFO: Configuration loaded successfully" >> $LOG_FILE
sleep 1

echo "$(date '+%b %d, %Y %I:%M:%S %p') INFO: School: Demo School" >> $LOG_FILE
sleep 1

echo "$(date '+%b %d, %Y %I:%M:%S %p') INFO: Database: localhost:1433" >> $LOG_FILE
sleep 1

echo "$(date '+%b %d, %Y %I:%M:%S %p') INFO: Sleeping for 60 seconds..." >> $LOG_FILE
sleep 1

echo "$(date '+%b %d, %Y %I:%M:%S %p') INFO: AttendanceSync started for Demo School" >> $LOG_FILE

echo ""
echo "✅ Sample logs generated!"
echo "📊 Summary of what was generated:"
echo "   🟢 WILL BE SHOWN (8 entries):"
echo "      - API requests and responses"
echo "      - Employee/biometric processing"
echo "      - Database operations"
echo "      - Errors and warnings"
echo "      - Success messages"
echo "      - Sync cycle information"
echo ""
echo "   🔴 WILL BE FILTERED OUT (5 entries):"
echo "      - Configuration loading messages"
echo "      - School/Database info"
echo "      - Sleeping notifications"
echo "      - General startup messages"
echo ""
echo "🖥️  Open the Desktop Application (Monitoring tab) to see the live filtering in action!"
echo "📁 Log file location: $(pwd)/$LOG_FILE"
