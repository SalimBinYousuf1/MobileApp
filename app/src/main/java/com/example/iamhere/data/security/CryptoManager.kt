package com.example.iamhere.data.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.interfaces.Box
import com.goterl.lazysodium.interfaces.Sign
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptoManager @Inject constructor(@ApplicationContext context: Context) {
    private val sodium = LazySodiumAndroid(SodiumAndroid())
    private val prefs = EncryptedSharedPreferences.create(
        context,
        "crypto_keys",
        MasterKey.Builder(context).setKeyGenParameterSpec(
            KeyGenParameterSpec.Builder("mesh_master", KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        ).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    data class DeviceKeys(
        val boxPublic: ByteArray,
        val boxSecret: ByteArray,
        val signPublic: ByteArray,
        val signSecret: ByteArray
    )

    fun getOrCreateKeys(): DeviceKeys {
        val existing = prefs.getString("box_pub", null)
        if (existing != null) {
            return DeviceKeys(
                Base64.decode(existing, Base64.NO_WRAP),
                Base64.decode(prefs.getString("box_sec", "")!!, Base64.NO_WRAP),
                Base64.decode(prefs.getString("sign_pub", "")!!, Base64.NO_WRAP),
                Base64.decode(prefs.getString("sign_sec", "")!!, Base64.NO_WRAP)
            )
        }

        val boxPub = ByteArray(Box.PUBLICKEYBYTES)
        val boxSec = ByteArray(Box.SECRETKEYBYTES)
        sodium.cryptoBoxKeypair(boxPub, boxSec)

        val signPub = ByteArray(Sign.PUBLICKEYBYTES)
        val signSec = ByteArray(Sign.SECRETKEYBYTES)
        sodium.cryptoSignKeypair(signPub, signSec)

        prefs.edit()
            .putString("box_pub", Base64.encodeToString(boxPub, Base64.NO_WRAP))
            .putString("box_sec", Base64.encodeToString(boxSec, Base64.NO_WRAP))
            .putString("sign_pub", Base64.encodeToString(signPub, Base64.NO_WRAP))
            .putString("sign_sec", Base64.encodeToString(signSec, Base64.NO_WRAP))
            .apply()

        return DeviceKeys(boxPub, boxSec, signPub, signSec)
    }

    fun resetKeys() = prefs.edit().clear().apply()

    fun seal(message: String, recipientPubKeyB64: String): ByteArray {
        val recipient = Base64.decode(recipientPubKeyB64, Base64.NO_WRAP)
        val msg = message.toByteArray(StandardCharsets.UTF_8)
        val out = ByteArray(msg.size + Box.SEALBYTES)
        sodium.cryptoBoxSeal(out, msg, msg.size.toLong(), recipient)
        return out
    }

    fun open(encrypted: ByteArray, myPublic: ByteArray, mySecret: ByteArray): String? {
        return runCatching {
            val out = ByteArray(encrypted.size - Box.SEALBYTES)
            val ok = sodium.cryptoBoxSealOpen(out, encrypted, encrypted.size.toLong(), myPublic, mySecret)
            if (ok) String(out, StandardCharsets.UTF_8) else null
        }.getOrNull()
    }

    fun sign(data: ByteArray, signSecret: ByteArray): ByteArray {
        val signature = ByteArray(Sign.BYTES)
        sodium.cryptoSignDetached(signature, data, data.size.toLong(), signSecret)
        return signature
    }

    fun verify(data: ByteArray, signature: ByteArray, signPubKeyB64: String): Boolean {
        val pub = Base64.decode(signPubKeyB64, Base64.NO_WRAP)
        return sodium.cryptoSignVerifyDetached(signature, data, data.size.toLong(), pub)
    }

    fun toB64(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)
}
