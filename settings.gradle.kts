/*
 * This file was generated by the Gradle 'init' task.
 *
 * The settings file is used to specify which projects to include in your build.
 *
 * Detailed information about configuring a multi-project build in Gradle can be found
 * in the user manual at https://docs.gradle.org/6.0.1/userguide/multi_project_builds.html
 */

pluginManagement {
    repositories {
        mavenCentral()
        jcenter()
        maven { url = uri("https://kotlin.bintray.com/kotlin-eap") }
        maven { url = uri("https://plugins.gradle.org/m2/") }
        maven { url = uri("https://kotlin.bintray.com/kotlinx") }
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.google.protobuf") {
                useModule("com.google.protobuf:protobuf-gradle-plugin:${requested.version}")
            }
        }
    }
}

rootProject.name = "3k-operator-sdk"
include("annotation")
include("core")
include("generator")
include("sample-app")