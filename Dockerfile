# Usar una imagen base de Java 17
FROM eclipse-temurin:17-jdk-alpine AS build

# Directorio de trabajo
WORKDIR /app

# Copiar archivos de Maven
COPY chambainfo-backend/chambainfo-backend/pom.xml .
COPY chambainfo-backend/chambainfo-backend/.mvn .mvn
COPY chambainfo-backend/chambainfo-backend/mvnw .

# Dar permisos de ejecución
RUN chmod +x mvnw

# Descargar dependencias
RUN ./mvnw dependency:go-offline -B

# Copiar código fuente
COPY chambainfo-backend/chambainfo-backend/src src

# Compilar la aplicación
RUN ./mvnw clean package -DskipTests

# Etapa de producción
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copiar el JAR compilado
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
