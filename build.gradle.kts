plugins {
    java
    id("org.springframework.boot") version "4.0.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "8.4.0"
}

spotless {
    java {
        googleJavaFormat()
    }
}

group = "com.app"
version = "0.0.1-SNAPSHOT"
description = "API Gateway for Ecommerce App"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

extra["springCloudVersion"] = "2025.1.1"

dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-gateway-server-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    implementation("com.app:shared-common:1.0.0-SNAPSHOT")
    implementation("com.app:shared-security:1.0.0-SNAPSHOT")
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:3.0.2")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testCompileOnly("org.projectlombok:lombok")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testAnnotationProcessor("org.projectlombok:lombok")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named("check") {
    dependsOn("spotlessCheck")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-serial", "-Xlint:-processing"))
}

val stopApp by tasks.registering(Exec::class) {
    group = "application"
    description = "Stops any running application processes for this service."
    commandLine(
        "sh",
        "-c",
        "ps aux | grep 'bootRun' | grep '${project.name}' | grep -v grep | awk '{print $2}' | xargs kill -9 || true")
}

tasks.named("bootRun") {
    dependsOn(stopApp)
}
