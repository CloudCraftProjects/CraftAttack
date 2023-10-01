plugins {
    id("java-library")
    id("maven-publish")

    alias(libs.plugins.pluginyml.bukkit)
    alias(libs.plugins.run.paper)
    alias(libs.plugins.shadow)
}

group = "dev.booky"
version = "1.10.3"

val plugin: Configuration by configurations.creating {
    isTransitive = false
}

repositories {
    maven("https://maven.pkg.github.com/CloudCraftProjects/*/") {
        name = "github"
        credentials(PasswordCredentials::class.java)
    }
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnlyApi(libs.luckperms.api)

    implementation(libs.bstats.bukkit)

    // need to be published to maven local manually
    compileOnlyApi(libs.cloudcore)
    compileOnlyApi(libs.launchplates)

    // testserver dependency plugins (maven)
    plugin(variantOf(libs.cloudcore) { classifier("all") })
    plugin(variantOf(libs.launchplates) { classifier("all") })
}

java {
    withSourcesJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
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
