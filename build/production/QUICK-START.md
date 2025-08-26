# AttendanceSync - Quick Start Guide

## 🚀 Getting Started (Windows)

### Option 1: Double-Click to Start (Recommended)
1. **Double-click** `Start-AttendanceSync.bat` to start the application immediately
2. The console window will show startup logs and keep running
3. Press Ctrl+C to stop the application when needed

### Option 2: Command Line Usage
1. Open Command Prompt
2. Navigate to the `bin` directory
3. Run: `attendancesync.bat` (starts automatically)
4. Or use specific commands: `attendancesync.bat start|stop|status|logs`

## 📝 Configuration

Before starting the application:

1. **Edit Configuration:**
   - Open `config\application.properties`
   - Update database settings (host, username, password)
   - Update school information
   - Update API URLs

2. **Test Configuration:**
   ```cmd
   attendancesync.bat test-connection
   attendancesync.bat test-api
   ```

## 📊 Monitoring

- **View Live Logs:** Double-click `Start-AttendanceSync.bat` or run `attendancesync.bat logs`
- **Check Status:** `attendancesync.bat status`
- **Log Files:** Located in `logs\attendance-sync.log`

## 🛠️ Available Commands

- **Start:** `attendancesync.bat start` or just `attendancesync.bat`
- **Stop:** `attendancesync.bat stop`
- **Restart:** `attendancesync.bat restart`
- **Status:** `attendancesync.bat status`
- **View Logs:** `attendancesync.bat logs`
- **Test DB:** `attendancesync.bat test-connection`
- **Test API:** `attendancesync.bat test-api`
- **Help:** `attendancesync.bat help`

## 🔧 Troubleshooting

### Java Version Error
If you see "UnsupportedClassVersionError", ensure you have Java 8 or higher:
```cmd
java -version
```

### Database Connection Issues
1. Check SQL Server is running
2. Verify connection details in `config\application.properties`
3. Test with: `attendancesync.bat test-connection`

### API Connection Issues
1. Check internet connectivity
2. Verify API URLs in configuration
3. Test with: `attendancesync.bat test-api`

## 📁 Directory Structure

```
bin/
├── Start-AttendanceSync.bat    ← Double-click to start
├── attendancesync.bat          ← Command-line interface
└── AttendanceSync.jar          ← Application JAR file

config/
├── application.properties      ← Main configuration
└── environment.env.template    ← Environment variables template

lib/                           ← Application dependencies
logs/                          ← Application logs
```

## 🎯 Quick Setup Checklist

- [ ] Install Java 8 or higher
- [ ] Configure database settings in `config\application.properties`
- [ ] Test database connection: `attendancesync.bat test-connection`
- [ ] Test API connection: `attendancesync.bat test-api`
- [ ] Start application: Double-click `Start-AttendanceSync.bat`
- [ ] Verify logs in `logs\attendance-sync.log`

## 📞 Support

For issues or questions, check the log files in the `logs` directory for detailed error messages.
