plugins {
    java
    alias(libs.plugins.springboot)
    alias(libs.plugins.spotless)
}

group = "com.app"
version = "1.0.0-SNAPSHOT"

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(25)) }
}

dependencies {
    implementation(platform(libs.sb.bom))
    implementation(platform(libs.sc.bom))

    implementation(libs.sb.starter.actuator)
    implementation(libs.sc.starter.gateway)
    implementation(libs.sb.starter.data.redis.reactive)
    implementation(libs.sb.starter.oauth2.resource.server)
    
    implementation("com.app:shared-common:1.0.0-SNAPSHOT")
    implementation("com.app:shared-security:1.0.0-SNAPSHOT")
    
    implementation(libs.springdoc.openapi.webflux.ui)
    
    developmentOnly(platform(libs.sb.bom))
    developmentOnly(libs.sb.docker.compose)

    compileOnly(libs.lombok)
    annotationProcessor(platform(libs.sb.bom))
    annotationProcessor(libs.lombok)
}

tasks.withType<JavaCompile>().configureEach {
    options.isFork = true
    options.forkOptions.jvmArgs = (options.forkOptions.jvmArgs ?: mutableListOf()).apply {
        addAll(listOf("--add-opens", "jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED", "--add-opens", "jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED", "--add-opens", "jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED", "--add-opens", "jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED", "--add-opens", "jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED", "--add-opens", "jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED", "--add-opens", "jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED", "--add-opens", "jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED", "--add-opens", "jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED"))
    }
}

spotless { java { googleJavaFormat("1.27.0") } }

tasks.withType<org.springframework.boot.gradle.tasks.run.BootRun> { systemProperties(System.getProperties().map { it.key.toString() to it.value }.toMap()) }

tasks.register<Exec>("stopApp") {
    group = "application"
    description = "Stops the running api-gateway application."
    commandLine("sh", "-c", "lsof -t -i:8080 | xargs kill -9 || true")
}
