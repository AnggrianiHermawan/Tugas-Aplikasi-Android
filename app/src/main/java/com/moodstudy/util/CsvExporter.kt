package com.moodstudy.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.moodstudy.data.model.MoodEntry
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {

    fun exportMoodToCsv(context: Context, moods: List<MoodEntry>): Uri? {
        if (moods.isEmpty()) return null

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
        val fileName = "moodstudy_$timeStamp.csv"

        val dir = context.getExternalFilesDir(null) ?: return null
        val file = File(dir, fileName)

        FileWriter(file).use { writer ->
            writer.append("Tanggal,Mood,Skor,Catatan\n")
            moods.forEach { m ->
                val safeNote = (m.catatan ?: "").replace("\"", "\"\"")
                writer.append("\"${m.tanggal}\",")
                    .append("\"${m.moodLabel}\",")
                    .append("\"${m.moodScore}\",")
                    .append("\"$safeNote\"")
                    .append("\n")
            }
        }

        return FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            file
        )
    }

    fun shareCsv(context: Context, uri: Uri) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(sendIntent, "Bagikan data mood sebagai CSV")
        )
    }
}