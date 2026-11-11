package com.example.basureroiot.data

// data class: un dato guardado en la base de datos
// (tipo: USO, NIVEL, ALERTA o VACIADO; valor: el número recibido; fechaHora: cuándo llegó).
data class Registro(val id: Long, val tipo: String, val valor: Int, val fechaHora: String) {

    // Texto fácil de leer para mostrar al usuario (en vez de "NIVEL: 25").
    fun descripcion(): String {
        return when (tipo) {
            "USO" -> "Uso número " + valor
            "NIVEL" -> "Nivel de llenado: " + valor + " %"
            "ALERTA" -> "Alerta: basurero casi lleno"
            "VACIADO" -> "Vaciado registrado"
            else -> tipo + ": " + valor
        }
    }
}
