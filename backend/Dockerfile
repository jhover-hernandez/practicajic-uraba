# RNF-13 · Imagen del backend para el piloto.
# 1) Compila con Maven  2) Ejecuta solo el .jar con el JRE (imagen más pequeña y sin herramientas de compilación).
FROM maven:3.9.9-eclipse-temurin-17 AS compilacion
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q package -DskipTests

FROM eclipse-temurin:17.0.13_11-jre
WORKDIR /app
RUN useradd --system --uid 1001 practicajic && mkdir -p /datos/archivos && chown -R practicajic /datos
COPY --from=compilacion /app/target/practicajic-*.jar app.jar
USER practicajic
ENV APP_ARCHIVOS_DIRECTORIO=/datos/archivos
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
