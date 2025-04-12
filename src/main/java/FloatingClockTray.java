package main.java;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.*;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    private static int waitTimeAll = 10;
    private static int fullWaitTimeAll = 600;
    private static boolean timeToShow = false;

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

        SystemTray tray = SystemTray.getSystemTray();
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
        timeLabel.setForeground(Color.decode(color));

        // 关闭窗口时隐藏
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        // 定时更新时间
        Timer timer = new Timer(1000, e -> updateTime());
        timer.start();

        frame.setVisible(true);
        frame.setFocusableWindowState(false);
        frame.setEnabled(false);

        // 托盘菜单
        PopupMenu popupMenu = new PopupMenu();

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        popupMenu.add(exitItem);

        trayIcon.setPopupMenu(popupMenu);

        Timer mainTimer = new Timer(1000, e -> {
            if (!isFullScreen) {
                fullScreenTimer++;
                if (fullScreenTimer==fullWaitTimeAll) {
                    enterFullScreen();
                    isFullScreen=true;
                }
            }
        });
        mainTimer.start();
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
            resetTimer(); // 鼠标移动
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void nativeMouseWheelMoved(NativeMouseWheelEvent event) {
        try {
            timeToShow=false;
            resetTimer(); // 鼠标滚动
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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
                    width = 300 + (int) ((screenSize.width - 300) * easeOutExpo(timer));
                    height = 120 + (int) ((screenSize.height - 120) * easeOutExpo(timer));
                    y = 50 + (int) (((double) ((screenSize.height - frame.getHeight()) / 2) - 50) * easeOutExpo(timer));
                    frame.setLocation((screenSize.width - frame.getWidth()) / 2, y);
                    frame.setSize(width, height);
                    frame.setShape(new RoundRectangle2D.Float(0, y, width, height, 0, 0));
                    InputStream fontStream = FloatingClockTray.class.getResourceAsStream("/minecraft.ttf");
                    assert fontStream != null;
                    Font MCFont;
                    try {
                        MCFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
                    } catch (FontFormatException | IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    timeLabel.setFont(MCFont.deriveFont(Font.PLAIN, 48 + (int) ((240 - 48) * easeOutExpo(timer))));
                } else {
                    frame.setLocation(((screenSize.width - frame.getWidth()) / 2), ((screenSize.height - frame.getHeight()) / 2)-2);
                    frame.setSize(screenSize.width +6, screenSize.height);
                    frame.setShape(new RoundRectangle2D.Float(0, y, screenSize.width, screenSize.height, 0, 0));
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
                    width = screenSize.width + (int) ((300 - screenSize.width) * easeOutExpo(timer));
                    height = screenSize.height + (int) ((120 - screenSize.height) * easeOutExpo(timer));
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
                    timeLabel.setFont(MCFont.deriveFont(Font.PLAIN, 240 + (int) ((48 - 240) * easeOutExpo(timer))));
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