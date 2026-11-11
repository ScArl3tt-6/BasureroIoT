package com.example.basureroiot.navegacion

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.basureroiot.data.Sesion
import com.example.basureroiot.ui.screens.PantallaConectar
import com.example.basureroiot.ui.screens.PantallaLogin
import com.example.basureroiot.ui.screens.PantallaPanel
import com.example.basureroiot.ui.screens.PantallaRegistro

// "Director de tránsito" de la app: decide qué pantalla se ve y cómo se pasa de una a otra.
@Composable
fun NavegacionApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            PantallaLogin(
                onIngresar = {
                    // El login ya validó usuario y contraseña. Se borra el login de la pila.
                    navController.navigate("conectar") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onCrearCuenta = { navController.navigate("registro") }
            )
        }

        composable("registro") {
            PantallaRegistro(onVolver = { navController.popBackStack() })
        }

        composable("conectar") {
            PantallaConectar(
                onConectado = {
                    navController.navigate("panel") {
                        popUpTo("conectar") { inclusive = true }
                    }
                }
            )
        }

        composable("panel") {
            PantallaPanel(
                onCerrarSesion = {
                    // Al cerrar sesión se borra la sesión y se vuelve al login
                    Sesion.cerrar()
                    navController.navigate("login") {
                        popUpTo("panel") { inclusive = true }
                    }
                }
            )
        }
    }
}
