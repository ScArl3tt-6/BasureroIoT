package com.example.basureroiot.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.basureroiot.bluetooth.ConexionBasurero
import com.example.basureroiot.data.DbHelper
import com.example.basureroiot.data.Sesion

// Pantalla de login: revisa usuario y contraseña contra la base de datos SQLite.
// Si son correctos, inicia la sesión y avisa a la navegación con onIngresar.
@Composable
fun PantallaLogin(
    onIngresar: () -> Unit,
    onCrearCuenta: () -> Unit
) {
    val contexto = LocalContext.current
    val db = remember { DbHelper(contexto) }

    var usuario by rememberSaveable { mutableStateOf("") }
    // La contraseña usa remember a propósito: no queremos que quede guardada en el estado de la app.
    var clave by remember { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Basurero IoT", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Monitoreo en vivo", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = usuario,
            onValueChange = { usuario = it },
            label = { Text("Usuario") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = clave,
            onValueChange = { clave = it },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (error.isNotEmpty()) {
            Text(text = error, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(onClick = {
            val nombre = usuario.trim()
            if (nombre.isBlank() || clave.isBlank()) {
                error = "Completa usuario y contraseña"
            } else {
                val encontrado = db.validarLogin(nombre, clave)
                if (encontrado == null) {
                    // Mensaje igual para usuario o contraseña incorrectos (no se da pistas)
                    error = "Usuario o contraseña incorrectos"
                } else {
                    error = ""
                    clave = ""
                    Sesion.iniciar(encontrado)
                    ConexionBasurero.usarBd(db)
                    onIngresar()
                }
            }
        }) {
            Text("Ingresar")
        }

        TextButton(onClick = onCrearCuenta) {
            Text("¿No tienes cuenta? Crear cuenta")
        }
    }
}
