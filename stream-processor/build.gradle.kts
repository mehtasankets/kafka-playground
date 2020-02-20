import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.21"
}

group = "com.mehtasan"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.21")
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")
    compile("org.apache.kafka:kafka-clients:2.4.0")
    compile("org.apache.kafka:kafka-streams:2.4.0")
    compile("com.fasterxml.jackson.core:jackson-databind:2.9.6")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.6")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}