/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Kotlin library project to get you started.
 */

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    kotlin("jvm") version "1.3.61"
    kotlin("kapt") version "1.3.61"
    kotlin("plugin.serialization") version "1.3.61" apply false
    id("com.google.protobuf") version "0.8.11" apply false
    id("com.github.marcoferrer.kroto-plus") version "0.6.1" apply false
    checkstyle
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
}

allprojects {
    //apply(plugin = "java-library")
    apply(plugin = "kotlin")
    apply(plugin = "kotlin-kapt")
    apply(plugin = "kotlinx-serialization")
    apply(plugin = "checkstyle")

    // Add Proto Generator Project
    if (name.contains("-proto")) {
        apply(plugin = "com.google.protobuf")
        apply(plugin = "com.github.marcoferrer.kroto-plus")
    }

    dependencies {
        // BOM Definitions
        implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
        implementation(platform("io.ktor:ktor-bom:1.3.1"))
        implementation(platform("io.grpc:grpc-bom:1.20.0"))
        // Issue with Guava Versions
        //implementation(platform("io.grpc:grpc-bom:1.28.0"))
        implementation(platform("org.junit:junit-bom:5.6.0"))

        implementation("ch.qos.logback:logback-classic:1.2.1")

        // Kotlin Dependencies
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        compile("org.jetbrains.kotlin:kotlin-compiler-embeddable")
        compile("org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable")
        compile("org.jetbrains.kotlin:kotlin-script-util")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.14.0")
        implementation("com.squareup:kotlinpoet:1.5.0")

        implementation("org.kodein.di:kodein-di-generic-jvm:6.4.1")
        implementation("org.kodein.di:kodein-di-framework-ktor-server-jvm:6.4.1")

        // Ktor Libraries
        implementation("io.ktor:ktor-server-core")
        implementation("io.ktor:ktor-metrics")

        // GRPC
        implementation("io.grpc:grpc-netty") {
            exclude("com.google.guava", "guava")
        }
        implementation("io.grpc:grpc-protobuf") {
            exclude("com.google.guava", "guava")
        }
        implementation("io.grpc:grpc-stub") {
            exclude("com.google.guava", "guava")
        }

        implementation("io.kubernetes:client-java:8.0.0")
        implementation("io.kubernetes:client-java-extended:8.0.0")
        //implementation("io.fabric8:kubernetes-client:4.8.0")

        //Test library.
        testImplementation("org.jetbrains.kotlin:kotlin-test")
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
        testImplementation("org.junit.jupiter:junit-jupiter-engine")
        testImplementation("org.junit.jupiter:junit-jupiter-api")
        testImplementation("io.mockk:mockk:1.9.3")
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
        kotlinOptions.suppressWarnings = true
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    configurations {
        implementation {
            resolutionStrategy.failOnVersionConflict()
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform {
            includeEngines("junit-jupiter")
        }
        failFast = true
        testLogging {
            events = setOf(
                org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
                org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED,
                org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
            )
        }
    }

    repositories {
        mavenCentral()
        jcenter()
        maven { url = uri("https://kotlin.bintray.com/ktor") }
        maven { url = uri("https://plugins.gradle.org/m2/") }
        maven { url = uri("https://kotlin.bintray.com/kotlinx") }
    }
}


project(":annotation") {
    apply(plugin = "java-library")
    dependencies {
        implementation(project(":core"))
    }
}

project(":core") {

}

project(":generator") {
    dependencies {
        implementation(project(":core"))
    }
}

project(":sample-app") {
    dependencies {
        implementation(project(":core"))
        implementation("io.ktor:ktor-server-netty")
        implementation("io.ktor:ktor-auth")
        implementation("io.ktor:ktor-gson")
    }
}

tasks {
    test {
        testLogging.showExceptions = true
    }
}