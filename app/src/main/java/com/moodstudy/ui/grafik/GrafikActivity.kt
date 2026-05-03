package com.moodstudy.ui.grafik

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.moodstudy.R
import com.moodstudy.data.db.DatabaseHelper
import com.moodstudy.data.model.MoodEntry
import com.moodstudy.data.repository.MoodRepository
import com.moodstudy.databinding.ActivityGrafikBinding
import com.moodstudy.ui.dashboard.DashboardActivity
import com.moodstudy.ui.profil.ProfilActivity
import com.moodstudy.ui.riwayat.RiwayatActivity
import com.moodstudy.util.SessionManager
import com.moodstudy.util.navigateTo
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class GrafikActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGrafikBinding
    private lateinit var repo: MoodRepository

    private val inFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val dayName = SimpleDateFormat("EEE", Locale("id", "ID"))
    private val dateOnly = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGrafikBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repo = MoodRepository(DatabaseHelper(this))

        // Tab Mingguan / Bulanan
        binding.btnMingguan.setOnClickListener { render("week") }
        binding.btnBulanan.setOnClickListener { render("month") }
        render("week") // default

        // Bottom Navigation
        binding.bottomNav.navBeranda.setOnClickListener {
            navigateTo(DashboardActivity::class.java, clearStack = true)
        }
        binding.bottomNav.navRiwayat.setOnClickListener {
            navigateTo(RiwayatActivity::class.java)
        }
        binding.bottomNav.navGrafik.setOnClickListener { /* already here */ }
        binding.bottomNav.navProfil.setOnClickListener {
            navigateTo(ProfilActivity::class.java)
        }
    }

    // ─── Tab state (Mingguan / Bulanan) ───────────────────────────────────────
    private fun setActiveTab(active: String) {
        val purple    = ColorStateList.valueOf(getColor(R.color.purple_primary))
        val none      = ColorStateList.valueOf(Color.TRANSPARENT)
        val muted     = Color.parseColor("#6B7280")
        val white     = Color.WHITE

        if (active == "week") {
            binding.btnMingguan.backgroundTintList = purple
            binding.btnMingguan.setTextColor(white)
            binding.btnBulanan.backgroundTintList  = none
            binding.btnBulanan.setTextColor(muted)
        } else {
            binding.btnBulanan.backgroundTintList  = purple
            binding.btnBulanan.setTextColor(white)
            binding.btnMingguan.backgroundTintList = none
            binding.btnMingguan.setTextColor(muted)
        }
    }

    // ─── Render data sesuai mode ──────────────────────────────────────────────
    private fun render(mode: String) {
        val userId = SessionManager(this).getUserId()
        val all    = if (userId > 0) repo.getMoods(userId) else emptyList()
        val data   = if (mode == "month") all.take(30).reversed()
        else                 all.take(7).reversed()

        binding.tvPeriod.text = if (mode == "month") "30 Hari Terakhir" else "7 Hari Terakhir"

        setActiveTab(mode)
        drawChart(data)
        fillStats(all)
    }

    // ─── Gambar grafik MPAndroidChart ────────────────────────────────────────
    private fun drawChart(items: List<MoodEntry>) {
        val entries = items.mapIndexed { idx, it ->
            Entry(idx.toFloat(), it.moodScore)
        }

        val dataSet = LineDataSet(entries, "Mood").apply {
            color         = Color.parseColor("#7C3AED")
            lineWidth     = 3f
            setDrawCircles(true)
            setCircleColor(Color.parseColor("#5B21B6"))
            circleRadius  = 4f
            setDrawValues(false)
            mode          = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor     = Color.parseColor("#7C3AED")
            fillAlpha     = 30
        }

        binding.lineChart.apply {
            data = LineData(dataSet)
            description.isEnabled = false
            legend.isEnabled      = false
            setTouchEnabled(true)
            setPinchZoom(true)
            animateX(500)

            axisRight.isEnabled = false

            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 6f
                setDrawGridLines(false)
                textColor = Color.parseColor("#6B7280")
            }

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                textColor   = Color.parseColor("#6B7280")
                valueFormatter = IndexAxisValueFormatter(
                    items.map { mood ->
                        val d = runCatching { inFmt.parse(mood.tanggal) }.getOrNull()
                        if (d != null) dayName.format(d) else ""
                    }
                )
            }

            invalidate()
        }
    }

    // ─── Statistik + Insight otomatis ────────────────────────────────────────
    private fun fillStats(all: List<MoodEntry>) {
        val userId              = SessionManager(this).getUserId()
        val (topMood, topCount) = repo.getMostFrequentMood(userId)

        val dummy = MoodEntry(userId = userId, tanggal = "", mood = topMood)
        binding.tvMoodTerbanyak.text = "${dummy.moodEmoji} ${dummy.moodLabel}"
        binding.tvMoodCount.text     = "$topCount kali"
        binding.tvTotalCatatan.text  = all.size.toString()

        val best = all.maxByOrNull { it.moodScore }
        val bestDay = best?.let {
            val d = runCatching { inFmt.parse(it.tanggal) }.getOrNull()
            if (d != null) dayName.format(d) else "-"
        } ?: "-"

        binding.tvHariTerbaik.text = "🔥 $bestDay"
        binding.tvStreak.text      = "🔥 ${calcStreak(all)}"

        // Insight waktu paling produktif
        binding.tvInsight.text     = generateInsight(all)
    }

    // ─── Hitung streak hari berturut-turut ───────────────────────────────────
    private fun calcStreak(all: List<MoodEntry>): Int {
        if (all.isEmpty()) return 0

        val dates = all.mapNotNull {
            val d = runCatching { inFmt.parse(it.tanggal) }.getOrNull() ?: return@mapNotNull null
            dateOnly.format(d)
        }.distinct()

        if (dates.isEmpty()) return 0

        val sorted  = dates.sortedDescending()
        var streak  = 1
        var current = sorted.first()

        fun prevDay(dateStr: String): String {
            val cal = Calendar.getInstance()
            cal.time = dateOnly.parse(dateStr) ?: return ""
            cal.add(Calendar.DAY_OF_YEAR, -1)
            return dateOnly.format(cal.time)
        }

        for (i in 1 until sorted.size) {
            if (sorted[i] == prevDay(current)) {
                streak++
                current = sorted[i]
            } else break
        }
        return streak
    }

    // ─── Generate insight waktu belajar/mood terbaik ─────────────────────────
    private fun generateInsight(all: List<MoodEntry>): String {
        if (all.size < 5) {
            return "Belum cukup data untuk insight. Isi mood beberapa hari lagi ya ✨"
        }

        data class Bucket(var total: Float = 0f, var count: Int = 0)

        val pagi  = Bucket()   // 05–11
        val siang = Bucket()   // 12–17
        val malam = Bucket()   // 18–23
        val dini  = Bucket()   // 00–04

        all.forEach { entry ->
            val d = runCatching { inFmt.parse(entry.tanggal) }.getOrNull() ?: return@forEach
            val cal = Calendar.getInstance().apply { time = d }
            val hour = cal.get(Calendar.HOUR_OF_DAY)

            val bucket = when (hour) {
                in 5..11   -> pagi
                in 12..17  -> siang
                in 18..23  -> malam
                else       -> dini
            }
            bucket.total += entry.moodScore
            bucket.count++
        }

        val avgPagi  = if (pagi.count  > 0) pagi.total  / pagi.count  else 0f
        val avgSiang = if (siang.count > 0) siang.total / siang.count else 0f
        val avgMalam = if (malam.count > 0) malam.total / malam.count else 0f
        val avgDini  = if (dini.count  > 0) dini.total  / dini.count  else 0f

        val list = listOf(
            "pagi"  to avgPagi,
            "siang" to avgSiang,
            "malam" to avgMalam,
            "dini"  to avgDini
        ).filter { it.second > 0f }

        if (list.isEmpty()) {
            return "Belum ada data jam isi mood. Coba isi mood di waktu yang berbeda-beda."
        }

        val best = list.maxByOrNull { it.second } ?: return "Belum ada insight."

        return when (best.first) {
            "pagi" -> "Kamu cenderung punya mood terbaik di pagi hari. Manfaatkan waktu ini untuk ngerjain tugas yang paling penting 💡"
            "siang" -> "Mood kamu cukup stabil di siang hari. Cocok buat kelas, diskusi, atau kerja kelompok."
            "malam" -> "Kamu lebih produktif di malam hari. Jadwalkan fokus belajar atau ngerjain project di jam-jam ini 🌙"
            else -> "Kamu sering aktif larut malam. Jaga jam tidur ya, tapi sepertinya kamu juga cukup produktif di jam-jam ini."
        }
    }
}