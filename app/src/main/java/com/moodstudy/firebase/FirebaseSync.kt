package com.moodstudy.firebase

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.moodstudy.data.model.MoodEntry

object FirebaseSync {

    private const val TAG = "FirebaseSync"

    fun pushMood(email: String, mood: MoodEntry) {
        if (email.isBlank()) return

        val db = Firebase.firestore
        val docRef = db.collection("users")
            .document(email.lowercase())
            .collection("moods")
            .document(mood.tanggal) // pakai timestamp sebagai id

        val data = hashMapOf(
            "tanggal" to mood.tanggal,
            "mood" to mood.mood,
            "moodLabel" to mood.moodLabel,
            "moodScore" to mood.moodScore,
            "catatan" to (mood.catatan ?: "")
        )

        docRef.set(data)
            .addOnSuccessListener { Log.d(TAG, "Mood synced to Firestore") }
            .addOnFailureListener { e -> Log.e(TAG, "Failed to sync mood", e) }
    }
}