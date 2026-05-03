package com.moodstudy.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.moodstudy.data.db.DatabaseHelper
import com.moodstudy.databinding.ActivityLoginBinding
import com.moodstudy.util.SessionManager
import com.moodstudy.util.showToast

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        session = SessionManager(this)

        // Kalau sudah login, langsung ke dashboard
        if (session.isLoggedIn()) {
            goToDashboard()
            return
        }

        binding.btnLogin.setOnClickListener { doLogin() }
        binding.tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun doLogin() {
        val email = binding.etEmail.text.toString().trim().lowercase()
        val pass  = binding.etPassword.text.toString()

        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Email tidak valid"
            binding.etEmail.requestFocus()
            return
        }
        if (pass.isBlank()) {
            binding.etPassword.error = "Password tidak boleh kosong"
            binding.etPassword.requestFocus()
            return
        }

        setLoading(true)

        val user = dbHelper.loginUser(email, pass)
        if (user == null) {
            setLoading(false)
            showToast("Email atau password salah")
            return
        }

        // Simpan session dan lanjut
        session.saveLogin(user.id, user.nama, user.email)
        setLoading(false)
        showToast("Selamat datang kembali, ${user.nama}")
        goToDashboard()
    }

    private fun goToDashboard() {
        val intent = Intent(this@LoginActivity, com.moodstudy.ui.dashboard.DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setLoading(loading: Boolean) {
        binding.btnLogin.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }
}