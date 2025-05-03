package main.java.com.xxyxxdmc.toolbox;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.*;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static main.java.com.xxyxxdmc.toolbox.MainToolBox.lowSleepMenu;
import static main.java.com.xxyxxdmc.toolbox.MainToolBox.sleepMenu;

public class FloatingClockTray implements NativeKeyListener, NativeMouseListener, NativeMouseInputListener, NativeMouseWheelListener {
    static JFrame clockFrame;
    static JLabel timeLabel;
    static boolean isClockHidden = false, isClockRunning = false, countdown = false, isFullScreen = false;
    private static int y = 50;
    static double timer = 0, fadeTime = 0;
    static int mainTime = 0;
    static int width = 300, height = 120; // 初始大小
    private static Timer resizeTimer;
    public static int waitTimeAll, fullWaitTimeAll, textSizeAll;
    public static int R, G, B;
    static Timer mainTimer;
    private static SystemTray tray;
    private static Font minecraftFont;
    private static final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    private static final String systemType = System.getProperty("os.name");
    //private static final String systemVersion = System.getProperty("os.version");
    //private static final String systemArch = System.getProperty("os.arch");
    private static String sleepType;

    public FloatingClockTray() {
    }
    static {
        InputStream fontStream = classLoader.getResourceAsStream("minecraft.ttf");
        assert fontStream != null;
        try {
            minecraftFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
        } catch (FontFormatException | IOException ignored) {}
    }

    public static void main(String[] args) {
        //LocalDate today = LocalDate.now(ZoneId.systemDefault());
        //DayOfWeek dayOfWeek = today.getDayOfWeek();
        JsonObject dataObject = DataJsonReader.getDataObject();
        JsonObject languageObject = DataJsonReader.getLanguageObject();
        String color = dataObject.get("color").getAsString();
        int waitTime = dataObject.get("wait_time").getAsInt();
        int fullWaitTime = dataObject.get("full_screen_wait_time").getAsInt();
        int textSize = dataObject.get("text_size").getAsInt();
        waitTimeAll=waitTime;
        fullWaitTimeAll=fullWaitTime;
        textSizeAll=textSize;
        R=Color.decode(color).getRed();
        G=Color.decode(color).getGreen();
        B=Color.decode(color).getBlue();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        clockFrame = new JFrame("Clock");
        clockFrame.setUndecorated(true);
        clockFrame.setAlwaysOnTop(true);
        clockFrame.setSize(width, height);
        clockFrame.setLocation((screenSize.width - clockFrame.getWidth()) / 2, y);
        clockFrame.setType(Window.Type.UTILITY);

        clockFrame.setShape(new RoundRectangle2D.Float(0, 0, width, height, 30, 30));

        timeLabel = new JLabel("", SwingConstants.CENTER);

        timeLabel.setFont(minecraftFont.deriveFont(Font.PLAIN, 48));
        clockFrame.add(timeLabel);
        clockFrame.getContentPane().setBackground(Color.BLACK);
        timeLabel.setForeground(new Color(R, G, B));

        clockFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        Timer timer = new Timer(1000, e -> {
            updateTime();
            if (!MainToolBox.runningClass[0]) {
                clockFrame.dispose();
                ((Timer)e.getSource()).stop();
            }
        });
        timer.start();

        clockFrame.setVisible(true);
        clockFrame.setFocusableWindowState(false);
        clockFrame.setEnabled(false);

        mainTimer = new Timer(1000, e -> {
            if (!MainToolBox.runningClass[0]) {
                ((Timer)e.getSource()).stop();
                mainTime=0;
                return;
            }
            mainTime++;
            if (mainTime == waitTimeAll && isClockHidden) {
                showTheFrame();
            } else if (mainTime == waitTimeAll + fullWaitTimeAll && !Objects.equals(sleepType, "low")) {
                enterFullScreen();
                isFullScreen = true;
            }
        });

        JLabel MSG = new JLabel(languageObject.get("ask_sentence").getAsString());
        boolean result = showConfirmDialogWithTimeout(MSG, languageObject.get("ask").getAsString(), 5 * 1000);

        if (!result) {
            lowSleepInTime(40);
            sleepMenu.setEnabled(false);
            lowSleepMenu.setEnabled(false);
        } else mainTimer.start();
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
        if (mainTimer ==null) return;
        mainTime=0;
        hideTheFrame();
    }

    @Override
    public void nativeMousePressed(NativeMouseEvent event) {
        if (mainTimer ==null) return;
        mainTime=0;
        hideTheFrame();
    }

