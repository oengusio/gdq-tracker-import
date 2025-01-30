plugins {
    application
    kotlin("jvm") version libs.versions.kotlin
}

group = "net.oengus"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(libs.jline)

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
