package com.moodstudy.ui.riwayat

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.moodstudy.data.model.MoodEntry
import com.moodstudy.databinding.ItemRiwayatBinding
import java.text.SimpleDateFormat
import java.util.Locale

class RiwayatAdapter(
    private val items: List<MoodEntry>
) : RecyclerView.Adapter<RiwayatAdapter.VH>() {

    private val inFmt   = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val angkaFmt = SimpleDateFormat("dd", Locale.getDefault())
    private val bulanFmt = SimpleDateFormat("MMM", Locale("id", "ID"))
    private val jamFmt   = SimpleDateFormat("HH:mm", Locale.getDefault())

    inner class VH(val binding: ItemRiwayatBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemRiwayatBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item   = items[position]
        val parsed = runCatching { inFmt.parse(item.tanggal) }.getOrNull()

        with(holder.binding) {
            // Tanggal angka + bulan
            tvTanggalAngka.text = if (parsed != null) angkaFmt.format(parsed) else "--"
            tvTanggalBulan.text = if (parsed != null) bulanFmt.format(parsed) else "--"

            // Emoji + warna background
            tvMoodIcon.text = item.moodEmoji
            tvMoodIcon.setBackgroundColor(moodColor(item.mood))

            // Label mood
            tvMoodLabel.text = item.moodLabel

            // Jam · catatan (atau hanya jam kalau catatan kosong)
            val jam = if (parsed != null) jamFmt.format(parsed) else ""
            tvJamCatatan.text = if (item.catatan.isNotEmpty()) {
                "$jam · ${item.catatan}"
            } else {
                jam
            }
        }
    }

    private fun moodColor(mood: String): Int = when (mood) {
        "semangat" -> Color.parseColor("#FFF3E0")
        "biasa"    -> Color.parseColor("#F3F4F6")
        "capek"    -> Color.parseColor("#EDE9FE")
        "stres"    -> Color.parseColor("#FEE2E2")
        "bosan"    -> Color.parseColor("#E0F2FE")
        else       -> Color.parseColor("#F9FAFB")
    }
}