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
