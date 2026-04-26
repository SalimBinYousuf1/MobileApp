package com.example.iamhere.data.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.interfaces.Box
import java.nio.charset.StandardCharsets
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.util.Base64
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptoManager @Inject constructor() {
    private val lazySodium = LazySodiumAndroid(SodiumAndroid())

    init { ensureKeystoreKey() }

    fun generateKeyPair(): Pair<ByteArray, ByteArray> {
        val pub = ByteArray(Box.PUBLICKEYBYTES)
        val sec = ByteArray(Box.SECRETKEYBYTES)
        lazySodium.cryptoBoxKeypair(pub, sec)
        return pub to sec
    }

    fun seal(message: String, recipientPubKey: String): ByteArray {
        val output = ByteArray(message.toByteArray().size + Box.SEALBYTES)
        val recipient = Base64.getDecoder().decode(recipientPubKey)
        lazySodium.cryptoBoxSeal(output, message.toByteArray(StandardCharsets.UTF_8), message.toByteArray().size.toLong(), recipient)
        return output
    }

    fun open(encrypted: ByteArray, myPrivateKey: ByteArray, myPublicKey: ByteArray): String? {
        return try {
            val output = ByteArray(encrypted.size - Box.SEALBYTES)
            val ok = lazySodium.cryptoBoxSealOpen(output, encrypted, encrypted.size.toLong(), myPublicKey, myPrivateKey)
            if (ok) String(output, StandardCharsets.UTF_8) else null
        } catch (_: Exception) { null }
    }

    fun sign(data: ByteArray, myPrivateKey: ByteArray): ByteArray {
        val signature = ByteArray(64)
        lazySodium.cryptoSignDetached(signature, data, data.size.toLong(), myPrivateKey)
        return signature
    }

    fun verify(data: ByteArray, signature: ByteArray, senderPubKey: String): Boolean {
        val pub = Base64.getDecoder().decode(senderPubKey)
        return lazySodium.cryptoSignVerifyDetached(signature, data, data.size.toLong(), pub)
    }

    private fun ensureKeystoreKey() {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (!keyStore.containsAlias("mesh_private_alias")) {
            val gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            gen.init(
                KeyGenParameterSpec.Builder(
                    "mesh_private_alias",
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
            )
            gen.generateKey()
        }
    }

    fun loadKeystoreSecret(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        return ks.getKey("mesh_private_alias", null) as SecretKey
    }
}
