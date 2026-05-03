package com.moodstudy.data.model

data class MoodEntry(
    val id: Int = 0,
    val userId: Int,
    val tanggal: String,
    val mood: String,
    val catatan: String = ""
) {
    val moodEmoji: String
        get() = when (mood) {
            "semangat" -> "🔥"
            "biasa"    -> "😐"
            "capek"    -> "😴"
            "stres"    -> "😣"
            "bosan"    -> "😑"
            else       -> "🙂"
        }

    val moodLabel: String
        get() = when (mood) {
            "semangat" -> "Semangat"
            "biasa"    -> "Biasa"
            "capek"    -> "Capek"
            "stres"    -> "Stres"
            "bosan"    -> "Bosan"
            else       -> mood
        }

    val moodScore: Float
        get() = when (mood) {
            "semangat" -> 5f
            "biasa"    -> 3f
            "capek"    -> 2f
            "stres"    -> 1f
            "bosan"    -> 2f
            else       -> 3f
        }
}