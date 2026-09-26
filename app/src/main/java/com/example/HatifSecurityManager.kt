package com.example

import android.app.KeyguardManager
import android.content.Context
import android.content.SharedPreferences
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object HatifSecurityManager {
    private const val PREFS_SECURITY = "hatif_security_prefs"
    private const val KEY_STORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS = "HatifDiaryMasterKey_v1"
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128

    // Keys in SharedPreferences
    private const val PREF_CUSTOM_PASSWORD_HASH = "pref_custom_password_hash"
    private const val PREF_SALT = "pref_salt"
    private const val PREF_CUSTOM_PASSWORD_SET = "pref_custom_password_set"

    // Default Security Question & Answer (Case-insensitive & trimmed)
    const val DEFAULT_SECURITY_QUESTION = "What does Hatif Wants?"
    const val DEFAULT_SECURITY_ANSWER = "hope"

    private val _isDiaryUnlocked = MutableStateFlow(false)
    val isDiaryUnlocked: StateFlow<Boolean> = _isDiaryUnlocked.asStateFlow()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_SECURITY, Context.MODE_PRIVATE)
    }

    private fun hashString(input: String, salt: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt)
        val bytes = md.digest(input.trim().lowercase().toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun getOrCreateSalt(context: Context): ByteArray {
        val prefs = getPrefs(context)
        val saltStr = prefs.getString(PREF_SALT, null)
        if (saltStr != null) {
            return Base64.decode(saltStr, Base64.NO_WRAP)
        }
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        val encoded = Base64.encodeToString(salt, Base64.NO_WRAP)
        prefs.edit().putString(PREF_SALT, encoded).apply()
        return salt
    }

    // ==========================================
    // KEYSTORE AES-256 MASTER KEY MANAGEMENT
    // ==========================================

    @Synchronized
    private fun getOrCreateMasterKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEY_STORE_PROVIDER)
        keyStore.load(null)

        if (keyStore.containsAlias(KEY_ALIAS)) {
            val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
            if (entry != null) {
                return entry.secretKey
            }
        }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEY_STORE_PROVIDER)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setRandomizedEncryptionRequired(true)
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    // ==========================================
    // DATA ENCRYPTION & DECRYPTION (AES-256-GCM)
    // ==========================================

    fun encryptData(plaintext: String): String {
        if (plaintext.isEmpty()) return ""
        try {
            val key = getOrCreateMasterKey()
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val iv = cipher.iv
            val cipherText = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

            // Combine IV (12 bytes) + CipherText
            val combined = ByteArray(iv.size + cipherText.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)

            return Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            // Fallback obfuscated encoding if keystore is in transient initialization
            return Base64.encodeToString(plaintext.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        }
    }

    fun decryptData(encryptedBase64: String): String {
        if (encryptedBase64.isEmpty()) return ""
        try {
            val combined = Base64.decode(encryptedBase64, Base64.NO_WRAP)
            if (combined.size <= GCM_IV_LENGTH) {
                return String(combined, Charsets.UTF_8)
            }

            val iv = ByteArray(GCM_IV_LENGTH)
            val cipherText = ByteArray(combined.size - GCM_IV_LENGTH)
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH)
            System.arraycopy(combined, GCM_IV_LENGTH, cipherText, 0, cipherText.size)

            val key = getOrCreateMasterKey()
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)

            val plainBytes = cipher.doFinal(cipherText)
            return String(plainBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            // Try raw Base64 fallback if not encrypted with GCM
            return try {
                String(Base64.decode(encryptedBase64, Base64.NO_WRAP), Charsets.UTF_8)
            } catch (ex: Exception) {
                ""
            }
        }
    }

    // ==========================================
    // UNLOCK & AUTHENTICATION METHODS
    // ==========================================

    fun lockDiary() {
        _isDiaryUnlocked.value = false
    }

    fun unlockDiary() {
        _isDiaryUnlocked.value = true
    }

    fun verifySecurityQuestion(answer: String): Boolean {
        val normalized = answer.trim().lowercase()
        val isCorrect = normalized == DEFAULT_SECURITY_ANSWER
        if (isCorrect) {
            _isDiaryUnlocked.value = true
        }
        return isCorrect
    }

    fun hasCustomPassword(context: Context): Boolean {
        return getPrefs(context).getBoolean(PREF_CUSTOM_PASSWORD_SET, false)
    }

    fun verifyCustomPassword(context: Context, password: String): Boolean {
        val prefs = getPrefs(context)
        val storedHash = prefs.getString(PREF_CUSTOM_PASSWORD_HASH, null) ?: return false
        val salt = getOrCreateSalt(context)
        val inputHash = hashString(password, salt)
        val isMatch = inputHash == storedHash
        if (isMatch) {
            _isDiaryUnlocked.value = true
        }
        return isMatch
    }

    fun setCustomPassword(context: Context, newPassword: String) {
        val salt = getOrCreateSalt(context)
        val hash = hashString(newPassword, salt)
        getPrefs(context).edit()
            .putString(PREF_CUSTOM_PASSWORD_HASH, hash)
            .putBoolean(PREF_CUSTOM_PASSWORD_SET, true)
            .apply()
    }

    /**
     * Attempts native Biometric Prompt on Android 9+ (API 28+).
     * On successful verification, unlocks the diary.
     */
    fun launchBiometricPrompt(
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            if (keyguardManager == null || !keyguardManager.isDeviceSecure) {
                onError("No biometric or screen lock enrolled. Use security question.")
                return
            }

            try {
                val cancellationSignal = CancellationSignal()
                val executor = context.mainExecutor

                val prompt = BiometricPrompt.Builder(context)
                    .setTitle("Hatif Diary Unlock")
                    .setSubtitle("Fingerprint or device credential authentication")
                    .setDescription("Scan your fingerprint to decrypt your private diary.")
                    .setNegativeButton("Use Security Question", executor) { _, _ ->
                        onError("Switched to Security Question.")
                    }
                    .build()

                prompt.authenticate(
                    cancellationSignal,
                    executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                            super.onAuthenticationSucceeded(result)
                            unlockDiary()
                            onSuccess()
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            onError("Biometric verification failed. Try again.")
                        }

                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                            super.onAuthenticationError(errorCode, errString)
                            onError(errString?.toString() ?: "Biometric error.")
                        }
                    }
                )
            } catch (e: Exception) {
                onError("Biometric authentication unavailable: ${e.localizedMessage}")
            }
        } else {
            onError("Biometrics require Android 9+. Use security question.")
        }
    }
}
