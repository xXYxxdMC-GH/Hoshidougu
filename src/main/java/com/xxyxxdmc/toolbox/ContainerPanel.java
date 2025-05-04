package main.java.com.xxyxxdmc.toolbox;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class ContainerPanel extends JPanel {
    private double angle;
    public ContainerPanel() {
        setLayout(null);
        setBounds(0, 0, 800, 800);
        setOpaque(false);
    }

    public void rotate(double degrees) {
        this.angle = degrees;
        repaint();
    }


    public void addNamePanel(NamePanel namePanel) {
        add(namePanel);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        AffineTransform transform = new AffineTransform();
        transform.rotate(Math.toRadians(angle), getWidth() / 2.0, getHeight() / 2.0);
        g2d.setTransform(transform);

        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    public BufferedImage cachePanel(JPanel panel) {
        BufferedImage image = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        panel.paint(g2d);
        g2d.dispose();
        return image;
    }


}