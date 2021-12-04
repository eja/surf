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

    private boolean authCheck() {
        return Setting.host.equals(MainActivity.hostCurrent);
    }

    @JavascriptInterface
    public String shell(String cmd) {
        if (authCheck()) {
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
    public void fileDownload(String url, String outputFile) {
        if (authCheck()) {
            try {
                URL u = new URL(url);
                URLConnection conn = u.openConnection();
                int contentLength = conn.getContentLength();
                DataInputStream stream = new DataInputStream(u.openStream());
                byte[] buffer = new byte[contentLength];
                stream.readFully(buffer);
                stream.close();
                DataOutputStream fos = new DataOutputStream(new FileOutputStream(Setting.path + File.separator + outputFile));
                fos.write(buffer);
                fos.flush();
                fos.close();
            } catch (IOException ignored) {
            }
        }
    }

    @JavascriptInterface
    public String fileRead(String fileName) {
        if (authCheck()) {
            return Setting.fileRead(fileName);
        } else {
            return "";
        }
    }

    @JavascriptInterface
    public void fileWrite(String fileName, String value) {
        if (authCheck()) {
            Setting.fileWrite(fileName, value);
        }
    }

    @JavascriptInterface
    public String settingGetAll() throws JSONException {
        if (authCheck()) {
            return Setting.load();
        } else {
            return "";
        }
    }

    @JavascriptInterface
    public String settingGet(String name) throws JSONException {
        if (authCheck()) {
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
    public void settingPut(String name, String value) throws JSONException {
        if (authCheck()) {
            Setting.eja.put(name, value);
            Setting.save();
        }
    }

    @JavascriptInterface
    public void settingInit() throws JSONException {
        if (authCheck()) {
            Setting.load();
        }
    }

    @JavascriptInterface
    public void bookAdd(String value) {
        if (authCheck()) {
            Setting.bookAdd(value);
        }
    }

    @JavascriptInterface
    public String bookRead() {
        if (authCheck()) {
            return Setting.fileRead("eja.book");
        } else {
            return "";
        }
    }

    @JavascriptInterface
    public void bookRemove(String index) {
        if (authCheck()) {
            Setting.bookRemove(Integer.parseInt(index));
        }
    }
}