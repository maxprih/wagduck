# Stage 1: Build the application
FROM "eclipse-temurin:${javaVersion!"17"}-jdk-jammy" AS builder
WORKDIR /app

# Install necessary tools (curl/wget, tar/unzip)
# hadolint ignore=DL3008
RUN apt-get update && apt-get install -y --no-install-recommends curl tar gzip ca-certificates unzip && rm -rf /var/lib/apt/lists/*

<#-- === Build Tool Setup & Wrapper Generation === -->
<#if buildTool == "GRADLE">
    <#-- Download and Setup Gradle -->
    ARG GRADLE_VERSION=${gradleVersion}
    RUN curl -fsSLO https://services.gradle.org/distributions/gradle-${r"${GRADLE_VERSION}"}-bin.zip \
        && unzip gradle-${r"${GRADLE_VERSION}"}-bin.zip -d /opt \
        && ln -s /opt/gradle-${r"${GRADLE_VERSION}"} /opt/gradle \
        && rm gradle-${r"${GRADLE_VERSION}"}-bin.zip
    ENV PATH="/opt/gradle/bin:${r"${PATH}"}"

    <#-- Copy only build files needed for wrapper generation -->
    COPY build.gradle.kts .
    COPY settings.gradle.kts .

    <#-- Generate Gradle Wrapper using the downloaded Gradle -->
    RUN gradle wrapper --gradle-version ${r"${GRADLE_VERSION}"} --no-daemon

    <#-- Make the generated wrapper script executable -->
    RUN chmod +x ./gradlew

    <#-- Download dependencies using the generated wrapper -->
    RUN ./gradlew dependencies --no-daemon

<#else> <#-- Assume Maven -->
    <#-- Download and Setup Maven -->
    ARG MAVEN_VERSION=${mavenVersion}
    RUN curl -fsSLO https://dlcdn.apache.org/maven/maven-3/${r"${MAVEN_VERSION}"}/binaries/apache-maven-${r"${MAVEN_VERSION}"}-bin.tar.gz \
        && tar -xzf apache-maven-${r"${MAVEN_VERSION}"}-bin.tar.gz -C /opt \
        && ln -s /opt/apache-maven-${r"${MAVEN_VERSION}"} /opt/maven \
        && rm apache-maven-${r"${MAVEN_VERSION}"}-bin.tar.gz
    ENV PATH="/opt/maven/bin:${r"${PATH}"}"

    <#-- Copy only pom.xml needed for wrapper generation -->
    COPY pom.xml .

    <#-- Generate Maven Wrapper using the downloaded Maven -->
    RUN mvn -N wrapper:wrapper -Dmaven=${r"${MAVEN_VERSION}"}

    <#-- Make the generated wrapper script executable -->
    RUN chmod +x ./mvnw

    <#-- Download dependencies using the generated wrapper -->
    RUN ./mvnw dependency:go-offline
</#if>
<#-- === End Build Tool Setup === -->

# Copy source code (after dependency download for better caching)
COPY src ./src

# Build the application using the generated wrapper
<#if buildTool == "GRADLE">
RUN ./gradlew build --no-daemon
<#else> <#-- Assume Maven -->
RUN ./mvnw package -DskipTests
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