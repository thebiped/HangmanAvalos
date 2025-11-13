package com.example.hangman.ui.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.hangman.R
import com.example.hangman.data.FirebaseService
import com.example.hangman.ui.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Fragmento que muestra el perfil del usuario con estadísticas, historial y edición de foto
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var selectedImageUri: Uri? = null

    // Referencias a vistas del layout
    private lateinit var txtNombre: TextView
    private lateinit var txtDescripcion: TextView
    private lateinit var txtNivel: TextView
    private lateinit var txtPuntosTotales: TextView
    private lateinit var txtGanadas: TextView
    private lateinit var txtPerdidas: TextView
    private lateinit var txtHoras: TextView
    private lateinit var imgPerfil: ImageView
    private lateinit var contenedorHistorial: LinearLayout
    private lateinit var barraNivel: View
    private lateinit var txtDificultad: TextView
    private lateinit var barraNivelContainer: RelativeLayout

    // Registro de launcher para seleccionar imagen desde galería
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // 🔹 Obtiene la imagen seleccionada
            selectedImageUri = result.data?.data
            selectedImageUri?.let { uri ->
                imgPerfil.setImageURI(uri)
                // 🔹 Subida de la imagen a Firebase
                FirebaseService.uploadProfileImage(auth.currentUser?.uid ?: return@let, uri,
                    onSuccess = { url ->
                        Glide.with(this).load(url).into(imgPerfil)
                        Toast.makeText(requireContext(), "Foto actualizada", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { e ->
                        Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    })
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🔹 Referencias a los elementos del layout
        txtNombre = view.findViewById(R.id.txtNombre)
        txtDescripcion = view.findViewById(R.id.txtDescripcion)
        txtNivel = view.findViewById(R.id.txtNivel)
        txtPuntosTotales = view.findViewById(R.id.txtPuntos)
        txtGanadas = view.findViewById(R.id.txtPartidasGanadas)
        txtPerdidas = view.findViewById(R.id.txtPartidasPerdidas)
        txtHoras = view.findViewById(R.id.txtHorasJugadas)
        imgPerfil = view.findViewById(R.id.imgPerfil)
        contenedorHistorial = view.findViewById(R.id.contenedorHistorial)
        barraNivel = view.findViewById(R.id.barraNivel)
        txtDificultad = view.findViewById(R.id.txtDificultad)
        barraNivelContainer = view.findViewById(R.id.barraNivelContainer)

        // 🔹 Botón para cerrar sesión
        view.findViewById<AppCompatButton>(R.id.btnCerrarSesion).setOnClickListener { cerrarSesion() }

        // 🔹 Carga los datos del usuario desde Firebase
        cargarDatosUsuario()
    }

    // Función que obtiene y muestra los datos del usuario
    private fun cargarDatosUsuario() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("usuarios").document(uid).addSnapshotListener { doc, e ->
            if (e != null) {
                Log.e("ProfileFragment", "Error al obtener usuario: ${e.message}")
                return@addSnapshotListener
            }
            if (doc != null && doc.exists()) {
                // 🔹 Extrae los datos principales del usuario
                val nombre = doc.getString("nombreUsuario") ?: "Jugador"
                val puntos = doc.getLong("puntos") ?: 0L
                val ganadas = doc.getLong("partidasGanadas") ?: 0L
                val perdidas = doc.getLong("partidasPerdidas") ?: 0L
                val horas = doc.getDouble("horasJugadas") ?: 0.0
                val foto = doc.getString("imagenPerfil")

                // 🔹 Actualiza las vistas con los datos del usuario
                txtNombre.text = nombre
                txtDescripcion.text = "Descripción vacía"
                txtPuntosTotales.text = puntos.toString()
                txtGanadas.text = ganadas.toString()
                txtPerdidas.text = perdidas.toString()
                txtHoras.text = String.format("%.1f h", horas)

                val nivel = doc.getLong("nivel")?.toInt() ?: calcularNivel(puntos)
                txtNivel.text = "Nivel $nivel"

                // 🔹 Actualiza barra de nivel y dificultad
                barraNivelContainer.post {
                    val porcentaje = when (nivel) {
                        1 -> 0.3f
                        2 -> 0.5f
                        3 -> 0.7f
                        4 -> 0.9f
                        else -> 1.0f
                    }
                    barraNivel.scaleX = porcentaje
                    txtDificultad.text = when (nivel) {
                        1 -> "Principiante"
                        2 -> "Normal"
                        3 -> "Avanzado"
                        4 -> "Experto"
                        else -> "Maestro"
                    }
                }

                // 🔹 Si tiene foto, cargarla, si no generar avatar con inicial
                if (!foto.isNullOrEmpty()) {
                    Glide.with(this).load(foto).into(imgPerfil)
                } else {
                    imgPerfil.setImageBitmap(generateInitialsAvatar(nombre))
                }

                // 🔹 Carga historial de partidas
                FirebaseService.getHistorialPartidas(
                    uid,
                    onComplete = { partidas ->
                        contenedorHistorial.removeAllViews()
                        for (partida in partidas) {
                            val fila = LinearLayout(requireContext()).apply {
                                orientation = LinearLayout.HORIZONTAL
                                setPadding(16, 8, 16, 8)
                            }

                            // 🔹 Estado de la partida (ganada/perdida)
                            val estado = TextView(requireContext()).apply {
                                text = partida.resultado.replaceFirstChar { it.uppercase() }
                                setTextColor(
                                    if (partida.resultado == "ganada") Color.parseColor("#10B981")
                                    else Color.parseColor("#EF4444")
                                )
                                setTypeface(null, Typeface.BOLD)
                                setPadding(0, 0, 10, 0)
                            }

                            // 🔹 Palabra jugada
                            val palabra = TextView(requireContext()).apply {
                                text = "Palabra: ${partida.palabra}"
                                setTextColor(Color.WHITE)
                                setPadding(0, 0, 10, 0)
                            }

                            // 🔹 Puntos obtenidos
                            val puntosTxt = TextView(requireContext()).apply {
                                text = "+${partida.puntos} Pts"
                                setTextColor(Color.WHITE)
                                setTypeface(null, Typeface.BOLD)
                            }

                            fila.addView(estado)
                            fila.addView(palabra)
                            fila.addView(puntosTxt)
                            contenedorHistorial.addView(fila) // 🔹 Agrega fila al historial
                        }
                    },
                    onError = { e ->
                        Toast.makeText(requireContext(), "Error al cargar historial: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e("ProfileFragment", "Error al cargar historial", e)
                    }
                )
            }
        }
    }

    // 🔹 Genera un avatar circular con la inicial del nombre
    private fun generateInitialsAvatar(name: String): Bitmap {
        val inicial = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
        val size = 250
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val bgPaint = Paint().apply { color = Color.parseColor("#8B5CF6"); style = Paint.Style.FILL }
        val textPaint = Paint().apply { color = Color.WHITE; textSize = 100f; isAntiAlias = true; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, bgPaint)
        val yPos = (canvas.height / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2)
        canvas.drawText(inicial, (canvas.width / 2f), yPos, textPaint)
        return bmp
    }

    // 🔹 Cierra sesión y vuelve al login
    private fun cerrarSesion() {
        auth.signOut()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    // 🔹 Calcula nivel aproximado según puntos si no está definido
    private fun calcularNivel(puntos: Long): Int {
        return when {
            puntos < 100 -> 1
            puntos < 250 -> 2
            puntos < 500 -> 3
            puntos < 1000 -> 4
            puntos < 2000 -> 5
            else -> 6
        }
    }
}
