# eja.surf

The idea behind this browser is to try to implement all known privacy features allowed by Android while keeping the code as easier as possible to read and understand.

A group of javascript callback functions is also in place to allow anyone to design a new landing page and funnel all url and search operation through your own server thus keeping as much control as possible under your hands.

Any contribution of course and usual more than welcome! :)

# ux
the browser interface is intentionally simple and clean, write something on the search box and click the search button on the keyboard, if the searching term is a valid url you will be redirected otherwise the search will be made via a custom google search engine.

The custom search engine will also group the results by official internationals enciclopedias and news agencies.

Pulling down the page from the top border (refreshing) will bring you to the home page.

A long click on any link will allow you to add that link to the bookmark list, this will replace the usual and confusing multi million background open windows.

At the right side of the search bar there are 2 buttons, the first from the left is to manage the bookmarks, the second is to manage the settings of the browser.

### Setup Page
You can host the whole browser logic on your own server by simply copying and adapting eja.surf html/css/js, thus allowing you a deeper control of the browser UI. By changing this field you must be sure that your url is compatible with the browser javascript interface otherwise you will be stuck on that page until you reinstall the browser.

### Delete everything on Exit
Delete any cookie/history/cache/etc. on start

### DNS over HTTPS
Use any DoH provider to reject ads, malware, etc...

### IP block list
You can choose what kind of content you would like to block, the lists are populated from well known adblocking projects

### Proxy
Choose whether to use a socks4/5 proxy or not

### SOCKS Host
The proxy socks hostname/ip

### SOCKS Port 
The proxy socks port

Restart the app for any change to take effect.
