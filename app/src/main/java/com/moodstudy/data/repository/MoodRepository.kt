package com.moodstudy.data.repository

import com.moodstudy.data.db.DatabaseHelper
import com.moodstudy.data.model.MoodEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MoodRepository(private val db: DatabaseHelper) {

    private val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun saveMood(userId: Int, mood: String, catatan: String): Boolean {
        val tanggal = fmt.format(Date())
        return db.insertMood(userId, tanggal, mood, catatan) > 0
    }

    fun getMoods(userId: Int): List<MoodEntry> = db.getMoodsByUser(userId)

    fun getLatest(userId: Int): MoodEntry? = db.getLatestMood(userId)

    fun getMoodStats(userId: Int): Map<String, Int> {
        return getMoods(userId).groupingBy { it.mood }.eachCount()
    }

    fun getMostFrequentMood(userId: Int): Pair<String, Int> {
        val stats = getMoodStats(userId)
        val top = stats.maxByOrNull { it.value }
        return Pair(top?.key ?: "biasa", top?.value ?: 0)
    }
}