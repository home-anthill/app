package eu.homeanthill

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

// Application-scoped cache so MasterKey is built only once (avoids repeated Keystore lookups).
// @Volatile ensures the write is immediately visible to all threads; the synchronized block
// prevents two threads from racing through the null check and both building a MasterKey.
@Volatile
private var cachedMasterKey: MasterKey? = null

@Suppress("DEPRECATION")
private fun Context.masterKey(): MasterKey {
    return cachedMasterKey ?: synchronized(MasterKey::class.java) {
        cachedMasterKey ?: MasterKey.Builder(applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
            .also { cachedMasterKey = it }
    }
}

/**
 * Returns the app's AES-256 encrypted SharedPreferences instance.
 * EncryptedSharedPreferences caches the instance internally; [masterKey] is also cached
 * to avoid a Keystore lookup on every call.
 *
 * @Suppress: MasterKey and EncryptedSharedPreferences are deprecated in security-crypto 1.1.0
 * but no stable replacement API exists yet. Suppress until an alternative is available.
 */
@Suppress("DEPRECATION")
fun Context.securePrefs(): SharedPreferences =
    EncryptedSharedPreferences.create(
        applicationContext,
        mainKey,
        masterKey(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )
