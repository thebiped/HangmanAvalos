package com.example.hangman.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.hangman.R
import com.example.hangman.WelcomeActivity
import com.example.hangman.models.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Se encarga de permitir que el usuario ingrese con email y contraseña, mostrando animaciones y modales
class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 🔹 Transición de pantalla suave al abrir
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // 🔹 Referencias a los campos de texto y botones
        val emailInput = findViewById<EditText>(R.id.emailEditText)
        val passwordInput = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registroText = findViewById<TextView>(R.id.registroText)

        val rootView = findViewById<ViewGroup>(android.R.id.content)
        var modalView: View? = null // 🔹 Modal de carga

        // 🔹 Botón para ir a registro
        registroText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }

        // 🔹 Botón para iniciar sesión
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val pass = passwordInput.text.toString().trim()

            // 🔹 Validación: si algún campo está vacío
            if (email.isEmpty() || pass.isEmpty()) {
                showErrorModal(rootView, "Por favor completa todos los campos")
                return@setOnClickListener
            }

            // 🔹 Mostrar modal de carga si no existe
            if (modalView == null) {
                modalView = layoutInflater.inflate(R.layout.dialog_login_success, rootView, false)
                rootView.addView(modalView)
            }

            val animation = modalView!!.findViewById<LottieAnimationView>(R.id.loadingLottie)
            val messageText = modalView!!.findViewById<TextView>(R.id.loadingMessage)

            modalView!!.visibility = View.VISIBLE
            modalView!!.alpha = 0f
            modalView!!.animate()?.alpha(1f)?.setDuration(400)?.start()
            messageText.text = "Verificando datos..."
            animation.playAnimation() // 🔹 Reproduce animación de carga

            // 🔹 Inicia sesión con Firebase
            auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener { authResult ->
                    val uid = authResult.user?.uid ?: return@addOnSuccessListener

                    // 🔹 Obtiene los datos del usuario en Firestore
                    db.collection("usuarios").document(uid).get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val user = document.toObject(UserData::class.java) ?: UserData()

                                messageText.text = "Inicio de sesión exitoso"
                                animation.setAnimation(R.raw.progressbar_login) // 🔹 Animación de éxito

                                // 🔹 Después de animación, redirige a pantalla principal
                                modalView?.postDelayed({
                                    val intent = Intent(this, WelcomeActivity::class.java)
                                    intent.putExtra("usuario", user)
                                    startActivity(intent)
                                    finish()
                                }, 2000)
                            } else {
                                modalView?.visibility = View.GONE
                                showErrorModal(rootView, "El usuario no existe")
                            }
                        }
                        .addOnFailureListener {
                            modalView?.visibility = View.GONE
                            showErrorModal(rootView, "Error al obtener datos del usuario")
                        }
                }
                .addOnFailureListener { e ->
                    modalView?.visibility = View.GONE
                    // 🔹 Analiza el error y muestra mensaje adecuado
                    val mensaje = when {
                        e.message?.contains("password") == true -> "Contraseña incorrecta"
                        e.message?.contains("no user record") == true -> "El usuario no existe"
                        else -> "Error al iniciar sesión"
                    }
                    showErrorModal(rootView, mensaje)
                }
        }
    }

    //  Función que muestra un modal de error breve y animado
    private fun showErrorModal(rootView: ViewGroup, mensaje: String) {
        val errorModal = layoutInflater.inflate(R.layout.dialog_error_message, rootView, false)
        val textView = errorModal.findViewById<TextView>(R.id.errorText)
        textView.text = mensaje

        rootView.addView(errorModal)
        errorModal.alpha = 0f
        errorModal.animate().alpha(1f).setDuration(300).start() // 🔹 Animación de aparición

        // 🔹 Después de 2s desaparece con animación
        errorModal.postDelayed({
            errorModal.animate().alpha(0f).setDuration(500).withEndAction {
                rootView.removeView(errorModal)
            }.start()
        }, 2000)
    }
}
