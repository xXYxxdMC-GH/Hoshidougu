package main.java.com.xxyxxdmc.toolbox;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

public class RouletteGame extends JPanel {
    private final int numPanels = 46;
    private final List<NamePanel_> namePanels = new ArrayList<>();
    private double angle = 0; // 轮盘当前角度
    private Timer timer;
    private double time = 0;

    public RouletteGame() {
        setBackground(Color.BLACK);
        setLayout(null); // 禁用默认布局

        // 生成 46 个 NamePanel
        for (int i = 0; i < numPanels; i++) {
            NamePanel_ namePanel = new NamePanel_(String.valueOf(i));
            namePanels.add(namePanel);
            add(namePanel);
        }

        JButton spinButton = new JButton("旋转轮盘");
        spinButton.addActionListener(e -> startSpin());
        spinButton.setBounds(300, 650, 200, 50);
        add(spinButton);

        updatePanelPositions(); // 初始化面板位置
    }

    private void startSpin() {
        time = 0;
        timer = new Timer(20, e -> {
            if (time < 1) {
                angle += 10 * easeOutExpo(time);
                time += 0.02;
                updatePanelPositions(); // 更新面板位置
                repaint();
            } else {
                ((Timer) e.getSource()).stop();
            }
        });
        timer.start();
    }

    private void updatePanelPositions() {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int radius = 350; // 轮盘半径

        double anglePerPanel = 360.0 / namePanels.size();
        for (int i = 0; i < namePanels.size(); i++) {
            double radians = Math.toRadians(i * anglePerPanel); // 转换成弧度
            int x = centerX + (int) (radius * Math.cos(radians)) - 75; // 让面板围绕中心
            int y = centerY + (int) (radius * Math.sin(radians)) - 20;

            namePanels.get(i).setBounds(x, y, 350, 40); // 正确定位
        }
    }

    private double easeOutExpo(double t) {
        return 1 - Math.pow(2, -10 * t);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("轮盘游戏");
        RouletteGame game = new RouletteGame();
        frame.setLocationRelativeTo(null);
        frame.add(game);
        frame.setSize(800, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

class NamePanel_ extends JPanel {
    private final String text;

    public NamePanel_(String text) {
        this.text = text;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int x = 400, y = 400, width = 350, height = 40;
        AffineTransform transform = new AffineTransform();
        transform.rotate(Math.toRadians(0), x, y+ (double) height /2);
        g2d.setTransform(transform);

        g2d.setColor(Color.BLACK);
        g2d.fillRect(400, 400, getWidth(), getHeight());

        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(5));
        g2d.drawRect(400, 400, getWidth(), getHeight());
        g2d.setFont(new Font("SansSerif", Font.BOLD, 20));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = (getWidth() - fm.stringWidth(text)) / 2;
        int textY = (getHeight() + fm.getAscent()) / 2 - fm.getDescent();
        g2d.drawString(text, textX, textY);
    }
}
