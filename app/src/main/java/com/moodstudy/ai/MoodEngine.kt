package com.moodstudy.ai

data class RekomendasiItem(
    val icon: String,
    val title: String,
    val desc: String
)

object MoodEngine {

    fun getMoodEmoji(mood: String): String = when (mood) {
        "semangat" -> "🔥"
        "capek"    -> "😴"
        "stres"    -> "😣"
        "bosan"    -> "😑"
        "biasa"    -> "😐"
        else       -> "🙂"
    }

    fun getHeroTitle(mood: String): String = when (mood) {
        "semangat" -> "Kamu Lagi Semangat! 🔥"
        "capek"    -> "Kamu Lagi Capek 😴"
        "stres"    -> "Kamu Lagi Stres 😣"
        "bosan"    -> "Kamu Lagi Bosan 😑"
        "biasa"    -> "Mood Kamu Biasa Aja 🙂"
        else       -> "Mood Kamu Hari Ini"
    }

    fun getHeroSubtitle(mood: String): String = when (mood) {
        "semangat" -> "Waktu terbaik untuk produktif!"
        "capek"    -> "Istirahat sebentar juga bagian dari produktif."
        "stres"    -> "Tenang, kita atur ritme belajarnya pelan‑pelan."
        "bosan"    -> "Coba ubah suasana atau metode belajar kamu."
        "biasa"    -> "Mood stabil, cocok buat belajar santai."
        else       -> "Sesuaikan ritme belajar dengan perasaanmu."
    }

    fun getRekomendasi(mood: String): List<RekomendasiItem> = when (mood) {
        "semangat" -> listOf(
            RekomendasiItem(
                icon = "📚",
                title = "Kerjakan tugas paling sulit dulu",
                desc = "Manfaatkan energi tinggi untuk ngerjain materi yang paling menantang."
            ),
            RekomendasiItem(
                icon = "⏱️",
                title = "Fokus belajar 1–2 jam",
                desc = "Atur sesi fokus (misal teknik Pomodoro) sebelum istirahat."
            ),
            RekomendasiItem(
                icon = "🎯",
                title = "Minimalkan distraksi",
                desc = "Matikan notifikasi yang tidak penting supaya fokus maksimal."
            )
        )

        "capek" -> listOf(
            RekomendasiItem(
                icon = "☕",
                title = "Istirahat 20–30 menit",
                desc = "Rebah sebentar, minum air, tarik napas dalam, jangan dipaksa."
            ),
            RekomendasiItem(
                icon = "🎧",
                title = "Belajar ringan",
                desc = "Pilih video pembelajaran ringan atau rangkuman singkat."
            ),
            RekomendasiItem(
                icon = "🛌",
                title = "Prioritaskan kondisi badan",
                desc = "Kalau benar‑benar lelah, lebih baik tidur cukup dulu."
            )
        )

        "stres" -> listOf(
            RekomendasiItem(
                icon = "🧘",
                title = "Tarik napas & stretching",
                desc = "Lakukan peregangan ringan dan atur napas 3–5 menit."
            ),
            RekomendasiItem(
                icon = "✏️",
                title = "Breakdown tugas besar",
                desc = "Pecah tugas besar jadi langkah kecil yang jelas."
            ),
            RekomendasiItem(
                icon = "✅",
                title = "Mulai dari hal termudah",
                desc = "Kerjakan bagian paling gampang dulu supaya rasa stres berkurang."
            )
        )

        "bosan" -> listOf(
            RekomendasiItem(
                icon = "🎮",
                title = "Ganti mode belajar",
                desc = "Coba quiz, flashcard, atau diskusi bareng teman."
            ),
            RekomendasiItem(
                icon = "📍",
                title = "Ubah lokasi belajar",
                desc = "Belajar di tempat baru (kafe, perpustakaan, ruang lain di rumah)."
            ),
            RekomendasiItem(
                icon = "🎵",
                title = "Belajar dengan musik",
                desc = "Putar playlist lo‑fi atau instrumental supaya lebih santai."
            )
        )

        "biasa" -> listOf(
            RekomendasiItem(
                icon = "📖",
                title = "Review materi kemarin",
                desc = "Cocok buat mengulang catatan atau latihan soal ringan."
            ),
            RekomendasiItem(
                icon = "📝",
                title = "Buat to‑do list singkat",
                desc = "Tulis 3 hal yang mau kamu selesaikan hari ini."
            ),
            RekomendasiItem(
                icon = "🚶",
                title = "Jeda singkat tiap 30 menit",
                desc = "Bangun sebentar, jalan sedikit, lalu lanjut belajar."
            )
        )

        else -> getRekomendasi("biasa")
    }
}