package com.example.basureroiot.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.basureroiot.bluetooth.ConexionBasurero
import com.example.basureroiot.bluetooth.DispositivoEmparejado
import com.example.basureroiot.bluetooth.EstadoConexion
import com.example.basureroiot.bluetooth.listarEmparejados

// ¿Tiene la app el permiso de Bluetooth? (en Android 11 o menos no hace falta pedirlo)
fun tienePermisoBluetooth(contexto: Context): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.BLUETOOTH_CONNECT) ==
            PackageManager.PERMISSION_GRANTED
}

// Pantalla para elegir el HC-05 y conectarse. Cuando la conexión resulta bien, llama a onConectado.
@Composable
fun PantallaConectar(onConectado: () -> Unit) {
    val contexto = LocalContext.current
    var permiso by remember { mutableStateOf(tienePermisoBluetooth(contexto)) }
    val pedirPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido -> permiso = concedido }

    var dispositivos by remember { mutableStateOf(emptyList<DispositivoEmparejado>()) }
    var elegido by remember { mutableStateOf<DispositivoEmparejado?>(null) }
    val estado = ConexionBasurero.estado

    // Si hay permiso, se carga la lista de dispositivos emparejados.
    LaunchedEffect(permiso) {
        if (permiso) dispositivos = listarEmparejados()
    }
    // Cuando la conexión resulta exitosa, se pasa al panel.
    LaunchedEffect(estado) {
        if (estado is EstadoConexion.Conectado) onConectado()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Conectar", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Estado: " + ConexionBasurero.textoEstado())

        if (!permiso) {
            Text("Para conectarte al basurero la app necesita el permiso de Bluetooth.")
            Button(onClick = { pedirPermiso.launch(Manifest.permission.BLUETOOTH_CONNECT) }) {
                Text("Dar permiso")
            }
        } else {
            Text(text = "Dispositivos emparejados", style = MaterialTheme.typography.titleMedium)
            if (dispositivos.isEmpty()) {
                Text("No hay dispositivos emparejados. Empareja el HC-05 en Ajustes > Bluetooth y presiona Actualizar lista.")
            }
            dispositivos.forEach { d ->
                OutlinedButton(
                    onClick = { elegido = d },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = (if (elegido == d) "✔ " else "") + d.nombre + "  (" + d.direccion + ")")
                }
            }
            OutlinedButton(onClick = { dispositivos = listarEmparejados() }) {
                Text("Actualizar lista")
            }
            Button(
                onClick = { elegido?.let { ConexionBasurero.conectar(it.direccion, false) } },
                enabled = elegido != null && estado !is EstadoConexion.Conectando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Conectar")
            }
        }

        HorizontalDivider()
        Text("Para probar sin el Arduino (por ejemplo en el emulador):")
        OutlinedButton(
            onClick = { ConexionBasurero.conectar("", true) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Usar simulador")
        }
    }
}
