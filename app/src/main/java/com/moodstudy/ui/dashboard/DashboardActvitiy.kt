package com.moodstudy.ui.dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.moodstudy.data.db.DatabaseHelper
import com.moodstudy.data.repository.MoodRepository
import com.moodstudy.databinding.ActivityDashboardBinding
import com.moodstudy.notification.ReminderScheduler
import com.moodstudy.ui.grafik.GrafikActivity
import com.moodstudy.ui.mood.InputMoodActivity
import com.moodstudy.ui.profil.ProfilActivity
import com.moodstudy.ui.reko.RekomendasiActivity
import com.moodstudy.ui.riwayat.RiwayatActivity
import com.moodstudy.util.SessionManager
import com.moodstudy.util.navigateTo
import com.moodstudy.util.showToast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var session: SessionManager
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var moodRepo: MoodRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session   = SessionManager(this)
        dbHelper  = DatabaseHelper(this)
        moodRepo  = MoodRepository(dbHelper)

        val name   = session.getUserName().ifEmpty { "User" }
        val userId = session.getUserId() ?: 0

        binding.tvGreeting.text = "Halo, $name 👋"

        requestNotificationPermissionIfNeeded()
        ReminderScheduler.scheduleDaily21(this)

        // Mood hari ini + streak
        if (userId > 0) {
            val moods = moodRepo.getMoods(userId)

            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val inFmt    = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val dayFmt   = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val todayMood = moods.firstOrNull { entry ->
                runCatching {
                    val d = inFmt.parse(entry.tanggal)
                    dayFmt.format(d!!) == todayStr
                }.getOrDefault(false)
            }

            if (todayMood != null) {
                binding.tvMoodQuestionLabel.text = "Mood kamu hari ini:"
                binding.tvMoodStatus.text        = "${todayMood.moodEmoji} ${todayMood.moodLabel}"
                binding.tvMoodEmoji.text         = todayMood.moodEmoji
            } else {
                binding.tvMoodQuestionLabel.text = "Bagaimana perasaan kamu hari ini?"
                binding.tvMoodStatus.text        = "Belum isi mood hari ini"
                binding.tvMoodEmoji.text         = "😊"
            }

            val streak = hitungStreak(moods.map { it.tanggal })
            binding.tvDashboardStreak.text = "$streak hari"
        }

        // Menu utama
        binding.cardMoodQuestion.setOnClickListener {
            navigateTo(InputMoodActivity::class.java)
        }
        binding.cardInputMood.setOnClickListener {
            navigateTo(InputMoodActivity::class.java)
        }
        binding.cardRekomendasi.setOnClickListener {
            openRekomendasiDenganMoodTerakhir()
        }
        binding.cardGrafik.setOnClickListener {
            navigateTo(GrafikActivity::class.java)
        }
        binding.cardRiwayat.setOnClickListener {
            navigateTo(RiwayatActivity::class.java)
        }

        // Bottom navigation
        binding.bottomNav.navBeranda.setOnClickListener { }
        binding.bottomNav.navRiwayat.setOnClickListener {
            navigateTo(RiwayatActivity::class.java)
        }
        binding.bottomNav.navGrafik.setOnClickListener {
            navigateTo(GrafikActivity::class.java)
        }
        binding.bottomNav.navProfil.setOnClickListener {
            navigateTo(ProfilActivity::class.java)
        }
    }

    private fun openRekomendasiDenganMoodTerakhir() {
        val userId = session.getUserId() ?: 0
        if (userId <= 0) {
            showToast("Silakan login dulu")
            return
        }

        val moods = moodRepo.getMoods(userId)
        val last  = moods.firstOrNull()

        if (last == null) {
            showToast("Belum ada data mood. Isi mood dulu ya 😊")
            return
        }

        val intent = Intent(this, RekomendasiActivity::class.java).apply {
            putExtra("mood", last.mood)
        }
        startActivity(intent)
    }

    private fun hitungStreak(tanggalList: List<String>): Int {
        if (tanggalList.isEmpty()) return 0

        val inFmt  = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dayFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val dates = tanggalList.mapNotNull {
            runCatching { dayFmt.format(inFmt.parse(it)!!) }.getOrNull()
        }.distinct().sortedDescending()

        if (dates.isEmpty()) return 0

        var streak  = 1
        var current = dates.first()
        val cal     = Calendar.getInstance()

        for (i in 1 until dates.size) {
            cal.time = dayFmt.parse(current)!!
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val prev = dayFmt.format(cal.time)
            if (dates[i] == prev) {
                streak++
                current = dates[i]
            } else break
        }
        return streak
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), 900)
            }
        }
    }
}