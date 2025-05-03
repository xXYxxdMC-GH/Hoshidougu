package main.java.com.xxyxxdmc.toolbox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

public class AdvancedRoulette extends JPanel {
    private static final JFrame frame = new JFrame("高级旋转轮盘");
    private static final Color[] COLORS = {
            new Color(205, 34, 89), new Color(78, 156, 210), new Color(143, 220, 44),
            new Color(231, 117, 98), new Color(56, 32, 180), new Color(189, 200, 76),
            new Color(39, 145, 223), new Color(220, 67, 154), new Color(124, 198, 232),
            new Color(88, 90, 240), new Color(33, 177, 77), new Color(250, 135, 55),
            new Color(173, 200, 234), new Color(110, 56, 147), new Color(212, 45, 123),
            new Color(92, 182, 17), new Color(255, 99, 71), new Color(150, 223, 140),
            new Color(14, 79, 196), new Color(199, 140, 180), new Color(128, 217, 240),
            new Color(35, 170, 210), new Color(255, 207, 72), new Color(146, 58, 202),
            new Color(240, 88, 121), new Color(95, 230, 49), new Color(120, 56, 236),
            new Color(200, 170, 110), new Color(68, 132, 215), new Color(223, 51, 157),
            new Color(160, 90, 230), new Color(245, 199, 63), new Color(55, 177, 94),
            new Color(221, 122, 241), new Color(99, 205, 144), new Color(185, 65, 200),
            new Color(72, 243, 180), new Color(240, 130, 45), new Color(88, 76, 219),
            new Color(215, 149, 56), new Color(60, 210, 243), new Color(255, 142, 193),
            new Color(123, 215, 88), new Color(209, 72, 112), new Color(77, 125, 240),
            new Color(182, 56, 210)
    };

    private int angle = 0;
    private Timer timer;
    private boolean spinning = false;
    private int speed = 20; // 初始旋转速度
    private double time;
    private static java.util.List<NamePanel> namePanels = new ArrayList<>();

    public AdvancedRoulette() {
        JButton spinButton = new JButton("旋转轮盘");
        spinButton.addActionListener(this::startSpin);
        add(spinButton, BorderLayout.SOUTH);
        setBackground(Color.BLACK);
    }

    private void startSpin(ActionEvent e) {
        if (spinning) return;
        spinning = true;
        speed = 50;
        time=0;
        namePanels.get(1).rotate(20);

//        timer = new Timer(40, event -> {
//            if (time<1) {
//                time+=0.02;
//                angle += (int) (speed * EasingFunctions.easeX2(time));
//            } else {
//                spinning = false;
//                time=0;
//                timer.stop();
//            }
//            repaint();
//        });
//        timer.start();
    }

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

    public static void main(String[] args) {
        frame.setSize(800, 800);
        frame.getContentPane().setBackground(Color.BLACK);
        frame.setLocationRelativeTo(null);
        frame.add(new AdvancedRoulette());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);
        for (int i=0;i<46;i++) {
            NamePanel namePanel = new NamePanel(String.valueOf(i), null, 360.0/46*i);
            namePanels.add(namePanel);
            namePanel.setVisible(true);
            frame.revalidate();
            frame.repaint();
        }
        frame.setVisible(true);
    }
}
