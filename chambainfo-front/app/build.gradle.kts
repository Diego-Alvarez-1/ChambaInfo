plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.chambainfo.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.chambainfo.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            // Para EMULADOR de Android Studio
            // 10.0.2.2 es la IP especial del emulador que apunta a localhost
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080/api/\"")
            applicationIdSuffix = ".debug"
        }

        create("debugDevice") {
            // Para CELULAR FÍSICO en desarrollo (casa, universidad, etc.)
            // ⚠️ CAMBIA ESTA IP según tu red actual
            buildConfigField("String", "BASE_URL", "\"http://192.168.20.54:8080/api/\"")
            applicationIdSuffix = ".debugdevice"
            isDebuggable = true
        }

        release {
            // Para PRODUCCIÓN (APK final con backend en la nube)
            // Esta URL se actualizará después de desplegar en Railway
            buildConfigField("String", "BASE_URL", "\"https://chambainfo-production.up.railway.app/api/\"")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true  // Habilitar BuildConfig para usar las constantes
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Activity & Fragment
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.fragment:fragment-ktx:1.8.0")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.0")

    // Retrofit para API REST
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // DataStore para guardar token
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // CardView & RecyclerView
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // CoordinatorLayout
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("com.google.code.gson:gson:2.10.1")
}