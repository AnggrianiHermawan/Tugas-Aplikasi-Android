package com.moodstudy.ui.splash

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.moodstudy.databinding.ActivitySplashBinding
import com.moodstudy.ui.auth.LoginActivity
import com.moodstudy.ui.dashboard.DashboardActivity
import com.moodstudy.util.SessionManager
import com.moodstudy.util.navigateTo

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val session = SessionManager(this)

        Handler(Looper.getMainLooper()).postDelayed({
            if (session.isLoggedIn()) {
                navigateTo(DashboardActivity::class.java, clearStack = true)
            } else {
                navigateTo(LoginActivity::class.java, clearStack = true)
            }
        }, 900)
    }
}