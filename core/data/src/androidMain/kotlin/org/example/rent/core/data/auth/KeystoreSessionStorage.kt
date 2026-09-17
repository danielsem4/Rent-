package org.example.rent.core.data.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.rent.core.domain.auth.SessionStorage

/**
 * Android [SessionStorage] backed by [EncryptedSharedPreferences], whose keys are held in the
 * Android Keystore (hardware-backed where available). Tokens survive process death and are encrypted
 * at rest. Reads/writes hop to [Dispatchers.IO] since the prefs do file I/O.
 */
class KeystoreSessionStorage(
    private val context: Context,
) : SessionStorage {

    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    override suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        prefs.getString(KEY_ACCESS, null)
    }

    override suspend fun getRefreshToken(): String? = withContext(Dispatchers.IO) {
        prefs.getString(KEY_REFRESH, null)
    }

    override suspend fun set(accessToken: String?, refreshToken: String?) {
        withContext(Dispatchers.IO) {
            prefs.edit().apply {
                if (accessToken == null) remove(KEY_ACCESS) else putString(KEY_ACCESS, accessToken)
                if (refreshToken == null) remove(KEY_REFRESH) else putString(KEY_REFRESH, refreshToken)
            }.apply()
        }
    }

    override suspend fun clear() {
        withContext(Dispatchers.IO) {
            prefs.edit().clear().apply()
        }
    }

    private companion object {
        const val PREFS_NAME = "rent_secure_session"
        const val KEY_ACCESS = "accessToken"
        const val KEY_REFRESH = "refreshToken"
    }
}
