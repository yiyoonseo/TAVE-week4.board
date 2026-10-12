# ── 1단계: jar 빌드용 (JDK 있는 이미지) ──
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x gradlew && ./gradlew bootJar --no-daemon -x test

# ── 2단계: 실행용 (가벼운 JRE 이미지에 jar만 복사) ──
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]