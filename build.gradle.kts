plugins {
    kotlin("jvm") version "1.5.21"
    java
}

group = "roflanbuldyga.cheapsms"
version = "0.1"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":cheapsms-api"))

    val ktbVersion = "1.3.8"
    implementation("com.github.elbekD:kt-telegram-bot:${ktbVersion}")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

val compileKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}