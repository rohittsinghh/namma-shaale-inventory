package com.nammashalli.inventory.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec

object EncryptionUtil {

    private const val KEY_ALIAS = "namma_shaale_api_key"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val AES_GCM_NOPADDING = "AES/GCM/NoPadding"
    private const val GCM_TAG_LEN = 128
    private const val GCM_IV_LEN = 12

    fun hashPassword(password: String): String {
        val salt = generateSalt()
        val hash = sha256("$salt:$password")
        return "$salt:$hash"
    }

    fun verifyPassword(password: String, storedHash: String): Boolean {
        return try {
            val parts = storedHash.split(":")
            if (parts.size != 2) return false
            val (salt, hash) = parts
            sha256("$salt:$password") == hash
        } catch (e: Exception) {
            false
        }
    }

    private fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun encryptApiKey(plaintext: String): String {
        return try {
            val key = getOrCreateKey()
            val cipher = Cipher.getInstance(AES_GCM_NOPADDING)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val iv = cipher.iv
            val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
            val combined = iv + ciphertext
            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            simpleEncrypt(plaintext)
        }
    }

    fun decryptApiKey(encrypted: String): String {
        if (encrypted.isBlank()) return ""
        return try {
            val combined = Base64.decode(encrypted, Base64.NO_WRAP)
            if (combined.size <= GCM_IV_LEN) {
                // Too short for AES-GCM — treat as simpleEncrypt (Base64 of plaintext)
                return String(combined, Charsets.UTF_8)
            }
            val iv = combined.copyOfRange(0, GCM_IV_LEN)
            val ciphertext = combined.copyOfRange(GCM_IV_LEN, combined.size)
            val key = getOrCreateKey()
            val cipher = Cipher.getInstance(AES_GCM_NOPADDING)
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LEN, iv))
            String(cipher.doFinal(ciphertext), Charsets.UTF_8)
        } catch (e: Exception) {
            // Base64 decode failed (e.g. key has '_', '-') → it's plaintext stored directly
            // OR AES-GCM decryption failed → try simpleDecrypt as a last resort
            try { simpleDecrypt(encrypted) } catch (e2: Exception) { encrypted }
        }
    }

    private fun getOrCreateKey(): java.security.Key {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            )
            keyGenerator.generateKey()
        }
        return keyStore.getKey(KEY_ALIAS, null)
    }

    fun simpleEncrypt(text: String): String =
        Base64.encodeToString(text.toByteArray(), Base64.NO_WRAP)

    fun simpleDecrypt(encoded: String): String =
        String(Base64.decode(encoded, Base64.NO_WRAP))
}
