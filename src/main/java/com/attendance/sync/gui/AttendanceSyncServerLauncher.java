package com.attendance.sync.gui;

import javax.swing.*;

/**
 * Server Mode Launcher for AttendanceSync Desktop Application
 * 
 * This launcher provides continuous operation mode for server environments:
 * - No auto-minimize (GUI stays visible for monitoring)
 * - Enhanced close protection to prevent accidental shutdown
 * - Continuous operation with keep-alive thread
 * - Proper cleanup on shutdown
 * 
 * @author AttendanceSync Team
 * @version 2.0
 */
public class AttendanceSyncServerLauncher {
    
    private static AttendanceSyncGUI gui;
    private static boolean serverMode = true; // Always true for server launcher
    
    public static void main(String[] args) {
        // Process command line arguments
        for (String arg : args) {
            if ("--server-mode".equals(arg)) {
                serverMode = true;
            } else if ("--desktop-mode".equals(arg)) {
                serverMode = false;
            }
        }
        
        System.out.println("🚀 AttendanceSync Server Launcher v2.0");
        System.out.println("================================================");
        
        SwingUtilities.invokeLater(() -> {
            launchServerModeGUI();
        });
    }
    
    private static void launchServerModeGUI() {
        try {
            // Create GUI with server mode enabled
            gui = new AttendanceSyncGUI(serverMode);
            
            // Set proper window title and branding
            gui.setTitle("AttendanceSync Server - Biometric Sync Monitor");
            
            // Show GUI - NO AUTO-MINIMIZE
            gui.setVisible(true);
            
            // Bring window to front and request focus
            gui.toFront();
            gui.requestFocus();
            
            if (serverMode) {
                System.out.println("🖥️ AttendanceSync Server Mode - GUI will remain visible for monitoring");
                System.out.println("🔄 Application will run continuously until explicitly stopped");
                System.out.println("🔒 Enhanced close protection enabled");
                System.out.println("📱 GUI will NOT auto-minimize - stays visible for monitoring");
                System.out.println("⚠️ Use File -> Exit or enhanced close dialog to stop application");
                System.out.println("🖼️ Application icon should be visible in taskbar");
            } else {
                System.out.println("🖥️ AttendanceSync Desktop Application started");
            }
            
            // Add shutdown hook to ensure proper cleanup
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("🛑 Shutting down AttendanceSync Desktop Application...");
                if (gui != null) {
                    try {
                        gui.dispose();
                    } catch (Exception e) {
                        System.err.println("Error during shutdown: " + e.getMessage());
                    }
                }
            }));
            
            // Prevent JVM from exiting when GUI is hidden
            // This is crucial for server mode operation
            if (serverMode) {
                keepApplicationAlive();
            }
            
        } catch (Exception e) {
            System.err.println("❌ Failed to launch AttendanceSync Desktop Application: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Keep the application alive in server mode
     * This prevents the JVM from exiting even if the GUI is hidden
     */
    private static void keepApplicationAlive() {
        Thread keepAliveThread = new Thread(() -> {
            try {
                System.out.println("🔄 Server mode keep-alive thread started");
                
                while (serverMode && gui != null) {
                    // Check every 30 seconds if GUI is still available
                    Thread.sleep(30000);
                    
                    // If GUI was disposed, exit gracefully
                    if (gui != null && !gui.isDisplayable()) {
                        System.out.println("🛑 GUI disposed - shutting down server mode");
                        break;
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("🔄 Keep-alive thread interrupted - normal shutdown");
            } catch (Exception e) {
                System.err.println("❌ Error in keep-alive thread: " + e.getMessage());
            } finally {
                System.out.println("🔄 Keep-alive thread ended");
            }
        }, "ServerModeKeepAlive");
        
        keepAliveThread.setDaemon(false); // Non-daemon thread keeps JVM alive
        keepAliveThread.start();
    }
    
    /**
     * Get the current GUI instance
     * @return Current AttendanceSyncGUI instance
     */
    public static AttendanceSyncGUI getGUI() {
        return gui;
    }
    
    /**
     * Check if running in server mode
     * @return true if server mode is enabled
     */
    public static boolean isServerMode() {
        return serverMode;
    }
    
    /**
     * Gracefully shutdown the application
     */
    public static void shutdown() {
        System.out.println("🛑 Initiating graceful shutdown...");
        serverMode = false;
        
        if (gui != null) {
            SwingUtilities.invokeLater(() -> {
                gui.dispose();
                System.exit(0);
            });
        } else {
            System.exit(0);
        }
    }
}
