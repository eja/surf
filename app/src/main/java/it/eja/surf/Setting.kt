// Copyright (C) by Ubaldo Porcheddu <ubaldo@eja.it>

package it.eja.surf

import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File
import java.io.IOException
import java.net.URL
import java.nio.charset.StandardCharsets

object Setting {
    var home = "http://surf.eja.it"
    var doh = "http://surf.eja.it"
    var path = ""
    var version = 0
    var reset = true
    var eja = JSONObject()
    var book = JSONArray()

    fun load() {
        val data = readInternal("eja.json")
        if (data.isNotEmpty()) {
            eja = JSONObject(data)
        } else {
            eja.put("home", home)
            save()
        }

        home = if (home.startsWith("http")) home else "http://$home"

        reset = eja.optBoolean("reset", reset)
        doh = eja.optString("doh", doh)

        if (eja.optBoolean("proxy", false)) {
            val sHost = eja.optString("socksHost")
            val sPort = eja.optString("socksPort")
            if (sHost.isNotEmpty()) System.setProperty("socksProxyHost", sHost)
            if (sPort.isNotEmpty()) System.setProperty("socksProxyPort", sPort)
        }

        book = eja.optJSONArray("bookmarks") ?: JSONArray()
    }

    fun save() {
        eja.put("bookmarks", book)
        writeInternal("eja.json", eja.toString())
    }

    fun bookAdd(value: String) {
        for (i in 0 until book.length()) {
            if (book.getString(i) == value) return
        }
        book.put(value)
        save()
    }

    fun bookEdit(index: Int, newValue: String) {
        if (index >= 0 && index < book.length()) {
            book.put(index, newValue)
            save()
        }
    }

    fun bookRemove(index: Int) {
        if (index >= 0 && index < book.length()) {
            book.remove(index)
            save()
        }
    }

    fun bookSwap(i: Int, j: Int) {
        if (i in 0 until book.length() && j in 0 until book.length()) {
            val temp = book.getString(i)
            book.put(i, book.getString(j))
            book.put(j, temp)
            save()
        }
    }

    private fun readInternal(fileName: String): String {
        val filePath = "$path${File.separator}$fileName"
        val file = File(filePath)
        if (!file.exists()) return ""
        return try {
            file.readText()
        } catch (e: IOException) {
            ""
        }
    }

    private fun writeInternal(fileName: String, value: String) {
        val filePath = "$path${File.separator}$fileName"
        try {
            File(filePath).writeText(value)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun dohToIp(domain: String): String {
        val baos = ByteArrayOutputStream()
        val query = DataOutputStream(baos)
        var ip = "0.0.0.0"
        try {
            val id = (Math.random() * 0xffff).toInt()
            query.apply {
                writeShort(id)
                writeShort(0x0100)
                writeShort(0x0001)
                writeShort(0x0000)
                writeShort(0x0000)
                writeShort(0x0000)
                domain.split('.').forEach { level ->
                    writeByte(level.length)
                    write(level.toByteArray(StandardCharsets.UTF_8))
                }
                writeByte(0x00)
                writeShort(0x0001)
                writeShort(0x0001)
            }

            val encodedQuery = Base64.encodeToString(baos.toByteArray(), Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
            val url = URL("$doh?dns=$encodedQuery")

            url.openStream().use { input ->
                val br = ByteArray(1024)
                val bw = baos.toByteArray()
                val bytesRead = input.read(br)

                var x = bw.size
                if (br[0] == bw[0] && br[1] == bw[1]) {
                    while (x < bytesRead && br[x] == 0xc0.toByte()) {
                        if (br[x + 3].toInt() == 0x01 && br[x + 11].toInt() == 4) {
                            ip = "${br[x + 12].toInt() and 0xff}.${br[x + 13].toInt() and 0xff}.${br[x + 14].toInt() and 0xff}.${br[x + 15].toInt() and 0xff}"
                            break
                        } else {
                            x += 11 + 1 + br[x + 11]
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ip
    }
}