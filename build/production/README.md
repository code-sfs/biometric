# AttendanceSync Production Build

## Quick Start

1. **Configure the application:**
   ```bash
   cd config
   cp environment.env.template environment.env
   # Edit environment.env with your settings
   ```

2. **Test the configuration:**
   ```bash
   ./bin/attendancesync.sh test-connection
   ./bin/attendancesync.sh test-api
   ```

3. **Start the application:**
   ```bash
   ./bin/attendancesync.sh start
   ```

4. **Monitor logs:**
   ```bash
   ./bin/attendancesync.sh logs
   ```

## Available Commands

- `./bin/attendancesync.sh start` - Start the service
- `./bin/attendancesync.sh stop` - Stop the service
- `./bin/attendancesync.sh restart` - Restart the service
- `./bin/attendancesync.sh status` - Check service status
- `./bin/attendancesync.sh logs` - View real-time logs
- `./bin/attendancesync.sh test-connection` - Test database connection
- `./bin/attendancesync.sh test-api` - Test API connectivity

## Directory Structure

- `bin/` - Executable files (JAR and scripts)
- `lib/` - Dependencies
- `config/` - Configuration files
- `logs/` - Application logs

## Configuration

Edit `config/application.properties` or use environment variables in `config/environment.env`

Built on: Thu Apr  2 10:25:13 AM IST 2026
Version: 2.0
