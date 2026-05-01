plugins {
    java
    idea
    alias(libs.plugins.springboot)
    alias(libs.plugins.spotless)
    `jvm-test-suite`
}

group = "com.app"
version = "1.0.0-SNAPSHOT"

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(25)) }
}

dependencies {
    implementation(platform(libs.sb.bom))
    implementation(platform(libs.sc.bom))

    implementation(libs.bundles.reactive.gateway)
    implementation(libs.sb.starter.oauth2.resource.server)
    implementation("com.app:shared-common:1.0.0-SNAPSHOT")
    implementation("com.app:shared-security:1.0.0-SNAPSHOT")
    
    implementation(libs.springdoc.openapi.webflux.ui)
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-reactor-resilience4j")
    
    developmentOnly(platform(libs.sb.bom))
    developmentOnly(libs.sb.docker.compose)

    compileOnly(libs.lombok)
    annotationProcessor(platform(libs.sb.bom))
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.sb.configuration.processor)
    
    testImplementation(platform(libs.testcontainers.bom))
    testImplementation(libs.sb.starter.test)
    testImplementation(libs.bundles.testcontainers)
    testImplementation(libs.jjwt.api)
    testImplementation(libs.jjwt.impl)
    testImplementation(libs.jjwt.jackson)
    testRuntimeOnly(libs.junit.platform.launcher)
}


@Suppress("UnstableApiUsage")
testing {
    suites {
        // 1. Standard Test Suite (Excludes Blackbox/Integration)
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            targets {
                all {
                    testTask.configure {
                        exclude("**/blackbox/**")
                        exclude("**/integration/**")
                        filter.isFailOnNoMatchingTests = false
                        failOnNoDiscoveredTests = false
                    }
                }
            }
        }

        // 2. Integration Test Suite (Overlaps src/test/java)
        register<JvmTestSuite>("integrationTest") {
            useJUnitJupiter()
            sources {
                java.setSrcDirs(listOf("src/test/java"))
                resources.setSrcDirs(listOf("src/test/resources"))
            }
            dependencies {
                implementation(project())
                implementation(libs.sb.starter.test)
                libs.bundles.testcontainers.get().forEach { implementation(it) }
                implementation("com.app:shared-common:1.0.0-SNAPSHOT")
                implementation("com.app:shared-security:1.0.0-SNAPSHOT")
                implementation(libs.sb.starter.webflux)
                implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-reactor-resilience4j")
            }
            targets {
                all {
                    testTask.configure {
                        include("**/integration/**")
                        shouldRunAfter(test)
                    }
                }
            }
        }

        // 3. Blackbox Test Suite (Overlaps src/test/java)
        register<JvmTestSuite>("blackboxTest") {
            useJUnitJupiter()
            sources {
                java.setSrcDirs(listOf("src/test/java"))
                resources.setSrcDirs(listOf("src/test/resources"))
            }
            dependencies {
                implementation(project())
                implementation(libs.sb.starter.test)
                libs.bundles.testcontainers.get().forEach { implementation(it) }
                implementation(libs.jjwt.api)
                implementation(libs.jjwt.impl)
                implementation(libs.jjwt.jackson)
                implementation("com.app:shared-common:1.0.0-SNAPSHOT")
                implementation("com.app:shared-security:1.0.0-SNAPSHOT")
                implementation(libs.sb.starter.webflux)
                implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-reactor-resilience4j")
            }
            targets {
                all {
                    testTask.configure {
                        include("**/blackbox/**")
                        shouldRunAfter(testing.suites.named("integrationTest"))
                        testLogging {
                            events("passed", "skipped", "failed")
                            showStandardStreams = true
                        }
                    }
                }
            }
        }
    }
}

tasks.named("check") {
    dependsOn(testing.suites.named("integrationTest"), testing.suites.named("blackboxTest"))
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.removeAll { it == "--enable-preview" }
}

spotless {
    java {
        googleJavaFormat("1.27.0")
        removeUnusedImports()
    }
}

tasks.bootBuildImage { environment.put("BP_JVM_CDS_ENABLED", "true") }


tasks.withType<org.springframework.boot.gradle.tasks.run.BootRun> { systemProperties(System.getProperties().map { it.key.toString() to it.value }.toMap()) }

tasks.register<Exec>("stopApp") {
    group = "application"
    description = "Stops the running api-gateway application."
    commandLine("sh", "-c", "lsof -t -i:8080 | xargs kill -9 || true")
}

tasks.clean {
    mustRunAfter("spotlessApply")
}
