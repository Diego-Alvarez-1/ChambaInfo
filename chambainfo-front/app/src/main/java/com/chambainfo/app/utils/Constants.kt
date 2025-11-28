package com.chambainfo.app.utils

import com.chambainfo.app.BuildConfig

object Constants {
    // La URL se configura automáticamente según el buildType:
    // - debug: http://10.0.2.2:8080/api/ (emulador)
    // - debugDevice: http://192.168.20.54:8080/api/ (celular físico - configurable)
    // - release: https://chambainfo-production.up.railway.app/api/ (producción)
    const val BASE_URL = BuildConfig.BASE_URL
}
