package com.example.automata

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.automata.auth.UserSession
import com.example.automata.databinding.ActivitySplashScreenBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreen : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Start animations
        startLogoAnimation()
        startTextAnimation()

        // Initialize session
        UserSession.init(applicationContext)

        // Delay and navigate after 3 seconds
        lifecycleScope.launch {
            delay(3000)
            val targetActivity = if (UserSession.isLoggedIn()) MainActivity::class.java else LoginActivity::class.java
            startActivity(Intent(this@SplashScreen, targetActivity))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
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
        binding.appName.animate().alpha(1f).translationY(0f).setStartDelay(500).setDuration(800).start()
        binding.tagline.animate().alpha(1f).translationY(0f).setStartDelay(700).setDuration(600).start()
        binding.loadingIndicator.animate().alpha(1f).setStartDelay(1000).setDuration(500).start()
        binding.footer.animate().alpha(1f).setStartDelay(1500).setDuration(500).start()
    }
}
