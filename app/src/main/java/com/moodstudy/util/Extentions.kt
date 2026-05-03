package com.moodstudy.util

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.Toast
import com.moodstudy.R

fun Activity.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Activity.navigateTo(clazz: Class<*>, clearStack: Boolean = false) {
    val intent = Intent(this, clazz)
    if (clearStack) {
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    startActivity(intent)

    if (android.os.Build.VERSION.SDK_INT >= 34) {
        overrideActivityTransition(
            android.app.Activity.OVERRIDE_TRANSITION_OPEN,
            R.anim.slide_in_right,
            R.anim.slide_out_left
        )
    } else {
        @Suppress("DEPRECATION")
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
}

fun View.visible()   { visibility = View.VISIBLE }
fun View.gone()      { visibility = View.GONE }
fun View.invisible() { visibility = View.INVISIBLE }