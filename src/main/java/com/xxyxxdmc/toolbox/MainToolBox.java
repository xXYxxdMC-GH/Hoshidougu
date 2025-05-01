package main.java.com.xxyxxdmc.toolbox;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

public class MainToolBox {
    private final static JFrame toolboxFrame = new JFrame("Toolbox");
    private static double frameTime = 0;
    private final static JLabel clockButton = new JLabel("", JLabel.CENTER);
    private final static JLabel cardDrawButton = new JLabel("", JLabel.CENTER);
    private final static JLabel wheelButton = new JLabel("", JLabel.CENTER);
    private final static JLabel musicPlayerButton = new JLabel("", JLabel.CENTER);
    private static final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    private final static JLabel[] labelList = {clockButton, cardDrawButton, wheelButton, musicPlayerButton};
    private final static java.util.List<int[]> locationList = new ArrayList<>();
    private static boolean isPlaying = false;
    private static final SystemTray mainTray;
    private static final PopupMenu trayMenu = new PopupMenu();
    private static boolean isHidden = false;
    static {
        locationList.add(new int[]{15, 15});
        locationList.add(new int[]{205, 15});
        locationList.add(new int[]{15, 205});
        locationList.add(new int[]{205, 205});
        for (int i=0;i<labelList.length;i++) {
            JLabel label = labelList[i];
            Container container = toolboxFrame.getContentPane();
            URL url = classLoader.getResource("wheel.png");
            ImageIcon icon = new ImageIcon(url);
            label.setOpaque(true);
            label.setForeground(Color.WHITE);
            label.setBackground(Color.BLACK);
            label.setBounds(locationList.get(i)[0], locationList.get(i)[1], 180, 180);
            label.setBorder(BorderFactory.createLineBorder(new Color(10,10,10), 7, true));
            Image image = icon.getImage().getScaledInstance(100, 100, Image.SCALE_DEFAULT); // 这里设置缩放尺寸
            ImageIcon scaledIcon = new ImageIcon(image);
            label.setIcon(scaledIcon);
        }
        mainTray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().getImage(classLoader.getResource("xxyxxdmc.png"));
        TrayIcon trayIcon = new TrayIcon(image, DataJsonReader.getLanguageObject().get("toolbox").getAsString());
        trayIcon.setImageAutoSize(true);
        try {mainTray.add(trayIcon);} catch (AWTException ignored) {}
        MenuItem exitItem = new MenuItem(DataJsonReader.getLanguageObject().get("exit").getAsString());
        exitItem.addActionListener(e -> {
            if (!isHidden) hideFrame(true);
            else System.exit(0);
        });
        trayMenu.add(exitItem);
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
        toolboxFrame.setLayout(null);
        toolboxFrame.setVisible(true);
        for (JLabel label : labelList) {
            toolboxFrame.add(label);
        }
        mainTray.getTrayIcons()[0].addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1) return;
                if (isPlaying) return;
                if (isHidden) showFrame();
                else hideFrame(false);
            }
        });
    }
    public static void main(String[] args) {
        showFrame();
        showAllLabel();
        mainTray.getTrayIcons()[0].setPopupMenu(trayMenu);
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
        if (isPlaying) return;
        isPlaying =true;
        isHidden=false;
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
                isPlaying =false;
                ((Timer) e.getSource()).stop();
            }
        }).start();
    }
    public static void hideFrame(boolean exitToolbox) {
        if (isPlaying && !exitToolbox) return;
        isPlaying =true;
        isHidden=true;
        new Timer(20, e -> {
            if (frameTime<1) {
                frameTime+=0.04;
                int side = 400 - (int) Math.ceil(100*EasingFunctions.easeOutExpo(frameTime));
                toolboxFrame.setSize(side, side);
                toolboxFrame.setShape(new RoundRectangle2D.Float(0, 0, side, side, 20, 20));
                toolboxFrame.setOpacity(1 - (float) EasingFunctions.easeOutExpo(frameTime));
                toolboxFrame.setLocationRelativeTo(null);
            } else {
                frameTime=0;
                isPlaying =false;
                ((Timer) e.getSource()).stop();
                if (exitToolbox) System.exit(0);
            }
        }).start();
    }
}
