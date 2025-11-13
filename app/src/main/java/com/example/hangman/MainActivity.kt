package com.example.hangman

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.hangman.ui.LoginActivity
import com.example.hangman.ui.RegisterActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen


class MainActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        // Inicio actividad principal
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Declarar botones principales
        val startButton = findViewById<Button>(R.id.startButton)
        val loginButton = findViewById<Button>(R.id.loginButton)

        // Interacciones generales
        startButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }


    }
}
