package com.example.hangman

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class SplashActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Inicializa el splash screen
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Controla el tiempo del splash
        var keepSplashScreen = true
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }

        // Espera un tiempo y termina el splash
        Handler(Looper.getMainLooper()).postDelayed({
            keepSplashScreen = false
        }, 1000)

        // Redirige a la pantalla principal después de que acaba el tiempo
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 1000)
    }
}
