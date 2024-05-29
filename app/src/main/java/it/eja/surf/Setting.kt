// Copyright (C) 2021-2024 by Ubaldo Porcheddu <ubaldo@eja.it>

package it.eja.surf

import android.util.Base64
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*

object Setting {
    var host = "eja.surf"
    var home = ""
    var path = ""
    var doh = ""
    var version = 0
    var reset = false
    var eja = JSONObject()
    var book = JSONArray()

    @Throws(JSONException::class)
    fun load(): String {
        val data = fileRead("eja.json")
        if (data.isNotEmpty()) {
            eja = JSONObject(data)
        } else {
            eja.put("host", host)
            save()
        }
        eja.put("version", version)
        host = eja.optString("host", host)
        reset = eja.optBoolean("reset", reset)
        doh = eja.optString("doh", doh)

        if (eja.optBoolean("proxy", false)) {
            eja.optString("socksHost")?.let { System.setProperty("socksProxyHost", it) }
            eja.optString("socksPort")?.let { System.setProperty("socksProxyPort", it) }
        }

        fileRead("eja.book").takeIf { it.isNotEmpty() }?.let { book = JSONArray(it) }

        home = "https://$host/"
        return eja.toString()
    }

    fun save() {
        fileWrite("eja.json", eja.toString())
    }

    fun bookAdd(value: String) {
        if (value.lowercase(Locale.getDefault()).startsWith("http")) {
            book.put(value)
            fileWrite("eja.book", book.toString())
        }
    }

    fun bookRemove(index: Int?) {
        index?.let {
            book.remove(it)
            fileWrite("eja.book", book.toString())
        }
    }

    fun fileRead(fileName: String?): String {
        val contentBuilder = StringBuilder()
        val filePath = "$path${File.separator}$fileName"
        val file = File(filePath)
        if (file.exists()) {
            try {
                BufferedReader(FileReader(filePath)).use { br ->
                    br.forEachLine { line ->
                        contentBuilder.append(line).append("\n")
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return contentBuilder.toString()
    }

    fun fileWrite(fileName: String?, value: String?) {
        val filePath = "$path${File.separator}$fileName"
        try {
            BufferedWriter(OutputStreamWriter(FileOutputStream(filePath))).use { writer ->
                writer.write(value)
            }
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

            url.openStream().use { `in` ->
                val br = ByteArray(1024)
                val bw = baos.toByteArray()
                val bytesRead = `in`.read(br)

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