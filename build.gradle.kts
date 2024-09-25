plugins {
    id("java-library")
    id("maven-publish")

    alias(libs.plugins.pluginyml.bukkit)
    alias(libs.plugins.run.paper)
    alias(libs.plugins.shadow)
}

group = "dev.booky"
version = "1.10.4-SNAPSHOT"

val plugin: Configuration by configurations.creating {
    isTransitive = false
}

repositories {
    maven("https://repo.cloudcraftmc.de/public/")
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnlyApi(libs.luckperms.api)

    implementation(libs.bstats.bukkit)

    // downloaded at runtime using library loader
    sequenceOf(
        libs.caffeine
    ).forEach {
        compileOnlyApi(it)
        library(it)
    }

    compileOnlyApi(libs.cloudcore)
    compileOnlyApi(libs.launchplates)
    compileOnly(libs.commandapi.bukkit.core)

    // testserver dependency plugins (maven)
    plugin(variantOf(libs.cloudcore) { classifier("all") })
    plugin(variantOf(libs.launchplates) { classifier("all") })
    plugin(libs.commandapi.bukkit.plugin)
}

java {
    withSourcesJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        artifactId = project.name.lowercase()
        from(components["java"])
    }
}

bukkit {
    main = "$group.craftattack.CaMain"
    apiVersion = "1.20"
    authors = listOf("booky10")
    depend = listOf("CommandAPI", "CloudCore", "LaunchPlates")
    softDepend = listOf("LuckPerms")
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD
}

tasks {
    runServer {
        minecraftVersion("1.20.2")

        pluginJars.from(plugin.resolve())
        downloadPlugins {
            hangar("CommandAPI", libs.versions.commandapi.get())
            github(
                "PaperMC", "Debuggery",
                "v${libs.versions.debuggery.get()}",
                "debuggery-bukkit-${libs.versions.debuggery.get()}.jar"
            )

            // not available on modrinth, github or hangar
            url("https://download.luckperms.net/1515/bukkit/loader/LuckPerms-Bukkit-5.4.102.jar")
        }
    }

    shadowJar {
        relocate("org.bstats", "${project.group}.craftattack.bstats")
    }

    assemble {
        dependsOn(shadowJar)
    }
}
