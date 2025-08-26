# AttendanceSync Desktop Application Guide

## Overview

The AttendanceSync Desktop Application provides a comprehensive graphical interface for managing biometric attendance synchronization services with advanced features including **live log monitoring** and **intelligent log filtering**.

## Key Features

### 🔴 Live Log Monitoring (NEW!)
- **Real-time log viewing** without manual refresh
- **Intelligent filtering** - shows only relevant logs:
  - API calls and responses
  - Biometric code processing
  - Database operations
  - Errors and warnings
  - Success/failure notifications
- **Automatic noise reduction** - filters out routine messages like "sleeping" and configuration loads
- **Visual indicators** with emoji prefixes for different log types:
  - 🌐 API calls and responses
  - 👤 Biometric/Employee processing
  - 🔌 Database operations
  - ✅ Success messages
  - ❌ Errors and failures
  - ⚠️ Warnings
  - 🔄 Sync operations

### 🎛️ Service Control Panel
- Start/Stop/Restart AttendanceSync service
- Real-time service status monitoring
- Uptime tracking
- Quick access to common operations

### ⚙️ Configuration Management
- Visual form-based configuration editing
- Real-time validation
- Import/Export configuration files
- Reset to defaults option

### 📊 System Monitoring
- Live performance statistics
- Memory usage tracking
- Success/failure rate monitoring
- System information display

### 🔧 Diagnostic Tools
- Database connection testing
- API connectivity verification
- System information display
- Log file management

### 🔄 System Tray Integration
- Minimize to system tray
- Background service monitoring
- Quick access from system tray
- Notifications for important events

## Getting Started

### Launching the Desktop Application

**Windows:**
```bash
# GUI Launcher (Recommended)
Double-click: AttendanceSync-GUI.bat

# Or run directly
java -cp "AttendanceSync.jar;../lib/*" com.attendance.sync.gui.AttendanceSyncLauncher
```

**Linux/Unix:**
```bash
# GUI Launcher
./attendancesync-gui.sh

# Or run directly
java -cp "AttendanceSync.jar:../lib/*" com.attendance.sync.gui.AttendanceSyncLauncher
```

### First Time Setup

1. **Launch the Application**
   - The application will start with the Control Panel tab active
   - System tray integration will be automatically enabled (if supported)

2. **Configure Settings** (Configuration tab)
   - Set database connection details
   - Configure school information
   - Set API endpoints
   - Adjust sync intervals and machine IDs

3. **Test Connections** (Quick Actions)
   - Use "🔌 Test Database" to verify database connectivity
   - Use "🌐 Test API" to verify API endpoints

4. **Start the Service** (Control Panel)
   - Click "▶ Start Service" to begin synchronization
   - Monitor the status in real-time

## Live Log Monitoring

### Understanding the Log Display

The **Monitoring tab** provides real-time log viewing with intelligent filtering:

**What's Shown:**
- ✅ **API Operations**: All API calls, requests, and responses
- 👤 **Biometric Processing**: Employee card processing, biometric codes
- 🔌 **Database Operations**: Connection status, record updates
- ❌ **Errors & Warnings**: All error conditions and warnings
- 🔄 **Sync Operations**: Sync cycle information, records processed

**What's Filtered Out:**
- Routine "sleeping" messages
- Configuration loading messages
- Verbose debugging information
- Timestamp-only entries

### Log Types and Visual Indicators

| Icon | Type | Description |
|------|------|-------------|
| 🌐 | API | API calls, requests, responses |
| 👤 | Employee | Biometric processing, employee records |
| 🔌 | Database | Database connections, operations |
| ✅ | Success | Successful operations |
| ❌ | Error | Error conditions, failures |
| ⚠️ | Warning | Warning conditions |
| 🔄 | Sync | Sync cycles, record processing |
| ℹ️ | Info | General information |

### Log Control Options

