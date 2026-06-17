# Surf

The idea behind this browser is to implement all known privacy features allowed by Android while keeping the code as simple as possible to read and understand.

## User Interface and Navigation

The browser interface is designed to be minimalistic. The navigation controls and address bar are completely hidden during browsing to provide an immersive full-screen experience.

To open the main menu interface—which contains the address bar and navigation controls—you may perform one of the following actions:
*   Press the back button or perform the back gesture on your device.
*   Press the hardware Menu or Search keys (if available on your device).

To return to the full-screen browser view, simply scroll down on the page. This action hides the toolbar container and returns focus back to the web content.

## Menu and Features

The menu serves as the central control hub for the browser. It combines navigation buttons, the address bar, and feature options into a single dialog.

**Control Options:**
The dialog features a multi-purpose text input field at the bottom accompanied by three primary action buttons:
*   **Go:** Navigates to the address currently in the input field (automatically prepending `https://` if no scheme is specified).
*   **Search:** Submits the input field content as a search query via the configured Home Page URL.
*   **Page Back / Close Browser:** A dynamic button that either navigates back in the page history or, if no back history exists in the current session, exits the application.

**Menu Options:**
*   **Settings:** Configures the core behavior and network settings of the application.
*   **Bookmarks:** Opens the list of saved pages.
    *   **Add Current Page:** Saves the currently loaded webpage directly to your bookmarks.
    *   **Manage Bookmarks:** A long-press on any existing bookmark reveals options to **Edit** the URL, **Delete** the entry, or reorder the list by moving items **Up** or **Down**.
*   **Find in Page:** Activates a dedicated search interface at the top of the screen. This allows you to search for specific text within the currently loaded webpage. The interface includes controls to navigate to the previous or next occurrence and a button to close the search mode.

## File Handling

The browser includes standard web file operations integrated directly with system services:
*   **Downloads:** Selecting a download link enqueues the request through the Android system `DownloadManager`. Files are saved to the public Downloads directory with cookie and user-agent headers maintained.
*   **Uploads:** Standard file-input fields trigger the system file chooser, allowing you to select and upload local documents or media.

## Configuration

The Settings menu allows for detailed customization of the browser's network and privacy behavior. Changes require an application restart to take effect.

### Home Page
You may define a custom URL to serve as the landing page. This URL also handles search queries generated from the main input bar. By hosting the browser logic on a private server, you can adapt the HTML, CSS, and JavaScript of the landing page to control the browser's UI and search funnel.

### DNS over HTTPS (DoH)
The browser supports DNS over HTTPS. This feature encrypts DNS queries, preventing third parties from intercepting or manipulating domain resolution requests. Additionally, this mechanism facilitates content filtering; by utilizing a DoH provider that resolves specific domains to a null address, such as the default provided, the browser effectively blocks advertisements and unwanted trackers.

### Clear Data on Exit
When enabled, the application will automatically purge all session data upon termination. This includes cookies, browsing history, cache, form data, and local storage.

### Proxy Configuration
The browser supports traffic tunneling via SOCKS4 or SOCKS5 proxies.
*   **SOCKS Host:** The IP address or hostname of the proxy server.
*   **SOCKS Port:** The port number of the proxy server.

If these fields are left empty, the proxy connection is disabled.
