@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package org.example.rent.core.data.auth

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import org.example.rent.core.domain.auth.SessionStorage
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFMutableDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import platform.posix.memcpy

/**
 * iOS [SessionStorage] backed by the Keychain (`kSecClassGenericPassword`). Access and refresh tokens
 * are stored as separate items under a shared service, so they persist across launches and are
 * protected by the system Keychain. Each write replaces the existing item (delete-then-add).
 */
class KeychainSessionStorage(
    private val service: String = "org.example.rent.session",
) : SessionStorage {

    override suspend fun getAccessToken(): String? = read(ACCOUNT_ACCESS)

    override suspend fun getRefreshToken(): String? = read(ACCOUNT_REFRESH)

    override suspend fun set(accessToken: String?, refreshToken: String?) {
        write(ACCOUNT_ACCESS, accessToken)
        write(ACCOUNT_REFRESH, refreshToken)
    }

    override suspend fun clear() {
        delete(ACCOUNT_ACCESS)
        delete(ACCOUNT_REFRESH)
    }

    private fun write(account: String, value: String?) {
        // Always clear the existing item first so a null wipes it and a non-null replaces it.
        delete(account)
        if (value == null) return

        val query = newQuery(account)
        val dataRef = CFBridgingRetain(value.toNSData())
        CFDictionarySetValue(query, kSecValueData, dataRef)
        SecItemAdd(query, null)
        CFRelease(dataRef)
        CFRelease(query)
    }

    private fun read(account: String): String? = memScoped {
        val query = newQuery(account)
        CFDictionarySetValue(query, kSecReturnData, kCFBooleanTrue)
        CFDictionarySetValue(query, kSecMatchLimit, kSecMatchLimitOne)

        val result = alloc<CFTypeRefVar>()
        val status = SecItemCopyMatching(query, result.ptr)
        CFRelease(query)
        if (status != errSecSuccess) return@memScoped null

        val data = CFBridgingRelease(result.value) as? NSData ?: return@memScoped null
        data.toKotlinString()
    }

    private fun delete(account: String) {
        val query = newQuery(account)
        SecItemDelete(query)
        CFRelease(query)
    }

    /** Builds a base query for one account (class + service + account). Caller must [CFRelease] it. */
    private fun newQuery(account: String): CFMutableDictionaryRef {
        val query = CFDictionaryCreateMutable(
            kCFAllocatorDefault,
            0,
            kCFTypeDictionaryKeyCallBacks.ptr,
            kCFTypeDictionaryValueCallBacks.ptr,
        )!!
        CFDictionarySetValue(query, kSecClass, kSecClassGenericPassword)
        query.putBridged(kSecAttrService, service)
        query.putBridged(kSecAttrAccount, account)
        return query
    }

    /**
     * Bridges a Kotlin [String] into the dictionary (Kotlin `String` bridges to `NSString`
     * automatically), releasing our owning ref since the dictionary retains its own.
     */
    private fun CFMutableDictionaryRef.putBridged(key: CFTypeRef?, value: String) {
        val ref = CFBridgingRetain(value)
        CFDictionarySetValue(this, key, ref)
        CFRelease(ref)
    }

    private fun String.toNSData(): NSData {
        val bytes = encodeToByteArray()
        if (bytes.isEmpty()) return NSData()
        return bytes.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
        }
    }

    private fun NSData.toKotlinString(): String {
        val size = length.toInt()
        if (size == 0) return ""
        val bytes = ByteArray(size)
        bytes.usePinned { pinned ->
            memcpy(pinned.addressOf(0), this.bytes, length)
        }
        return bytes.decodeToString()
    }

    private companion object {
        const val ACCOUNT_ACCESS = "accessToken"
        const val ACCOUNT_REFRESH = "refreshToken"
    }
}
