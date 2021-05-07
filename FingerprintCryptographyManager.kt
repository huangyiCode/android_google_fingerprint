package com.example.fingerprint

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.google.gson.Gson
import java.nio.charset.Charset
import java.security.InvalidAlgorithmParameterException
import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object FingerprintCryptographyManager {

    private const val AES_MODE_GCM =
        KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_GCM + "/" + KeyProperties.ENCRYPTION_PADDING_NONE

    private const val KEY_ALIAS = "mike"

    private const val SHARED_PREFERENCE_MIKE = "mike_preference"
    private const val SHARED_PREFERENCE_MIKE_KEY = "mike_preference_key"
    private const val SHARED_PREFERENCE_BIND_FINGERPRINT = "mike_preference_bind_fingerprint"


    @Throws(
        NoSuchProviderException::class,
        NoSuchAlgorithmException::class,
        InvalidAlgorithmParameterException::class
    )
    fun createKEY(): SecretKey? {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        keyStore.getKey(KEY_ALIAS, null)?.let { return it as SecretKey }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )
        val build = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or
                    KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(true)
            .setKeySize(256)
            .build()
        keyGenerator.init(build)
        return keyGenerator.generateKey()
    }

    fun getCipher(context: Context): Cipher {
        val cipher = Cipher.getInstance(AES_MODE_GCM)
        if (getBindFingerprintStatus(
                context
            )
        ) {
            cipher.init(
                KeyProperties.PURPOSE_DECRYPT,
                createKEY(),
                GCMParameterSpec(128,
                    getCipherTextFromSharedPreference(
                        context
                    ).iv)
            )
        } else {
            cipher.init(KeyProperties.PURPOSE_ENCRYPT,
                createKEY()
            )
        }
        return cipher
    }

    @Throws(Exception::class)
    fun encyption(
        plaintext: String,
        cipher: Cipher
    ): CipherTextWrapper {
        val encodedBytes = cipher.doFinal(plaintext.toByteArray(Charset.forName("UTF-8")))
        return CipherTextWrapper(
            encodedBytes,
            cipher.iv
        )
    }

    @Throws(java.lang.Exception::class)
    fun decyption(encrypted: ByteArray?, cipher: Cipher): String {
        return String(cipher.doFinal(encrypted), Charset.forName("UTF-8"))
    }

    fun saveCipherTextToSharedPreference(context: Context, cipherText: CipherTextWrapper) {
        with(context.getSharedPreferences(SHARED_PREFERENCE_MIKE, Context.MODE_PRIVATE)) {
            edit().putString(SHARED_PREFERENCE_MIKE_KEY, Gson().toJson(cipherText)).apply()
        }
    }

    fun getCipherTextFromSharedPreference(context: Context): CipherTextWrapper {
        context.getSharedPreferences(SHARED_PREFERENCE_MIKE, Context.MODE_PRIVATE)
            .getString(SHARED_PREFERENCE_MIKE_KEY, null).let {
            return Gson().fromJson(it, CipherTextWrapper::class.java)
        }
    }

    fun setBindFingerPrintStatus(context: Context, status: Boolean) {
        with(context.getSharedPreferences(SHARED_PREFERENCE_MIKE, Context.MODE_PRIVATE)) {
            edit().putBoolean(
                SHARED_PREFERENCE_BIND_FINGERPRINT,
                status
            ).apply()
        }
    }

    fun getBindFingerprintStatus(context: Context) =
        context.getSharedPreferences(SHARED_PREFERENCE_MIKE, Context.MODE_PRIVATE)
            .getBoolean(SHARED_PREFERENCE_BIND_FINGERPRINT, false)


    data class CipherTextWrapper(val cipherText: ByteArray, val iv: ByteArray)

}