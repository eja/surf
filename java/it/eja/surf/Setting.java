package it.eja.surf;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class Setting {
    public static String host = "eja.surf";
    public static String home = "";
    public static String uuid = "";
    public static int version = 0;
    public static JSONObject eja = new JSONObject();
    public static JSONArray book = new JSONArray();
    public static JSONArray allow = new JSONArray();
    public static JSONArray block = new JSONArray();

    public static String load() throws JSONException {
        String data;
        data = MainActivity.fileRead("eja.json");
        if (data.length() > 0) {
            eja = new JSONObject(data);
            if (!eja.getString("host").isEmpty()) {
                host = eja.getString("host");
            }
        } else {
            eja.put("host", host);
            eja.put("reset", true);
            Setting.save();
        }
        eja.put("uuid", uuid);
        eja.put("version", version);
        home = String.format("https://%s/?uuid=%s", host, uuid);
        data = MainActivity.fileRead("eja.book");
        if (data.length() > 0) {
            book = new JSONArray(data);
        }
        if (eja.has("allow") && !eja.getString("allow").isEmpty()) {
            data = MainActivity.fileRead("eja.allow");
            if (data.length() > 0) {
                allow = new JSONArray(data);
            }
        }
        if (eja.has("block") && !eja.getString("block").isEmpty()) {
            data = MainActivity.fileRead("eja.block");
            if (data.length() > 0) {
                block = new JSONArray(data);
            }
        }
        return eja.toString();
    }

    public static void save() {
        MainActivity.fileWrite("eja.json", eja.toString());
    }

    public static void bookAdd(String value) {
        if (value.toLowerCase().startsWith("http")) {
            book.put(value);
            MainActivity.fileWrite("eja.book", book.toString());
        }
    }

    public static void bookRemove(Integer index) {
        book.remove(index);
        MainActivity.fileWrite("eja.book", book.toString());
    }
}
