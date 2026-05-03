package com.moodstudy.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.moodstudy.data.model.MoodEntry
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {

    fun exportMoodToPdf(context: Context, moods: List<MoodEntry>): Uri? {
        if (moods.isEmpty()) return null

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
        val fileName = "moodstudy_$timeStamp.pdf"

        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: return null
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, fileName)

        val pageWidth = 595  // A4 72dpi kira-kira
        val pageHeight = 842

        val pdf = android.graphics.pdf.PdfDocument()
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(
            pageWidth,
            pageHeight,
            1
        ).create()
        val page = pdf.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            textSize = 18f
            color = android.graphics.Color.BLACK
        }

        val textPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 11f
            color = android.graphics.Color.DKGRAY
        }

        var y = 40f

        canvas.drawText("Laporan Mood - MoodStudy", 40f, y, titlePaint)
        y += 20f
        canvas.drawText("Dicetak: $timeStamp", 40f, y, textPaint)
        y += 30f

        canvas.drawText("Tanggal", 40f, y, textPaint)
        canvas.drawText("Mood", 220f, y, textPaint)
        canvas.drawText("Catatan", 320f, y, textPaint)
        y += 16f

        val maxWidthNote = pageWidth - 60

        moods.forEach { m ->
            if (y > pageHeight - 40) {
                pdf.finishPage(page)
                // buat page baru kalau penuh
                val info2 = android.graphics.pdf.PdfDocument.PageInfo.Builder(
                    pageWidth,
                    pageHeight,
                    pdf.pages.size + 1
                ).create()
                val page2 = pdf.startPage(info2)
                canvas.setMatrix(page2.canvas.matrix)
                y = 40f
            }

            canvas.drawText(m.tanggal, 40f, y, textPaint)
            canvas.drawText("${m.moodEmoji} ${m.moodLabel}", 220f, y, textPaint)

            val note = (m.catatan ?: "").ifBlank { "-" }
            // pecah catatan panjang jadi beberapa baris sederhana
            var current = note
            var lineY = y
            while (current.isNotEmpty()) {
                val count = textPaint.breakText(
                    current,
                    true,
                    maxWidthNote.toFloat(),
                    null
                )
                val part = current.substring(0, count)
                canvas.drawText(part, 320f, lineY, textPaint)
                current = current.substring(count)
                lineY += 14f
            }
            y = lineY + 4f
        }

        pdf.finishPage(page)

        FileOutputStream(file).use { out ->
            pdf.writeTo(out)
        }
        pdf.close()

        return FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            file
        )
    }

    fun sharePdf(context: Context, uri: Uri) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(send, "Bagikan laporan mood (PDF)")
        )
    }
}