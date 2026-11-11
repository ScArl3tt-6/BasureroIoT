package com.example.basureroiot.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.basureroiot.data.DbHelper

// Pantalla de registro: valida los datos y crea el usuario en SQLite
// (la contraseña se guarda con hash PBKDF2 y sal, nunca en texto plano).
@Composable
fun PantallaRegistro(onVolver: () -> Unit) {
    val contexto = LocalContext.current
    val db = remember { DbHelper(contexto) }

    var usuario by rememberSaveable { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var repetir by remember { mutableStateOf("") }
    var mensaje by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Crear cuenta", style = MaterialTheme.typography.headlineMedium)

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
            label = { Text("Contraseña (8 o más caracteres)") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = repetir,
            onValueChange = { repetir = it },
            label = { Text("Repetir contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (mensaje.isNotEmpty()) {
            Text(text = mensaje, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(onClick = {
            val nombre = usuario.trim()
            // when sin argumento: se revisa cada condición en orden
            when {
                nombre.isBlank() -> {
                    mensaje = "Escribe un nombre de usuario"
                }
                clave.length < 8 -> {
                    mensaje = "La contraseña debe tener al menos 8 caracteres"
                }
                clave != repetir -> {
                    mensaje = "Las contraseñas no coinciden"
                }
                !db.registrarUsuario(nombre, clave) -> {
                    mensaje = "Ese usuario ya existe"
                }
                else -> {
                    Toast.makeText(contexto, "Cuenta creada. Ya puedes iniciar sesión", Toast.LENGTH_LONG).show()
                    onVolver()
                }
            }
        }) {
            Text("Registrar")
        }

        TextButton(onClick = onVolver) {
            Text("Volver al login")
        }
    }
}
