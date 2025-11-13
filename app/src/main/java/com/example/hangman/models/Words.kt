package com.example.hangman.models

object Words {
    // Diccionario principal de palabras
    val DICTIONARY = listOf(
        "casa", "perro", "gato", "sol", "luz", "flor", "mar", "pan", "voz", "paz",
        "cielo", "libro", "verde", "mesa", "silla", "nube", "piedra", "fuego", "lluvia", "viento",
        "correr", "camino", "puerta", "montaña", "bosque", "rio", "playa", "barco", "sombra", "estrella",
        "ventana", "escuela", "maestro", "jardin", "ciudad", "paisaje", "fruta", "cultura", "tiempo", "historia",
        "naturaleza", "universo", "computadora", "telefono", "bicicleta", "cancion", "musica", "felicidad", "amistad", "familia",
        "trabajo", "problema", "solucion", "respuesta", "pregunta", "aventura", "memoria", "recuerdo", "verdad", "mentira",
        "corazon", "emocion", "silencio", "ruido", "peligro", "misterio", "secreto", "fantasma", "dragon", "planeta"
    )

    // Devuelve palabras filtradas según el nivel de dificultad (progresivo)
    fun getWordsByDifficulty(level: Int): List<String> {
        val minLength = 3 + level
        val maxLength = 5 + level * 2
        return DICTIONARY.filter { it.length in minLength..maxLength }
    }

    // Devuelve palabras filtradas según la categoría seleccionada
    fun getWordsByCategory(category: String): List<String> {
        return when(category.lowercase()) {
            "animales" -> listOf("perro", "gato", "elefante", "jirafa", "dragon")
            "deportes" -> listOf("futbol", "tenis", "natacion", "basket", "golf")
            "comidas" -> listOf("pizza", "hamburguesa", "ensalada", "pasta", "tarta")
            "naturaleza" -> listOf("rio", "montaña", "bosque", "estrella", "nube")
            "tecnologia" -> listOf("computadora", "telefono", "bicicleta", "robot", "internet")
            "emociones" -> listOf("felicidad", "amistad", "amor", "miedo", "tristeza")
            else -> DICTIONARY
        }
    }
}
