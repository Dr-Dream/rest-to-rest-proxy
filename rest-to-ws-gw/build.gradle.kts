plugins {
    kotlin("jvm") version "1.7.20"
    kotlin("plugin.allopen") version "1.7.20"
    id("io.quarkus")
}

repositories {
    mavenCentral()
    mavenLocal()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:quarkus-camel-bom:${quarkusPlatformVersion}"))
    implementation(project(":rest-to-rest-proxy-model"))
    implementation("org.apache.camel.quarkus:camel-quarkus-vertx")
    implementation("org.apache.camel.quarkus:camel-quarkus-log")
    implementation("org.apache.camel.quarkus:camel-quarkus-vertx-websocket")
    implementation("org.apache.camel.quarkus:camel-quarkus-jackson")
    implementation("org.apache.camel.quarkus:camel-quarkus-seda")
    implementation("org.apache.camel.quarkus:camel-quarkus-direct")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-jackson")
    implementation("io.quarkus:quarkus-config-yaml")
    implementation("io.quarkus:quarkus-reactive-routes")
    implementation("io.quarkus:quarkus-resteasy-jackson")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.quarkus:quarkus-arc")
    testImplementation("io.quarkus:quarkus-junit5")
}

group = "com.dm.net.rest.proxy"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

allOpen {
    annotation("javax.ws.rs.Path")
    annotation("javax.enterprise.context.ApplicationScoped")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
    kotlinOptions.javaParameters = true
}
