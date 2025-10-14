package com.chambainfo.app.utils

import android.content.Context
import android.widget.Toast

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun String.isValidDni(): Boolean {
    return this.length == 8 && this.all { it.isDigit() }
}

fun String.isValidCelular(): Boolean {
    return this.length == 9 && this.all { it.isDigit() }
}