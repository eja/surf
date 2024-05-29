# eja.surf

The idea behind this browser is to implement all known privacy features allowed by Android while keeping the code as simple as possible to read and understand.

A group of JavaScript callback functions is also in place to allow anyone to design a new landing page and funnel all URL and search operations through their own server, thus maintaining as much control as possible.

## UX

The browser interface is intentionally simple and clean. Write something in the search box and click the search button on the keyboard. If the search term is a valid URL, you will be redirected; otherwise, the search will be conducted via a custom Google search engine.

The custom search engine will also group the results by official international encyclopedias and news agencies.

Pulling down the page from the top border (refreshing) will bring you to the home page.

A long click on any link will allow you to add that link to the bookmark list, replacing the usual and confusing multi-million background open windows.

On the right side of the search bar, there are two buttons. The first, from the left, is to manage the bookmarks, and the second is to manage the settings of the browser.

## Setup Page

You can host the whole browser logic on your own server by simply copying and adapting the eja.surf HTML/CSS/JS, thus allowing you deeper control of the browser UI. By changing this field, you must ensure that your URL is compatible with the browser JavaScript interface; otherwise, you will be stuck on that page until you reinstall the browser.

## Settings

### Delete Everything on Exit

Delete any cookies, history, cache, etc., on start.

### DNS over HTTPS

Use any DoH provider to reject ads, malware, etc.

### Proxy

Choose whether to use a SOCKS4/5 proxy or not.

### SOCKS Host

The proxy SOCKS hostname/IP.

### SOCKS Port

The proxy SOCKS port.

Restart the app for any changes to take effect.
