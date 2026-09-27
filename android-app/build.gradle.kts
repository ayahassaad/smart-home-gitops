// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.6.1" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    // Kotlin 2.0+ moved the Compose compiler out of AGP's composeOptions
    // block and into its own Gradle plugin — required whenever
    // org.jetbrains.kotlin.android is 2.0 or newer.
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}
