package it.eja.surf;

import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Setting {
    public static String host = "eja.surf";
    public static String home = "";
    public static String path = "";
    public static String doh = "";
    public static int version = 0;
    public static boolean reset = false;
    public static JSONObject eja = new JSONObject();
    public static JSONArray book = new JSONArray();
    public static JSONArray allow = new JSONArray();
    public static JSONArray block = new JSONArray();

    public static String load() throws JSONException {
        String data = fileRead("eja.json");
        if (data.length() > 0) {
            eja = new JSONObject(data);
        } else {
            eja.put("host", host);
            Setting.save();
        }
        eja.put("version", version);
        if (eja.has("host")) {
            host = eja.getString("host");
        }
        if (eja.has("reset")) {
            reset = eja.getBoolean("reset");
        }
        if (eja.has("doh")) {
            doh = eja.getString("doh");
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
        home = String.format("https://%s/", host);
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

    public static String dohToIp(String domain) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream query = new DataOutputStream(baos);
        String ip = "0.0.0.0";
        try {
            int id = (int) (Math.random() * 0xffff);
            query.writeShort(id);
            query.writeShort(0x0100);
            query.writeShort(0x0001);
            query.writeShort(0x0000);
            query.writeShort(0x0000);
            query.writeShort(0x0000);
            String[] levels = domain.split("\\.");
            for (String level : levels) {
                byte[] bytes = level.getBytes(StandardCharsets.UTF_8);
                query.writeByte(bytes.length);
                query.write(bytes);
            }
            query.writeByte(0x00);
            query.writeShort(0x0001);
            query.writeShort(0x0001);
            byte[] br = new byte[1024];
            byte[] bw = baos.toByteArray();
            InputStream in = new URL(String.format("%s?dns=%s", Setting.doh, Base64.encodeToString(bw, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP))).openStream();
            int l = in.read(br);
            int x = bw.length;
            if (br[0] == bw[0] && br[1] == bw[1]) {
                while (x < l && br[x] == (byte) 0xc0) {
                    if (br[x + 3] == 0x01 && br[x + 11] == 4) {
                        ip = String.format("%d.%d.%d.%d", br[x + 12] & 0xff, br[x + 13] & 0xff, br[x + 14] & 0xff, br[x + 15] & 0xff);
                        break;
                    } else {
                        x = x + 11 + 1 + br[x + 11];
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ip;
    }
}