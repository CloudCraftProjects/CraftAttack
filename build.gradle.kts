plugins {
    id("java-library")
    id("maven-publish")

    id("net.minecrell.plugin-yml.bukkit") version "0.5.3"
    id("xyz.jpenilla.run-paper") version "1.0.6"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "dev.booky"
version = "1.10.3"

repositories {
    // TODO: find an actual repository for this
    mavenLocal {
        content {
            includeGroup("dev.booky")
        }
    }

    maven("https://s01.oss.sonatype.org/content/repositories/snapshots") {
        content {
            includeGroup("dev.jorel")
        }
    }

    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    compileOnlyApi("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")

    api("org.bstats:bstats-bukkit:3.0.2")

    // need to be published to maven local manually
    compileOnlyApi("dev.booky:cloudchat:1.1.1") {
        exclude("io.papermc.paper")
    }
    compileOnlyApi("dev.booky:launchplates:1.0.0") {
        exclude("io.papermc.paper")
        exclude("org.bstats")
    }
    compileOnlyApi("dev.booky:cloudcore:1.0.0") {
        exclude("io.papermc.paper")
        exclude("org.bstats")
    }
}

java {
    withSourcesJar()
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

publishing {
    publications.create<MavenPublication>("maven") {
        artifactId = project.name.lowercase()
        from(components["java"])
    }
}

bukkit {
    main = "$group.craftattack.CaMain"
    apiVersion = "1.19"
    authors = listOf("booky10")
    depend = listOf("CloudCore", "LaunchPlates")
    softDepend = listOf("CloudChat")
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD
}

tasks {
    runServer {
        minecraftVersion("1.19.4")
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

    assemble {
        dependsOn(shadowJar)
    }
}
