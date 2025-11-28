## 🎉 Parte 1 Completada: Build Variants Configurados

### ✅ Cambios Realizados

He configurado exitosamente los 3 build variants en tu proyecto Android:

#### 1. Modificado `build.gradle.kts`
- ✅ **debug**: Para emulador → `http://10.0.2.2:8080/api/`
- ✅ **debugDevice**: Para celular físico → `http://192.168.20.54:8080/api/`
- ✅ **release**: Para producción → `https://chambainfo-production.up.railway.app/api/`

#### 2. Actualizado `Constants.kt`
- ✅ Ahora usa `BuildConfig.BASE_URL` en lugar de URL hardcodeada
- ✅ La URL cambia automáticamente según el build variant

---

### 📱 Cómo Usar Cada Build Variant

#### **Opción 1: Desarrollo en Emulador** (debug)
```bash
cd chambainfo-front
./gradlew assembleDebug
```
O simplemente presiona **Run** en Android Studio (usa debug por defecto)

#### **Opción 2: Desarrollo en Celular Físico** (debugDevice)

**Paso 1:** Cambia la IP cuando cambies de red

Abre `build.gradle.kts` y busca esta línea:
```kotlin
buildConfigField("String", "BASE_URL", "\"http://192.168.20.54:8080/api/\"")
//                                              ↑ Cambia esta IP
```

**Paso 2:** En Android Studio, selecciona el build variant:
- Ve a: **View → Tool Windows → Build Variants**
- Selecciona **debugDevice** en el dropdown
- Conecta tu celular por USB
- Presiona **Run**

O desde terminal:
```bash
./gradlew assembleDebugDevice
```

**Paso 3:** Instala el APK en tu celular:
```bash
adb install app/build/outputs/apk/debugDevice/app-debugdevice.apk
```

#### **Opción 3: APK de Producción** (release)
```bash
./gradlew assembleRelease
# APK estará en: app/build/outputs/apk/release/app-release.apk
```

---

### 💡 Tips Importantes

#### Cómo saber tu IP actual:
```bash
# En Mac/Linux:
ifconfig | grep "inet " | grep -v 127.0.0.1

# En Windows (CMD):
ipconfig
```

#### Cambiar de red (casa → universidad):
1. Abre `build.gradle.kts`
2. Busca la sección `debugDevice`
3. Cambia la IP:
   ```kotlin
   // En casa:
   buildConfigField("String", "BASE_URL", "\"http://192.168.1.100:8080/api/\"")
   
   // En la universidad:
   buildConfigField("String", "BASE_URL", "\"http://192.168.20.54:8080/api/\"")
   ```
4. Click en **Sync Now** en Android Studio
5. Vuelve a ejecutar la app

---

### 🚀 Siguiente Paso: Desplegar Backend en Railway

Ahora que la app está configurada, necesitamos desplegar el backend en la nube para que el APK de **release** funcione en cualquier celular.

¿Quieres que continuemos con el despliegue en Railway?
