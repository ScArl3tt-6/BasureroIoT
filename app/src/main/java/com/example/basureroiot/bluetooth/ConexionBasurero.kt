package com.example.basureroiot.bluetooth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.basureroiot.data.DbHelper
import com.example.basureroiot.data.Sesion
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// "object" = existe UNA sola copia para toda la app. Así todas las pantallas ven
// la misma conexión y los mismos datos. Cuando un valor cambia, la pantalla se actualiza sola.
// Ahora, además, lo que llega del basurero se guarda en la base de datos SQLite.
object ConexionBasurero {

    private const val CLAVE = "TACHO26"   // la misma clave que está en el Arduino

    var estado by mutableStateOf<EstadoConexion>(EstadoConexion.Desconectado)
        private set
    var tapa by mutableStateOf("SIN DATO")
        private set
    var usos by mutableStateOf(0)
        private set
    var nivel by mutableStateOf(-1)          // -1 = todavía no hay dato
        private set
    var alerta by mutableStateOf(false)
        private set
    var ultimoMensaje by mutableStateOf("")
        private set
    var ultimaActualizacion by mutableStateOf("")   // hora del último dato recibido
        private set

    // Lista de lo que ha llegado en esta sesión (hora + mensaje).
    val registros = mutableStateListOf<String>()

    private var dispositivo: DispositivoBasurero? = null
    private var generacion = 0   // sirve para ignorar avisos de una conexión vieja
    private var bd: DbHelper? = null

    // El login le entrega la base de datos para poder guardar los datos que lleguen.
    fun usarBd(db: DbHelper) {
        bd = db
    }

    // Abre la conexión. simulado = true usa el basurero de mentira (para el emulador).
    fun conectar(direccion: String, simulado: Boolean) {
        generacion++
        dispositivo?.desconectar()
        val miGeneracion = generacion
        val alEstado: (EstadoConexion) -> Unit = { if (miGeneracion == generacion) cambiarEstado(it) }
        val alLinea: (String) -> Unit = { if (miGeneracion == generacion) procesar(it) }
        val nuevo: DispositivoBasurero =
            if (simulado) BasureroSimulado(alEstado, alLinea) else BasureroBt(alEstado, alLinea)
        dispositivo = nuevo
        nuevo.conectar(direccion)
    }

    fun desconectar() {
        generacion++
        dispositivo?.desconectar()
        dispositivo = null
        estado = EstadoConexion.Desconectado
        // Se borran los datos en pantalla para que el próximo usuario no vea los del anterior.
        tapa = "SIN DATO"
        usos = 0
        nivel = -1
        alerta = false
        ultimoMensaje = ""
        ultimaActualizacion = ""
        registros.clear()
    }

    // accion: "A" = abrir tapa, "R" = reiniciar contador, "S" = pedir estado
    fun enviar(accion: String) {
        dispositivo?.enviar(CLAVE + ":" + accion)
    }

    // Marca un vaciado: guarda el registro y reinicia el contador del Arduino.
    fun marcarVaciado() {
        enviar("R")
        guardar("VACIADO")
        guardarEnBd("VACIADO", 0)
    }

    fun textoEstado(): String {
        return when (val e = estado) {
            is EstadoConexion.Desconectado -> "Desconectado"
            is EstadoConexion.Conectando -> "Conectando..."
            is EstadoConexion.Conectado -> "Conectado"
            is EstadoConexion.Error -> "Error: " + e.mensaje
        }
    }

    private fun cambiarEstado(nuevo: EstadoConexion) {
        estado = nuevo
        if (nuevo is EstadoConexion.Conectado) enviar("S")   // al conectar, pide usos y nivel
    }

    // Interpreta cada mensaje que llega del Arduino.
    private fun procesar(linea: String) {
        ultimoMensaje = linea
        ultimaActualizacion = horaActual()
        when {
            linea.startsWith("ESTADO:") -> tapa = linea.removePrefix("ESTADO:")
            linea.startsWith("USOS:") -> {
                val nuevo = linea.removePrefix("USOS:").toIntOrNull()
                if (nuevo != null) {
                    // Si el contador subió exactamente en 1, hubo un uso nuevo: se guarda.
                    if (nuevo == usos + 1) guardarEnBd("USO", nuevo)
                    usos = nuevo
                }
            }
            linea.startsWith("NIVEL:") -> {
                val nuevoNivel = linea.removePrefix("NIVEL:").toIntOrNull()
                if (nuevoNivel != null) {
                    // Solo se guarda si el nivel cambió (así no se repite el mismo valor)
                    if (nuevoNivel != nivel) guardarEnBd("NIVEL", nuevoNivel)
                    nivel = nuevoNivel
                    alerta = nuevoNivel >= 80
                }
            }
            linea.startsWith("ALERTA:LLENO") -> {
                alerta = true
                guardarEnBd("ALERTA", 1)
            }
        }
        guardar(linea)
    }

    private fun horaActual(): String {
        return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
    }

    private fun guardar(linea: String) {
        registros.add(horaActual() + "  " + linea)
        if (registros.size > 100) registros.removeAt(0)
    }

    // Guarda un dato en SQLite para el usuario que tiene la sesión iniciada.
    private fun guardarEnBd(tipo: String, valor: Int) {
        val usuario = Sesion.usuario ?: return
        bd?.guardarRegistro(usuario.id, tipo, valor)
    }
}
