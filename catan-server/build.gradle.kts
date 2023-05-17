import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion = "1.7.10"
val ktorVersion = "2.1.3"

plugins {
    id("kotlin")
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "hu.span"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()

}


dependencies {
    implementation("com.catan:sdk:0.3-snapshot")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("io.ktor:ktor-network:$ktorVersion")
    implementation("io.ktor:ktor-server-websockets:$ktorVersion")
    implementation("com.google.code.gson:gson:2.10.1")

    implementation("org.hibernate:hibernate-core:6.1.7.Final")
    implementation("org.hibernate:hibernate-testing:6.1.7.Final")
    implementation("org.postgresql:postgresql:42.5.4")

    testImplementation("io.kotest:kotest-runner-junit5:5.5.5")
    testImplementation("io.kotest:kotest-assertions-core:5.5.5")
    testImplementation("io.mockk:mockk:1.13.4")
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

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
