package com.example.hangman

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.example.hangman.data.FirebaseService
import com.example.hangman.databinding.ActivityModoClasicoBinding
import com.example.hangman.models.Words
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

// Actividad para el modo de juego temático
class ModoTematicaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModoClasicoBinding // Binding del layout
    private var palabraActual = ""                            // Palabra que el jugador debe adivinar
    private val letrasAdivinadas = mutableSetOf<Char>()       // Letras correctas adivinadas
    private var intentosRestantes = 8                         // Intentos restantes
    private var nivelActual = 1                               // Nivel del jugador
    private var puntos = 0                                    // Puntos acumulados
    private var partidasGanadas = 0                           // Partidas ganadas
    private var letrasExtraPorNivel = 0                       // Letras extra por nivel
    private var pistaUsada = false                             // Control de uso de pista
    private var partidaStartMillis: Long = 0L                 // Tiempo de inicio de la partida

    private lateinit var tematica: String                     // Temática seleccionada
    private var palabrasFiltradas = listOf<String>()          // Palabras filtradas según temática

    private val auth = FirebaseAuth.getInstance()             // Instancia de autenticación
    private val db = FirebaseFirestore.getInstance()          // Instancia de Firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModoClasicoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener temática desde intent y filtrar palabras
        tematica = intent.getStringExtra("tematica") ?: "general"
        palabrasFiltradas = Words.getWordsByCategory(tematica).map { it.uppercase() }

        // Si no hay palabras para la temática, mostrar alerta y salir
        if (palabrasFiltradas.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Sin palabras")
                .setMessage("No hay palabras para la temática \"$tematica\".")
                .setPositiveButton("Volver") { _, _ -> finish() }
                .setCancelable(false)
                .show()
            return
        }

        // Botones de ayuda y pausa
        binding.btnAyuda.setOnClickListener { mostrarPista() }
        binding.btnPausa.setOnClickListener { mostrarDialogoPausa() }

        // Obtener puntos, nivel y partidas ganadas del usuario antes de iniciar el juego
        obtenerPuntosUsuario { pts ->
            puntos = pts
            binding.txtPuntos.text = "Puntos: $puntos"
            obtenerNivelUsuario { nivel ->
                nivelActual = nivel
                obtenerPartidasGanadas { startGame() }
            }
        }
    }

    // Iniciar nueva partida
    private fun startGame() {
        // Filtrar palabras posibles según nivel y letras extra
        val posiblesPalabras = palabrasFiltradas.filter { it.length <= nivelActual + letrasExtraPorNivel + 4 }
        palabraActual = posiblesPalabras.random().uppercase()

        // Reiniciar estado de la partida
        letrasAdivinadas.clear()
        intentosRestantes = intentosPorNivel(nivelActual)
        pistaUsada = false
        binding.txtIntentos.text = "Intentos: $intentosRestantes"
        binding.imgAhorcado.setImageResource(R.drawable.ahorcado_1)
        binding.txtPuntos.text = "Puntos: $puntos"

        // Revelar pistas iniciales según nivel y longitud de la palabra
        val indicesPistas = revelarPistasIniciales(palabraActual, nivelActual)
        indicesPistas.forEach { i -> letrasAdivinadas.add(palabraActual[i]) }

        actualizarPalabraMostrada()  // Mostrar palabra con letras adivinadas
        generarTeclado()             // Generar teclado interactivo
        partidaStartMillis = System.currentTimeMillis() // Guardar tiempo de inicio
    }

    // Definir intentos según nivel
    private fun intentosPorNivel(nivel: Int) = when (nivel) {
        1 -> 8
        2 -> 7
        3 -> 6
        4 -> 5
        else -> 4
    }

    // Actualizar el TextView con la palabra mostrando letras adivinadas y guiones
    private fun actualizarPalabraMostrada() {
        val mostrada = palabraActual.map {
            if (letrasAdivinadas.contains(it)) it else '_'
        }.joinToString(" ")
        binding.txtPalabra.text = mostrada
    }

    // Generar teclado dinámico con letras A-Z + Ñ + W en orden correcto
    private fun generarTeclado() {
        val tecladoContainer = binding.keyboardContainer
        tecladoContainer.removeAllViews()

        val letras = ('A'..'Z').toMutableList().apply { add('Ñ') }
        if (!letras.contains('W')) letras.add(letras.indexOf('V') + 1, 'W')

        val letrasPorFila = 9
        val totalFilas = (letras.size + letrasPorFila - 1) / letrasPorFila

        // Crear filas de botones
        for (fila in 0 until totalFilas) {
            val filaLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val inicio = fila * letrasPorFila
            val fin = minOf(inicio + letrasPorFila, letras.size)

            // Crear botones de cada letra en la fila
            letras.subList(inicio, fin).forEach { letra ->
                val boton = MaterialButton(this).apply {
                    text = letra.toString()
                    setTextColor(Color.WHITE)
                    textSize = 20f
                    strokeWidth = 3
                    cornerRadius = 80
                    backgroundTintList = ColorStateList.valueOf(Color.parseColor("#6A0DAD"))
                    rippleColor = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                        setMargins(6, 6, 6, 6)
                    }
                    setPadding(0, 10, 0, 10)

                    // Manejar clic en letra
                    setOnClickListener {
                        manejarLetra(letra, this)
                        isEnabled = false
                        alpha = 0.5f
                    }
                }
                filaLayout.addView(boton)
            }
            tecladoContainer.addView(filaLayout)
        }
    }

    // Manejar la selección de una letra
    private fun manejarLetra(letra: Char, boton: MaterialButton) {
        boton.isEnabled = false
        boton.alpha = 0.8f

        if (palabraActual.contains(letra)) { // Letra correcta
            boton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50"))
            letrasAdivinadas.add(letra)
            actualizarPalabraMostrada()
            verificarVictoria()
        } else { // Letra incorrecta
            boton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F44336"))
            intentosRestantes--
            binding.txtIntentos.text = "Intentos: $intentosRestantes"
            updateHangman()
            verificarDerrota()
        }
    }

    // Actualizar imagen del ahorcado según fallos
    private fun updateHangman() {
        val fallos = 8 - intentosRestantes
        val resId = resources.getIdentifier("ahorcado_$fallos", "drawable", packageName)
        if (resId != 0) binding.imgAhorcado.setImageResource(resId)
    }

    // Verificar si el jugador ganó
    private fun verificarVictoria() {
        if (palabraActual.all { letrasAdivinadas.contains(it) }) {
            desactivarTeclado()
            val puntosGanados = 10 + (nivelActual - 1) * 2
            sumarPuntos(puntosGanados)
            var mensajeExtra = ""
            partidasGanadas++
            if (partidasGanadas % 10 == 0) { // Subir de nivel cada 10 partidas
                nivelActual++
                letrasExtraPorNivel++
                actualizarNivelFirebase()
                mensajeExtra = "\n¡Subiste al nivel $nivelActual!"
            }
            guardarPartida(true, puntosGanados)
            mostrarDialogoResultado("¡Felicidades! Adivinaste la palabra.$mensajeExtra", puntosGanados, true)
        }
    }

    // Verificar si el jugador perdió
    private fun verificarDerrota() {
        if (intentosRestantes <= 0) {
            desactivarTeclado()
            binding.txtPalabra.text = palabraActual.toCharArray().joinToString(" ")
            guardarPartida(false, 0)
            mostrarDialogoResultado("Perdiste. La palabra era: $palabraActual", 0, false)
        }
    }

    // Desactivar todos los botones del teclado
    private fun desactivarTeclado() {
        for (i in 0 until binding.keyboardContainer.childCount) {
            val fila = binding.keyboardContainer.getChildAt(i)
            if (fila is LinearLayout) {
                for (j in 0 until fila.childCount) {
                    (fila.getChildAt(j) as? MaterialButton)?.apply {
                        isEnabled = false
                        alpha = 0.5f
                    }
                }
            }
        }
    }

    // Generar pistas iniciales según nivel y tamaño de palabra
    private fun revelarPistasIniciales(palabra: String, nivel: Int): List<Int> {
        val longitud = palabra.length
        var cantidadPistas = 0

        when (nivel) {
            1 -> cantidadPistas = when {
                longitud <= 5 -> 2
                longitud <= 8 -> 2
                else -> 3
            }
            2 -> cantidadPistas = when {
                longitud <= 5 -> 1
                longitud <= 8 -> 2
                else -> 2
            }
            3 -> cantidadPistas = 1
            4 -> cantidadPistas = if (longitud > 5) 1 else 0
            else -> cantidadPistas = 0
        }

        if (cantidadPistas == 0) return emptyList()

        val indices = mutableSetOf<Int>()
        while (indices.size < cantidadPistas) { // Elegir posiciones aleatorias
            val i = (palabra.indices).random()
            indices.add(i)
        }
        return indices.toList()
    }

    // Mostrar diálogo de pausa
    private fun mostrarDialogoPausa() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_pausa, null)
        val dialog = Dialog(this)
        dialog.setCancelable(false)
        dialog.setContentView(dialogView)
        dialog.window?.apply {
            setLayout((resources.displayMetrics.widthPixels * 0.85).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        dialog.show()
        dialogView.findViewById<Button>(R.id.btnContinuar).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<Button>(R.id.btnReset).setOnClickListener { startGame(); dialog.dismiss() }
        dialogView.findViewById<Button>(R.id.btnLeave).setOnClickListener { finish() }
    }

    // Mostrar diálogo de resultado de la partida
    private fun mostrarDialogoResultado(mensaje: String, puntosGanados: Int, gano: Boolean) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_resultado, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).setCancelable(false).create()
        val txtResultado = dialogView.findViewById<TextView>(R.id.txtResultado)
        val txtPuntos = dialogView.findViewById<TextView>(R.id.txtPuntos)
        val btnSeguir = dialogView.findViewById<AppCompatButton>(R.id.btnSeguir)
        val btnSalir = dialogView.findViewById<AppCompatButton>(R.id.btnSalir)

        txtResultado.text = mensaje
        txtPuntos.visibility = if (gano) View.VISIBLE else View.GONE
        if (gano) txtPuntos.text = "¡Ganaste $puntosGanados puntos!"

        btnSeguir.setOnClickListener { dialog.dismiss(); startGame() }
        btnSalir.setOnClickListener { dialog.dismiss(); finish() }

        dialog.window?.apply {
            setBackgroundDrawable(ContextCompat.getDrawable(this@ModoTematicaActivity, R.drawable.bg_dialog_overlay))
            setLayout((resources.displayMetrics.widthPixels * 0.85).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.CENTER)
        }
        dialog.show()
        desactivarTeclado()
    }

    // Sumar puntos al usuario y actualizar Firestore
    private fun sumarPuntos(cantidad: Int) {
        puntos += cantidad
        binding.txtPuntos.text = "Puntos: $puntos"
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("usuarios").document(uid)
                .update("puntos", puntos)
                .addOnFailureListener { e -> Log.e("ModoTematica", "Error al guardar puntos: ${e.message}") }
        }
    }

    // Actualizar nivel del usuario en Firestore
    private fun actualizarNivelFirebase() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("usuarios").document(uid).update("nivel", nivelActual)
            .addOnFailureListener { e -> Log.e("ModoTematica", "Error al guardar nivel: ${e.message}") }
    }

    // Guardar partida en Firebase
    private fun guardarPartida(gano: Boolean, puntosGanados: Int) {
        val duracion = ((System.currentTimeMillis() - partidaStartMillis) / 1000).toInt()
        val estado = if (gano) "ganada" else "perdida"

        FirebaseService.guardarPartidaAtomic(estado, palabraActual, puntosGanados, duracion)

        val uid = auth.currentUser?.uid ?: return
        val docRef = db.collection("usuarios").document(uid)
        if (gano) {
            docRef.update("partidasGanadas", FieldValue.increment(1))
        } else {
            docRef.update("partidasPerdidas", FieldValue.increment(1))
        }
    }

    // Obtener nivel actual del usuario
    private fun obtenerNivelUsuario(callback: (Int) -> Unit) {
        val uid = auth.currentUser?.uid ?: return callback(1)
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc -> callback((doc.getLong("nivel") ?: 1L).toInt()) }
            .addOnFailureListener { callback(1) }
    }

    // Obtener partidas ganadas del usuario
    private fun obtenerPartidasGanadas(callback: () -> Unit) {
        val uid = auth.currentUser?.uid ?: return callback()
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                partidasGanadas = (doc.getLong("partidasGanadas") ?: 0L).toInt()
                callback()
            }
            .addOnFailureListener { callback() }
    }

    // Obtener puntos del usuario
    private fun obtenerPuntosUsuario(callback: (Int) -> Unit) {
        val uid = auth.currentUser?.uid ?: return callback(0)
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc -> callback((doc.getLong("puntos") ?: 0L).toInt()) }
            .addOnFailureListener { callback(0) }
    }

    // Mostrar pista al jugador
    private fun mostrarPista() {
        if (pistaUsada) { mostrarModalAdvertencia("Ya usaste la ayuda en esta ronda."); return }
        if (puntos < 10) { mostrarModalAdvertencia("Necesitás al menos 10 puntos para usar la ayuda."); return }

        val confirmView = LayoutInflater.from(this).inflate(R.layout.dialog_confirmacion_ayuda, null)
        val dialogConfirm = AlertDialog.Builder(this, R.style.CustomAlertDialogStyle)
            .setView(confirmView).setCancelable(false).create()

        confirmView.findViewById<Button>(R.id.btnCancelar).setOnClickListener { dialogConfirm.dismiss() }
        confirmView.findViewById<Button>(R.id.btnContinuar).setOnClickListener {
            dialogConfirm.dismiss()
            sumarPuntos(-10)
            pistaUsada = true

            val letrasDisponibles = palabraActual.toSet().filter { it !in letrasAdivinadas }
            if (letrasDisponibles.isEmpty()) { mostrarModalAdvertencia("Ya descubriste todas las letras."); return@setOnClickListener }
            val letraAyuda = letrasDisponibles.random()

            val ayudaView = LayoutInflater.from(this).inflate(R.layout.dialog_ayuda, null)
            ayudaView.findViewById<TextView>(R.id.txtAyuda).text = "¡Ayuda! Una letra es: $letraAyuda"
            AlertDialog.Builder(this, R.style.CustomAlertDialogStyle).setView(ayudaView).create().show()
        }
        dialogConfirm.show()
    }

    // Mostrar modal de advertencia
    private fun mostrarModalAdvertencia(mensaje: String) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_advertencia, null)
        val txtMensaje = view.findViewById<TextView>(R.id.txtMensajeAdvertencia)
        val btnCerrar = view.findViewById<Button>(R.id.btnCerrar)
        txtMensaje.text = mensaje
        val dialog = AlertDialog.Builder(this).setView(view).create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        btnCerrar.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
