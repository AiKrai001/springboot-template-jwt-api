plugins {
  kotlin("jvm") version "2.2.21"
  kotlin("plugin.spring") version "2.2.21"
  id("com.diffplug.spotless") version "6.25.0"
  id("org.springframework.boot") version "4.0.5"
  id("io.spring.dependency-management") version "1.1.7"
  id("com.google.devtools.ksp") version "2.2.21-2.0.4"
  id("tech.argonariod.gradle-plugin-jimmer") version "1.2.0"
}

group = "com.app"
version = "0.0.1-SNAPSHOT"
val jimmerVersion = "0.10.6"
val springdocVersion = "3.0.3"


java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(21)
  }
}

repositories {
  mavenCentral()
}

kotlin {
  compilerOptions {
    freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
  }
}

spotless {
  kotlin {
    ktlint()
      .editorConfigOverride(
        mapOf(
          "ktlint_standard_no-wildcard-imports" to "disabled",
          "ktlint_standard_trailing-comma-on-call-site" to "disabled",
          "ktlint_standard_trailing-comma-on-declaration-site" to "disabled",
          "indent_size" to "2"
        )
      )
    target("src/**/*.kt")
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}

jimmer {
  version = jimmerVersion
}

dependencies {
  implementation(kotlin("reflect"))
  implementation(kotlin("stdlib-jdk8"))
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.8.1")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-opentelemetry")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-jdbc")
  implementation("tools.jackson.module:jackson-module-kotlin")

  // redis
  implementation("org.springframework.boot:spring-boot-starter-data-redis")
  runtimeOnly("io.micrometer:micrometer-registry-prometheus")
//  implementation("com.fasterxml.jackson.core:jackson-databind")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.boot:spring-boot-webmvc-test")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
  // springdoc
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${springdocVersion}")

  implementation("org.babyfish.jimmer:jimmer-spring-boot-starter:${jimmerVersion}")

  // db
  implementation("org.postgresql:postgresql")
  runtimeOnly("com.h2database:h2")
//  implementation("com.ongres.scram:client:2.1")

  // lombok
  compileOnly("org.projectlombok:lombok")

  // hutool
//  implementation("cn.hutool:hutool-all:5.8.24")
  implementation("cn.hutool:hutool-all:5.8.43")
  // sa-token
  implementation("cn.dev33:sa-token-spring-boot4-starter:1.45.0")
  implementation("cn.dev33:sa-token-jwt:1.45.0")


//  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.3")
}
