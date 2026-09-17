package org.example.rent.core.data.networking

/**
 * Base API host. Pick the one that matches how you run the app (device can't reach `localhost`):
 *  - Physical device:  http://<this-machine-LAN-IP>:5001/api/  (must share the Mac's Wi-Fi)
 *  - Android emulator: http://10.0.2.2:5001/api/
 * Currently set to the dev machine's LAN IP; update it when the network/IP changes.
 */
actual val platformBaseUrl: String = "http://192.168.68.57:5001/api/"
