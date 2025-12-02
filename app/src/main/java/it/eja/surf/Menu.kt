// Copyright (C) by Ubaldo Porcheddu <ubaldo@eja.it>

package it.eja.surf

import android.app.AlertDialog
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

class Menu(private val main: MainActivity) {

    fun show() {
        val container = LinearLayout(main).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        val options = arrayOf("Settings", "Bookmarks", "Find in Page")

        val listView = ListView(main).apply {
            adapter = ArrayAdapter(main, android.R.layout.simple_list_item_1, options)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        val inputContainer = LinearLayout(main).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(20, 0, 20, 20)
            gravity = Gravity.CENTER_VERTICAL
            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 0, 0, 0)
            layoutParams = params
        }

        val urlInput = EditText(main).apply {
            hint = "Search or enter address"
            setText(main.webView.url)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            imeOptions = EditorInfo.IME_ACTION_GO
            setSingleLine()
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        inputContainer.addView(urlInput)
        container.addView(listView)
        container.addView(inputContainer)

        val dialog = AlertDialog.Builder(main)
            .setView(container)
            .setPositiveButton("Go") { _, _ ->
                main.processSearch(urlInput.text.toString())
            }
            .setNeutralButton("Home") { _, _ ->
                main.webView.loadUrl(Setting.home)
            }
            .setNegativeButton("Add") { _, _ ->
                val url = urlInput.text.toString()
                if (url.isNotEmpty()) {
                    Setting.bookAdd(url)
                    Toast.makeText(main, "Saved", Toast.LENGTH_SHORT).show()
                }
            }
            .create()

        listView.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> showSettings()
                1 -> showBookmarks()
                2 -> main.showFindMode()
            }
            dialog.dismiss()
        }

        urlInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE) {
                main.processSearch(urlInput.text.toString())
                dialog.dismiss()
                true
            } else false
        }

        dialog.show()
    }

    private fun showBookmarks() {
        val bookList = ArrayList<String>()
        for (i in 0 until Setting.book.length()) bookList.add(Setting.book.optString(i))

        val adapter = ArrayAdapter(main, android.R.layout.simple_list_item_1, bookList)
        val listView = ListView(main)
        listView.adapter = adapter
        listView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val dialog = AlertDialog.Builder(main)
            .setTitle("Bookmarks")
            .setView(listView)
            .setPositiveButton("Add Page") { _, _ ->
                val url = main.webView.url
                if (url != null && url.startsWith("http")) {
                    Setting.bookAdd(url)
                    Toast.makeText(main, "Saved", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Back") { _, _ -> show() }
            .create()

        listView.setOnItemClickListener { _, _, position, _ ->
            val url = bookList[position]
            main.processSearch(url)
            dialog.dismiss()
        }

        listView.setOnItemLongClickListener { _, _, position, _ ->
            val options = arrayOf("Edit", "Delete", "Move Up", "Move Down")
            AlertDialog.Builder(main)
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> showEditBookmark(position)
                        1 -> Setting.bookRemove(position)
                        2 -> Setting.bookSwap(position, position - 1)
                        3 -> Setting.bookSwap(position, position + 1)
                    }
                    dialog.dismiss()
                    if (which != 0) showBookmarks()
                }
                .show()
            true
        }

        dialog.show()
    }

    private fun showEditBookmark(index: Int) {
        val currentUrl = Setting.book.optString(index)
        val input = EditText(main).apply {
            setText(currentUrl)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
        }

        AlertDialog.Builder(main)
            .setTitle("Edit Bookmark")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                Setting.bookEdit(index, input.text.toString())
                showBookmarks()
            }
            .setNegativeButton("Cancel") { _, _ -> showBookmarks() }
            .show()
    }

    private fun showSettings() {
        val layout = LinearLayout(main).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }
        val inputHost = EditText(main).apply { hint = "Home Page"; setText(Setting.home) }
        val inputDoh = EditText(main).apply { hint = "DNS over HTTPS"; setText(Setting.doh) }
        val checkReset = CheckBox(main).apply { text = "Clear Data on Exit"; isChecked = Setting.reset }
        val inputSocksHost = EditText(main).apply { hint = "SOCKS Host"; setText(System.getProperty("socksProxyHost") ?: "") }
        val inputSocksPort = EditText(main).apply { hint = "SOCKS Port"; inputType = InputType.TYPE_CLASS_NUMBER; setText(System.getProperty("socksProxyPort") ?: "") }

        layout.addView(TextView(main).apply { text = "Settings"; textSize = 20f })
        layout.addView(inputHost)
        layout.addView(inputDoh)
        layout.addView(inputSocksHost)
        layout.addView(inputSocksPort)
        layout.addView(checkReset)

        AlertDialog.Builder(main)
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                Setting.home = inputHost.text.toString()
                Setting.doh = inputDoh.text.toString()
                Setting.reset = checkReset.isChecked

                Setting.home = if (Setting.home.startsWith("http")) Setting.home else "http://${Setting.home}"

                Setting.eja.put("home", Setting.home)
                Setting.eja.put("doh", Setting.doh)
                Setting.eja.put("reset", Setting.reset)

                val sHost = inputSocksHost.text.toString()
                val sPort = inputSocksPort.text.toString()
                if (sHost.isNotEmpty() && sPort.isNotEmpty()) {
                    Setting.eja.put("proxy", true)
                    Setting.eja.put("socksHost", sHost)
                    Setting.eja.put("socksPort", sPort)
                    System.setProperty("socksProxyHost", sHost)
                    System.setProperty("socksProxyPort", sPort)
                } else {
                    Setting.eja.put("proxy", false)
                    System.clearProperty("socksProxyHost")
                    System.clearProperty("socksProxyPort")
                }
                Setting.save()
                Toast.makeText(main, "Saved", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}