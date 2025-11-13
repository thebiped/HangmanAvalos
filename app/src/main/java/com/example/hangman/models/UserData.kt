package com.example.hangman.models

import java.io.Serializable

// Clase de datos que representa la información de un usuario
// Serializable permite enviar objetos entre actividades mediante Intents
data class UserData(
    val uid: String = "",               // ID único del usuario en Firebase
    val nombreUsuario: String = "",     // Nombre del usuario
    val email: String = "",             // Correo electrónico del usuario
    val imagenPerfil: String = "",      // URL de la imagen de perfil
    val partidasGanadas: Long = 0,      // Cantidad de partidas ganadas
    val partidasPerdidas: Long = 0,    // Cantidad de partidas perdidas
    val horasJugadas: Double = 0.0,    // Total de horas jugadas
    val puntos: Long = 0,               // Puntos acumulados por el usuario
    val nivel: Long = 1                 // Nivel actual del usuario
) : Serializable
