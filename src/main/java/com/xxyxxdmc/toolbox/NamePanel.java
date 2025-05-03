package main.java.com.xxyxxdmc.toolbox;

import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class NamePanel extends JPanel {
    private double angle = 0;
    private static final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    private static Font minecraftFont;
    private final String text;
    private Color color = Color.BLACK;
    private static final JFrame frame = new JFrame("");
    private static java.util.List<NamePanel> namePanels = new ArrayList<>();

    static {
        InputStream fontStream = classLoader.getResourceAsStream("minecraft.ttf");
        assert fontStream != null;
        try {
            minecraftFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
        } catch (FontFormatException | IOException ignored) {}
    }
    public void rotate(double degrees) {
        this.angle = degrees;
        repaint();
    }

    public NamePanel(String name, @Nullable Color nowColor, double nowAngle) {
        this.text = name;
        if (nowColor != null) this.color=nowColor;
        this.angle = nowAngle;
        setOpaque(false);
        setBounds(0, 0, frame.getWidth(), frame.getHeight());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int x = 520, y = 400, width = 350, height = 45;
        AffineTransform transform = new AffineTransform();
        transform.rotate(Math.toRadians(angle), x - 30, y+ (double) height /2);
        g2d.setTransform(transform);

        g2d.setColor(color);
        g2d.fillRect(x, y, width, height);

        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(5));
        g2d.drawRect(x, y, width, height);

        g2d.setFont(minecraftFont.deriveFont(Font.PLAIN, 20));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = x + (width - fm.stringWidth(text))-5;
        int textY = y + (height + fm.getAscent()) / 2;

        g2d.drawString(text, textX, textY);
    }

    public static void main(String[] args) {
        System.setProperty("prism.forceGPU", "true");
        frame.setSize(800,800);
        frame.setAlwaysOnTop(true);
        frame.getContentPane().setBackground(Color.BLACK);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setLayout(null);
        frame.add(new Arrow());
        frame.revalidate();
        frame.repaint();
        for (int i=0;i<46;i++) {
            NamePanel namePanel = new NamePanel(String.valueOf(i), null, 0);
            namePanels.add(namePanel);
            frame.add(namePanel);
            frame.revalidate();
            frame.repaint();
        }
        for (int i=0;i<namePanels.size();i++) {
            NamePanel namePanel = namePanels.get(i);
            namePanel.setVisible(true);
            namePanel.setBounds(0, 0, frame.getWidth(), frame.getHeight());
            final double[] time = {0};
            double anglePerPanel = 360.0 / 46;
            double targetAngle = i * anglePerPanel;
            new Timer(20, e -> {
                if (time[0] <1){
                    namePanel.rotate(targetAngle * EasingFunctions.easeInOutExpo(time[0]));
                    time[0] +=0.08;
                    frame.revalidate();
                    frame.repaint();
                } else {
                    frame.revalidate();
                    frame.repaint();
                    ((Timer)e.getSource()).stop();
                }
            }).start();
        }
    }
}
class Arrow extends JLabel {
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int radius = 350;

        g2d.setColor(Color.WHITE);
        g2d.fillPolygon(new int[]{centerX + radius, centerX+radius+20, centerX+radius+20}, new int[]{centerY, centerY - 10, centerY + 10}, 3);
    }
}
