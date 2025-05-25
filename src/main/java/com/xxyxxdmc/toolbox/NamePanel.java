package main.java.com.xxyxxdmc.toolbox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class NamePanel extends JLabel {
    private double angle;
    private static final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    private static Font minecraftFont;
    private final String text;
    private Color color = Color.BLACK;
    public static final JFrame wheelFrame = new JFrame("");
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
    public void rotatePlus(double degrees) {
        this.angle = this.angle+degrees;
        repaint();
    }

    public NamePanel(String name, Color nowColor, double nowAngle) {
        this.text = name;
        if (nowColor != null) this.color=nowColor;
        this.angle = nowAngle;
        setOpaque(false);
        setBounds(0, 0, wheelFrame.getWidth(), wheelFrame.getHeight());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int x = wheelFrame.getWidth() / 2 + 20, y = wheelFrame.getHeight() / 2 - 30, width = wheelFrame.getWidth() / 8 * 3, height = 45;
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
        wheelFrame.setSize(800,800);
        wheelFrame.setUndecorated(true);
        wheelFrame.setShape(new RoundRectangle2D.Float(0, 0, 800, 800, 30, 30));
        wheelFrame.setAlwaysOnTop(true);
        wheelFrame.getContentPane().setBackground(Color.BLACK);
        wheelFrame.setLocationRelativeTo(null);
        wheelFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        wheelFrame.setVisible(true);
        wheelFrame.setLayout(null);
        wheelFrame.setResizable(false);
        Arrow arrow = new Arrow();
        wheelFrame.add(arrow);
        arrow.setVisible(true);
        arrow.setBounds(0, 0, wheelFrame.getWidth(), wheelFrame.getHeight());
        ContainerPanel containerPanel = new ContainerPanel();
        wheelFrame.add(containerPanel);
        wheelFrame.revalidate();
        wheelFrame.repaint();
        for (int i=0;i<46;i++) {
            NamePanel namePanel = new NamePanel(String.valueOf(i), null, 0);
            namePanels.add(namePanel);
            containerPanel.addNamePanel(namePanel);
            wheelFrame.revalidate();
            wheelFrame.repaint();
        }
        for (int i=0;i<namePanels.size();i++) {
            NamePanel namePanel = namePanels.get(i);
            namePanel.setVisible(true);
            namePanel.setBounds(0, 0, wheelFrame.getWidth(), wheelFrame.getHeight());
            final double[] time = {0};
            double anglePerPanel = 360.0 / 46;
            double targetAngle = i * anglePerPanel;
            new Timer(20, e -> {
                if (time[0] <1){
                    namePanel.rotate(targetAngle * EasingFunctions.easeInOutExpo(time[0]));
                    time[0] +=0.08;
                    wheelFrame.revalidate();
                    wheelFrame.repaint();
                } else {
                    wheelFrame.revalidate();
                    wheelFrame.repaint();
                    ((Timer)e.getSource()).stop();
                }
            }).start();
        }
        wheelFrame.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                double angle = 360 * Math.random();
                final double[] time = {0};
                double targetAngle = 360 * Math.random();
                new Timer(20, e1 -> {
                    if (time[0] <1){
                        containerPanel.rotate(targetAngle * EasingFunctions.easeInOutExpo(time[0]));
                        time[0] +=0.08;
                        wheelFrame.revalidate();
                        wheelFrame.repaint();
                    } else {
                        wheelFrame.revalidate();
                        wheelFrame.repaint();
                        ((Timer)e1.getSource()).stop();
                    }
                }).start();
            }
            @Override
            public void mousePressed(MouseEvent e) {}
            @Override
            public void mouseReleased(MouseEvent e) {}
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
        });
    }
}
class Arrow extends JLabel {
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int centerX = 400;
        int centerY = 400;
        int radius = 350;

        g2d.setColor(Color.WHITE);
        g2d.fillPolygon(new int[]{centerX + radius + 5, centerX+radius+35, centerX+radius+35}, new int[]{centerY, centerY - 15, centerY + 15}, 3);
    }
}
