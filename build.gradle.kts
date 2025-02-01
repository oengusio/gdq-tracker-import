plugins {
    application
    kotlin("jvm") version libs.versions.kotlin
    id("com.gradleup.shadow") version libs.versions.shadow
    kotlin("plugin.serialization") version libs.versions.plugin.serialisation
}

group = "net.oengus"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(libs.jline)
    implementation(libs.jline.console)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("net.oengus.gdqimporter.MainKt")
}

kotlin {
    jvmToolchain(21)
}
