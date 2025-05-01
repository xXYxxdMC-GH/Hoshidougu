package main.java.com.xxyxxdmc.toolbox;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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

public class FloatingClockTray implements NativeKeyListener, NativeMouseListener, NativeMouseInputListener, NativeMouseWheelListener {
    private static final JFrame frame = new JFrame("Clock");
    private static JLabel timeLabel;
    private static boolean isHidden = false, isRunning = false, countdown = false, isFullScreen = false;
    private static int y = 50;
    private static double timer = 0, fadeTime = 0;
    private static int mainTime = 0;
    private static int width = 300, height = 120; // 初始大小
    private static Timer resizeTimer;
    private static int waitTimeAll, fullWaitTimeAll, textSizeAll;
    public static int R, G, B;
    private static Timer mainTimer;
    private static PopupMenu popupMenu;
    private static Menu sleepMenu, lowSleepMenu;
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
        try {
            GlobalScreen.registerNativeHook();
        } catch (Exception ignored) {}
        GlobalScreen.addNativeKeyListener(new FloatingClockTray());
        GlobalScreen.addNativeMouseListener(new FloatingClockTray());
        GlobalScreen.addNativeMouseMotionListener(new FloatingClockTray());
        GlobalScreen.addNativeMouseWheelListener(new FloatingClockTray());

        JsonObject dataObject = DataJsonReader.getDataObject();
        JsonObject languageObject = DataJsonReader.getLanguageObject();
        JsonObject timeTableObject = DataJsonReader.getTimeTableObject();
        String jsonPath = DataJsonReader.getJsonPath();
        Gson dataGson = DataJsonReader.getDataGson();
        File jsonFile = new File(jsonPath);

        String language = dataObject.get("language").getAsString();
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

        tray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().getImage(classLoader.getResource("icons/xxyxxdmc.png"));

        TrayIcon trayIcon = new TrayIcon(image, languageObject.get("clock").getAsString());
        trayIcon.setImageAutoSize(true);
        try {tray.add(trayIcon);} catch (AWTException ignored) {}

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setUndecorated(true);
        frame.setAlwaysOnTop(true);
        frame.setSize(width, height);
        frame.setLocation((screenSize.width - frame.getWidth()) / 2, y);
        frame.setType(Window.Type.UTILITY);

        frame.setShape(new RoundRectangle2D.Float(0, 0, width, height, 30, 30));

        timeLabel = new JLabel("", SwingConstants.CENTER);

        timeLabel.setFont(minecraftFont.deriveFont(Font.PLAIN, 48));
        frame.add(timeLabel);
        frame.getContentPane().setBackground(Color.BLACK);
        timeLabel.setForeground(new Color(R, G, B));

        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        Timer timer = new Timer(1000, e -> {
            updateTime();
            if (!MainToolBox.runningClass[0]) {

            }
        });
        timer.start();

        frame.setVisible(true);
        frame.setFocusableWindowState(false);
        frame.setEnabled(false);

        popupMenu = new PopupMenu();
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
                Color selectedColor = JColorChooser.showDialog(frame, languageObject.get("choose_color").getAsString(), colorPanel.getBackground());
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
        popupMenu.add(settingItem);

        MenuItem countdownItem = new MenuItem(languageObject.get("countdown").getAsString());
        countdownItem.addActionListener(e -> {
            countdown=!countdown;
            if (!countdown) countdownItem.setLabel(languageObject.get("countdown").getAsString());
            else countdownItem.setLabel(languageObject.get("countdown").getAsString()+" √");
        });
        popupMenu.add(countdownItem);
        MenuItem fullScreenItem = new MenuItem(languageObject.get("full_screen").getAsString());
        fullScreenItem.addActionListener(e -> {
            ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
            if (isHidden) showTheFrame();
            scheduledExecutorService.schedule(() -> {
                isRunning=false;
                isFullScreen=true;
                mainTime =0;
                enterFullScreen();
            },2,TimeUnit.SECONDS);
        });
        popupMenu.add(fullScreenItem);
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
        popupMenu.add(languageMenu);

