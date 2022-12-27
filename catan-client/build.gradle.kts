import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion = "1.7.10"
val ktorVersion = "2.1.3"

plugins {
    kotlin("jvm") version "1.7.10"
    application
    id("org.openjfx.javafxplugin") version "0.0.7"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "hu.span"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

javafx{
    modules("javafx.base", "javafx.graphics", "javafx.controls")
}


dependencies {
    implementation("com.catan:sdk:0.3-snapshot")
    testImplementation(kotlin("test"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("io.ktor:ktor-network:$ktorVersion")
    implementation("io.ktor:ktor-server-websockets:$ktorVersion")
    implementation("no.tornado:tornadofx:1.7.20")
}


tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

application {
    mainClass.set("MainKt")
}