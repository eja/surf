// Copyright (C) 2021-2024 by Ubaldo Porcheddu <ubaldo@eja.it>

package it.eja.surf

import android.webkit.JavascriptInterface
import org.json.JSONException
import java.io.*
import java.net.URL

class Javascript {

    private fun authCheck(): Boolean {
        return Setting.host == MainActivity.hostCurrent
    }

    @JavascriptInterface
    fun shell(cmd: String?): String {
        if (!authCheck() || cmd == null) return ""
        return try {
            val process = Runtime.getRuntime().exec(cmd)
            val output = StringBuilder()
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                val buffer = CharArray(4096)
                var read: Int
                while (reader.read(buffer).also { read = it } > 0) {
                    output.append(buffer, 0, read)
                }
            }
            process.waitFor()
            output.toString()
        } catch (e: IOException) {
            ""
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            ""
        }
    }

    @JavascriptInterface
    fun fileDownload(url: String?, outputFile: String) {
        if (!authCheck() || url.isNullOrEmpty()) return
        try {
            val u = URL(url)
            val conn = u.openConnection()
            val contentLength = conn.contentLength
            DataInputStream(u.openStream()).use { stream ->
                val buffer = ByteArray(contentLength)
                stream.readFully(buffer)
                DataOutputStream(FileOutputStream("${Setting.path}${File.separator}$outputFile")).use { fos ->
                    fos.write(buffer)
                    fos.flush()
                }
            }
        } catch (ignored: IOException) {
        }
    }

    @JavascriptInterface
    fun fileRead(fileName: String?): String {
        return if (authCheck()) {
            Setting.fileRead(fileName)
        } else {
            ""
        }
    }

    @JavascriptInterface
    fun fileWrite(fileName: String?, value: String?) {
        if (authCheck()) {
            Setting.fileWrite(fileName, value)
        }
    }

    @JavascriptInterface
    @Throws(JSONException::class)
    fun settingGetAll(): String {
        return if (authCheck()) {
            Setting.load()
        } else {
            ""
        }
    }

    @JavascriptInterface
    @Throws(JSONException::class)
    fun settingGet(name: String): String? {
        return if (authCheck()) {
            if (name.isEmpty()) {
                Setting.load()
            } else {
                Setting.eja.getString(name)
            }
        } else {
            ""
        }
    }

    @JavascriptInterface
    @Throws(JSONException::class)
    fun settingPut(name: String?, value: String?) {
        if (authCheck()) {
            Setting.eja.put(name, value)
            Setting.save()
        }
    }

    @JavascriptInterface
    @Throws(JSONException::class)
    fun settingInit() {
        if (authCheck()) {
            Setting.load()
        }
    }

    @JavascriptInterface
    fun bookAdd(value: String) {
        if (authCheck()) {
            Setting.bookAdd(value)
        }
    }

    @JavascriptInterface
    fun bookRead(): String {
        return if (authCheck()) {
            Setting.fileRead("eja.book")
        } else {
            ""
        }
    }

    @JavascriptInterface
    fun bookRemove(index: String) {
        if (authCheck()) {
            try {
                Setting.bookRemove(index.toInt())
            } catch (e: NumberFormatException) {
                // Handle the error, maybe log it
            }
        }
    }
}