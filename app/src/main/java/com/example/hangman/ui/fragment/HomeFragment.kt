package com.example.hangman.ui.fragment

import android.graphics.Color
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.example.hangman.R
import com.example.hangman.databinding.FragmentHomeBinding
import com.example.hangman.ui.LoginActivity
import com.example.hangman.ModoClasicoActivity
import com.example.hangman.ModoContraRelojActivity
import com.example.hangman.TematicaActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.app.AlertDialog
import android.content.Intent
import android.util.Log

// Fragmento que representa la pantalla principal del usuario (Home)
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!! // 🔹 Referencia segura al binding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        // 🔹 Configura los botones de modos de juego
        binding.btnJugarClasico.setOnClickListener {
            startActivity(Intent(requireContext(), ModoClasicoActivity::class.java))
        }
        binding.btnModoTematico.setOnClickListener {
            startActivity(Intent(requireContext(), TematicaActivity::class.java))
        }
        binding.btnModoReloj.setOnClickListener {
            startActivity(Intent(requireContext(), ModoContraRelojActivity::class.java))
        }

        // 🔹 Botón para cerrar sesión
        binding.btnCerrarSesion.setOnClickListener { mostrarDialogoCerrarSesion() }

        // 🔹 Obtiene estadísticas del usuario en tiempo real
        val uid = auth.currentUser?.uid
        if (uid != null) obtenerEstadisticasUsuario(uid) else mostrarDatosVacios()

        return view
    }

    // Función que obtiene las estadísticas del usuario desde Firebase
    private fun obtenerEstadisticasUsuario(uid: String) {
        db.collection("usuarios").document(uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("HomeFragment", "Error snapshot: ${e.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    // 🔹 Extrae datos y actualiza las vistas
                    val ganadas = snapshot.getLong("partidasGanadas") ?: 0
                    val perdidas = snapshot.getLong("partidasPerdidas") ?: 0
                    val horas = snapshot.getDouble("horasJugadas") ?: 0.0
                    val puntos = snapshot.getLong("puntos") ?: 0

                    binding.txtPartidasGanadas.text = ganadas.toString()
                    binding.txtPartidasPerdidas.text = perdidas.toString()
                    binding.txtHorasJugadas.text = "${"%.2f".format(horas)}hs"
                    binding.txtPuntos.text = "$puntos"
                } else {
                    // 🔹 Si no hay datos, muestra valores vacíos
                    mostrarDatosVacios()
                }
            }
    }

    // Muestra valores predeterminados cuando no hay datos de usuario
    private fun mostrarDatosVacios() {
        binding.txtPartidasGanadas.text = "0"
        binding.txtPartidasPerdidas.text = "0"
        binding.txtHorasJugadas.text = "0hs"
        binding.txtPuntos.text = "Puntos: 0"
    }

    // Muestra un diálogo personalizado para confirmar cierre de sesión
    private fun mostrarDialogoCerrarSesion() {
        val view = layoutInflater.inflate(R.layout.dialog_logout, null)
        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogStyle)
            .setView(view)
            .setCancelable(false)
            .create()

        // 🔹 Aplica desenfoque o transparencia al fondo según versión Android
        val rootView = requireActivity().window.decorView.findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            rootView.setRenderEffect(RenderEffect.createBlurEffect(8f, 8f, Shader.TileMode.CLAMP))
        } else {
            rootView.alpha = 0.7f
        }

        // 🔹 Al cerrar el diálogo, restablece el fondo
        dialog.setOnDismissListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) rootView.setRenderEffect(null)
            else rootView.alpha = 1f
        }

        // 🔹 Configuración de la ventana del diálogo
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout((resources.displayMetrics.widthPixels * 0.85).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.CENTER)
            setDimAmount(0.6f)
        }

        dialog.show()

        // 🔹 Configura botones del diálogo
        view.findViewById<Button>(R.id.btnCerrarSesion).setOnClickListener {
            dialog.dismiss()
            cerrarSesion()
        }
        view.findViewById<ImageButton>(R.id.btnCerrarModal).setOnClickListener { dialog.dismiss() }
        view.findViewById<Button>(R.id.btnCancelar).setOnClickListener { dialog.dismiss() }
    }

    // Función que cierra sesión y redirige al login
    private fun cerrarSesion() {
        val sharedPref = requireActivity().getSharedPreferences("user_prefs", 0)
        sharedPref.edit().clear().apply()
        startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 🔹 Limpia binding para evitar memory leaks
    }
}
