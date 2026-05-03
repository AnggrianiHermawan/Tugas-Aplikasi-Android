package com.moodstudy.data.repository

import android.content.ContentValues
import com.moodstudy.data.db.DatabaseHelper

class UserRepository(private val dbHelper: DatabaseHelper) {

    fun isEmailExist(email: String): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_USER,
            arrayOf("id"),
            "email = ?",
            arrayOf(email),
            null,
            null,
            null
        )
        val exists = cursor.moveToFirst()
        cursor.close()
        return exists
    }

    fun registerUser(name: String, email: String, password: String): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            // kolomnya 'nama', bukan 'name'
            put("nama", name)
            put("email", email)
            put("password", password)
        }
        return db.insert(DatabaseHelper.TABLE_USER, null, values)
    }
}