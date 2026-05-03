package com.moodstudy.ui.profil

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.moodstudy.databinding.ActivityProfilBinding
import com.moodstudy.notification.ReminderScheduler
import com.moodstudy.ui.auth.LoginActivity
import com.moodstudy.ui.dashboard.DashboardActivity
import com.moodstudy.ui.grafik.GrafikActivity
import com.moodstudy.ui.riwayat.RiwayatActivity
import com.moodstudy.util.SessionManager
import com.moodstudy.util.navigateTo
import com.moodstudy.util.showToast

class ProfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfilBinding

    private val prefs by lazy {
        getSharedPreferences("moodstudy_settings", MODE_PRIVATE)
    }

    private val notifPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                setReminderEnabled(true)
                showToast("Notifikasi harian diaktifkan 🎉")
            } else {
                // Kembalikan switch ke OFF karena izin ditolak
                binding.switchNotifikasi.post {
                    binding.switchNotifikasi.isChecked = false
                }
                showToast("Izin notifikasi ditolak.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val session = SessionManager(this)
        val nama = session.getUserName().ifEmpty { "User" }
        val email = session.getUserEmail().ifEmpty { "-" }

        binding.tvNama.text = nama
        binding.tvEmail.text = email
        binding.tvInitial.text = nama.trim().firstOrNull()?.uppercase() ?: "U"

        // Set status awal switch dari SharedPreferences
        val enabled = prefs.getBoolean("daily_reminder_enabled", false)
        updateNotifUI(enabled)

        // Listener switch geser ON/OFF
        binding.switchNotifikasi.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= 33 &&
                    ContextCompat.checkSelfPermission(
                        this, Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    setReminderEnabled(true)
                    showToast("Notifikasi harian diaktifkan 🎉")
                }
            } else {
                setReminderEnabled(false)
                showToast("Notifikasi harian dimatikan.")
            }
        }

        // Klik kartu juga toggle switch (supaya area klik lebih luas)
        binding.itemNotifikasi.setOnClickListener {
            binding.switchNotifikasi.isChecked = !binding.switchNotifikasi.isChecked
        }

        binding.itemTentang.setOnClickListener {
            showToast("MoodStudy v1.0 — AI rule-based rekomendasi belajar.")
        }

        binding.itemKeluar.setOnClickListener {
            session.logout()
            navigateTo(LoginActivity::class.java, clearStack = true)
        }

        binding.bottomNav.navBeranda.setOnClickListener {
            navigateTo(DashboardActivity::class.java, clearStack = true)
        }
        binding.bottomNav.navRiwayat.setOnClickListener {
            navigateTo(RiwayatActivity::class.java)
        }
        binding.bottomNav.navGrafik.setOnClickListener {
            navigateTo(GrafikActivity::class.java)
        }
        binding.bottomNav.navProfil.setOnClickListener { }
    }

    private fun setReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("daily_reminder_enabled", enabled).apply()
        updateNotifUI(enabled)
        if (enabled) {
            ReminderScheduler.scheduleDaily21(this)
        } else {
            ReminderScheduler.cancelDaily(this)
        }
    }

    private fun updateNotifUI(enabled: Boolean) {
        binding.switchNotifikasi.post {
            binding.switchNotifikasi.isChecked = enabled
        }
        binding.tvNotifStatus.text = if (enabled) "Aktif, jam 21.00" else "Nonaktif"
    }
}