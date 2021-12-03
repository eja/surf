package it.eja.surf;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class Setting {
    public static boolean reset = true;
    public static String host = "eja.surf";
    public static String home = "";
    public static String path = "";
    public static int version = 0;
    public static JSONObject eja = new JSONObject();
    public static JSONArray book = new JSONArray();
    public static JSONArray allow = new JSONArray();
    public static JSONArray block = new JSONArray();

    public static String load() throws JSONException {
        String data = fileRead("eja.json");
        if (data.length() > 0) {
            eja = new JSONObject(data);
            if (!eja.getString("host").isEmpty()) {
                host = eja.getString("host");
            }
        } else {
            eja.put("host", host);
            eja.put("reset", reset);
            Setting.save();
        }
        eja.put("version", version);
        home = String.format("https://%s/", host);
        if (eja.has("reset")) {
            reset = eja.getBoolean("reset");
        }
        if (Setting.eja.has("proxy") && Setting.eja.getBoolean("proxy")) {
            if (Setting.eja.has("socksHost") && Setting.eja.has("socksPort")) {
                System.setProperty("socksProxyHost", Setting.eja.getString("socksHost"));
                System.setProperty("socksProxyPort", Setting.eja.getString("socksPort"));
            }
        }
        data = fileRead("eja.book");
        if (data.length() > 0) {
            book = new JSONArray(data);
        }
        if (eja.has("allow") && !eja.getString("allow").isEmpty()) {
            data = fileRead("eja.allow");
            if (data.length() > 0) {
                allow = new JSONArray(data);
            }
        }
        if (eja.has("block") && !eja.getString("block").isEmpty()) {
            data = fileRead("eja.block");
            if (data.length() > 0) {
                block = new JSONArray(data);
            }
        }
        return eja.toString();
    }

    public static void save() {
        fileWrite("eja.json", eja.toString());
    }

    public static void bookAdd(String value) {
        if (value.toLowerCase().startsWith("http")) {
            book.put(value);
            fileWrite("eja.book", book.toString());
        }
    }

    public static void bookRemove(Integer index) {
        book.remove(index);
        fileWrite("eja.book", book.toString());
    }

    public static String fileRead(String fileName) {
        StringBuilder contentBuilder = new StringBuilder();
        String filePath = String.format("%s%s%s", path, File.separator, fileName);
        File file = new File(filePath);
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String sCurrentLine;
                while ((sCurrentLine = br.readLine()) != null) {
                    contentBuilder.append(sCurrentLine).append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return contentBuilder.toString();
    }

    public static void fileWrite(String fileName, String value) {
        String filePath = String.format("%s%s%s", path, File.separator, fileName);
        Writer writer;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(filePath)));
            writer.write(value);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean checkList(JSONArray list, String value) {
        for (int i = 0; i < list.length(); i++) {
            try {
                if (value.equals(list.getString(i))) {
                    return true;
                } else if (list.getString(i).startsWith("*") && value.endsWith(list.getString(i).substring(1))) {
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}