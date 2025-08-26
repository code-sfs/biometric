package com.attendance.sync.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Simple launcher for AttendanceSync Desktop Application
 * This creates a basic launcher that can start both the GUI and console versions
 */
public class AttendanceSyncLauncher extends JFrame {
    
    private static final Color PRIMARY_COLOR = new Color(52, 152, 219);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font NORMAL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    
    public AttendanceSyncLauncher() {
        setupUI();
    }
    
    private void setupUI() {
        setTitle("AttendanceSync Launcher");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(500, 80));
        headerPanel.setLayout(new BorderLayout());
        
        JLabel titleLabel = new JLabel("AttendanceSync", SwingConstants.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Biometric Attendance Synchronization System", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(Color.WHITE);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Content panel
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Launch options
        JLabel instructionLabel = new JLabel("Choose how to run AttendanceSync:");
        instructionLabel.setFont(NORMAL_FONT);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        contentPanel.add(instructionLabel, gbc);
        
        // Desktop GUI button
        JButton guiButton = createStyledButton("🖥️ Desktop Application", "Launch with full GUI interface");
        guiButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                launchGUI();
            }
        });
        gbc.gridy++; gbc.gridwidth = 2;
        contentPanel.add(guiButton, gbc);
        
        // Console button
        JButton consoleButton = createStyledButton("⚡ Console Version", "Run in console mode (original)");
        consoleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                launchConsole();
            }
        });
        gbc.gridy++; gbc.gridwidth = 2;
        contentPanel.add(consoleButton, gbc);
        
        // Configuration button
        JButton configButton = createStyledButton("⚙️ Configuration Only", "Edit configuration without starting service");
        configButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                launchConfigOnly();
            }
        });
        gbc.gridy++; gbc.gridwidth = 2;
        contentPanel.add(configButton, gbc);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Footer
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(Color.LIGHT_GRAY);
        footerPanel.setPreferredSize(new Dimension(500, 30));
        
        JLabel footerLabel = new JLabel("Version 2.0 - Desktop Edition", SwingConstants.CENTER);
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        footerPanel.add(footerLabel);
        
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JButton createStyledButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setFont(NORMAL_FONT);
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(300, 50));
        button.setToolTipText(tooltip);
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR);
            }
        });
        
        return button;
    }
    
    private void launchGUI() {
        try {
            // Try to launch the full GUI application
            SwingUtilities.invokeLater(() -> {
                try {
                    AttendanceSyncGUI gui = new AttendanceSyncGUI();
                    gui.setVisible(true);
                    dispose(); // Close launcher
                } catch (Exception e) {
                    showError("Failed to launch GUI application", e);
                }
            });
        } catch (Exception e) {
            showError("Error launching GUI", e);
        }
    }
    
    private void launchConsole() {
        try {
            // Launch the original console application
            dispose(); // Close launcher
            
            // Run in separate thread to avoid blocking
            new Thread(() -> {
                try {
                    com.attendance.sync.AttendanceSync.main(new String[0]);
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> showError("Failed to launch console application", e));
                }
            }).start();
            
        } catch (Exception e) {
            showError("Error launching console application", e);
        }
    }
    
    private void launchConfigOnly() {
        try {
            // Launch only the configuration part of the GUI
            SwingUtilities.invokeLater(() -> {
                try {
                    AttendanceSyncGUI gui = new AttendanceSyncGUI();
                    gui.setVisible(true);
                    // Switch to configuration tab
                    JTabbedPane tabbedPane = findTabbedPane(gui.getContentPane());
                    if (tabbedPane != null) {
                        tabbedPane.setSelectedIndex(1); // Configuration tab
                    }
                    dispose(); // Close launcher
                } catch (Exception e) {
                    showError("Failed to launch configuration", e);
                }
            });
        } catch (Exception e) {
            showError("Error launching configuration", e);
        }
    }
    
    private JTabbedPane findTabbedPane(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JTabbedPane) {
                return (JTabbedPane) component;
            } else if (component instanceof Container) {
                JTabbedPane found = findTabbedPane((Container) component);
                if (found != null) return found;
            }
        }
        return null;
    }
    
    private void showError(String message, Exception e) {
        String fullMessage = message + "\n\nError: " + e.getMessage();
        JOptionPane.showMessageDialog(this, fullMessage, "Error", JOptionPane.ERROR_MESSAGE);
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
        }
        
        SwingUtilities.invokeLater(() -> {
            new AttendanceSyncLauncher().setVisible(true);
        });
    }
}
