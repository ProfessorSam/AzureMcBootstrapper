plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.gh.professorsam.azmc"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.logging.log4j:log4j-core:2.23.1")
    implementation("com.squareup.okhttp:okhttp:2.7.5")
    implementation("com.googlecode.json-simple:json-simple:1.1.1")
    implementation("io.github.cdimascio:dotenv-java:3.0.0")
    implementation("net.lingala.zip4j:zip4j:2.11.5")
    implementation("com.azure:azure-storage-blob:12.26.0")
}

tasks.compileJava {
    options.encoding = "UTF-8"
}

tasks.shadowJar {
    manifest {
        attributes("Main-Class" to "com.gh.professorsam.azmc.AzureMCBootstrapper")
    }
}