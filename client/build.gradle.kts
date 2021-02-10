plugins {
    kotlin("js")
    kotlin("plugin.serialization") version "1.4.20"
}

dependencies {
    implementation(kotlin("stdlib-js"))
    implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.4.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
}

kotlin {
    js {
        browser {}
        binaries.executable()
    }
}