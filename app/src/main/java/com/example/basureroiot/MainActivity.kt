package com.example.basureroiot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.basureroiot.navegacion.NavegacionApp
import com.example.basureroiot.ui.theme.BasureroIoTTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Tema de la app: colores y tipografías
            BasureroIoTTheme {
                // Surface = el "fondo" de la app
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Aquí arranca todo: la navegación muestra primero el login
                    NavegacionApp()
                }
            }
        }
    }
}
