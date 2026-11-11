package com.example.basureroiot.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.basureroiot.bluetooth.ConexionBasurero
import com.example.basureroiot.data.DbHelper
import com.example.basureroiot.data.Registro
import com.example.basureroiot.data.Sesion

// Panel en vivo: muestra lo que manda el basurero, permite enviarle órdenes
// y muestra las últimas lecturas, que se leen de la base de datos SQLite.
@Composable
fun PantallaPanel(onCerrarSesion: () -> Unit) {
    val c = ConexionBasurero
    val contexto = LocalContext.current
    val db = remember { DbHelper(contexto) }
    var guardados by remember { mutableStateOf(emptyList<Registro>()) }

    // Cada vez que cambian los datos en vivo, se vuelven a leer los últimos guardados.
    LaunchedEffect(c.usos, c.nivel, c.tapa) {
        val usuario = Sesion.usuario
        guardados = if (usuario != null) db.ultimosRegistros(usuario.id, 5) else emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Panel en vivo", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Usuario: " + (Sesion.usuario?.nombre ?: "-"))
        Text(text = "Conexión: " + c.textoEstado())

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Tapa", style = MaterialTheme.typography.labelLarge)
                Text(text = c.tapa, style = MaterialTheme.typography.headlineSmall)
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Usos", style = MaterialTheme.typography.labelLarge)
                Text(text = c.usos.toString(), style = MaterialTheme.typography.displayMedium)
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (c.nivel < 0) {
                    Text(text = "Nivel de llenado: sin dato", style = MaterialTheme.typography.labelLarge)
                } else {
                    Text(text = "Nivel de llenado: " + c.nivel + " %", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { c.nivel / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        if (c.alerta) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = "⚠ Basurero casi lleno",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        // Cada botón envía una orden con clave al Arduino.
        Button(onClick = { c.enviar("A") }, modifier = Modifier.fillMaxWidth()) {
            Text("Abrir tapa")
        }
        OutlinedButton(onClick = { c.enviar("R") }, modifier = Modifier.fillMaxWidth()) {
            Text("Reiniciar contador")
        }
        // Pide al basurero que informe AHORA sus usos y su nivel (en el basurero real vuelve a medir el nivel).
        OutlinedButton(onClick = { c.enviar("S") }, modifier = Modifier.fillMaxWidth()) {
            Text("Actualizar datos")
        }
        Text(
            text = "Última actualización: " + (if (c.ultimaActualizacion.isEmpty()) "-" else c.ultimaActualizacion),
            style = MaterialTheme.typography.bodySmall
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Últimas lecturas", style = MaterialTheme.typography.labelLarge)
                if (guardados.isEmpty()) {
                    Text(text = "Todavía no hay lecturas", style = MaterialTheme.typography.bodySmall)
                }
                guardados.forEach { r ->
                    Text(
                        text = r.fechaHora.substring(11) + "  " + r.descripcion(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Text(text = "Último mensaje: " + c.ultimoMensaje, style = MaterialTheme.typography.bodySmall)

        TextButton(onClick = {
            c.desconectar()
            onCerrarSesion()
        }) {
            Text("Cerrar sesión")
        }
    }
}
