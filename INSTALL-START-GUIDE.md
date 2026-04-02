# AttendanceSync Installation and Startup Guide (Windows + Linux)

This document explains how to install, configure, and start the AttendanceSync biometric application on Windows and Linux.

## 1) What this project contains

AttendanceSync is a Java application that:
- reads biometric punch records from SQL Server
- sends them to configured API endpoints
- supports both console/service-style and GUI launch modes

The production package is created in `build/production/` and includes:
- `bin/AttendanceSync.jar`
- OS startup scripts (`.sh` and `.bat`)
- `config/application.properties`
- `lib/` dependency jars
- `logs/` output files

## 2) Prerequisites

Install these first on both operating systems:
- Java 8+ (`java` and `javac` should be available in PATH)
- Network access to:
  - your SQL Server
  - your API endpoint(s)

For building from source on Linux:
- `bash`
- `wget`

## 3) Build the production package

From project root:

```bash
cd /path/to/biometric
chmod +x build.sh
./build.sh
```

After success, use:
- `build/production/`

## 4) Configure the application

Edit:
- `build/production/config/application.properties`

Minimum required properties:
- `db.host`
- `db.port`
- `db.name`
- `db.username`
- `db.password`
- `school.code`
- `school.name`
- `api.primary.url`
- `machine.ids`

Example:

```properties
db.host=localhost
db.port=1433
db.name=Realtime
db.username=sa
db.password=your_password
school.code=your_school_code
school.name=Your School Name
api.primary.url=https://your-domain/v1/guests/StaffBiomatricPunch
api.fallback.url=
api.timeout=30000
app.sleep.interval=60000
app.debug.enabled=true
app.log.level=INFO
machine.ids=101,102,103,104,105,106
```

## 5) Start on Linux

Change to production folder:

```bash
cd build/production
chmod +x bin/*.sh
```

### Console/service mode (recommended for background sync)

```bash
./bin/attendancesync.sh start
```

Useful commands:

```bash
./bin/attendancesync.sh status
./bin/attendancesync.sh logs
./bin/attendancesync.sh stop
./bin/attendancesync.sh restart
```

### GUI mode

```bash
./bin/attendancesync-gui.sh
```

### Server GUI continuous mode

```bash
./bin/AttendanceSync-Server.sh
```

## 6) Start on Windows

Open Command Prompt and go to:
- `build\production\bin`

### Console/service mode

```cmd
attendancesync.bat start
```

Useful commands:

```cmd
attendancesync.bat status
attendancesync.bat logs
attendancesync.bat stop
attendancesync.bat restart
```

### GUI mode

Double-click:
- `AttendanceSync-GUI.bat`

or run:

```cmd
AttendanceSync-GUI.bat
```

### Quick-start launcher

Double-click:
- `Start-AttendanceSync.bat`

### Server GUI continuous mode

Run:

```cmd
AttendanceSync-Server.bat
```

If `AttendanceSync-Server.bat` is not available in your package, use `AttendanceSync-GUI.bat` or `attendancesync.bat start`.

## 7) Validate installation (both OS)

From production folder:

Linux:

```bash
./bin/attendancesync.sh test-connection
./bin/attendancesync.sh test-api
```

Windows:

```cmd
attendancesync.bat test-connection
attendancesync.bat test-api
```

Check logs:
- `build/production/logs/attendance-sync.log`
- `build/production/logs/application.out`

## 8) Common issues

- Java not found:
  - install JDK/JRE and add Java to PATH
- DB test fails:
  - verify SQL Server host/port/db/user/password
  - verify SQL Server allows remote access
- API test fails:
  - verify `api.primary.url`
  - verify outbound network/firewall access
- Unknown machine ID in logs:
  - ensure device IDs are included in `machine.ids`

## 9) Security note

- Do not keep real credentials in version-controlled files.
- Prefer environment-specific config files per deployment.
- Rotate credentials if they were committed previously.

