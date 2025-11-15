package com.chambainfo.app.utils

import android.content.Context
import android.widget.Toast

/**
 * Muestra un mensaje Toast en el contexto actual.
 *
 * @param message El mensaje a mostrar.
 * @param duration La duración del Toast (por defecto LENGTH_SHORT).
 */
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * Valida si una cadena es un DNI válido (8 dígitos numéricos).
 *
 * @return true si el DNI es válido, false en caso contrario.
 */
fun String.isValidDni(): Boolean {
    return this.length == 8 && this.all { it.isDigit() }
}

/**
 * Valida si una cadena es un número de celular válido (9 dígitos numéricos).
 *
 * @return true si el celular es válido, false en caso contrario.
 */
fun String.isValidCelular(): Boolean {
    return this.length == 9 && this.all { it.isDigit() }
}