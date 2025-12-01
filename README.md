# Surf

The idea behind this browser is to implement all known privacy features allowed by Android while keeping the code as simple as possible to read and understand.

## User Interface and Navigation

The browser interface is designed to be minimalistic. The navigation toolbar automatically hides during browsing to provide an immersive full-screen experience.

To reveal the toolbar and navigation controls, you may perform one of the following actions:
*   Scroll the page upward.
*   Perform a "swipe to refresh" gesture (pulling down from the top).
*   Press the device's Menu or Search key.

The search bar functions as a dual-purpose input field. If a valid URL is entered, the browser navigates directly to the address. If a search term is entered, it is processed via the configured Home Page URL.

## Menu and Features

Access to browser functions is provided via the menu button (vertical ellipsis) located on the right side of the navigation bar. The menu provides the following options:

### Home
Navigates the browser immediately to the defined Home Page.

### Bookmarks
The bookmark system has been updated to allow for list-based management.
*   **Add Bookmark:** Select "Add Page" within the Bookmarks menu to save the current URL.
*   **Manage Bookmarks:** A long-press on any existing bookmark reveals options to **Edit** the URL, **Delete** the entry, or reorder the list by moving items **Up** or **Down**.

### Settings
Configures the core behavior and network settings of the application.

### Find in Page
Activates a dedicated search interface at the top of the screen. This allows you to search for specific text within the currently loaded webpage. The interface includes controls to navigate to the previous or next occurrence and a button to close the search mode.

### Reload Page
Refreshes the current webpage.

## Configuration

The Settings menu allows for detailed customization of the browser's network and privacy behavior. Changes require an application restart to take effect.

### Home Page
You may define a custom URL to serve as the landing page. This URL also handles search queries generated from the main input bar. By hosting the browser logic on a private server, you can adapt the HTML, CSS, and JavaScript of the landing page to control the browser's UI and search funnel.

### DNS over HTTPS (DoH)
The browser supports DNS over HTTPS. This feature encrypts DNS queries, preventing third parties from intercepting or manipulating domain resolution requests. Additionally, this mechanism facilitates content filtering; by utilizing a DoH provider that resolves specific domains to a null address—such as the default provider—the browser effectively blocks advertisements and unwanted trackers.

### Clear Data on Exit
When enabled, the application will automatically purge all session data upon termination. This includes cookies, browsing history, cache, form data, and local storage.

### Proxy Configuration
The browser supports traffic tunneling via SOCKS4 or SOCKS5 proxies.
*   **SOCKS Host:** The IP address or hostname of the proxy server.
*   **SOCKS Port:** The port number of the proxy server.

If these fields are left empty, the proxy connection is disabled.
