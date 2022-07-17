plugins {
    id("java-library")
    id("maven-publish")
    id("xyz.jpenilla.run-paper") version "1.0.6"
}

group = "dev.booky"
version = "1.9.1"

repositories {
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    api("io.papermc.paper:paper-api:1.19-R0.1-SNAPSHOT")
    api("dev.jorel:commandapi-core:8.4.0")
    api("com.mojang:brigadier:1.0.18")
}

java {
    withSourcesJar()
}

publishing {
    publications.create<MavenPublication>("maven") {
        artifactId = project.name.toLowerCase()
        from(components["java"])
    }
}

tasks {
    runServer {
        minecraftVersion("1.19")
    }

    processResources {
        inputs.property("version", project.version)
        filesMatching("plugin.yml") {
            expand("version" to project.version)
        }
    }
}
