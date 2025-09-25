package com.attendance.sync.gui;

import com.attendance.sync.AttendanceSync;
import com.attendance.sync.Constants;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

/**
 * AttendanceSync Desktop GUI Application
 * 
 * Provides a comprehensive desktop interface for managing the AttendanceSync service
 * with real-time monitoring, configuration management, and system tray integration.
 */
public class AttendanceSyncGUI extends JFrame {
    
    // Core components
    private AttendanceSync attendanceSync;
    private Thread syncThread;
    private Timer statusTimer;
    private SystemTray systemTray;
    private TrayIcon trayIcon;
    
    // Live log monitoring
    private WatchService logWatchService;
    private Thread logWatchThread;
    private volatile boolean logMonitoringActive = false;
    private long lastLogFileSize = 0;
    
    // GUI Components
    private JPanel mainPanel;
    private JTabbedPane tabbedPane;
    
    // Control Panel
    private JButton startButton;
    private JButton stopButton;
    private JButton restartButton;
    private JLabel statusLabel;
    private JLabel uptimeLabel;
    private JProgressBar statusProgress;
    
    // Configuration Panel
    private JTextField dbHostField;
    private JTextField dbPortField;
    private JTextField dbNameField;
    private JTextField dbUsernameField;
    private JPasswordField dbPasswordField;
    private JTextField schoolCodeField;
    private JTextField schoolNameField;
    private JTextField apiPrimaryField;
    private JTextField apiFallbackField;
    private JTextField apiTimeoutField;
    private JTextField sleepIntervalField;
    private JTextField machineIdsField;
    private JCheckBox debugEnabledBox;
    
    // Monitoring Panel
    private JTextArea logTextArea;
    private JScrollPane logScrollPane;
    private JTable statsTable;
    private DefaultTableModel statsTableModel;
    
    // Status variables
    private boolean isServiceRunning = false;
    private long serviceStartTime = 0;
    private int syncCount = 0;
    private int successCount = 0;
    private int errorCount = 0;
    private boolean isServerMode = false;
    
    // Colors and fonts
    private static final Color PRIMARY_COLOR = new Color(52, 152, 219);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private static final Color ERROR_COLOR = new Color(231, 76, 60);
    private static final Color WARNING_COLOR = new Color(241, 196, 15);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font NORMAL_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    
    public AttendanceSyncGUI() {
        this(false); // Default: not server mode
    }
    
    public AttendanceSyncGUI(boolean serverMode) {
        this.isServerMode = serverMode;
        
        // Set the application name for the taskbar and window manager
        System.setProperty("java.awt.Window.locationByPlatform", "true");
        
        // For Windows - set taskbar grouping
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            System.setProperty("swing.useSystemUIFeedback", "true");
            // This helps with Windows taskbar identification
            System.setProperty("awt.useSystemAAFontSettings", "on");
        }
        
        // For Linux window managers
        if (System.getProperty("os.name").toLowerCase().contains("linux")) {
            // Set WM_CLASS property for Linux window managers
            try {
                Class<?> xToolkit = Class.forName("sun.awt.X11.XToolkit");
                java.lang.reflect.Field awtAppClassNameField = xToolkit.getDeclaredField("awtAppClassName");
                awtAppClassNameField.setAccessible(true);
                awtAppClassNameField.set(null, "AttendanceSync");
            } catch (Exception e) {
                // Ignore if not available
            }
        }
        
        initializeGUI();
        loadConfiguration();
        setupSystemTray();
        startStatusTimer();
        startLiveLogMonitoring();
        
