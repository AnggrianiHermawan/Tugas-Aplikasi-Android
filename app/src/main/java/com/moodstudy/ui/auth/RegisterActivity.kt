package com.moodstudy.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.moodstudy.data.db.DatabaseHelper
import com.moodstudy.data.repository.UserRepository
import com.moodstudy.databinding.ActivityRegisterBinding
import com.moodstudy.util.SessionManager
import com.moodstudy.util.showToast

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var userRepo: UserRepository
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRepo = UserRepository(DatabaseHelper(this))
        session = SessionManager(this)

        binding.btnRegister.setOnClickListener { doRegister() }
        binding.tvLoginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun doRegister() {
        val name  = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim().lowercase()
        val pass  = binding.etPassword.text.toString()
        val pass2 = binding.etConfirmPassword.text.toString()

        // Validasi input
        if (name.isBlank()) {
            binding.etName.error = "Nama tidak boleh kosong"
            binding.etName.requestFocus()
            return
        }
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Email tidak valid"
            binding.etEmail.requestFocus()
            return
        }
        if (pass.length < 6) {
            binding.etPassword.error = "Password minimal 6 karakter"
            binding.etPassword.requestFocus()
            return
        }
        if (pass != pass2) {
            binding.etConfirmPassword.error = "Konfirmasi password tidak cocok"
            binding.etConfirmPassword.requestFocus()
            return
        }

        // Cek apakah email sudah terdaftar di SQLite
        if (userRepo.isEmailExist(email)) {
            binding.etEmail.error = "Email sudah terdaftar"
            binding.etEmail.requestFocus()
            return
        }

        // Loading state untuk proses lokal
        setLoading(true)

        // Simpan ke SQLite
        val userId = userRepo.registerUser(name, email, pass)
        if (userId <= 0L) {
            setLoading(false)
            showToast("Gagal mendaftar, coba lagi.")
            return
        }

        // Simpan session lokal
        session.saveLogin(userId.toInt(), name, email)

        // Matikan loading & anggap SUKSES secara lokal dulu
        setLoading(false)
        showToast("Daftar berhasil! Selamat datang, $name 🎉")

        // Mulai sync ke Firestore di background (tanpa loading)
        syncUserToFirestore(name, email)

        // Lanjut ke dashboard
        goToDashboard()
    }

    private fun syncUserToFirestore(name: String, email: String) {
        val db = Firebase.firestore
        val userDoc = hashMapOf(
            "email"     to email,
            "name"      to name,
            "createdAt" to Timestamp.now()
        )
        db.collection("users")
            .document(email)
            .set(userDoc, SetOptions.merge())
            .addOnSuccessListener {
                showToast("Firestore: OK")
            }
            .addOnFailureListener { e ->
                showToast("Firestore gagal: ${e.message}")
            }
    }

    private fun goToDashboard() {
        val intent = Intent(this@RegisterActivity, com.moodstudy.ui.dashboard.DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setLoading(loading: Boolean) {
        binding.btnRegister.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }
}