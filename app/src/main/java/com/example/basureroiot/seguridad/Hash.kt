package com.example.basureroiot.seguridad

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

// Cifrado de contraseñas con PBKDF2 y sal (salt).
// - La sal es un texto aleatorio distinto para cada usuario.
// - El hash se calcula repitiendo el cálculo miles de veces (así probar contraseñas es lento).
object Hash {
    private const val ITERACIONES = 120000
    private const val LARGO_BITS = 256

    // Crea una sal aleatoria de 16 bytes, guardada como texto.
    fun generarSal(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    // Calcula el hash PBKDF2 de la contraseña con la sal indicada.
    fun calcular(clave: String, sal: String): String {
        val especificacion = PBEKeySpec(
            clave.toCharArray(),
            Base64.decode(sal, Base64.NO_WRAP),
            ITERACIONES,
            LARGO_BITS
        )
        val fabrica = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val resultado = fabrica.generateSecret(especificacion).encoded
        return Base64.encodeToString(resultado, Base64.NO_WRAP)
    }

    // ¿La contraseña escrita corresponde al hash guardado?
    fun coincide(clave: String, sal: String, hashGuardado: String): Boolean {
        val nuevo = calcular(clave, sal)
        return MessageDigest.isEqual(nuevo.toByteArray(), hashGuardado.toByteArray())
    }
}
