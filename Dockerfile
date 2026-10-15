# ---- Stage 1: build the Angular UI ----
FROM node:24-alpine AS frontend
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npx ng build --configuration production

# ---- Stage 2: build the Spring Boot jar with the UI inside ----
FROM maven:3.9-eclipse-temurin-21 AS backend
WORKDIR /app/backend
COPY backend/pom.xml ./
COPY backend/src ./src
COPY --from=frontend /app/frontend/dist/frontend/browser/ ./src/main/resources/static/
# tests run locally before every push; the image build only packages
RUN mvn -B -q package -DskipTests

# ---- Stage 3: small runtime image ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend /app/backend/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]