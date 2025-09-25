# AttendanceSync Windows Deployment Guide

## 🚀 **Step-by-Step Windows Deployment**

### **Prerequisites**
- Windows 7, 8, 10, or 11 (32-bit or 64-bit)
- Java 8 or higher installed
- Network connectivity to your database server
- Administrator rights (for Windows Service installation)

---

## **Option 1: Simple Desktop Deployment (Recommended)**

### **Step 1: Copy Files to Windows Machine**

1. **Download/Copy the entire `build/production` folder** to your Windows machine
2. **Choose a location** like:
   ```
   C:\AttendanceSync\
   ```
3. **Your folder structure should look like:**
   ```
   C:\AttendanceSync\
   ├── bin\
   │   ├── AttendanceSync.jar
   │   ├── AttendanceSync-GUI.bat
   │   ├── AttendanceSync-Server.bat     ← For continuous operation
   │   ├── attendancesync.bat
   │   └── Start-AttendanceSync.bat
   ├── config\
   │   ├── application.properties
   │   └── environment.env.template
   ├── lib\
   │   ├── httpclient-4.5.14.jar
   │   ├── httpcore-4.4.16.jar
   │   ├── json-simple-1.1.1.jar
   │   ├── mssql-jdbc-12.4.2.jre8.jar
   │   └── commons-logging-1.2.jar
   ├── logs\
   ├── QUICK-START.md
   └── DESKTOP-APP-GUIDE.md
   ```

### **Step 2: Check Java Installation**

Open **Command Prompt** and run:
```cmd
java -version
```

**If Java is not installed:**
1. Download Java 8 JRE from: https://www.oracle.com/java/technologies/javase-jre8-downloads.html
2. Install it with default settings
3. Verify installation: `java -version`

### **Step 3: Configure the Application**

1. **Navigate to:** `C:\AttendanceSync\config\`
2. **Edit `application.properties`** with Notepad:
   ```properties
   # Database Configuration
   db.host=your-database-server
   db.port=1433
   db.name=Realtime
   db.username=sa
   db.password=yourpassword
   
   # School Configuration
   school.code=yourschoolcode
   school.name=Your School Name
   
   # API Configuration
   api.primary.url=https://your-api-server.com/attendance
   api.fallback.url=https://backup-api-server.com/attendance
   api.timeout=30000
   
   # Application Settings
   app.sleep.interval=60000
   machine.ids=101,102,103,104,105,106
   app.debug.enabled=true
   ```

### **Step 4: Start the Application**

**For Continuous Server Operation (Your Problem Solution):**

1. **Navigate to:** `C:\AttendanceSync\bin\`
2. **Double-click:** `AttendanceSync-Server.bat`
3. **Result:** 
   - Application starts and auto-minimizes to system tray
   - Runs continuously without closing
   - Auto-restarts if it crashes
   - No "session ended" messages!

**For Interactive Desktop Use:**
1. **Double-click:** `AttendanceSync-GUI.bat`
2. Choose "Start Desktop Application"

---

## **Option 2: Windows Service Installation (Advanced)**

### **For Production Servers - Run as Windows Service**

### **Step 1: Install NSSM (Non-Sucking Service Manager)**

1. **Download NSSM** from: https://nssm.cc/download
2. **Extract** to `C:\nssm\`
3. **Add to PATH** or use full path

### **Step 2: Create Windows Service**

**Open Command Prompt as Administrator** and run:

```cmd
# Navigate to your AttendanceSync directory
cd C:\AttendanceSync\bin

# Install service using NSSM
C:\nssm\nssm.exe install AttendanceSync "AttendanceSync-Server.bat"

# Set service to restart automatically
C:\nssm\nssm.exe set AttendanceSync AppDirectory C:\AttendanceSync\bin
C:\nssm\nssm.exe set AttendanceSync DisplayName "AttendanceSync Biometric Service"
C:\nssm\nssm.exe set AttendanceSync Description "Biometric Attendance Synchronization Service"
C:\nssm\nssm.exe set AttendanceSync Start SERVICE_AUTO_START

