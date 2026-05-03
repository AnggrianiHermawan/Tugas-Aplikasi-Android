package com.moodstudy.data.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.moodstudy.data.model.MoodEntry
import com.moodstudy.data.model.User

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME = "moodstudy.db"
        private const val DB_VERSION = 1
        const val TABLE_USER = "users"
        const val TABLE_MOOD = "mood_entries"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_USER (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nama TEXT NOT NULL,
                email TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_MOOD (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                tanggal TEXT NOT NULL,
                mood TEXT NOT NULL,
                catatan TEXT DEFAULT '',
                FOREIGN KEY(user_id) REFERENCES $TABLE_USER(id)
            )
            """.trimIndent()
        )

        // seed user default
        val cv = ContentValues().apply {
            put("nama", "Anggriani")
            put("email", "anggriani@gmail.com")
            put("password", "12345678")
        }
        db.insert(TABLE_USER, null, cv)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MOOD")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        onCreate(db)
    }

    fun insertUser(nama: String, email: String, password: String): Long {
        val cv = ContentValues().apply {
            put("nama", nama)
            put("email", email)
            put("password", password)
        }
        return writableDatabase.insert(TABLE_USER, null, cv)
    }

    fun isEmailTaken(email: String): Boolean {
        val cursor = readableDatabase.query(
            TABLE_USER, arrayOf("id"),
            "email=?", arrayOf(email), null, null, null
        )
        return cursor.use { it.count > 0 }
    }

    fun loginUser(email: String, password: String): User? {
        val cursor = readableDatabase.query(
            TABLE_USER, null,
            "email=? AND password=?", arrayOf(email, password),
            null, null, null
        )
        return cursor.use {
            if (it.moveToFirst()) {
                User(
                    it.getInt(0),
                    it.getString(1),
                    it.getString(2),
                    it.getString(3)
                )
            } else null
        }
    }

    fun getUserByEmail(email: String): User? {
        val cursor = readableDatabase.query(
            TABLE_USER, null,
            "email=?", arrayOf(email), null, null, null
        )
        return cursor.use {
            if (it.moveToFirst()) {
                User(
                    it.getInt(0),
                    it.getString(1),
                    it.getString(2),
                    it.getString(3)
                )
            } else null
        }
    }

    fun insertMood(userId: Int, tanggal: String, mood: String, catatan: String): Long {
        val cv = ContentValues().apply {
            put("user_id", userId)
            put("tanggal", tanggal)
            put("mood", mood)
            put("catatan", catatan)
        }
        return writableDatabase.insert(TABLE_MOOD, null, cv)
    }

    fun getMoodsByUser(userId: Int): List<MoodEntry> {
        val list = mutableListOf<MoodEntry>()
        val cursor = readableDatabase.query(
            TABLE_MOOD, null,
            "user_id=?", arrayOf(userId.toString()),
            null, null, "tanggal DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    MoodEntry(
                        id = it.getInt(0),
                        userId = it.getInt(1),
                        tanggal = it.getString(2),
                        mood = it.getString(3),
                        catatan = it.getString(4)
                    )
                )
            }
        }
        return list
    }

    fun getLatestMood(userId: Int): MoodEntry? {
        val cursor = readableDatabase.query(
            TABLE_MOOD, null,
            "user_id=?", arrayOf(userId.toString()),
            null, null, "tanggal DESC", "1"
        )
        return cursor.use {
            if (it.moveToFirst()) {
                MoodEntry(
                    id = it.getInt(0),
                    userId = it.getInt(1),
                    tanggal = it.getString(2),
                    mood = it.getString(3),
                    catatan = it.getString(4)
                )
            } else null
        }
    }
}