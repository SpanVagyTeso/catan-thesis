group = "com.catan"
version = "1.0-SNAPSHOT"

plugins{
    kotlin("jvm") version "1.7.10"
}

repositories {
    mavenCentral()
    mavenLocal()
}

tasks.register<JavaExec>("runClient"){
    group = "runStuff"
    dependsOn(":catan-client:build")
    classpath = files("catan-client/build/libs/catan-client-1.0-SNAPSHOT-all.jar")

}

tasks.register<JavaExec>("runServer"){
    group = "runStuff"
    dependsOn(":catan-server:build")
    classpath = files("catan-server/build/libs/catan-server-1.0-SNAPSHOT-all.jar")

}