import me.modmuss50.mpp.ModPublishExtension
import me.modmuss50.mpp.PublishModTask

plugins {
    id("java-library")
    id("maven-publish")

    alias(libs.plugins.pluginyml.bukkit)
    alias(libs.plugins.run.paper)
    alias(libs.plugins.shadow)
    alias(libs.plugins.publishing)
}

group = "dev.booky"

val plugin: Configuration by configurations.creating {
    isTransitive = false
}

repositories {
    mavenLocal {
        content {
            includeGroup("dev.jorel")
        }
    }
    maven("https://repo.cloudcraftmc.de/public/")
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.luckperms.api)

    implementation(libs.bstats.bukkit)

    // downloaded at runtime using library loader
    sequenceOf(
        libs.caffeine
    ).forEach {
        compileOnlyApi(it)
        library(it)
    }

    compileOnlyApi(libs.cloudcore)
    compileOnly(libs.launchplates)
    compileOnly(libs.commandapi.paper.core)

    // testserver dependency plugins (maven)
    plugin(variantOf(libs.cloudcore) { classifier("all") })
    plugin(variantOf(libs.launchplates) { classifier("all") })
    plugin(libs.commandapi.paper.plugin)
}

java {
    withSourcesJar()
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
        vendor = JvmVendorSpec.ADOPTIUM
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
    apiVersion = "1.21.9"
    authors = listOf("booky10")
    depend = listOf("CommandAPI", "CloudCore")
    softDepend = listOf("LuckPerms", "LaunchPlates")
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD
}

tasks {
    runServer {
        minecraftVersion("1.21.9")

        pluginJars.from(plugin.resolve())
        downloadPlugins {
            github(
                "PaperMC", "Debuggery",
                "v${libs.versions.debuggery.get()}",
                "debuggery-bukkit-${libs.versions.debuggery.get()}.jar"
            )

            // not available on modrinth, github or hangar
            url("https://download.luckperms.net/1605/bukkit/loader/LuckPerms-Bukkit-5.5.16.jar")
        }
    }

    withType<Jar> {
        // no spigot mappings are used, disable useless remapping step
        manifest.attributes("paperweight-mappings-namespace" to "mojang")
    }

    shadowJar {
        relocate("org.bstats", "${project.group}.craftattack.bstats")
    }

    assemble {
        dependsOn(shadowJar)
    }
}

configure<ModPublishExtension> {
    val repositoryName = "CloudCraftProjects/CraftAttack"
    changelog = "See https://github.com/$repositoryName/releases/tag/v${project.version}"
    type = if (project.version.toString().endsWith("-SNAPSHOT")) BETA else STABLE
    dryRun = !hasProperty("noDryPublish")

    file = tasks.named<Jar>("shadowJar").flatMap { it.archiveFile }
    additionalFiles.from(tasks.named<Jar>("sourcesJar"))

    github {
        accessToken = providers.environmentVariable("GITHUB_API_TOKEN")
            .orElse(providers.gradleProperty("ccGithubToken"))

        displayName = "${rootProject.name} v${project.version}"

        repository = repositoryName
        commitish = "master"
        tagName = "v${project.version}"

        if (project != rootProject) {
            parent(rootProject.tasks.named("publishGithub"))
        }
    }
    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_API_TOKEN")
            .orElse(providers.gradleProperty("ccModrinthToken"))

        version = "${project.version}"
        displayName = "${rootProject.name} v${project.version}"
        modLoaders.add("paper")

        projectId = "xA2xhZUF"
        minecraftVersionRange {
            start = rootProject.libs.versions.paper.get().split("-")[0]
            end = "latest"
        }

        requires("commandapi", "cloudcore")
        // even though luckperms doesn't publish paper jars on modrinth, add it as optional
        optional("launchplates", "luckperms")
    }
}

tasks.withType<PublishModTask> {
    dependsOn(tasks.named<Jar>("shadowJar"))
    dependsOn(tasks.named<Jar>("sourcesJar"))
}
