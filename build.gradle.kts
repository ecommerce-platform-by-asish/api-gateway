plugins {
    id("org.graalvm.buildtools.native") version "1.1.0"

    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spotless)
}

group = "com.app"
version = "1.0.0-SNAPSHOT"
description = "API Gateway and Edge Service for Ecommerce App"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

dependencies {
    implementation(platform(libs.spring.boot.bom))
    implementation(platform(libs.spring.cloud.bom))

    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.cloud.starter.gateway)
    implementation(libs.spring.boot.starter.data.redis.reactive)
    implementation(libs.spring.boot.starter.oauth2.resource.server)
    
    // Identity & Tracing
    implementation("com.app:shared-common:1.0.0-SNAPSHOT")
    implementation("com.app:shared-security:1.0.0-SNAPSHOT")
    
    // Swagger/OpenAPI Aggregation
    implementation(libs.springdoc.openapi.webflux.ui)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    annotationProcessor(platform(libs.spring.boot.bom))
    annotationProcessor(libs.spring.boot.configuration.processor)
}

tasks.withType<JavaCompile>().configureEach {
    options.isFork = true
    options.forkOptions.jvmArgs = (options.forkOptions.jvmArgs ?: mutableListOf()).apply {
        addAll(listOf(
            "--add-opens", "jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
            "--add-opens", "jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED",
            "--add-opens", "jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
            "--add-opens", "jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED",
            "--add-opens", "jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED",
            "--add-opens", "jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED",
            "--add-opens", "jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED",
            "--add-opens", "jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
            "--add-opens", "jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED"
        ))
    }
    options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-serial", "-Xlint:-processing", "-Xdoclint:none"))
}

spotless {
    java {
        googleJavaFormat("1.27.0")
    }
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.bouncycastle" && requested.name.startsWith("bcprov")) {
            useVersion("1.84")
        }
    }
}
