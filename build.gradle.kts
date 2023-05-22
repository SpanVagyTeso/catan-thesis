group = "com.catan"
version = "1.0-SNAPSHOT"

plugins {
    kotlin("jvm") version "1.8.20"
}

repositories {
    mavenCentral()
    mavenLocal()
}

tasks.register<JavaExec>("runClient") {
    group = "runStuff"
    dependsOn(":catan-client:build")
    copy {
        from("catan-client/build/resources/main")
        into("build/resources")
        include("*.*")
    }
    classpath = files("catan-client/build/libs/catan-client-1.0-SNAPSHOT-all.jar")

}

tasks.register<JavaExec>("runServer") {
    group = "runStuff"
    dependsOn(":catan-server:build")
    copy {
        from("catan-server/build/resources/main")
        into("build/resources")
        include("*.*")
    }
    classpath = files("catan-server/build/libs/catan-server-1.0-SNAPSHOT-all.jar")

}

tasks.register<Zip>("zipThem") {
    group = "runStuff"
    delete("zip")
    dependsOn(":catan-server:build")
    dependsOn(":catan-client:build")
    copy {
        from(
            "catan-server/build/resources/main",
            "catan-client/build/resources/main",
        )
        into("zip/build/resources")
        include("*.*")
    }
    copy {
        from(
            "catan-server/build/libs/",
            "catan-client/build/libs/"
        )
        into("zip")
        include("*.*")
    }
    from("zip/")
    include("*.*", "*/*")



}

tasks.register<Task>("buildSDK") {
    group = "runStuff"
    dependsOn(":catan-sdk:build")
}
dependencies {
    implementation(kotlin("stdlib-jdk8"))
}
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
