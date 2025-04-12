package main.java.com.xxyxxdmc.time;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.*;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static javax.swing.JOptionPane.INFORMATION_MESSAGE;

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
    private static int R;
    private static int G;
    private static int B;
    private static Timer mainTimer;
    private static PopupMenu popupMenu;
    private static MenuItem sleepMenu;
    private static MenuItem sleep10;
    private static MenuItem sleep20;
    private static MenuItem sleep40;
    private static MenuItem sleep60;
    private static MenuItem lowSleepMenu;
    private static MenuItem lowSleep10;
    private static MenuItem lowSleep20;
    private static MenuItem lowSleep40;
    private static MenuItem lowSleep60;
    private static SystemTray tray;

    public FloatingClockTray() {
    }

    public static void main(String[] args) throws Exception {
        if (!SystemTray.isSupported()) {
            throw new Exception();
        }
        try {
            GlobalScreen.registerNativeHook();
        } catch (Exception ignored) {
        }
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
            settingFrame.setSize(290, 180);
            Dimension frameSize = settingFrame.getSize();
            int x = (screenSize.width - frameSize.width) / 2;
            int y = (screenSize.height - frameSize.height) / 2;
            settingFrame.setLocation(x,y);
            settingFrame.setEnabled(true);
            settingFrame.setVisible(true);
            settingFrame.setResizable(false);
            settingFrame.setLayout(new FlowLayout()); // 使用流式布局

            JTextField waitTextField = new JTextField(18);
            waitTextField.setText(String.valueOf(waitTimeAll));
            ((AbstractDocument) waitTextField.getDocument()).setDocumentFilter(new NumericFilter());
            JTextField fullWaitTextField = new JTextField(16);// 创建文本框，设置宽度为 20 个字符
            fullWaitTextField.setText(String.valueOf(fullWaitTimeAll));
            ((AbstractDocument) fullWaitTextField.getDocument()).setDocumentFilter(new NumericFilter());
            JLabel waitLabel = new JLabel("Wait Time:"); // 添加标签
            JLabel fullWaitLabel = new JLabel("Full Wait Time:"); // 添加标签
            JLabel colorLabel = new JLabel("Color:"); // 添加标签
            JPanel colorPanel = new JPanel();
            colorPanel.setBackground(new Color(R, G, B));
            JSlider RSlider = new JSlider(0, 255, R);
            JSlider GSlider = new JSlider(0, 255, G);
            JSlider BSlider = new JSlider(0, 255, B);
            RSlider.setMajorTickSpacing(50);
            RSlider.setPaintTicks(true);
            RSlider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    R = RSlider.getValue();
                    colorPanel.setBackground(new Color(R, G, B));
                }
            });
            GSlider.setMajorTickSpacing(50);
            GSlider.setPaintTicks(true);
            GSlider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    G = GSlider.getValue();
                    colorPanel.setBackground(new Color(R, G, B));
                }
            });
            BSlider.setMajorTickSpacing(50);
            BSlider.setPaintTicks(true);
            BSlider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    B = BSlider.getValue();
                    colorPanel.setBackground(new Color(R, G, B));
                }
            });
            settingFrame.add(waitLabel);
            settingFrame.add(waitTextField);
            settingFrame.add(fullWaitLabel);
            settingFrame.add(fullWaitTextField);
            settingFrame.add(colorLabel);
            settingFrame.add(RSlider, BorderLayout.SOUTH);
            settingFrame.add(colorPanel);
            settingFrame.add(GSlider, BorderLayout.SOUTH);
            settingFrame.add(BSlider, BorderLayout.SOUTH);

            settingFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    Color color = colorPanel.getBackground();
                    String hex = String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
                    String waitText = waitTextField.getText();
                    String fullWaitText = fullWaitTextField.getText();
                    if (Integer.parseInt(waitText)<5) waitText="5";
                    if (Integer.parseInt(fullWaitText)<10) fullWaitText="10";
                    if (Integer.parseInt(waitText)>120) waitText="120";
                    if (Integer.parseInt(fullWaitText)>2400) fullWaitText="2400";
                    if (Integer.parseInt(waitText)>Integer.parseInt(fullWaitText)) {
                        waitText="30";fullWaitText="600";
                    }
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

        sleepMenu = new MenuItem("Sleep");
        sleep10 = new MenuItem("Sleep 10 minutes");
        sleep20 = new MenuItem("Sleep 20 minutes");
        sleep40 = new MenuItem("Sleep 40 minutes");
        sleep60 = new MenuItem("Sleep 60 minutes");
        sleep10.addActionListener(e -> {
            sleepInTime(10);
            popupMenu.remove(sleep10);
            popupMenu.remove(sleep20);
            popupMenu.remove(sleep40);
            popupMenu.remove(sleep60);
            tray.getTrayIcons()[0].setPopupMenu(popupMenu);
        });
        sleep20.addActionListener(e -> {
            sleepInTime(20);
            popupMenu.remove(sleep10);
            popupMenu.remove(sleep20);
            popupMenu.remove(sleep40);
            popupMenu.remove(sleep60);
            tray.getTrayIcons()[0].setPopupMenu(popupMenu);
        });
        sleep40.addActionListener(e -> {
            sleepInTime(40);
            popupMenu.remove(sleep10);
            popupMenu.remove(sleep20);
            popupMenu.remove(sleep40);
            popupMenu.remove(sleep60);
            tray.getTrayIcons()[0].setPopupMenu(popupMenu);
        });
        sleep60.addActionListener(e -> {
            sleepInTime(60);
            popupMenu.remove(sleep10);
            popupMenu.remove(sleep20);
            popupMenu.remove(sleep40);
            popupMenu.remove(sleep60);
            tray.getTrayIcons()[0].setPopupMenu(popupMenu);
        });
        sleepMenu.addActionListener(e -> {
            popupMenu.add(sleep10);
            popupMenu.add(sleep20);
            popupMenu.add(sleep40);
            popupMenu.add(sleep60);
            popupMenu.remove(sleepMenu); // 删除折叠菜单项
            tray.getTrayIcons()[0].setPopupMenu(popupMenu); // 重新加载菜单
        });
        popupMenu.add(sleepMenu);
        lowSleepMenu = new MenuItem("Low Sleep");
        lowSleep10 = new MenuItem("Low Sleep 10 minutes");
        lowSleep20 = new MenuItem("Low Sleep 20 minutes");
        lowSleep40 = new MenuItem("Low Sleep 40 minutes");
        lowSleep60 = new MenuItem("Low Sleep 60 minutes");
        lowSleep10.addActionListener(e -> {
            lowSleepInTime(10);
            popupMenu.remove(lowSleep10);
            popupMenu.remove(lowSleep20);
            popupMenu.remove(lowSleep40);
            popupMenu.remove(lowSleep60);
            tray.getTrayIcons()[0].setPopupMenu(popupMenu);
        });lowSleep20.addActionListener(e -> {
            lowSleepInTime(20);
            popupMenu.remove(lowSleep10);
            popupMenu.remove(lowSleep20);
            popupMenu.remove(lowSleep40);
            popupMenu.remove(lowSleep60);
            tray.getTrayIcons()[0].setPopupMenu(popupMenu);
        });lowSleep40.addActionListener(e -> {
            lowSleepInTime(40);
            popupMenu.remove(lowSleep10);
            popupMenu.remove(lowSleep20);
            popupMenu.remove(lowSleep40);
            popupMenu.remove(lowSleep60);
            tray.getTrayIcons()[0].setPopupMenu(popupMenu);
        });lowSleep60.addActionListener(e -> {
            lowSleepInTime(60);
            popupMenu.remove(lowSleep10);
            popupMenu.remove(lowSleep20);
            popupMenu.remove(lowSleep40);
            popupMenu.remove(lowSleep60);
            tray.getTrayIcons()[0].setPopupMenu(popupMenu);
        });lowSleepMenu.addActionListener(e -> {
            popupMenu.add(lowSleep10);
            popupMenu.add(lowSleep20);
            popupMenu.add(lowSleep40);
            popupMenu.add(lowSleep60);
            popupMenu.remove(sleepMenu);
            popupMenu.remove(lowSleepMenu);
            tray.getTrayIcons()[0].setPopupMenu(popupMenu);
        });
        popupMenu.add(lowSleepMenu);

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
        mainTimer.start();

        int confirm = JOptionPane.showConfirmDialog(null, "Do you want to enable low sleep for 40 minutes now?");

        if (confirm == JOptionPane.OK_OPTION) {
            lowSleepInTime(40);
            popupMenu.remove(sleepMenu);
            popupMenu.remove(lowSleepMenu);
            popupMenu.remove(lowSleep10);
            popupMenu.remove(lowSleep20);
            popupMenu.remove(lowSleep40);
            popupMenu.remove(lowSleep60);
            tray.getTrayIcons()[0].setPopupMenu(popupMenu);
        }
    }

    private static void updateTime() {
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        timeLabel.setText(time);
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent event) {
        try {
            timeToShow=false;
            resetTimer();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void nativeMousePressed(NativeMouseEvent event) {
        try {
            timeToShow=false;
            resetTimer();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void nativeMouseMoved(NativeMouseEvent event) {
        try {
            timeToShow=false;
            resetTimer();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void nativeMouseDragged(NativeMouseEvent event) {
        try {
            timeToShow=false;
            resetTimer();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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
            mainTimer.start();
            isRunning=false;
            try {resetTimer();} catch (InterruptedException ignored) {}
            popupMenu.remove(sleep10);
            popupMenu.remove(sleep20);
            popupMenu.remove(sleep40);
            popupMenu.remove(sleep60);
            popupMenu.add(sleepMenu);
            tray.getTrayIcons()[0].setPopupMenu(popupMenu);
        }, minutes, TimeUnit.MINUTES);
    }

    public static void lowSleepInTime(int minutes) {
        mainTimer.stop();
        fullScreenTimer=0;
        isFullScreen=false;
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        scheduler.schedule(() -> {
            mainTimer.start();
            popupMenu.remove(lowSleep10);
            popupMenu.remove(lowSleep20);
            popupMenu.remove(lowSleep40);
            popupMenu.remove(lowSleep60);
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
    private static void resetTimer() throws InterruptedException {
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
            Timer swingTimer = new Timer(20, e -> {
                timer += 0.04;
                y = 50 + (int) ((-120 - 50) * easeInBack(timer));
                frame.setLocation((screenSize.width - frame.getWidth()) / 2, y);
            });
            swingTimer.start();
            TimeUnit.SECONDS.sleep(1);
            timer=0;
            swingTimer.stop();
            frame.setVisible(false);
            isHidden = true;
            isRunning=false;
        }
        scheduler.shutdownNow();
        scheduler = Executors.newScheduledThreadPool(1);
        timeToShow=true;
        scheduler.schedule(() -> {
            if (!timeToShow) {
                try {
                    isRunning=false;
                    resetTimer();
                } catch (InterruptedException ignored){}
                return;
            }
            Timer swingTimer = new Timer(20, e -> {
                timer += 0.04;
                y = -120 + (int) ((50 + 120) * easeOutExpo(timer));
                frame.setLocation((screenSize.width - frame.getWidth()) / 2, y);
            });
            swingTimer.start();
            frame.setVisible(true);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            timer=0;
            swingTimer.stop();
            isHidden = false;
            isRunning=false;
        }, waitTimeAll, TimeUnit.SECONDS);
    }

    private static void enterFullScreen(){
        if (isRunning) return;
        isRunning=true;
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
                    frame.setSize(width, height);
                    frame.setShape(new RoundRectangle2D.Float(0, 0, width, height, 0, 0));
                    InputStream fontStream = FloatingClockTray.class.getResourceAsStream("/minecraft.ttf");
                    assert fontStream != null;
                    Font MCFont;
                    try {
                        MCFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
                    } catch (FontFormatException | IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    timeLabel.setFont(MCFont.deriveFont(Font.PLAIN, (float) (48 + ((260 - 48) * easeOutExpo(timer)))));
                } else {
                    frame.setLocation(((screenSize.width - frame.getWidth()) / 2), ((screenSize.height - frame.getHeight()) / 2));
                    frame.setSize(screenSize.width, screenSize.height);
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
                    frame.setSize(width, height);
                    frame.setShape(new RoundRectangle2D.Float(0, 0, width, height, 30, 30));
                    InputStream fontStream = FloatingClockTray.class.getResourceAsStream("/minecraft.ttf");
                    assert fontStream != null;
                    Font MCFont;
                    try {
                        MCFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
                    } catch (FontFormatException | IOException ex) {
                        throw new RuntimeException(ex);
                    }
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
}
class NumericFilter extends DocumentFilter {
    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        if (string.matches("\\d+")) { // 仅允许数字
            super.insertString(fb, offset, string, attr);
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attrs) throws BadLocationException {
        if (string.matches("\\d+")) { // 仅允许数字
            super.replace(fb, offset, length, string, attrs);
        }
    }
}