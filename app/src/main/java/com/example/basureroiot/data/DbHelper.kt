package com.example.basureroiot.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.basureroiot.seguridad.Hash
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Base de datos SQLite de la app, con dos tablas: Usuario y Registro.
// Todas las consultas usan parámetros (los signos ?) y no se arman uniendo textos:
// así no se puede hacer una inyección SQL.
class DbHelper(contexto: Context) : SQLiteOpenHelper(contexto, "basurero.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE Usuario (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nombre_usuario TEXT NOT NULL UNIQUE, " +
                    "hash_clave TEXT NOT NULL, " +
                    "sal TEXT NOT NULL, " +
                    "creado_en TEXT NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE Registro (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "usuario_id INTEGER NOT NULL, " +
                    "tipo TEXT NOT NULL, " +
                    "valor INTEGER NOT NULL, " +
                    "fecha_hora TEXT NOT NULL, " +
                    "FOREIGN KEY (usuario_id) REFERENCES Usuario(id))"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, versionAnterior: Int, versionNueva: Int) {
        db.execSQL("DROP TABLE IF EXISTS Registro")
        db.execSQL("DROP TABLE IF EXISTS Usuario")
        onCreate(db)
    }

    // ---------- Usuarios ----------

    // Crea un usuario nuevo. Devuelve false si ese nombre ya existe.
    fun registrarUsuario(nombre: String, clave: String): Boolean {
        val sal = Hash.generarSal()
        val valores = ContentValues().apply {
            put("nombre_usuario", nombre)
            put("hash_clave", Hash.calcular(clave, sal))
            put("sal", sal)
            put("creado_en", ahora())
        }
        // insert devuelve -1 si falla (por ejemplo, si el usuario ya existe)
        return writableDatabase.insert("Usuario", null, valores) != -1L
    }

    // Revisa usuario y contraseña. Devuelve el usuario si son correctos, o null si no.
    fun validarLogin(nombre: String, clave: String): Usuario? {
        val cursor = readableDatabase.query(
            "Usuario",
            arrayOf("id", "hash_clave", "sal"),
            "nombre_usuario = ?",
            arrayOf(nombre),
            null, null, null
        )
        cursor.use {
            if (it.moveToFirst()) {
                val id = it.getLong(0)
                val hashGuardado = it.getString(1)
                val sal = it.getString(2)
                if (Hash.coincide(clave, sal, hashGuardado)) {
                    return Usuario(id, nombre)
                }
            }
        }
        return null
    }

    // ---------- Registros ----------

    fun guardarRegistro(usuarioId: Long, tipo: String, valor: Int) {
        val valores = ContentValues().apply {
            put("usuario_id", usuarioId)
            put("tipo", tipo)
            put("valor", valor)
            put("fecha_hora", ahora())
        }
        writableDatabase.insert("Registro", null, valores)
    }

    // Los últimos registros del usuario (el más nuevo primero).
    fun ultimosRegistros(usuarioId: Long, cantidad: Int): List<Registro> {
        val lista = mutableListOf<Registro>()
        val cursor = readableDatabase.query(
            "Registro",
            arrayOf("id", "tipo", "valor", "fecha_hora"),
            "usuario_id = ?",
            arrayOf(usuarioId.toString()),
            null, null,
            "id DESC",
            cantidad.toString()
        )
        cursor.use {
            while (it.moveToNext()) {
                lista.add(Registro(it.getLong(0), it.getString(1), it.getInt(2), it.getString(3)))
            }
        }
        return lista
    }

    // Cuántos usos hubo cada día (día y cantidad), el día más nuevo primero.
    fun usosPorDia(usuarioId: Long): List<Pair<String, Int>> {
        val lista = mutableListOf<Pair<String, Int>>()
        val cursor = readableDatabase.rawQuery(
            "SELECT substr(fecha_hora, 1, 10) AS dia, COUNT(*) FROM Registro " +
                    "WHERE usuario_id = ? AND tipo = 'USO' GROUP BY dia ORDER BY dia DESC",
            arrayOf(usuarioId.toString())
        )
        cursor.use {
            while (it.moveToNext()) {
                lista.add(Pair(it.getString(0), it.getInt(1)))
            }
        }
        return lista
    }

    private fun ahora(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    }
}