# Start the service
net start AttendanceSync
```

### **Step 3: Manage the Service**

```cmd
# Start service
net start AttendanceSync

# Stop service
net stop AttendanceSync

# Check service status
sc query AttendanceSync

# Remove service (if needed)
C:\nssm\nssm.exe remove AttendanceSync confirm
```

---

## **Option 3: Scheduled Task (Alternative)**

### **For Automatic Startup without Service Installation**

1. **Open Task Scheduler** (`taskschd.msc`)
2. **Create Basic Task:**
   - Name: `AttendanceSync`
   - Trigger: `When the computer starts`
   - Action: `Start a program`
   - Program: `C:\AttendanceSync\bin\AttendanceSync-Server.bat`
   - Start in: `C:\AttendanceSync\bin`

3. **Configure Task:**
   - Run whether user is logged on or not
   - Run with highest privileges
   - Configure for Windows 10

---

## **🔧 Configuration Examples**

### **Common Database Configurations:**

**SQL Server (Local):**
```properties
db.host=localhost
db.port=1433
db.name=Realtime
db.username=sa
db.password=yourpassword
```

**SQL Server (Remote):**
```properties
db.host=192.168.1.100
db.port=1433
db.name=Realtime
db.username=attendanceuser
db.password=securepassword
```

**SQL Server (Named Instance):**
```properties
db.host=server\\SQLEXPRESS
db.port=1433
db.name=Realtime
db.username=sa
db.password=yourpassword
```

---

## **🚨 Troubleshooting Windows Deployment**

### **Problem: Java Not Found**
```cmd
'java' is not recognized as an internal or external command
```
**Solution:**
1. Install Java JRE 8 from Oracle
2. Or add Java to PATH: `set PATH=%PATH%;C:\Program Files\Java\jre1.8.0_XX\bin`

### **Problem: Database Connection Failed**
**Solution:**
1. Check database server is running
2. Verify connection string in `application.properties`
3. Test with: `telnet your-db-server 1433`
4. Check Windows Firewall settings

### **Problem: Application Closes After Few Hours**
**Solution:**
Use **Server Mode**: `AttendanceSync-Server.bat` instead of regular GUI launcher

### **Problem: Cannot Access System Tray**
**Solution:**
1. Check if system tray icons are hidden
2. Look for AttendanceSync icon in hidden icons area
3. Right-click system tray → Customize notifications

---

## **🎯 Quick Deployment Commands**

### **Copy these commands for quick setup:**

```cmd
# Create directory
mkdir C:\AttendanceSync

# Copy your files to C:\AttendanceSync\

# Test Java
java -version

# Navigate to application
cd C:\AttendanceSync\bin

# Start in server mode (continuous operation)
AttendanceSync-Server.bat

# Or start GUI for configuration
AttendanceSync-GUI.bat
```

---

## **📝 Post-Deployment Checklist**

### **Verify Deployment:**

1. ✅ **Java Working:** `java -version` shows Java 8+
2. ✅ **Files Copied:** All folders (bin, config, lib, logs) exist
3. ✅ **Configuration:** `application.properties` has correct settings
4. ✅ **Database Access:** Can connect to database server
5. ✅ **Application Starts:** Server mode runs without errors
6. ✅ **System Tray:** Application minimizes to tray
7. ✅ **Auto-Restart:** Application restarts if crashed
8. ✅ **Logs Working:** Live logs appear in GUI monitoring tab

### **Final Test:**

1. **Start Server Mode:** Run `AttendanceSync-Server.bat`
2. **Check System Tray:** Look for AttendanceSync icon
3. **Access GUI:** Right-click tray icon → Show Console
4. **Test Connections:** Use Control Panel → Test Database/API
5. **Monitor Logs:** Check Monitoring tab for live filtered logs
6. **Leave Running:** Application should run continuously

---

**🎉 Your AttendanceSync application is now deployed and will run continuously on Windows without the "session ended" problem!**

**For Support:** Check the live logs in the Monitoring tab for real-time troubleshooting.
