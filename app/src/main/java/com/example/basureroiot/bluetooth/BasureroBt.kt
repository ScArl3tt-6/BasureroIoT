package com.example.basureroiot.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.os.Looper
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

// sealed class: la conexión solo puede estar en UNO de estos estados.
sealed class EstadoConexion {
    object Desconectado : EstadoConexion()
    object Conectando : EstadoConexion()
    object Conectado : EstadoConexion()
    class Error(val mensaje: String) : EstadoConexion()
}

// data class: un dispositivo Bluetooth ya emparejado (nombre y dirección).
data class DispositivoEmparejado(val nombre: String, val direccion: String)

// interface: lo que cualquier "basurero" (el real por Bluetooth o el simulado) sabe hacer.
interface DispositivoBasurero {
    fun conectar(direccion: String)
    fun enviar(texto: String)
    fun desconectar()
}

// Lista los dispositivos que ya están emparejados en el celular (Ajustes > Bluetooth).
@SuppressLint("MissingPermission")
@Suppress("DEPRECATION")
fun listarEmparejados(): List<DispositivoEmparejado> {
    return try {
        val adaptador = BluetoothAdapter.getDefaultAdapter() ?: return emptyList()
        adaptador.bondedDevices.map { DispositivoEmparejado(it.name ?: "Sin nombre", it.address) }
    } catch (e: SecurityException) {
        emptyList()
    }
}

// El basurero real: se conecta por Bluetooth clásico (SPP) al módulo HC-05.
class BasureroBt(
    private val alCambiarEstado: (EstadoConexion) -> Unit,
    private val alRecibirLinea: (String) -> Unit
) : DispositivoBasurero {

    // Identificador estándar del perfil serie (SPP) que usa el HC-05.
    private val uuidSpp: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val principal = Handler(Looper.getMainLooper())
    private var socket: BluetoothSocket? = null
    private var salida: OutputStream? = null
    @Volatile private var activo = false

    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    override fun conectar(direccion: String) {
        cerrarSocket()
        avisar(EstadoConexion.Conectando)
        // La conexión puede tardar, por eso se hace en otro hilo (para no congelar la pantalla).
        Thread {
            try {
                val adaptador = BluetoothAdapter.getDefaultAdapter()
                if (adaptador == null) {
                    avisar(EstadoConexion.Error("Este celular no tiene Bluetooth"))
                    return@Thread
                }
                val dispositivo = adaptador.getRemoteDevice(direccion)
                val s = dispositivo.createRfcommSocketToServiceRecord(uuidSpp)
                s.connect()
                socket = s
                salida = s.outputStream
                activo = true
                avisar(EstadoConexion.Conectado)
                leer(s.inputStream)
            } catch (e: Exception) {
                cerrarSocket()
                avisar(EstadoConexion.Error("No se pudo conectar: " + (e.message ?: "error desconocido")))
            }
        }.start()
    }

    // Lee lo que manda el Arduino y lo separa en líneas (cada mensaje termina en salto de línea).
    private fun leer(entrada: InputStream) {
        val buffer = ByteArray(256)
        val linea = StringBuilder()
        try {
            while (activo) {
                val n = entrada.read(buffer)
                if (n < 0) break
                for (i in 0 until n) {
                    val c = buffer[i].toInt().toChar()
                    if (c == '\n') {
                        val texto = linea.toString().trim()
                        linea.clear()
                        if (texto.isNotEmpty()) principal.post { alRecibirLinea(texto) }
                    } else if (c != '\r') {
                        linea.append(c)
                    }
                }
            }
        } catch (e: Exception) {
            // la conexión se cerró: no hace falta hacer nada aquí
        }
        if (activo) {
            cerrarSocket()
            avisar(EstadoConexion.Error("Se perdió la conexión"))
        }
    }

    override fun enviar(texto: String) {
        val out = salida ?: return
        Thread {
            try {
                out.write((texto + "\n").toByteArray())
                out.flush()
            } catch (e: Exception) {
                avisar(EstadoConexion.Error("No se pudo enviar el comando"))
            }
        }.start()
    }

    override fun desconectar() {
        cerrarSocket()
        avisar(EstadoConexion.Desconectado)
    }

    private fun cerrarSocket() {
        activo = false
        try {
            socket?.close()
        } catch (e: Exception) {
            // ya estaba cerrado
        }
        socket = null
        salida = null
    }

    // Los avisos a la pantalla siempre se hacen en el hilo principal.
    private fun avisar(estado: EstadoConexion) {
        principal.post { alCambiarEstado(estado) }
    }
}
