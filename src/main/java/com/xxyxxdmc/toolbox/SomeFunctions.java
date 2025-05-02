package main.java.com.xxyxxdmc.toolbox;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SomeFunctions {
    public static boolean checkProcessIsOrNotRunning(String targetProcess) {
        Process process;
        try {
            process = Runtime.getRuntime().exec("jps -l");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line = null;
        while (true) {
            try {if ((line = reader.readLine()) == null) break;
            } catch (IOException ignored) { }
            assert line != null;
            if (line.contains(targetProcess)) {
                System.out.println(line);
                return true;
            }
        }
        return false;
    }
    public static long getProcessPID(String targetProcess) {
        Process process;
        try {
            process = Runtime.getRuntime().exec("jps -l");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line = null;
        while (true) {
            try {if ((line = reader.readLine()) == null) break;
            } catch (IOException ignored) { }
            assert line != null;
            if (line.contains(targetProcess)) System.out.println(line);
        }
        return 0;
    }
    public static void stopProcess(long pid) {
        ProcessHandle.of(pid).ifPresent(ProcessHandle::destroy);
    }
    public static JSlider createSlider(int min, int max, int value) {
        JSlider slider = new JSlider(min, max, value);
        slider.setMajorTickSpacing(50);
        slider.setMinorTickSpacing(5);
        slider.setPaintTicks(false);
        slider.setPaintLabels(false);
        slider.setPreferredSize(new Dimension(250, 50)); // Ôö¼Ó¿í¶È
        return slider;
    }

    public static JPanel createRow(JComponent label, JComponent input, JComponent value) {
        JPanel rowPanel = new JPanel(new BorderLayout(10, 10));
        rowPanel.add(label, BorderLayout.WEST);
        rowPanel.add(input, BorderLayout.CENTER);
        rowPanel.add(value, BorderLayout.EAST);
        return rowPanel;
    }
}
