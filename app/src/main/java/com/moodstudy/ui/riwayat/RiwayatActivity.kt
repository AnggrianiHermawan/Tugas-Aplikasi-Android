package com.moodstudy.ui.riwayat

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.moodstudy.data.db.DatabaseHelper
import com.moodstudy.data.repository.MoodRepository
import com.moodstudy.databinding.ActivityRiwayatBinding
import com.moodstudy.ui.dashboard.DashboardActivity
import com.moodstudy.ui.grafik.GrafikActivity
import com.moodstudy.ui.profil.ProfilActivity
import com.moodstudy.util.SessionManager
import com.moodstudy.util.navigateTo

class RiwayatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRiwayatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRiwayatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val session = SessionManager(this)
        val userId  = session.getUserId() ?: 0
        val repo    = MoodRepository(DatabaseHelper(this))
        val moods   = if (userId > 0) repo.getMoods(userId) else emptyList()

        if (moods.isEmpty()) {
            binding.rvRiwayat.visibility = View.GONE
            binding.tvEmpty.visibility   = View.VISIBLE
        } else {
            binding.tvEmpty.visibility   = View.GONE
            binding.rvRiwayat.visibility = View.VISIBLE
            binding.rvRiwayat.layoutManager = LinearLayoutManager(this)
            binding.rvRiwayat.adapter       = RiwayatAdapter(moods)
        }

        binding.bottomNav.navBeranda.setOnClickListener {
            navigateTo(DashboardActivity::class.java, clearStack = true)
        }
        binding.bottomNav.navRiwayat.setOnClickListener { /* sudah di sini */ }
        binding.bottomNav.navGrafik.setOnClickListener {
            navigateTo(GrafikActivity::class.java)
        }
        binding.bottomNav.navProfil.setOnClickListener {
            navigateTo(ProfilActivity::class.java)
        }
    }
}