package main.java.com.xxyxxdmc.toolbox;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class MainToolBox {
    private final static JFrame toolboxFrame = new JFrame("Toolbox");
    private static double frameTime = 0;
    private final static JLabel clockButton = new JLabel("");
    private final static JLabel cardDrawButton = new JLabel("");
    private final static JLabel wheelButton = new JLabel("");
    private final static JLabel musicPlayerButton = new JLabel("");
    private final static JLabel[] labelList = {clockButton, cardDrawButton, wheelButton, musicPlayerButton};
    private static boolean isPlayer = false;
    static {
        for (JLabel label : labelList) {
            label.setOpaque(true);
            label.setForeground(Color.BLACK);
            label.setPreferredSize(new Dimension(100, 100));
        }
    }
    static {
        toolboxFrame.setSize(300,300);
        toolboxFrame.setUndecorated(true);
        toolboxFrame.setLocationRelativeTo(null);
        toolboxFrame.setResizable(false);
        toolboxFrame.setType(Window.Type.UTILITY);
        toolboxFrame.setBackground(Color.BLACK);
        toolboxFrame.setShape(new RoundRectangle2D.Float(0, 0, 300, 300, 20, 20));
        toolboxFrame.getContentPane().setBackground(new Color(10,10,10));
        toolboxFrame.setAlwaysOnTop(true);
        toolboxFrame.setOpacity(0.1f);
        toolboxFrame.setLayout(new GridLayout(2,2));
        toolboxFrame.setVisible(true);
        for (JLabel label : labelList) {
            toolboxFrame.add(label);
        }
    }
    public static void main(String[] args) {
        showFrame();
        showAllLabel();
    }
    public static void showAllLabel() {
        for (JLabel label : labelList) {
            label.setEnabled(true);
            label.setVisible(true);
        }
        toolboxFrame.revalidate();
        toolboxFrame.repaint();
    }
    public static void hideAllLabel() {

    }
    public static void showFrame() {
        if (isPlayer) return;
        isPlayer=true;
        new Timer(20, e -> {
            if (frameTime<1) {
                frameTime+=0.04;
                int side = 300 + (int) Math.ceil(100*EasingFunctions.easeOutExpo(frameTime));
                toolboxFrame.setSize(side, side);
                toolboxFrame.setShape(new RoundRectangle2D.Float(0, 0, side, side, 20, 20));
                toolboxFrame.setOpacity((float) EasingFunctions.easeOutExpo(frameTime));
                toolboxFrame.setLocationRelativeTo(null);
            } else {
                frameTime=0;
                isPlayer=false;
                ((Timer) e.getSource()).stop();
            }
        }).start();
    }
    public static void hideFrame(boolean exitToolbox) {
        if (isPlayer) return;
        isPlayer=true;
        new Timer(20, e -> {
            if (frameTime<1) {
                frameTime+=0.04;
                int side = 300 + (int) Math.ceil(100*EasingFunctions.easeOutExpo(frameTime));
                toolboxFrame.setSize(side, side);
                toolboxFrame.setShape(new RoundRectangle2D.Float(0, 0, side, side, 20, 20));
                toolboxFrame.setOpacity((float) EasingFunctions.easeOutExpo(frameTime));
                toolboxFrame.setLocationRelativeTo(null);
            } else {
                frameTime=0;
                isPlayer=false;
                ((Timer) e.getSource()).stop();
                if (exitToolbox) System.exit(0);
            }
        }).start();
    }
}
