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
