package com.attendance.sync.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Application icon and branding utilities
 */
public class AppIcon {
    
    private static ImageIcon cachedIcon = null;
    
    /**
     * Creates the application icon for taskbar and window
     */
    public static ImageIcon createApplicationIcon() {
        if (cachedIcon != null) {
            return cachedIcon;
        }
        
        // Create a 64x64 icon with attendance/biometric theme
        BufferedImage icon = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = icon.createGraphics();
        
        // Enable anti-aliasing for smooth graphics
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // Background gradient (blue to darker blue)
        GradientPaint gradient = new GradientPaint(0, 0, new Color(52, 152, 219), 64, 64, new Color(41, 128, 185));
        g2d.setPaint(gradient);
        g2d.fillRoundRect(0, 0, 64, 64, 12, 12);
        
        // Border
        g2d.setColor(new Color(34, 98, 145));
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawRoundRect(1, 1, 62, 62, 12, 12);
        
        // Fingerprint/biometric symbol (simplified)
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        // Draw fingerprint-like concentric ovals
        int centerX = 32, centerY = 28;
        for (int i = 0; i < 4; i++) {
            int width = 8 + (i * 6);
            int height = 6 + (i * 4);
            g2d.drawOval(centerX - width/2, centerY - height/2, width, height);
        }
        
        // Add small dots for tech/digital feel
        g2d.fillOval(20, 48, 4, 4);
        g2d.fillOval(32, 48, 4, 4);
        g2d.fillOval(44, 48, 4, 4);
        
        // Add "A" for AttendanceSync (larger and more visible)
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "A";
        int textX = (64 - fm.stringWidth(text)) / 2;
        int textY = 58;
        
        // Add shadow for better visibility
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.drawString(text, textX + 1, textY + 1);
        
        // Draw main text
        g2d.setColor(Color.WHITE);
        g2d.drawString(text, textX, textY);
        
        g2d.dispose();
        
        cachedIcon = new ImageIcon(icon);
        return cachedIcon;
    }
    
    /**
     * Creates a smaller version for system tray
     */
    public static Image createTrayIcon() {
        ImageIcon appIcon = createApplicationIcon();
        return appIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
    }
    
    /**
     * Creates multiple sizes for different contexts
     */
    public static java.util.List<Image> createIconList() {
        ImageIcon baseIcon = createApplicationIcon();
        java.util.List<Image> icons = new java.util.ArrayList<>();
        
        // Common icon sizes for Windows/Linux
        int[] sizes = {16, 20, 24, 32, 48, 64, 128, 256};
        
        for (int size : sizes) {
            Image scaledIcon = baseIcon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
            icons.add(scaledIcon);
        }
        
        return icons;
    }
}
