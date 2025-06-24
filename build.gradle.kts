import java.util.*
import java.text.NumberFormat
import java.text.ParseException

plugins {
    java
    id("fabric-loom") version ("1.10-SNAPSHOT")
    id("com.gradleup.shadow") version ("8.3.1")
    id("io.freefair.lombok") version ("8.11")
}

base {
    archivesName.set(project.name)
}

loom {
    log4jConfigs.from(file(("log-config.xml")))
    accessWidenerPath.set(project.file("src/main/resources/skyblockaddons.accesswidener"))
    runConfigs {
        getByName("client") {
            vmArg("-Xmx4G")
            property("mixin.debug", "true")
            property("devauth.enabled", "true")
            property("sba.data.online", "false")
        }
        remove(getByName("server"))
    }
    mixin.useLegacyMixinAp = false
}

repositories {
    // Add repositories to retrieve artifacts from in here.
    // You should only use this when depending on other mods because
    // Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
    // See https://docs.gradle.org/current/userguide/declaring_repositories.html
    // for more information about repositories.
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    maven("https://repo.hypixel.net/repository/Hypixel/")
    //maven("https://repo.nea.moe/releases")
    maven("https://jitpack.io") {
        content {
            includeGroupByRegex("com\\.github\\..*")
        }
    }
    maven("https://api.modrinth.com/maven") {
        content {
            includeGroup("maven.modrinth")
        }
    }
    maven("https://maven.parchmentmc.org")
}

val bundle : Configuration by configurations.creating {
    configurations.modImplementation.get().extendsFrom(this)
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${properties["minecraft_version"]}")
//	mappings("net.fabricmc:yarn:${properties["yarn_mappings"]}")
    mappings(loom.layered {
        officialMojangMappings()
        if (properties["parchment_version"] != null) {
            parchment("org.parchmentmc.data:parchment-${properties["minecraft_version"]}:${properties["parchment_version"]}@zip")
        }
    })
    modImplementation("net.fabricmc:fabric-loader:${properties["loader_version"]}")

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation("net.fabricmc.fabric-api:fabric-api:${properties["fabric_version"]}")

    modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.1")
    implementation ("net.hypixel:mod-api:1.0.1")
    //bundle("moe.nea:libautoupdate:1.3.1")
    bundle("com.github.nea89o:libautoupdate:841d9f7e78")
    // Discord RPC for Java https://github.com/jagrosh/DiscordIPC
    bundle("io.github.cdagaming:DiscordIPC:0.10.5") {
        exclude(module = "log4j")
        because("Different version conflicts with Minecraft's Log4J")
        exclude(module = "gson")
        because("Different version conflicts with Minecraft's GSON")
    }
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.1")
}

tasks.withType(JavaCompile::class).configureEach {
    options.compilerArgs.add("-Xlint:unchecked")
    options.compilerArgs.add("-Xlint:deprecation")
    options.encoding = "UTF-8"
}

tasks.remapJar {
    input = tasks.shadowJar.get().archiveFile
    archiveFileName = "${project.name}-${version}-for-MC-${properties["minecraft_version"]}.jar"
}

tasks.processResources {
    dependsOn("copyLicenses")

    if (project.hasProperty("runningOnCi")) {
        val nf = NumberFormat.getIntegerInstance(Locale.US)
        val buildNumber = project.property("buildNumber")
        val runAttempt = project.property("runAttempt")
        val includeRunAttempt = nf.parse(runAttempt as String).toInt() > 1

        try {
            if (includeRunAttempt) {
                project.setProperty("buildNumber", "${buildNumber}.${nf.parse(runAttempt).toInt() - 1}")
            }
            project.version = "${project.version}+" + project.property("buildNumber")
        } catch (e: ParseException) {
            throw InvalidUserDataException("Build number could not be parsed (${e.message})", e)
        }
    }

    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand(mapOf("version" to project.version))
    }
}

tasks.register("copyLicenses", Copy::class) {
    from(project.projectDir) {
        include(
            "LICENSE",
            "dependencyLicenses/**"
        )
    }
    from(".github/docs/NOTICES.md").into(project.projectDir)
    sourceSets.main.get().output.resourcesDir?.let { into(it) }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.jar {
    destinationDirectory.set(layout.buildDirectory.dir("intermediates"))
    from("LICENSE") {
        rename { "${it}_${base.archivesName.get()}"}
    }
    manifest.attributes.run {
        this["Main-Class"] = "SkyblockAddonsInstallerFrame"
    }
}

tasks.shadowJar {
    destinationDirectory.set(layout.buildDirectory.dir("intermediates"))
    configurations = listOf(bundle)

    val basePackage = "${project.group}.${project.name.lowercase(Locale.US)}"
    relocate("com.jagrosh.discordipc", "${basePackage}.discordipc")
//	relocate("net.hypixel.modapi", "${basePackage}.modapi")
    relocate("moe.nea.libautoupdate", "${basePackage}.libautoupdate")
}

tasks.assemble.get().dependsOn(tasks.remapJar)