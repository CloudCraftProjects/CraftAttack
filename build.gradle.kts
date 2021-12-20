plugins {
    `java-library`
    `maven-publish`

    id("xyz.jpenilla.run-paper") version "1.0.4"
}

group = "tk.booky"
version = "1.8.2"

repositories {
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://jitpack.io/")
}

dependencies {
    api("io.papermc.paper:paper-api:1.18.1-R0.1-SNAPSHOT")
    api("dev.jorel.CommandAPI:commandapi-core:6.5.3")
    api("com.mojang:brigadier:1.0.18")
    api("net.luckperms:api:5.3")
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications.create<MavenPublication>("maven") {
        artifactId = project.name.toLowerCase()
        from(components["java"])
    }
}

tasks {
    runServer {
        minecraftVersion("1.18.1")
    }

    processResources {
        inputs.property("version", project.version)
        filesMatching("plugin.yml") {
            expand("version" to project.version)
        }
    }
}
