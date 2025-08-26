# AttendanceSync Quick Start Guide

## 🚀 Getting Started with AttendanceSync

### For Windows Users

**Option 1: Easy Start (Recommended)**
1. Navigate to the `bin` folder
2. Double-click `AttendanceSync-GUI.bat`
3. Choose "Start Desktop Application" from the launcher

**Option 2: Command Line**
```cmd
cd bin
attendancesync.bat start
```

### For Linux/Unix Users

**GUI Application:**
```bash
cd bin
./attendancesync-gui.sh
```

**Console Application:**
```bash
cd bin
./attendancesync.sh start
```

## 🔴 NEW: Live Log Monitoring

### What's New in Version 2.0?

**Real-Time Log Monitoring:**
- No more refreshing - logs appear instantly
- Smart filtering shows only what matters:
  - 🌐 API calls and responses
  - 👤 Employee/Biometric processing  
  - 🔌 Database operations
  - ❌ Errors and warnings
  - ✅ Success notifications

**Filtered Out (Noise Reduction):**
- Routine "sleeping" messages
- Configuration loading notifications
- Verbose debug information

### Quick Demo

1. **Start the GUI Application**
2. **Go to Monitoring Tab** - You'll see "🔴 Live log monitoring started"
3. **Start the Service** (Control Panel → ▶ Start Service)
4. **Watch Live Logs** - API calls, database operations, and errors appear in real-time with visual indicators

## ⚡ 5-Minute Setup

### Step 1: Configure Database (2 minutes)
1. Open **Configuration tab**
2. Set your database details:
   - Host: `your-db-server`
   - Port: `1433`
   - Database: `Realtime`
   - Username/Password: Your credentials

### Step 2: Test Connections (1 minute)
1. Go to **Control Panel**
2. Click **🔌 Test Database**
3. Click **🌐 Test API**
4. Verify both show success ✅

### Step 3: Start Service (1 minute)
1. Click **▶ Start Service**
2. Go to **Monitoring tab**
3. Watch live logs with filtered, relevant information

### Step 4: Monitor & Verify (1 minute)
1. Check **Statistics tab** for performance metrics
2. Monitor real-time sync operations in logs
3. Verify attendance records are being processed

## 🎯 Key Features at a Glance

| Feature | Location | What It Does |
|---------|----------|--------------|
| **Live Logs** | Monitoring Tab | Real-time filtered log viewing |
| **Service Control** | Control Panel | Start/Stop/Restart with status |
| **Quick Tests** | Control Panel | Test DB and API connectivity |
| **Smart Config** | Configuration Tab | Visual form-based settings |
| **System Tray** | Right-click tray icon | Background monitoring |
| **Statistics** | Statistics Tab | Performance and memory metrics |

## 🔧 Common Commands

### Windows Commands (Command Prompt)
```cmd
# Start service
attendancesync.bat start

# Stop service  
attendancesync.bat stop

# View status
attendancesync.bat status

# Test connections
attendancesync.bat test-connection
attendancesync.bat test-api

# Start GUI
AttendanceSync-GUI.bat
```

### Linux Commands (Terminal)
```bash
# Start service
./attendancesync.sh start

# Stop service
./attendancesync.sh stop

# View status  
./attendancesync.sh status

# Test connections
./attendancesync.sh test-connection
./attendancesync.sh test-api

# Start GUI
./attendancesync-gui.sh
```

## 🚨 Troubleshooting (2-Minute Fixes)

### Problem: Service Won't Start
**Quick Fix:**
1. Go to Control Panel → **🔌 Test Database**
2. If it fails, check Configuration tab settings
3. If database is OK, check **🌐 Test API**
4. Review **Monitoring tab** for specific error messages

### Problem: No Live Logs Appearing
**Quick Fix:**
1. Check "Live Monitoring" checkbox is enabled
2. Verify service is actually running (Control Panel shows "Running")
3. Click **🗑️ Clear Display** and restart service
4. New logs should appear with emoji indicators

### Problem: Too Many/Few Logs
**Quick Fix:**
- **Too many**: The filtering should reduce noise automatically
- **Too few**: Check if service is active and processing records
- **Save current logs** using 💾 Save Logs to review what's being captured

## 📁 File Locations

### Configuration Files
- `config/application.properties` - Main configuration
- `config/environment.env` - Environment variables (optional)

### Log Files  
- `logs/attendance-sync.log` - Application logs
- `logs/application.out` - Console output

### Executables
- `bin/AttendanceSync.jar` - Main application
- `bin/attendancesync.sh` - Linux script
- `bin/attendancesync.bat` - Windows script
- `bin/AttendanceSync-GUI.bat` - Windows GUI launcher

## 🔗 Integration Examples

### Running as Windows Service
```cmd
# Install as service (requires admin)
sc create AttendanceSync binPath="C:\path\to\bin\attendancesync.bat start"

# Start service
sc start AttendanceSync
```

### Linux Background Service
```bash
# Run in background
nohup ./attendancesync.sh start &

# Check if running
ps aux | grep AttendanceSync
```

### Monitoring with GUI
```bash
# Start GUI for monitoring running service
./attendancesync-gui.sh
# Service can be started/stopped from GUI even if started via command line
```

## 💡 Pro Tips

1. **Use System Tray**: Minimize GUI to tray for background monitoring
2. **Live Monitoring**: Perfect for real-time troubleshooting
3. **Filter Benefits**: Focus on API calls, errors, and biometric processing
4. **Quick Actions**: Use Control Panel buttons for fast testing
5. **Save Logs**: Export filtered logs for analysis and support

## ⏰ Maintenance Schedule

### Daily
- Check **Statistics tab** for success rates
- Review **Monitoring tab** for any error patterns

### Weekly  
- **💾 Save Logs** for historical analysis
- Verify **database connectivity** using test tools

### Monthly
- **Export configuration** for backup
- Review **memory usage** in Statistics tab
- **Restart service** for optimal performance

---

**Need Help?** 
- Check **Monitoring tab** for real-time error information
- Use **Tools tab** for system diagnostics  
- Review **DESKTOP-APP-GUIDE.md** for detailed documentation

**AttendanceSync v2.0** - *Now with Live Log Monitoring!*
