package com.example.hangman

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.hangman.databinding.ActivityTematicaBinding

class TematicaActivity : AppCompatActivity() {

    //  Binding para acceder a los elementos del layout
    private lateinit var binding: ActivityTematicaBinding

    //  Guarda la temática seleccionada por el usuario
    private var temaSeleccionado: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Inicializa la pantalla de selección de temática
        super.onCreate(savedInstanceState)
        binding = ActivityTematicaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //  Configura los botones principales
        // Cada botón llama a showConfirmDialog con la temática correspondiente
        binding.btnCerrarTematica.setOnClickListener { finish() } // cierra la pantalla
        binding.btnAnimales.setOnClickListener { showConfirmDialog("animales") }
        binding.btnDeportes.setOnClickListener { showConfirmDialog("deportes") }
        binding.btnComidas.setOnClickListener { showConfirmDialog("comidas") }
        binding.btnCiencia.setOnClickListener { showConfirmDialog("naturaleza") }
        binding.btnTecnologia.setOnClickListener { showConfirmDialog("tecnologia") }
        binding.btnEmociones.setOnClickListener { showConfirmDialog("emociones") }
    }

    //  Función que muestra un diálogo de confirmación para la temática elegida
    private fun showConfirmDialog(tematica: String) {
        temaSeleccionado = tematica // guarda temporalmente la temática

        //  Inflar layout personalizado del diálogo
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm, null)
        val txtConfirm = dialogView.findViewById<android.widget.TextView>(R.id.txtConfirmacion)

        //  Cambia el texto del diálogo según la temática
        txtConfirm.text = "¿Querés jugar ahora en la temática «${tematica.replaceFirstChar { it.uppercase() }}\"?"

        //  Crear el diálogo con estilo personalizado
        val dialog = AlertDialog.Builder(this, R.style.CustomAlertDialogStyle)
            .setView(dialogView)
            .setCancelable(true) // se puede cerrar tocando fuera
            .create()

        //  Configura botones del diálogo
        dialogView.findViewById<android.widget.Button>(R.id.btnCancelar).setOnClickListener {
            dialog.dismiss() // cierra el diálogo si se cancela
        }

        dialogView.findViewById<android.widget.Button>(R.id.btnContinuar).setOnClickListener {
            dialog.dismiss() // cierra diálogo
            abrirModo(tematica) // abre la pantalla de juego con la temática
        }

        //  Ajustes visuales del diálogo
        dialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent) // fondo transparente
            setLayout((resources.displayMetrics.widthPixels * 0.85).toInt(), WindowManager.LayoutParams.WRAP_CONTENT) // tamaño
            setGravity(android.view.Gravity.CENTER) // centrado en pantalla
        }

        dialog.show() // muestra el diálogo
    }

    //  Función que redirige a la actividad de juego con la temática seleccionada
    private fun abrirModo(tematica: String) {
        val intent = Intent(this, ModoTematicaActivity::class.java)
        intent.putExtra("tematica", tematica) // pasa la temática a la siguiente actividad
        startActivity(intent) // inicia la nueva actividad
    }
}
