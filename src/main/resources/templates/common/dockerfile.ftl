# Stage 1: Build the application
FROM "eclipse-temurin:${javaVersion!"17"}-jdk-jammy" AS builder
WORKDIR /app

# Copy pom.xml/build.gradle.kts and wrapper scripts first to leverage Docker cache
<#if buildTool == "GRADLE">
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle.kts .
COPY settings.gradle.kts . <#-- Assuming standard settings file exists -->
<#else> <#-- Assume Maven -->
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
</#if>

# Download dependencies (Gradle) or plugins/dependencies (Maven)
<#if buildTool == "GRADLE">
RUN ./gradlew dependencies
<#else> <#-- Assume Maven -->
RUN ./mvnw dependency:go-offline
</#if>

# Copy source code
COPY src ./src

# Build the application
<#if buildTool == "GRADLE">
RUN ./gradlew build --no-daemon <#-- Use --no-daemon for CI/Docker -->
<#else> <#-- Assume Maven -->
RUN ./mvnw package -DskipTests <#-- Skip tests during docker build -->
</#if>

# Stage 2: Create the final runtime image
FROM "eclipse-temurin:${javaVersion!"17"}-jre-jammy" AS runtime
WORKDIR /app

# Copy the built JAR file from the builder stage
<#if buildTool == "GRADLE">
COPY --from=builder /app/build/libs/${jarNamePattern!"*.jar"} app.jar
<#else> <#-- Assume Maven -->
COPY --from=builder /app/target/${jarNamePattern!"*.jar"} app.jar
</#if>

# Expose the application port
EXPOSE ${appPort!8080}

# Set the entrypoint to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

# Optional: Add user/group for security best practices
# RUN addgroup -S appgroup && adduser -S appuser -G appgroup
# USER appuser