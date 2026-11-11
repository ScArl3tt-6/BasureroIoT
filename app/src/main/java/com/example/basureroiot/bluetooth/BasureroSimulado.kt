package com.example.basureroiot.bluetooth

import android.os.Handler
import android.os.Looper

// Basurero de mentira para probar la app en el emulador o sin el Arduino.
// Responde a los mismos comandos que el Arduino real:
// - Cada apertura de la tapa cuenta como un uso.
// - El nivel de llenado sube un 5 % por cada uso (parte en 25 %).
// - Con 80 % o más manda la alerta de lleno.
// - "Reiniciar" (R) deja el contador en 0 y el nivel de nuevo en 25 % (basurero vaciado).
class BasureroSimulado(
    private val alCambiarEstado: (EstadoConexion) -> Unit,
    private val alRecibirLinea: (String) -> Unit
) : DispositivoBasurero {

    private val principal = Handler(Looper.getMainLooper())
    private var conectado = false
    private var usos = 0

    // Nivel simulado: 25 % más 5 % por cada uso, con máximo de 100 %.
    private fun nivel(): Int {
        return minOf(100, 25 + usos * 5)
    }

    override fun conectar(direccion: String) {
        alCambiarEstado(EstadoConexion.Conectando)
        principal.postDelayed({
            conectado = true
            alCambiarEstado(EstadoConexion.Conectado)
            alRecibirLinea("ESTADO:LISTO")
        }, 800)
    }

    override fun enviar(texto: String) {
        if (!conectado) return
        principal.postDelayed({
            if (!texto.startsWith("TACHO26:")) {
                alRecibirLinea("ERROR:NO_AUTORIZADO")
            } else {
                when (texto.removePrefix("TACHO26:")) {
                    "A" -> abrirTapa()
                    "R" -> {
                        usos = 0
                        alRecibirLinea("USOS:0")
                        alRecibirLinea("NIVEL:" + nivel())
                    }
                    "S" -> {
                        alRecibirLinea("USOS:" + usos)
                        alRecibirLinea("NIVEL:" + nivel())
                    }
                    else -> alRecibirLinea("ERROR:COMANDO_INVALIDO")
                }
            }
        }, 300)
    }

    // Abre la tapa (cuenta como un uso) y la cierra a los 2 segundos.
    private fun abrirTapa() {
        usos++
        alRecibirLinea("ESTADO:ABIERTO")
        alRecibirLinea("USOS:" + usos)
        principal.postDelayed({
            alRecibirLinea("ESTADO:CERRADO")
            alRecibirLinea("NIVEL:" + nivel())
            if (nivel() >= 80) alRecibirLinea("ALERTA:LLENO")
        }, 2000)
    }

    override fun desconectar() {
        conectado = false
        alCambiarEstado(EstadoConexion.Desconectado)
    }
}