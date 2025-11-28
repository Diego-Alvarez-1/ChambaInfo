# 🚀 Guía de Despliegue en Railway

Esta guía te llevará paso a paso para desplegar el backend de ChambaInfo en Railway.

---

## 📋 Prerequisitos

✅ Proyecto en GitHub: `https://github.com/Diego-Alvarez-1/ChambaInfo.git`  
✅ Archivos creados: `Dockerfile` y `railway.json`  
✅ Base de datos Neon PostgreSQL configurada

---

## 🎯 Paso 1: Crear Cuenta en Railway

1. Ve a **https://railway.app**
2. Click en **"Login"** o **"Start a New Project"**
3. Selecciona **"Login with GitHub"** (recomendado)
4. Autoriza Railway para acceder a tus repositorios

---

## 🎯 Paso 2: Crear Nuevo Proyecto

1. Una vez dentro de Railway, click en **"New Project"**
2. Selecciona **"Deploy from GitHub repo"**
3. Busca y selecciona **"Diego-Alvarez-1/ChambaInfo"**
4. Railway detectará automáticamente el `Dockerfile`

---

## 🎯 Paso 3: Configurar Variables de Entorno

Después de seleccionar el repositorio, Railway te llevará al dashboard del proyecto.

1. Click en tu servicio (aparecerá como "chambainfo" o similar)
2. Ve a la pestaña **"Variables"**
3. Click en **"New Variable"** y agrega las siguientes:

### Variables Requeridas:

```bash
# Base de datos (Neon PostgreSQL)
SPRING_DATASOURCE_URL=jdbc:postgresql://ep-weathered-glade-ad7ooio9-pooler.c-2.us-east-1.aws.neon.tech:5432/neondb?sslmode=require
SPRING_DATASOURCE_USERNAME=neondb_owner
SPRING_DATASOURCE_PASSWORD=npg_BX5JbIUQOnD6

# JWT
JWT_SECRET=ChambaInfo2025SecretKeyMustBeLongEnoughForHS512AlgorithmMinimum512Bits
JWT_EXPIRATION=86400000

# RENIEC API
RENIEC_API_URL=https://api.decolecta.com/v1/reniec/dni
RENIEC_API_TOKEN=sk_10710.ZqPR38Cv4c1EJ4YPob1CGi8SOo1pYfzt

# Server
SERVER_PORT=8080
SERVER_SERVLET_CONTEXT_PATH=/api

# JPA
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=false
SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.PostgreSQLDialect
```

> [!IMPORTANT]
> Copia y pega cada variable **exactamente como está**. Railway las usará automáticamente.

---

## 🎯 Paso 4: Desplegar

1. Después de agregar las variables, Railway iniciará el despliegue automáticamente
2. Verás los logs en tiempo real
3. El proceso tomará **5-10 minutos** la primera vez

### Monitorear el Despliegue:

- Ve a la pestaña **"Deployments"** para ver el progreso
- Los logs mostrarán:
  - ✅ Building Docker image...
  - ✅ Installing dependencies...
  - ✅ Compiling application...
  - ✅ Starting application...
  - ✅ Application started on port 8080

---

## 🎯 Paso 5: Obtener la URL Pública

1. Una vez que el despliegue esté completo (status: **Active**)
2. Ve a la pestaña **"Settings"**
3. Busca la sección **"Networking"** o **"Domains"**
4. Click en **"Generate Domain"**
5. Railway te dará una URL como:
   ```
   https://chambainfo-production.up.railway.app
   ```

> [!TIP]
> Puedes personalizar el dominio si quieres. Por defecto, Railway genera uno automáticamente.

---

## 🎯 Paso 6: Verificar que Funciona

### Probar desde el navegador:

Abre tu navegador y ve a:
```
https://TU-URL-RAILWAY.up.railway.app/api/health
```

O prueba el endpoint de autenticación:
```
https://TU-URL-RAILWAY.up.railway.app/api/auth/login
```

### Probar con cURL:

```bash
curl https://TU-URL-RAILWAY.up.railway.app/api/health
```

Si ves una respuesta, ¡tu backend está funcionando! 🎉

---

## 🎯 Paso 7: Actualizar Android con la URL de Producción

1. Abre `chambainfo-front/app/build.gradle.kts`
2. Busca la sección `release` (línea ~37)
3. Reemplaza la URL con tu URL de Railway:

```kotlin
release {
    buildConfigField("String", "BASE_URL", "\"https://TU-URL-RAILWAY.up.railway.app/api/\"")
    isMinifyEnabled = false
    proguardFiles(...)
}
```

4. Sync Gradle
5. Genera el APK de release:
   ```bash
   cd chambainfo-front
   ./gradlew assembleRelease
   ```

---

## 🎯 Paso 8: Probar el APK en tu Celular

1. El APK estará en: `chambainfo-front/app/build/outputs/apk/release/app-release.apk`
2. Transfiere el APK a tu celular (USB, email, Drive, etc.)
3. Instala el APK
4. Abre la app y prueba el login

¡Debería conectarse al backend en Railway! 🚀

---

## 🔄 Actualizaciones Futuras

Cada vez que hagas cambios en el backend:

1. Haz commit y push a GitHub:
   ```bash
   git add .
   git commit -m "Actualización del backend"
   git push origin main
   ```

2. Railway detectará los cambios automáticamente y redesplegará

---

## 🐛 Troubleshooting

### El despliegue falla:
- Revisa los logs en Railway (pestaña "Deployments")
- Verifica que todas las variables de entorno estén correctas
- Asegúrate de que el `Dockerfile` esté en la raíz del proyecto

### La app no se conecta:
- Verifica que la URL en `build.gradle.kts` sea correcta
- Asegúrate de incluir `/api/` al final de la URL
- Verifica que el backend esté activo en Railway (status: Active)

### Error 502 Bad Gateway:
- El backend está iniciando, espera 1-2 minutos
- Revisa los logs para ver si hay errores de conexión a la base de datos

---

## 💰 Costos

Railway ofrece:
- ✅ **$5 USD de crédito gratis al mes** (sin tarjeta de crédito)
- ✅ **500 horas de ejecución gratis**
- ✅ Suficiente para proyectos pequeños/medianos

Para proyectos más grandes, considera el plan de pago ($5/mes).

---

## 📚 Recursos Adicionales

- [Documentación de Railway](https://docs.railway.app)
- [Railway Discord](https://discord.gg/railway) - Soporte de la comunidad
- [Guía de Spring Boot en Railway](https://docs.railway.app/guides/spring-boot)

---

## ✅ Checklist Final

- [ ] Cuenta de Railway creada
- [ ] Proyecto conectado a GitHub
- [ ] Variables de entorno configuradas
- [ ] Despliegue exitoso (status: Active)
- [ ] URL pública generada
- [ ] Backend verificado (endpoint /api/health responde)
- [ ] URL actualizada en `build.gradle.kts`
- [ ] APK de release generado
- [ ] APK probado en dispositivo físico

---

¡Listo! Tu app Android ahora puede funcionar en cualquier celular con internet 🎉