        if (serverMode) {
            appendLog("🖥️ Started in Server Mode - Enhanced protection enabled");
            appendLog("📌 GUI will remain visible for monitoring and control");
        }
    }
    
    private void initializeGUI() {
        // Set up the main window
        setTitle("AttendanceSync - Live Monitor");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // Set application name for taskbar - try multiple approaches
        setName("AttendanceSync");
        
        // Set application icon
        try {
            setIconImages(AppIcon.createIconList());
        } catch (Exception e) {
            System.err.println("Could not load application icon: " + e.getMessage());
        }
        
        // Handle window closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (isServerMode) {
                    // Server mode - show enhanced protection dialog
                    int option = JOptionPane.showConfirmDialog(
                        AttendanceSyncGUI.this,
                        "⚠️ SERVER MODE - Application is running continuously!\n\n" +
                        "Closing this window will stop the attendance synchronization service.\n" +
                        "This may cause data loss and interrupt biometric sync operations.\n\n" +
                        "Do you want to:\n\n" +
                        "• Minimize to system tray (Keep running - RECOMMENDED)\n" +
                        "• Force stop and exit (⚠️ WARNING: May cause data loss)\n" +
                        "• Cancel (Keep window open)",
                        "⚠️ Server Mode Protection",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE
                    );
                    
                    if (option == JOptionPane.YES_OPTION) {
                        minimizeToTray();
                    } else if (option == JOptionPane.NO_OPTION) {
                        // Require double confirmation for server mode exit
                        int confirmExit = JOptionPane.showConfirmDialog(
                            AttendanceSyncGUI.this,
                            "🛑 FINAL WARNING\n\n" +
                            "Are you absolutely sure you want to stop the server?\n" +
                            "This will interrupt all ongoing sync operations.\n\n" +
                            "Type 'YES' to confirm shutdown:",
                            "Confirm Server Shutdown",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.ERROR_MESSAGE
                        );
                        
                        if (confirmExit == JOptionPane.YES_OPTION) {
                            String userInput = JOptionPane.showInputDialog(
                                AttendanceSyncGUI.this,
                                "Type 'YES' to confirm server shutdown:",
                                "Confirm Shutdown",
                                JOptionPane.WARNING_MESSAGE
                            );
                            
                            if ("YES".equals(userInput)) {
                                stopService();
                                stopLiveLogMonitoring();
                                System.out.println("🛑 Server mode shutdown confirmed by user");
                                System.exit(0);
                            }
                        }
                    }
                } else {
                    // Normal mode - standard dialog
                    if (isServiceRunning) {
                        int option = JOptionPane.showConfirmDialog(
                            AttendanceSyncGUI.this,
                            "AttendanceSync service is running. Do you want to:\n\n" +
                            "• Minimize to system tray (Recommended)\n" +
                            "• Stop service and exit\n" +
                            "• Cancel",
                            "Service Running",
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                        );
                        
                        if (option == JOptionPane.YES_OPTION) {
                            minimizeToTray();
                        } else if (option == JOptionPane.NO_OPTION) {
                            stopService();
                            stopLiveLogMonitoring();
                            System.exit(0);
                        }
                    } else {
                        stopLiveLogMonitoring();
                        System.exit(0);
                    }
                }
            }
        });
        
        createMainPanel();
        add(mainPanel);
    }
    
    private void createMainPanel() {
        mainPanel = new JPanel(new BorderLayout());
        
        // Create header panel
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(NORMAL_FONT);
        
        // Add tabs
        tabbedPane.addTab("🎛️ Control Panel", createControlPanel());
        tabbedPane.addTab("⚙️ Configuration", createConfigurationPanel());
        tabbedPane.addTab("📊 Monitoring", createMonitoringPanel());
        tabbedPane.addTab("📈 Statistics", createStatisticsPanel());
        tabbedPane.addTab("🔧 Tools", createToolsPanel());
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // Create status bar
        JPanel statusBar = createStatusBar();
        mainPanel.add(statusBar, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("AttendanceSync Management Console");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel versionLabel = new JLabel("Version 2.0 - Desktop Edition");
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        versionLabel.setForeground(Color.WHITE);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(versionLabel, BorderLayout.SOUTH);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        
        return headerPanel;
    }
    
    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new BorderLayout(10, 10));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Service control section
        JPanel servicePanel = new JPanel(new GridBagLayout());
        servicePanel.setBorder(new TitledBorder("Service Control"));
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Status display
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        statusLabel = new JLabel("Service Status: Stopped");
        statusLabel.setFont(HEADER_FONT);
        statusLabel.setForeground(ERROR_COLOR);
        servicePanel.add(statusLabel, gbc);
        
        gbc.gridy++; gbc.gridwidth = 3;
        statusProgress = new JProgressBar();
        statusProgress.setStringPainted(true);
        statusProgress.setString("Service Stopped");
        statusProgress.setForeground(ERROR_COLOR);
        servicePanel.add(statusProgress, gbc);
        
        gbc.gridy++; gbc.gridwidth = 3;
        uptimeLabel = new JLabel("Uptime: --");
        uptimeLabel.setFont(NORMAL_FONT);
        servicePanel.add(uptimeLabel, gbc);
        
        // Control buttons
        gbc.gridy++; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        
        startButton = createStyledButton("▶ Start Service", SUCCESS_COLOR);
        startButton.addActionListener(e -> startService());
        servicePanel.add(startButton, gbc);
        
        gbc.gridx++;
        stopButton = createStyledButton("⏹ Stop Service", ERROR_COLOR);
        stopButton.addActionListener(e -> stopService());
        stopButton.setEnabled(false);
        servicePanel.add(stopButton, gbc);
        
        gbc.gridx++;
        restartButton = createStyledButton("🔄 Restart Service", WARNING_COLOR);
        restartButton.addActionListener(e -> restartService());
        restartButton.setEnabled(false);
        servicePanel.add(restartButton, gbc);
        
        controlPanel.add(servicePanel, BorderLayout.NORTH);
        
        // Quick actions section
        JPanel quickActionsPanel = createQuickActionsPanel();
        controlPanel.add(quickActionsPanel, BorderLayout.CENTER);
        
        return controlPanel;
    }
    
    private JPanel createQuickActionsPanel() {
        JPanel quickPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        quickPanel.setBorder(new TitledBorder("Quick Actions"));
        
        JButton testDbButton = createStyledButton("🔌 Test Database", PRIMARY_COLOR);
        testDbButton.addActionListener(e -> testDatabaseConnection());
        quickPanel.add(testDbButton);
        
        JButton testApiButton = createStyledButton("🌐 Test API", PRIMARY_COLOR);
        testApiButton.addActionListener(e -> testApiConnection());
        quickPanel.add(testApiButton);
        
        JButton viewLogsButton = createStyledButton("📝 View Logs", PRIMARY_COLOR);
        viewLogsButton.addActionListener(e -> tabbedPane.setSelectedIndex(2));
        quickPanel.add(viewLogsButton);
        
        JButton openConfigButton = createStyledButton("⚙️ Edit Config", PRIMARY_COLOR);
        openConfigButton.addActionListener(e -> tabbedPane.setSelectedIndex(1));
        quickPanel.add(openConfigButton);
        
        JButton saveConfigButton = createStyledButton("💾 Save Config", SUCCESS_COLOR);
        saveConfigButton.addActionListener(e -> saveConfiguration());
        quickPanel.add(saveConfigButton);
        
        JButton aboutButton = createStyledButton("ℹ️ About", PRIMARY_COLOR);
        aboutButton.addActionListener(e -> showAboutDialog());
        quickPanel.add(aboutButton);
        
        return quickPanel;
    }
    
    private JPanel createConfigurationPanel() {
        JPanel configPanel = new JPanel(new BorderLayout());
        configPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create scroll pane for configuration form
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Database Configuration
        addSectionHeader(formPanel, gbc, "Database Configuration");
        
        dbHostField = addConfigField(formPanel, gbc, "Host:", "localhost");
        dbPortField = addConfigField(formPanel, gbc, "Port:", "1433");
        dbNameField = addConfigField(formPanel, gbc, "Database Name:", "Realtime");
        dbUsernameField = addConfigField(formPanel, gbc, "Username:", "sa");
        dbPasswordField = addPasswordField(formPanel, gbc, "Password:");
        
        // School Configuration
        addSectionHeader(formPanel, gbc, "School Configuration");
        
        schoolCodeField = addConfigField(formPanel, gbc, "School Code:", "demo");
        schoolNameField = addConfigField(formPanel, gbc, "School Name:", "Demo School");
        
        // API Configuration
        addSectionHeader(formPanel, gbc, "API Configuration");
        
        apiPrimaryField = addConfigField(formPanel, gbc, "Primary API URL:", "");
        apiFallbackField = addConfigField(formPanel, gbc, "Fallback API URL:", "");
        apiTimeoutField = addConfigField(formPanel, gbc, "Timeout (ms):", "30000");
        
        // Application Configuration
        addSectionHeader(formPanel, gbc, "Application Configuration");
        
        sleepIntervalField = addConfigField(formPanel, gbc, "Sleep Interval (ms):", "60000");
        machineIdsField = addConfigField(formPanel, gbc, "Machine IDs:", "101,102,103,104,105,106");
        
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        debugEnabledBox = new JCheckBox("Enable Debug Mode");
        debugEnabledBox.setFont(NORMAL_FONT);
        formPanel.add(debugEnabledBox, gbc);
        
        JScrollPane configScrollPane = new JScrollPane(formPanel);
        configScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        configPanel.add(configScrollPane, BorderLayout.CENTER);
        
        // Configuration buttons
        JPanel configButtonPanel = new JPanel(new FlowLayout());
        
        JButton loadButton = createStyledButton("📂 Load Config", PRIMARY_COLOR);
        loadButton.addActionListener(e -> loadConfiguration());
        configButtonPanel.add(loadButton);
        
        JButton saveButton = createStyledButton("💾 Save Config", SUCCESS_COLOR);
        saveButton.addActionListener(e -> saveConfiguration());
        configButtonPanel.add(saveButton);
        
        JButton resetButton = createStyledButton("🔄 Reset", WARNING_COLOR);
        resetButton.addActionListener(e -> resetConfiguration());
        configButtonPanel.add(resetButton);
        
        configPanel.add(configButtonPanel, BorderLayout.SOUTH);
        
        return configPanel;
    }
    
    private JPanel createMonitoringPanel() {
        JPanel monitorPanel = new JPanel(new BorderLayout());
        monitorPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Log viewer
        logTextArea = new JTextArea();
        logTextArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        logTextArea.setEditable(false);
        logTextArea.setBackground(Color.BLACK);
        logTextArea.setForeground(Color.GREEN);
        
        // Auto-scroll to bottom
        DefaultCaret caret = (DefaultCaret) logTextArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        
        logScrollPane = new JScrollPane(logTextArea);
        logScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        logScrollPane.setBorder(new TitledBorder("Live Application Logs"));
        
        monitorPanel.add(logScrollPane, BorderLayout.CENTER);
        
        // Log control panel
        JPanel logControlPanel = new JPanel(new FlowLayout());
        
        JButton clearLogsButton = createStyledButton("🗑️ Clear Display", WARNING_COLOR);
        clearLogsButton.addActionListener(e -> logTextArea.setText(""));
        logControlPanel.add(clearLogsButton);
        
        JButton saveLogsButton = createStyledButton("💾 Save Logs", PRIMARY_COLOR);
        saveLogsButton.addActionListener(e -> saveLogs());
        logControlPanel.add(saveLogsButton);
        
        JCheckBox liveLogsCheckBox = new JCheckBox("Live Monitoring", true);
        liveLogsCheckBox.setFont(NORMAL_FONT);
        liveLogsCheckBox.addActionListener(e -> {
            if (liveLogsCheckBox.isSelected()) {
                startLiveLogMonitoring();
            } else {
                stopLiveLogMonitoring();
            }
        });
        logControlPanel.add(liveLogsCheckBox);
        
        JLabel filterLabel = new JLabel("| Filters: API calls, Biometric codes, Errors only");
        filterLabel.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        filterLabel.setForeground(Color.GRAY);
        logControlPanel.add(filterLabel);
        
        monitorPanel.add(logControlPanel, BorderLayout.SOUTH);
        
        return monitorPanel;
    }
    
    private JPanel createStatisticsPanel() {
        JPanel statsPanel = new JPanel(new BorderLayout());
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Statistics table
        String[] columnNames = {"Metric", "Value", "Description"};
        statsTableModel = new DefaultTableModel(columnNames, 0);
        statsTable = new JTable(statsTableModel);
        statsTable.setFont(NORMAL_FONT);
        statsTable.getTableHeader().setFont(HEADER_FONT);
        
        JScrollPane statsScrollPane = new JScrollPane(statsTable);
        statsScrollPane.setBorder(new TitledBorder("Performance Statistics"));
        
        statsPanel.add(statsScrollPane, BorderLayout.CENTER);
        
        // Update statistics
        updateStatistics();
        
        return statsPanel;
    }
    
    private JPanel createToolsPanel() {
        JPanel toolsPanel = new JPanel(new GridBagLayout());
        toolsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // System information
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel systemInfoLabel = new JLabel("System Information");
        systemInfoLabel.setFont(HEADER_FONT);
        toolsPanel.add(systemInfoLabel, gbc);
        
        gbc.gridy++; gbc.gridwidth = 1;
        JButton systemInfoButton = createStyledButton("💻 View System Info", PRIMARY_COLOR);
        systemInfoButton.addActionListener(e -> showSystemInfo());
        toolsPanel.add(systemInfoButton, gbc);
        
        gbc.gridx++;
        JButton logsLocationButton = createStyledButton("📁 Open Logs Folder", PRIMARY_COLOR);
        logsLocationButton.addActionListener(e -> openLogsFolder());
        toolsPanel.add(logsLocationButton, gbc);
        
        // Configuration tools
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        JLabel configToolsLabel = new JLabel("Configuration Tools");
        configToolsLabel.setFont(HEADER_FONT);
        toolsPanel.add(configToolsLabel, gbc);
        
        gbc.gridy++; gbc.gridwidth = 1;
        JButton exportConfigButton = createStyledButton("📤 Export Config", PRIMARY_COLOR);
        exportConfigButton.addActionListener(e -> exportConfiguration());
        toolsPanel.add(exportConfigButton, gbc);
        
        gbc.gridx++;
        JButton importConfigButton = createStyledButton("📥 Import Config", PRIMARY_COLOR);
        importConfigButton.addActionListener(e -> importConfiguration());
        toolsPanel.add(importConfigButton, gbc);
        
        return toolsPanel;
    }
    
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
        statusBar.setPreferredSize(new Dimension(0, 25));
        
        JLabel statusBarLabel = new JLabel("Ready");
        statusBarLabel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        statusBar.add(statusBarLabel, BorderLayout.WEST);
        
        JLabel timeLabel = new JLabel(new SimpleDateFormat("HH:mm:ss").format(new Date()));
        timeLabel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        statusBar.add(timeLabel, BorderLayout.EAST);
        
        return statusBar;
    }
    
    // Service control methods
    private void startService() {
        if (isServiceRunning) {
            JOptionPane.showMessageDialog(this, "Service is already running!", "Service Status", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        try {
            attendanceSync = new AttendanceSync();
            syncThread = new Thread(attendanceSync);
            syncThread.start();
            
            isServiceRunning = true;
            serviceStartTime = System.currentTimeMillis();
            
            updateControlButtons();
            updateStatusDisplay("Running", SUCCESS_COLOR);
            appendLog("✅ AttendanceSync service started successfully");
            
            if (systemTray != null) {
                trayIcon.displayMessage("AttendanceSync", "Service started successfully", TrayIcon.MessageType.INFO);
            }
            
        } catch (Exception e) {
            appendLog("❌ Failed to start service: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Failed to start service: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void stopService() {
        if (!isServiceRunning) {
            JOptionPane.showMessageDialog(this, "Service is not running!", "Service Status", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        try {
            if (syncThread != null && syncThread.isAlive()) {
                syncThread.interrupt();
            }
            
            isServiceRunning = false;
            serviceStartTime = 0;
            
            updateControlButtons();
            updateStatusDisplay("Stopped", ERROR_COLOR);
            appendLog("⏹ AttendanceSync service stopped");
            
            if (systemTray != null) {
                trayIcon.displayMessage("AttendanceSync", "Service stopped", TrayIcon.MessageType.WARNING);
            }
            
        } catch (Exception e) {
            appendLog("❌ Error stopping service: " + e.getMessage());
        }
    }
    
    private void restartService() {
        appendLog("🔄 Restarting AttendanceSync service...");
        stopService();
        
        // Wait a moment before restarting
        Timer restartTimer = new Timer();
        restartTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> startService());
            }
        }, 2000);
    }
    
    // Utility methods for GUI components
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(NORMAL_FONT);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    private void addSectionHeader(JPanel panel, GridBagConstraints gbc, String text) {
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        JLabel headerLabel = new JLabel(text);
        headerLabel.setFont(HEADER_FONT);
        headerLabel.setForeground(PRIMARY_COLOR);
        panel.add(headerLabel, gbc);
        gbc.gridy++;
    }
    
    private JTextField addConfigField(JPanel panel, GridBagConstraints gbc, String labelText, String defaultValue) {
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 1;
        JLabel label = new JLabel(labelText);
        label.setFont(NORMAL_FONT);
        panel.add(label, gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JTextField field = new JTextField(defaultValue, 20);
        field.setFont(NORMAL_FONT);
        panel.add(field, gbc);
        gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE;
        
        return field;
    }
    
    private JPasswordField addPasswordField(JPanel panel, GridBagConstraints gbc, String labelText) {
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 1;
        JLabel label = new JLabel(labelText);
        label.setFont(NORMAL_FONT);
        panel.add(label, gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JPasswordField field = new JPasswordField(20);
        field.setFont(NORMAL_FONT);
        panel.add(field, gbc);
        gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE;
        
        return field;
    }
    
    private void updateControlButtons() {
        startButton.setEnabled(!isServiceRunning);
        stopButton.setEnabled(isServiceRunning);
        restartButton.setEnabled(isServiceRunning);
    }
    
    private void updateStatusDisplay(String status, Color color) {
        statusLabel.setText("Service Status: " + status);
        statusLabel.setForeground(color);
        statusProgress.setString("Service " + status);
        statusProgress.setForeground(color);
        statusProgress.setValue(isServiceRunning ? 100 : 0);
    }
    
    private void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
            logTextArea.append("[" + timestamp + "] " + message + "\n");
            logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
        });
    }
    
    // Configuration methods
    private void loadConfiguration() {
        try {
            Properties config = new Properties();
            FileInputStream configFile = new FileInputStream(Constants.DEFAULT_CONFIG_PATH);
            config.load(configFile);
            configFile.close();
            
            // Load values into form fields
            dbHostField.setText(config.getProperty("db.host", "localhost"));
            dbPortField.setText(config.getProperty("db.port", "1433"));
            dbNameField.setText(config.getProperty("db.name", "Realtime"));
            dbUsernameField.setText(config.getProperty("db.username", "sa"));
            dbPasswordField.setText(config.getProperty("db.password", ""));
            
            schoolCodeField.setText(config.getProperty("school.code", "demo"));
            schoolNameField.setText(config.getProperty("school.name", "Demo School"));
            
            apiPrimaryField.setText(config.getProperty("api.primary.url", ""));
            apiFallbackField.setText(config.getProperty("api.fallback.url", ""));
            apiTimeoutField.setText(config.getProperty("api.timeout", "30000"));
            
            sleepIntervalField.setText(config.getProperty("app.sleep.interval", "60000"));
            machineIdsField.setText(config.getProperty("machine.ids", "101,102,103,104,105,106"));
            debugEnabledBox.setSelected(Boolean.parseBoolean(config.getProperty("app.debug.enabled", "true")));
            
            appendLog("✅ Configuration loaded successfully");
            
        } catch (IOException e) {
            appendLog("❌ Failed to load configuration: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Failed to load configuration: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void saveConfiguration() {
        try {
            Properties config = new Properties();
            
            // Database settings
            config.setProperty("db.host", dbHostField.getText());
            config.setProperty("db.port", dbPortField.getText());
            config.setProperty("db.name", dbNameField.getText());
            config.setProperty("db.username", dbUsernameField.getText());
            config.setProperty("db.password", new String(dbPasswordField.getPassword()));
            
            // School settings
            config.setProperty("school.code", schoolCodeField.getText());
            config.setProperty("school.name", schoolNameField.getText());
            
            // API settings
            config.setProperty("api.primary.url", apiPrimaryField.getText());
            config.setProperty("api.fallback.url", apiFallbackField.getText());
            config.setProperty("api.timeout", apiTimeoutField.getText());
            
            // Application settings
            config.setProperty("app.sleep.interval", sleepIntervalField.getText());
            config.setProperty("app.debug.enabled", String.valueOf(debugEnabledBox.isSelected()));
            config.setProperty("machine.ids", machineIdsField.getText());
            
            // Save to file
            FileOutputStream configFile = new FileOutputStream(Constants.DEFAULT_CONFIG_PATH);
            config.store(configFile, "AttendanceSync Configuration - Saved from GUI");
            configFile.close();
            
            appendLog("✅ Configuration saved successfully");
            JOptionPane.showMessageDialog(this, "Configuration saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (IOException e) {
            appendLog("❌ Failed to save configuration: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Failed to save configuration: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void resetConfiguration() {
        int option = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to reset all configuration to default values?", 
            "Reset Configuration", 
            JOptionPane.YES_NO_OPTION);
            
        if (option == JOptionPane.YES_OPTION) {
            // Reset to default values
            dbHostField.setText("localhost");
            dbPortField.setText("1433");
            dbNameField.setText("Realtime");
            dbUsernameField.setText("sa");
            dbPasswordField.setText("");
            
            schoolCodeField.setText("demo");
            schoolNameField.setText("Demo School");
            
            apiPrimaryField.setText("");
            apiFallbackField.setText("");
            apiTimeoutField.setText("30000");
            
            sleepIntervalField.setText("60000");
            machineIdsField.setText("101,102,103,104,105,106");
            debugEnabledBox.setSelected(true);
            
            appendLog("🔄 Configuration reset to defaults");
        }
    }
    
    // Test methods
    private void testDatabaseConnection() {
        appendLog("🔌 Testing database connection...");
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    if (attendanceSync == null) {
                        attendanceSync = new AttendanceSync();
                    }
                    Connection con = attendanceSync.getConnection();
                    if (con != null) {
                        con.close();
                        return true;
                    }
                    return false;
                } catch (Exception e) {
                    appendLog("❌ Database connection failed: " + e.getMessage());
                    return false;
                }
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        appendLog("✅ Database connection successful!");
                        JOptionPane.showMessageDialog(AttendanceSyncGUI.this, "Database connection successful!", "Test Result", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        appendLog("❌ Database connection failed!");
                        JOptionPane.showMessageDialog(AttendanceSyncGUI.this, "Database connection failed!", "Test Result", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    appendLog("❌ Database test error: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    private void testApiConnection() {
        appendLog("🌐 Testing API connection...");
        
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                try {
                    if (attendanceSync == null) {
                        attendanceSync = new AttendanceSync();
                    }
                    attendanceSync.testApi();
                    return "success";
                } catch (Exception e) {
                    return "error: " + e.getMessage();
                }
            }
            
            @Override
            protected void done() {
                try {
                    String result = get();
                    if (result.equals("success")) {
                        appendLog("✅ API connection test completed!");
                        JOptionPane.showMessageDialog(AttendanceSyncGUI.this, "API connection test completed!", "Test Result", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        appendLog("❌ API test failed: " + result);
                        JOptionPane.showMessageDialog(AttendanceSyncGUI.this, "API test failed: " + result, "Test Result", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    appendLog("❌ API test error: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    // System tray methods
    private void setupSystemTray() {
        if (!SystemTray.isSupported()) {
            appendLog("⚠️ System tray is not supported");
            return;
        }
        
        try {
            systemTray = SystemTray.getSystemTray();
            
            // Create tray icon
            Image trayImage = AppIcon.createTrayIcon();
            trayIcon = new TrayIcon(trayImage, "AttendanceSync");
            trayIcon.setImageAutoSize(true);
            
            // Create popup menu
            PopupMenu trayMenu = new PopupMenu();
            
            MenuItem showItem = new MenuItem("Show Console");
            showItem.addActionListener(e -> {
                setVisible(true);
                setState(JFrame.NORMAL);
                toFront();
            });
            
            MenuItem startItem = new MenuItem("Start Service");
            startItem.addActionListener(e -> startService());
            
            MenuItem stopItem = new MenuItem("Stop Service");
            stopItem.addActionListener(e -> stopService());
            
            MenuItem exitItem = new MenuItem("Exit");
            exitItem.addActionListener(e -> {
                if (isServiceRunning) {
                    stopService();
                }
                System.exit(0);
            });
            
            trayMenu.add(showItem);
            trayMenu.addSeparator();
            trayMenu.add(startItem);
            trayMenu.add(stopItem);
            trayMenu.addSeparator();
            trayMenu.add(exitItem);
            
            trayIcon.setPopupMenu(trayMenu);
            
            // Double-click to show window
            trayIcon.addActionListener(e -> {
                setVisible(true);
                setState(JFrame.NORMAL);
                toFront();
            });
            
            systemTray.add(trayIcon);
            appendLog("✅ System tray integration enabled");
            
        } catch (Exception e) {
            appendLog("❌ Failed to setup system tray: " + e.getMessage());
        }
    }
    
    private void minimizeToTray() {
        if (systemTray != null) {
            setVisible(false);
            trayIcon.displayMessage("AttendanceSync", "Application minimized to system tray", TrayIcon.MessageType.INFO);
        }
    }
    
    // Additional utility methods
    private void startStatusTimer() {
        statusTimer = new Timer();
        statusTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    updateUptime();
                    updateStatistics();
                });
            }
        }, 1000, 1000);
    }
    
    private void updateUptime() {
        if (isServiceRunning && serviceStartTime > 0) {
            long uptime = System.currentTimeMillis() - serviceStartTime;
            long hours = uptime / (1000 * 60 * 60);
            long minutes = (uptime % (1000 * 60 * 60)) / (1000 * 60);
            long seconds = (uptime % (1000 * 60)) / 1000;
            
            uptimeLabel.setText(String.format("Uptime: %02d:%02d:%02d", hours, minutes, seconds));
        } else {
            uptimeLabel.setText("Uptime: --");
        }
    }
    
    private void updateStatistics() {
        statsTableModel.setRowCount(0);
        
        // Add statistics rows
        statsTableModel.addRow(new Object[]{"Service Status", isServiceRunning ? "Running" : "Stopped", "Current service state"});
        statsTableModel.addRow(new Object[]{"Sync Cycles", syncCount, "Total sync cycles executed"});
        statsTableModel.addRow(new Object[]{"Successful Syncs", successCount, "Records successfully synchronized"});
        statsTableModel.addRow(new Object[]{"Failed Syncs", errorCount, "Records that failed to sync"});
        
        if (syncCount > 0) {
            double successRate = (double) successCount / syncCount * 100;
            statsTableModel.addRow(new Object[]{"Success Rate", String.format("%.1f%%", successRate), "Percentage of successful syncs"});
        }
        
        // System information
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long usedMemory = totalMemory - freeMemory;
        
        statsTableModel.addRow(new Object[]{"Memory Used", usedMemory + " MB", "Current memory usage"});
        statsTableModel.addRow(new Object[]{"Memory Total", totalMemory + " MB", "Total allocated memory"});
        statsTableModel.addRow(new Object[]{"Java Version", System.getProperty("java.version"), "Runtime Java version"});
    }
    
    private void saveLogs() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("attendance-sync-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + ".log"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                PrintWriter writer = new PrintWriter(file);
                writer.write(logTextArea.getText());
                writer.close();
                
                appendLog("✅ Logs saved to: " + file.getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Logs saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                
            } catch (IOException e) {
                appendLog("❌ Failed to save logs: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Failed to save logs: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void showSystemInfo() {
        StringBuilder info = new StringBuilder();
        info.append("System Information\n");
        info.append("==================\n\n");
        info.append("OS Name: ").append(System.getProperty("os.name")).append("\n");
        info.append("OS Version: ").append(System.getProperty("os.version")).append("\n");
        info.append("OS Architecture: ").append(System.getProperty("os.arch")).append("\n");
        info.append("Java Version: ").append(System.getProperty("java.version")).append("\n");
        info.append("Java Vendor: ").append(System.getProperty("java.vendor")).append("\n");
        info.append("Java Home: ").append(System.getProperty("java.home")).append("\n");
        info.append("User Name: ").append(System.getProperty("user.name")).append("\n");
        info.append("User Home: ").append(System.getProperty("user.home")).append("\n");
        info.append("Working Directory: ").append(System.getProperty("user.dir")).append("\n");
        
        Runtime runtime = Runtime.getRuntime();
        info.append("Available Processors: ").append(runtime.availableProcessors()).append("\n");
        info.append("Max Memory: ").append(runtime.maxMemory() / (1024 * 1024)).append(" MB\n");
        info.append("Total Memory: ").append(runtime.totalMemory() / (1024 * 1024)).append(" MB\n");
        info.append("Free Memory: ").append(runtime.freeMemory() / (1024 * 1024)).append(" MB\n");
        
        JTextArea textArea = new JTextArea(info.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        JOptionPane.showMessageDialog(this, scrollPane, "System Information", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void openLogsFolder() {
        try {
            File logsDir = new File("logs");
            if (!logsDir.exists()) {
                logsDir = new File(System.getProperty("user.dir"));
            }
            
            Desktop.getDesktop().open(logsDir);
        } catch (Exception e) {
            appendLog("❌ Failed to open logs folder: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Failed to open logs folder: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void exportConfiguration() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("attendancesync-config-" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".properties"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File source = new File(Constants.DEFAULT_CONFIG_PATH);
                File destination = fileChooser.getSelectedFile();
                
                Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                appendLog("✅ Configuration exported to: " + destination.getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Configuration exported successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                
            } catch (Exception e) {
                appendLog("❌ Failed to export configuration: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Failed to export configuration: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void importConfiguration() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Properties Files", "properties"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            int option = JOptionPane.showConfirmDialog(this,
                "This will replace your current configuration. Are you sure?",
                "Import Configuration",
                JOptionPane.YES_NO_OPTION);
                
            if (option == JOptionPane.YES_OPTION) {
                try {
                    File source = fileChooser.getSelectedFile();
                    File destination = new File(Constants.DEFAULT_CONFIG_PATH);
                    
                    Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    
                    loadConfiguration();
                    appendLog("✅ Configuration imported from: " + source.getAbsolutePath());
                    JOptionPane.showMessageDialog(this, "Configuration imported successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    
                } catch (Exception e) {
                    appendLog("❌ Failed to import configuration: " + e.getMessage());
                    JOptionPane.showMessageDialog(this, "Failed to import configuration: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private void showAboutDialog() {
        StringBuilder about = new StringBuilder();
        about.append("AttendanceSync Desktop Management Console\n\n");
        about.append("Version: 2.0 - Desktop Edition\n");
        about.append("Build Date: ").append(new SimpleDateFormat("yyyy-MM-dd").format(new Date())).append("\n\n");
        about.append("A comprehensive desktop application for managing\n");
        about.append("biometric attendance synchronization services.\n\n");
        about.append("Features:\n");
        about.append("• Service Control & Monitoring\n");
        about.append("• Configuration Management\n");
        about.append("• Real-time Log Viewing\n");
        about.append("• System Tray Integration\n");
        about.append("• Performance Statistics\n");
        about.append("• Database & API Testing\n\n");
        about.append("© 2025 AttendanceSync Team\n");
        about.append("All rights reserved.");
        
        JOptionPane.showMessageDialog(this, about.toString(), "About AttendanceSync", JOptionPane.INFORMATION_MESSAGE);
    }
    
    // Live log monitoring methods
    private void startLiveLogMonitoring() {
        if (logMonitoringActive) {
            return;
        }
        
        logMonitoringActive = true;
        
        // Load existing filtered logs first
        loadFilteredLogs();
        
        logWatchThread = new Thread(() -> {
            try {
                Path logFilePath = Paths.get(Constants.DEFAULT_LOG_PATH);
                Path logDir = logFilePath.getParent();
                
                // Debug: Show the paths being used
                appendLog("🔍 Debug: Log file path: " + logFilePath.toAbsolutePath());
                appendLog("🔍 Debug: Log directory: " + logDir.toAbsolutePath());
                appendLog("🔍 Debug: Working directory: " + System.getProperty("user.dir"));
                
                // Create logs directory if it doesn't exist
                if (!Files.exists(logDir)) {
                    Files.createDirectories(logDir);
                    appendLog("📁 Created logs directory: " + logDir.toAbsolutePath());
                }
                
                // Create log file if it doesn't exist
                if (!Files.exists(logFilePath)) {
                    Files.createFile(logFilePath);
                    appendLog("📄 Created log file: " + logFilePath.toAbsolutePath());
                }
                
                logWatchService = FileSystems.getDefault().newWatchService();
                logDir.register(logWatchService, StandardWatchEventKinds.ENTRY_MODIFY);
                
                // Get initial file size
                if (Files.exists(logFilePath)) {
                    lastLogFileSize = Files.size(logFilePath);
                    appendLog("📏 Initial log file size: " + lastLogFileSize + " bytes");
                }
                
                appendLog("🔴 Live log monitoring started");
                appendLog("👀 Watching for changes to: " + logFilePath.getFileName());
                
                while (logMonitoringActive) {
                    WatchKey key = logWatchService.take();
                    
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                            Path changed = (Path) event.context();
                            if (changed.toString().equals(logFilePath.getFileName().toString())) {
                                // File was modified, read new content
                                appendLog("📝 Log file changed detected");
                                readNewLogContent(logFilePath);
                            }
                        }
                    }
                    
                    key.reset();
                }
                
            } catch (IOException | InterruptedException e) {
                if (logMonitoringActive) {
                    SwingUtilities.invokeLater(() -> 
                        appendLog("❌ Log monitoring error: " + e.getMessage()));
                }
            } finally {
                try {
                    if (logWatchService != null) {
                        logWatchService.close();
                    }
                } catch (IOException e) {
                    // Ignore cleanup errors
                }
            }
        });
        
        logWatchThread.setDaemon(true);
        logWatchThread.start();
    }
    
    private void stopLiveLogMonitoring() {
        logMonitoringActive = false;
        
        if (logWatchThread != null) {
            logWatchThread.interrupt();
        }
        
        try {
            if (logWatchService != null) {
                logWatchService.close();
            }
        } catch (IOException e) {
            // Ignore cleanup errors
        }
        
        appendLog("⏹ Live log monitoring stopped");
    }
    
    private void readNewLogContent(Path logFilePath) {
        try {
            if (!Files.exists(logFilePath)) {
                appendLog("⚠️ Log file not found: " + logFilePath.toAbsolutePath());
                return;
            }
            
            long currentSize = Files.size(logFilePath);
            appendLog("📊 File size check - Current: " + currentSize + ", Last: " + lastLogFileSize);
            
            if (currentSize <= lastLogFileSize) {
                appendLog("ℹ️ No new content in log file");
                return; // No new content
            }
            
            // Read only the new content
            try (RandomAccessFile file = new RandomAccessFile(logFilePath.toFile(), "r")) {
                file.seek(lastLogFileSize);
                
                String line;
                int newLinesCount = 0;
                int filteredLinesCount = 0;
                
                while ((line = file.readLine()) != null) {
                    newLinesCount++;
                    final String logLine = line;
                    if (shouldShowLogLine(logLine)) {
                        filteredLinesCount++;
                        SwingUtilities.invokeLater(() -> {
                            String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
                            logTextArea.append("[" + timestamp + "] " + formatLogLine(logLine) + "\n");
                            logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
                        });
                    }
                }
                
                lastLogFileSize = currentSize;
                appendLog("✅ Processed " + newLinesCount + " new lines, showed " + filteredLinesCount + " filtered lines");
            }
            
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> 
                appendLog("❌ Error reading log file: " + e.getMessage()));
        }
    }
    
    private void loadFilteredLogs() {
        try {
            File logFile = new File(Constants.DEFAULT_LOG_PATH);
            if (!logFile.exists()) {
                appendLog("📄 Log file not found, live monitoring will show new logs");
                return;
            }
            
            // Clear existing content
            logTextArea.setText("");
            
            try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
                String line;
                java.util.List<String> recentLines = new java.util.ArrayList<>();
                
                // Read all lines and keep last 50 relevant ones
                while ((line = reader.readLine()) != null) {
                    if (shouldShowLogLine(line)) {
                        recentLines.add(formatLogLine(line));
                        if (recentLines.size() > 50) {
                            recentLines.remove(0);
                        }
                    }
                }
                
                // Display recent filtered logs
                for (String logLine : recentLines) {
                    String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
                    logTextArea.append("[" + timestamp + "] " + logLine + "\n");
                }
                
                logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
                lastLogFileSize = logFile.length();
                
                appendLog("📄 Loaded recent filtered logs (" + recentLines.size() + " entries)");
            }
            
        } catch (IOException e) {
            appendLog("❌ Failed to load existing logs: " + e.getMessage());
        }
    }
    
    private boolean shouldShowLogLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return false;
        }
        
        String lowerLine = line.toLowerCase();
        
        // Show API related logs
        if (lowerLine.contains("api") || lowerLine.contains("request") || lowerLine.contains("response")) {
            return true;
        }
        
        // Show biometric code related logs
        if (lowerLine.contains("biomatric") || lowerLine.contains("biometric") || 
            lowerLine.contains("employee") || lowerLine.contains("processing")) {
            return true;
        }
        
        // Show errors and important status
        if (lowerLine.contains("error") || lowerLine.contains("exception") || 
            lowerLine.contains("failed") || lowerLine.contains("success") ||
            lowerLine.contains("warning") || lowerLine.contains("severe")) {
            return true;
        }
        
        // Show database operations
        if (lowerLine.contains("database") || lowerLine.contains("connection") ||
            lowerLine.contains("updated") || lowerLine.contains("recorded")) {
            return true;
        }
        
        // Show sync cycle information
        if (lowerLine.contains("sync") || lowerLine.contains("cycle") ||
            lowerLine.contains("started") || lowerLine.contains("records")) {
            return true;
        }
        
        // Filter out common noise
        if (lowerLine.contains("sleeping") || lowerLine.contains("configuration loaded") ||
            lowerLine.contains("school:") || lowerLine.contains("database:")) {
            return false;
        }
        
        return false;
    }
    
    private String formatLogLine(String line) {
        // Remove timestamp from log line if it exists (our GUI adds its own)
        if (line.matches("^\\w{3} \\d{2}, \\d{4} \\d{1,2}:\\d{2}:\\d{2} [AP]M.*")) {
            // Java logging format: "Jan 01, 2025 12:00:00 PM ..."
            int messageStart = line.indexOf("M ") + 2;
            if (messageStart < line.length()) {
                line = line.substring(messageStart);
            }
        }
        
        // Add emoji indicators for different log types
        String lowerLine = line.toLowerCase();
        
        if (lowerLine.contains("error") || lowerLine.contains("exception") || lowerLine.contains("failed")) {
            return "❌ " + line;
        } else if (lowerLine.contains("success") || lowerLine.contains("recorded")) {
            return "✅ " + line;
        } else if (lowerLine.contains("warning")) {
            return "⚠️ " + line;
        } else if (lowerLine.contains("api") || lowerLine.contains("request") || lowerLine.contains("response")) {
            return "🌐 " + line;
        } else if (lowerLine.contains("biomatric") || lowerLine.contains("biometric") || lowerLine.contains("employee")) {
            return "👤 " + line;
        } else if (lowerLine.contains("database") || lowerLine.contains("connection")) {
            return "🔌 " + line;
        } else if (lowerLine.contains("sync") || lowerLine.contains("cycle")) {
            return "🔄 " + line;
        }
        
        return "ℹ️ " + line;
    }
    
    public static void main(String[] args) {
        // Set look and feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Use default look and feel
            System.err.println("Could not set look and feel: " + e.getMessage());
        }
        
        // Check for server mode argument
        boolean serverMode = false;
        if (args.length > 0 && "server".equalsIgnoreCase(args[0])) {
            serverMode = true;
        }
        
        final boolean isServerMode = serverMode;
        SwingUtilities.invokeLater(() -> {
            new AttendanceSyncGUI(isServerMode).setVisible(true);
        });
    }
}
