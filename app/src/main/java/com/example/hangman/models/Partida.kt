package com.example.hangman.models

// Clase de datos que representa una partida de juego
data class Partida(
    val uid: String = "",                // ID único del usuario que jugó la partida
    val resultado: String = "",          // Resultado de la partida: "ganada" o "perdida"
    val palabra: String = "",            // Palabra que se jugó en la partida
    val puntos: Int = 0,                 // Puntos obtenidos en la partida
    val duracionSegundos: Int = 0,      // Duración de la partida en segundos
    val fecha: Long = System.currentTimeMillis() // Timestamp de cuándo se jugó
)
