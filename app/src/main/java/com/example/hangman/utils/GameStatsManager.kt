package com.example.hangman.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

//  Objeto que se encarga de manejar las estadísticas de juego del usuario
object GameStatsManager {

    //  Instancias de Firebase
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    //  Función principal para actualizar las estadísticas del jugador
    // Recibe los puntos ganados, si ganó la partida y la duración en segundos
    fun actualizarEstadisticas(
        puntosGanados: Int,
        gano: Boolean,
        duracionSegundos: Long
    ) {
        //  Obtiene el UID del usuario actual, si no hay usuario termina
        val uid = auth.currentUser?.uid ?: return
        val userRef = db.collection("usuarios").document(uid)

        //  Transacción atómica para asegurar consistencia en la base de datos
        db.runTransaction { t ->
            val snapshot = t.get(userRef) // obtiene los datos actuales del usuario

            //  Lee valores actuales, si no existen asigna valores por defecto
            val puntos = snapshot.getLong("puntos") ?: 0L
            val ganadas = snapshot.getLong("partidasGanadas") ?: 0L
            val perdidas = snapshot.getLong("partidasPerdidas") ?: 0L
            val horas = snapshot.getDouble("horasJugadas") ?: 0.0

            //  Calcula nuevos valores
            val nuevosPuntos = puntos + puntosGanados.toLong() // suma los puntos ganados
            val nuevoNivel = calcularNivelDesdePuntos(nuevosPuntos) // determina el nivel según puntos

            //  Actualiza la base de datos con los nuevos valores
            t.update(userRef, mapOf(
                "puntos" to nuevosPuntos,
                "partidasGanadas" to (ganadas + if (gano) 1 else 0), // aumenta si ganó
                "partidasPerdidas" to (perdidas + if (!gano) 1 else 0), // aumenta si perdió
                "horasJugadas" to (horas + (duracionSegundos / 3600.0)), // convierte segundos a horas
                "nivel" to nuevoNivel
            ))
        }
    }

    //  Función que determina el nivel del usuario según los puntos
    fun calcularNivelDesdePuntos(puntos: Long): Long {
        //  Condiciones escalonadas según rango de puntos
        return when {
            puntos < 50 -> 1
            puntos < 100 -> 2
            puntos < 200 -> 3
            puntos < 350 -> 4
            else -> 5
        }
    }
}
