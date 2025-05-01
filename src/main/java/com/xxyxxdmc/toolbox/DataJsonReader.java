package main.java.com.xxyxxdmc.toolbox;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class DataJsonReader {
    private static final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    private static final JsonObject dataObject;
    private static final JsonObject timeTableObject;
    private static final JsonObject languageObject;
    private static final String jarDir;
    private static final String jsonPath;
    private static final Gson dataGson;
    static {
        jarDir = System.getProperty("user.dir");
        jsonPath = jarDir + "/data.json";
        File jsonFile = new File(jsonPath);
        if (!jsonFile.exists()) { createDefaultJSON(Paths.get(jsonPath));}
        dataGson = new Gson();
        FileReader fileReader = null;
        try {fileReader = new FileReader(jsonPath);} catch (FileNotFoundException ignored) {}
        assert fileReader != null;
        dataObject = dataGson.fromJson(fileReader, JsonObject.class);
        try {fileReader.close();} catch (IOException ignored) {}
        String language = dataObject.get("language").getAsString();
        InputStream languageStream = classLoader.getResourceAsStream(String.format("lang/%s.json", language));
        assert languageStream != null;
        String languageText = new Scanner(languageStream, StandardCharsets.UTF_8).useDelimiter("\\A").next();
        InputStream timeTableStream = classLoader.getResourceAsStream("schedule/time_table.json");
        assert timeTableStream != null;
        String timeTableText = new Scanner(timeTableStream, StandardCharsets.UTF_8).useDelimiter("\\A").next();
        Gson timeTableGson = new Gson();
        timeTableObject = timeTableGson.fromJson(timeTableText, JsonObject.class);
        Gson languageGson = new Gson();
        languageObject = languageGson.fromJson(languageText, JsonObject.class);
    }
    public static void createDefaultJSON(Path path) {
        String defaultJSON = "{\"language\":\"zh_cn\",\"color\":\"#FF6D23\",\"full_screen_wait_time\":\"300\",\"wait_time\":\"60\",\"text_size\":\"260\"}";
        try {Files.write(path, defaultJSON.getBytes());} catch (IOException ignored) {}
    }
    public static String getJarDir() {
        return jarDir;
    }
    public static String getJsonPath() {
        return jsonPath;
    }
    public static Gson getDataGson() {
        return dataGson;
    }
    public static JsonObject getDataObject() {
        return dataObject;
    }
    public static JsonObject getLanguageObject() {
        return languageObject;
    }
    public static JsonObject getTimeTableObject() {
        return timeTableObject;
    }
}
