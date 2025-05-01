package main.java.com.xxyxxdmc.toolbox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Test {
    public static void main(String[] args) throws IOException {
        Process process = Runtime.getRuntime().exec("jps -l");
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println("Java 应用正在运行：" + line);
        }
    }
}
