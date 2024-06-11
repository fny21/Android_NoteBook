// C:\Users\c1123\Android_NoteBook\settings.gradle.kts

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.application") version "8.3.1"
        id("org.jetbrains.kotlin.android") version "1.8.0"
        id("com.google.gms.google-services") version "4.3.10"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "NootBook"
include(":app")
