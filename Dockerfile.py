dockerfile_content = """# ==========================================
# Fase de Compilación (Build Stage)
# ==========================================
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Copiar archivos de configuración de Maven y dependencias
mvnw pom.xml ./
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

# Copiar el código fuente y empaquetar la aplicación
COPY src src
RUN ./mvnw clean package -DskipTests

# ==========================================
# Fase de Ejecución (Runtime Stage)
# ==========================================
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Crear un usuario no privilegiado por seguridad
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# Copiar el JAR generado desde la fase de construcción
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto en el que corre Spring Boot
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
"""

with open("Dockerfile", "w", encoding="utf-8") as f:
    f.write(dockerfile_content)

print("Dockerfile created successfully")