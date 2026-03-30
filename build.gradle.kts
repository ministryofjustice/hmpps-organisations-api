plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "10.1.1"
  id("org.openapi.generator") version "7.21.0"
  kotlin("plugin.spring") version "2.3.20"
  kotlin("plugin.jpa") version "2.3.20"
}

allOpen {
  annotations(
    "javax.persistence.Entity",
    "javax.persistence.MappedSuperclass",
    "javax.persistence.Embeddable",
  )
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  // Spring boot dependencies
  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:2.1.0")
  implementation("org.springframework.boot:spring-boot-starter-webclient")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-flyway")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:7.3.0")
  implementation("io.sentry:sentry-spring-boot-4-starter:8.37.1")
  implementation("io.github.cdimascio:dotenv-kotlin:6.5.1")
  implementation("org.springframework.boot:spring-boot-starter-validation")

  // Database dependencies
  runtimeOnly("org.flywaydb:flyway-database-postgresql")
  runtimeOnly("org.postgresql:postgresql:42.7.10")
  implementation("org.hibernate.orm:hibernate-envers")
  implementation("org.springframework.data:spring-data-envers")

  // OpenAPI
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.2")
  implementation("org.openapitools:jackson-databind-nullable:0.2.10")

  implementation("io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations:2.26.1")

  // Test dependencies
  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:2.1.0")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.39") {
    exclude(group = "io.swagger.core.v3")
  }
  testImplementation("io.jsonwebtoken:jjwt-impl:0.13.0")
  testImplementation("io.jsonwebtoken:jjwt-jackson:0.13.0")
  testImplementation("net.javacrumbs.json-unit:json-unit:5.1.1")
  testImplementation("net.javacrumbs.json-unit:json-unit-assertj:5.1.1")
  testImplementation("net.javacrumbs.json-unit:json-unit-json-path:5.1.1")
  testImplementation("org.awaitility:awaitility-kotlin:4.3.0")
  testImplementation("org.mockito:mockito-inline:5.2.0")
  testImplementation("org.springframework.boot:spring-boot-starter-webclient-test")
  testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("org.testcontainers:testcontainers-postgresql:2.0.4")
  testImplementation("org.wiremock:wiremock-standalone:3.13.2")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.39") {
    exclude(group = "io.swagger.core.v3")
  }
}

kotlin {
  jvmToolchain(25)
}

java {
  sourceCompatibility = JavaVersion.VERSION_24
  targetCompatibility = JavaVersion.VERSION_24
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24
    compilerOptions.freeCompilerArgs.add("-Xannotation-default-target=param-property")
  }
}
