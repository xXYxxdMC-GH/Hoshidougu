package main.java.com.xxyxxdmc.time;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.*;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FloatingClockTray implements NativeKeyListener, NativeMouseListener, NativeMouseInputListener, NativeMouseWheelListener {
    private static final JFrame frame = new JFrame("Clock");
    private static JLabel timeLabel;
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static boolean isHidden = false;
    private static int y = 50;
    private static double timer = 0;
    private static boolean isRunning = false;
    private static int fullScreenTimer = 0;
    private static boolean isFullScreen = false;
    private static int width = 300, height = 120; // 初始大小
    private static Timer resizeTimer;
    private static int waitTimeAll;
    private static int fullWaitTimeAll;
    private static boolean timeToShow = false;
    private static int R, G, B;
    private static Timer mainTimer;
    private static PopupMenu popupMenu;
    private static MenuItem sleep10, sleep20, sleep30, sleep40, sleep45, sleep50, sleep60, sleepCustom;
    private static MenuItem lowSleep10, lowSleep20, lowSleep30, lowSleep40, lowSleep45, lowSleep50, lowSleep60, lowSleepCustom;
    private static Menu sleepMenu, lowSleepMenu;
    private static SystemTray tray;
    private static boolean countdown = false;

    public FloatingClockTray() {
    }

    public static void main(String[] args) throws Exception {
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
        String languageJson = Objects.requireNonNull(FloatingClockTray.class.getResource("/en_us.json")).getPath();
        File languageFile = new File(languageJson);
        System.out.print(new String(languageJson.getBytes()));
        if (!Files.exists(jsonPath)) {
            createDefaultJSON(jsonPath);
        }
        String content = new String(Files.readAllBytes(jsonPath));
        JSONObject json = new JSONObject(content);
        String color = json.getString("color");
        int waitTime = json.getInt("wait_time");
        int fullWaitTime = json.getInt("full_screen_wait_time");
        waitTimeAll=waitTime;
        fullWaitTimeAll=fullWaitTime;
        R=Color.decode(color).getRed();
        G=Color.decode(color).getGreen();
        B=Color.decode(color).getBlue();

        tray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().getImage(FloatingClockTray.class.getResource("/xxyxxdmc.png"));

        TrayIcon trayIcon = new TrayIcon(image, "Clock");
        trayIcon.setImageAutoSize(true);
        tray.add(trayIcon);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setUndecorated(true); // 无边框
        frame.setAlwaysOnTop(true); // 置顶
        frame.setSize(width, height);
        frame.setLocation((screenSize.width - frame.getWidth()) / 2, y);
        frame.setType(Window.Type.UTILITY);

        frame.setShape(new RoundRectangle2D.Float(0, 0, width, height, 30, 30));

        timeLabel = new JLabel("", JLabel.CENTER);
        InputStream fontStream = FloatingClockTray.class.getResourceAsStream("/minecraft.ttf");
        assert fontStream != null;
        Font MCFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
        timeLabel.setFont(MCFont.deriveFont(Font.PLAIN, 48));
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
        MenuItem settingItem = new MenuItem("Setting");
        settingItem.addActionListener(e -> {
            JFrame settingFrame = new JFrame("Setting");
            settingFrame.setSize(350, 250);
            settingFrame.setLocationRelativeTo(null); // 居中显示
            settingFrame.setResizable(false);
            settingFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JPanel mainPanel = new JPanel();
            mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            mainPanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            JLabel waitLabel = new JLabel("Wait Time:");
            JSlider waitSlider = new JSlider(5, 240, waitTimeAll);
            waitSlider.setMajorTickSpacing(50);
            waitSlider.setMinorTickSpacing(5);
            waitSlider.setPaintTicks(true);
            waitSlider.setPaintLabels(true);
            waitSlider.addChangeListener((ChangeEvent e1) -> waitTimeAll = waitSlider.getValue());

            JLabel fullWaitLabel = new JLabel("Full Wait Time:");
            JSlider fullWaitSlider = new JSlider(10, 720, fullWaitTimeAll);
            fullWaitSlider.setMajorTickSpacing(100);
            fullWaitSlider.setMinorTickSpacing(10);
            fullWaitSlider.setPaintTicks(true);
            fullWaitSlider.setPaintLabels(true);
            fullWaitSlider.addChangeListener((ChangeEvent e2) -> fullWaitTimeAll = fullWaitSlider.getValue());

            JLabel colorLabel = new JLabel("Color:");
            JPanel colorPanel = new JPanel();
            colorPanel.setBackground(new Color(R, G, B));
            colorPanel.setPreferredSize(new Dimension(50, 30));
            JButton colorButton = new JButton("Color");
            colorButton.addActionListener(colorE -> {
                Color selectedColor = JColorChooser.showDialog(frame, "Choose a Color", colorPanel.getBackground());
                if (selectedColor != null) {
                    colorPanel.setBackground(selectedColor);
                }
            });

            gbc.gridx = 0; gbc.gridy = 0; mainPanel.add(waitLabel, gbc);
            gbc.gridx = 1; mainPanel.add(waitSlider, gbc);

            gbc.gridx = 0; gbc.gridy = 1; mainPanel.add(fullWaitLabel, gbc);
            gbc.gridx = 1; mainPanel.add(fullWaitSlider, gbc);

            gbc.gridx = 0; gbc.gridy = 2; mainPanel.add(colorLabel, gbc);
            gbc.gridx = 1; mainPanel.add(colorButton, gbc);
            gbc.gridx = 2; mainPanel.add(colorPanel, gbc);

            settingFrame.add(mainPanel);
            settingFrame.setVisible(true);

            settingFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    Color color = colorPanel.getBackground();
                    String hex = String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
                    String waitText = String.valueOf(waitSlider.getValue());
                    String fullWaitText = String.valueOf(fullWaitSlider.getValue());
                    Path jarDir = Paths.get(System.getProperty("user.dir"));
                    Path jsonPath = jarDir.resolve("data.json");
                    String defaultJSON = String.format("{\n  \"color\": \"%s\",\n  \"full_screen_wait_time\": %s,\n  \"wait_time\": %s\n}", hex, fullWaitText, waitText);
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

        MenuItem countdownItem = new MenuItem("Countdown");
        countdownItem.addActionListener(e -> {
            countdown=!countdown;
            if (countdown) countdownItem.setLabel("Countdown");
            else countdownItem.setLabel("Countdown ✔");
        });
        MenuItem fullScreenItem = new MenuItem("Full Screen");
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

        sleepMenu = new Menu("Sleep");
        sleep10 = new MenuItem("Sleep 10 minutes");
        sleep20 = new MenuItem("Sleep 20 minutes");
        sleep30 = new MenuItem("Sleep 30 minutes");
        sleep40 = new MenuItem("Sleep 40 minutes");
        sleep45 = new MenuItem("Sleep 45 minutes");
        sleep50 = new MenuItem("Sleep 50 minutes");
        sleep60 = new MenuItem("Sleep 60 minutes");
        sleepCustom = new MenuItem("Sleep Custom Time");
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
            removeAllSleep();
        });
        sleep20.addActionListener(e -> {
            sleepInTime(20);
            removeAllSleep();
        });
        sleep30.addActionListener(e -> {
            sleepInTime(30);
            removeAllSleep();
        });
        sleep40.addActionListener(e -> {
            sleepInTime(40);
            removeAllSleep();
        });
        sleep45.addActionListener(e -> {
            sleepInTime(40);
            removeAllSleep();
        });
        sleep50.addActionListener(e -> {
            sleepInTime(40);
            removeAllSleep();
        });
        sleep60.addActionListener(e -> {
            sleepInTime(60);
            removeAllSleep();
        });
        lowSleepMenu = new Menu("Low Sleep");
        lowSleep10 = new MenuItem("Low Sleep 10 minutes");
        lowSleep20 = new MenuItem("Low Sleep 20 minutes");
        lowSleep30 = new MenuItem("Low Sleep 30 minutes");
        lowSleep40 = new MenuItem("Low Sleep 40 minutes");
        lowSleep45 = new MenuItem("Low Sleep 45 minutes");
        lowSleep50 = new MenuItem("Low Sleep 50 minutes");
        lowSleep60 = new MenuItem("Low Sleep 60 minutes");
        lowSleepCustom = new MenuItem("Low Sleep Custom Time");
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
            removeAllLowSleep();
        });lowSleep20.addActionListener(e -> {
            lowSleepInTime(20);
            removeAllLowSleep();
        });lowSleep30.addActionListener(e -> {
            lowSleepInTime(30);
            removeAllLowSleep();
        });lowSleep40.addActionListener(e -> {
            lowSleepInTime(40);
            removeAllLowSleep();
        });lowSleep45.addActionListener(e -> {
            lowSleepInTime(45);
            removeAllLowSleep();
        });lowSleep50.addActionListener(e -> {
            lowSleepInTime(50);
            removeAllLowSleep();
        });lowSleep60.addActionListener(e -> {
            lowSleepInTime(60);
            removeAllLowSleep();
        });
        popupMenu.add(lowSleepMenu);

        popupMenu.add(countdownItem);

        MenuItem exitItem = new MenuItem("Exit");
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

        String message = "Do you want to enable low sleep for 40 minutes now?";
        JLabel MSG = new JLabel(message);
        boolean result = showConfirmDialogWithTimeout(MSG, "Ask", 5 * 1000);

        if (!result) {
            lowSleepInTime(40);
            popupMenu.remove(sleepMenu);
            popupMenu.remove(lowSleepMenu);
            removeAllLowSleep();
        } else mainTimer.start();
    }

    private static void updateTime() {
        String time = "00:00:00";
        LocalTime now = LocalTime.now();
        int hour = now.getHour();
        int minute = now.getMinute();
        if (countdown && hour<=17) {
            if (minute<=40){
                int second = now.getSecond();
                time = String.format("%s%d:%s%d:%s%d",(17-hour>0&&17-hour<10)?"0":"",17-hour,(40-minute>0&&40-minute<10)?"0":"",40-minute,(60-second<10)?"0":"",60-second);
                timeLabel.setText(time);
            } else {
                timeLabel.setText(time);
            }
        } else {
            time = new SimpleDateFormat("HH:mm:ss").format(new Date());
            timeLabel.setText(time);
        }

    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent event) {
        timeToShow=false;
        resetTimer();
    }

    @Override
    public void nativeMousePressed(NativeMouseEvent event) {
        timeToShow=false;
        resetTimer();
    }

    @Override
    public void nativeMouseMoved(NativeMouseEvent event) {
        timeToShow=false;
        resetTimer();
    }

    @Override
    public void nativeMouseDragged(NativeMouseEvent event) {
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
            removeAllSleep();
            popupMenu.add(sleepMenu);
            popupMenu.add(lowSleepMenu);
        }, minutes, TimeUnit.MINUTES);
    }

    public static void lowSleepInTime(int minutes) {
        mainTimer.stop();
        fullScreenTimer=0;
        isFullScreen=false;
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        scheduler.schedule(() -> {
            removeAllLowSleep();
            popupMenu.add(lowSleepMenu);
            popupMenu.add(sleepMenu);
        }, minutes, TimeUnit.MINUTES);
    }

    public static void removeAllLowSleep() {
        popupMenu.remove(lowSleep10);
        popupMenu.remove(lowSleep20);
        popupMenu.remove(lowSleep40);
        popupMenu.remove(lowSleep60);
        tray.getTrayIcons()[0].setPopupMenu(popupMenu);
    }

    public static void removeAllSleep() {
        popupMenu.remove(sleep10);
        popupMenu.remove(sleep20);
        popupMenu.remove(sleep40);
        popupMenu.remove(sleep60);
        tray.getTrayIcons()[0].setPopupMenu(popupMenu);
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
        resizeTimer = new Timer(20, new ActionListener()  {
            final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            @Override
            public void actionPerformed(ActionEvent e) {
                if ((width < screenSize.width || height < screenSize.height) && timer < 1) {
                    timer += 0.02;
                    width = (int) Math.ceil(300 + ((screenSize.width - 300) * easeOutExpo(timer)));
                    height = (int) Math.ceil(120 +((screenSize.height - 120) * easeOutExpo(timer)));
                    y = 50 + (int) (((double) ((screenSize.height - frame.getHeight()) / 2) - 50) * easeOutExpo(timer));
                    frame.setLocation((screenSize.width - frame.getWidth()) / 2, y);
                    frame.setPreferredSize(new Dimension(width, height));
                    frame.pack();
                    frame.setShape(new RoundRectangle2D.Float(0, 0, width, height, 0, 0));
                    InputStream fontStream = FloatingClockTray.class.getResourceAsStream("/minecraft.ttf");
                    assert fontStream != null;
                    Font MCFont;
                    try {MCFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);} catch (FontFormatException | IOException ex) {throw new RuntimeException(ex);}
                    timeLabel.setFont(MCFont.deriveFont(Font.PLAIN, (float) (48 + ((260 - 48) * easeOutExpo(timer)))));
                } else {
                    frame.setLocation(((screenSize.width - frame.getWidth()) / 2), ((screenSize.height - frame.getHeight()) / 2));
                    frame.setPreferredSize(screenSize);
                    frame.pack();
                    frame.setShape(new RoundRectangle2D.Float(0, 0, screenSize.width, screenSize.height, 0, 0));
                    timer=0;
                    isRunning=false;
                    ((Timer) e.getSource()).stop();
                }
            }
        });
        resizeTimer.setRepeats(true);
        resizeTimer.start();
    }

    private static void exitFullScreen(){
        if (isRunning) return;
        isRunning=true;
        mainTimer.start();
        resizeTimer = new Timer(20, new ActionListener()  {
            final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            @Override
            public void actionPerformed(ActionEvent e) {
                if ((width < screenSize.width || height < screenSize.height) && timer < 1) {
                    timer += 0.02;
                    width = (int) Math.ceil(screenSize.width + ((300 - screenSize.width) * easeOutExpo(timer)));
                    height = (int) Math.ceil(screenSize.height + ((120 - screenSize.height) * easeOutExpo(timer)));
                    y = (screenSize.height - frame.getHeight()) / 2 + (int) ((50 - (double) (screenSize.height - frame.getHeight()) / 2) * easeOutExpo(timer));
                    frame.setLocation((screenSize.width - frame.getWidth()) / 2, y);
                    frame.setPreferredSize(new Dimension(width, height));
                    frame.pack();
                    frame.setShape(new RoundRectangle2D.Float(0, 0, width, height, 30, 30));
                    InputStream fontStream = FloatingClockTray.class.getResourceAsStream("/minecraft.ttf");
                    assert fontStream != null;
                    Font MCFont;
                    try {MCFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);} catch (FontFormatException | IOException ex) {throw new RuntimeException(ex);}
                    timeLabel.setFont(MCFont.deriveFont(Font.PLAIN, (float) (260 + ((48 - 260) * easeOutExpo(timer)))));
                } else {
                    timer=0;
                    isRunning=false;
                    ((Timer) e.getSource()).stop();
                }
            }
        });
        resizeTimer.setRepeats(true);
        resizeTimer.start();
    }
    private static void createDefaultJSON(Path path) {
        String defaultJSON = "{\n  \"color\": \"#FFFFFF\",\n  \"full_screen_wait_time\": 600,\n  \"wait_time\": 30\n}";
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