- **🗑️ Clear Display**: Clears the current log display (doesn't affect log files)
- **💾 Save Logs**: Save current filtered logs to a file
- **Live Monitoring Checkbox**: Enable/disable real-time monitoring
- **Filter Information**: Shows current filtering rules

## Tab Descriptions

### 🎛️ Control Panel
- **Service Status**: Real-time status display with uptime
- **Control Buttons**: Start, Stop, Restart service
- **Quick Actions**: Database test, API test, configuration shortcuts
- **Status Indicators**: Visual progress bars and status colors

### ⚙️ Configuration
- **Database Settings**: Host, port, database name, credentials
- **School Information**: School code and name
- **API Configuration**: Primary and fallback URLs, timeout settings
- **Application Settings**: Sleep intervals, machine IDs, debug mode
- **Action Buttons**: Load, Save, Reset configuration

### 📊 Monitoring
- **Live Log Display**: Real-time filtered log viewing
- **Auto-scroll**: Automatically scrolls to show latest entries
- **Console-style Display**: Dark background with colored text
- **Log Controls**: Clear, save, and monitoring toggle options

### 📈 Statistics
- **Service Metrics**: Runtime statistics, sync counts
- **Performance Data**: Success rates, memory usage
- **System Information**: Java version, memory statistics
- **Real-time Updates**: Statistics refresh every second

### 🔧 Tools
- **System Information**: Detailed system and Java environment info
- **File Operations**: Open logs folder, configuration management
- **Import/Export**: Configuration backup and restore
- **Diagnostic Tools**: Additional system utilities

## Troubleshooting

### Live Log Monitoring Issues

**Problem**: Live monitoring not showing new logs
**Solution**:
- Check that the logs directory exists
- Verify log file permissions
- Toggle live monitoring off and on
- Check if the service is actually running and generating logs

**Problem**: Too many or too few logs showing
**Solution**:
- The filtering is designed to show only relevant logs
- If you need to see all logs, use the traditional log files
- Save current display logs to review filtered content

### Service Control Issues

**Problem**: Service won't start
**Solution**:
- Check database configuration
- Verify API endpoints
- Review error messages in the log display
- Use diagnostic tools to test connections

### Configuration Issues

**Problem**: Settings not saving
**Solution**:
- Check file permissions on config directory
- Verify all required fields are filled
- Use "Reset" and reconfigure if needed

## Best Practices

### Service Management
1. **Always test connections** before starting the service
2. **Monitor logs regularly** using the live monitoring feature
3. **Use system tray** for background operation
4. **Save configurations** before making major changes

### Log Management
1. **Live monitoring** is ideal for real-time troubleshooting
2. **Save filtered logs** for support and analysis
3. **Clear display regularly** to focus on current issues
4. **Use the filtering** to focus on relevant information

### Performance Optimization
1. **Monitor memory usage** in the Statistics tab
2. **Adjust sync intervals** based on your requirements
3. **Use appropriate timeouts** for API calls
4. **Regular service restarts** for long-running operations

## Keyboard Shortcuts

- **Ctrl+Tab**: Switch between tabs
- **Ctrl+S**: Save configuration (when in Configuration tab)
- **Ctrl+R**: Restart service (when service is running)
- **F5**: Refresh statistics
- **Esc**: Minimize to system tray

## System Requirements

- **Java 8 or higher** (Java 8 recommended for maximum compatibility)
- **Minimum 512MB RAM** (1GB recommended)
- **Network connectivity** for database and API access
- **Windows 7+** or **Linux with GUI support**
- **Screen resolution**: 1024x768 minimum (1280x720 recommended)

## Support and Troubleshooting

For additional support:
1. Check the live log monitoring for real-time error information
2. Use diagnostic tools in the Tools tab
3. Save and review filtered logs
4. Export configuration for backup/analysis
5. Review system information for environment details

---

**AttendanceSync Desktop Application v2.0**  
*Enhanced with Live Log Monitoring and Intelligent Filtering*
