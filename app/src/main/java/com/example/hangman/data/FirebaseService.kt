package com.example.hangman.data

import android.net.Uri
import android.util.Log
import com.example.hangman.models.Partida
import com.example.hangman.models.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

// Objeto singleton que maneja todas las operaciones de Firebase
object FirebaseService {

    private val auth = FirebaseAuth.getInstance()       // Instancia de autenticación
    private val db = FirebaseFirestore.getInstance()    // Instancia de Firestore
    private val storage = FirebaseStorage.getInstance() // Instancia de almacenamiento

    // Subir foto de perfil
    fun uploadProfileImage(
        uid: String,
        uri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val ref = storage.reference.child("profile_images/$uid.jpg") // Ruta de la imagen
        ref.putFile(uri)                                             // Subir archivo
            .continueWithTask { ref.downloadUrl }                    // Obtener URL de descarga
            .addOnSuccessListener { onSuccess(it.toString()) }       // Llamar callback de éxito
            .addOnFailureListener { onFailure(it) }                 // Llamar callback de error
    }

    // Guardar partida y actualizar estadísticas de usuario de forma atómica
    fun guardarPartidaAtomic(resultado: String, palabra: String, puntosGanados: Int, duracionSegundos: Int) {
        val uid = auth.currentUser?.uid ?: return                   // Si no hay usuario, salir
        val userRef = db.collection("usuarios").document(uid)       // Referencia al usuario
        val partidaRef = userRef.collection("partidas").document() // Nueva partida

        db.runTransaction { transaction ->                          // Transacción atómica
            val snapshot = transaction.get(userRef)                // Obtener datos actuales del usuario

            // Leer estadísticas actuales
            val partidasGanadas = snapshot.getLong("partidasGanadas") ?: 0
            val partidasPerdidas = snapshot.getLong("partidasPerdidas") ?: 0
            val puntosActuales = snapshot.getLong("puntos") ?: 0
            val horasJugadas = snapshot.getDouble("horasJugadas") ?: 0.0

            // Calcular nuevas estadísticas según el resultado
            val nuevasHoras = horasJugadas + (duracionSegundos / 3600.0)
            val nuevosPuntos = if (resultado == "ganada") puntosActuales + puntosGanados else puntosActuales
            val nuevasGanadas = if (resultado == "ganada") partidasGanadas + 1 else partidasGanadas
            val nuevasPerdidas = if (resultado == "perdida") partidasPerdidas + 1 else partidasPerdidas
            val nuevoNivel = calcularNivelDesdePuntos(nuevosPuntos)

            // Actualizar estadísticas del usuario
            transaction.update(userRef, mapOf(
                "puntos" to nuevosPuntos,
                "partidasGanadas" to nuevasGanadas,
                "partidasPerdidas" to nuevasPerdidas,
                "horasJugadas" to nuevasHoras,
                "nivel" to nuevoNivel
            ))

            // Guardar la partida en la subcolección
            transaction.set(partidaRef, mapOf(
                "resultado" to resultado,
                "palabra" to palabra,
                "puntos" to puntosGanados,
                "duracionSegundos" to duracionSegundos,
                "fecha" to System.currentTimeMillis()
            ))
        }.addOnSuccessListener {
            Log.d("FirebaseService", "Partida guardada correctamente ($resultado)") // Log de éxito
        }.addOnFailureListener { e ->
            Log.e("FirebaseService", "Error al guardar partida: ${e.message}")    // Log de error
        }
    }

    // Calcular nivel de usuario según puntos acumulados
    private fun calcularNivelDesdePuntos(puntos: Long): Long {
        return when {
            puntos < 50 -> 1
            puntos < 100 -> 2
            puntos < 200 -> 3
            puntos < 350 -> 4
            else -> 5
        }
    }

    // Obtener historial de partidas recientes
    fun getHistorialPartidas(uid: String, onComplete: (List<Partida>) -> Unit, onError: (Exception) -> Unit) {
        db.collection("usuarios").document(uid).collection("partidas")
            .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING) // Ordenar por fecha
            .limit(5)                                                                  // Limitar a 5 partidas
            .get()
            .addOnSuccessListener { snap ->
                val lista = snap.documents.mapNotNull { it.toObject(Partida::class.java) } // Convertir a lista de Partida
                onComplete(lista)                                                         // Retornar lista
            }
            .addOnFailureListener { onError(it) }                                        // Manejar error
    }

    // Obtener ranking global de usuarios
    fun getRanking(onComplete: (List<UserData>) -> Unit, onError: (Exception) -> Unit) {
        db.collection("usuarios")
            .orderBy("puntos", com.google.firebase.firestore.Query.Direction.DESCENDING) // Ordenar por puntos
            .get()
            .addOnSuccessListener { result ->
                val lista = result.documents.mapNotNull { it.toObject(UserData::class.java) } // Convertir a lista de UserData
                onComplete(lista)
            }
            .addOnFailureListener {
                Log.e("FirebaseService", "Error al obtener ranking: ${it.message}") // Log de error
                onError(it)
            }
    }
}
