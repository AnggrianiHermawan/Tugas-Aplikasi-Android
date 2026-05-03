package com.moodstudy.ui.mood

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.moodstudy.R
import com.moodstudy.data.db.DatabaseHelper
import com.moodstudy.data.repository.MoodRepository
import com.moodstudy.databinding.ActivityInputMoodBinding
import com.moodstudy.ui.reko.RekomendasiActivity
import com.moodstudy.util.SessionManager
import com.moodstudy.util.showToast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InputMoodActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInputMoodBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var session: SessionManager
    private lateinit var repo: MoodRepository

    private var selectedMood: String? = null

    // ✅ Launcher untuk terima hasil dari FaceScanActivity
    private val faceScanLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val detectedMood = result.data?.getStringExtra("detected_mood")
            if (!detectedMood.isNullOrEmpty()) {
                setSelectedMood(detectedMood)
                val label = when (detectedMood) {
                    "semangat" -> "Semangat 🔥"
                    "capek"    -> "Capek 😴"
                    "stres"    -> "Stres 😣"
                    "bosan"    -> "Bosan 😑"
                    else       -> "Biasa 😐"
                }
                showToast("Mood terdeteksi: $label")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputMoodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        session  = SessionManager(this)
        repo     = MoodRepository(dbHelper)

        if (!session.isLoggedIn()) {
            showToast("Silakan login dulu")
            finish()
            return
        }

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val inFmt    = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dayFmt   = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val userId   = session.getUserId() ?: 0
        val moods    = repo.getMoods(userId)

        val sudahInputHariIni = moods.any { entry ->
            runCatching {
                dayFmt.format(inFmt.parse(entry.tanggal)!!) == todayStr
            }.getOrDefault(false)
        }

        if (sudahInputHariIni) {
            tampilkanDialogSudahInput()
        }

        setupToolbar()
        setupMoodCards()
        setupCatatanWatcher()

        binding.btnSimpan.setOnClickListener {
            simpanMood()
        }
    }

    private fun tampilkanDialogSudahInput() {
        AlertDialog.Builder(this)
            .setTitle("Mood Sudah Dicatat 😊")
            .setMessage("Kamu sudah mengisi mood hari ini.\nMau mengganti dengan mood baru?")
            .setPositiveButton("Ya, Ganti") { dialog, _ -> dialog.dismiss() }
            .setNegativeButton("Tidak") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener { finish() }

        // ✅ Tombol scan wajah → buka FaceScanActivity
        binding.btnScanWajah.setOnClickListener {
            val intent = Intent(this, FaceScanActivity::class.java)
            faceScanLauncher.launch(intent)
        }
    }

    private fun setupMoodCards() {
        binding.cardSemangat.setOnClickListener { setSelectedMood("semangat") }
        binding.cardBiasa.setOnClickListener    { setSelectedMood("biasa") }
        binding.cardCapek.setOnClickListener    { setSelectedMood("capek") }
        binding.cardStres.setOnClickListener    { setSelectedMood("stres") }
        binding.cardBosan.setOnClickListener    { setSelectedMood("bosan") }
    }

    // ✅ Dibuat internal fun agar bisa dipanggil dari faceScanLauncher
    private fun setSelectedMood(mood: String) {
        selectedMood = mood

        val defaultColor  = ContextCompat.getColor(this, R.color.surface_light)
        val selectedColor = ContextCompat.getColor(this, R.color.purple_light)

        binding.cardSemangat.setCardBackgroundColor(defaultColor)
        binding.cardBiasa.setCardBackgroundColor(defaultColor)
        binding.cardCapek.setCardBackgroundColor(defaultColor)
        binding.cardStres.setCardBackgroundColor(defaultColor)
        binding.cardBosan.setCardBackgroundColor(defaultColor)

        when (mood) {
            "semangat" -> binding.cardSemangat.setCardBackgroundColor(selectedColor)
            "biasa"    -> binding.cardBiasa.setCardBackgroundColor(selectedColor)
            "capek"    -> binding.cardCapek.setCardBackgroundColor(selectedColor)
            "stres"    -> binding.cardStres.setCardBackgroundColor(selectedColor)
            "bosan"    -> binding.cardBosan.setCardBackgroundColor(selectedColor)
        }
    }

    private fun setupCatatanWatcher() {
        binding.etCatatan.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tvCharCount.text = "${s?.length ?: 0}/100"
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun simpanMood() {
        val userId = session.getUserId()
        if (userId == null || userId <= 0) {
            showToast("User tidak valid, silakan login ulang")
            return
        }

        val mood = selectedMood
        if (mood.isNullOrEmpty()) {
            showToast("Pilih mood dulu")
            return
        }

        val catatan = binding.etCatatan.text.toString().trim()
        val tanggal = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val result = dbHelper.insertMood(
            userId  = userId,
            tanggal = tanggal,
            mood    = mood,
            catatan = catatan
        )

        if (result > 0L) {
            showToast("Mood tersimpan. Terima kasih sudah mencatat 😊")
            val intent = Intent(this, RekomendasiActivity::class.java).apply {
                putExtra("mood", mood)
            }
            startActivity(intent)
            finish()
        } else {
            showToast("Gagal menyimpan mood, coba lagi.")
        }
    }
}