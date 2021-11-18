package it.eja.surf;

import android.webkit.JavascriptInterface;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class Javascript {

    @JavascriptInterface
    public String shell(String uuid, String cmd) {
        if (uuid.equals(Setting.uuid)) {
            try {
                Process process = Runtime.getRuntime().exec(cmd);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream())
                );
                char[] buffer = new char[4096];
                StringBuilder output = new StringBuilder();
                int read;
                while ((read = reader.read(buffer)) > 0) {
                    output.append(buffer, 0, read);
                }
                reader.close();
                process.waitFor();
                return output.toString();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            return "";
        }
    }

    @JavascriptInterface
    public void fileDownload(String uuid, String url, String outputFile) {
        if (uuid.equals(Setting.uuid)) {
            try {
                URL u = new URL(url);
                URLConnection conn = u.openConnection();
                int contentLength = conn.getContentLength();
                DataInputStream stream = new DataInputStream(u.openStream());
                byte[] buffer = new byte[contentLength];
                stream.readFully(buffer);
                stream.close();
                DataOutputStream fos = new DataOutputStream(new FileOutputStream(MainActivity.filePath + File.separator + outputFile));
                fos.write(buffer);
                fos.flush();
                fos.close();
            } catch (IOException ignored) {
            }
        }
    }

    @JavascriptInterface
    public String fileRead(String uuid, String fileName) {
        if (uuid.equals(Setting.uuid)) {
            return MainActivity.fileRead(fileName);
        } else {
            return "";
        }
    }

    @JavascriptInterface
    public void fileWrite(String uuid, String fileName, String value) {
        if (uuid.equals(Setting.uuid)) {
            MainActivity.fileWrite(fileName, value);
        }
    }

    @JavascriptInterface
    public String settingGetAll(String uuid) throws JSONException {
        if (uuid.equals(Setting.uuid)) {
            return Setting.load();
        } else {
            return "";
        }
    }

    @JavascriptInterface
    public String settingGet(String uuid, String name) throws JSONException {
        if (uuid.equals(Setting.uuid)) {
            if (name.isEmpty()) {
                return Setting.load();
            } else {
                return Setting.eja.getString(name);
            }
        } else {
            return "";
        }
    }

    @JavascriptInterface
    public void settingPut(String uuid, String name, String value) throws JSONException {
        if (uuid.equals(Setting.uuid)) {
            Setting.eja.put(name, value);
            Setting.save();
        }
    }

    @JavascriptInterface
    public void settingInit(String uuid) throws JSONException {
        if (uuid.equals(Setting.uuid)) {
            Setting.load();
        }
    }

    @JavascriptInterface
    public void bookAdd(String uuid, String value) {
        if (uuid.equals(Setting.uuid)) {
            Setting.bookAdd(value);
        }
    }

    @JavascriptInterface
    public String bookRead(String uuid) {
        if (uuid.equals(Setting.uuid)) {
            return MainActivity.fileRead("eja.book");
        } else {
            return "";
        }
    }

    @JavascriptInterface
    public void bookRemove(String uuid, String index) {
        if (uuid.equals(Setting.uuid)) {
            Setting.bookRemove(Integer.parseInt(index));
        }
    }

    @JavascriptInterface
    public void reset(String uuid) {
        if (uuid.equals(Setting.uuid)) {
            MainActivity.webReset();
        }
    }
}