    @Override
    public void nativeMouseMoved(NativeMouseEvent event) {
        if (mainTimer ==null) return;
        mainTime=0;
        hideTheFrame();
    }

    @Override
    public void nativeMouseDragged(NativeMouseEvent event) {
        if (mainTimer ==null) return;
        mainTime=0;
        hideTheFrame();
    }

    @Override
    public void nativeMouseWheelMoved(NativeMouseWheelEvent nativeEvent){
        if (mainTimer ==null||!MainToolBox.runningClass[0]) return;
        mainTime=0;
        hideTheFrame();
    }

    public static void sleepInTime(int minutes) {
        mainTimer.stop();
        timer=0;
        mainTime =0;
        isFullScreen=false;
        isClockRunning =true;
        sleepType="sleep";
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(() -> {
            sleepType=null;
            isClockRunning =false;
            mainTimer.start();
            sleepMenu.setEnabled(true);
            lowSleepMenu.setEnabled(true);
        }, minutes, TimeUnit.MINUTES);
    }

    public static void lowSleepInTime(int minutes) {
        mainTime=0;
        sleepType="low";
        isFullScreen=false;
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(() -> {
            sleepType=null;
            lowSleepMenu.setEnabled(true);
            sleepMenu.setEnabled(true);
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
    private static void hideTheLabel() {
        Timer fadeTimer = new Timer(20, e -> {
            if (fadeTime<1) {
                fadeTime+=0.04;
                timeLabel.setForeground(new Color((int) Math.floor(R*(1-easeOutExpo(fadeTime))),(int) Math.floor(G*(1-easeOutExpo(fadeTime))), (int) Math.floor(B*(1-easeOutExpo(fadeTime)))));
            } else {
                fadeTime=0;
                ((Timer) e.getSource()).stop();
            }
        });
        fadeTimer.start();
    }
    private static void showTheLabel() {
        Timer fadeTimer = new Timer(20, e -> {
            if (fadeTime<1) {
                fadeTime+=0.04;
                timeLabel.setForeground(new Color((int) Math.floor(R*easeOutExpo(fadeTime)),(int) Math.floor(G*easeOutExpo(fadeTime)), (int) Math.floor(B*easeOutExpo(fadeTime))));
            } else {
                fadeTime=0;
                ((Timer) e.getSource()).stop();
            }
        });
        fadeTimer.start();
    }
    private static void hideTheFrame() {
        if (isClockRunning || isClockHidden) return;
        isClockRunning =true;
        if (isFullScreen) {
            isClockRunning =false;
            isFullScreen=false;
            exitFullScreen();
            return;
        }
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        mainTimer.stop();
        Timer swingTimer = new Timer(20, e -> {
            if (timer < 1) {
                timer += 0.06;
                y = 50 + (int) ((-120 - 50) * easeInBack(timer));
                clockFrame.setLocation((screenSize.width - clockFrame.getWidth()) / 2, y);
            } else {
                timer = 0;
                isClockHidden = true;
                isClockRunning = false;
                clockFrame.setVisible(false);
                mainTime=0;
                mainTimer.start();
                ((Timer) e.getSource()).stop();
            }
        });
        swingTimer.start();
    }
    public static void showTheFrame() {
        if (isClockRunning) return;
        isClockRunning =true;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Timer swingTimer = new Timer(20, e -> {
            if (timer<1){
                timer += 0.06;
                y = -120 + (int) ((50 + 120) * easeOutExpo(timer));
                clockFrame.setLocation((screenSize.width - clockFrame.getWidth()) / 2, y);
            } else {
                timer=0;
                isClockHidden = false;
                isClockRunning =false;
                ((Timer) e.getSource()).stop();
            }
        });
        swingTimer.start();
        clockFrame.setVisible(true);
    }

    public static void enterFullScreen(){
        if (isClockRunning) return;
        isClockRunning =true;
        mainTimer.stop();
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        resizeTimer = new Timer(20, e -> {
            if (timer < 1) {
                timer += 0.04;
                width = (int) Math.ceil(300 + ((screenSize.width - 300) * easeOutExpo(timer)));
                height = (int) Math.ceil(120 +((screenSize.height - 120) * easeOutExpo(timer)));
                y = 50 + (int) (((double) ((screenSize.height - clockFrame.getHeight()) / 2) - 50) * easeOutExpo(timer));
                clockFrame.setLocation((screenSize.width - clockFrame.getWidth()) / 2, y);
                clockFrame.setPreferredSize(new Dimension(width, height));
                clockFrame.pack();
                clockFrame.setShape(new RoundRectangle2D.Float(0, 0, width, height, 0, 0));
                timeLabel.setFont(minecraftFont.deriveFont(Font.PLAIN, (float) (48 + ((textSizeAll - 48) * easeOutExpo(timer)))));
            } else {
                clockFrame.setLocation(((screenSize.width - clockFrame.getWidth()) / 2), ((screenSize.height - clockFrame.getHeight()) / 2));
                clockFrame.setPreferredSize(screenSize);
                clockFrame.pack();
                clockFrame.setShape(new RoundRectangle2D.Double(0, 0, screenSize.width, screenSize.height, 30 * (1-easeOutExpo(timer)), 30 * (1-easeOutExpo(timer))));
                timer=0;
                isClockRunning =false;
                ((Timer) e.getSource()).stop();
                clockFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
        });
        resizeTimer.setRepeats(true);
        resizeTimer.start();
    }

    private static void exitFullScreen(){
        if (isClockRunning) return;
        isClockRunning =true;
        clockFrame.setExtendedState(JFrame.NORMAL);
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        resizeTimer = new Timer(20, e -> {
            if (timer < 1) {
                timer += 0.04;
                width = (int) Math.ceil(screenSize.width + ((300 - screenSize.width) * easeOutExpo(timer)));
                height = (int) Math.ceil(screenSize.height + ((120 - screenSize.height) * easeOutExpo(timer)));
                y = (screenSize.height - clockFrame.getHeight()) / 2 + (int) ((50 - (double) (screenSize.height - clockFrame.getHeight()) / 2) * easeOutExpo(timer));
                clockFrame.setLocation((screenSize.width - clockFrame.getWidth()) / 2, y);
                clockFrame.setPreferredSize(new Dimension(width, height));
                clockFrame.pack();
                clockFrame.setShape(new RoundRectangle2D.Double(0, 0, width, height, 30 * easeOutExpo(timer), 30 * easeOutExpo(timer)));
                timeLabel.setFont(minecraftFont.deriveFont(Font.PLAIN, (float) (textSizeAll + ((48 - textSizeAll) * easeOutExpo(timer)))));
            } else {
                timer=0;
                isClockRunning =false;
                clockFrame.setShape(new RoundRectangle2D.Double(0, 0, width, height, 30, 30));
                mainTime=waitTimeAll;
                mainTimer.start();
                ((Timer) e.getSource()).stop();
            }
        });
        resizeTimer.setRepeats(true);
        resizeTimer.start();
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

    public static String detectOS() {
        String os = systemType.toLowerCase();
        if (os.contains("win")) {
            return "Windows";
        } else if ((os.contains("mac"))) {
            return "MacOS";
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            return "Linux";
        } else return "Unsupported OS";
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

class CPUControlRow extends JPanel {
    private JToggleButton powerToggle;
    private JSlider frequencySlider;

    public CPUControlRow() {
        setLayout(new FlowLayout());

        // **创建小尺寸的开关按钮**
        powerToggle = new JToggleButton("CPU 限制: 关闭");
        powerToggle.setPreferredSize(new Dimension(40, 25)); // **调整按钮大小**
        powerToggle.addActionListener(e -> {
            if (powerToggle.isSelected()) {
                powerToggle.setText("CPU 限制: 开启");
                adjustCPUFrequency(frequencySlider.getValue());
            } else {
                powerToggle.setText("CPU 限制: 关闭");
                resetCPUFrequency();
            }
        });

        // **创建滑动条 (50% - 100%)**
        frequencySlider = new JSlider(JSlider.HORIZONTAL, 30, 100, 85);
        frequencySlider.setMajorTickSpacing(10);
        frequencySlider.setPaintLabels(true);
        frequencySlider.setPreferredSize(new Dimension(150, 30)); // **设置滑动条大小**
        frequencySlider.addChangeListener((ChangeEvent e) -> {
            if (powerToggle.isSelected()) {
                adjustCPUFrequency(frequencySlider.getValue());
            }
        });

        // **添加组件到面板**
        add(powerToggle);
        add(new JLabel("CPU 限制 (%)："));
        add(frequencySlider);
    }

    // **调整 CPU 频率**
    private void adjustCPUFrequency(int value) {
        try {
            new ProcessBuilder("powercfg", "/SETACVALUEINDEX", "SCHEME_BALANCED", "SUB_PROCESSOR", "MAXPROCSTATE", String.valueOf(value)).start();
            System.out.println("CPU 频率调整为: " + value + "%");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // **恢复 CPU 频率**
    private void resetCPUFrequency() {
        adjustCPUFrequency(100);
    }

}