        sleepMenu = new Menu(languageObject.get("sleep").getAsString());
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
        popupMenu.add(sleepMenu);
        sleepCustom.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(null, "Please enter minutes", "Custom", JOptionPane.PLAIN_MESSAGE);
            if (input==null) return;
            sleepInTime(Integer.parseInt(input));
            sleepMenu.setEnabled(false);
            lowSleepMenu.setEnabled(false);
        });
        lowSleepMenu = new Menu(languageObject.get("low_sleep").getAsString());
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
        popupMenu.add(lowSleepMenu);

        MenuItem exitItem = new MenuItem(languageObject.get("exit").getAsString());
        exitItem.addActionListener(e -> System.exit(0));
        popupMenu.add(exitItem);

        trayIcon.setPopupMenu(popupMenu);

        mainTimer = new Timer(1000, e -> {
            mainTime++;
            if (mainTime == waitTimeAll && isHidden) {
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

    public static void finish() {
        System.exit(0);
    }

    private static JSlider createSlider(int min, int max, int value) {
        JSlider slider = new JSlider(min, max, value);
        slider.setMajorTickSpacing(50);
        slider.setMinorTickSpacing(5);
        slider.setPaintTicks(false);
        slider.setPaintLabels(false);
        slider.setPreferredSize(new Dimension(250, 50)); // 增加宽度
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
        if (mainTimer ==null) return;
        mainTime=0;
        hideTheFrame();
    }

    public static void sleepInTime(int minutes) {
        mainTimer.stop();
        timer=0;
        mainTime =0;
        isFullScreen=false;
        isRunning=true;
        sleepType="sleep";
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(() -> {
            sleepType=null;
            isRunning=false;
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
        if (isRunning||isHidden) return;
        isRunning=true;
        if (isFullScreen) {
            isRunning=false;
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
                frame.setLocation((screenSize.width - frame.getWidth()) / 2, y);
            } else {
                timer = 0;
                isHidden = true;
                isRunning = false;
                frame.setVisible(false);
                mainTime=0;
                mainTimer.start();
                ((Timer) e.getSource()).stop();
            }
        });
        swingTimer.start();
    }
    private static void showTheFrame() {
        if (isRunning) return;
        isRunning=true;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Timer swingTimer = new Timer(20, e -> {
            if (timer<1){
                timer += 0.06;
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
    }

    private static void enterFullScreen(){
        if (isRunning) return;
        isRunning=true;
        mainTimer.stop();
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        resizeTimer = new Timer(20, e -> {
            if (timer < 1) {
                timer += 0.04;
                width = (int) Math.ceil(300 + ((screenSize.width - 300) * easeOutExpo(timer)));
                height = (int) Math.ceil(120 +((screenSize.height - 120) * easeOutExpo(timer)));
                y = 50 + (int) (((double) ((screenSize.height - frame.getHeight()) / 2) - 50) * easeOutExpo(timer));
                frame.setLocation((screenSize.width - frame.getWidth()) / 2, y);
                frame.setPreferredSize(new Dimension(width, height));
                frame.pack();
                frame.setShape(new RoundRectangle2D.Float(0, 0, width, height, 0, 0));
                timeLabel.setFont(minecraftFont.deriveFont(Font.PLAIN, (float) (48 + ((textSizeAll - 48) * easeOutExpo(timer)))));
            } else {
                frame.setLocation(((screenSize.width - frame.getWidth()) / 2), ((screenSize.height - frame.getHeight()) / 2));
                frame.setPreferredSize(screenSize);
                frame.pack();
                frame.setShape(new RoundRectangle2D.Double(0, 0, screenSize.width, screenSize.height, 30 * (1-easeOutExpo(timer)), 30 * (1-easeOutExpo(timer))));
                timer=0;
                isRunning=false;
                ((Timer) e.getSource()).stop();
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
        });
        resizeTimer.setRepeats(true);
        resizeTimer.start();
    }

    private static void exitFullScreen(){
        if (isRunning) return;
        isRunning=true;
        frame.setExtendedState(JFrame.NORMAL);
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        resizeTimer = new Timer(20, e -> {
            if (timer < 1) {
                timer += 0.04;
                width = (int) Math.ceil(screenSize.width + ((300 - screenSize.width) * easeOutExpo(timer)));
                height = (int) Math.ceil(screenSize.height + ((120 - screenSize.height) * easeOutExpo(timer)));
                y = (screenSize.height - frame.getHeight()) / 2 + (int) ((50 - (double) (screenSize.height - frame.getHeight()) / 2) * easeOutExpo(timer));
                frame.setLocation((screenSize.width - frame.getWidth()) / 2, y);
                frame.setPreferredSize(new Dimension(width, height));
                frame.pack();
                frame.setShape(new RoundRectangle2D.Double(0, 0, width, height, 30 * easeOutExpo(timer), 30 * easeOutExpo(timer)));
                timeLabel.setFont(minecraftFont.deriveFont(Font.PLAIN, (float) (textSizeAll + ((48 - textSizeAll) * easeOutExpo(timer)))));
            } else {
                timer=0;
                isRunning=false;
                frame.setShape(new RoundRectangle2D.Double(0, 0, width, height, 30, 30));
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

