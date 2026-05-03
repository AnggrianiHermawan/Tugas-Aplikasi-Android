package com.moodstudy.ui.reko

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.moodstudy.ai.MoodEngine
import com.moodstudy.databinding.ActivityRekomendasiBinding
import com.moodstudy.ui.dashboard.DashboardActivity
import com.moodstudy.util.navigateTo

class RekomendasiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRekomendasiBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRekomendasiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mood = intent.getStringExtra("mood") ?: "biasa"

        binding.tvMoodEmoji.text = MoodEngine.getMoodEmoji(mood)
        binding.tvHeroTitle.text = MoodEngine.getHeroTitle(mood)
        binding.tvHeroSubtitle.text = MoodEngine.getHeroSubtitle(mood)

        binding.rvRekomendasi.layoutManager = LinearLayoutManager(this)
        binding.rvRekomendasi.adapter = RekomendasiAdapter(MoodEngine.getRekomendasi(mood))

        binding.btnBack.setOnClickListener { finish() }
        binding.btnBackDashboard.setOnClickListener {
            navigateTo(DashboardActivity::class.java, clearStack = true)
        }
    }
}