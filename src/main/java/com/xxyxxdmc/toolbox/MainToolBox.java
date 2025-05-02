package main.java.com.xxyxxdmc.toolbox;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static main.java.com.xxyxxdmc.toolbox.FloatingClockTray.*;
import static main.java.com.xxyxxdmc.toolbox.SomeFunctions.createRow;
import static main.java.com.xxyxxdmc.toolbox.SomeFunctions.createSlider;

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
    private final static String[] iconList = {"clock", "card", "wheel", "player"};
    public static boolean[] runningClass = {false, false, false, false};
    private static Menu clockMenu;
    private static boolean isPlaying = false;
    private static final SystemTray mainTray;
    private static final PopupMenu trayMenu = new PopupMenu();
    private static boolean isHidden = false;
    public static Menu sleepMenu , lowSleepMenu;
    // 主要是关于Label和UI的静态更新
    static {
        locationList.add(new int[]{15, 15});
        locationList.add(new int[]{205, 15});
        locationList.add(new int[]{15, 205});
        locationList.add(new int[]{205, 205});
        for (int i=0;i<labelList.length;i++) {
            JLabel label = labelList[i];
            URL url = classLoader.getResource(String.format("icons/%s.png", iconList[i]));
            assert url != null;
            ImageIcon icon = new ImageIcon(url);
            label.setOpaque(true);
            label.setForeground(Color.WHITE);
            label.setBackground(Color.BLACK);
            label.setBounds(locationList.get(i)[0], locationList.get(i)[1], 180, 180);
            label.setBorder(BorderFactory.createLineBorder(new Color(10,10,10), 7, true));
            Image image = icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(image);
            label.setIcon(scaledIcon);
            int finalI = i;
            label.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    switch (iconList[finalI]) {
                        case "clock" -> {
                            if (!runningClass[0]) {
                                runningClass[0]=true;
                                FloatingClockTray.main(null);
                                trayMenu.add(clockMenu);
                            } else {
                                runningClass[0]=false;
                                trayMenu.remove(clockMenu);
                                clockFrame.dispose();
                                clockFrame.removeAll();
                                isClockHidden=true;
                                try {
                                    GlobalScreen.unregisterNativeHook();
                                } catch (NativeHookException ignored) {}
                                System.gc();
                            }
                        }
                        default -> {}
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {

                }

                @Override
                public void mouseExited(MouseEvent e) {

                }
                @Override
                public void mousePressed(MouseEvent e) {}
                @Override
                public void mouseReleased(MouseEvent e) {}
            });
        }
        mainTray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().getImage(classLoader.getResource("icons/xxyxxdmc.png"));
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
    //主要是关于小工具菜单的初始化
    static {
        JsonObject languageObject = DataJsonReader.getLanguageObject();
        JsonObject dataObject = DataJsonReader.getDataObject();
        Gson dataGson = DataJsonReader.getDataGson();
        File jsonFile = new File(DataJsonReader.getJsonPath());
        clockMenu = new Menu(languageObject.get("clock").getAsString());
        MenuItem settingItem = new MenuItem(languageObject.get("setting").getAsString());
        settingItem.addActionListener(e -> {
            JFrame settingFrame = new JFrame(languageObject.get("setting").getAsString());
            settingFrame.setSize(350, 200);
            settingFrame.setLocationRelativeTo(null); // 居中显示
            settingFrame.setResizable(false);
            settingFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

            JLabel waitLabel = new JLabel(languageObject.get("wait_time").getAsString());
            JSlider waitSlider = createSlider(5, 240, waitTimeAll);
            JLabel waitValueLabel = new JLabel(String.valueOf(waitTimeAll));
            waitSlider.addChangeListener(e1 -> waitValueLabel.setText(String.valueOf(waitSlider.getValue())));

            JLabel fullWaitLabel = new JLabel(languageObject.get("full_wait_time").getAsString());
            JSlider fullWaitSlider = createSlider(10, 720, fullWaitTimeAll);
            JLabel fullWaitValueLabel = new JLabel(String.valueOf(fullWaitTimeAll));
            fullWaitSlider.addChangeListener(e2 -> fullWaitValueLabel.setText(String.valueOf(fullWaitSlider.getValue())));

            JLabel textSizeLabel = new JLabel(languageObject.get("text_size").getAsString());
            JSlider textSizeSlider = createSlider(100, 300, textSizeAll);
            JLabel textSizeValueLabel = new JLabel(String.valueOf(textSizeAll));
            textSizeSlider.addChangeListener(e2 -> textSizeValueLabel.setText(String.valueOf(textSizeSlider.getValue())));

            JLabel colorLabel = new JLabel(languageObject.get("color").getAsString());
            JPanel colorPanel = new JPanel();
            colorPanel.setBackground(new Color(R, G, B));
            colorPanel.setPreferredSize(new Dimension(50, 30));
            JButton colorButton = new JButton(languageObject.get("color").getAsString());
            colorButton.addActionListener(colorE -> {
                Color selectedColor = JColorChooser.showDialog(clockFrame, languageObject.get("choose_color").getAsString(), colorPanel.getBackground());
                if (selectedColor != null) {
                    colorPanel.setBackground(selectedColor);
                }
            });

            settingFrame.add(mainPanel);
            settingFrame.setVisible(true);

            mainPanel.add(createRow(waitLabel, waitSlider, waitValueLabel));
            mainPanel.add(createRow(fullWaitLabel, fullWaitSlider, fullWaitValueLabel));
            mainPanel.add(createRow(colorLabel, colorButton, colorPanel));
            mainPanel.add(createRow(textSizeLabel, textSizeSlider, textSizeValueLabel));
            CPUControlRow cpuControl = new CPUControlRow();
            mainPanel.add(cpuControl);

            settingFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    Color color = colorPanel.getBackground();
                    String hex = String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
                    String waitText = String.valueOf(waitSlider.getValue());
                    String fullWaitText = String.valueOf(fullWaitSlider.getValue());
                    String textSizeText = String.valueOf(textSizeSlider.getValue());
                    dataObject.addProperty("color", hex);
                    dataObject.addProperty("full_screen_wait_time", fullWaitText);
                    dataObject.addProperty("wait_time", waitText);
                    dataObject.addProperty("text_size", textSizeText);
                    try (FileWriter writer = new FileWriter(jsonFile)) {
                        dataGson.toJson(dataObject, writer);
                    } catch (Exception ignored) {}
                    fullWaitTimeAll= Integer.parseInt(fullWaitText);
                    waitTimeAll= Integer.parseInt(waitText);
                    R=color.getRed();
                    G=color.getGreen();
                    B=color.getBlue();
                    timeLabel.setForeground(new Color(R, G, B));
                }
            });
        });
        clockMenu.add(settingItem);
        MenuItem countdownItem = new MenuItem(languageObject.get("countdown").getAsString());
        countdownItem.addActionListener(e -> {
            countdown=!countdown;
            if (!countdown) countdownItem.setLabel(languageObject.get("countdown").getAsString());
            else countdownItem.setLabel(languageObject.get("countdown").getAsString()+" √");
        });
        clockMenu.add(countdownItem);
        MenuItem fullScreenItem = new MenuItem(languageObject.get("full_screen").getAsString());
        fullScreenItem.addActionListener(e -> {
            ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
            if (isClockHidden) showTheFrame();
            scheduledExecutorService.schedule(() -> {
                isClockRunning =false;
                isFullScreen=true;
                mainTime =0;
                enterFullScreen();
            },2, TimeUnit.SECONDS);
        });
        clockMenu.add(fullScreenItem);
        String language = dataObject.get("language").getAsString();
        Menu languageMenu = new Menu(languageObject.get("language").getAsString());
        MenuItem englishTL = new MenuItem("English"+(language.equals("en_us")?" √":""));
        MenuItem chineseTL = new MenuItem("中文"+(language.equals("zh_cn")?" √":""));
        MenuItem japaneseTL = new MenuItem("日本語"+(language.equals("ja_jp")?" √":""));
        languageMenu.add(englishTL);
        languageMenu.add(chineseTL);
        languageMenu.add(japaneseTL);
        englishTL.addActionListener(e -> {
            if (englishTL.getLabel().contains("√")) return;
            dataObject.addProperty("language", "en_us");
            try (FileWriter writer = new FileWriter(jsonFile)) {
                dataGson.toJson(dataObject, writer);
            } catch (Exception ignored) {}
            englishTL.setLabel("English √");
            chineseTL.setLabel("中文");
            japaneseTL.setLabel("日本語");
            JOptionPane.showMessageDialog(null, languageObject.get("restart_info").getAsString());
        });chineseTL.addActionListener(e -> {
            if (chineseTL.getLabel().contains("√")) return;
            dataObject.addProperty("language", "zh_cn");
            try (FileWriter writer = new FileWriter(jsonFile)) {
                dataGson.toJson(dataObject, writer);
            } catch (Exception ignored) {}
            englishTL.setLabel("English");
            chineseTL.setLabel("中文 √");
            japaneseTL.setLabel("日本語");
            JOptionPane.showMessageDialog(null, languageObject.get("restart_info").getAsString());
        });japaneseTL.addActionListener(e -> {
            if (japaneseTL.getLabel().contains("√")) return;
            dataObject.addProperty("language", "ja_jp");
            try (FileWriter writer = new FileWriter(jsonFile)) {
                dataGson.toJson(dataObject, writer);
            } catch (Exception ignored) {}
            englishTL.setLabel("English");
            chineseTL.setLabel("中文");
            japaneseTL.setLabel("日本語 √");
            JOptionPane.showMessageDialog(null, languageObject.get("restart_info").getAsString());
        });
        sleepMenu = new Menu(languageObject.get("sleep").getAsString());
        lowSleepMenu = new Menu(languageObject.get("low_sleep").getAsString());
        int[] sleepList = {10,20,30,40,45,50,60};
        MenuItem sleepCustom = new MenuItem(languageObject.get("sleep_custom_time").getAsString());
        for (int i : sleepList) {
            MenuItem sleepItem = new MenuItem(String.format(languageObject.get("sleep_in_time").getAsString(),i));
            sleepItem.addActionListener(e -> {
                sleepInTime(i);
                sleepMenu.setEnabled(false);
                lowSleepMenu.setEnabled(false);
            });
            sleepMenu.add(sleepItem);
        }
        sleepMenu.add(sleepCustom);
        sleepCustom.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(null, "Please enter minutes", "Custom", JOptionPane.PLAIN_MESSAGE);
            if (input==null) return;
            sleepInTime(Integer.parseInt(input));
            sleepMenu.setEnabled(false);
            lowSleepMenu.setEnabled(false);
        });
        int[] lowSleepList = {10,20,30,40,45,50,60};
        MenuItem lowSleepCustom = new MenuItem(languageObject.get("low_sleep_custom_time").getAsString());
        for (int i : lowSleepList) {
            MenuItem lowSleepItem = new MenuItem(String.format(languageObject.get("low_sleep_in_time").getAsString(), i));
            lowSleepItem.addActionListener(e -> {
                lowSleepInTime(i);
                sleepMenu.setEnabled(false);
                lowSleepMenu.setEnabled(false);
            });
            lowSleepMenu.add(lowSleepItem);
        }
        lowSleepMenu.add(lowSleepCustom);
        lowSleepCustom.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(null, "Please enter minutes", "Custom", JOptionPane.PLAIN_MESSAGE);
            if (input==null) return;
            lowSleepInTime(Integer.parseInt(input));
            sleepMenu.setEnabled(false);
            lowSleepMenu.setEnabled(false);
        });
        clockMenu.add(sleepMenu);
        clockMenu.add(lowSleepMenu);
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
