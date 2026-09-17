package org.example.rent.core.data.networking

/**
 * Base API URL, resolved per platform. Routes passed to the HttpClient helpers are relative to this
 * and are joined by [constructRoute], so the trailing slash is required.
 *
 * NOTE: a device/emulator cannot reach `localhost` — that resolves to the device itself:
 *  - Android emulator:  http://10.0.2.2:5001/api/
 *  - iOS simulator:     http://localhost:5001/api/
 *  - Physical device:   http://<your-machine-ip>:5001/api/
 */
expect val platformBaseUrl: String

object UrlConstants {
    val BASE_URL: String get() = platformBaseUrl

    const val REFRESH_ENDPOINT = "worker-auth/refresh"
}
