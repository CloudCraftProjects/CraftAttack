plugins {
    id("java-library")
    id("maven-publish")

    id("xyz.jpenilla.run-paper") version "1.0.6"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "dev.booky"
version = "1.10.2"

repositories {
    // TODO: find an actual repository for this
    mavenLocal {
        content {
            includeGroup("dev.booky")
        }
    }

    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    compileOnlyApi("io.papermc.paper:paper-api:1.19.2-R0.1-SNAPSHOT")
    compileOnlyApi("com.mojang:brigadier:1.0.18")

    compileOnlyApi("org.spongepowered:configurate-yaml:4.1.2")
    compileOnlyApi("dev.jorel:commandapi-core:8.5.1")

    api("org.bstats:bstats-bukkit:3.0.0")

    // needs to be published to maven local manually
    compileOnlyApi("dev.booky:cloudchat:1.1.1")
}

java {
    withSourcesJar()
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

publishing {
    publications.create<MavenPublication>("maven") {
        artifactId = project.name.toLowerCase()
        from(components["java"])
    }
}

tasks {
    runServer {
        minecraftVersion("1.19.2")
    }

    processResources {
        inputs.property("version", project.version)
        filesMatching("plugin.yml") {
            expand("version" to project.version)
        }
    }

    shadowJar {
        relocate("org.bstats", "dev.booky.craftattack.bstats")
    }

    build {
        dependsOn(shadowJar)
    }
}
