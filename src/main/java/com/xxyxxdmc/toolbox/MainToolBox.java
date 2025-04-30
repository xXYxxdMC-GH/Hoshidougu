package main.java.com.xxyxxdmc.toolbox;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class MainToolBox {
    private final static JFrame toolboxFrame = new JFrame("Toolbox");
    private static double frameTime = 0;
    static {
        toolboxFrame.setSize(300,300);
        toolboxFrame.setUndecorated(true);
        toolboxFrame.setLocationRelativeTo(null);
        toolboxFrame.setResizable(false);
        toolboxFrame.setType(Window.Type.UTILITY);
        toolboxFrame.setBackground(Color.BLACK);
        toolboxFrame.setShape(new RoundRectangle2D.Float(0, 0, 300, 300, 20, 20));
        toolboxFrame.getContentPane().setBackground(Color.BLACK);
        toolboxFrame.setAlwaysOnTop(true);
        toolboxFrame.setOpacity(0.1f);
        toolboxFrame.setLayout(new GridLayout());
        toolboxFrame.setVisible(true);
    }
    public static void main(String[] args) {
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
                ((Timer) e.getSource()).stop();
            }
        }).start();
    }
}
