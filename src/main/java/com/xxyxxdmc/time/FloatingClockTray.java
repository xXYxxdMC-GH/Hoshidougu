package main.java.com.xxyxxdmc.time;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.*;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FloatingClockTray implements NativeKeyListener, NativeMouseListener, NativeMouseInputListener, NativeMouseWheelListener {
    private static final JFrame frame = new JFrame("Clock");
    private static JLabel timeLabel, weekLabel;
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static boolean isHidden = false, isRunning = false, countdown = false, timeToShow = false, isFullScreen = false;
    private static int y = 50;
    private static double timer = 0;
    private static int fullScreenTimer = 0;
    private static int width = 300, height = 120; // 初始大小
    private static Timer resizeTimer;
    private static int waitTimeAll, fullWaitTimeAll;
    public static int R, G, B;
    private static Timer mainTimer;
    private static PopupMenu popupMenu;
    private static MenuItem sleep10, sleep20, sleep30, sleep40, sleep45, sleep50, sleep60, sleepCustom;
    private static MenuItem lowSleep10, lowSleep20, lowSleep30, lowSleep40, lowSleep45, lowSleep50, lowSleep60, lowSleepCustom;
    private static Menu sleepMenu, lowSleepMenu;
    private static SystemTray tray;
    private static Font minecraftFont;

    public FloatingClockTray() {
    }
    static {
        InputStream fontStream = FloatingClockTray.class.getResourceAsStream("/minecraft.ttf");
        assert fontStream != null;
        try {
            minecraftFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
        } catch (FontFormatException | IOException ignored) {}
    }

    public static void main(String[] args) throws Exception {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        if (!SystemTray.isSupported()) {
            throw new Exception();
        }
        try {
            GlobalScreen.registerNativeHook();
        } catch (Exception ignored) {}
        GlobalScreen.addNativeKeyListener(new FloatingClockTray());
        GlobalScreen.addNativeMouseListener(new FloatingClockTray());
        GlobalScreen.addNativeMouseMotionListener(new FloatingClockTray());
        GlobalScreen.addNativeMouseWheelListener(new FloatingClockTray());

        Path jarDir = Paths.get(System.getProperty("user.dir"));
        Path jsonPath = jarDir.resolve("data.json");
        if (!Files.exists(jsonPath)) {
            createDefaultJSON(jsonPath);
        }
        String content = new String(Files.readAllBytes(jsonPath));
        JSONObject json = new JSONObject(content);
        String language = json.getString("language");
        String color = json.getString("color");
        int waitTime = json.getInt("wait_time");
        int fullWaitTime = json.getInt("full_screen_wait_time");
        waitTimeAll=waitTime;
        fullWaitTimeAll=fullWaitTime;
        R=Color.decode(color).getRed();
        G=Color.decode(color).getGreen();
        B=Color.decode(color).getBlue();

        InputStream languageStream = FloatingClockTray.class.getResourceAsStream(String.format("/%s.json", language));
        assert languageStream != null;
        String languageText = new Scanner(languageStream, StandardCharsets.UTF_8).useDelimiter("\\A").next();
        JSONObject languageObject = new JSONObject(languageText);

        tray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().getImage(FloatingClockTray.class.getResource("/xxyxxdmc.png"));

        TrayIcon trayIcon = new TrayIcon(image, languageObject.getString("clock"));
        trayIcon.setImageAutoSize(true);
        tray.add(trayIcon);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setUndecorated(true); // 无边框
        frame.setAlwaysOnTop(true); // 置顶
        frame.setSize(width, height);
        frame.setLocation((screenSize.width - frame.getWidth()) / 2, y);
        frame.setType(Window.Type.UTILITY);

        frame.setShape(new RoundRectangle2D.Float(0, 0, width, height, 30, 30));

        timeLabel = new JLabel("", SwingConstants.CENTER);

        timeLabel.setFont(minecraftFont.deriveFont(Font.PLAIN, 48));
        frame.add(timeLabel);
        frame.getContentPane().setBackground(Color.BLACK);
        timeLabel.setForeground(new Color(R, G, B));

        // 关闭窗口时隐藏
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        // 定时更新时间
        Timer timer = new Timer(1000, e -> updateTime());
        timer.start();

        frame.setVisible(true);
        frame.setFocusableWindowState(false);
        frame.setEnabled(false);

        // 托盘菜单
        popupMenu = new PopupMenu();
        MenuItem settingItem = new MenuItem(languageObject.getString("setting"));
        settingItem.addActionListener(e -> {
            JFrame settingFrame = new JFrame(languageObject.getString("setting"));
            settingFrame.setSize(300, 150);
            settingFrame.setLocationRelativeTo(null); // 居中显示
            settingFrame.setResizable(false);
            settingFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

            JLabel waitLabel = new JLabel(languageObject.getString("wait_time"));
            JSlider waitSlider = createSlider(5, 240, waitTimeAll);
            JLabel waitValueLabel = new JLabel(String.valueOf(waitTimeAll));
            waitSlider.addChangeListener(e1 -> waitValueLabel.setText(String.valueOf(waitSlider.getValue())));
            waitSlider.setPaintLabels(false);
            waitSlider.setPaintTicks(false);

            JLabel fullWaitLabel = new JLabel(languageObject.getString("full_wait_time"));
            JSlider fullWaitSlider = createSlider(2, 720, fullWaitTimeAll);
            JLabel fullWaitValueLabel = new JLabel(String.valueOf(fullWaitTimeAll));
            fullWaitSlider.addChangeListener(e2 -> fullWaitValueLabel.setText(String.valueOf(fullWaitSlider.getValue())));
            fullWaitSlider.setPaintLabels(false);
            fullWaitSlider.setPaintTicks(false);

            JLabel colorLabel = new JLabel(languageObject.getString("color"));
            JPanel colorPanel = new JPanel();
            colorPanel.setBackground(new Color(R, G, B));
            colorPanel.setPreferredSize(new Dimension(50, 30));
            JButton colorButton = new JButton(languageObject.getString("color"));
            colorButton.addActionListener(colorE -> {
                Color selectedColor = JColorChooser.showDialog(frame, languageObject.getString("choose_color"), colorPanel.getBackground());
                if (selectedColor != null) {
                    colorPanel.setBackground(selectedColor);
                }
            });

            settingFrame.add(mainPanel);
            settingFrame.setVisible(true);

            mainPanel.add(createRow(waitLabel, waitSlider, waitValueLabel));
            mainPanel.add(createRow(fullWaitLabel, fullWaitSlider, fullWaitValueLabel));
            mainPanel.add(createRow(colorLabel, colorButton, colorPanel));

            settingFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    Color color = colorPanel.getBackground();
                    String hex = String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
                    String waitText = String.valueOf(waitSlider.getValue());
                    String fullWaitText = String.valueOf(fullWaitSlider.getValue());
                    Path jarDir = Paths.get(System.getProperty("user.dir"));
                    Path jsonPath = jarDir.resolve("data.json");
                    String defaultJSON = String.format("{\n  \"language\": \"%s\",\n  \"color\": \"%s\",\n  \"full_screen_wait_time\": %s,\n  \"wait_time\": %s\n}", language, hex, fullWaitText, waitText);
                    try {Files.write(jsonPath, defaultJSON.getBytes());} catch (IOException ignored) {}
                    fullWaitTimeAll= Integer.parseInt(fullWaitText);
                    waitTimeAll= Integer.parseInt(waitText);
                    R=color.getRed();
                    G=color.getGreen();
                    B=color.getBlue();
                    timeLabel.setForeground(new Color(R, G, B));
                }
            });
        });
        popupMenu.add(settingItem);

        MenuItem countdownItem = new MenuItem(languageObject.getString("countdown"));
        countdownItem.addActionListener(e -> {
            countdown=!countdown;
            if (!countdown) countdownItem.setLabel(languageObject.getString("countdown"));
            else countdownItem.setLabel(languageObject.getString("countdown")+languageObject.getString("yes"));
        });
        MenuItem fullScreenItem = new MenuItem(languageObject.getString("full_screen"));
        fullScreenItem.addActionListener(e -> {
            ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3);
            scheduledExecutorService.schedule(() -> {
                isRunning=false;
                isFullScreen=true;
                fullScreenTimer=0;
                enterFullScreen();
            },5,TimeUnit.SECONDS);
        });
        popupMenu.add(fullScreenItem);
        Menu languageMenu = new Menu(languageObject.getString("language"));
        MenuItem englishTL = new MenuItem("English");
        MenuItem chineseTL = new MenuItem("中文");
        MenuItem japaneseTL = new MenuItem("日本語");
        languageMenu.add(englishTL);
        languageMenu.add(chineseTL);
        languageMenu.add(japaneseTL);
        englishTL.addActionListener(e -> {
            String defaultJSON = String.format("{\n  \"language\": \"en_us\",\n  \"color\": \"%s\",\n  \"full_screen_wait_time\": %s,\n  \"wait_time\": %s\n}", color, fullWaitTimeAll, waitTimeAll);
            try {Files.write(jsonPath, defaultJSON.getBytes());} catch (IOException ignored) {}
            popupMenu.remove(languageMenu);
            JOptionPane.showMessageDialog(null, languageObject.getString("restart_info"));
        });chineseTL.addActionListener(e -> {
            String defaultJSON = String.format("{\n  \"language\": \"zh_cn\",\n  \"color\": \"%s\",\n  \"full_screen_wait_time\": %s,\n  \"wait_time\": %s\n}", color, fullWaitTimeAll, waitTimeAll);
            try {Files.write(jsonPath, defaultJSON.getBytes());} catch (IOException ignored) {}
            popupMenu.remove(languageMenu);
            JOptionPane.showMessageDialog(null, languageObject.getString("restart_info"));
        });japaneseTL.addActionListener(e -> {
            String defaultJSON = String.format("{\n  \"language\": \"ja_jp\",\n  \"color\": \"%s\",\n  \"full_screen_wait_time\": %s,\n  \"wait_time\": %s\n}", color, fullWaitTimeAll, waitTimeAll);
            try {Files.write(jsonPath, defaultJSON.getBytes());} catch (IOException ignored) {}
            popupMenu.remove(languageMenu);
            JOptionPane.showMessageDialog(null, languageObject.getString("restart_info"));
        });
        popupMenu.add(languageMenu);

        sleepMenu = new Menu(languageObject.getString("sleep"));
        sleep10 = new MenuItem(String.format(languageObject.getString("sleep_in_time"), "10"));
        sleep20 = new MenuItem(String.format(languageObject.getString("sleep_in_time"), "20"));
        sleep30 = new MenuItem(String.format(languageObject.getString("sleep_in_time"), "30"));
        sleep40 = new MenuItem(String.format(languageObject.getString("sleep_in_time"), "40"));
        sleep45 = new MenuItem(String.format(languageObject.getString("sleep_in_time"), "45"));
        sleep50 = new MenuItem(String.format(languageObject.getString("sleep_in_time"), "50"));
        sleep60 = new MenuItem(String.format(languageObject.getString("sleep_in_time"), "60"));
        sleepCustom = new MenuItem(languageObject.getString("sleep_custom_time"));
        sleepMenu.add(sleep10);
        sleepMenu.add(sleep20);
        sleepMenu.add(sleep30);
        sleepMenu.add(sleep40);
        sleepMenu.add(sleep45);
        sleepMenu.add(sleep50);
        sleepMenu.add(sleep60);
        sleepMenu.add(sleepCustom);
        popupMenu.add(sleepMenu);
        sleep10.addActionListener(e -> {
            sleepInTime(10);
            popupMenu.remove(sleepMenu);
            popupMenu.remove(lowSleepMenu);
            tray.getTrayIcons()[0].setPopupMenu(popupMenu);
        });
        sleep20.addActionListener(e -> {
            sleepInTime(20);
            popupMenu.remove(sleepMenu);
            popupMenu.remove(lowSleepMenu);
            tray.getTrayIcons()[0].setPopupMenu(popupMenu);
        });
        sleep30.addActionListener(e -> {
            sleepInTime(30);
            popupMenu.remove(sleepMenu);
            popupMenu.remove(lowSleepMenu);
            tray.getTrayIcons()[0].setPopupMenu(popupMenu);
        });
        sleep40.addActionListener(e -> {
            sleepInTime(40);
            popupMenu.remove(sleepMenu);
            popupMenu.remove(lowSleepMenu);
            tray.getTrayIcons()[0].setPopupMenu(popupMenu);
        });
        sleep45.addActionListener(e -> {
            sleepInTime(40);
            popupMenu.remove(sleepMenu);
            popupMenu.remove(lowSleepMenu);
            tray.getTrayIcons()[0].setPopupMenu(popupMenu);
        });
        sleep50.addActionListener(e -> {
            sleepInTime(40);
            popupMenu.remove(sleepMenu);
            popupMenu.remove(lowSleepMenu);
            tray.getTrayIcons()[0].setPopupMenu(popupMenu);
        });
        sleep60.addActionListener(e -> {
            sleepInTime(60);
            popupMenu.remove(sleepMenu);
            popupMenu.remove(lowSleepMenu);
            tray.getTrayIcons()[0].setPopupMenu(popupMenu);
        });
        sleepCustom.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(null, "Please enter minutes", "Custom", JOptionPane.PLAIN_MESSAGE);
            if (input==null) return;
            sleepInTime(Integer.parseInt(input));
            popupMenu.remove(sleepMenu);
            popupMenu.remove(lowSleepMenu);
            tray.getTrayIcons()[0].setPopupMenu(popupMenu);
        });
        lowSleepMenu = new Menu(languageObject.getString("low_sleep"));
        lowSleep10 = new MenuItem(String.format(languageObject.getString("low_sleep_in_time"), "10"));
        lowSleep20 = new MenuItem(String.format(languageObject.getString("low_sleep_in_time"), "20"));
        lowSleep30 = new MenuItem(String.format(languageObject.getString("low_sleep_in_time"), "30"));
        lowSleep40 = new MenuItem(String.format(languageObject.getString("low_sleep_in_time"), "40"));
        lowSleep45 = new MenuItem(String.format(languageObject.getString("low_sleep_in_time"), "45"));
        lowSleep50 = new MenuItem(String.format(languageObject.getString("low_sleep_in_time"), "50"));
        lowSleep60 = new MenuItem(String.format(languageObject.getString("low_sleep_in_time"), "60"));
        lowSleepCustom = new MenuItem(languageObject.getString("low_sleep_custom_time"));
        lowSleepMenu.add(lowSleep10);
        lowSleepMenu.add(lowSleep20);
        lowSleepMenu.add(lowSleep30);
        lowSleepMenu.add(lowSleep40);
        lowSleepMenu.add(lowSleep45);
        lowSleepMenu.add(lowSleep50);
        lowSleepMenu.add(lowSleep60);
        lowSleepMenu.add(lowSleepCustom);
        lowSleep10.addActionListener(e -> {
            lowSleepInTime(10);
            popupMenu.remove(sleepMenu);
            popupMenu.remove(lowSleepMenu);
            tray.getTrayIcons()[0].setPopupMenu(popupMenu);
        });lowSleep20.addActionListener(e -> {
            lowSleepInTime(20);
            popupMenu.remove(sleepMenu);
            popupMenu.remove(lowSleepMenu);
            tray.getTrayIcons()[0].setPopupMenu(popupMenu);
        });lowSleep30.addActionListener(e -> {
            lowSleepInTime(30);
            popupMenu.remove(sleepMenu);
            popupMenu.remove(lowSleepMenu);
            tray.getTrayIcons()[0].setPopupMenu(popupMenu);
        });lowSleep40.addActionListener(e -> {
            lowSleepInTime(40);
            popupMenu.remove(sleepMenu);
            popupMenu.remove(lowSleepMenu);
            tray.getTrayIcons()[0].setPopupMenu(popupMenu);
        });lowSleep45.addActionListener(e -> {
            lowSleepInTime(45);
            popupMenu.remove(sleepMenu);
            popupMenu.remove(lowSleepMenu);
            tray.getTrayIcons()[0].setPopupMenu(popupMenu);
        });lowSleep50.addActionListener(e -> {
            lowSleepInTime(50);
            popupMenu.remove(sleepMenu);
            popupMenu.remove(lowSleepMenu);
            tray.getTrayIcons()[0].setPopupMenu(popupMenu);
        });lowSleep60.addActionListener(e -> {
            lowSleepInTime(60);
            popupMenu.remove(sleepMenu);
            popupMenu.remove(lowSleepMenu);
            tray.getTrayIcons()[0].setPopupMenu(popupMenu);
        });lowSleepCustom.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(null, "Please enter minutes", "Custom", JOptionPane.PLAIN_MESSAGE);
            if (input==null) return;
            lowSleepInTime(Integer.parseInt(input));
            popupMenu.remove(sleepMenu);
            popupMenu.remove(lowSleepMenu);
            tray.getTrayIcons()[0].setPopupMenu(popupMenu);
        });
        popupMenu.add(lowSleepMenu);

        popupMenu.add(countdownItem);

        MenuItem exitItem = new MenuItem(languageObject.getString("exit"));
        exitItem.addActionListener(e -> System.exit(0));
        popupMenu.add(exitItem);

        trayIcon.setPopupMenu(popupMenu);

        mainTimer = new Timer(1000, e -> {
            if (!isFullScreen) {
                fullScreenTimer++;
                if (fullScreenTimer==fullWaitTimeAll) {
                    enterFullScreen();
                    isFullScreen=true;
                }
            }
        });

        JLabel MSG = new JLabel(languageObject.getString("ask_sentence"));
        boolean result = showConfirmDialogWithTimeout(MSG, languageObject.getString("ask"), 5 * 1000);

        if (!result) {
            lowSleepInTime(40);
            popupMenu.remove(sleepMenu);
            popupMenu.remove(lowSleepMenu);
            tray.getTrayIcons()[0].setPopupMenu(popupMenu);
        } else mainTimer.start();
    }

    private static JSlider createSlider(int min, int max, int value) {
        JSlider slider = new JSlider(min, max, value);
        slider.setMajorTickSpacing(50);
        slider.setMinorTickSpacing(5);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setPreferredSize(new Dimension(200, 50)); // 增加宽度
        return slider;
    }

    private static JPanel createRow(JComponent label, JComponent input, JComponent value) {
        JPanel rowPanel = new JPanel(new BorderLayout(10, 10));
        rowPanel.add(label, BorderLayout.WEST);
        rowPanel.add(input, BorderLayout.CENTER);
        rowPanel.add(value, BorderLayout.EAST);
        return rowPanel;
    }

    private static void updateTime() {
        String time;
        LocalTime now = LocalTime.now();
        if (countdown) {
            LocalTime targetTime = LocalTime.of(17, 40, 0); // 倒计时目标时间
            if (now.isBefore(targetTime)) {
                Duration duration = Duration.between(now, targetTime);
                long hours = duration.toHours();
                long minutes = duration.toMinutes() % 60;
                long seconds = duration.getSeconds() % 60;
                time = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            } else {
                time = "00:00:00";
            }
        } else {
            time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        }
        timeLabel.setText(time);
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent event) {
        if (mainTimer==null) return;
        timeToShow=false;
        resetTimer();
    }

    @Override
    public void nativeMousePressed(NativeMouseEvent event) {
        if (mainTimer==null) return;
        timeToShow=false;
        resetTimer();
    }

    @Override
    public void nativeMouseMoved(NativeMouseEvent event) {
        if (mainTimer==null) return;
        timeToShow=false;
        resetTimer();
    }

    @Override
    public void nativeMouseDragged(NativeMouseEvent event) {
        if (mainTimer==null) return;
        timeToShow=false;
        resetTimer();
    }

    public static void sleepInTime(int minutes) {
        scheduler.shutdownNow();
        mainTimer.stop();
        timer=0;
        fullScreenTimer=0;
        isFullScreen=false;
        isRunning=true;
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(() -> {
            isRunning=false;
            resetTimer();
            popupMenu.add(sleepMenu);
            popupMenu.add(lowSleepMenu);
            tray.getTrayIcons()[0].setPopupMenu(popupMenu);
        }, minutes, TimeUnit.MINUTES);
    }

    public static void lowSleepInTime(int minutes) {
        mainTimer.stop();
        fullScreenTimer=0;
        isFullScreen=false;
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        scheduler.schedule(() -> {
            popupMenu.add(lowSleepMenu);
            popupMenu.add(sleepMenu);
            tray.getTrayIcons()[0].setPopupMenu(popupMenu);
        }, minutes, TimeUnit.MINUTES);
    }

    public static double easeInBack(double x) {
        double c1 = 1.70158;
        double c3 = c1 + 1;
        return c3 * x * x * x - c1 * x * x;
    }
    public static double easeOutExpo(double x){
        return x == 1 ? 1 : 1 - Math.pow(2, -10 * x);
    }
    private static void resetTimer(){
        if (isRunning) return;
        isRunning=true;
        fullScreenTimer=0;
        if (isFullScreen) {
            isRunning=false;
            exitFullScreen();
            isFullScreen=false;
            return;
        }
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        if (!isHidden) {
            mainTimer.stop();
            Timer swingTimer = new Timer(20, e -> {
                if (timer<1){
                    timer += 0.04;
                    y = 50 + (int) ((-120 - 50) * easeInBack(timer));
                    frame.setLocation((screenSize.width - frame.getWidth()) / 2, y);
                } else {
                    timer=0;
                    isHidden = true;
                    isRunning=false;
                    frame.setVisible(false);
                    ((Timer) e.getSource()).stop();
                }
            });
            swingTimer.start();
        }
        scheduler.shutdownNow();
        scheduler = Executors.newScheduledThreadPool(1);
        timeToShow=true;
        scheduler.schedule(() -> {
            if (!timeToShow) {
                isRunning=false;
                resetTimer();
                return;
            }
            mainTimer.start();
            Timer swingTimer = new Timer(20, e -> {
                if (timer<1){
                    timer += 0.04;
                    y = -120 + (int) ((50 + 120) * easeOutExpo(timer));
                    frame.setLocation((screenSize.width - frame.getWidth()) / 2, y);
                } else {
                    timer=0;
                    isHidden = false;
                    isRunning=false;
                    ((Timer) e.getSource()).stop();
                }
            });
            swingTimer.start();
            frame.setVisible(true);

        }, (waitTimeAll* 1000L)/2, TimeUnit.MILLISECONDS);
    }

    private static void enterFullScreen(){
        if (isRunning) return;
        isRunning=true;
        mainTimer.stop();
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        resizeTimer = new Timer(20, e -> {
            if (timer < 1) {
                timer += 0.02;
                width = (int) Math.ceil(300 + ((screenSize.width - 300) * easeOutExpo(timer)));
                height = (int) Math.ceil(120 +((screenSize.height - 120) * easeOutExpo(timer)));
                y = 50 + (int) (((double) ((screenSize.height - frame.getHeight()) / 2) - 50) * easeOutExpo(timer));
                frame.setLocation((screenSize.width - frame.getWidth()) / 2, y);
                frame.setPreferredSize(new Dimension(width, height));
                frame.pack();
                frame.setShape(new RoundRectangle2D.Float(0, 0, width, height, 0, 0));
                timeLabel.setFont(minecraftFont.deriveFont(Font.PLAIN, (float) (48 + ((260 - 48) * easeOutExpo(timer)))));
            } else {
                frame.setLocation(((screenSize.width - frame.getWidth()) / 2), ((screenSize.height - frame.getHeight()) / 2));
                frame.setPreferredSize(screenSize);
                frame.pack();
                frame.setShape(new RoundRectangle2D.Double(0, 0, screenSize.width, screenSize.height, 30 * (1-easeOutExpo(timer)), 30 * (1-easeOutExpo(timer))));
                timer=0;
                isRunning=false;
                ((Timer) e.getSource()).stop();
            }
        });
        resizeTimer.setRepeats(true);
        resizeTimer.start();
    }

    private static void exitFullScreen(){
        if (isRunning) return;
        isRunning=true;
        mainTimer.start();
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        resizeTimer = new Timer(20, e -> {
            if (timer < 1) {
                timer += 0.02;
                width = (int) Math.ceil(screenSize.width + ((300 - screenSize.width) * easeOutExpo(timer)));
                height = (int) Math.ceil(screenSize.height + ((120 - screenSize.height) * easeOutExpo(timer)));
                y = (screenSize.height - frame.getHeight()) / 2 + (int) ((50 - (double) (screenSize.height - frame.getHeight()) / 2) * easeOutExpo(timer));
                frame.setLocation((screenSize.width - frame.getWidth()) / 2, y);
                frame.setPreferredSize(new Dimension(width, height));
                frame.pack();
                frame.setShape(new RoundRectangle2D.Double(0, 0, width, height, 30 * easeOutExpo(timer), 30 * easeOutExpo(timer)));
                timeLabel.setFont(minecraftFont.deriveFont(Font.PLAIN, (float) (260 + ((48 - 260) * easeOutExpo(timer)))));
            } else {
                timer=0;
                isRunning=false;
                frame.setShape(new RoundRectangle2D.Double(0, 0, width, height, 30, 30));
                ((Timer) e.getSource()).stop();
            }
        });
        resizeTimer.setRepeats(true);
        resizeTimer.start();
    }
    static void createDefaultJSON(Path path) {
        String defaultJSON = "{\n  \"language\": \"en_us\",\n  \"color\": \"#FFFFFF\",\n  \"full_screen_wait_time\": 600,\n  \"wait_time\": 30\n}";
        try {
            Files.write(path, defaultJSON.getBytes());
        } catch (IOException ignored) {
        }
    }

    public static boolean showConfirmDialogWithTimeout(Object params, String title, int timeout_ms) {
        final JOptionPane msg = new JOptionPane(params, JOptionPane.WARNING_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        final JDialog dlg = msg.createDialog(title);

        msg.setInitialSelectionValue(JOptionPane.OK_OPTION);
        dlg.setAlwaysOnTop(true);
        dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        // 自动关闭定时器
        Timer t = new Timer(timeout_ms, e -> {
            msg.setValue(JOptionPane.CANCEL_OPTION); // 在超时时手动设置值
            dlg.setVisible(false);
            dlg.dispose();
        });
        t.setRepeats(false); // 只执行一次
        t.start();

        dlg.setVisible(true);
        Object selectedValue = msg.getValue();

        return (selectedValue != null && selectedValue.equals(JOptionPane.CANCEL_OPTION));
    }

}
class ColorCirclePanel extends JPanel {
    private Color selectedColor = new Color(FloatingClockTray.R, FloatingClockTray.G, FloatingClockTray.B);

    public ColorCirclePanel() {
        setPreferredSize(new Dimension(150, 150));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedColor = getColorAtPoint(e.getX(), e.getY());
                FloatingClockTray.R = selectedColor.getRed();
                FloatingClockTray.G = selectedColor.getGreen();
                FloatingClockTray.B = selectedColor.getBlue();
                repaint(); // 重新绘制
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int diameter = Math.min(getWidth(), getHeight()) - 10;
        for (int i = 0; i < 360; i++) {
            g2d.setColor(Color.getHSBColor(i/360f, 1.0f, 1.0f));
            g2d.fillArc(5, 5, diameter, diameter, i, 1);
        }

        g2d.setColor(selectedColor);
        g2d.fillOval(getWidth() / 2 - 10, getHeight() / 2 - 10, 20, 20);
    }

    private Color getColorAtPoint(int x, int y) {
        float hue = (float) Math.atan2(y - (double) getHeight() / 2, x - (double) getWidth() / 2) / (2 * (float) Math.PI) + 0.5f;
        return Color.getHSBColor(hue, 1.0f, 1.0f);
    }
}

