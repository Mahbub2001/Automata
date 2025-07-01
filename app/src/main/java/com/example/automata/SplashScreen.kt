package com.example.automata

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.automata.auth.UserSession
import com.example.automata.databinding.ActivitySplashScreenBinding

class SplashScreen : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Start animations
        startLogoAnimation()
        startTextAnimation()
        // Initialize UserSession
        UserSession.init(applicationContext)
        // Delay the redirection logic until animations finish
        Handler(Looper.getMainLooper()).postDelayed({
            if (UserSession.isLoggedIn()) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 3000)
    }

    private fun startLogoAnimation() {
        binding.logo.apply {
            alpha = 0f
            scaleX = 0.8f
            scaleY = 0.8f
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(1000)
                .start()
        }
    }

    private fun startTextAnimation() {
        binding.appName.apply {
            alpha = 0f
            translationY = 50f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(500)
                .setDuration(800)
                .start()
        }

        binding.tagline.apply {
            alpha = 0f
            translationY = 30f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(700)
                .setDuration(600)
                .start()
        }

        binding.loadingIndicator.apply {
            alpha = 0f
            animate()
                .alpha(1f)
                .setStartDelay(1000)
                .setDuration(500)
                .start()
        }

        binding.footer.apply {
            alpha = 0f
            animate()
                .alpha(1f)
                .setStartDelay(1500)
                .setDuration(500)
                .start()
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}