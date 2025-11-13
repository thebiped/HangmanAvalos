package com.example.hangman

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.hangman.ui.fragment.HomeFragment
import com.example.hangman.ui.fragment.RankingFragment
import com.example.hangman.ui.fragment.ProfileFragment
import com.example.hangman.databinding.ActivityNavbarbottomBinding

class NavbarBottomActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNavbarbottomBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavbarbottomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configura la pantalla inicial y el fragment por defecto
        replaceFragment(HomeFragment())

        // Configura la navegación inferior y cambios de fragment
        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> replaceFragment(HomeFragment())
                R.id.nav_profile -> replaceFragment(ProfileFragment())
                R.id.nav_ranking -> replaceFragment(RankingFragment())
            }
            true
        }
    }

    // Reemplaza el fragment mostrado
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
