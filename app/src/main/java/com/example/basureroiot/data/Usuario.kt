package com.example.basureroiot.data

// data class: un usuario de la app (su id en la base de datos y su nombre).
// La contraseña NUNCA se guarda aquí ni en texto plano en ninguna parte.
data class Usuario(val id: Long, val nombre: String)
