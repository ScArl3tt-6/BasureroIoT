package com.example.basureroiot.data

// object: una sola copia para toda la app. Guarda quién inició sesión.
// Si usuario es null, nadie ha iniciado sesión.
object Sesion {
    var usuario: Usuario? = null
        private set

    fun iniciar(nuevo: Usuario) {
        usuario = nuevo
    }

    fun cerrar() {
        usuario = null
    }
}
